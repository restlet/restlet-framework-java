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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.resource.Directory;
import org.restlet.resource.ServerResource;

/** Unit tests for {@link Router}. */
class RouterTestCase {

    /** Minimal concrete fixture. */
    static class MyServerResource extends ServerResource {}

    private Restlet targetRestlet() {
        return new Restlet(new Context()) {
            @Override
            public void handle(Request request, Response response) {
                super.handle(request, response);
                response.setStatus(Status.SUCCESS_OK);
            }
        };
    }

    @Test
    void defaultConstructor_hasExpectedDefaults() {
        Router router = new Router();

        assertEquals(Template.MODE_EQUALS, router.getDefaultMatchingMode());
        assertFalse(router.getDefaultMatchingQuery());
        assertNull(router.getDefaultRoute());
        assertEquals(Router.MODE_FIRST_MATCH, router.getRoutingMode());
        assertEquals(0.5F, router.getRequiredScore());
        assertEquals(1, router.getMaxAttempts());
        assertEquals(500L, router.getRetryDelay());
        assertNotNull(router.getRoutes());
        assertTrue(router.getRoutes().isEmpty());
    }

    @Test
    void attach_withPlainRestlet_usesDefaultMatchingMode() {
        Router router = new Router(new Context());

        TemplateRoute route = router.attach(targetRestlet());

        assertEquals(1, router.getRoutes().size());
        assertEquals(Template.MODE_EQUALS, route.getMatchingMode());
    }

    @Test
    void attach_withDirectoryTarget_usesStartsWithMatchingMode() {
        Router router = new Router(new Context());
        Directory directory = new Directory(router.getContext(), "clap://class/");

        TemplateRoute route = router.attach(directory);

        assertEquals(Template.MODE_STARTS_WITH, route.getMatchingMode());
    }

    @Test
    void attach_withRouterTarget_usesStartsWithMatchingMode() {
        Router router = new Router(new Context());
        Router subRouter = new Router(router.getContext());

        TemplateRoute route = router.attach(subRouter);

        assertEquals(Template.MODE_STARTS_WITH, route.getMatchingMode());
    }

    @Test
    void attach_withFilterTarget_delegatesMatchingModeToNext() {
        Router router = new Router(new Context());
        Directory directory = new Directory(router.getContext(), "clap://class/");
        Extractor filter = new Extractor(router.getContext(), directory);

        TemplateRoute route = router.attach(filter);

        assertEquals(Template.MODE_STARTS_WITH, route.getMatchingMode());
    }

    @Test
    void attach_withPathTemplateAndExplicitMode_setsGivenMode() {
        Router router = new Router(new Context());

        TemplateRoute route = router.attach("/foo", targetRestlet(), Template.MODE_STARTS_WITH);

        assertEquals(Template.MODE_STARTS_WITH, route.getMatchingMode());
        assertEquals(1, router.getRoutes().size());
    }

    @Test
    void attach_withResourceClass_createsFinderRoute() {
        Router router = new Router(new Context());

        TemplateRoute route = router.attach("/foo", MyServerResource.class);

        assertNotNull(route);
        assertEquals(1, router.getRoutes().size());
    }

    @Test
    void attachDefault_setsDefaultRouteWithStartsWithMode() {
        Router router = new Router(new Context());
        Restlet target = targetRestlet();

        TemplateRoute route = router.attachDefault(target);

        assertSame(route, router.getDefaultRoute());
        assertEquals(Template.MODE_STARTS_WITH, route.getMatchingMode());
    }

    @Test
    void attachDefault_withResourceClass_setsDefaultRoute() {
        Router router = new Router(new Context());

        TemplateRoute route = router.attachDefault(MyServerResource.class);

        assertSame(route, router.getDefaultRoute());
    }

    @Test
    void detach_byRestlet_removesMatchingRoutesAndDefaultRoute() {
        Router router = new Router(new Context());
        Restlet target = targetRestlet();
        router.attach("/foo", target);
        router.attachDefault(target);

        router.detach(target);

        assertTrue(router.getRoutes().isEmpty());
        assertNull(router.getDefaultRoute());
    }

    @Test
    void detach_byClass_removesFinderRoutesAndDefaultRoute() {
        Router router = new Router(new Context());
        router.attach("/foo", MyServerResource.class);
        router.attachDefault(MyServerResource.class);

        router.detach(MyServerResource.class);

        assertTrue(router.getRoutes().isEmpty());
        assertNull(router.getDefaultRoute());
    }

    @Test
    void getNext_firstMatch_returnsMatchingRoute() {
        Router router = new Router(new Context());
        Route route = router.attach("/foo", targetRestlet());
        Request request = new Request(Method.GET, "http://localhost/foo");
        request.getResourceRef().setBaseRef(new Reference("http://localhost"));
        Response response = new Response(request);

        Restlet next = router.getNext(request, response);

        assertNotNull(next);
        assertSame(route, next);
    }

