/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.test.engine;

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

    private static int LOOP_NUMBER = 20;

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
     *            the size of the representation
     * @return A DomRepresentation.
     */
    private static Representation createChunkedRepresentation(int size) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            builder.append("a");
        }
        final Representation rep = new StringRepresentation(builder.toString(),
                MediaType.TEXT_PLAIN);
        rep.setSize(Representation.UNKNOWN_SIZE);
        return rep;
    }

    @Override
    protected void call(String uri) throws Exception {
        for (int i = 0; i < LOOP_NUMBER; i++) {
            sendPut(uri, 10);
        }

        for (int i = 0; i < LOOP_NUMBER; i++) {
            sendPut(uri, 50000);
        }

        sendPut(uri, 10);
    }

    @Override
    protected Application createApplication(Component component) {
        final Application application = new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", PutTestResource.class);
                return router;
            }
        };

        return application;
    }

    private void sendPut(String uri, int size) throws Exception {
        Request request = new Request(Method.PUT, uri,
                createChunkedRepresentation(size));
        Client c = new Client(Protocol.HTTP);
        Response r = c.handle(request);

        try {
            if (!r.getStatus().isSuccess()) {
                System.out.println(r.getStatus());
            }

            assertNotNull(r.getEntity());
            assertEquals(createChunkedRepresentation(size).getText(), r
                    .getEntity().getText());
        } finally {
            r.release();
            c.stop();
        }
    }

}
