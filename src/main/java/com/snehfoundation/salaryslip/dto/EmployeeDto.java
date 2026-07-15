package com.snehfoundation.salaryslip.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * What the dashboard table actually needs to render one row.
 * Deliberately smaller than {@link com.snehfoundation.salaryslip.model.Employee} --
 * the full salary breakdown (HRA, conveyance, etc.) belongs on the slip
 * preview/PDF, not the summary table.
 * <p>
 * There is no "status" concept anymore -- the app is fully stateless, so
 * Preview and Download are always available and never reflect a
 * previously-generated file.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeDto {

    private String month;
    private String employeeId;
    private String name;
    private String project;
    private String designation;
    private BigDecimal netPay;

    /** Used to build Preview/Download links without re-deriving it in the template. */
    private String fileSafeIdentifier;
}