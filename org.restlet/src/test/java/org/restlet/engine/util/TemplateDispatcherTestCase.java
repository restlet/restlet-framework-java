/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.StringRepresentation;

class TemplateDispatcherTestCase {

    private final TemplateDispatcher dispatcher = new TemplateDispatcher();

    @Test
    void afterHandle_entityWithoutLocationRef_setsItFromResourceRef() {
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);
        response.setEntity(new StringRepresentation("body"));

        dispatcher.afterHandle(request, response);

        assertEquals(
                request.getResourceRef().getTargetRef().toString(),
                response.getEntity().getLocationRef().toString());
    }

    @Test
    void afterHandle_entityWithExistingLocationRef_leavesItUnchanged() {
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);
        StringRepresentation entity = new StringRepresentation("body");
        entity.setLocationRef("http://localhost/other");
        response.setEntity(entity);

        dispatcher.afterHandle(request, response);

        assertEquals("http://localhost/other", response.getEntity().getLocationRef().toString());
    }

    @Test
    void afterHandle_nullEntity_doesNothing() {
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);

        dispatcher.afterHandle(request, response);

        assertNull(response.getEntity());
    }

    @Test
    void beforeHandle_nullProtocol_throwsUnsupportedOperationException() {
        Request request = new Request(Method.GET, "relative/path");
        Response response = new Response(request);

        assertThrows(
                UnsupportedOperationException.class,
                () -> dispatcher.beforeHandle(request, response));
    }

    @Test
    void beforeHandle_uriWithoutTemplate_leavesResourceRefUnchanged() {
        Request request = new Request(Method.GET, "http://localhost/test");
        Reference original = request.getResourceRef();
        Response response = new Response(request);

        int result = dispatcher.beforeHandle(request, response);

        assertEquals(original.toString(), request.getResourceRef().toString());
        assertEquals(org.restlet.routing.Filter.CONTINUE, result);
    }

    @Test
    void beforeHandle_uriWithTemplate_resolvesPlaceholder() {
        Request request = new Request(Method.GET, "http://localhost/{name}");
        request.getAttributes().put("name", "world");
        Response response = new Response(request);

        dispatcher.beforeHandle(request, response);

        assertEquals("http://localhost/world", request.getResourceRef().toString());
    }

    @Test
    void beforeHandle_setsOriginalRef() {
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);

        dispatcher.beforeHandle(request, response);

        assertEquals(request.getResourceRef().getTargetRef(), request.getOriginalRef());
    }
}
