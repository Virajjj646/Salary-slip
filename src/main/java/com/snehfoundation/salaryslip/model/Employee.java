package com.snehfoundation.salaryslip.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Domain object representing a single employee's salary record for a given month.
 * <p>
 * This is a direct, one-row-per-instance mapping of the uploaded Excel sheet.
 * There is no database and no shared application-level storage -- instances
 * of this class live only inside the uploading user's own {@code HttpSession}
 * (see {@link com.snehfoundation.salaryslip.util.EmployeeSessionHelper}), so
 * one user's data is never visible to another user.
 * <p>
 * Implements {@link Serializable} because it's stored as an HttpSession
 * attribute -- standard practice for session-scoped data, so the servlet
 * container can persist/replicate sessions without warnings if it ever needs to.
 * <p>
 * Column mapping (confirmed against the real SNEH Foundation dummy dataset):
 * Month | Employee Name | Employee ID | Project | Designation | Aadhaar | PAN |
 * Reporting Branch | Days | Basic | HRA | Special Allowance | Conveyance |
 * Total Earnings | Net Pay | Payment Mode
 * <p>
 * Note: the source Excel has NO explicit "Reimbursement" or "Deductions" columns.
 * - {@code reimbursement} defaults to {@link BigDecimal#ZERO} unless a future
 *   Excel version supplies it (kept as a field so the template/PDF layout,
 *   which does show a Reimbursement row, always has something safe to render).
 * - {@code deductions} is derived as (totalEarnings - netPay) rather than
 *   hardcoded to zero, so real deduction data will flow through correctly if
 *   it's ever present in the sheet.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    /** e.g. "May 2026" -- shown on the dashboard and the salary slip itself. */
    private String month;

    private String name;

    /** e.g. "E-001". Used together with name to build a collision-safe PDF filename. */
    private String employeeId;

    private String project;

    private String designation;

    /** Kept as String — Aadhaar numbers can have leading digits that must not be treated as a number. */
    private String aadhaar;

    private String pan;

    private String reportingBranch;

    private Integer daysCalculated;

    private BigDecimal basicSalary;

    private BigDecimal hra;

    private BigDecimal specialAllowance;

    private BigDecimal conveyance;

    /** Not present as its own column in the source Excel; defaults to zero. */
    @Builder.Default
    private BigDecimal reimbursement = BigDecimal.ZERO;

    private BigDecimal totalEarnings;

    private BigDecimal netPay;

    private String paymentMode;

    /**
     * Derived, not read from Excel: totalEarnings - netPay.
     * In the current dataset this is always zero, but computing it (rather than
     * hardcoding zero) means real deduction rows will be picked up correctly
     * if a future Excel includes them.
     */
    public BigDecimal getDeductions() {
        if (totalEarnings == null || netPay == null) {
            return BigDecimal.ZERO;
        }
        return totalEarnings.subtract(netPay);
    }

    /**
     * Collision-safe identifier used for PDF/ZIP-entry filenames, e.g.
     * "E-001_Archana_Patil". Several employees in the real dataset share the
     * same name across different months/IDs, so name alone is not safe as a
     * filename key.
     * <p>
     * Strips anything outside [A-Za-z0-9_-] -- not just whitespace -- so the
     * resulting filename is safe to save on Windows, macOS, or Linux
     * regardless of which OS the server runs on. Characters like
     * {@code \ / : * ? " < > |} are illegal in Windows filenames and would
     * otherwise cause a download to silently fail to save on a Windows machine.
     */
    public String getFileSafeIdentifier() {
        String safeName = sanitizeForFilename(name == null ? "Unknown" : name);
        String safeId = sanitizeForFilename(employeeId == null ? "NOID" : employeeId);
        return safeId + "_" + safeName;
    }

    private String sanitizeForFilename(String input) {
        String collapsedWhitespace = input.trim().replaceAll("\\s+", "_");
        return collapsedWhitespace.replaceAll("[^A-Za-z0-9_-]", "");
    }
}