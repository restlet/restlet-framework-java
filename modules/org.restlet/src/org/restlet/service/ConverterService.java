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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.Context;
import org.restlet.engine.Engine;
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
     * @param sourceVariant
     *            The source variant.
     * @return The list of object class that can be converted.
     */
    public List<Class<?>> getObjectClasses(Variant sourceVariant) {
        List<Class<?>> result = null;
        List<Class<?>> helperObjectClasses = null;

        for (ConverterHelper ch : Engine.getInstance()
                .getRegisteredConverters()) {
            helperObjectClasses = ch.getObjectClasses(sourceVariant);

            if (helperObjectClasses != null) {
                if (result == null) {
                    result = new ArrayList<Class<?>>();
                }

                result.addAll(helperObjectClasses);
            }
        }

        return result;
    }

    /**
     * Returns the list of variants that can be converted from a given object
     * class.
     * 
     * @param sourceClass
     *            The source class.
     * @return The list of variants that can be converted.
     */
    public List<Variant> getVariants(Class<?> sourceClass) {
        List<Variant> result = null;
        List<Variant> helperVariants = null;

        for (ConverterHelper ch : Engine.getInstance()
                .getRegisteredConverters()) {
            helperVariants = ch.getVariants(sourceClass);

            if (helperVariants != null) {
                if (result == null) {
                    result = new ArrayList<Variant>();
                }

                result.addAll(helperVariants);
            }
        }

        return result;
    }

    /**
     * Converts a Representation into a regular Java object.
     * 
     * @param sourceRepresentation
     *            The source representation to convert.
     * @return The converted Java object.
     * @throws IOException
     */
    public Object toObject(Representation sourceRepresentation)
            throws IOException {
        return toObject(sourceRepresentation, null, null);
    }

    /**
     * Converts a Representation into a regular Java object.
     * 
     * @param <T>
     *            The expected class of the Java object.
     * @param sourceRepresentation
     *            The source representation to convert.
     * @param targetClass
     *            The target class of the Java object.
     * @param resource
     *            The parent resource.
     * @return The converted Java object.
     * @throws IOException
     */
    public <T> T toObject(Representation sourceRepresentation,
            Class<T> targetClass, UniformResource resource) throws IOException {
        T result = null;
        ConverterHelper ch = ConverterUtils.getHelper(sourceRepresentation,
                targetClass, resource);

        if (ch != null) {
            result = ch.toObject(sourceRepresentation, targetClass, resource);
        } else {
            Context.getCurrentLogger().warning(
                    "Unable to find a converter for this representation : "
                            + sourceRepresentation);
        }

        return result;
    }

    /**
     * Converts a regular Java object into a Representation.
     * 
     * @param sourceObject
     *            The source object to convert.
     * @return The converted representation.
     */
    public Representation toRepresentation(Object sourceObject) {
        return toRepresentation(sourceObject, null, null);
    }

    /**
     * Converts a regular Java object into a Representation.
     * 
     * @param sourceObject
     *            The source object to convert.
     * @param targetVariant
     *            The target representation variant.
     * @param resource
     *            The parent resource.
     * @return The converted representation.
     */
    public Representation toRepresentation(Object sourceObject,
            Variant targetVariant, UniformResource resource) {
        Representation result = null;
        ConverterHelper ch = ConverterUtils.getHelper(sourceObject,
                targetVariant, resource);

        if (ch != null) {
            result = ch.toRepresentation(sourceObject, targetVariant, resource);
        } else {
            Context.getCurrentLogger().warning(
                    "Unable to find a converter for this object : "
                            + sourceObject);
        }

        return result;
    }

}
