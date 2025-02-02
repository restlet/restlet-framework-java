/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.*;
import org.restlet.data.*;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Restlet unit tests for HTTP Basic authentication client/server.
 *
 * @author Stian Soiland
 * @author Jerome Louvel
 */
public class HttpBasicTestCase {

    public static class AuthenticatedRestlet extends Restlet {
        @Override
        public void handle(Request request, Response response) {
            response.setEntity(AUTHENTICATED_MSG, MediaType.TEXT_PLAIN);
        }
    }

    public static class TestVerifier extends MapVerifier {
        public TestVerifier() {
            getLocalSecrets().put(SHORT_USERNAME, SHORT_PASSWORD.toCharArray());
            getLocalSecrets().put(LONG_USERNAME, LONG_PASSWORD.toCharArray());
        }

        @Override
        public int verify(String identifier, char[] inputSecret) {
            // NOTE: Allocating Strings are not really secure treatment of passwords
            String almostSecret = new String(inputSecret);

            try {
                return super.verify(identifier, inputSecret);
            } finally {
                // Clear secret from memory as soon as possible (This is better
                // treatment, but useless due to our almostSecret
                // copy)
                Arrays.fill(inputSecret, '\000');
            }
        }
    }

    public static final String AUTHENTICATED_MSG = "You are authenticated";

    public static final String LONG_PASSWORD = "thisLongPasswordIsExtremelySecure";

    public static final String LONG_USERNAME = "aVeryLongUsernameIsIndeedRequiredForThisTest";

    public static final String SHORT_PASSWORD = "pw15";

    public static final String SHORT_USERNAME = "user13";

    public static final String WRONG_USERNAME = "wrongUser";

    private ChallengeAuthenticator authenticator;

    private Component component;

    private String uri;

    private MapVerifier verifier;

    @Test
    public void guardLong() {
        assertEquals(
                Verifier.RESULT_VALID,
                this.verifier.verify(LONG_USERNAME, LONG_PASSWORD.toCharArray()),
                "Didn't authenticate short user/pwd"
        );
    }

    @Test
    public void guardLongWrong() {
        assertEquals(
                Verifier.RESULT_INVALID,
                this.verifier.verify(LONG_USERNAME, SHORT_PASSWORD.toCharArray()),
                "Authenticated long username with wrong password"
        );
    }

    // Test our guard.checkSecret() stand-alone
    @Test
    public void guardShort() {
        assertEquals(
                Verifier.RESULT_VALID,
                this.verifier.verify(SHORT_USERNAME, SHORT_PASSWORD.toCharArray()),
                "Didn't authenticate short user/pwd"
        );
    }

    @Test
    public void guardShortWrong() {
        assertEquals(
                Verifier.RESULT_INVALID,
                this.verifier.verify(SHORT_USERNAME, LONG_PASSWORD.toCharArray()),
                "Authenticated short username with wrong password"
        );
    }

    @Test
    public void guardWrongUser() {
        assertEquals(
                Verifier.RESULT_INVALID,
                this.verifier.verify(WRONG_USERNAME, SHORT_PASSWORD.toCharArray()),
                "Authenticated wrong username"
        );
    }

    public void HttpBasicLong() throws Exception {
        Request request = new Request(Method.GET, this.uri);
        Client client = new Client(Protocol.HTTP);

        ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, LONG_USERNAME, LONG_PASSWORD);
        request.setChallengeResponse(authentication);

        final Response response = client.handle(request);
        assertEquals(
                Status.SUCCESS_OK, response.getStatus(),
                "Long username did not return 200 OK"
        );
        assertEquals(AUTHENTICATED_MSG, response.getEntity().getText());

        client.stop();
    }

    public void HttpBasicLongWrong() throws Exception {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);

        final ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, LONG_USERNAME, SHORT_PASSWORD);
        request.setChallengeResponse(authentication);

        final Response response = client.handle(request);

        assertEquals(
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus(),
                "Long username w/wrong pw did not throw 403"
        );

        client.stop();
    }

    // Test various HTTP Basic auth connections
    public void HttpBasicNone() throws Exception {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);
        final Response response = client.handle(request);
        assertEquals(
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus(),
                "No user did not throw 401"
        );
        client.stop();
    }

    public void HttpBasicShort() throws Exception {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);

        final ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, SHORT_USERNAME, SHORT_PASSWORD);
        request.setChallengeResponse(authentication);

        final Response response = client.handle(request);
        assertEquals(
                Status.SUCCESS_OK, response.getStatus(),
                "Short username did not return 200 OK"
        );
        assertEquals(AUTHENTICATED_MSG, response.getEntity().getText());

        client.stop();
    }

    public void HttpBasicShortWrong() throws Exception {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);

        final ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, SHORT_USERNAME, LONG_PASSWORD);
        request.setChallengeResponse(authentication);

        final Response response = client.handle(request);

        assertEquals(
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus(),
                "Short username did not throw 401"
        );

        client.stop();
    }

    public void HttpBasicWrongUser() throws Exception {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);

        final ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, WRONG_USERNAME, SHORT_PASSWORD);
        request.setChallengeResponse(authentication);

        final Response response = client.handle(request);

        assertEquals(
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus(),
                "Wrong username did not throw 401"
        );

        client.stop();
    }

    @BeforeEach
    public void makeServer() throws Exception {
        this.component = new Component();
        final Server server = this.component.getServers().add(Protocol.HTTP, 0);

        final Application application = new Application() {
            @Override
            public Restlet createInboundRoot() {
                HttpBasicTestCase.this.verifier = new TestVerifier();
                HttpBasicTestCase.this.authenticator = new ChallengeAuthenticator(
                        getContext(), ChallengeScheme.HTTP_BASIC,
                        HttpBasicTestCase.class.getSimpleName());
                HttpBasicTestCase.this.authenticator
                        .setVerifier(HttpBasicTestCase.this.verifier);
                HttpBasicTestCase.this.authenticator
                        .setNext(new AuthenticatedRestlet());
                return HttpBasicTestCase.this.authenticator;
            }
        };

        this.component.getDefaultHost().attach(application);
        this.component.start();
        this.uri = "http://localhost:" + server.getActualPort() + "/";
    }

    @AfterEach
    public void stopServer() throws Exception {
        if (this.component.isStarted()) {
            this.component.stop();
        }
        this.component = null;
    }

    @Test
    public void testHttpBasic() throws Exception {
        HttpBasicWrongUser();
        HttpBasicShort();
        HttpBasicShortWrong();
        HttpBasicNone();
        HttpBasicLong();
        HttpBasicLongWrong();
    }

}
