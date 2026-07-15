package com.snehfoundation.salaryslip.dto;

/**
 * A single row that couldn't be parsed into an {@link com.snehfoundation.salaryslip.model.Employee}.
 * Row-level problems (a blank required cell, a non-numeric salary figure, etc.)
 * don't abort the whole upload -- the row is skipped and reported here so the
 * admin can go fix that one row in the spreadsheet.
 */
public record ExcelRowError(int excelRowNumber, String reason) {
}