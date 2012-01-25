/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.engine;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Message;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.util.Series;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This tests the ability of the connectors to handle chunked encoding.
 * 
 * The test uses each connector to PUT an entity that will be sent chunked and
 * also to receive a chunked response.
 */
public class ChunkedEncodingTestCase extends BaseConnectorsTestCase {

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
            checkForChunkedHeader(getRequest());

            final DomRepresentation dom = new DomRepresentation(entity);
            DomRepresentation rep = null;
            try {
                final Document doc = dom.getDocument();
                assertXML(dom);
                rep = new DomRepresentation(MediaType.TEXT_XML, doc);
                getResponse().setEntity(rep);
            } catch (IOException ex) {
                ex.printStackTrace();
                fail(ex.getMessage());
            }
            return rep;
        }
    }

    private static int LOOP_NUMBER = 50;

    static void assertXML(DomRepresentation entity) {
        try {
            final Document document = entity.getDocument();
            final Node root = document.getDocumentElement();
            final NodeList children = root.getChildNodes();

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
        @SuppressWarnings("unchecked")
        Series<Header> headers = (Series<Header>) message.getAttributes().get(
                HeaderConstants.ATTRIBUTE_HEADERS);
        Header p = headers.getFirst(HeaderConstants.HEADER_TRANSFER_ENCODING);
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
        final Document doc = createDocument();
        final Element root = doc.createElement("root");

        doc.appendChild(root);

        for (int i = 0; i < 2; i++) {
            final Element e = doc.createElement("child-" + i);
            e.setAttribute("name", "name-" + i);
            root.appendChild(e);
        }

        Representation rep = null;
        try {
            rep = new StringRepresentation(new DomRepresentation(
                    MediaType.TEXT_XML, doc).getText());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        rep.setSize(-1);
        return rep;
    }

    boolean checkedForChunkedResponse;

    @Override
    protected void call(String uri) throws Exception {
        for (int i = 0; i < LOOP_NUMBER; i++) {
            sendGet(uri);
            sendPut(uri);
        }
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

    private void sendGet(String uri) throws Exception {
        final Request request = new Request(Method.GET, uri);
        Client c = new Client(Protocol.HTTP);
        final Response r = c.handle(request);
        try {
            assertEquals(r.getStatus().getDescription(), Status.SUCCESS_OK,
                    r.getStatus());
            assertXML(new DomRepresentation(r.getEntity()));
        } finally {
            r.release();
            c.stop();
        }
    }

    private void sendPut(String uri) throws Exception {
        Request request = new Request(Method.PUT, uri, createTestXml());
        Client c = new Client(Protocol.HTTP);
        Response r = c.handle(request);

        try {
            checkForChunkedHeader(r);
            assertEquals(r.getStatus().getDescription(), Status.SUCCESS_OK,
                    r.getStatus());
            assertXML(new DomRepresentation(r.getEntity()));
        } finally {
            r.release();
            c.stop();
        }

    }

}
