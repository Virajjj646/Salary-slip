package com.snehfoundation.salaryslip.controller;

import com.snehfoundation.salaryslip.dto.ExcelParseResult;
import com.snehfoundation.salaryslip.service.EmployeeStore;
import com.snehfoundation.salaryslip.service.ExcelService;
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
 * data enters this application. The file is read straight from
 * {@link MultipartFile#getInputStream()} by {@link ExcelService} and never
 * written to disk -- this app is fully stateless aside from the in-memory
 * {@link EmployeeStore}.
 * <p>
 * Any exception thrown here (bad format, unreadable file, file too large) is
 * caught by {@link com.snehfoundation.salaryslip.exception.GlobalExceptionHandler}.
 */
@Controller
@RequiredArgsConstructor
public class FileUploadController {

    private static final Logger log = LoggerFactory.getLogger(FileUploadController.class);

    private final ExcelService excelService;
    private final EmployeeStore employeeStore;

    @PostMapping("/upload")
    public String uploadExcel(@RequestParam("file") MultipartFile file,
                              RedirectAttributes redirectAttributes) throws IOException {

        ExcelParseResult result = excelService.parse(file);

        // Per spec: a new upload replaces the previous batch entirely.
        employeeStore.replaceAll(result.employees());

        log.info("Excel upload processed: {} employees loaded, {} row error(s).",
                result.successCount(), result.rowErrors().size());

        redirectAttributes.addFlashAttribute("uploadSuccess",
                result.successCount() + " employee record(s) loaded successfully.");

        if (result.hasRowErrors()) {
            redirectAttributes.addFlashAttribute("uploadRowErrors", result.rowErrors());
        }

        return "redirect:/";
    }
}