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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Header;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.StringRepresentation;

/** Unit tests for {@link Redirector}. */
class RedirectorTestCase {

    @Test
    void twoArgConstructor_defaultsToServerOutboundMode() {
        Redirector redirector = new Redirector(new Context(), "/target");

        assertEquals(Redirector.MODE_SERVER_OUTBOUND, redirector.getMode());
        assertEquals("/target", redirector.getTargetTemplate());
        assertTrue(redirector.isHeadersCleaning());
    }

    @Test
    void threeArgConstructor_setsGivenMode() {
        Redirector redirector =
                new Redirector(new Context(), "/target", Redirector.MODE_CLIENT_FOUND);

        assertEquals(Redirector.MODE_CLIENT_FOUND, redirector.getMode());
    }

    @Test
    void settersRoundTrip() {
        Redirector redirector = new Redirector(new Context(), "/target");

        redirector.setMode(Redirector.MODE_CLIENT_TEMPORARY);
        assertEquals(Redirector.MODE_CLIENT_TEMPORARY, redirector.getMode());

        redirector.setTargetTemplate("/other");
        assertEquals("/other", redirector.getTargetTemplate());

        redirector.setHeadersCleaning(false);
        assertFalse(redirector.isHeadersCleaning());
    }

    @Test
    void getTargetRef_withRelativeTemplate_isResolvedAgainstResourceRef() {
        Redirector redirector = new Redirector(new Context(), "/target");
        Request request = new Request(Method.GET, "http://localhost/foo/bar");
        Response response = new Response(request);

        Reference targetRef = redirector.getTargetRef(request, response);

        // getTargetRef() returns a reference that is relative to the request's
        // resource reference; resolving it yields the absolute target URI.
        assertEquals("/target", targetRef.toString());
        assertEquals("http://localhost/target", targetRef.getTargetRef().toString());
    }

    @Test
    void getTargetRef_withAbsoluteTemplate_isUsedAsIs() {
        Redirector redirector = new Redirector(new Context(), "http://example.com/target");
        Request request = new Request(Method.GET, "http://localhost/foo");
        Response response = new Response(request);

        Reference targetRef = redirector.getTargetRef(request, response);

        assertEquals("http://example.com/target", targetRef.toString());
    }

    @Test
    void getTargetRef_withTemplateVariable_isResolvedFromRequest() {
        Redirector redirector =
                new Redirector(
                        new Context(), "http://example.com/{a}", Redirector.MODE_CLIENT_FOUND);
        Request request = new Request(Method.GET, "http://localhost/foo");
        request.getAttributes().put("a", "resolved");
        Response response = new Response(request);

        Reference targetRef = redirector.getTargetRef(request, response);

        assertEquals("http://example.com/resolved", targetRef.toString());
    }

    @Test
    void handle_clientPermanentMode_redirectsPermanently() {
        Redirector redirector =
                new Redirector(new Context(), "/new", Redirector.MODE_CLIENT_PERMANENT);
        Request request = new Request(Method.GET, "http://localhost/old");
        Response response = new Response(request);

        redirector.handle(request, response);

        assertEquals(Status.REDIRECTION_PERMANENT, response.getStatus());
        assertEquals("http://localhost/new", response.getLocationRef().getTargetRef().toString());
    }

    @Test
    void handle_clientFoundMode_redirectsWithFoundStatus() {
        Redirector redirector = new Redirector(new Context(), "/new", Redirector.MODE_CLIENT_FOUND);
        Request request = new Request(Method.GET, "http://localhost/old");
        Response response = new Response(request);

        redirector.handle(request, response);

        assertEquals(Status.REDIRECTION_FOUND, response.getStatus());
        assertEquals("http://localhost/new", response.getLocationRef().getTargetRef().toString());
    }

    @Test
    void handle_clientSeeOtherMode_redirectsWithSeeOtherStatus() {
        Redirector redirector =
                new Redirector(new Context(), "/new", Redirector.MODE_CLIENT_SEE_OTHER);
        Request request = new Request(Method.GET, "http://localhost/old");
        Response response = new Response(request);

        redirector.handle(request, response);

        assertEquals(Status.REDIRECTION_SEE_OTHER, response.getStatus());
    }

    @Test
    void handle_clientTemporaryMode_redirectsWithTemporaryStatus() {
        Redirector redirector =
                new Redirector(new Context(), "/new", Redirector.MODE_CLIENT_TEMPORARY);
        Request request = new Request(Method.GET, "http://localhost/old");
        Response response = new Response(request);

        redirector.handle(request, response);

        assertEquals(Status.REDIRECTION_TEMPORARY, response.getStatus());
    }

