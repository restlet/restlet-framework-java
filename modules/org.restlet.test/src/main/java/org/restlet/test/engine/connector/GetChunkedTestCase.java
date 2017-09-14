/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.test.engine.connector;

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

            final StringBuilder builder = new StringBuilder();
            builder.append("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">");
            builder.append("<xsl:output method=\"text\"/>");
            builder.append("<xsl:template match=\"/\">");
            builder.append("<xsl:apply-templates />");
            builder.append("</xsl:template>");
            builder.append("</xsl:stylesheet>");
            final Representation transformSheet = new StringRepresentation(
                    builder.toString(), MediaType.TEXT_XML);

            // Instantiates the representation with both source and stylesheet.
            final Representation representation = new TransformRepresentation(
                    getContext(), source, transformSheet);
            // Set the right media-type
            representation.setMediaType(variant.getMediaType());

            return representation;

        }
    }

    @Override
    protected void call(String uri) throws Exception {
        final Request request = new Request(Method.GET, uri);
        Client c = new Client(Protocol.HTTP);
        final Response r = c.handle(request);
        assertEquals(r.getStatus().getDescription(), Status.SUCCESS_OK,
                r.getStatus());
        assertEquals("Hello world", r.getEntity().getText());
        c.stop();
    }

    @Override
    protected Application createApplication(Component component) {
        final Application application = new Application() {
            @Override
            public Restlet createInboundRoot() {
                final Router router = new Router(getContext());
                router.attach("/test", GetChunkedTestResource.class);
                return router;
            }
        };

        return application;
    }
}
