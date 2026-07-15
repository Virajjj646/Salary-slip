package com.snehfoundation.salaryslip.controller;

import com.snehfoundation.salaryslip.dto.ExcelParseResult;
import com.snehfoundation.salaryslip.service.ExcelService;
import com.snehfoundation.salaryslip.util.EmployeeSessionHelper;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;

/**
 * Handles the Excel upload -- the only entry point through which employee
 * data enters this application -- and the "Clear Uploaded Data" action.
 * <p>
 * The file is read straight from {@link MultipartFile#getInputStream()} by
 * {@link ExcelService} and never written to disk. The parsed employee list
 * is stored in the current user's own {@link HttpSession} via
 * {@link EmployeeSessionHelper} -- never in any shared/singleton service --
 * so one user's uploaded data is never visible to another user's session.
 * <p>
 * Any exception thrown here (bad format, unreadable file, file too large) is
 * caught by {@link com.snehfoundation.salaryslip.exception.GlobalExceptionHandler}.
 */
@Controller
@RequiredArgsConstructor
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    private final ExcelService excelService;
    private final EmployeeSessionHelper employeeSessionHelper;

    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) throws IOException {

        ExcelParseResult result = excelService.parse(file);

        // Per spec: a new upload replaces this session's previous batch entirely.
        employeeSessionHelper.setEmployees(session, result.employees());

        log.info("Excel upload processed for session {}: {} employees loaded, {} row error(s).",
                session.getId(), result.successCount(), result.rowErrors().size());

        redirectAttributes.addFlashAttribute("uploadSuccess",
                result.successCount() + " employee record(s) loaded successfully.");

        if (result.hasRowErrors()) {
            redirectAttributes.addFlashAttribute("uploadRowErrors", result.rowErrors());
        }

        return "redirect:/";
    }

    /**
     * "Clear Uploaded Data" -- removes this session's employee list so the
     * dashboard behaves as if nothing was ever uploaded. Only touches the
     * "employees" session attribute; nothing else in the session is affected.
     */
    @PostMapping("/clear-data")
    public String clearData(HttpSession session, RedirectAttributes redirectAttributes) {
        employeeSessionHelper.clear(session);
        log.info("Uploaded data cleared for session {}.", session.getId());

        redirectAttributes.addFlashAttribute("uploadSuccess", "Uploaded data cleared.");
        return "redirect:/";
    }
}