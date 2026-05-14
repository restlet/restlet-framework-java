/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

/**
 * Test that a simple get using SSL works for all the connectors.
 *
 * @author Kevin Conaway
 * @author Bruno Harbulot
 */
public class SslGetTestCase extends SslBaseConnectorsTestCase {

    @Override
    protected void doTest(final int serverPort) throws Exception {
        final Client client = new Client(Protocol.HTTPS);
        client.setContext(new Context());
        configureSslClientParameters(client);

        final Request request = new Request(Method.GET, format("https://localhost:%d", serverPort));
        final Response response = client.handle(request);

        assertEquals(
                Status.SUCCESS_OK, response.getStatus(), response.getStatus().getDescription());
        assertEquals("Hello world", response.getEntity().getText());

        client.stop();
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
