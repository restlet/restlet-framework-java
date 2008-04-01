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
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.TransformRepresentation;
import org.restlet.resource.Variant;

/**
 * Test that a simple get works for all the connectors.
 * 
 * @author Kevin Conaway
 */
public class GetChunkedTestCase extends BaseConnectorsTestCase {

    @Override
    protected Application createApplication(Component component) {
        Application application = new Application(component.getContext()) {
            @Override
            public Restlet createRoot() {
                Router router = new Router(getContext());
                router.attach("/test", GetChunkedTestResource.class);
                return router;
            }
        };

        return application;
    }

    @Override
    protected void call(String uri) throws Exception {
        Request request = new Request(Method.GET, uri);
        Response r = new Client(Protocol.HTTP).handle(request);
        assertEquals(r.getStatus().getDescription(), Status.SUCCESS_OK, r
                .getStatus());
        assertEquals("Hello world", r.getEntity().getText());
    }

    public static class GetChunkedTestResource extends Resource {

        public GetChunkedTestResource(Context ctx, Request request,
                Response response) {
            super(ctx, request, response);
            getVariants().add(new Variant(MediaType.TEXT_PLAIN));
        }

        @Override
        public Representation represent(Variant variant) {
            // Get the source XML
            Representation source = new StringRepresentation(
                    "<?xml version='1.0'?><mail>Hello world</mail>",
                    MediaType.APPLICATION_XML);

            StringBuilder builder = new StringBuilder();
            builder
                    .append("<xsl:stylesheet xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" version=\"1.0\">");
            builder.append("<xsl:output method=\"text\"/>");
            builder.append("<xsl:template match=\"/\">");
            builder.append("<xsl:apply-templates />");
            builder.append("</xsl:template>");
            builder.append("</xsl:stylesheet>");
            Representation transformSheet = new StringRepresentation(builder
                    .toString(), MediaType.TEXT_XML);

            // Instantiates the representation with both source and stylesheet.
            Representation representation = new TransformRepresentation(
                    getContext(), source, transformSheet);
            // Set the right media-type
            representation.setMediaType(variant.getMediaType());

            return representation;

        }
    }
}
