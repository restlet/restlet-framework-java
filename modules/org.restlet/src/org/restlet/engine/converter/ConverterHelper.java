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

package org.restlet.engine.converter;

import java.util.List;

import org.restlet.resource.Representation;
import org.restlet.resource.UniformResource;
import org.restlet.resource.Variant;

/**
 * Converter between Representations and regular Java objects.
 * 
 * @author Jerome Louvel
 */
public abstract class ConverterHelper {

    /**
     * Returns the list of object classes that can be converted from a given
     * variant.
     * 
     * @param variant
     *            The source variant.
     * @return The list of object class that can be converted.
     */
    public abstract List<Class<?>> getObjectClasses(Variant variant);

    /**
     * Returns the list of variants that can be converted from a given object
     * class.
     * 
     * @param objectClass
     *            The source object class.
     * @return The list of variants that can be converted.
     */
    public abstract List<Variant> getVariants(Class<?> objectClass);

    /**
     * Converts a Representation into a regular Java object.
     * 
     * @param <T>
     *            The expected class of the Java object.
     * @param resource
     *            The calling resource.
     * @param representation
     *            The source representation to convert.
     * @param objectClass
     *            The expected class of the Java object.
     * @return The converted Java object.
     */
    public abstract <T> T toObject(UniformResource resource,
            Representation representation, Class<T> objectClass);

    /**
     * Converts a regular Java object into a Representation.
     * 
     * @param resource
     *            The calling resource.
     * @param object
     *            The source object to convert.
     * @param variant
     *            The expected representation metadata.
     * @return The converted representation.
     */
    public abstract Representation toRepresentation(UniformResource resource,
            Object object, Variant variant);

}
