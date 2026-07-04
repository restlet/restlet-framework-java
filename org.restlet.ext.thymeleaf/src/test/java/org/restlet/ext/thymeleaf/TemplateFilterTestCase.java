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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Encoding;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Resolver;

/** Unit tests for {@link TemplateFilter}. */
class TemplateFilterTestCase {

    private static final Encoding THYMELEAF = new Encoding("thymeleaf", "Thymeleaf");

    private static class TestTemplateFilter extends TemplateFilter {
        TestTemplateFilter() {
            super();
        }

        TestTemplateFilter(org.restlet.Context context) {
            super(context);
        }

        TestTemplateFilter(org.restlet.Context context, org.restlet.Restlet next) {
            super(context, next);
        }

        TestTemplateFilter(
                org.restlet.Context context, org.restlet.Restlet next, Map<String, Object> model) {
            super(context, next, model);
        }

        TestTemplateFilter(
                org.restlet.Context context, org.restlet.Restlet next, Resolver<Object> resolver) {
            super(context, next, resolver);
        }
    }

    private Request newRequest() {
        Request request = new Request(Method.GET, "/");
        request.setEntity(new StringRepresentation("foo=bar", MediaType.APPLICATION_WWW_FORM));
        return request;
    }

    private TemplateRepresentation templateEntity() {
        TemplateRepresentation representation =
                new TemplateRepresentation("myTemplate", Locale.ENGLISH, MediaType.TEXT_HTML);
        representation.getEncodings().add(THYMELEAF);
        return representation;
    }

    @Test
    void getLocale_defaultsToJvmDefault() {
        assertEquals(Locale.getDefault(), new TestTemplateFilter().getLocale());
    }

    @Test
    void afterHandle_noEntity_doesNothing() {
        TestTemplateFilter filter = new TestTemplateFilter(new Context());
        Request request = newRequest();
        Response response = new Response(request);

        filter.afterHandle(request, response);

        assertNull(response.getEntity());
    }

    @Test
    void afterHandle_entityWithoutThymeleafEncoding_leavesEntityUnchanged() {
        TestTemplateFilter filter = new TestTemplateFilter(new Context());
        Request request = newRequest();
        Response response = new Response(request);
        Representation entity = new StringRepresentation("plain", MediaType.TEXT_PLAIN);
        response.setEntity(entity);

        filter.afterHandle(request, response);

        assertSame(entity, response.getEntity());
    }

    @Test
    void afterHandle_withoutDataModel_usesRequestResponseResolver() {
        TestTemplateFilter filter = new TestTemplateFilter(new Context());
        Request request = newRequest();
        Response response = new Response(request);
        response.setEntity(templateEntity());

        filter.afterHandle(request, response);

        Representation result = response.getEntity();
        assertTrue(result instanceof TemplateRepresentation);
        TemplateRepresentation tr = (TemplateRepresentation) result;
        assertEquals("myTemplate", tr.getTemplateName());
        assertEquals(MediaType.TEXT_HTML, tr.getMediaType());
        assertEquals("bar", tr.context.getVariable("foo"));
    }

    @Test
    void afterHandle_withMapDataModel_usesMap() {
        Map<String, Object> model = new ConcurrentHashMap<>();
        model.put("key", "value");
        TestTemplateFilter filter = new TestTemplateFilter(new Context(), null, model);
        Request request = newRequest();
        Response response = new Response(request);
        response.setEntity(templateEntity());

        filter.afterHandle(request, response);

        TemplateRepresentation tr = (TemplateRepresentation) response.getEntity();
        assertEquals("value", tr.context.getVariable("key"));
    }

    @Test
    void afterHandle_withResolverDataModel_usesResolver() {
        Resolver<Object> resolver =
                new Resolver<>() {
                    @Override
                    public Object resolve(String key) {
                        return "resolved-" + key;
                    }
                };
        TestTemplateFilter filter = new TestTemplateFilter(new Context(), null, resolver);
        Request request = newRequest();
        Response response = new Response(request);
        response.setEntity(templateEntity());

        filter.afterHandle(request, response);

        TemplateRepresentation tr = (TemplateRepresentation) response.getEntity();
        assertEquals("resolved-anyKey", tr.context.getVariable("anyKey"));
    }
}
