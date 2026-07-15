package com.snehfoundation.salaryslip.dto;

import com.snehfoundation.salaryslip.model.Employee;

import java.util.List;

/**
 * Outcome of {@code ExcelService.parse(...)}.
 * <p>
 * A header/format problem that makes the whole file unusable throws
 * {@link com.snehfoundation.salaryslip.exception.InvalidExcelFormatException}
 * instead -- this result type only represents a file whose headers were
 * valid, where zero or more individual data rows had problems.
 */
public record ExcelParseResult(List<Employee> employees, List<ExcelRowError> rowErrors) {

    public boolean hasRowErrors() {
        return rowErrors != null && !rowErrors.isEmpty();
    }

    public int successCount() {
        return employees == null ? 0 : employees.size();
    }
}