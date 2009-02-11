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

package org.restlet.service;

import org.restlet.resource.Representation;
import org.restlet.resource.UniformResource;
import org.restlet.resource.Variant;

/**
 * Service converting between representation and regular Java objects. The
 * convertion can work in work directions.<br>
 * <br>
 * By default, the following conversions are supported. Additional ones can be
 * plugged into the engine.
 * 
 * @author Jerome Louvel
 */
public class ConverterService extends Service {

    /**
     * Constructor.
     */
    public ConverterService() {
        super();
    }

    /**
     * Constructor.
     * 
     * @param enabled
     *            True if the service has been enabled.
     */
    public ConverterService(boolean enabled) {
        super(enabled);
    }

    /**
     * Converts a Representation into a regular Java object.
     * 
     * @param <T>
     *            The expected class of the Java object.
     * @param resource
     *            The calling resource.
     * @param representation
     *            The representation to convert.
     * @param objectClass
     *            The expected class of the Java object.
     * @return The converted Java object.
     */
    public <T> T toObject(UniformResource resource,
            Representation representation, Class<T> objectClass) {
        return null;
    }

    /**
     * Converts a regular Java object into a Representation.
     * 
     * @param resource
     *            The calling resource.
     * @param object
     *            The object to convert.
     * @param variant
     *            The expected representation metadata.
     * @return The converted representation.
     */
    public Representation toRepresentation(UniformResource resource,
            Object object, Variant variant) {
        return null;
    }

}
