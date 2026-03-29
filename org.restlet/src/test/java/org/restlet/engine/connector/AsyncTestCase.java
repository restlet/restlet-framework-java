/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.Uniform;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

/**
 * Unit tests for asynchronous queued Request handling.
 *
 * @author Florian Buecklers
 */
class AsyncTestCase {

    private Context context;

    private Component clientComponent;

    private Component originComponent;

    private int testPort;

    private boolean requestEntityExpected(Method method) {
        return method == Method.POST || method == Method.PUT;
    }

    private boolean responseEntityExpected(Method method) {
        return method == Method.GET || method == Method.PUT;
    }

    private void testCall(Context context, int count, Method method) throws Exception {
        final CountDownLatch latch = new CountDownLatch(count);

        final Uniform responseHandler =
                (request, response) -> {
                    String item = request.getResourceRef().getQueryAsForm().getFirstValue("item");

                    try {
                        assertEquals(item, Integer.toString(response.getAge()));
                        if (responseEntityExpected(request.getMethod())) {
                            assertEquals(Status.SUCCESS_OK, response.getStatus());
                            assertTrue(response.isEntityAvailable());
                            assertNotNull(response.getEntityAsText());
                        } else {
                            assertEquals(Status.SUCCESS_NO_CONTENT, response.getStatus());
                            assertFalse(response.isEntityAvailable());
                        }
                    } finally {
                        latch.countDown();
                    }
                };

        Restlet client = context.getClientDispatcher();
        for (int i = 0; i < count; ++i) {
            Representation rep = null;
            if (requestEntityExpected(method)) {
                rep = new StringRepresentation("Item: " + i);
            }

            Reference ref = new Reference("http://localhost:" + testPort + "/?item=" + i);
            Request request = new Request(method, ref, rep);
            request.setOnResponse(responseHandler);

            client.handle(request);
        }

        latch.await();
    }

    @BeforeEach
    protected void setUpEach() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(true);
        // Create components
        clientComponent = new Component();
        originComponent = new Component();

        // Create a new Restlet that will display some path information.
        final Restlet trace =
                new Restlet(originComponent.getContext().createChildContext()) {
                    @Override
                    public void handle(Request request, Response response) {
                        // let's set the item number as age ;-)
                        response.setAge(
                                Integer.parseInt(
                                        request.getResourceRef()
                                                .getQueryAsForm()
                                                .getFirstValue("item")));

                        if (responseEntityExpected(request.getMethod())) {
                            // Print the requested URI path
                            String message =
                                    "Resource URI:  "
                                            + request.getResourceRef()
                                            + '\n'
                                            + "Base URI:      "
                                            + request.getResourceRef().getBaseRef()
                                            + '\n'
                                            + "Remaining part: "
                                            + request.getResourceRef().getRemainingPart()
                                            + '\n'
                                            + "Method name: "
                                            + request.getMethod()
                                            + '\n';

                            if (requestEntityExpected(request.getMethod())) {
                                message += request.getEntityAsText();
                                request.getEntity().release();
                            }

                            response.setEntity(
                                    new StringRepresentation(message, MediaType.TEXT_PLAIN));

                            response.setStatus(Status.SUCCESS_OK);
                        } else {
                            // consume entity
                            if (requestEntityExpected(request.getMethod()))
                                request.getEntityAsText();

                            response.setStatus(Status.SUCCESS_NO_CONTENT);
                        }
                    }
                };

        originComponent.getDefaultHost().attach("", trace);

        // Create the server connectors
        Server server = originComponent.getServers().add(Protocol.HTTP, 0);
        server.getContext().getParameters().add("maxQueued", "-1");

        // Create the client connectors
        Client client = clientComponent.getClients().add(Protocol.HTTP);
        context = client.getContext();
        context.getParameters().add("maxQueued", "-1");
        context.getParameters().add("maxConnectionsPerHost", "6");

        // Now, let's start the components!
        originComponent.start();
        clientComponent.start();
        testPort = server.getActualPort();
    }

    @Test
    void testGet() throws Exception {
        testCall(context, 10, Method.GET);
    }

    @Test
    void testPost() throws Exception {
        testCall(context, 10, Method.POST);
    }

    @Test
    void testPut() throws Exception {
        testCall(context, 10, Method.PUT);
    }

    @Test
    void testDelete() throws Exception {
        testCall(context, 10, Method.DELETE);
    }

    @AfterEach
    protected void tearDownEach() throws Exception {
        // Stop the components
        clientComponent.stop();
        originComponent.stop();
        Engine.clearThreadLocalVariables();
    }
}
