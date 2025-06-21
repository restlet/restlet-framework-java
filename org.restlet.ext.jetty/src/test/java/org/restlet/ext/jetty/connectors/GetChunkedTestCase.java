/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
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

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test that a simple get works for all the connectors.
 *
 * @author Kevin Conaway
 */
public class GetChunkedTestCase extends BaseConnectorsTestCase {

    private static final String text = "<?xml version='1.0'?><mail>" + "a".repeat(1000) + "</mail>";

    @Override
    protected void doTest(final int serverPort) throws Exception {
        final String uri = format("http://localhost:%d", serverPort);

        final Client client = new Client(Protocol.HTTP);
        final Request request = new Request(Method.GET, uri);
        final Response response = client.handle(request);

        try {
            assertEquals(Status.SUCCESS_OK, response.getStatus(), response.getStatus().getDescription());
            assertChunkedHeader(response);
            assertEquals(text, response.getEntity().getText());
        } finally {
            response.release();
            client.stop();
        }
    }

    @Override
    protected Application createApplication() {
        return new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attachDefault(GetChunkedTestResource.class);
                return router;
            }
        };
    }

    public static class GetChunkedTestResource extends ServerResource {

        public GetChunkedTestResource() {
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        }

        @Override
        public Representation get(Variant variant) {
            final Representation rep = new StringRepresentation( text, MediaType.APPLICATION_XML);
            rep.setSize(Representation.UNKNOWN_SIZE); // force chunked encoding
            return rep;
        }
    }

    private static void assertChunkedHeader(Message message) {
        final Header transferEncoding = message.getHeaders()
                .getFirst(HeaderConstants.HEADER_TRANSFER_ENCODING, true);
        assertNotNull(transferEncoding);
        assertEquals("chunked", transferEncoding.getValue());
    }

}
