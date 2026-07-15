package com.snehfoundation.salaryslip.controller;

import com.snehfoundation.salaryslip.exception.SalarySlipNotFoundException;
import com.snehfoundation.salaryslip.model.Employee;
import com.snehfoundation.salaryslip.service.PdfService;
import com.snehfoundation.salaryslip.service.ZipService;
import com.snehfoundation.salaryslip.util.EmployeeSessionHelper;
import com.snehfoundation.salaryslip.util.LogoProvider;
import com.snehfoundation.salaryslip.util.NumberToWordsConverter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

/**
 * Renders the salary slip HTML directly in the browser, and generates PDFs
 * (single or bulk-as-ZIP) entirely in memory. All employee lookups come from
 * the current user's own {@code HttpSession} via {@link EmployeeSessionHelper}
 * -- never from any shared/singleton service -- so one user can never
 * preview, download, or ZIP another user's uploaded salary data.
 */
@Controller
@RequiredArgsConstructor
public class SalarySlipController {

    private final EmployeeSessionHelper employeeSessionHelper;
    private final NumberToWordsConverter numberToWordsConverter;
    private final PdfService pdfService;
    private final ZipService zipService;
    private final LogoProvider logoProvider;

    @GetMapping("/preview/{employeeId}")
    public String preview(@PathVariable String employeeId, HttpSession session, Model model) {
        Employee employee = findEmployeeOrThrow(session, employeeId);

        model.addAttribute("employee", employee);
        model.addAttribute("amountInWords", numberToWordsConverter.toWords(employee.getNetPay()));
        model.addAttribute("logoDataUri", logoProvider.getLogoDataUri().orElse(null));

        return "salary-slip";
    }

    /**
     * Renders this employee's PDF on the spot and streams it back --
     * nothing is generated ahead of time and nothing is saved afterward.
     */
    @GetMapping("/download/{employeeId}")
    @ResponseBody
    public ResponseEntity<byte[]> download(@PathVariable String employeeId, HttpSession session) throws IOException {
        Employee employee = findEmployeeOrThrow(session, employeeId);

        byte[] pdfBytes = pdfService.generate(employee);
        String downloadName = buildDownloadFilename(employee);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + downloadName + "\"")
                .body(pdfBytes);
    }

    /**
     * Generates every employee in *this session's* list in memory and
     * streams them back as one ZIP.
     */
    @GetMapping("/download-all")
    public String downloadAllZip(HttpSession session, HttpServletResponse response,
                                 RedirectAttributes redirectAttributes) throws IOException {
        List<Employee> employees = employeeSessionHelper.getEmployees(session);

        if (employees.isEmpty()) {
            redirectAttributes.addFlashAttribute("uploadError",
                    "No employees loaded yet. Upload an Excel file first.");
            return "redirect:/";
        }

        byte[] zipBytes = zipService.createZipOfAll(employees);

        response.setContentType("application/zip");
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"SNEH_Salary_Slips.zip\"");
        response.setContentLength(zipBytes.length);
        response.getOutputStream().write(zipBytes);
        response.getOutputStream().flush();

        // Response already streamed and committed directly -- returning null
        // tells Spring MVC there's no view to resolve, rather than treating
        // the empty return value as a view name to look up.
        return null;
    }

    private Employee findEmployeeOrThrow(HttpSession session, String employeeId) {
        return employeeSessionHelper.findByEmployeeId(session, employeeId)
                .orElseThrow(() -> new SalarySlipNotFoundException(
                        "No employee found with ID: " + employeeId + ". It may have been cleared, replaced by a newer upload, or your session may have expired."));
    }

    /** Same [A-Za-z0-9_-] sanitization as Employee.getFileSafeIdentifier(), so downloads save cleanly on Windows/macOS/Linux alike. */
    private String buildDownloadFilename(Employee employee) {
        String safeName = employee.getName() == null ? "Unknown" : employee.getName().trim()
                .replaceAll("\\s+", "_").replaceAll("[^A-Za-z0-9_-]", "");
        String safeMonth = employee.getMonth() == null ? "Unspecified" : employee.getMonth().trim()
                .replaceAll("\\s+", "_").replaceAll("[^A-Za-z0-9_-]", "");
        return "SalarySlip_" + safeName + "_" + safeMonth + ".pdf";
    }
}