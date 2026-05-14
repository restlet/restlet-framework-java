/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.jaas;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Reference;

/** Unit tests for {@link ChallengeCallbackHandler}. */
class ChallengeCallbackHandlerTestCase {

    private Request request;
    private Response response;
    private ChallengeCallbackHandler handler;

    @BeforeEach
    void setUp() {
        request = new Request(Method.GET, new Reference("http://localhost/test"));
        response = new Response(request);
        handler = new ChallengeCallbackHandler(request, response);
    }

    @Test
    void testConstructor() {
        assertEquals(request, handler.getRequest());
        assertEquals(response, handler.getResponse());
    }

    @Test
    void testSetRequest() {
        Request newRequest = new Request(Method.POST, new Reference("http://localhost/other"));
        handler.setRequest(newRequest);
        assertEquals(newRequest, handler.getRequest());
    }

    @Test
    void testSetResponse() {
        Response newResponse = new Response(request);
        handler.setResponse(newResponse);
        assertEquals(newResponse, handler.getResponse());
    }

    @Test
    void testHandleNameCallback() throws UnsupportedCallbackException {
        request.setChallengeResponse(
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "john", "secret"));

        NameCallback nameCallback = new NameCallback("Username:");
        handler.handle(new javax.security.auth.callback.Callback[] {nameCallback});

        assertEquals("john", nameCallback.getName());
    }

    @Test
    void testHandlePasswordCallback() throws UnsupportedCallbackException {
        request.setChallengeResponse(
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "john", "secret"));

        PasswordCallback passwordCallback = new PasswordCallback("Password:", false);
        handler.handle(new javax.security.auth.callback.Callback[] {passwordCallback});

        assertArrayEquals("secret".toCharArray(), passwordCallback.getPassword());
    }

    @Test
    void testHandleNullCallbacks() throws UnsupportedCallbackException {
        handler.handle((javax.security.auth.callback.Callback[]) null);
    }

    @Test
    void testHandleEmptyCallbacks() throws UnsupportedCallbackException {
        handler.handle(new javax.security.auth.callback.Callback[] {});
    }

    @Test
    void testHandleUnsupportedCallback() {
        javax.security.auth.callback.Callback unsupported =
                new javax.security.auth.callback.Callback() {};
        assertThrows(
                UnsupportedCallbackException.class,
                () -> handler.handle(new javax.security.auth.callback.Callback[] {unsupported}));
    }

    @Test
    void testHandleNameCallbackWithoutChallengeResponse() {
        assertNull(request.getChallengeResponse());
        NameCallback nameCallback = new NameCallback("Username:");
        assertThrows(
                UnsupportedCallbackException.class,
                () -> handler.handle(new javax.security.auth.callback.Callback[] {nameCallback}));
    }
}
