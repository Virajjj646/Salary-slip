package com.snehfoundation.salaryslip.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * This is a server-rendered Thymeleaf app, not a REST API, so exceptions
 * don't map to JSON error bodies -- they map to a flash-attribute error
 * message and a redirect back to the dashboard, which is where every
 * upload/generate/download action is triggered from.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(InvalidExcelFormatException.class)
    public String handleInvalidExcel(InvalidExcelFormatException ex, RedirectAttributes redirectAttributes) {
        log.warn("Rejected Excel upload: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("uploadError", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleFileTooLarge(MaxUploadSizeExceededException ex, RedirectAttributes redirectAttributes) {
        log.warn("Upload rejected -- file too large: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("uploadError",
                "That file is too large. Please upload an Excel file under 10MB.");
        return "redirect:/";
    }

    @ExceptionHandler(SalarySlipNotFoundException.class)
    public String handleSlipNotFound(SalarySlipNotFoundException ex, RedirectAttributes redirectAttributes) {
        log.warn("Salary slip lookup failed: {}", ex.getMessage());
        redirectAttributes.addFlashAttribute("uploadError", ex.getMessage());
        return "redirect:/";
    }

    @ExceptionHandler(Exception.class)
    public String handleUnexpected(Exception ex, RedirectAttributes redirectAttributes) {
        log.error("Unexpected error", ex);
        redirectAttributes.addFlashAttribute("uploadError",
                "Something went wrong processing that request. Please try again.");
        return "redirect:/";
    }
}