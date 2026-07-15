package com.snehfoundation.salaryslip.controller;

import com.snehfoundation.salaryslip.dto.EmployeeDto;
import com.snehfoundation.salaryslip.model.Employee;
import com.snehfoundation.salaryslip.service.EmployeeStore;
import com.snehfoundation.salaryslip.util.LogoProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * Renders the main (and only) admin dashboard page. Every request rebuilds
 * the employee list fresh from {@link EmployeeStore} -- there's no status to
 * compute anymore, since the app is fully stateless: Preview and Download
 * are always available for any employee in the current batch.
 */
@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final EmployeeStore employeeStore;
    private final LogoProvider logoProvider;

    @GetMapping("/")
    public String dashboard(Model model) {
        List<EmployeeDto> employees = employeeStore.getAll().stream()
                .map(this::toDto)
                .toList();

        model.addAttribute("employees", employees);
        model.addAttribute("employeeCount", employees.size());
        model.addAttribute("isEmpty", employeeStore.isEmpty());
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