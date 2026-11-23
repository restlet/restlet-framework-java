/**
 * Copyright 2005-2024 Qlik
 *
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.routing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;

/**
 * Unit tests for {@link Redirector}: normalization of the target reference
 * (#238).
 */
class RedirectorTestCase {

    private Reference targetRefOf(String template, String resourceRef) {
        Redirector redirector =
                new Redirector(new Context(), template, Redirector.MODE_CLIENT_FOUND);
        Request request = new Request(Method.GET, resourceRef);
        return redirector.getTargetRef(request, new Response(request));
    }

    @Test
    void getTargetRef_withAbsoluteTemplate_removesDotSegments() {
        Reference targetRef =
                targetRefOf("http://example.com/a/b/../c/./d?x=1#f", "http://localhost/foo");

        assertEquals("http://example.com/a/c/d?x=1#f", targetRef.toString());
    }

    @Test
    void getTargetRef_withAbsolutePathTemplate_removesDotSegmentsAndKeepsTheBase() {
        Reference targetRef = targetRefOf("/a/b/../c?x=../y", "http://localhost:8080/root/res");

        assertEquals("/a/c?x=../y", targetRef.toString());
        assertEquals("http://localhost:8080/root/res", targetRef.getBaseRef().toString());
        assertEquals("http://localhost:8080/a/c?x=../y", targetRef.getTargetRef().toString());
    }

    @Test
    void getTargetRef_withVariableExpandingToDotSegments_removesThem() {
        Redirector redirector =
                new Redirector(
                        new Context(), "http://example.com/root/{a}", Redirector.MODE_CLIENT_FOUND);
        Request request = new Request(Method.GET, "http://localhost/foo");
        request.getAttributes().put("a", "x/../y");

        Reference targetRef = redirector.getTargetRef(request, new Response(request));

        assertEquals("http://example.com/root/y", targetRef.toString());
    }

    @Test
    void getTargetRef_withRelativePathTemplate_isLeftUntouched() {
        Reference targetRef = targetRefOf("../up/./x", "http://localhost:8080/root/sub/res");

        assertEquals("../up/./x", targetRef.toString());
        assertEquals("http://localhost:8080/root/up/x", targetRef.getTargetRef().toString());
    }

    @Test
    void getTargetRef_withoutDotSegments_isUnchanged() {
        assertEquals(
                "http://example.com",
                targetRefOf("http://example.com", "http://localhost/foo").toString());
        assertEquals(
                "http://example.com/",
                targetRefOf("http://example.com/", "http://localhost/foo").toString());
        assertEquals(
                "http://example.com/a/b/",
                targetRefOf("http://example.com/a/b/", "http://localhost/foo").toString());
        assertEquals("/a/b?x=1", targetRefOf("/a/b?x=1", "http://localhost/foo").toString());
    }

    @Test
    void handle_clientFoundMode_locationHasNoDotSegments() {
        Redirector redirector =
                new Redirector(
                        new Context(), "http://example.com/a/b/../c", Redirector.MODE_CLIENT_FOUND);
        Request request = new Request(Method.GET, "http://localhost/old");
        Response response = new Response(request);

        redirector.handle(request, response);

        assertEquals(Status.REDIRECTION_FOUND, response.getStatus());
        assertEquals("http://example.com/a/c", response.getLocationRef().toString());
    }

    @Test
    void handle_serverInboundMode_dispatchesANormalizedResourceRef() {
        Context context = new Context();
        List<String> seen = new ArrayList<>();
        context.setServerDispatcher(
                new Restlet(context) {
                    @Override
                    public void handle(Request req, Response resp) {
                        seen.add(req.getResourceRef().toString());
                    }
                });
        Redirector redirector =
                new Redirector(
                        context, "http://backend/a/b/../c/./d", Redirector.MODE_SERVER_INBOUND);
        Request request = new Request(Method.GET, "http://localhost/old");

        redirector.handle(request, new Response(request));

        assertEquals(List.of("http://backend/a/c/d"), seen);
        assertEquals("http://localhost/old", request.getResourceRef().toString());
    }
}
