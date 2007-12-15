/*
 * Copyright 2005-2007 Noelios Consulting.
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

import junit.framework.TestCase;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.util.Series;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.noelios.restlet.ConnectorHelper;
import com.noelios.restlet.Engine;
import com.noelios.restlet.http.HttpConstants;
import com.noelios.restlet.http.StreamClientHelper;
import com.noelios.restlet.http.StreamServerHelper;

/**
 * This tests the ability of the connectors to handle chunked encoding.
 * 
 * The test uses each connector to PUT an entity that will be sent chunked and
 * also to receive a chunked response.
 */
public class ChunkedEncodingTestCase extends TestCase {

    Component component;

    String uri;

    static int port;

    static {
        port = 1137;
        if (System.getProperties().containsKey("restlet.test.port")) {
            port = Integer.parseInt(System.getProperty("restlet.test.port"));
        }
    }

    public void testDefaultAndDefault() throws Exception {
        runTest(new StreamServerHelper(null), new StreamClientHelper(null));
    }

    public void testDefaultAndHttpClient() throws Exception {
        runTest(new StreamServerHelper(null),
                new com.noelios.restlet.ext.httpclient.HttpClientHelper(null));
    }

    public void testDefaultAndJdkNet() throws Exception {
        runTest(new StreamServerHelper(null),
                new com.noelios.restlet.ext.net.HttpClientHelper(null));
    }

    public void testJettyAndDefault() throws Exception {
        // Jetty will not send a chunked response when a client sends
        // Connection: close, which the default client helper does
        runTest(new com.noelios.restlet.ext.jetty.HttpServerHelper(null),
                new StreamClientHelper(null), false);
    }

    public void testJettyAndHttpClient() throws Exception {
        runTest(new com.noelios.restlet.ext.jetty.HttpServerHelper(null),
                new com.noelios.restlet.ext.httpclient.HttpClientHelper(null));
    }

    public void testJettyAndJdkNet() throws Exception {
        runTest(new com.noelios.restlet.ext.jetty.HttpServerHelper(null),
                new com.noelios.restlet.ext.net.HttpClientHelper(null));
    }

    public void testSimpleAndDefault() throws Exception {
        // Simple will not send a chunked response when a client sends
        // Connection: close, which the default client helper does
        runTest(new com.noelios.restlet.ext.simple.HttpServerHelper(null),
                new StreamClientHelper(null), false);
    }

    public void testSimpleAndHttpClient() throws Exception {
        runTest(new com.noelios.restlet.ext.simple.HttpServerHelper(null),
                new com.noelios.restlet.ext.httpclient.HttpClientHelper(null));
    }

    public void testSimpleAndJdkNet() throws Exception {
        runTest(new com.noelios.restlet.ext.simple.HttpServerHelper(null),
                new com.noelios.restlet.ext.net.HttpClientHelper(null));
    }

    // Helper methods

    private void runTest(ConnectorHelper server, ConnectorHelper client)
            throws Exception {
        runTest(server, client, true);
    }

    private void runTest(ConnectorHelper server, ConnectorHelper client,
            boolean checkedForChunkedResponse) throws Exception {
        Engine nre = new Engine(false);
        nre.getRegisteredServers().add(server);
        nre.getRegisteredClients().add(client);
        org.restlet.util.Engine.setInstance(nre);

        start();
        sendPut(checkedForChunkedResponse);
        stop();
    }

    private void start() throws Exception {
        component = new Component();
        component.getServers().add(Protocol.HTTP, port);
        uri = "http://localhost:" + (port++) + "/test/";

        Application application = new Application(component.getContext()) {
            @Override
            public Restlet createRoot() {
                Router router = new Router(getContext());
                router.attach("/test/", PutTestResource.class);
                return router;
            }
        };

        component.getDefaultHost().attach(application);
        component.start();
    }

    private void stop() throws Exception {
        if (component != null && component.isStarted()) {
            component.stop();
        }
    }

    @SuppressWarnings("unchecked")
    private void sendPut(boolean checkedForChunkedResponse) throws Exception {
        Request request = new Request(Method.PUT, uri, createTestXml());
        Response r = new Client(Protocol.HTTP).handle(request);

        if (checkedForChunkedResponse) {
            Series<Parameter> parameters = (Series<Parameter>) r
                    .getAttributes().get(HttpConstants.ATTRIBUTE_HEADERS);
            checkForChunkedHeader(parameters);
        }

        assertXML(r.getEntityAsDom());
    }

    private Representation createTestXml() {
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

    private Document createDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                    .newDocument();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex);
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

    static void checkForChunkedHeader(Series<Parameter> parameters) {
        Parameter p = parameters
                .getFirst(HttpConstants.HEADER_TRANSFER_ENCODING);
        assertFalse(p == null);
        assertEquals("chunked", p.getValue());
    }

    public static class PutTestResource extends Resource {

        public PutTestResource(Context ctx, Request request, Response response) {
            super(ctx, request, response);
        }

        public boolean allowPut() {
            return true;
        }

        @SuppressWarnings("unchecked")
        public void storeRepresentation(Representation entity) {
            Series<Parameter> parameters = (Series<Parameter>) getRequest()
                    .getAttributes().get(HttpConstants.ATTRIBUTE_HEADERS);
            checkForChunkedHeader(parameters);

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
}
