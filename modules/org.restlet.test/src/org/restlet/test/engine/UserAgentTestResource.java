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

package org.restlet.test.engine;

import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;

/**
 * Simple resource that returns at least text/html and text/xml representations.
 */
public class UserAgentTestResource extends ServerResource {

    public UserAgentTestResource() {
        getVariants().add(new Variant(MediaType.TEXT_XML));
        getVariants().add(new Variant(MediaType.TEXT_HTML));
    }

    @Override
    public Representation get(Variant variant) throws ResourceException {
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
