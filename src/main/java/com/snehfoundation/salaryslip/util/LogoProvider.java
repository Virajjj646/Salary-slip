package com.snehfoundation.salaryslip.util;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.Optional;

/**
 * Looks for a logo file under static/img/ and returns it as a base64 data
 * URI (e.g. "data:image/png;base64,...."). A data URI works identically for:
 * - the browser preview (an ordinary <img> tag)
 * - OpenHTMLToPDF generation (which has no HTTP context to fetch "/img/..."
 *   from, so a classpath-relative <img src> would silently fail there)
 * <p>
 * If no logo file has been added yet, returns empty -- templates fall back
 * to a plain text badge in that case, so nothing breaks before the real
 * artwork is dropped in.
 */
@Component
public class LogoProvider {

    private static final String[] CANDIDATE_PATHS = {
            "static/img/sneh-logo.png",
            "static/img/sneh-logo.jpg",
            "static/img/sneh-logo.jpeg",
            "static/img/logo.png",
            "static/img/logo.jpg"
    };

    public Optional<String> getLogoDataUri() {
        for (String path : CANDIDATE_PATHS) {
            ClassPathResource resource = new ClassPathResource(path);
            if (resource.exists()) {
                try (InputStream inputStream = resource.getInputStream()) {
                    byte[] bytes = inputStream.readAllBytes();
                    String mimeType = path.endsWith(".png") ? "image/png" : "image/jpeg";
                    String base64 = Base64.getEncoder().encodeToString(bytes);
                    return Optional.of("data:" + mimeType + ";base64," + base64);
                } catch (IOException e) {
                    // Try the next candidate rather than failing the whole page render
                }
            }
        }
        return Optional.empty();
    }
}