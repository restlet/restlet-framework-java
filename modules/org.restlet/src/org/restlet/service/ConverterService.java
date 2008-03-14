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
public class ConverterService {
    /**
     * Converts a representation into a higher-level object. Returns null by
     * default.
     * 
     * @param representation
     *                The representation to convert.
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
     *                The higher-level object.
     * @return A representation.
     */
    public Representation toRepresentation(Object object) {
        return null;
    }

}
