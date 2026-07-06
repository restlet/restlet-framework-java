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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;

/** Unit tests for {@link MapVerifier}. */
class MapVerifierTestCase {

    @Test
    void defaultConstructor_hasEmptyLocalSecrets() {
        MapVerifier verifier = new MapVerifier();
        assertTrue(verifier.getLocalSecrets().isEmpty());
    }

    @Test
    void constructorWithMap_usesProvidedMap() {
        ConcurrentMap<String, char[]> secrets = new ConcurrentHashMap<>();
        secrets.put("alice", "secret".toCharArray());

        MapVerifier verifier = new MapVerifier(secrets);

        assertSame(secrets, verifier.getLocalSecrets());
    }

    @Test
    void getLocalSecret_withNullIdentifier_returnsNull() {
        MapVerifier verifier = new MapVerifier();
        assertNull(verifier.getLocalSecret(null));
    }

    @Test
    void getLocalSecret_withKnownIdentifier_returnsSecret() {
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("alice", "secret".toCharArray());

        assertEquals("secret", new String(verifier.getLocalSecret("alice")));
    }

    @Test
    void getLocalSecret_withUnknownIdentifier_returnsNull() {
        MapVerifier verifier = new MapVerifier();
        assertNull(verifier.getLocalSecret("unknown"));
    }

    @Test
    void setLocalSecrets_withNull_clearsMap() {
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("alice", "secret".toCharArray());

        verifier.setLocalSecrets(null);

        assertTrue(verifier.getLocalSecrets().isEmpty());
    }

    @Test
    void setLocalSecrets_withNewMap_replacesContents() {
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("old", "secret".toCharArray());

        verifier.setLocalSecrets(Map.of("new", "other".toCharArray()));

        assertEquals(1, verifier.getLocalSecrets().size());
        assertTrue(verifier.getLocalSecrets().containsKey("new"));
    }

    @Test
    void setLocalSecrets_withSameMapInstance_isNoOp() {
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("alice", "secret".toCharArray());

        verifier.setLocalSecrets(verifier.getLocalSecrets());

        assertEquals(1, verifier.getLocalSecrets().size());
    }

    @Test
    void verifyIdentifierSecret_withMatchingSecret_returnsValid() {
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("alice", "secret".toCharArray());

        assertEquals(Verifier.RESULT_VALID, verifier.verify("alice", "secret".toCharArray()));
    }

    @Test
    void verifyIdentifierSecret_withWrongSecret_returnsInvalid() {
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("alice", "secret".toCharArray());

        assertEquals(Verifier.RESULT_INVALID, verifier.verify("alice", "wrong".toCharArray()));
    }

    @Test
    void verifyRequestResponse_withMissingChallengeResponse_returnsMissing() {
        MapVerifier verifier = new MapVerifier();
        Request request = new Request();
        Response response = new Response(request);

        assertEquals(Verifier.RESULT_MISSING, verifier.verify(request, response));
    }

    @Test
    void verifyRequestResponse_withValidCredentials_setsUserOnClientInfo() {
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("alice", "secret".toCharArray());
        Request request = new Request();
        request.setChallengeResponse(
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "alice", "secret"));
        Response response = new Response(request);

        int result = verifier.verify(request, response);

        assertEquals(Verifier.RESULT_VALID, result);
        assertEquals("alice", request.getClientInfo().getUser().getIdentifier());
    }

    @Test
    void verifyRequestResponse_withInvalidCredentials_doesNotSetUser() {
        MapVerifier verifier = new MapVerifier();
        verifier.getLocalSecrets().put("alice", "secret".toCharArray());
        Request request = new Request();
        request.setChallengeResponse(
                new ChallengeResponse(ChallengeScheme.HTTP_BASIC, "alice", "wrong"));
        Response response = new Response(request);

        int result = verifier.verify(request, response);

        assertEquals(Verifier.RESULT_INVALID, result);
        assertNull(request.getClientInfo().getUser());
    }
}
