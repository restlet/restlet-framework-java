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

package org.restlet.example.book.restlet.ch10;

import java.io.File;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.FileRepresentation;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.TransformRepresentation;
import org.restlet.resource.Variant;

/**
 *
 */
public class TransformerResource extends Resource {

    public TransformerResource(Context context, Request request,
            Response response) {
        super(context, request, response);
        // This resource is able to generate two kinds of representations.
        getVariants().add(new Variant(MediaType.TEXT_HTML));
        getVariants().add(new Variant(MediaType.TEXT_PLAIN));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final File dir = new File(
                "D:\\alaska\\forge\\build\\swc\\nre\\trunk\\books\\apress\\manuscript\\sample");
        // Get the source XML
        final Representation source = new FileRepresentation(new File(dir,
                "mail.xml"), MediaType.APPLICATION_XML);

        Representation transformSheet = null;
        // Get the XSLT stylesheet
        if (MediaType.TEXT_HTML.equals(variant.getMediaType())) {
            transformSheet = new FileRepresentation(new File(dir,
                    "mail_html.xsl"), MediaType.TEXT_XML);
        } else {
            transformSheet = new FileRepresentation(new File(dir,
                    "mail_text.xsl"), MediaType.TEXT_XML);
        }

        // Instantiates the representation with both source and stylesheet.
        final Representation representation = new TransformRepresentation(
                getContext(), source, transformSheet);
        // Set the right media-type
        representation.setMediaType(variant.getMediaType());

        return representation;
    }
}
