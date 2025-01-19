/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

/**
 * This tests the ability of the connectors to handle chunked encoding.
 *
 * The test uses each connector to PUT an entity that will be sent chunked and
 * also to receive a chunked response.
 */
public class ChunkedEncodingPutTestCase extends BaseConnectorsTestCase {
    private static final int LOOP_NUMBER = 200;

    @Override
    protected void doTestUri(String uri) throws Exception {
        for (int testIndex = 0; testIndex < LOOP_NUMBER; testIndex++) {
            sendPut(testIndex, uri, 10);
        }

        for (int i = 0; i < LOOP_NUMBER; i++) {
            sendPut(i, uri, 50000);
        }

        sendPut(0, uri, 100000);
    }

    @Override
    protected Application createApplication(Component component) {
        return new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", PutTestResource.class);
                return router;
            }
        };
    }

    /**
     * Test resource that answers to PUT requests by sending back the received
     * entity.
     */
    public static class PutTestResource extends ServerResource {
        public PutTestResource() {
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
            setNegotiated(false);
        }

        @Override
        public Representation put(Representation entity) {
            return entity;
        }
    }

    /**
     * Returns a StringRepresentation which size depends on the given argument.
     *
     * @param size
     *         the size of the representation
     * @return A DomRepresentation.
     */
    private static Representation createChunkedRepresentation(int size) {
        Representation rep = new StringRepresentation("a".repeat(size), MediaType.TEXT_PLAIN);
        rep.setSize(Representation.UNKNOWN_SIZE); // force chunked encoding
        return rep;
    }

    private void sendPut(int testIndex, final String uri, final int size) throws Exception {
        final Request request = new Request(Method.PUT, uri, createChunkedRepresentation(size));
        final Client client = new Client(Protocol.HTTP);
        final Response response = client.handle(request);

        try {
            if (response.getStatus().isError()) {
                System.out.println(response.getStatus());
            }

            assertNotNull(response.getEntity(), String.format("test #%d - size %d: response's entity is null", testIndex, size));
            String responseEntity = response.getEntity().getText();
            assertEquals(size, responseEntity.length(), String.format("test #%d - size %d: length of response's entity is wrong", testIndex, size));
            String expectedResponseEntity = createChunkedRepresentation(size).getText();
            assertEquals(expectedResponseEntity, responseEntity, String.format("test #%d - size %d: response's entity is wrong", testIndex, size));
        } finally {
            response.release();
            client.stop();
        }
    }

}
