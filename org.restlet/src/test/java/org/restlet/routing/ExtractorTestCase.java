/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.representation.StringRepresentation;

/** Unit tests for {@link Extractor}. */
class ExtractorTestCase {

    @Test
    void constructors_createUsableInstances() {
        assertInstanceOf(Extractor.class, new Extractor());
        assertInstanceOf(Extractor.class, new Extractor(new Context()));
        assertInstanceOf(Extractor.class, new Extractor(new Context(), null));
    }

    @Test
    void beforeHandle_withoutAnyExtractsConfigured_leavesAttributesEmpty() {
        Extractor extractor = new Extractor();
        Request request = new Request();
        request.setResourceRef("http://localhost/test?foo=bar");
        Response response = new Response(request);

        int result = extractor.beforeHandle(request, response);

        assertEquals(Filter.CONTINUE, result);
        assertTrue(request.getAttributes().isEmpty());
    }

    @Test
    void beforeHandle_extractsFirstQueryValue() {
        Extractor extractor = new Extractor();
        extractor.extractFromQuery("attr", "foo", true);

        Request request = new Request();
        request.setResourceRef("http://localhost/test?foo=bar1&foo=bar2");
        Response response = new Response(request);

        extractor.beforeHandle(request, response);

        assertEquals("bar1", request.getAttributes().get("attr"));
    }

    @Test
    void beforeHandle_extractsAllQueryValues() {
        Extractor extractor = new Extractor();
        extractor.extractFromQuery("attr", "foo", false);

        Request request = new Request();
        request.setResourceRef("http://localhost/test?foo=bar1&foo=bar2");
        Response response = new Response(request);

        extractor.beforeHandle(request, response);

        @SuppressWarnings("unchecked")
        List<Parameter> values = (List<Parameter>) request.getAttributes().get("attr");
        assertEquals(2, values.size());
    }

    @Test
    void beforeHandle_missingQueryParameter_doesNotSetAttribute() {
        Extractor extractor = new Extractor();
        extractor.extractFromQuery("attr", "missing", true);

        Request request = new Request();
        request.setResourceRef("http://localhost/test?foo=bar");
        Response response = new Response(request);

        extractor.beforeHandle(request, response);

        assertNull(request.getAttributes().get("attr"));
    }

    @Test
    void beforeHandle_extractsFirstEntityValue() {
        Extractor extractor = new Extractor();
        extractor.extractFromEntity("attr", "foo", true);

        Request request = new Request();
        request.setMethod(org.restlet.data.Method.POST);
        request.setEntity(
                new StringRepresentation("foo=bar1&foo=bar2", MediaType.APPLICATION_WWW_FORM));
        Response response = new Response(request);

        extractor.beforeHandle(request, response);

        assertEquals("bar1", request.getAttributes().get("attr"));
    }

    @Test
    void beforeHandle_extractsAllEntityValues() {
        Extractor extractor = new Extractor();
        extractor.extractFromEntity("attr", "foo", false);

        Request request = new Request();
        request.setMethod(org.restlet.data.Method.POST);
        request.setEntity(
                new StringRepresentation("foo=bar1&foo=bar2", MediaType.APPLICATION_WWW_FORM));
        Response response = new Response(request);

        extractor.beforeHandle(request, response);

        @SuppressWarnings("unchecked")
        List<Parameter> values = (List<Parameter>) request.getAttributes().get("attr");
        assertEquals(2, values.size());
    }

    @Test
    void beforeHandle_extractsFirstCookieValue() {
        Extractor extractor = new Extractor();
        extractor.extractFromCookie("attr", "foo", true);

        Request request = new Request();
        request.getCookies().add(new Cookie("foo", "bar1"));
        request.getCookies().add(new Cookie("foo", "bar2"));
        Response response = new Response(request);

        extractor.beforeHandle(request, response);

        assertEquals("bar1", request.getAttributes().get("attr"));
    }

    @Test
    void beforeHandle_extractsAllCookieValues() {
        Extractor extractor = new Extractor();
        extractor.extractFromCookie("attr", "foo", false);

        Request request = new Request();
        request.getCookies().add(new Cookie("foo", "bar1"));
        request.getCookies().add(new Cookie("foo", "bar2"));
        Response response = new Response(request);

        extractor.beforeHandle(request, response);

        @SuppressWarnings("unchecked")
        List<Cookie> values = (List<Cookie>) request.getAttributes().get("attr");
        assertEquals(2, values.size());
    }
}
