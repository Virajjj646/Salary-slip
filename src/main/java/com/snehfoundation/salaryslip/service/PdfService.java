package com.snehfoundation.salaryslip.service;

import com.snehfoundation.salaryslip.model.Employee;

import java.io.IOException;

/**
 * Renders the salary-slip Thymeleaf template to a PDF entirely in memory via
 * OpenHTMLToPDF -- nothing is ever written to disk.
 * <p>
 * Uses the exact same "salary-slip" template that {@code SalarySlipController}
 * uses for the browser preview -- what the admin previews is what gets generated.
 */
public interface PdfService {

    /**
     * Renders this employee's salary slip to PDF bytes, ready to be streamed
     * straight to an HTTP response (single download) or fed into a ZIP
     * archive (bulk download). Nothing is persisted anywhere.
     */
    byte[] generate(Employee employee) throws IOException;
}