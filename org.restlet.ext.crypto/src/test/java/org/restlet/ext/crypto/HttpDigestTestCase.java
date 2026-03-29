/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.crypto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.restlet.data.ChallengeScheme.HTTP_DIGEST;
import static org.restlet.engine.security.AuthenticatorUtils.formatResponse;
import static org.restlet.engine.security.AuthenticatorUtils.parseResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeRequest;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.routing.Router;
import org.restlet.security.MapVerifier;

/**
 * Restlet unit tests for HTTP DIGEST authentication client/server.
 *
 * @author Jerome Louvel
 */
class HttpDigestTestCase {

    @Test
    void testDigest() {

        // Try unauthenticated request
        Request request = new Request(Method.GET, "/");
        Response response = testApplication.handle(request);
        assertEquals(Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());

        ChallengeRequest httpDigestChallengeRequest =
                response.getChallengeRequests().stream()
                        .filter(cr -> HTTP_DIGEST.equals(cr.getScheme()))
                        .findFirst()
                        .orElse(null);
        assertNotNull(httpDigestChallengeRequest);

        String realm = httpDigestChallengeRequest.getRealm();
        assertEquals("TestRealm", realm);

        // String opaque = httpDigestChallengeRequest.getParameters().getFirstValue("opaque");
        // String qop = httpDigestChallengeRequest.getParameters().getFirstValue("qop");
        // assertEquals(null, opaque);
        // assertEquals("auth", qop);

        // Try authenticated request
        request = new Request(Method.GET, "/");

        ChallengeResponse challengeResponseAsDefinedByClient =
                new ChallengeResponse(
                        httpDigestChallengeRequest, response, "scott", "tiger".toCharArray());
        String authHeaderAsSentByHttpClient =
                formatResponse(challengeResponseAsDefinedByClient, request, request.getHeaders());
        ChallengeResponse challengeResponseAsParsedByServer =
                parseResponse(request, authHeaderAsSentByHttpClient, request.getHeaders());

        request.setChallengeResponse(challengeResponseAsParsedByServer);
        response = testApplication.handle(request);
        assertTrue(response.getStatus().isSuccess());
    }

    private Application testApplication;

    @BeforeEach
    protected void setUpEach() {
        Engine.clearThreadLocalVariables();
        testApplication = new MyApplication();
    }

    private static class MyApplication extends Application {
        @Override
        public Restlet createInboundRoot() {
            Router router = new Router(getContext());

            DigestAuthenticator authenticator =
                    new DigestAuthenticator(getContext(), "TestRealm", "mySecretServerKey");
            MapVerifier mapVerifier = new MapVerifier();
            mapVerifier.getLocalSecrets().put("scott", "tiger".toCharArray());
            authenticator.setWrappedVerifier(mapVerifier);

            Restlet restlet =
                    new Restlet(getContext()) {
                        @Override
                        public void handle(Request request, Response response) {
                            response.setEntity("hello, world", MediaType.TEXT_PLAIN);
                        }
                    };
            authenticator.setNext(restlet);
            router.attach("/", authenticator);
            return router;
        }
    }
}
