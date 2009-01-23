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

package org.restlet.data;

/**
 * Reference for linked resources. It has helper methods to build RDF blank
 * nodes and constants for common RDF vocabularies such as RDFS and OWL.
 * 
 * @author Jerome Louvel
 */
public class LinkReference extends Reference {

    /**
     * Creates a reference to a blank node.
     * 
     * @param identifier
     *            The blank node identifier.
     * @return A reference to a blank node.
     */
    public static Reference createBlankReference(String identifier) {
        return new Reference("_:" + identifier);
    }

    /**
     * Indicates if a reference is identifying a blank node.
     * 
     * @param reference
     *            The reference to test.
     * @return True if a reference is identifying a blank node.
     */
    public static boolean isBlank(Reference reference) {
        return ((reference != null) && ("_".equals(reference.getScheme())));
    }
}
