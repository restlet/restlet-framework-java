/**
 * Copyright 2005-2024 Qlik
 * <p>
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * <p>
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty.connectors;

import org.restlet.*;
import org.restlet.data.*;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.util.WrapperRepresentation;

import java.io.IOException;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This tests the ability of the connectors to handle chunked encoding.
 *
 * The test uses each connector to PUT an entity that will be sent chunked and
 * also to receive a chunked response.
 */
public class ChunkedEncodingTestCase extends BaseConnectorsTestCase {

    private static final int LOOP_NUMBER = 50;

    @Override
    protected void doTest(final int serverPort) throws Exception {
        final String uri = format("http://localhost:%d", serverPort);

        for (int testIndex = 0; testIndex < LOOP_NUMBER; testIndex++) {
            sendGet(testIndex, uri);
            sendPut(testIndex, uri);
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

    private void sendGet(int testIndex, String uri) throws Exception {
        final Request request = new Request(Method.GET, uri);
        final Client client = new Client(Protocol.HTTP);
        final Response response = client.handle(request);

        try {
            assertEquals(Status.SUCCESS_OK, response.getStatus(), format("test #%d: response's status is wrong", testIndex));
            assertXML(testIndex, response.getEntity());
        } finally {
            response.release();
            client.stop();
        }
    }

    private void sendPut(int testIndex, String uri) throws Exception {
        final Request request = new Request(Method.PUT, uri, createTestXml());
        final Client client = new Client(Protocol.HTTP);
        final Response response = client.handle(request);

        try {
            assertChunkedHeader(response);
            assertEquals(Status.SUCCESS_OK, response.getStatus(), format("test #%d: response's status is wrong", testIndex));
            assertXML(testIndex, response.getEntity());
        } finally {
            response.release();
            client.stop();
        }

    }

    public static class PutTestResource extends ServerResource {

        public PutTestResource() {
            getVariants().add(new Variant(MediaType.TEXT_XML));
            setNegotiated(false);
        }

        @Override
        public Representation get() {
            return createTestXml();
        }

        @Override
        public Representation put(Representation entity) {
            assertChunkedHeader(getRequest());

            return new WrapperRepresentation(entity);
        }
    }

    private void assertXML(int testIndex, Representation entity) {
        try {
            String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root><child-0 name=\"name-0\"/><child-1 name=\"name-1\"/></root>";
            String text = entity.getText();
            assertEquals(expected, text, format("test #%d: xml representation is wrong", testIndex));
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    private static void assertChunkedHeader(Message message) {
        final Header transferEncoding = message.getHeaders()
                .getFirst(HeaderConstants.HEADER_TRANSFER_ENCODING, true);
        assertNotNull(transferEncoding);
        assertEquals("chunked", transferEncoding.getValue());
    }

    private static Representation createTestXml() {
        String xmlRepresentationAsString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root><child-0 name=\"name-0\"/><child-1 name=\"name-1\"/></root>";
        Representation rep = new StringRepresentation(xmlRepresentationAsString);
        rep.setSize(Representation.UNKNOWN_SIZE); // force chunked encoding

        return rep;
    }

}
