/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.thymeleaf;

import org.junit.jupiter.api.Test;
import org.restlet.data.MediaType;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import java.util.Locale;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for the Thymeleaf extension.
 * 
 * @author Thierry Boileau
 */
public class ThymeleafTestCase {

    @Test
    public void testTemplate() throws Exception {
        ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
        templateResolver.setPrefix("org/restlet/ext/thymeleaf/");
        templateResolver.setSuffix(".html");
        templateResolver.setCacheTTLMs(3600000L);

        final Map<String, Object> map = Map.of("welcome", "Hello, world");

        final String result = new TemplateRepresentation("test",
                TemplateRepresentation.createTemplateEngine(templateResolver),
                Locale.getDefault(), map, MediaType.TEXT_PLAIN).getText();
        assertTrue(result.contains("Hello, world"));
    }

}
