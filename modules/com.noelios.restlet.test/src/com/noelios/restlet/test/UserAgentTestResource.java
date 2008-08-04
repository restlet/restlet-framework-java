/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.test;

import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.ResourceException;
import org.restlet.resource.StringRepresentation;
import org.restlet.resource.Variant;

/**
 * Simple resource that returns at least text/html and text/xml representations.
 */
public class UserAgentTestResource extends Resource {

    public UserAgentTestResource(Context context, Request request,
            Response response) {
        super(context, request, response);
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    public Representation represent(Variant variant) throws ResourceException {
        final MediaType mediaType = variant.getMediaType();
        if (mediaType.equals(MediaType.TEXT_XML)) {
            return new StringRepresentation("<a>b</a>", mediaType);
        } else if (mediaType.equals(MediaType.TEXT_HTML)) {
            return new StringRepresentation("<html><body>a</body></html>",
                    mediaType);
        }

        return null;
    }
}
