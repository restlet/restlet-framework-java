/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.security;

import java.io.IOException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.security.Guard;
import org.restlet.test.RestletTestCase;

/**
 * Restlet unit tests for HTTP Basic authentication client/server. By default,
 * runs server on localhost on port {@value #DEFAULT_PORT}, which can be
 * overriden by setting system property {@value #RESTLET_TEST_PORT}
 * 
 * @author Stian Soiland
 */
public class HttpBasicTestCase extends RestletTestCase {

    public class AuthenticatedRestlet extends Restlet {
        @Override
        public void handle(Request request, Response response) {
            response.setEntity(AUTHENTICATED_MSG, MediaType.TEXT_PLAIN);
        }
    }

    public class TestGuard extends Guard {
        public TestGuard(Context context) {
            super(context, ChallengeScheme.HTTP_BASIC, HttpBasicTestCase.class
                    .getSimpleName());
            getSecrets().put(SHORT_USERNAME, SHORT_PASSWORD.toCharArray());
            getSecrets().put(LONG_USERNAME, LONG_PASSWORD.toCharArray());
        }

        @Override
        public boolean checkSecret(Request request, String identifier,
                char[] secret) {
            // NOTE: Allocating Strings are not really secure treatment of
            // passwords
            final String almostSecret = new String(secret);
            System.out.println("Checking " + identifier + " " + almostSecret);
            try {
                return super.checkSecret(request, identifier, secret);
            } finally {
                // Clear secret from memory as soon as possible (This is better
                // treatment, but of course useless due to our almostSecret
                // copy)
                Arrays.fill(secret, '\000');
            }
        }
    }

    public static final String WRONG_USERNAME = "wrongUser";

    public static final String SHORT_USERNAME = "user13";

    public static final String SHORT_PASSWORD = "pw15";

    public static final String LONG_USERNAME = "aVeryLongUsernameIsIndeedRequiredForThisTest";

    public static final String LONG_PASSWORD = "thisLongPasswordIsExtremelySecure";

    public static final String AUTHENTICATED_MSG = "You are authenticated";

    public static void main(String[] args) {
        new HttpBasicTestCase().testHTTPBasic();
    }

    private Component component;

    private String uri;

    private TestGuard guard;

    public void guardLong() {
        assertTrue("Didn't authenticate short user/pwd", this.guard
                .checkSecret(null, LONG_USERNAME, LONG_PASSWORD.toCharArray()));
    }

    public void guardLongWrong() {
        assertFalse("Authenticated long username with wrong password",
                this.guard.checkSecret(null, LONG_USERNAME, SHORT_PASSWORD
                        .toCharArray()));
    }

    // Test our guard.checkSecret() stand-alone
    public void guardShort() {
        assertTrue("Didn't authenticate short user/pwd",
                this.guard.checkSecret(null, SHORT_USERNAME, SHORT_PASSWORD
                        .toCharArray()));
    }

    public void guardShortWrong() {
        assertFalse("Authenticated short username with wrong password",
                this.guard.checkSecret(null, SHORT_USERNAME, LONG_PASSWORD
                        .toCharArray()));
    }

    public void guardWrongUser() {
        assertFalse("Authenticated wrong username", this.guard.checkSecret(
                null, WRONG_USERNAME, SHORT_PASSWORD.toCharArray()));
    }

    public void HTTPBasicLong() throws IOException {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);

        final ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, LONG_USERNAME, LONG_PASSWORD);
        request.setChallengeResponse(authentication);

        final Response response = client.handle(request);
        assertEquals("Long username did not return 200 OK", Status.SUCCESS_OK,
                response.getStatus());
        assertEquals(AUTHENTICATED_MSG, response.getEntity().getText());
    }

    public void HTTPBasicLongWrong() {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);

        final ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, LONG_USERNAME, SHORT_PASSWORD);
        request.setChallengeResponse(authentication);

        final Response response = client.handle(request);

        assertEquals("Long username w/wrong pw did not throw 403",
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
    }

    // Test various HTTP Basic auth connections
    public void HTTPBasicNone() throws IOException {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);
        final Response response = client.handle(request);
        assertEquals("No user did not throw 401",
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getEntity().getText().contains(
                "requires user authentication"));
    }

    public void HTTPBasicShort() throws IOException {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);

        final ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, SHORT_USERNAME, SHORT_PASSWORD);
        request.setChallengeResponse(authentication);

        final Response response = client.handle(request);
        assertEquals("Short username did not return 200 OK", Status.SUCCESS_OK,
                response.getStatus());
        assertEquals(AUTHENTICATED_MSG, response.getEntity().getText());
    }

    public void HTTPBasicShortWrong() {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);

        final ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, SHORT_USERNAME, LONG_PASSWORD);
        request.setChallengeResponse(authentication);

        final Response response = client.handle(request);

        assertEquals("Short username did not throw 401",
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
    }

    public void HTTPBasicWrongUser() {
        final Request request = new Request(Method.GET, this.uri);
        final Client client = new Client(Protocol.HTTP);

        final ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, WRONG_USERNAME, SHORT_PASSWORD);
        request.setChallengeResponse(authentication);

        final Response response = client.handle(request);

        assertEquals("Wrong username did not throw 401",
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
    }

    @Before
    public void makeServer() throws Exception {
        int port = TEST_PORT;
        this.component = new Component();
        this.component.getServers().add(Protocol.HTTP, port);
        this.uri = "http://localhost:" + port + "/";

        final Application application = new Application() {
            @Override
            public Restlet createRoot() {
                HttpBasicTestCase.this.guard = new TestGuard(getContext());
                HttpBasicTestCase.this.guard
                        .setNext(new AuthenticatedRestlet());
                return HttpBasicTestCase.this.guard;
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
    }

    public void testHTTPBasic() {
        try {
            makeServer();
            HTTPBasicWrongUser();
            HTTPBasicShort();
            HTTPBasicShortWrong();
            HTTPBasicNone();
            HTTPBasicLong();
            HTTPBasicLongWrong();
            stopServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
