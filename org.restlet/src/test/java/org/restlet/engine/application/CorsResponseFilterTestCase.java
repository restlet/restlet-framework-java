/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.application;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.resource.Get;
import org.restlet.resource.Options;
import org.restlet.resource.ServerResource;

/**
 * @author Manuel Boillod
 */
class CorsResponseFilterTestCase {

    private CorsFilter corsFilter;

    public static class DummyServerResource extends ServerResource {
        @Options
        public void doOption() {
            // Does nothing on purpose
        }

        @Get
        public void doGet() {
            // Does nothing on purpose
        }
    }

    @BeforeEach
    void setUpEach() {
        corsFilter = new CorsFilter();
        corsFilter.setNext(DummyServerResource.class);
    }

    // INVALID CORS REQUESTS

    @Test
    void testGet_withoutOrigin() {
        Request request = new Request();
        request.setMethod(Method.GET);
        Response response = corsFilter.handle(request);
        assertNoCorsHeaders(response);
    }

    @Test
    void testOption_withoutOrigin() {
        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        Response response = corsFilter.handle(request);
        assertNoCorsHeaders(response);
    }

    @Test
    void testOption_withoutRequestMethod() {
        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        request.getHeaders().set("Origin", "localhost");
        Response response = corsFilter.handle(request);
        assertNoCorsHeaders(response);
    }

    // VALID CORS REQUESTS

