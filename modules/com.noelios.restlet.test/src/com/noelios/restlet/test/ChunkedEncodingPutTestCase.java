/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.test;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * This tests the ability of the connectors to handle chunked encoding.
 * 
 * The test uses each connector to PUT an entity that will be sent chunked and
 * also to receive a chunked response.
 */
public class ChunkedEncodingPutTestCase extends BaseConnectorsTestCase {

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
        @SuppressWarnings("unchecked")
        public void storeRepresentation(Representation entity) {
            getResponse().setEntity(entity);
        }
    }

    /**
     * Returns a StringRepresentation which size depends on the given argument.
     * 
     * @param size
     *                the size of the representation
     * @return A DomRepresentation.
     */
    private static Representation createChunckedRepresentation(int size) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < size; i++) {
            builder.append("a");
        }
        Representation rep = new StringRepresentation(builder.toString(),
                MediaType.TEXT_PLAIN);
        rep.setSize(Representation.UNKNOWN_SIZE);
        return rep;
    }

    @Override
    protected void call(String uri) throws Exception {
        for (int i = 0; i < 50; i++) {
            sendPut(uri, 10);
        }
        for (int i = 0; i < 50; i++) {
            sendPut(uri, 1024);
        }
        for (int i = 0; i < 50; i++) {
            sendPut(uri, 10240);
        }
    }

    @Override
    protected Application createApplication(Component component) {
        Application application = new Application(component.getContext()) {
            @Override
            public Restlet createRoot() {
                Router router = new Router(getContext());
                router.attach("/test", PutTestResource.class);
                return router;
            }
        };
        return application;
    }

    private void sendPut(String uri, int size) throws Exception {
        Request request = new Request(Method.PUT, uri,
                createChunckedRepresentation(size));
        Response r = new Client(Protocol.HTTP).handle(request);

        try {
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
        // super.testGrizzlyAndApache();
    }

    @Override
    public void testGrizzlyAndInternal() throws Exception {
        // super.testGrizzlyAndInternal();
    }

    @Override
    public void testGrizzlyAndJdkNet() throws Exception {
        // super.testGrizzlyAndJdkNet();
    }

    @Override
    public void testInternalAndApache() throws Exception {
        // super.testInternalAndApache();
    }

    @Override
    public void testInternalAndInternal() throws Exception {
        // super.testInternalAndInternal();
    }

    @Override
    public void testInternalAndJdkNet() throws Exception {
        // super.testInternalAndJdkNet();
    }

    @Override
    public void testJettyAndApache() throws Exception {
        // super.testJettyAndApache();
    }

    @Override
    public void testJettyAndInternal() throws Exception {
        // super.testJettyAndInternal();
    }

    @Override
    public void testJettyAndJdkNet() throws Exception {
        // super.testJettyAndJdkNet();
    }

    @Override
    public void testSimpleAndApache() throws Exception {
        // super.testSimpleAndApache();
    }

    @Override
    public void testSimpleAndInternal() throws Exception {
        // super.testSimpleAndInternal();
    }

    @Override
    public void testSimpleAndJdkNet() throws Exception {
        super.testSimpleAndJdkNet();
    }

}
