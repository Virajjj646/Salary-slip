package com.snehfoundation.salaryslip.controller;

import com.snehfoundation.salaryslip.dto.EmployeeDto;
import com.snehfoundation.salaryslip.model.Employee;
import com.snehfoundation.salaryslip.util.EmployeeSessionHelper;
import com.snehfoundation.salaryslip.util.LogoProvider;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Renders the main (and only) admin dashboard page. Every request rebuilds
 * the employee list fresh from the current user's own {@code HttpSession}
 * (via {@link EmployeeSessionHelper}) -- never from any shared/singleton
 * service, so this page only ever shows data the current user themselves
 * uploaded in this session.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final EmployeeSessionHelper employeeSessionHelper;
    private final LogoProvider logoProvider;

    @GetMapping("/")
    public String dashboard(HttpSession session, Model model) {
        List<EmployeeDto> employees = employeeSessionHelper.getEmployees(session).stream()
                .map(this::toDto)
                .toList();

        model.addAttribute("employees", employees);
        model.addAttribute("employeeCount", employees.size());
        model.addAttribute("isEmpty", employeeSessionHelper.isEmpty(session));
        model.addAttribute("logoDataUri", logoProvider.getLogoDataUri().orElse(null));

        // uploadSuccess / uploadError / uploadRowErrors arrive automatically
        // via RedirectAttributes flash attributes from FileUploadController
        // and GlobalExceptionHandler -- nothing to add here for those.

        return "dashboard";
    }

    private EmployeeDto toDto(Employee employee) {
        return EmployeeDto.builder()
                .month(employee.getMonth())
                .employeeId(employee.getEmployeeId())
                .name(employee.getName())
                .project(employee.getProject())
                .designation(employee.getDesignation())
                .netPay(employee.getNetPay())
                .fileSafeIdentifier(employee.getFileSafeIdentifier())
                .build();
    }
}