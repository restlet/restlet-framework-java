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
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Status;

/** Unit tests for {@link ChallengeAuthenticator}. */
class ChallengeAuthenticatorTestCase {

    /** Verifier returning a fixed result, for exercising each authenticate() branch. */
    private static class FixedResultVerifier implements Verifier {
        private final int result;

        FixedResultVerifier(int result) {
            this.result = result;
        }

        @Override
        public int verify(Request request, Response response) {
            return result;
        }
    }

    @Test
    void constructorWithRealmAndVerifier_setsFields() {
        Verifier verifier = new FixedResultVerifier(Verifier.RESULT_VALID);
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(
                        null, true, ChallengeScheme.HTTP_BASIC, "realm", verifier);

        assertTrue(authenticator.isOptional());
        assertEquals(ChallengeScheme.HTTP_BASIC, authenticator.getScheme());
        assertEquals("realm", authenticator.getRealm());
        assertSame(verifier, authenticator.getVerifier());
        assertTrue(authenticator.isRechallenging());
    }

    @Test
    void constructorWithoutOptional_defaultsToRequired() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "realm");
        assertFalse(authenticator.isOptional());
    }

    @Test
    void constructorWithContext_usesContextDefaultVerifier() {
        Verifier verifier = new FixedResultVerifier(Verifier.RESULT_VALID);
        Context context = new Context();
        context.setDefaultVerifier(verifier);

        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(context, true, ChallengeScheme.HTTP_BASIC, "realm");

        assertSame(verifier, authenticator.getVerifier());
    }

    @Test
    void authenticate_validCredentials_returnsTrue() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(
                        null,
                        false,
                        ChallengeScheme.HTTP_BASIC,
                        "realm",
                        new FixedResultVerifier(Verifier.RESULT_VALID));
        Request request = new Request();
        Response response = new Response(request);

        assertTrue(authenticator.authenticate(request, response));
    }

    @Test
    void authenticate_missingCredentialsNotOptional_challengesAndReturnsFalse() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(
                        null,
                        false,
                        ChallengeScheme.HTTP_BASIC,
                        "realm",
                        new FixedResultVerifier(Verifier.RESULT_MISSING));
        Request request = new Request();
        Response response = new Response(request);

        boolean result = authenticator.authenticate(request, response);

        assertFalse(result);
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
        assertEquals(1, response.getChallengeRequests().size());
    }

    @Test
    void authenticate_missingCredentialsOptional_doesNotChallenge() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(
                        null,
                        true,
                        ChallengeScheme.HTTP_BASIC,
                        "realm",
                        new FixedResultVerifier(Verifier.RESULT_MISSING));
        Request request = new Request();
        Response response = new Response(request);

        boolean result = authenticator.authenticate(request, response);

        assertFalse(result);
        assertTrue(response.getChallengeRequests().isEmpty());
    }

    @Test
    void authenticate_invalidCredentialsRechallenging_callsChallenge() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(
                        null,
                        false,
                        ChallengeScheme.HTTP_BASIC,
                        "realm",
                        new FixedResultVerifier(Verifier.RESULT_INVALID));
        Request request = new Request();
        Response response = new Response(request);

        boolean result = authenticator.authenticate(request, response);

        assertFalse(result);
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
        assertEquals(1, response.getChallengeRequests().size());
    }

    @Test
    void authenticate_invalidCredentialsNotRechallenging_callsForbid() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(
                        null,
                        false,
                        ChallengeScheme.HTTP_BASIC,
                        "realm",
                        new FixedResultVerifier(Verifier.RESULT_INVALID));
        authenticator.setRechallenging(false);
        Request request = new Request();
        Response response = new Response(request);

        boolean result = authenticator.authenticate(request, response);

        assertFalse(result);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
        assertTrue(response.getChallengeRequests().isEmpty());
    }

    @Test
    void authenticate_staleCredentials_challengesWithStaleFlag() {
        final boolean[] staleFlag = new boolean[1];
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(
                        null,
                        false,
                        ChallengeScheme.HTTP_BASIC,
                        "realm",
                        new FixedResultVerifier(Verifier.RESULT_STALE)) {
                    @Override
                    protected ChallengeRequest createChallengeRequest(boolean stale) {
                        staleFlag[0] = stale;
                        return super.createChallengeRequest(stale);
                    }
                };
        Request request = new Request();
        Response response = new Response(request);

        boolean result = authenticator.authenticate(request, response);

        assertFalse(result);
        assertTrue(staleFlag[0]);
        assertEquals(1, response.getChallengeRequests().size());
    }

    @Test
    void authenticate_unknownIdentifierRechallenging_callsChallenge() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(
                        null,
                        false,
                        ChallengeScheme.HTTP_BASIC,
                        "realm",
                        new FixedResultVerifier(Verifier.RESULT_UNKNOWN));
        Request request = new Request();
        Response response = new Response(request);

        boolean result = authenticator.authenticate(request, response);

        assertFalse(result);
        assertEquals(1, response.getChallengeRequests().size());
    }

    @Test
    void authenticate_unknownIdentifierNotRechallenging_callsForbid() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(
                        null,
                        false,
                        ChallengeScheme.HTTP_BASIC,
                        "realm",
                        new FixedResultVerifier(Verifier.RESULT_UNKNOWN));
        authenticator.setRechallenging(false);
        Request request = new Request();
        Response response = new Response(request);

        boolean result = authenticator.authenticate(request, response);

        assertFalse(result);
        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
    }

    @Test
    void authenticate_withoutVerifier_setsInternalServerError() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(null, false, ChallengeScheme.HTTP_BASIC, "realm", null);
        Request request = new Request();
        Response response = new Response(request);

        boolean result = authenticator.authenticate(request, response);

        assertFalse(result);
        assertEquals(Status.SERVER_ERROR_INTERNAL, response.getStatus());
    }

    @Test
    void challenge_addsChallengeRequestAndSetsUnauthorizedStatus() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "realm");
        Request request = new Request();
        Response response = new Response(request);

        authenticator.challenge(response, false);

        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
        assertEquals(1, response.getChallengeRequests().size());
        assertEquals(
                ChallengeScheme.HTTP_BASIC, response.getChallengeRequests().getFirst().getScheme());
        assertEquals("realm", response.getChallengeRequests().getFirst().getRealm());
    }

    @Test
    void forbid_setsForbiddenStatus() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "realm");
        Request request = new Request();
        Response response = new Response(request);

        authenticator.forbid(response);

        assertEquals(Status.CLIENT_ERROR_FORBIDDEN, response.getStatus());
    }

    @Test
    void createChallengeRequest_returnsRequestWithSchemeAndRealm() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "realm");

        ChallengeRequest challengeRequest = authenticator.createChallengeRequest(true);

        assertEquals(ChallengeScheme.HTTP_BASIC, challengeRequest.getScheme());
        assertEquals("realm", challengeRequest.getRealm());
    }

    @Test
    void getRealmSetRealm_roundTrip() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "realm");
        authenticator.setRealm("other-realm");
        assertEquals("other-realm", authenticator.getRealm());
    }

    @Test
    void getVerifierSetVerifier_roundTrip() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "realm");
        assertNull(authenticator.getVerifier());

        Verifier verifier = new FixedResultVerifier(Verifier.RESULT_VALID);
        authenticator.setVerifier(verifier);
        assertSame(verifier, authenticator.getVerifier());
    }

    @Test
    void isRechallengingSetRechallenging_roundTrip() {
        ChallengeAuthenticator authenticator =
                new ChallengeAuthenticator(null, ChallengeScheme.HTTP_BASIC, "realm");
        assertTrue(authenticator.isRechallenging());
        authenticator.setRechallenging(false);
        assertFalse(authenticator.isRechallenging());
    }
}
