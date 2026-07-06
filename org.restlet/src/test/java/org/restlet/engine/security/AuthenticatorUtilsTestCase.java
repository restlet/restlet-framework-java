/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.AuthenticationInfo;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;

class AuthenticatorUtilsTestCase {

    @Test
    void anyNull_noNulls_returnsFalse() {
        assertFalse(AuthenticatorUtils.anyNull("a", "b", 1));
    }

    @Test
    void anyNull_someNull_returnsTrue() {
        assertTrue(AuthenticatorUtils.anyNull("a", null, 1));
    }

    @Test
    void formatNonceCount_padsToEightHexCharacters() {
        assertEquals("00000001", AuthenticatorUtils.formatNonceCount(1));
        assertEquals("000000ff", AuthenticatorUtils.formatNonceCount(255));
    }

    @Test
    void formatAuthenticationInfo_null_returnsEmptyString() {
        assertEquals("", AuthenticatorUtils.formatAuthenticationInfo(null));
    }

    @Test
    void formatAuthenticationInfo_allFieldsSet_formatsAllParameters() {
        AuthenticationInfo info = new AuthenticationInfo("next", 5, "client", "auth", "digest");
        String result = AuthenticatorUtils.formatAuthenticationInfo(info);
        assertTrue(result.contains("nextnonce=\"next\""));
        assertTrue(result.contains("qop=auth"));
        assertTrue(result.contains("nc=00000005"));
        assertTrue(result.contains("rspauth=\"digest\""));
        assertTrue(result.contains("cnonce=client"));
    }

    @Test
    void formatAuthenticationInfo_emptyFields_areOmitted() {
        AuthenticationInfo info = new AuthenticationInfo("", 0, "", "", "");
        assertEquals("", AuthenticatorUtils.formatAuthenticationInfo(info));
    }

    @Test
    void formatRequest_nullChallenge_returnsNull() {
        assertNull(AuthenticatorUtils.formatRequest(null, new Response(new Request()), null));
    }

    @Test
    void formatRequest_nullScheme_returnsNull() {
        ChallengeRequest challenge = new ChallengeRequest((ChallengeScheme) null);
        assertNull(AuthenticatorUtils.formatRequest(challenge, new Response(new Request()), null));
    }

    @Test
    void formatRequest_rawValuePresent_isUsedDirectly() {
        ChallengeRequest challenge = new ChallengeRequest(ChallengeScheme.HTTP_BASIC);
        challenge.setRawValue("realm=\"test\"");
        String result =
                AuthenticatorUtils.formatRequest(challenge, new Response(new Request()), null);
        assertEquals("Basic realm=\"test\"", result);
    }

    @Test
    void formatResponse_nullChallenge_returnsNull() {
        assertNull(AuthenticatorUtils.formatResponse(null, new Request(), null));
    }

    @Test
    void formatResponse_nullScheme_returnsNull() {
        ChallengeResponse challenge = new ChallengeResponse((ChallengeScheme) null);
        assertNull(AuthenticatorUtils.formatResponse(challenge, new Request(), null));
    }

    @Test
    void formatResponse_rawValuePresent_isUsedDirectly() {
        ChallengeResponse challenge = new ChallengeResponse(ChallengeScheme.HTTP_BASIC);
        challenge.setRawValue("dXNlcjpwYXNz");
        String result = AuthenticatorUtils.formatResponse(challenge, new Request(), null);
        assertEquals("Basic dXNlcjpwYXNz", result);
    }

    @Test
    void parseAuthenticationInfo_allFields_parsesEachParameter() {
        AuthenticationInfo info =
                AuthenticatorUtils.parseAuthenticationInfo(
                        "nextnonce=\"next\", qop=auth, nc=00000005, rspauth=\"digest\", cnonce=client");
        assertEquals("next", info.getNextServerNonce());
        assertEquals("auth", info.getQuality());
        assertEquals(5, info.getNonceCount());
        assertEquals("digest", info.getResponseDigest());
        assertEquals("client", info.getClientNonce());
    }

    @Test
    void parseAuthenticationInfo_invalidNonceCount_defaultsToZero() {
        AuthenticationInfo info = AuthenticatorUtils.parseAuthenticationInfo("nc=notahexnumber");
        assertEquals(0, info.getNonceCount());
    }

    @Test
    void parseAuthenticationInfo_unparseableHeader_returnsNull() {
        assertNull(AuthenticatorUtils.parseAuthenticationInfo(""));
    }

    @Test
    void parseRequest_nullHeader_returnsEmptyList() {
        assertTrue(
                AuthenticatorUtils.parseRequest(new Response(new Request()), null, null).isEmpty());
    }

    @Test
    void parseResponse_nullHeader_returnsNull() {
        assertNull(AuthenticatorUtils.parseResponse(new Request(), null, null));
    }

    @Test
    void parseResponse_headerWithoutSpace_returnsNull() {
        assertNull(AuthenticatorUtils.parseResponse(new Request(), "NoSpaceHeader", null));
    }

    @Test
    void parseResponse_validHeader_parsesSchemeAndRawValue() {
        ChallengeResponse result =
                AuthenticatorUtils.parseResponse(new Request(), "Basic dXNlcjpwYXNz", null);
        assertEquals("Basic", result.getScheme().getTechnicalName());
        assertEquals("dXNlcjpwYXNz", result.getRawValue());
    }

    @Test
    void update_noMatchingChallengeRequest_leavesRealmAndNonceNull() {
        Request request = new Request(Method.GET, "http://example.com/path");
        Response response = new Response(request);
        ChallengeResponse challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC);

        AuthenticatorUtils.update(challengeResponse, request, response);

        assertNull(challengeResponse.getRealm());
        assertNull(challengeResponse.getServerNonce());
    }

    @Test
    void update_matchingChallengeRequest_copiesRealmAndNonce() {
        Request request = new Request(Method.GET, "http://example.com/path");
        Response response = new Response(request);
        ChallengeRequest challengeRequest = new ChallengeRequest(ChallengeScheme.HTTP_DIGEST);
        challengeRequest.setRealm("test-realm");
        challengeRequest.setServerNonce("test-nonce");
        response.getChallengeRequests().add(challengeRequest);

        ChallengeResponse challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_DIGEST);
        AuthenticatorUtils.update(challengeResponse, request, response);

        assertEquals("test-realm", challengeResponse.getRealm());
        assertEquals("test-nonce", challengeResponse.getServerNonce());
    }

    @Test
    void updateReference_noChallengeResponse_returnsUnchangedReference() {
        Request request = new Request(Method.GET, "http://example.com/path");
        assertEquals(
                request.getResourceRef(),
                AuthenticatorUtils.updateReference(request.getResourceRef(), null, request));
    }

    @Test
    void updateReference_rawValuePresent_returnsUnchangedReference() {
        Request request = new Request(Method.GET, "http://example.com/path");
        ChallengeResponse challengeResponse = new ChallengeResponse(ChallengeScheme.HTTP_BASIC);
        challengeResponse.setRawValue("dXNlcjpwYXNz");
        assertEquals(
                request.getResourceRef(),
                AuthenticatorUtils.updateReference(
                        request.getResourceRef(), challengeResponse, request));
    }
}
