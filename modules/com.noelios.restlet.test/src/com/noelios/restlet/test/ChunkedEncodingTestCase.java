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

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Message;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.noelios.restlet.http.HttpConstants;

/**
 * This tests the ability of the connectors to handle chunked encoding.
 * 
 * The test uses each connector to PUT an entity that will be sent chunked and
 * also to receive a chunked response.
 */
public class ChunkedEncodingTestCase extends BaseConnectorsTestCase {

    public static class PutTestResource extends Resource {

        public PutTestResource(Context ctx, Request request, Response response) {
            super(ctx, request, response);
            getVariants().add(new Variant(MediaType.TEXT_XML));
        }

        @Override
        public boolean allowPut() {
            return true;
        }

        @Override
        public Representation represent(Variant variant) {
            return createTestXml();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void storeRepresentation(Representation entity) {
            checkForChunkedHeader(getRequest());

            DomRepresentation dom = new DomRepresentation(entity);
            try {
                Document doc = dom.getDocument();
                assertXML(dom);
                getResponse().setEntity(
                        new DomRepresentation(MediaType.TEXT_XML, doc));
            } catch (IOException ex) {
                ex.printStackTrace();
                fail(ex.getMessage());

            }
        }
    }

    static void assertXML(DomRepresentation entity) {
        try {
            Document document = entity.getDocument();
            Node root = document.getDocumentElement();
            NodeList children = root.getChildNodes();

            assertEquals("root", root.getNodeName());
            assertEquals(2, children.getLength());
            assertEquals("child-0", children.item(0).getNodeName());
            assertEquals("name-0", children.item(0).getAttributes()
                    .getNamedItem("name").getNodeValue());
            assertEquals("child-1", children.item(1).getNodeName());
            assertEquals("name-1", children.item(1).getAttributes()
                    .getNamedItem("name").getNodeValue());

        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    static void checkForChunkedHeader(Message message) {
        Form parameters = (Form) message.getAttributes().get(
                "org.restlet.http.headers");
        Parameter p = parameters
                .getFirst(HttpConstants.HEADER_TRANSFER_ENCODING);
        assertFalse(p == null);
        assertEquals("chunked", p.getValue());
    }

    private static Document createDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .newDocument();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Representation createTestXml() {
        Document doc = createDocument();
        Element root = doc.createElement("root");

        doc.appendChild(root);

        for (int i = 0; i < 2; i++) {
            Element e = doc.createElement("child-" + i);
            e.setAttribute("name", "name-" + i);
            root.appendChild(e);
        }

        return new DomRepresentation(MediaType.TEXT_XML, doc);
    }

    boolean checkedForChunkedResponse;

    @Override
    protected void call(String uri) throws Exception {
        sendPut(uri);
        sendGet(uri);
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

    private void sendGet(String uri) throws Exception {
        Request request = new Request(Method.GET, uri);
        Response r = new Client(Protocol.HTTP).handle(request);
        assertEquals(r.getStatus().getDescription(), Status.SUCCESS_OK, r
                .getStatus());
        assertXML(r.getEntityAsDom());
    }

    @SuppressWarnings("unchecked")
    private void sendPut(String uri) throws Exception {
        Request request = new Request(Method.PUT, uri, createTestXml());
        Response r = new Client(Protocol.HTTP).handle(request);

        if (checkedForChunkedResponse) {
            checkForChunkedHeader(r);
        }

        assertEquals(r.getStatus().getDescription(), Status.SUCCESS_OK, r
                .getStatus());
        assertXML(r.getEntityAsDom());
    }

    @Override
    public void setUp() {
        super.setUp();
        checkedForChunkedResponse = true;
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
    public void testInternalAndInternal() throws Exception {
        super.testInternalAndInternal();
    }

    @Override
    public void testJettyAndInternal() throws Exception {
        // Jetty will not send a chunked response when a client sends
        // Connection: close, which the default client helper does
        checkedForChunkedResponse = false;
        super.testJettyAndInternal();
    }

    @Override
    public void testSimpleAndInternal() throws Exception {
        // Simple will not send a chunked response when a client sends
        // Connection: close, which the default client helper does
        checkedForChunkedResponse = false;
        super.testSimpleAndInternal();
    }
}
