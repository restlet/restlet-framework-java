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

package org.restlet.test.engine.connector;

import java.util.concurrent.CountDownLatch;

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
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.test.RestletTestCase;

/**
 * Unit tests for asynchronous queued Request handling.
 * 
 * @author Florian Buecklers
 */
public class AsynchroneTestCase extends RestletTestCase {

    private Context context;

    private Component clientComponent;

    private Component originComponent;

    private boolean requestEntityExpected(Method method) {
        return method == Method.POST || method == Method.PUT;
    }

    private boolean responseEntityExpected(Method method) {
        return method == Method.GET || method == Method.PUT;
    }

    private void testCall(Context context, int count, Method method)
            throws Exception {
        final CountDownLatch latch = new CountDownLatch(count);

        final Uniform responseHandler = new Uniform() {
            @Override
            public void handle(Request request, Response response) {
                String item = request.getResourceRef().getQueryAsForm()
                        .getFirstValue("item");

                try {
                    assertEquals(item, Integer.toString(response.getAge()));
                    if (responseEntityExpected(request.getMethod())) {
                        assertEquals(Status.SUCCESS_OK, response.getStatus());
                        assertTrue(response.isEntityAvailable());
                        assertNotNull(response.getEntityAsText());
                    } else {
                        assertEquals(Status.SUCCESS_NO_CONTENT,
                                response.getStatus());
                        assertFalse(response.isEntityAvailable());
                    }
                } finally {
                    latch.countDown();
                }
            }
        };

        Restlet client = context.getClientDispatcher();
        for (int i = 0; i < count; ++i) {
            Representation rep = null;
            if (requestEntityExpected(method)) {
                rep = new StringRepresentation("Item: " + i);
            }

            Reference ref = new Reference("http://localhost:" + TEST_PORT
                    + "/?item=" + i);
            Request request = new Request(method, ref, rep);
            request.setOnResponse(responseHandler);

            client.handle(request);
        }

        latch.await();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Create components
        clientComponent = new Component();
        originComponent = new Component();

        // Create a new Restlet that will display some path information.
        final Restlet trace = new Restlet(originComponent.getContext()
                .createChildContext()) {
            @Override
            public void handle(Request request, Response response) {
                // lets set the item number as age ;-)
                response.setAge(Integer.valueOf(request.getResourceRef()
                        .getQueryAsForm().getFirstValue("item")));

                if (responseEntityExpected(request.getMethod())) {
                    // Print the requested URI path
                    String message = "Resource URI:  "
                            + request.getResourceRef() + '\n'
                            + "Base URI:      "
                            + request.getResourceRef().getBaseRef() + '\n'
                            + "Remaining part: "
                            + request.getResourceRef().getRemainingPart()
                            + '\n' + "Method name: " + request.getMethod()
                            + '\n';

                    if (requestEntityExpected(request.getMethod())) {
                        message += request.getEntityAsText();
                        request.getEntity().release();
                    }

                    response.setEntity(new StringRepresentation(message,
                            MediaType.TEXT_PLAIN));

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
        Server server = originComponent.getServers().add(Protocol.HTTP,
                TEST_PORT);
        server.getContext().getParameters().add("maxQueued", "-1");

        // Create the client connectors
        Client client = clientComponent.getClients().add(Protocol.HTTP);
        context = client.getContext();
        context.getParameters().add("maxQueued", "-1");
        context.getParameters().add("maxConnectionsPerHost", "6");

        // Now, let's start the components!
        originComponent.start();
        clientComponent.start();
    }

    public void testGet() throws Exception {
        testCall(context, 10, Method.GET);
    }

    public void testPost() throws Exception {
        testCall(context, 10, Method.POST);
    }

    public void testPut() throws Exception {
        testCall(context, 10, Method.PUT);
    }

    public void testDelete() throws Exception {
        testCall(context, 10, Method.DELETE);
    }

    @Override
    protected void tearDown() throws Exception {
        // Stop the components
        clientComponent.stop();
        originComponent.stop();

        super.tearDown();
    }
}
