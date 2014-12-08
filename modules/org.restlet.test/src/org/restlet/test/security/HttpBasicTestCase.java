/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.security;

import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.security.ChallengeAuthenticator;
import org.restlet.security.MapVerifier;
import org.restlet.security.Verifier;
import org.restlet.test.RestletTestCase;

/**
 * Restlet unit tests for HTTP Basic authentication client/server. By default,
 * runs server on localhost on port {@value #DEFAULT_PORT}, which can be
 * overridden by setting system property {@value #RESTLET_TEST_PORT}
 * 
 * @author Stian Soiland
 * @author Jerome Louvel
 */
public class HttpBasicTestCase extends RestletTestCase {

    public class AuthenticatedRestlet extends Restlet {
        @Override
        public void handle(Request request, Response response) {
            response.setEntity(AUTHENTICATED_MSG, MediaType.TEXT_PLAIN);
        }
    }

    public class TestVerifier extends MapVerifier {
        public TestVerifier() {
            getLocalSecrets().put(SHORT_USERNAME, SHORT_PASSWORD.toCharArray());
            getLocalSecrets().put(LONG_USERNAME, LONG_PASSWORD.toCharArray());
        }

        @Override
        public int verify(String identifier, char[] inputSecret) {
            // NOTE: Allocating Strings are not really secure treatment of
            // passwords
            String almostSecret = new String(inputSecret);
            System.out.println("Checking " + identifier + " " + almostSecret);

            try {
                return super.verify(identifier, inputSecret);
            } finally {
                // Clear secret from memory as soon as possible (This is better
                // treatment, but of course useless due to our almostSecret
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

    public static void main(String[] args) {
        new HttpBasicTestCase().testHttpBasic();
    }

    private ChallengeAuthenticator authenticator;

    private Component component;

    private String uri;

    private MapVerifier verifier;

    public void guardLong() {
        assertEquals("Didn't authenticate short user/pwd",
                Verifier.RESULT_INVALID, this.verifier.verify(LONG_USERNAME,
                        LONG_PASSWORD.toCharArray()));
    }

    public void guardLongWrong() {
        assertEquals(
                "Authenticated long username with wrong password",
                Verifier.RESULT_INVALID,
                this.verifier.verify(LONG_USERNAME,
                        SHORT_PASSWORD.toCharArray()));
    }

    // Test our guard.checkSecret() stand-alone
    public void guardShort() {
        assertEquals(
                "Didn't authenticate short user/pwd",
                Verifier.RESULT_VALID,
                this.verifier.verify(SHORT_USERNAME,
                        SHORT_PASSWORD.toCharArray()));
    }

    public void guardShortWrong() {
        assertEquals(
                "Authenticated short username with wrong password",
                Verifier.RESULT_INVALID,
                this.verifier.verify(SHORT_USERNAME,
                        LONG_PASSWORD.toCharArray()));
    }

    public void guardWrongUser() {
        assertEquals(
                "Authenticated wrong username",
                Verifier.RESULT_INVALID,
                this.verifier.verify(WRONG_USERNAME,
                        SHORT_PASSWORD.toCharArray()));
    }

    public void HttpBasicLong() throws Exception {
        Request request = new Request(Method.GET, this.uri);
        Client client = new Client(Protocol.HTTP);

        ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, LONG_USERNAME, LONG_PASSWORD);
        request.setChallengeResponse(authentication);

        final Response response = client.handle(request);
        assertEquals("Long username did not return 200 OK", Status.SUCCESS_OK,
                response.getStatus());
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

        assertEquals("Long username w/wrong pw did not throw 403",
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());

        client.stop();
    }

    // Test various HTTP Basic auth connections
    public void HttpBasicNone() throws Exception {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);
        final Response response = client.handle(request);
        assertEquals("No user did not throw 401",
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
        client.stop();
    }

    public void HttpBasicShort() throws Exception {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);

        final ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, SHORT_USERNAME, SHORT_PASSWORD);
        request.setChallengeResponse(authentication);

        final Response response = client.handle(request);
        assertEquals("Short username did not return 200 OK", Status.SUCCESS_OK,
                response.getStatus());
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

        assertEquals("Short username did not throw 401",
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());

        client.stop();
    }

    public void HttpBasicWrongUser() throws Exception {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);

        final ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, WRONG_USERNAME, SHORT_PASSWORD);
        request.setChallengeResponse(authentication);

        final Response response = client.handle(request);

        assertEquals("Wrong username did not throw 401",
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());

        client.stop();
    }

    @Before
    public void makeServer() throws Exception {
        int port = TEST_PORT;
        this.component = new Component();
        this.component.getServers().add(Protocol.HTTP, port);
        this.uri = "http://localhost:" + port + "/";

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
    }

    @After
    public void stopServer() throws Exception {
        if ((this.component != null) && this.component.isStarted()) {
            this.component.stop();
        }
        this.component = null;
    }

    public void testHttpBasic() {
        try {
            makeServer();
            HttpBasicWrongUser();
            HttpBasicShort();
            HttpBasicShortWrong();
            HttpBasicNone();
            HttpBasicLong();
            HttpBasicLongWrong();
            stopServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
