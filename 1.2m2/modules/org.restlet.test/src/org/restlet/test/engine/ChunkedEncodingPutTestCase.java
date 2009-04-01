/**
 * Copyright 2005-2009 Noelios Technologies.
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
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.routing.Router;

/**
 * This tests the ability of the connectors to handle chunked encoding.
 * 
 * The test uses each connector to PUT an entity that will be sent chunked and
 * also to receive a chunked response.
 */
public class ChunkedEncodingPutTestCase extends BaseConnectorsTestCase {

    private static int LOOP_NUMBER = 50;

    /**
     * Test resource that answers to PUT requests by sending back the received
     * entity.
     */
    public static class PutTestResource extends Resource {
        public PutTestResource(Context ctx, Request request, Response response) {
            super(ctx, request, response);
            getVariants().add(new Variant(MediaType.TEXT_XML));
            setModifiable(true);
        }

        @Override
        public void storeRepresentation(Representation entity) {
            getResponse().setEntity(entity);
        }
    }

    /**
     * Returns a StringRepresentation which size depends on the given argument.
     * 
     * @param size
     *            the size of the representation
     * @return A DomRepresentation.
     */
    private static Representation createChunckedRepresentation(int size) {
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
            sendPut(uri, 1024);
        }
        for (int i = 0; i < LOOP_NUMBER; i++) {
            sendPut(uri, 10240);
        }
    }

    @Override
    protected Application createApplication(Component component) {
        final Application application = new Application() {
            @Override
            public Restlet createRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", PutTestResource.class);
                return router;
            }
        };

        return application;
    }

    private void sendPut(String uri, int size) throws Exception {
        final Request request = new Request(Method.PUT, uri,
                createChunckedRepresentation(size));
        final Response r = new Client(Protocol.HTTP).handle(request);

        try {
            if (!r.getStatus().isSuccess()) {
                System.out.println(r.getStatus());
            }

            assertNotNull(r.getEntity());
            assertEquals(createChunckedRepresentation(size).getText(), r
                    .getEntity().getText());
        } finally {
            r.release();
        }

    }

    @Override
    public void setUp() {
        super.setUp();
    }

    @Override
    public void testGrizzlyAndApache() throws Exception {
        super.testGrizzlyAndApache();
    }

    @Override
    public void testGrizzlyAndInternal() throws Exception {
        super.testGrizzlyAndInternal();
    }

    @Override
    public void testGrizzlyAndJdkNet() throws Exception {
        super.testGrizzlyAndJdkNet();
    }

    @Override
    public void testInternalAndApache() throws Exception {
        super.testInternalAndApache();
    }

    @Override
    public void testInternalAndInternal() throws Exception {
        super.testInternalAndInternal();
    }

    @Override
    public void testInternalAndJdkNet() throws Exception {
        super.testInternalAndJdkNet();
    }

    @Override
    public void testJettyAndApache() throws Exception {
        super.testJettyAndApache();
    }

    @Override
    public void testJettyAndInternal() throws Exception {
        super.testJettyAndInternal();
    }

    @Override
    public void testJettyAndJdkNet() throws Exception {
        super.testJettyAndJdkNet();
    }

    @Override
    public void testSimpleAndApache() throws Exception {
        super.testSimpleAndApache();
    }

    @Override
    public void testSimpleAndInternal() throws Exception {
        super.testSimpleAndInternal();
    }

    @Override
    public void testSimpleAndJdkNet() throws Exception {
    	// TODO to be fixed
    	//super.testSimpleAndJdkNet();
    }

}
