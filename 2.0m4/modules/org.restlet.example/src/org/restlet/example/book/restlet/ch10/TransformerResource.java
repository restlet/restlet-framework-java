/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.book.restlet.ch10;

import java.io.File;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.ext.xml.TransformRepresentation;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;

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
