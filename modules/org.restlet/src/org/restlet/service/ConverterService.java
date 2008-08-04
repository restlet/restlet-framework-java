/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.service;

import org.restlet.resource.Representation;

/**
 * Service converting message entities into higher-level objects. As the default
 * implementation doesn't do any convertion by default, you have to subclass it
 * and update the Application's "converterService" property with your own
 * instance. Once this done, any Restlet or Resource that is part of this
 * application can easily convert from representations to objects and the other
 * way around. You just have to use the getEntityAsObject() method to convert a
 * message's representation into a higher-level object and the setEntity(Object)
 * method to convert a higher-level object into a representation.
 * 
 * @see org.restlet.data.Message#getEntityAsObject()
 * @see org.restlet.data.Message#setEntity(Object)
 * @author Jerome Louvel (contact@noelios.com)
 * @deprecated Since 1.1 with no replacement as it doesn't fit well with content
 *             negotiation. Most users prefer to handle those conversion in
 *             Resource subclasses.
 */
@Deprecated
public class ConverterService extends Service {
    /**
     * Converts a representation into a higher-level object. Returns null by
     * default.
     * 
     * @param representation
     *            The representation to convert.
     * @return A higher-level object.
     */
    public Object toObject(Representation representation) {
        return null;
    }

    /**
     * Converts a higher-level object into a representation. Returns null by
     * default.
     * 
     * @param object
     *            The higher-level object.
     * @return A representation.
     */
    public Representation toRepresentation(Object object) {
        return null;
    }

}