    @Test
    void getNext_noMatchAndNoDefaultRoute_setsNotFoundStatus() {
        Router router = new Router(new Context());
        router.attach("/foo", targetRestlet(), Template.MODE_EQUALS);
        Request request = new Request(Method.GET, "http://localhost/bar");
        Response response = new Response(request);

        Restlet next = router.getNext(request, response);

        assertNull(next);
        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    @Test
    void getNext_noMatch_fallsBackToDefaultRoute() {
        Router router = new Router(new Context());
        router.attach("/foo", targetRestlet(), Template.MODE_EQUALS);
        Restlet defaultTarget = targetRestlet();
        TemplateRoute defaultRoute = router.attachDefault(defaultTarget);
        Request request = new Request(Method.GET, "http://localhost/bar");
        Response response = new Response(request);

        Restlet next = router.getNext(request, response);

        assertSame(defaultRoute, next);
    }

    @Test
    void getNext_bestMatchMode_selectsHighestScoringRoute() {
        Router router = new Router(new Context());
        router.setRoutingMode(Router.MODE_BEST_MATCH);
        Restlet shortTarget = targetRestlet();
        Restlet longTarget = targetRestlet();
        router.attach("/foo", shortTarget, Template.MODE_STARTS_WITH);
        Route longRoute = router.attach("/foo/bar", longTarget, Template.MODE_STARTS_WITH);
        Request request = new Request(Method.GET, "http://localhost/foo/bar");
        request.getResourceRef().setBaseRef(new Reference("http://localhost"));
        Response response = new Response(request);

        Restlet next = router.getNext(request, response);

        assertSame(longRoute, next);
    }

    @Test
    void getNext_customMode_usesGetCustomOverride() {
        Restlet target = targetRestlet();
        Router router =
                new Router(new Context()) {
                    @Override
                    protected Route getCustom(Request request, Response response) {
                        return getRoutes().isEmpty() ? null : getRoutes().get(0);
                    }
                };
        router.setRoutingMode(Router.MODE_CUSTOM);
        Route route = router.attach("/foo", target);
        Request request = new Request(Method.GET, "http://localhost/foo");
        Response response = new Response(request);

        Restlet next = router.getNext(request, response);

        assertSame(route, next);
    }

    @Test
    void getNext_retriesUpToMaxAttempts() {
        java.util.concurrent.atomic.AtomicInteger attempts =
                new java.util.concurrent.atomic.AtomicInteger();
        Restlet target = targetRestlet();
        Router router =
                new Router(new Context()) {
                    @Override
                    protected Route getCustom(Request request, Response response) {
                        return attempts.incrementAndGet() < 2 ? null : getRoutes().get(0);
                    }
                };
        router.setRoutingMode(Router.MODE_CUSTOM);
        router.setMaxAttempts(3);
        router.setRetryDelay(1L);
        Route route = router.attach("/foo", target);
        Request request = new Request(Method.GET, "http://localhost/foo");
        Response response = new Response(request);

        Restlet next = router.getNext(request, response);

        assertSame(route, next);
        assertEquals(2, attempts.get());
    }

    @Test
    void handle_dispatchesToMatchingRoute() {
        Router router = new Router(new Context());
        router.attach("/foo", targetRestlet());
        Request request = new Request(Method.GET, "http://localhost/foo");
        request.getResourceRef().setBaseRef(new Reference("http://localhost"));
        Response response = new Response(request);

        router.handle(request, response);

        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    @Test
    void handle_withNoMatch_setsNotFoundStatus() {
        Router router = new Router(new Context());
        Request request = new Request(Method.GET, "http://localhost/foo");
        Response response = new Response(request);

        router.handle(request, response);

        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
    }

    @Test
    void redirectPermanent_attachesRedirectorWithPermanentMode() {
        Router router = new Router(new Context());

        TemplateRoute route = router.redirectPermanent("/old", "/new");

        assertTrue(route.getNext() instanceof Redirector);
        assertEquals(Redirector.MODE_CLIENT_PERMANENT, ((Redirector) route.getNext()).getMode());
    }

    @Test
    void redirectSeeOther_attachesRedirectorWithSeeOtherMode() {
        Router router = new Router(new Context());

        TemplateRoute route = router.redirectSeeOther("/old", "/new");

        assertEquals(Redirector.MODE_CLIENT_SEE_OTHER, ((Redirector) route.getNext()).getMode());
    }

    @Test
    void redirectTemporary_attachesRedirectorWithTemporaryMode() {
        Router router = new Router(new Context());

        TemplateRoute route = router.redirectTemporary("/old", "/new");

        assertEquals(Redirector.MODE_CLIENT_TEMPORARY, ((Redirector) route.getNext()).getMode());
    }

    @Test
    void startAndStop_propagateToRoutesAndDefaultRoute() throws Exception {
        Router router = new Router(new Context());
        TemplateRoute route = router.attach("/foo", targetRestlet());
        TemplateRoute defaultRoute = router.attachDefault(targetRestlet());

        router.start();

        assertTrue(router.isStarted());
        assertTrue(route.isStarted());
        assertTrue(defaultRoute.isStarted());

        router.stop();

        assertFalse(router.isStarted());
        assertFalse(route.isStarted());
        assertFalse(defaultRoute.isStarted());
    }

    @Test
    void settersRoundTrip() {
        Router router = new Router();

        router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);
        assertEquals(Template.MODE_STARTS_WITH, router.getDefaultMatchingMode());

        router.setDefaultMatchingQuery(true);
        assertTrue(router.getDefaultMatchingQuery());

        router.setMaxAttempts(5);
        assertEquals(5, router.getMaxAttempts());

        router.setRequiredScore(0.9F);
        assertEquals(0.9F, router.getRequiredScore());

        router.setRetryDelay(100L);
        assertEquals(100L, router.getRetryDelay());

        router.setRoutingMode(Router.MODE_LAST_MATCH);
        assertEquals(Router.MODE_LAST_MATCH, router.getRoutingMode());

        org.restlet.util.RouteList routes = new org.restlet.util.RouteList();
        router.setRoutes(routes);
        assertSame(routes, router.getRoutes());
    }
}
