package com.snehfoundation.salaryslip.service.impl;

import com.snehfoundation.salaryslip.model.Employee;
import com.snehfoundation.salaryslip.service.EmployeeStore;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * In-memory, thread-safe implementation of {@link EmployeeStore}.
 * <p>
 * Uses an {@link AtomicReference} holding an immutable list so that a full
 * batch replace (new Excel upload) is a single atomic pointer swap — readers
 * never see a partially-updated list, and there's no need for explicit locking.
 * <p>
 * IMPORTANT (Render free-tier note): this data lives only in the JVM's heap.
 * It does NOT survive an application restart/redeploy -- the instance
 * spins down after inactivity on Render's free tier, and this in-memory
 * batch is gone on the next cold start. This is expected behavior for this
 * stateless design, not a bug -- re-upload the Excel after a cold start.
 */
@Component
public class InMemoryEmployeeStore implements EmployeeStore {

    private final AtomicReference<List<Employee>> currentBatch =
            new AtomicReference<>(Collections.emptyList());

    @Override
    public void replaceAll(List<Employee> employees) {
        List<Employee> immutableCopy = employees == null
                ? Collections.emptyList()
                : List.copyOf(employees);
        currentBatch.set(immutableCopy);
    }

    @Override
    public List<Employee> getAll() {
        return currentBatch.get();
    }

    @Override
    public Optional<Employee> findByEmployeeId(String employeeId) {
        if (employeeId == null) {
            return Optional.empty();
        }
        return currentBatch.get().stream()
                .filter(e -> employeeId.equalsIgnoreCase(e.getEmployeeId()))
                .findFirst();
    }

    @Override
    public boolean isEmpty() {
        return currentBatch.get().isEmpty();
    }

    @Override
    public int count() {
        return currentBatch.get().size();
    }
}