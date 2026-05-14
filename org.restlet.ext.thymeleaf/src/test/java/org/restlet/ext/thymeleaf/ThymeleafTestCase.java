/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.thymeleaf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

/**
 * Unit test for the Thymeleaf extension.
 *
 * @author Thierry Boileau
 */
class ThymeleafTestCase {

    @Test
    void testTemplate() throws Exception {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("org/restlet/ext/thymeleaf/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L);

        final Map<String, Object> map = Map.of("welcome", "Hello, world");

        final String result =
                new TemplateRepresentation(
                                "test",
                                TemplateRepresentation.createTemplateEngine(templateResolver),
                                Locale.getDefault(),
                                map,
                                MediaType.TEXT_PLAIN)
                        .getText();
        assertTrue(result.contains("Hello, world"));
    }
}
