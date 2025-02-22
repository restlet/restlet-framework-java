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
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.util.Series;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that a simple get using SSL works for all the connectors.
 * 
 * @author Kevin Conaway
 * @author Bruno Harbulot
 */
public class SslGetTestCase extends SslBaseConnectorsTestCase {

    @Override
    protected boolean shouldDebug() {
        return true;
    }

    @Override
    protected void doTest(final int serverPort) throws Exception {
        final Response response = sendGet(format("https://localhost:%d", serverPort));

        assertEquals(Status.SUCCESS_OK, response.getStatus(), response.getStatus().getDescription());
        assertEquals("Hello world", response.getEntity().getText());
    }

    private Response sendGet(final String uri) {
        final Client client = new Client(Protocol.HTTPS);
        client.setContext(new Context());
        configureSslClientParameters(client);

        final Request request = new Request(Method.GET, uri);
        return client.handle(request);
    }

    @Override
    protected Application createApplication() {
        return new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attachDefault(GetTestResource.class);
                return router;
            }
        };
    }

    public static class GetTestResource extends ServerResource {

        public GetTestResource() {
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        }

        @Override
        public Representation get(Variant variant) {
            return new StringRepresentation("Hello world", MediaType.TEXT_PLAIN);
        }
    }

}
