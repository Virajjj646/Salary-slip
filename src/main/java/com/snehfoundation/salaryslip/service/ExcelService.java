package com.snehfoundation.salaryslip.service;

import com.snehfoundation.salaryslip.dto.ExcelParseResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Parses an uploaded Excel workbook (.xlsx/.xls) into {@link com.snehfoundation.salaryslip.model.Employee}
 * objects using Apache POI.
 */
public interface ExcelService {

    /**
     * Parses the given workbook.
     *
     * @throws com.snehfoundation.salaryslip.exception.InvalidExcelFormatException
     *         if the file isn't a real workbook, has no data sheet, or is missing
     *         one or more required header columns. Nothing is returned in this case.
     * @throws IOException if the file can't be read at all.
     */
    ExcelParseResult parse(MultipartFile file) throws IOException;
}