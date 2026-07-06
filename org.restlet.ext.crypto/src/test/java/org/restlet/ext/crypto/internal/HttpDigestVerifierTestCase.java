/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.crypto.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Digest;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.engine.security.AuthenticatorUtils;
import org.restlet.ext.crypto.DigestAuthenticator;
import org.restlet.ext.crypto.DigestUtils;
import org.restlet.security.MapVerifier;
import org.restlet.security.Verifier;

/** Unit tests for {@link HttpDigestVerifier}. */
class HttpDigestVerifierTestCase {

    private static final String REALM = "TestRealm";

    private static final String SERVER_KEY = "mySecretServerKey";

    private static final String RESOURCE_PATH = "/protected/resource";

    private DigestAuthenticator authenticator;

    private HttpDigestVerifier verifier;

    private String serverNonce;

    private String ha1;

    private Request createRequest() {
        return new Request(Method.GET, "http://example.com" + RESOURCE_PATH);
    }

    private String computeResponse(String ha1Value, String nonce, String requestUri) {
        String ha2 = DigestUtils.toMd5(Method.GET.toString() + ":" + requestUri);
        return DigestUtils.toMd5(ha1Value + ":" + nonce + ":" + ha2);
    }

    private String computeResponseWithQop(
            String ha1Value,
            String nonce,
            String nc,
            String cnonce,
            String qop,
            String requestUri) {
        String ha2 = DigestUtils.toMd5(Method.GET.toString() + ":" + requestUri);
        return DigestUtils.toMd5(
                ha1Value
                        + ":"
                        + nonce
                        + ":"
                        + AuthenticatorUtils.formatNonceCount(Integer.parseInt(nc))
                        + ":"
                        + cnonce
                        + ":"
                        + qop
                        + ":"
                        + ha2);
    }

    private ChallengeResponse createChallengeResponse(
            String identifier,
            char[] responseSecret,
            String quality,
            Reference digestRef,
            String clientNonce,
            String serverNonceValue,
            int serverNonceCount) {
        return new ChallengeResponse(
                ChallengeScheme.HTTP_DIGEST,
                null,
                identifier,
                responseSecret,
                Digest.ALGORITHM_NONE,
                REALM,
                quality,
                digestRef,
                null,
                null,
                clientNonce,
                serverNonceValue,
                serverNonceCount,
                0L);
    }

    @BeforeEach
    void setUpEach() {
        authenticator = new DigestAuthenticator(new Context(), REALM, SERVER_KEY);
        MapVerifier mapVerifier = new MapVerifier();
        mapVerifier.getLocalSecrets().put("scott", "tiger".toCharArray());
        authenticator.setWrappedVerifier(mapVerifier);
        verifier = (HttpDigestVerifier) authenticator.getVerifier();

        serverNonce = authenticator.generateServerNonce();
        ha1 = DigestUtils.toHttpDigest("scott", "tiger".toCharArray(), REALM);
    }

    @Test
    void verify_noChallengeResponse_returnsMissing() {
        Request request = createRequest();
        Response response = new Response(request);

        assertEquals(Verifier.RESULT_MISSING, verifier.verify(request, response));
    }

    @Test
    void verify_nullSecret_returnsInvalid() {
        Request request = createRequest();
        Response response = new Response(request);
        ChallengeResponse cr =
                createChallengeResponse(
                        "scott", null, null, new Reference(RESOURCE_PATH), null, serverNonce, 0);
        request.setChallengeResponse(cr);

        assertEquals(Verifier.RESULT_INVALID, verifier.verify(request, response));
    }

    @Test
    void verify_serverNonceForgedWithWrongSecretKey_returnsInvalid() {
        Request request = createRequest();
        Response response = new Response(request);
        String forgedNonce = CryptoUtils.makeNonce("wrongServerKey");
        String expected = computeResponse(ha1, forgedNonce, RESOURCE_PATH);
        ChallengeResponse cr =
                createChallengeResponse(
                        "scott",
                        expected.toCharArray(),
                        null,
                        new Reference(RESOURCE_PATH),
                        null,
                        forgedNonce,
                        0);
        request.setChallengeResponse(cr);

        assertEquals(Verifier.RESULT_INVALID, verifier.verify(request, response));
    }

    @Test
    void verify_staleServerNonce_returnsStale() {
        authenticator.setMaxServerNonceAge(-1L);
        Request request = createRequest();
        Response response = new Response(request);
        String expected = computeResponse(ha1, serverNonce, RESOURCE_PATH);
        ChallengeResponse cr =
                createChallengeResponse(
                        "scott",
                        expected.toCharArray(),
                        null,
                        new Reference(RESOURCE_PATH),
                        null,
                        serverNonce,
                        0);
        request.setChallengeResponse(cr);

        assertEquals(Verifier.RESULT_STALE, verifier.verify(request, response));
    }

