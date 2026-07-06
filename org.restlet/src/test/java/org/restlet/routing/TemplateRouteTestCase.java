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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Status;

/** Unit tests for {@link TemplateRoute}. */
class TemplateRouteTestCase {

    @Test
    void constructorWithNext_hasNoTemplate() {
        Restlet next = new MockRestlet(null);

        TemplateRoute route = new TemplateRoute(next);

        assertNull(route.getTemplate());
        assertNull(route.getRouter());
    }

    @Test
    void constructorWithStringPattern_createsStartsWithTemplate() {
        Router router = new Router(new Context());
        Restlet next = new MockRestlet(null);

        TemplateRoute route = new TemplateRoute(router, "/foo", next);

        assertNotNull(route.getTemplate());
        assertEquals(Template.MODE_STARTS_WITH, route.getMatchingMode());
    }

    @Test
    void constructorWithTemplate_defaultsMatchingQueryFromRouter() {
        Router router = new Router();
        router.setDefaultMatchingQuery(true);
        Template template = new Template("/foo");

        TemplateRoute route = new TemplateRoute(router, template, new MockRestlet(null));

        assertTrue(route.isMatchingQuery());
    }

    @Test
    void constructorWithoutRouter_matchingQueryDefaultsTrue() {
        TemplateRoute route = new TemplateRoute(null, new Template("/foo"), new MockRestlet(null));

        assertTrue(route.isMatchingQuery());
    }

    @Test
    void score_withoutRouter_returnsZero() {
        TemplateRoute route = new TemplateRoute(null, new Template("/foo"), new MockRestlet(null));
        Request request = new Request(Method.GET, "http://localhost/foo");
        Response response = new Response(request);

        assertEquals(0F, route.score(request, response));
    }

    @Test
    void score_withMatchingUri_returnsPositiveScore() {
        Router router = new Router(new Context());
        TemplateRoute route = new TemplateRoute(router, "/foo", new MockRestlet(null));
        Request request = new Request(Method.GET, "http://localhost/foo");
        request.getResourceRef().setBaseRef(new org.restlet.data.Reference("http://localhost"));
        Response response = new Response(request);

        float score = route.score(request, response);

        assertTrue(score > 0F);
    }

    @Test
    void score_withNonMatchingUri_returnsZero() {
        Router router = new Router(new Context());
        TemplateRoute route = new TemplateRoute(router, "/foo", new MockRestlet(null));
        Request request = new Request(Method.GET, "http://localhost/bar");
        Response response = new Response(request);

        assertEquals(0F, route.score(request, response));
    }

    @Test
    void beforeHandle_onMatch_updatesBaseReferenceAndContinues() {
        Router router = new Router(new Context());
        Restlet next =
                new Restlet(new Context()) {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setStatus(Status.SUCCESS_OK);
                    }
                };
        TemplateRoute route = new TemplateRoute(router, "/foo", next);
        Request request = new Request(Method.GET, "http://localhost/foo/bar");
        request.getResourceRef().setBaseRef(new org.restlet.data.Reference("http://localhost"));
        Response response = new Response(request);

        route.handle(request, response);

        assertEquals(Status.SUCCESS_OK, response.getStatus());
        assertEquals("/foo", request.getResourceRef().getBaseRef().getPath());
    }

    @Test
    void beforeHandle_onNoMatch_setsNotFoundStatus() {
        Router router = new Router(new Context());
        Restlet next = new MockRestlet(new Context());
        TemplateRoute route =
                new TemplateRoute(router, new Template("/foo", Template.MODE_EQUALS), next);
        Request request = new Request(Method.GET, "http://localhost/other");
        Response response = new Response(request);

        route.handle(request, response);

        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    @Test
    void setMatchingMode_updatesTemplate() {
        TemplateRoute route = new TemplateRoute(null, new Template("/foo"), new MockRestlet(null));

        route.setMatchingMode(Template.MODE_STARTS_WITH);

        assertEquals(Template.MODE_STARTS_WITH, route.getMatchingMode());
    }

    @Test
    void setMatchingQuery_roundTrip() {
        TemplateRoute route = new TemplateRoute(null, new Template("/foo"), new MockRestlet(null));

        route.setMatchingQuery(false);

        assertEquals(false, route.isMatchingQuery());
    }

    @Test
    void setTemplate_roundTrip() {
        TemplateRoute route = new TemplateRoute(new MockRestlet(null));
        Template template = new Template("/bar");

        route.setTemplate(template);

        assertEquals(template, route.getTemplate());
    }

    @Test
    void toString_withTemplate_includesPatternAndNext() {
        Restlet next = new MockRestlet(null);
        TemplateRoute route = new TemplateRoute(null, new Template("/foo"), next);

        String result = route.toString();

        assertTrue(result.contains("/foo"));
    }

    @Test
    void toString_withoutTemplate_fallsBackToSuper() {
        TemplateRoute route = new TemplateRoute(new MockRestlet(null));

        String result = route.toString();

        assertNotNull(result);
    }
}
