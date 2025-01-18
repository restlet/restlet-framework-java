/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.test.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
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

    private static final int LOOP_NUMBER = 50;

    @Override
    protected void call(String uri) throws Exception {
        for (int i = 0; i < LOOP_NUMBER; i++) {
            sendGet(uri);
            sendPut(uri);
        }
    }

    @Override
    protected Application createApplication(Component component) {
        return new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", PutTestResource.class);
                return router;
            }
        };
    }


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
            assertChunkedHeader(getRequest());

            final DomRepresentation dom = new DomRepresentation(entity);
            DomRepresentation rep = null;
            try {
                final Document doc = dom.getDocument();
                assertXML(dom);
                rep = new DomRepresentation(MediaType.TEXT_XML, doc);
                getResponse().setEntity(rep);
            } catch (IOException ex) {
                fail("Cannot send XML response", ex);
            }
            return rep;
        }
    }

    private static void assertXML(DomRepresentation entity) {
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

    private static void assertChunkedHeader(Message message) {
        @SuppressWarnings("unchecked")
        Series<Header> headers = (Series<Header>) message.getAttributes().get(
                HeaderConstants.ATTRIBUTE_HEADERS);
        Header p = headers.getFirst(HeaderConstants.HEADER_TRANSFER_ENCODING,
                true);
        assertNotNull(p);
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
            String xmlRepresentionAsString = new DomRepresentation(MediaType.TEXT_XML, doc).getText();
            rep = new StringRepresentation(xmlRepresentionAsString);
            rep.setSize(-1); // force chunked encoding
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return rep;
    }


    private void sendGet(String uri) throws Exception {
        final Request request = new Request(Method.GET, uri);
        final Client client = new Client(Protocol.HTTP);
        final Response response = client.handle(request);
        try {
            assertEquals(Status.SUCCESS_OK, response.getStatus(), response.getStatus().getDescription());
            assertXML(new DomRepresentation(response.getEntity()));
        } finally {
            response.release();
            client.stop();
        }
    }

    private void sendPut(String uri) throws Exception {
        Request request = new Request(Method.PUT, uri, createTestXml());
        Client client = new Client(Protocol.HTTP);
        Response response = client.handle(request);

        try {
            assertChunkedHeader(response);
            assertEquals(Status.SUCCESS_OK, response.getStatus(), response.getStatus().getDescription());
            assertXML(new DomRepresentation(response.getEntity()));
        } finally {
            response.release();
            client.stop();
        }

    }

}