    @Test
    void verify_missingDigestRef_returnsMissing() {
        Request request = createRequest();
        Response response = new Response(request);
        String expected = computeResponse(ha1, serverNonce, RESOURCE_PATH);
        ChallengeResponse cr =
                createChallengeResponse(
                        "scott", expected.toCharArray(), null, null, null, serverNonce, 0);
        request.setChallengeResponse(cr);

        assertEquals(Verifier.RESULT_MISSING, verifier.verify(request, response));
    }

    @Test
    void verify_digestRefDoesNotMatchRequestUri_returnsInvalid() {
        Request request = createRequest();
        Response response = new Response(request);
        String expected = computeResponse(ha1, serverNonce, RESOURCE_PATH);
        ChallengeResponse cr =
                createChallengeResponse(
                        "scott",
                        expected.toCharArray(),
                        null,
                        new Reference("/some/other/path"),
                        null,
                        serverNonce,
                        0);
        request.setChallengeResponse(cr);

        assertEquals(Verifier.RESULT_INVALID, verifier.verify(request, response));
    }

    @Test
    void verify_unknownIdentifier_returnsInvalid() {
        Request request = createRequest();
        Response response = new Response(request);
        String expected = computeResponse(ha1, serverNonce, RESOURCE_PATH);
        ChallengeResponse cr =
                createChallengeResponse(
                        "unknownUser",
                        expected.toCharArray(),
                        null,
                        new Reference(RESOURCE_PATH),
                        null,
                        serverNonce,
                        0);
        request.setChallengeResponse(cr);

        assertEquals(Verifier.RESULT_INVALID, verifier.verify(request, response));
    }

    @Test
    void verify_wrongPassword_returnsInvalid() {
        Request request = createRequest();
        Response response = new Response(request);
        String wrongHa1 = DigestUtils.toHttpDigest("scott", "wrongPassword".toCharArray(), REALM);
        String computedWithWrongPassword = computeResponse(wrongHa1, serverNonce, RESOURCE_PATH);
        ChallengeResponse cr =
                createChallengeResponse(
                        "scott",
                        computedWithWrongPassword.toCharArray(),
                        null,
                        new Reference(RESOURCE_PATH),
                        null,
                        serverNonce,
                        0);
        request.setChallengeResponse(cr);

        assertEquals(Verifier.RESULT_INVALID, verifier.verify(request, response));
    }

    @Test
    void verify_validCredentialsWithoutQop_returnsValid() {
        Request request = createRequest();
        Response response = new Response(request);
        String expected = computeResponse(ha1, serverNonce, RESOURCE_PATH);
        ChallengeResponse cr =
                createChallengeResponse(
                        "scott",
                        expected.toCharArray(),
                        null,
                        new Reference(RESOURCE_PATH),
                        null,
                        serverNonce,
                        0);
        request.setChallengeResponse(cr);

        assertEquals(Verifier.RESULT_VALID, verifier.verify(request, response));
        assertEquals("scott", request.getClientInfo().getUser().getIdentifier());
    }

    @Test
    void verify_validCredentialsWithQop_returnsValid() {
        Request request = createRequest();
        Response response = new Response(request);
        String nc = "1";
        String clientNonce = "abcdef0123456789";
        String qop = "auth";
        String expected =
                computeResponseWithQop(ha1, serverNonce, nc, clientNonce, qop, RESOURCE_PATH);
        ChallengeResponse cr =
                createChallengeResponse(
                        "scott",
                        expected.toCharArray(),
                        qop,
                        new Reference(RESOURCE_PATH),
                        clientNonce,
                        serverNonce,
                        Integer.parseInt(nc));
        request.setChallengeResponse(cr);

        assertEquals(Verifier.RESULT_VALID, verifier.verify(request, response));
    }

    @Test
    void digest_withNonHttpDigestAlgorithm_fallsBackToSuper() {
        verifier.setAlgorithm(Digest.ALGORITHM_MD5);
        char[] result = verifier.getWrappedSecretDigest("scott");

        assertEquals(DigestUtils.toMd5("tiger"), new String(result));
    }

    @Test
    void getDigestAuthenticator_returnsConstructorValue() {
        assertEquals(authenticator, verifier.getDigestAuthenticator());
    }

    @Test
    void setDigestAuthenticator_updatesGetter() {
        DigestAuthenticator other =
                new DigestAuthenticator(new Context(), "OtherRealm", "otherKey");

        verifier.setDigestAuthenticator(other);

        assertEquals(other, verifier.getDigestAuthenticator());
    }
}
