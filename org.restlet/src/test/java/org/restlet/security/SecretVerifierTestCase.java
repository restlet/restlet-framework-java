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
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;

/** Unit tests for {@link SecretVerifier}. */
class SecretVerifierTestCase {

    /** Minimal concrete fixture, since {@link SecretVerifier} is abstract. */
    private static class TestSecretVerifier extends SecretVerifier {

        @Override
        public int verify(String identifier, char[] secret) {
            return "alice".equals(identifier) && compare(secret, "secret".toCharArray())
                    ? RESULT_VALID
                    : RESULT_INVALID;
        }
    }

    @Test
    void compare_withEqualSecrets_returnsTrue() {
        assertTrue(SecretVerifier.compare("secret".toCharArray(), "secret".toCharArray()));
    }

    @Test
    void compare_withDifferentSecrets_returnsFalse() {
        assertFalse(SecretVerifier.compare("secret".toCharArray(), "other!".toCharArray()));
    }

    @Test
    void compare_withDifferentLengths_returnsFalse() {
        assertFalse(SecretVerifier.compare("secret".toCharArray(), "short".toCharArray()));
    }

    @Test
    void compare_withOneNullSecret_returnsFalse() {
        assertFalse(SecretVerifier.compare(null, "secret".toCharArray()));
        assertFalse(SecretVerifier.compare("secret".toCharArray(), null));
    }

    @Test
    void compare_withBothNullSecrets_returnsFalse() {
        assertFalse(SecretVerifier.compare(null, null));
    }

    @Test
    void createUser_returnsUserWithGivenIdentifier() {
        TestSecretVerifier verifier = new TestSecretVerifier();
        Request request = new Request();
        Response response = new Response(request);

        User user = verifier.createUser("alice", request, response);

        assertEquals("alice", user.getIdentifier());
    }

    @Test
    void getIdentifier_extractsFromChallengeResponse() {
        TestSecretVerifier verifier = new TestSecretVerifier();
        Request request = new Request();
        request.setChallengeResponse(
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "alice", "secret"));
        Response response = new Response(request);

        assertEquals("alice", verifier.getIdentifier(request, response));
    }

    @Test
    void getSecret_extractsFromChallengeResponse() {
        TestSecretVerifier verifier = new TestSecretVerifier();
        Request request = new Request();
        request.setChallengeResponse(
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "alice", "secret"));
        Response response = new Response(request);

        assertEquals("secret", new String(verifier.getSecret(request, response)));
    }

    @Test
    void verify_withoutChallengeResponse_returnsMissing() {
        TestSecretVerifier verifier = new TestSecretVerifier();
        Request request = new Request();
        Response response = new Response(request);

        assertEquals(Verifier.RESULT_MISSING, verifier.verify(request, response));
    }

    @Test
    void verify_withValidCredentials_setsUserOnClientInfo() {
        TestSecretVerifier verifier = new TestSecretVerifier();
        Request request = new Request();
        request.setChallengeResponse(
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "alice", "secret"));
        Response response = new Response(request);

        int result = verifier.verify(request, response);

        assertEquals(Verifier.RESULT_VALID, result);
        assertEquals("alice", request.getClientInfo().getUser().getIdentifier());
    }

    @Test
    void verify_withInvalidCredentials_doesNotSetUser() {
        TestSecretVerifier verifier = new TestSecretVerifier();
        Request request = new Request();
        request.setChallengeResponse(
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "alice", "wrong"));
        Response response = new Response(request);

        int result = verifier.verify(request, response);

        assertEquals(Verifier.RESULT_INVALID, result);
        assertNull(request.getClientInfo().getUser());
    }
}
