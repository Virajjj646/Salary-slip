package com.snehfoundation.salaryslip.service.impl;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.snehfoundation.salaryslip.model.Employee;
import com.snehfoundation.salaryslip.service.PdfService;
import com.snehfoundation.salaryslip.util.LogoProvider;
import com.snehfoundation.salaryslip.util.NumberToWordsConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Renders "salary-slip.html" to PDF bytes using OpenHTMLToPDF -- entirely in
 * memory, nothing touches disk.
 * <p>
 * Rather than hitting the /preview HTTP endpoint internally, this calls the
 * Thymeleaf {@link TemplateEngine} directly with the same template name and
 * the same model variables ("employee", "amountInWords", "logoDataUri") that
 * SalarySlipController uses -- so preview and generated PDF are always in sync.
 */
@Service
@RequiredArgsConstructor
public class PdfServiceImpl implements PdfService {

    private final TemplateEngine templateEngine;
    private final NumberToWordsConverter numberToWordsConverter;
    private final LogoProvider logoProvider;

    @Override
    public byte[] generate(Employee employee) throws IOException {
        String html = renderHtml(employee);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.useFastMode();
            // No baseUri needed: the template's CSS is embedded inline, not linked externally.
            builder.withHtmlContent(html, null);
            builder.toStream(outputStream);
            builder.run();
            return outputStream.toByteArray();
        } catch (IOException ioe) {
            throw ioe;
        } catch (Exception e) {
            throw new IOException(
                    "Failed to render PDF for employee " + employee.getEmployeeId() + " (" + employee.getName() + ")", e);
        }
    }

    private String renderHtml(Employee employee) {
        Context context = new Context();
        context.setVariable("employee", employee);
        context.setVariable("amountInWords", numberToWordsConverter.toWords(employee.getNetPay()));
        context.setVariable("logoDataUri", logoProvider.getLogoDataUri().orElse(null));
        return templateEngine.process("salary-slip", context);
    }
}