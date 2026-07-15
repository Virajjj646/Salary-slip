package com.snehfoundation.salaryslip.service;

import com.snehfoundation.salaryslip.model.Employee;

import java.util.List;
import java.util.Optional;

/**
 * Holds the "current batch" of employees parsed from the most recently
 * uploaded Excel file. This is the entire persistence layer for the
 * application — there is intentionally no database.
 * <p>
 * Uploading a new Excel file replaces the previous batch entirely, per spec.
 * <p>
 * Implementations must be thread-safe: a dashboard read and an Excel
 * re-upload could plausibly happen concurrently (e.g. two admin browser tabs).
 */
public interface EmployeeStore {

    /**
     * Atomically replaces the entire in-memory batch with a new list,
     * e.g. after a fresh Excel upload.
     */
    void replaceAll(List<Employee> employees);

    /** Returns an immutable snapshot of the current batch. Empty list if nothing uploaded yet. */
    List<Employee> getAll();

    /** Looks up a single employee by ID within the current batch. */
    Optional<Employee> findByEmployeeId(String employeeId);

    /** True if no Excel has been uploaded yet (or the last upload was empty). */
    boolean isEmpty();

    int count();
}