    @Test
    void handle_serverOutboundModeWithoutDispatcher_logsWarningAndDoesNotThrow() {
        Redirector redirector =
                new Redirector(
                        new Context(), "http://backend/target", Redirector.MODE_SERVER_OUTBOUND);
        Request request = new Request(Method.GET, "http://localhost/foo");
        Response response = new Response(request);

        redirector.handle(request, response);
    }

    @Test
    void handle_serverInboundModeWithoutDispatcher_logsWarningAndDoesNotThrow() {
        Redirector redirector =
                new Redirector(
                        new Context(), "http://backend/target", Redirector.MODE_SERVER_INBOUND);
        Request request = new Request(Method.GET, "http://localhost/foo");
        Response response = new Response(request);

        redirector.handle(request, response);
    }

    @Test
    void serverRedirect_withNext_dispatchesRequestAndRestoresResourceRef() {
        Redirector redirector = new Redirector(new Context(), "http://backend/target");
        Reference originalResourceRef = new Reference("http://localhost/foo");
        Request request = new Request(Method.GET, originalResourceRef);
        Response response = new Response(request);
        Reference targetRef = new Reference("http://backend/target");

        Restlet next =
                new Restlet(new Context()) {
                    @Override
                    public void handle(Request req, Response resp) {
                        super.handle(req, resp);
                        assertEquals(targetRef.toString(), req.getResourceRef().toString());
                        resp.setEntity(new StringRepresentation("payload"));
                    }
                };

        redirector.serverRedirect(next, targetRef, request, response);

        assertEquals(originalResourceRef.toString(), request.getResourceRef().toString());
        assertNotNull(response.getEntity());
    }

    @Test
    void serverRedirect_withNullNext_logsWarningAndDoesNotThrow() {
        Redirector redirector = new Redirector(new Context(), "/target");
        Request request = new Request(Method.GET, "http://localhost/foo");
        Response response = new Response(request);

        redirector.serverRedirect(null, new Reference("http://backend/target"), request, response);
    }

    @Test
    void rewriteRequest_withHeadersCleaningEnabled_removesHeadersAttribute() {
        Redirector redirector = new Redirector(new Context(), "/target");
        Request request = new Request(Method.GET, "http://localhost/foo");
        request.getHeaders().add(new Header("X-Custom", "value"));

        redirector.rewrite(request);

        assertNull(request.getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS));
    }

    @Test
    void rewriteRequest_withHeadersCleaningDisabled_keepsOnlyExtensionHeaders() {
        Redirector redirector = new Redirector(new Context(), "/target");
        redirector.setHeadersCleaning(false);
        Request request = new Request(Method.GET, "http://localhost/foo");
        request.getHeaders().add(new Header("X-Custom", "value"));
        request.getHeaders().add(new Header(HeaderConstants.HEADER_CONTENT_TYPE, "text/plain"));

        redirector.rewrite(request);

        org.restlet.util.Series<Header> remaining = request.getHeaders();
        assertEquals(1, remaining.size());
        assertEquals("X-Custom", remaining.get(0).getName());
    }

    @Test
    void rewriteResponse_withHeadersCleaningEnabled_removesHeadersAttribute() {
        Redirector redirector = new Redirector(new Context(), "/target");
        Request request = new Request(Method.GET, "http://localhost/foo");
        Response response = new Response(request);
        response.getHeaders().add(new Header("X-Custom", "value"));

        redirector.rewrite(response);

        assertNull(response.getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS));
    }

    @Test
    void rewriteRepresentation_returnsSameInstanceByDefault() {
        Redirector redirector = new Redirector(new Context(), "/target");
        StringRepresentation representation = new StringRepresentation("body");

        assertEquals(
                representation,
                redirector.rewrite((org.restlet.representation.Representation) representation));
    }

    @Test
    void rewriteLocation_rewritesResponseLocationUsingTemplate() {
        Redirector redirector =
                new Redirector(
                        new Context(), "http://backend{rr}", Redirector.MODE_SERVER_OUTBOUND);
        Request request = new Request(Method.GET, "http://myproxy/foo");
        request.getResourceRef().setBaseRef(new Reference("http://myproxy"));
        Response response = new Response(request);
        response.setLocationRef("http://backend/foo");

        redirector.rewriteLocation(request, response);

        assertEquals("http://myproxy/foo", response.getLocationRef().toString());
    }

    @Test
    void rewriteLocation_withoutLocationRef_isNoOp() {
        Redirector redirector = new Redirector(new Context(), "http://backend{rr}");
        Request request = new Request(Method.GET, "http://myproxy/foo");
        Response response = new Response(request);

        redirector.rewriteLocation(request, response);

        assertNull(response.getLocationRef());
    }
}
