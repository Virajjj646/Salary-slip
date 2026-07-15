package com.snehfoundation.salaryslip.exception;

/**
 * Thrown when a controller is asked to preview/generate/download a salary
 * slip for an employeeId that doesn't exist in the current in-memory batch,
 * or when a download is requested for a PDF that hasn't been generated yet.
 * Used starting in later modules (preview/generate/download endpoints);
 * declared now alongside the other exception types.
 */
public class SalarySlipNotFoundException extends RuntimeException {

    public SalarySlipNotFoundException(String message) {
        super(message);
    }
}