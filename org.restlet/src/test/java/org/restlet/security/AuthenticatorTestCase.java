/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

/** Unit tests for {@link Authenticator}. */
class AuthenticatorTestCase {

    /** Minimal concrete fixture, since {@link Authenticator} is abstract. */
    private static class TestAuthenticator extends Authenticator {

        private boolean authenticateResult;

        private int authenticateCallCount;

        TestAuthenticator(Context context) {
            super(context);
        }

        TestAuthenticator(Context context, boolean optional) {
            super(context, optional);
        }

        TestAuthenticator(
                Context context, boolean multiAuthenticating, boolean optional, Enroler enroler) {
            super(context, multiAuthenticating, optional, enroler);
        }

        void setAuthenticateResult(boolean authenticateResult) {
            this.authenticateResult = authenticateResult;
        }

        int getAuthenticateCallCount() {
            return authenticateCallCount;
        }

        @Override
        protected boolean authenticate(Request request, Response response) {
            authenticateCallCount++;
            return authenticateResult;
        }
    }

    @Test
    void constructorWithContext_defaultsToRequiredAndMultiAuthenticating() {
        TestAuthenticator authenticator = new TestAuthenticator(new Context());
        assertFalse(authenticator.isOptional());
        assertTrue(authenticator.isMultiAuthenticating());
    }

    @Test
    void constructorWithOptional_setsOptionalFlag() {
        TestAuthenticator authenticator = new TestAuthenticator(new Context(), true);
        assertTrue(authenticator.isOptional());
        assertTrue(authenticator.isMultiAuthenticating());
    }

    @Test
    void constructorWithContext_usesContextDefaultEnroler() {
        Enroler enroler = clientInfo -> {};
        Context context = new Context();
        context.setDefaultEnroler(enroler);

        TestAuthenticator authenticator = new TestAuthenticator(context);

        assertSame(enroler, authenticator.getEnroler());
    }

    @Test
    void getEnrolerSetEnroler_roundTrip() {
        TestAuthenticator authenticator = new TestAuthenticator(null, false, false, null);
        assertNull(authenticator.getEnroler());

        Enroler enroler = clientInfo -> {};
        authenticator.setEnroler(enroler);
        assertSame(enroler, authenticator.getEnroler());
    }

    @Test
    void isOptionalSetOptional_roundTrip() {
        TestAuthenticator authenticator = new TestAuthenticator(null, false, false, null);
        assertFalse(authenticator.isOptional());
        authenticator.setOptional(true);
        assertTrue(authenticator.isOptional());
    }

    @Test
    void isMultiAuthenticatingSetMultiAuthenticating_roundTrip() {
        TestAuthenticator authenticator = new TestAuthenticator(null, false, false, null);
        assertFalse(authenticator.isMultiAuthenticating());
        authenticator.setMultiAuthenticating(true);
        assertTrue(authenticator.isMultiAuthenticating());
    }

    @Test
    void beforeHandle_whenAuthenticateSucceeds_returnsContinueAndMarksAuthenticated() {
        TestAuthenticator authenticator = new TestAuthenticator(null, true, false, null);
        authenticator.setAuthenticateResult(true);
        Request request = new Request();
        Response response = new Response(request);

        int result = authenticator.beforeHandle(request, response);

        assertEquals(Filter.CONTINUE, result);
        assertTrue(request.getClientInfo().isAuthenticated());
    }

    @Test
    void beforeHandle_whenAuthenticateFailsAndNotOptional_returnsStopAndMarksUnauthenticated() {
        TestAuthenticator authenticator = new TestAuthenticator(null, true, false, null);
        authenticator.setAuthenticateResult(false);
        Request request = new Request();
        Response response = new Response(request);

        int result = authenticator.beforeHandle(request, response);

        assertEquals(Filter.STOP, result);
        assertFalse(request.getClientInfo().isAuthenticated());
    }

    @Test
    void beforeHandle_whenAuthenticateFailsAndOptional_returnsContinueAndSetsOkStatus() {
        TestAuthenticator authenticator = new TestAuthenticator(null, true, true, null);
        authenticator.setAuthenticateResult(false);
        Request request = new Request();
        Response response = new Response(request);

        int result = authenticator.beforeHandle(request, response);

        assertEquals(Filter.CONTINUE, result);
        assertEquals(Status.SUCCESS_OK, response.getStatus());
    }

    @Test
    void beforeHandle_whenAlreadyAuthenticatedAndNotMultiAuthenticating_skipsAuthenticate() {
        TestAuthenticator authenticator = new TestAuthenticator(null, false, false, null);
        authenticator.setAuthenticateResult(true);
        Request request = new Request();
        request.getClientInfo().setAuthenticated(true);
        Response response = new Response(request);

        int result = authenticator.beforeHandle(request, response);

        assertEquals(Filter.CONTINUE, result);
        assertEquals(0, authenticator.getAuthenticateCallCount());
    }

    @Test
    void beforeHandle_whenAlreadyAuthenticatedAndMultiAuthenticating_reAuthenticates() {
        TestAuthenticator authenticator = new TestAuthenticator(null, true, false, null);
        authenticator.setAuthenticateResult(true);
        Request request = new Request();
        request.getClientInfo().setAuthenticated(true);
        Response response = new Response(request);

        authenticator.beforeHandle(request, response);

        assertEquals(1, authenticator.getAuthenticateCallCount());
    }

    @Test
    void authenticated_clearsChallengeRequestsAndInvokesEnroler() {
        java.util.concurrent.atomic.AtomicBoolean enroled =
                new java.util.concurrent.atomic.AtomicBoolean(false);
        Enroler enroler = clientInfo -> enroled.set(true);
        TestAuthenticator authenticator = new TestAuthenticator(null, true, false, enroler);
        Request request = new Request();
        Response response = new Response(request);
        response.getChallengeRequests()
                .add(
                        new org.restlet.data.ChallengeRequest(
                                org.restlet.data.ChallengeScheme.HTTP_BASIC, "realm"));

        int result = authenticator.authenticated(request, response);

        assertEquals(Filter.CONTINUE, result);
        assertTrue(request.getClientInfo().isAuthenticated());
        assertTrue(response.getChallengeRequests().isEmpty());
        assertTrue(enroled.get());
    }

    @Test
    void unauthenticated_marksUnauthenticatedAndReturnsStop() {
        TestAuthenticator authenticator = new TestAuthenticator(null, true, false, null);
        Request request = new Request();
        request.getClientInfo().setAuthenticated(true);
        Response response = new Response(request);

        int result = authenticator.unauthenticated(request, response);

        assertEquals(Filter.STOP, result);
        assertFalse(request.getClientInfo().isAuthenticated());
    }
}