    @Test
    void testGet() {
        Request request = new Request();
        request.setMethod(Method.GET);
        request.getHeaders().set("Origin", "localhost");
        Response response = corsFilter.handle(request);
        assertEquals("*", response.getAccessControlAllowOrigin());
        assertNull(response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertIsEmpty(response.getAccessControlAllowMethods());
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }

    @Test
    void testGet_withAuthenticationAllowed() {
        corsFilter.setAllowedCredentials(true);

        Request request = new Request();
        request.setMethod(Method.GET);
        request.getHeaders().set("Origin", "localhost");
        Response response = corsFilter.handle(request);
        assertEquals("localhost", response.getAccessControlAllowOrigin());
        assertEquals(Boolean.TRUE, response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertIsEmpty(response.getAccessControlAllowMethods());
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }

    @Test
    void testOption_requestGet() {
        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        request.getHeaders().set("Origin", "localhost");
        request.setAccessControlRequestMethod(Method.GET);
        Response response = corsFilter.handle(request);
        assertEquals("*", response.getAccessControlAllowOrigin());
        assertNull(response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertThat(
                response.getAccessControlAllowMethods(),
                Matchers.containsInAnyOrder(Method.GET, Method.HEAD, Method.OPTIONS));
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }

    @Test
    void testOption_requestGet_skippingResource() {
        corsFilter.setSkippingResourceForCorsOptions(true);

        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        request.getHeaders().set("Origin", "localhost");
        request.setAccessControlRequestMethod(Method.GET);
        Response response = corsFilter.handle(request);
        assertEquals("*", response.getAccessControlAllowOrigin());
        assertNull(response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertThat(
                response.getAccessControlAllowMethods(),
                Matchers.containsInAnyOrder(
                        Method.GET, Method.POST, Method.PUT, Method.DELETE, Method.PATCH));
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }

    @Test
    void testOption_requestPost_skippingResource() {
        corsFilter.setSkippingResourceForCorsOptions(true);

        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        request.getHeaders().set("Origin", "localhost");
        request.setAccessControlRequestMethod(Method.POST);
        Response response = corsFilter.handle(request);
        assertEquals("*", response.getAccessControlAllowOrigin());
        assertNull(response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertThat(
                response.getAccessControlAllowMethods(),
                Matchers.containsInAnyOrder(
                        Method.GET, Method.POST, Method.PUT, Method.DELETE, Method.PATCH));
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }

    @Test
    void testOption_requestGet_withAuthenticationAllowed() {
        corsFilter.setAllowedCredentials(true);

        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        request.getHeaders().set("Origin", "localhost");
        request.setAccessControlRequestMethod(Method.GET);
        Response response = corsFilter.handle(request);
        assertEquals("localhost", response.getAccessControlAllowOrigin());
        assertEquals(Boolean.TRUE, response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertThat(
                response.getAccessControlAllowMethods(),
                Matchers.containsInAnyOrder(Method.GET, Method.HEAD, Method.OPTIONS));
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }

    // VARY: ORIGIN (Fetch standard, "CORS protocol and HTTP caches"; RFC 9110 section 12.5.5)

    /** Static "*" without credentials: same answer for everybody, Vary must not be used. */
    @Test
    void testGet_staticWildcard_noVary() {
        Request request = new Request();
        request.setMethod(Method.GET);
        request.getHeaders().set("Origin", "localhost");
        Response response = corsFilter.handle(request);
        assertFalse(response.getDimensions().contains(org.restlet.data.Dimension.ORIGIN));
    }

    @Test
    void testOption_requestGet_staticWildcard_noVary() {
        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        request.getHeaders().set("Origin", "localhost");
        request.setAccessControlRequestMethod(Method.GET);
        Response response = corsFilter.handle(request);
        assertFalse(response.getDimensions().contains(org.restlet.data.Dimension.ORIGIN));
    }

    @Test
    void testGet_staticWildcard_withoutOrigin_noVary() {
        Request request = new Request();
        request.setMethod(Method.GET);
        Response response = corsFilter.handle(request);
        assertFalse(response.getDimensions().contains(org.restlet.data.Dimension.ORIGIN));
    }

    @Test
    void testGet_withAuthenticationAllowed_varyOrigin() {
        corsFilter.setAllowedCredentials(true);

        Request request = new Request();
        request.setMethod(Method.GET);
        request.getHeaders().set("Origin", "localhost");
        Response response = corsFilter.handle(request);
        assertTrue(response.getDimensions().contains(org.restlet.data.Dimension.ORIGIN));
    }

    @Test
    void testOption_requestGet_withAuthenticationAllowed_varyOrigin() {
        corsFilter.setAllowedCredentials(true);

        Request request = new Request();
        request.setMethod(Method.OPTIONS);
        request.getHeaders().set("Origin", "localhost");
        request.setAccessControlRequestMethod(Method.GET);
        Response response = corsFilter.handle(request);
        assertTrue(response.getDimensions().contains(org.restlet.data.Dimension.ORIGIN));
    }

    @Test
    void testGet_allowList_allowedOrigin_varyOrigin() {
        corsFilter.setAllowedOrigins(java.util.Collections.singleton("http://allowed.example"));

        Request request = new Request();
        request.setMethod(Method.GET);
        request.getHeaders().set("Origin", "http://allowed.example");
        Response response = corsFilter.handle(request);
        assertEquals("http://allowed.example", response.getAccessControlAllowOrigin());
        assertTrue(response.getDimensions().contains(org.restlet.data.Dimension.ORIGIN));
    }

    @Test
    void testGet_allowList_originNotAllowed_varyOrigin() {
        corsFilter.setAllowedOrigins(java.util.Collections.singleton("http://allowed.example"));

        Request request = new Request();
        request.setMethod(Method.GET);
        request.getHeaders().set("Origin", "http://other.example");
        Response response = corsFilter.handle(request);
        assertNoCorsHeaders(response);
        // A cache must not reuse this answer for the allowed origin
        assertTrue(response.getDimensions().contains(org.restlet.data.Dimension.ORIGIN));
    }

    /**
     * Without Origin, the response lacks CORS headers: a cache must not serve it later to a CORS
     * request.
     */
    @Test
    void testGet_allowList_withoutOrigin_varyOrigin() {
        corsFilter.setAllowedOrigins(java.util.Collections.singleton("http://allowed.example"));

        Request request = new Request();
        request.setMethod(Method.GET);
        Response response = corsFilter.handle(request);
        assertNoCorsHeaders(response);
        assertTrue(response.getDimensions().contains(org.restlet.data.Dimension.ORIGIN));
    }

    @Test
    void testGet_withAuthenticationAllowed_withoutOrigin_varyOrigin() {
        corsFilter.setAllowedCredentials(true);

        Request request = new Request();
        request.setMethod(Method.GET);
        Response response = corsFilter.handle(request);
        assertNoCorsHeaders(response);
        assertTrue(response.getDimensions().contains(org.restlet.data.Dimension.ORIGIN));
    }

    // ORIGIN MATCHER (opt-in)

    @Test
    void testOriginMatcher_acceptsMatchingOrigin_echoesExactOrigin() {
        corsFilter.setAllowedOrigins(new java.util.HashSet<String>());
        corsFilter.setAllowedOriginMatcher(origin -> origin.endsWith(".example.com"));

        Request request = new Request();
        request.setMethod(Method.GET);
        request.getHeaders().set("Origin", "https://app.example.com");
        Response response = corsFilter.handle(request);
        assertEquals("https://app.example.com", response.getAccessControlAllowOrigin());
        assertTrue(response.getDimensions().contains(org.restlet.data.Dimension.ORIGIN));
    }

    @Test
    void testOriginMatcher_rejectsOtherOrigin() {
        corsFilter.setAllowedOrigins(new java.util.HashSet<String>());
        corsFilter.setAllowedOriginMatcher(origin -> origin.endsWith(".example.com"));

        Request request = new Request();
        request.setMethod(Method.GET);
        request.getHeaders().set("Origin", "https://evil.example.org");
        assertNoCorsHeaders(corsFilter.handle(request));
    }

    @Test
    void testOriginMatcher_withCredentials() {
        corsFilter.setAllowedOrigins(new java.util.HashSet<String>());
        corsFilter.setAllowedOriginMatcher(origin -> origin.endsWith(".example.com"));
        corsFilter.setAllowedCredentials(true);

        Request request = new Request();
        request.setMethod(Method.GET);
        request.getHeaders().set("Origin", "https://app.example.com");
        Response response = corsFilter.handle(request);
        assertEquals("https://app.example.com", response.getAccessControlAllowOrigin());
        assertEquals(Boolean.TRUE, response.getAccessControlAllowCredentials());
    }

    @Test
    void testOriginMatcher_changedAfterFirstRequest_isApplied() {
        corsFilter.setAllowedOrigins(new java.util.HashSet<String>());
        Request first = new Request();
        first.setMethod(Method.GET);
        first.getHeaders().set("Origin", "https://app.example.com");
        assertNoCorsHeaders(corsFilter.handle(first));

        corsFilter.setAllowedOriginMatcher(origin -> origin.endsWith(".example.com"));
        Request second = new Request();
        second.setMethod(Method.GET);
        second.getHeaders().set("Origin", "https://app.example.com");
        assertEquals(
                "https://app.example.com", corsFilter.handle(second).getAccessControlAllowOrigin());
    }

    private void assertIsEmpty(Collection<?> collection) {
        assertNotNull(collection);
        assertTrue(collection.isEmpty());
    }

    public void assertNoCorsHeaders(Response response) {
        assertNull(response.getAccessControlAllowOrigin());
        assertNull(response.getAccessControlAllowCredentials());
        assertIsEmpty(response.getAccessControlAllowHeaders());
        assertIsEmpty(response.getAccessControlAllowMethods());
        assertIsEmpty(response.getAccessControlExposeHeaders());
    }
}
