package com.snehfoundation.salaryslip.util;

import com.snehfoundation.salaryslip.model.Employee;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Centralizes how the current user's uploaded employee list is read from and
 * written to their own {@link HttpSession}.
 * <p>
 * This class is deliberately stateless -- it holds no fields except a
 * constant key name, and every method takes the caller's own {@code HttpSession}
 * as a parameter. The employee data itself lives only inside each user's
 * individual session object (owned by the servlet container, tied to their
 * session cookie), never in this bean. Because this bean has no data of its
 * own, being a singleton is completely safe -- there is nothing here for two
 * different users' requests to collide over.
 * <p>
 * This replaces the old {@code EmployeeStore}/{@code InMemoryEmployeeStore},
 * which held one shared list for the whole application -- the root cause of
 * one user being able to see another user's uploaded salary data.
 */
@Component
public class EmployeeSessionHelper {

    private static final String SESSION_KEY_EMPLOYEES = "employees";

    /** Replaces whatever employee list this session currently has, per spec: overwrite, never merge/append. */
    public void setEmployees(HttpSession session, List<Employee> employees) {
        List<Employee> immutableCopy = employees == null ? Collections.emptyList() : List.copyOf(employees);
        session.setAttribute(SESSION_KEY_EMPLOYEES, immutableCopy);
    }

    /** Returns this session's employee list, or an empty list if nothing has been uploaded (or the session is new/expired). */
    @SuppressWarnings("unchecked")
    public List<Employee> getEmployees(HttpSession session) {
        Object raw = session.getAttribute(SESSION_KEY_EMPLOYEES);
        if (raw instanceof List<?> list) {
            return (List<Employee>) list;
        }
        return Collections.emptyList();
    }

    /** Looks up a single employee by ID within *this session's* list only. */
    public Optional<Employee> findByEmployeeId(HttpSession session, String employeeId) {
        if (employeeId == null) {
            return Optional.empty();
        }
        return getEmployees(session).stream()
                .filter(e -> employeeId.equalsIgnoreCase(e.getEmployeeId()))
                .findFirst();
    }

    public boolean isEmpty(HttpSession session) {
        return getEmployees(session).isEmpty();
    }

    /** Removes the uploaded employee list from this session -- powers the "Clear Uploaded Data" button. */
    public void clear(HttpSession session) {
        session.removeAttribute(SESSION_KEY_EMPLOYEES);
    }
}