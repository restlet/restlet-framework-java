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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.routing.Filter;

/** Unit tests for {@link Authorizer}. */
class AuthorizerTestCase {

    /** Minimal concrete fixture, since {@link Authorizer} is abstract. */
    private static class TestAuthorizer extends Authorizer {

        private boolean authorizeResult;

        TestAuthorizer() {
            super();
        }

        TestAuthorizer(String identifier) {
            super(identifier);
        }

        void setAuthorizeResult(boolean authorizeResult) {
            this.authorizeResult = authorizeResult;
        }

        @Override
        protected boolean authorize(Request request, Response response) {
            return authorizeResult;
        }
    }

    @Test
    void defaultConstructor_hasNullIdentifier() {
        TestAuthorizer authorizer = new TestAuthorizer();
        assertNull(authorizer.getIdentifier());
    }

    @Test
    void constructorWithIdentifier_setsIdentifier() {
        TestAuthorizer authorizer = new TestAuthorizer("my-id");
        assertEquals("my-id", authorizer.getIdentifier());
    }

    @Test
    void getIdentifierSetIdentifier_roundTrip() {
        TestAuthorizer authorizer = new TestAuthorizer();
        authorizer.setIdentifier("another-id");
        assertEquals("another-id", authorizer.getIdentifier());
    }

    @Test
    void authorized_default_returnsContinue() {
        TestAuthorizer authorizer = new TestAuthorizer();
        Request request = new Request();
        Response response = new Response(request);

        assertEquals(Filter.CONTINUE, authorizer.authorized(request, response));
    }

    @Test
    void unauthorized_default_setsForbiddenStatusAndReturnsStop() {
        TestAuthorizer authorizer = new TestAuthorizer();
        Request request = new Request();
        Response response = new Response(request);

        int result = authorizer.unauthorized(request, response);

        assertEquals(Filter.STOP, result);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
    }

    @Test
    void beforeHandle_whenAuthorizeSucceeds_returnsContinue() {
        TestAuthorizer authorizer = new TestAuthorizer();
        authorizer.setAuthorizeResult(true);
        Request request = new Request();
        Response response = new Response(request);

        assertEquals(Filter.CONTINUE, authorizer.beforeHandle(request, response));
    }

    @Test
    void beforeHandle_whenAuthorizeFails_setsForbiddenAndReturnsStop() {
        TestAuthorizer authorizer = new TestAuthorizer();
        authorizer.setAuthorizeResult(false);
        Request request = new Request();
        Response response = new Response(request);

        int result = authorizer.beforeHandle(request, response);

        assertEquals(Filter.STOP, result);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
    }

    @Test
    void always_authorize_returnsTrue() {
        Request request = new Request();
        Response response = new Response(request);

        assertTrue(Authorizer.ALWAYS.authorize(request, response));
    }

    @Test
    void never_authorize_returnsFalse() {
        Request request = new Request();
        Response response = new Response(request);

        assertFalse(Authorizer.NEVER.authorize(request, response));
    }

    @Test
    void authenticated_authorize_returnsTrueOnlyWhenClientAuthenticated() {
        Request request = new Request();
        Response response = new Response(request);
        assertFalse(Authorizer.AUTHENTICATED.authorize(request, response));

        request.getClientInfo().setAuthenticated(true);
        assertTrue(Authorizer.AUTHENTICATED.authorize(request, response));
    }

    @Test
    void authenticated_beforeHandle_whenNotAuthenticated_setsUnauthorizedStatus() {
        Request request = new Request();
        Response response = new Response(request);

        int result = Authorizer.AUTHENTICATED.beforeHandle(request, response);

        assertEquals(Filter.STOP, result);
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
    }

    @Test
    void authenticated_beforeHandle_whenAuthenticated_returnsContinue() {
        Request request = new Request();
        request.getClientInfo().setAuthenticated(true);
        Response response = new Response(request);

        int result = Authorizer.AUTHENTICATED.beforeHandle(request, response);

        assertEquals(Filter.CONTINUE, result);
    }
}
