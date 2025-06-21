/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.connector;

import org.restlet.*;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * This tests the ability of the connectors to handle chunked encoding.
 *
 * The test uses each connector to PUT an entity that will be sent chunked and
 * also to receive a chunked response.
 */
public class ChunkedEncodingPutTestCase extends BaseConnectorsTestCase {
    private static final int LOOP_NUMBER = 20;

    @Override
    protected void doTest(final int serverPort) throws Exception {
        final String uri = format("http://localhost:%d", serverPort);

        for (int testIndex = 0; testIndex < LOOP_NUMBER; testIndex++) {
            sendPut(testIndex, uri, 10);
            sendPut(testIndex, uri, 50_000);
            sendPut(testIndex, uri, 100_000);
        }
    }

    @Override
    protected Application createApplication() {
        return new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attachDefault(PutTestResource.class);
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

    private void sendPut(int testIndex, final String uri, final int size) throws Exception {
        final Request request = new Request(Method.PUT, uri, createChunkedRepresentation(size));
        final Client client = new Client(Protocol.HTTP);
        final Response response = client.handle(request);

        try {
            if (response.getStatus().isError()) {
                System.out.println(response.getStatus());
            }

            assertNotNull(response.getEntity(), format("test #%d - size %d: response's entity is null", testIndex, size));
            final String responseEntity = response.getEntity().getText();
            assertNotNull(responseEntity, format("test #%d - size %d: response's entity content is null", testIndex, size));
            assertEquals(size, responseEntity.length(), format("test #%d - size %d: length of response's entity is wrong", testIndex, size));
            final String expectedResponseEntity = createChunkedRepresentation(size).getText();
            assertEquals(expectedResponseEntity, responseEntity, format("test #%d - size %d: response's entity is wrong", testIndex, size));
        } finally {
            response.release();
            client.stop();
        }
    }

    /**
     * Returns a StringRepresentation which size depends on the given argument.
     *
     * @param size
     *         the size of the representation
     * @return A DomRepresentation.
     */
    private Representation createChunkedRepresentation(int size) {
        Representation rep = new StringRepresentation("a".repeat(size), MediaType.TEXT_PLAIN);
        rep.setSize(Representation.UNKNOWN_SIZE); // force chunked encoding
        return rep;
    }

}
