/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.thymeleaf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Resolver;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

/** Unit tests for {@link TemplateRepresentation}. */
class TemplateRepresentationTestCase {

    private static TemplateEngine buildEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("org/restlet/ext/thymeleaf/");
        resolver.setSuffix(".html");
        return TemplateRepresentation.createTemplateEngine(resolver);
    }

    @Test
    void getLocale_returnsLocaleFromConstructor() {
        TemplateRepresentation tr =
                new TemplateRepresentation(
                        "test", buildEngine(), Locale.FRENCH, MediaType.TEXT_PLAIN);
        assertEquals(Locale.FRENCH, tr.getLocale());
    }

    @Test
    void getTemplateName_returnsNameFromConstructor() {
        TemplateRepresentation tr =
                new TemplateRepresentation(
                        "myTemplate", buildEngine(), Locale.getDefault(), MediaType.TEXT_PLAIN);
        assertEquals("myTemplate", tr.getTemplateName());
    }

    @Test
    void setTemplateName_changesTemplateName() {
        TemplateRepresentation tr =
                new TemplateRepresentation(
                        "original", buildEngine(), Locale.getDefault(), MediaType.TEXT_PLAIN);
        tr.setTemplateName("changed");
        assertEquals("changed", tr.getTemplateName());
    }

    @Test
    void createTemplateEngine_noArg_returnsNonNull() {
        assertNotNull(TemplateRepresentation.createTemplateEngine());
    }

    @Test
    void createTemplateResolver_returnsNonNull() {
        ITemplateResolver resolver = TemplateRepresentation.createTemplateResolver();
        assertNotNull(resolver);
    }

    @Test
    void wrapConstructor_copiesTemplateName() {
        TemplateRepresentation original =
                new TemplateRepresentation(
                        "test", buildEngine(), Locale.getDefault(), MediaType.TEXT_PLAIN);
        TemplateRepresentation wrapped =
                new TemplateRepresentation(
                        original, buildEngine(), Locale.ENGLISH, MediaType.TEXT_HTML);
        assertEquals("test", wrapped.getTemplateName());
    }

    @Test
    void setDataModel_withResolver_rendersTemplateWithoutThrowing() throws Exception {
        // ResolverContext.getVariableNames() returns an empty set, so Thymeleaf's OGNL evaluator
        // cannot see resolver variables — ${welcome} resolves to null and the <p> renders empty.
        TemplateRepresentation tr =
                new TemplateRepresentation(
                        "test", buildEngine(), Locale.getDefault(), Map.of(), MediaType.TEXT_PLAIN);
        tr.setDataModel(
                new Resolver<>() {
                    @Override
                    public Object resolve(String name) {
                        return "welcome".equals(name) ? "Hello, resolver" : null;
                    }
                });
        String result = tr.getText();
        assertNotNull(result);
        assertFalse(result.contains("Hello, resolver"));
    }

    @Test
    void write_whenTemplateNotFound_throwsIoException() {
        // createTemplateEngine() uses /WEB-INF/templates/ prefix; "nonexistent" won't resolve
        TemplateRepresentation tr =
                new TemplateRepresentation(
                        "nonexistent", Locale.getDefault(), MediaType.TEXT_PLAIN);
        assertThrows(IOException.class, tr::getText);
    }

    @Test
    void setDataModel_withFormRequest_rendersFormValues() throws Exception {
        TemplateRepresentation tr =
                new TemplateRepresentation(
                        "test", buildEngine(), Locale.getDefault(), MediaType.TEXT_PLAIN);
        Request request = new Request();
        request.setEntity(
                new StringRepresentation(
                        "welcome=Hello+from+form", MediaType.APPLICATION_WWW_FORM));
        tr.setDataModel(request, new Response(request));
        assertTrue(tr.getText().contains("Hello from form"));
    }

    @Test
    void setDataModel_withEmptyFormRequest_rendersTemplateSuccessfully() throws Exception {
        TemplateRepresentation tr =
                new TemplateRepresentation(
                        "test", buildEngine(), Locale.getDefault(), MediaType.TEXT_PLAIN);
        Request request = new Request();
        request.setEntity(new StringRepresentation("", MediaType.APPLICATION_WWW_FORM));
        tr.setDataModel(request, new Response(request));
        assertNotNull(tr.getText());
    }
}
