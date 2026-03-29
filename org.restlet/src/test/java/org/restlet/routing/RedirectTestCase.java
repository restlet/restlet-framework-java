/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.routing;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.representation.StringRepresentation;

/**
 * Unit tests for the RedirectRestlet.
 *
 * @author Jerome Louvel
 */
class RedirectTestCase {

    private static final int TEST_PORT = 1337;

    private void testCall(Context context, Method method, String uri) throws Exception {
        final Response response = context.getClientDispatcher().handle(new Request(method, uri));
        assertNotNull(response.getEntity());
        response.getEntity().write(System.out);
    }

    @BeforeEach
    public void setUp() {
        Engine.clearThreadLocalVariables();
        Engine.register();
    }

    @AfterEach
    public void tearDown() {
        Engine.clearThreadLocalVariables();
        Engine.register();
    }

    /** Tests the cookies parsing. */
    @Test
    void testRedirect() throws Exception {
        // Create components
        final Component clientComponent = new Component();
        final Component proxyComponent = new Component();
        final Component originComponent = new Component();

        // Create the client connectors
        clientComponent.getClients().add(Protocol.HTTP);
        proxyComponent.getClients().add(Protocol.HTTP);

        // Create the proxy Restlet
        final String target = "http://localhost:" + (TEST_PORT + 1) + "{rr}";
        final Redirector proxy =
                new Redirector(
                        proxyComponent.getContext().createChildContext(),
                        target,
                        Redirector.MODE_SERVER_OUTBOUND);

        // Create a new Restlet that will display some path information.
        final Restlet trace =
                new Restlet(originComponent.getContext().createChildContext()) {
                    @Override
                    public void handle(Request request, Response response) {
                        // Print the requested URI path
                        final String message =
                                "Resource URI:  "
                                        + request.getResourceRef()
                                        + '\n'
                                        + "Base URI:      "
                                        + request.getResourceRef().getBaseRef()
                                        + '\n'
                                        + "Remaining part: "
                                        + request.getResourceRef().getRemainingPart()
                                        + '\n'
                                        + "Method name:   "
                                        + request.getMethod()
                                        + '\n';
                        response.setEntity(new StringRepresentation(message, MediaType.TEXT_PLAIN));
                    }
                };

        // Set the component roots
        proxyComponent.getDefaultHost().attach("", proxy);
        originComponent.getDefaultHost().attach("", trace);

        // Create the server connectors
        proxyComponent.getServers().add(Protocol.HTTP, TEST_PORT);
        originComponent.getServers().add(Protocol.HTTP, TEST_PORT + 1);

        // Now, let's start the components!
        originComponent.start();
        proxyComponent.start();
        clientComponent.start();

        // Tests
        final Context context = clientComponent.getContext();
        String uri = format("http://localhost:%d/?foo=bar", TEST_PORT);
        testCall(context, Method.GET, uri);
        testCall(context, Method.DELETE, uri);

        uri = format("http://localhost:%d/abcd/efgh/ijkl?foo=bar&foo=beer", TEST_PORT);
        testCall(context, Method.GET, uri);
        testCall(context, Method.DELETE, uri);

        uri =
                "http://localhost:"
                        + TEST_PORT
                        + "/v1/client/kwse/CnJlNUQV9%252BNNqbUf7Lhs2BYEK2Y%253D"
                        + "/user/johnm/uVGYTDK4kK4zsu96VHGeTCzfwso%253D/";
        testCall(context, Method.GET, uri);

        // Stop the components
        clientComponent.stop();
        originComponent.stop();
        proxyComponent.stop();
    }
}
