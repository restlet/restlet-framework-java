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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;

/** Unit tests for {@link MethodAuthorizer}. */
class MethodAuthorizerTestCase {

    private Request requestWith(Method method, boolean authenticated) {
        Request request = new Request();
        request.setMethod(method);
        request.getClientInfo().setAuthenticated(authenticated);
        return request;
    }

    @Test
    void defaultConstructor_hasEmptyMethodLists() {
        MethodAuthorizer authorizer = new MethodAuthorizer();
        assertTrue(authorizer.getAnonymousMethods().isEmpty());
        assertTrue(authorizer.getAuthenticatedMethods().isEmpty());
    }

    @Test
    void constructorWithIdentifier_setsIdentifier() {
        MethodAuthorizer authorizer = new MethodAuthorizer("my-id");
        assertEquals("my-id", authorizer.getIdentifier());
    }

    @Test
    void authorize_anonymousWithAllowedMethod_returnsTrue() {
        MethodAuthorizer authorizer = new MethodAuthorizer();
        authorizer.setAnonymousMethods(List.of(Method.GET));

        Request request = requestWith(Method.GET, false);
        assertTrue(authorizer.authorize(request, new Response(request)));
    }

    @Test
    void authorize_anonymousWithDisallowedMethod_returnsFalse() {
        MethodAuthorizer authorizer = new MethodAuthorizer();
        authorizer.setAnonymousMethods(List.of(Method.GET));

        Request request = requestWith(Method.POST, false);
        assertFalse(authorizer.authorize(request, new Response(request)));
    }

    @Test
    void authorize_authenticatedWithAllowedMethod_returnsTrue() {
        MethodAuthorizer authorizer = new MethodAuthorizer();
        authorizer.setAuthenticatedMethods(List.of(Method.PUT));

        Request request = requestWith(Method.PUT, true);
        assertTrue(authorizer.authorize(request, new Response(request)));
    }

    @Test
    void authorize_authenticatedWithDisallowedMethod_returnsFalse() {
        MethodAuthorizer authorizer = new MethodAuthorizer();
        authorizer.setAuthenticatedMethods(List.of(Method.PUT));

        Request request = requestWith(Method.DELETE, true);
        assertFalse(authorizer.authorize(request, new Response(request)));
    }

    @Test
    void setAnonymousMethods_withNull_clearsList() {
        MethodAuthorizer authorizer = new MethodAuthorizer();
        authorizer.setAnonymousMethods(List.of(Method.GET));
        authorizer.setAnonymousMethods(null);
        assertTrue(authorizer.getAnonymousMethods().isEmpty());
    }

    @Test
    void setAnonymousMethods_withSameListInstance_isNoOp() {
        MethodAuthorizer authorizer = new MethodAuthorizer();
        authorizer.setAnonymousMethods(authorizer.getAnonymousMethods());
        assertTrue(authorizer.getAnonymousMethods().isEmpty());
    }

    @Test
    void setAuthenticatedMethods_withNull_clearsList() {
        MethodAuthorizer authorizer = new MethodAuthorizer();
        authorizer.setAuthenticatedMethods(List.of(Method.PUT));
        authorizer.setAuthenticatedMethods(null);
        assertTrue(authorizer.getAuthenticatedMethods().isEmpty());
    }

    @Test
    void setAuthenticatedMethods_withSameListInstance_isNoOp() {
        MethodAuthorizer authorizer = new MethodAuthorizer();
        authorizer.setAuthenticatedMethods(authorizer.getAuthenticatedMethods());
        assertTrue(authorizer.getAuthenticatedMethods().isEmpty());
    }
}
