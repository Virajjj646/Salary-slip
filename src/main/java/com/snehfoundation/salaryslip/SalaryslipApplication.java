package com.snehfoundation.salaryslip;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the SNEH Foundation Salary Slip Management System.
 *
 * This application is fully stateless: all employee data lives only in
 * memory (populated from an uploaded Excel file, read directly from the
 * multipart request), and every PDF/ZIP is rendered on demand and streamed
 * straight to the response. Nothing is ever written to disk.
 *
 * No JDBC/DataSource starter is declared in pom.xml, so Spring Boot has
 * nothing to auto-configure a DB connection from -- there's no
 * DataSourceAutoConfiguration to exclude in the first place.
 */
@SpringBootApplication
public class SalaryslipApplication {

    public static void main(String[] args) {
        SpringApplication.run(SalaryslipApplication.class, args);
    }
}