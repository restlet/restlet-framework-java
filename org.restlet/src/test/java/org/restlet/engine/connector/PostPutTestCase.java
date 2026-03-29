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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;

/**
 * Unit tests for POST and PUT requests.
 *
 * @author Jerome Louvel
 */
public class PostPutTestCase extends BaseConnectorsTestCase {

    @Override
    protected void doTest(final int serverPort) throws Exception {
        final String uri = format("http://localhost:%d", serverPort);

        final Client client = new Client(Protocol.HTTP);
        try {
            testCall(client, Method.POST, uri);
            testCall(client, Method.PUT, uri);
        } finally {
            client.stop();
        }
    }

    @Override
    protected Application createApplication() {
        return new Application() {
            @Override
            public Restlet createInboundRoot() {
                return new Restlet(getContext()) {
                    @Override
                    public void handle(Request request, Response response) {
                        Representation entity = request.getEntity();
                        if (entity != null) {
                            final Form form = new Form(entity);
                            response.setEntity(form.getWebRepresentation());
                        }
                    }
                };
            }
        };
    }

    private void testCall(Client client, Method method, String uri) {
        final Form inputForm = new Form();
        inputForm.add("a", "a");
        inputForm.add("b", "b");

        final Request request = new Request(method, uri);
        request.setEntity(inputForm.getWebRepresentation());

        final Response response = client.handle(request);
        final Representation entity = response.getEntity();
        assertNotNull(entity);

        final Form outputForm = new Form(entity);
        assertEquals(2, outputForm.size());
        assertEquals("a", outputForm.getFirstValue("a"));
        assertEquals("b", outputForm.getFirstValue("b"));

        response.release();
    }
}
