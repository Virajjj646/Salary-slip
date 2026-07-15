package com.snehfoundation.salaryslip.exception;

/**
 * Thrown when the uploaded file isn't a usable Excel file at all: wrong
 * extension, corrupted workbook, or missing required header columns.
 * This aborts the entire upload — nothing is stored, the previous
 * in-memory batch is left untouched.
 */
public class InvalidExcelFormatException extends RuntimeException {

    public InvalidExcelFormatException(String message) {
        super(message);
    }

    public InvalidExcelFormatException(String message, Throwable cause) {
        super(message, cause);
    }
}