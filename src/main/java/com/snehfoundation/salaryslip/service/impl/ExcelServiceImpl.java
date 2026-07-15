package com.snehfoundation.salaryslip.service.impl;

import com.snehfoundation.salaryslip.dto.ExcelParseResult;
import com.snehfoundation.salaryslip.dto.ExcelRowError;
import com.snehfoundation.salaryslip.exception.InvalidExcelFormatException;
import com.snehfoundation.salaryslip.model.Employee;
import com.snehfoundation.salaryslip.service.ExcelService;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads the SNEH Foundation salary data workbook.
 * <p>
 * Header lookup is name-based (not fixed column indices), so the admin can
 * reorder columns in the sheet without breaking the upload. Column names are
 * matched case-insensitively with whitespace trimmed.
 * <p>
 * A row with a problem (missing name/ID, unparsable amount, etc.) is skipped
 * and reported in {@link ExcelParseResult#rowErrors()} rather than failing
 * the entire upload -- one bad row in a 70-row sheet shouldn't block the other 69.
 */
@Service
public class ExcelServiceImpl implements ExcelService {

    // Required headers -- upload is rejected outright if any of these are missing.
    private static final String[] REQUIRED_HEADERS = {
            "Month", "Employee Name", "Employee ID", "Project", "Designation",
            "Aadhaar", "PAN", "Reporting Branch", "Days", "Basic", "HRA",
            "Special Allowance", "Conveyance", "Total Earnings", "Net Pay", "Payment Mode"
    };

    @Override
    public ExcelParseResult parse(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new InvalidExcelFormatException("No file was uploaded, or the file is empty.");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !(fileName.toLowerCase().endsWith(".xlsx") || fileName.toLowerCase().endsWith(".xls"))) {
            throw new InvalidExcelFormatException(
                    "Unsupported file type. Please upload a .xlsx or .xls Excel file.");
        }

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() < 2) {
                throw new InvalidExcelFormatException(
                        "The Excel sheet appears to be empty (no header row + data rows found).");
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            Map<String, Integer> columnIndex = buildColumnIndex(headerRow);
            validateRequiredHeaders(columnIndex);

            DataFormatter formatter = new DataFormatter();
            List<Employee> employees = new ArrayList<>();
            List<ExcelRowError> rowErrors = new ArrayList<>();

            for (int rowNum = headerRow.getRowNum() + 1; rowNum <= sheet.getLastRowNum(); rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null || isRowBlank(row, formatter)) {
                    continue; // silently skip trailing/blank rows -- not an error
                }

                // Excel row number as a human would see it (1-indexed, including header)
                int humanRowNumber = rowNum + 1;
                try {
                    Employee employee = mapRowToEmployee(row, columnIndex, formatter);
                    employees.add(employee);
                } catch (RowParseException rpe) {
                    rowErrors.add(new ExcelRowError(humanRowNumber, rpe.getMessage()));
                }
            }

            return new ExcelParseResult(employees, rowErrors);

        } catch (InvalidExcelFormatException e) {
            throw e;
        } catch (Exception e) {
            // Anything POI throws for a genuinely corrupt/unreadable workbook lands here
            throw new InvalidExcelFormatException(
                    "Could not read the Excel file. It may be corrupted or in an unsupported format.", e);
        }
    }

    private Map<String, Integer> buildColumnIndex(Row headerRow) {
        Map<String, Integer> index = new LinkedHashMap<>();
        DataFormatter formatter = new DataFormatter();
        for (Cell cell : headerRow) {
            String header = formatter.formatCellValue(cell).trim();
            if (!header.isEmpty()) {
                index.put(header.toLowerCase(), cell.getColumnIndex());
            }
        }
        return index;
    }

    private void validateRequiredHeaders(Map<String, Integer> columnIndex) {
        List<String> missing = new ArrayList<>();
        for (String required : REQUIRED_HEADERS) {
            if (!columnIndex.containsKey(required.toLowerCase())) {
                missing.add(required);
            }
        }
        if (!missing.isEmpty()) {
            throw new InvalidExcelFormatException(
                    "The uploaded Excel is missing required column(s): " + String.join(", ", missing)
                            + ". Please use the standard SNEH Foundation salary data template.");
        }
    }

    private boolean isRowBlank(Row row, DataFormatter formatter) {
        for (Cell cell : row) {
            if (!formatter.formatCellValue(cell).trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private Employee mapRowToEmployee(Row row, Map<String, Integer> columnIndex, DataFormatter formatter) {
        String name = getString(row, columnIndex, "Employee Name", formatter);
        String employeeId = getString(row, columnIndex, "Employee ID", formatter);

        if (isBlank(name) || isBlank(employeeId)) {
            throw new RowParseException("Missing Employee Name or Employee ID.");
        }

        try {
            return Employee.builder()
                    .month(getString(row, columnIndex, "Month", formatter))
                    .name(name)
                    .employeeId(employeeId)
                    .project(getString(row, columnIndex, "Project", formatter))
                    .designation(getString(row, columnIndex, "Designation", formatter))
                    .aadhaar(getString(row, columnIndex, "Aadhaar", formatter))
                    .pan(getString(row, columnIndex, "PAN", formatter))
                    .reportingBranch(getString(row, columnIndex, "Reporting Branch", formatter))
                    .daysCalculated(getInteger(row, columnIndex, "Days"))
                    .basicSalary(getBigDecimal(row, columnIndex, "Basic"))
                    .hra(getBigDecimal(row, columnIndex, "HRA"))
                    .specialAllowance(getBigDecimal(row, columnIndex, "Special Allowance"))
                    .conveyance(getBigDecimal(row, columnIndex, "Conveyance"))
                    .reimbursement(BigDecimal.ZERO)
                    .totalEarnings(getBigDecimal(row, columnIndex, "Total Earnings"))
                    .netPay(getBigDecimal(row, columnIndex, "Net Pay"))
                    .paymentMode(getString(row, columnIndex, "Payment Mode", formatter))
                    .build();
        } catch (RowParseException rpe) {
            throw rpe;
        } catch (Exception e) {
            throw new RowParseException("Could not parse one or more numeric fields (Basic/HRA/Conveyance/Total Earnings/Net Pay).");
        }
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private Cell getCell(Row row, Map<String, Integer> columnIndex, String header) {
        Integer idx = columnIndex.get(header.toLowerCase());
        if (idx == null) {
            return null;
        }
        return row.getCell(idx);
    }

    private String getString(Row row, Map<String, Integer> columnIndex, String header, DataFormatter formatter) {
        Cell cell = getCell(row, columnIndex, header);
        if (cell == null) {
            return null;
        }
        // DataFormatter avoids scientific notation for long numeric-looking strings
        // like Aadhaar numbers, and safely stringifies any cell type.
        String value = formatter.formatCellValue(cell).trim();
        return value.isEmpty() ? null : value;
    }

    private BigDecimal getBigDecimal(Row row, Map<String, Integer> columnIndex, String header) {
        Cell cell = getCell(row, columnIndex, header);
        if (cell == null) {
            return BigDecimal.ZERO;
        }
        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }
        String raw = new DataFormatter().formatCellValue(cell).trim().replace(",", "");
        if (raw.isEmpty()) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(raw);
        } catch (NumberFormatException nfe) {
            throw new RowParseException("Column '" + header + "' has a non-numeric value: '" + raw + "'.");
        }
    }

    private Integer getInteger(Row row, Map<String, Integer> columnIndex, String header) {
        BigDecimal value = getBigDecimal(row, columnIndex, header);
        return value.intValue();
    }

    /** Internal-only signal for "skip this row, here's why" -- never escapes this class. */
    private static class RowParseException extends RuntimeException {
        RowParseException(String message) {
            super(message);
        }
    }
}