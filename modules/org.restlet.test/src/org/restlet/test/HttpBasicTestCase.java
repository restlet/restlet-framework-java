/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package org.restlet.test;

import java.io.IOException;
import java.util.Arrays;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Guard;
import org.restlet.Restlet;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Restlet unit tests for HTTP Basic authentication client/server. By default,
 * runs server on localhost on port {@value #DEFAULT_PORT}, which can be
 * overriden by setting system property {@value #RESTLET_TEST_PORT}
 * 
 * @author Stian Soiland
 */
public class HttpBasicTestCase extends TestCase {

    private static final String RESTLET_TEST_PORT = "restlet.test.port";

    public static final String WRONG_USERNAME = "wrongUser";

    public static final String SHORT_USERNAME = "user13";

    public static final String SHORT_PASSWORD = "pw15";

    public static final String LONG_USERNAME = "aVeryLongUsernameIsIndeedRequiredForThisTest";

    public static final String LONG_PASSWORD = "thisLongPasswordIsExtremelySecure";

    public static final String AUTHENTICATED_MSG = "You are authenticated";

    public static final int DEFAULT_PORT = 1337;

    private Component component;

    private String uri;

    private TestGuard guard;

    public static void main(String[] args) {
        new HttpBasicTestCase().testHTTPBasic();
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

    public class TestGuard extends Guard {
        public TestGuard(Context context) {
            super(context, ChallengeScheme.HTTP_BASIC, HttpBasicTestCase.class
                    .getSimpleName());
            this.getSecrets().put(SHORT_USERNAME, SHORT_PASSWORD.toCharArray());
            this.getSecrets().put(LONG_USERNAME, LONG_PASSWORD.toCharArray());
        }

        @Override
        public boolean checkSecret(Request request, String identifier,
                char[] secret) {
            // NOTE: Allocating Strings are not really secure treatment of
            // passwords
            String almostSecret = new String(secret);
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

    public class AuthenticatedRestlet extends Restlet {
        @Override
        public void handle(Request request, Response response) {
            response.setEntity(AUTHENTICATED_MSG, MediaType.TEXT_PLAIN);
        }
    }

    @Before
    public void makeServer() throws Exception {
        int port;

        if (System.getProperties().containsKey(RESTLET_TEST_PORT)) {
            port = Integer.parseInt(System.getProperty(RESTLET_TEST_PORT));
        }

        port = DEFAULT_PORT;
        component = new Component();
        component.getServers().add(Protocol.HTTP, port);
        uri = "http://localhost:" + port + "/";

        Application application = new Application(component.getContext()) {
            @Override
            public Restlet createRoot() {
                guard = new TestGuard(getContext());
                guard.setNext(new AuthenticatedRestlet());
                return guard;
            }
        };

        component.getDefaultHost().attach(application);
        component.start();
    }

    @After
    public void stopServer() throws Exception {
        if (component != null && component.isStarted()) {
            component.stop();
        }
    }

    // Test our guard.checkSecret() stand-alone
    public void guardShort() {
        assertTrue("Didn't authenticate short user/pwd", guard.checkSecret(
                null, SHORT_USERNAME, SHORT_PASSWORD.toCharArray()));
    }

    public void guardLong() {
        assertTrue("Didn't authenticate short user/pwd", guard.checkSecret(
                null, LONG_USERNAME, LONG_PASSWORD.toCharArray()));
    }

    public void guardShortWrong() {
        assertFalse("Authenticated short username with wrong password", guard
                .checkSecret(null, SHORT_USERNAME, LONG_PASSWORD.toCharArray()));
    }

    public void guardLongWrong() {
        assertFalse("Authenticated long username with wrong password", guard
                .checkSecret(null, LONG_USERNAME, SHORT_PASSWORD.toCharArray()));
    }

    public void guardWrongUser() {
        assertFalse("Authenticated wrong username", guard.checkSecret(null,
                WRONG_USERNAME, SHORT_PASSWORD.toCharArray()));
    }

    // Test various HTTP Basic auth connections
    public void HTTPBasicNone() throws IOException {
        Request request = new Request(Method.GET, uri);
        Client client = new Client(Protocol.HTTP);
        Response response = client.handle(request);
        assertEquals("No user did not throw 401",
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
        assertTrue(response.getEntity().getText().contains(
                "requires user authentication"));
    }

    public void HTTPBasicShort() throws IOException {
        Request request = new Request(Method.GET, uri);
        Client client = new Client(Protocol.HTTP);

        ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, SHORT_USERNAME, SHORT_PASSWORD);
        request.setChallengeResponse(authentication);

        Response response = client.handle(request);
        assertEquals("Short username did not return 200 OK", Status.SUCCESS_OK,
                response.getStatus());
        assertEquals(AUTHENTICATED_MSG, response.getEntity().getText());
    }

    public void HTTPBasicShortWrong() {
        Request request = new Request(Method.GET, uri);
        Client client = new Client(Protocol.HTTP);

        ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, SHORT_USERNAME, LONG_PASSWORD);
        request.setChallengeResponse(authentication);

        Response response = client.handle(request);

        assertEquals("Short username did not throw 401",
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
    }

    public void HTTPBasicLong() throws IOException {
        Request request = new Request(Method.GET, uri);
        Client client = new Client(Protocol.HTTP);

        ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, LONG_USERNAME, LONG_PASSWORD);
        request.setChallengeResponse(authentication);

        Response response = client.handle(request);
        assertEquals("Long username did not return 200 OK", Status.SUCCESS_OK,
                response.getStatus());
        assertEquals(AUTHENTICATED_MSG, response.getEntity().getText());
    }

    public void HTTPBasicLongWrong() {
        Request request = new Request(Method.GET, uri);
        Client client = new Client(Protocol.HTTP);

        ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, LONG_USERNAME, SHORT_PASSWORD);
        request.setChallengeResponse(authentication);

        Response response = client.handle(request);

        assertEquals("Long username w/wrong pw did not throw 403",
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
    }

    public void HTTPBasicWrongUser() {
        Request request = new Request(Method.GET, uri);
        Client client = new Client(Protocol.HTTP);

        ChallengeResponse authentication = new ChallengeResponse(
                ChallengeScheme.HTTP_BASIC, WRONG_USERNAME, SHORT_PASSWORD);
        request.setChallengeResponse(authentication);

        Response response = client.handle(request);

        assertEquals("Wrong username did not throw 401",
                Status.CLIENT_ERROR_UNAUTHORIZED, response.getStatus());
    }
}
