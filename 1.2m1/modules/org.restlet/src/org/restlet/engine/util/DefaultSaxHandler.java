/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.util;

import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Default SAX handler.
 * 
 * @author Jerome Louvel
 */
public class DefaultSaxHandler extends DefaultHandler implements
        LSResourceResolver {

    /**
     * Allow the application to resolve external resources.
     * 
     * @param type
     *            The type of the resource being resolved.
     * @param namespaceUri
     *            The namespace of the resource being resolved.
     * @param publicId
     *            The public identifier.
     * @param systemId
     *            The system identifier.
     * @param baseUri
     *            The absolute base URI of the resource being parsed.
     * 
     */
    public LSInput resolveResource(String type, String namespaceUri,
            String publicId, String systemId, String baseUri) {
        return null;
    }

}
