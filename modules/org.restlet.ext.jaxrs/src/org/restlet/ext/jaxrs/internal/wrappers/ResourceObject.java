/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.ext.jaxrs.internal.wrappers;

/**
 * An instance wraps a JAX-RS resource object. See section 3 of JAX-RS
 * specification.
 * 
 * @author Stephan Koops
 */
public class ResourceObject {

    private final Object jaxRsResourceObject;

    private final ResourceClass resourceClass;

    /**
     * Creates a new wrapper for a resource object
     * 
     * @param jaxRsResourceObject
     *            the resource object
     * @param resourceClass
     *            the wrapped resource class
     * @param logger
     *            The logger to log unexpected Exceptions.
     */
    ResourceObject(Object jaxRsResourceObject, ResourceClass resourceClass) {
        if (jaxRsResourceObject == null) {
            throw new IllegalArgumentException(
                    "The JAX-RS resource object must not be null");
        }
        if (resourceClass == null) {
            throw new IllegalArgumentException(
                    "The ResourceClass must not be null");
        }
        if (jaxRsResourceObject instanceof ResourceObject) {
            throw new IllegalArgumentException(
                    "The given resource class object should not be an instance of the wrapping class ResourceObject");
        }
        this.jaxRsResourceObject = jaxRsResourceObject;
        this.resourceClass = resourceClass;
    }

    /**
     * @return Returns the wrapped JAX-RS resource object. Returns never null.
     */
    public Object getJaxRsResourceObject() {
        return this.jaxRsResourceObject;
    }

    /**
     * @return Returns the wrapped resource class. Returns never null.
     */
    public ResourceClass getResourceClass() {
        return this.resourceClass;
    }
}