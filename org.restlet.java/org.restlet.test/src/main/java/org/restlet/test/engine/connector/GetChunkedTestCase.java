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

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.xml.TransformRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;

/**
 * Test that a simple get works for all the connectors.
 *
 * @author Kevin Conaway
 */
public class GetChunkedTestCase extends BaseConnectorsTestCase {

    @Override
    protected void doTestUri(String uri) throws Exception {
        final Client client = new Client(Protocol.HTTP);
        final Request request = new Request(Method.GET, uri);
        final Response response = client.handle(request);

        try {
            assertEquals(Status.SUCCESS_OK, response.getStatus(), response.getStatus().getDescription());
            assertEquals("Hello world", response.getEntity().getText());
        } finally {
            response.release();
            client.stop();
        }
    }

    @Override
    protected Application createApplication(Component component) {
        return new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", GetChunkedTestResource.class);
                return router;
            }
        };
    }

    public static class GetChunkedTestResource extends ServerResource {

        public GetChunkedTestResource() {
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        }

        @Override
        public Representation get(Variant variant) {
            // Get the source XML
            final Representation source = new StringRepresentation(
                    "<?xml version='1.0'?><mail>Hello world</mail>",
                    MediaType.APPLICATION_XML);

            final Representation transformSheet = getTransformSheetRepresentation();

            // Instantiates the representation with both source and stylesheet.
            final Representation representation = new TransformRepresentation(
                    getContext(), source, transformSheet);
            // Set the right media-type
            representation.setMediaType(variant.getMediaType());

            return representation;
        }

        private static Representation getTransformSheetRepresentation() {
            final String xsltAsString = "<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">" +
                    "<xsl:output method=\"text\"/>" +
                    "<xsl:template match=\"/\">" +
                    "<xsl:apply-templates />" +
                    "</xsl:template>" +
                    "</xsl:stylesheet>";
            return new StringRepresentation(xsltAsString, MediaType.TEXT_XML);
        }
    }
}
