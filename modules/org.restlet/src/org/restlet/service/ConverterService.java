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

package org.restlet.service;

import java.util.List;

import org.restlet.Context;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.converter.ConverterUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

/**
 * Application service converting between representation and regular Java
 * objects. The conversion can work in work directions.<br>
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
     * Returns the list of object classes that can be converted from a given
     * variant.
     * 
     * @param variant
     *            The source variant.
     * @return The list of object class that can be converted.
     */
    public List<Class<?>> getObjectClasses(Variant variant) {
        return null;
    }

    /**
     * Returns the list of variants that can be converted from a given object
     * class.
     * 
     * @param objectClass
     *            The source object class.
     * @return The list of variants that can be converted.
     */
    public List<Variant> getVariants(Class<?> objectClass) {
        return null;
    }

    /**
     * Converts a Representation into a regular Java object.
     * 
     * @param resource
     *            The calling resource.
     * @param representation
     *            The representation to convert.
     * @param objectClass
     *            The expected class of the Java object.
     * @return The converted Java object.
     */
    public Object toObject(Representation representation) {
        return toObject(representation, null, null);
    }

    /**
     * Converts a Representation into a regular Java object.
     * 
     * @param <T>
     *            The expected class of the Java object.
     * @param representation
     *            The representation to convert.
     * @param targetClass
     *            The target class of the Java object.
     * @param resource
     *            The calling resource.
     * @return The converted Java object.
     */
    public <T> T toObject(Representation representation, Class<T> targetClass,
            UniformResource resource) {
        return null;
    }

    /**
     * Converts a regular Java object into a Representation.
     * 
     * @param object
     *            The object to convert.
     * @return The converted representation.
     */
    public Representation toRepresentation(Object object) {
        return toRepresentation(object, null, null);
    }

    /**
     * Converts a regular Java object into a Representation.
     * 
     * @param object
     *            The object to convert.
     * @param targetVariant
     *            The target variant.
     * @param resource
     *            The calling resource.
     * @return The converted representation.
     */
    public Representation toRepresentation(Object object,
            Variant targetVariant, UniformResource resource) {
        Representation result = null;
        ConverterHelper ch = ConverterUtils.getHelper(object, targetVariant,
                resource);

        if (ch != null) {
            result = ch.toRepresentation(object, targetVariant, resource);
        } else {
            Context.getCurrentLogger().warning(
                    "Unable to find a converter for this object : " + object);
        }

        return result;
    }

}
