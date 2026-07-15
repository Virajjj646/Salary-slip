package com.snehfoundation.salaryslip.service;

import com.snehfoundation.salaryslip.model.Employee;

import java.io.IOException;
import java.util.List;

/**
 * Bundles every employee's salary slip PDF into a single ZIP archive,
 * entirely in memory. Delegates the actual PDF rendering to {@link PdfService}
 * for each employee, one at a time, and streams the results straight into a
 * ZipOutputStream backed by a byte array -- nothing is ever written to disk.
 */
public interface ZipService {

    /**
     * @return the completed ZIP archive as raw bytes, ready to stream to an
     *         HTTP response.
     */
    byte[] createZipOfAll(List<Employee> employees) throws IOException;
}