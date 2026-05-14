/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.jaas;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.security.Verifier;

/** Unit tests for {@link JaasVerifier}. */
class JaasVerifierTestCase {

    @Test
    void testConstructor() {
        JaasVerifier verifier = new JaasVerifier("myApp");
        assertEquals("myApp", verifier.getName());
        assertNull(verifier.getConfiguration());
        assertNull(verifier.getUserPrincipalClassName());
    }

    @Test
    void testSetName() {
        JaasVerifier verifier = new JaasVerifier("initial");
        verifier.setName("updated");
        assertEquals("updated", verifier.getName());
    }

    @Test
    void testSetUserPrincipalClassName() {
        JaasVerifier verifier = new JaasVerifier("myApp");
        verifier.setUserPrincipalClassName("com.example.MyPrincipal");
        assertEquals("com.example.MyPrincipal", verifier.getUserPrincipalClassName());
    }

    @Test
    void testCreateCallbackHandler() {
        JaasVerifier verifier = new JaasVerifier("myApp");
        Request request = new Request(Method.GET, new Reference("http://localhost/test"));
        Response response = new Response(request);

        var handler = verifier.createCallbackHandler(request, response);

        assertNotNull(handler);
        assertInstanceOf(ChallengeCallbackHandler.class, handler);
    }

    @Test
    void testVerifyReturnsInvalidWhenLoginFails() {
        JaasVerifier verifier = new JaasVerifier("nonExistentLoginModule");
        Request request = new Request(Method.GET, new Reference("http://localhost/test"));
        Response response = new Response(request);

        int result = verifier.verify(request, response);

        assertEquals(Verifier.RESULT_INVALID, result);
    }
}
