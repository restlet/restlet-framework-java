/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;

/**
 * Unit tests for the {@link ResourceException} class.
 *
 * @author Jerome Louvel
 */
class ResourceExceptionTestCase {

    @Test
    void constructor_withCodeOnly_setsMatchingStatus() {
        ResourceException re = new ResourceException(404);

        assertEquals(404, re.getStatus().getCode());
        assertNull(re.getRequest());
        assertNull(re.getResponse());
    }

    @Test
    void constructor_withCodeAndReasonPhrase_setsReasonPhrase() {
        ResourceException re = new ResourceException(500, "Custom Reason");

        assertEquals(500, re.getStatus().getCode());
        assertEquals("Custom Reason", re.getStatus().getReasonPhrase());
    }

    @Test
    void constructor_withCodeReasonPhraseAndDescription_setsDescription() {
        ResourceException re = new ResourceException(400, "Bad Request", "Missing field");

        assertEquals(400, re.getStatus().getCode());
        assertEquals("Bad Request", re.getStatus().getReasonPhrase());
        assertEquals("Missing field", re.getStatus().getDescription());
    }

    @Test
    void constructor_withStatus_copiesStatus() {
        ResourceException re = new ResourceException(Status.CLIENT_ERROR_FORBIDDEN);

        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, re.getStatus());
    }

    @Test
    void constructor_withStatusAndCause_setsCauseAndStatus() {
        Exception cause = new IllegalStateException("boom");
        ResourceException re = new ResourceException(Status.SERVER_ERROR_INTERNAL, cause);

        assertEquals(Status.SERVER_ERROR_INTERNAL, re.getStatus());
        assertSame(cause, re.getCause());
    }

    @Test
    void constructor_withThrowableOnly_defaultsToInternalServerError() {
        Exception cause = new RuntimeException("failure");
        ResourceException re = new ResourceException(cause);

        assertEquals(Status.SERVER_ERROR_INTERNAL.getCode(), re.getStatus().getCode());
        assertSame(cause, re.getCause());
    }

    @Test
    void constructor_withStatusRequestAndResponse_exposesRequestAndResponse() {
        Request request = new Request(Method.GET, "http://localhost/test");
        Response response = new Response(request);

        ResourceException re =
                new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, request, response);

        assertEquals(Status.CLIENT_ERROR_NOT_FOUND, re.getStatus());
        assertSame(request, re.getRequest());
        assertSame(response, re.getResponse());
    }

    @Test
    void constructor_withStatusAndDescription_appliesDescriptionToCopiedStatus() {
        ResourceException re =
                new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid input");

        assertEquals(Status.CLIENT_ERROR_BAD_REQUEST.getCode(), re.getStatus().getCode());
        assertEquals("Invalid input", re.getStatus().getDescription());
    }

    @Test
    void getMessage_returnsStatusToString() {
        ResourceException re = new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

        assertEquals(Status.CLIENT_ERROR_NOT_FOUND.toString(), re.getMessage());
    }

    @Test
    void constructor_withCodeNameDescriptionAndUri_setsAllStatusFields() {
        ResourceException re =
                new ResourceException(
                        422, "Unprocessable", "Bad payload", "http://example.com/spec");

        assertEquals(422, re.getStatus().getCode());
        assertEquals("Unprocessable", re.getStatus().getReasonPhrase());
        assertEquals("Bad payload", re.getStatus().getDescription());
        assertNotNull(re.getStatus().getUri());
    }
}
