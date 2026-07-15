package com.snehfoundation.salaryslip.service.impl;

import com.snehfoundation.salaryslip.model.Employee;
import com.snehfoundation.salaryslip.service.PdfService;
import com.snehfoundation.salaryslip.service.ZipService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ZipServiceImpl implements ZipService {

    private static final Logger log = LoggerFactory.getLogger(ZipServiceImpl.class);

    private final PdfService pdfService;

    @Override
    public byte[] createZipOfAll(List<Employee> employees) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             ZipOutputStream zipOut = new ZipOutputStream(byteArrayOutputStream)) {

            for (Employee employee : employees) {
                try {
                    byte[] pdfBytes = pdfService.generate(employee);
                    String entryName = buildEntryName(employee);

                    zipOut.putNextEntry(new ZipEntry(entryName));
                    zipOut.write(pdfBytes);
                    zipOut.closeEntry();
                } catch (Exception e) {
                    // One employee's slip failing to render shouldn't sink
                    // the whole ZIP -- skip it and keep going, same tolerance
                    // policy the old Generate All had for individual failures.
                    log.error("Skipping {} ({}) in ZIP export -- PDF generation failed",
                            employee.getEmployeeId(), employee.getName(), e);
                }
            }

            zipOut.finish();
            return byteArrayOutputStream.toByteArray();
        }
    }

    private String buildEntryName(Employee employee) {
        String safeMonth = employee.getMonth() == null
                ? "Unspecified"
                : employee.getMonth().trim().replaceAll("\\s+", "_").replaceAll("[^A-Za-z0-9_-]", "");
        return employee.getFileSafeIdentifier() + "_" + safeMonth + ".pdf";
    }
}