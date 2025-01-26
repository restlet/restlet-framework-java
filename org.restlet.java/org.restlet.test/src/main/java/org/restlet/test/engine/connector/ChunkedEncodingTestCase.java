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
import java.util.stream.Stream;

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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * This tests the ability of the connectors to handle chunked encoding.
 *
 * The test uses each connector to PUT an entity that will be sent chunked and
 * also to receive a chunked response.
 */
public class ChunkedEncodingTestCase extends BaseConnectorsTestCase {

    private static final int LOOP_NUMBER = 50;

    @Override
    protected Stream<ConnectorTestCase> listTestCases() {
        return Stream.of(
                new ConnectorTestCase(HttpServer.INTERNAL, HttpClient.INTERNAL)
        );
    }

    @Override
    protected void doTestUri(String uri) throws Exception {
        for (int testIndex = 0; testIndex < LOOP_NUMBER; testIndex++) {
            sendGet(testIndex, uri);
            sendPut(testIndex, uri);
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
                rep = new DomRepresentation(MediaType.TEXT_XML, doc);
                getResponse().setEntity(rep);
            } catch (IOException ex) {
                fail("Cannot send XML response", ex);
            }
            return rep;
        }
    }

    private static void assertXML(int testIndex, DomRepresentation entity) {
        try {
            String expected = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><root><child-0 name=\"name-0\"/><child-1 name=\"name-1\"/></root>";
            String text = entity.getText();
            assertEquals(expected, text, String.format("test #%d: xml representation is wrong", testIndex));
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
    }

    private static void assertChunkedHeader(Message message) {
        final Header transferEncoding = message.getHeaders()
                .getFirst(HeaderConstants.HEADER_TRANSFER_ENCODING, true);
        assertNotNull(transferEncoding);
        assertEquals("chunked", transferEncoding.getValue());
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
            String xmlRepresentationAsString = new DomRepresentation(MediaType.TEXT_XML, doc).getText();
            rep = new StringRepresentation(xmlRepresentationAsString);
            rep.setSize(Representation.UNKNOWN_SIZE); // force chunked encoding
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return rep;
    }


    private void sendGet(int testIndex, String uri) throws Exception {
        final Request request = new Request(Method.GET, uri);
        final Client client = new Client(Protocol.HTTP);
        final Response response = client.handle(request);

        try {
            assertEquals(Status.SUCCESS_OK, response.getStatus(), String.format("test #%d: response's status is wrong", testIndex));
            assertXML(testIndex, new DomRepresentation(response.getEntity()));
        } finally {
            response.release();
            client.stop();
        }
    }

    private void sendPut(int testIndex, String uri) throws Exception {
        final Request request = new Request(Method.PUT, uri, createTestXml());
        final Client client = new Client(Protocol.HTTP);
        final Response response = client.handle(request);

        try {
            assertChunkedHeader(response);
            assertEquals(Status.SUCCESS_OK, response.getStatus(), String.format("test #%d: response's status is wrong", testIndex));
            assertXML(testIndex, new DomRepresentation(response.getEntity()));
        } finally {
            response.release();
            client.stop();
        }

    }

}
