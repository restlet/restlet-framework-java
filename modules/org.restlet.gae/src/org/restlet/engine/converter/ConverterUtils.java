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

package org.restlet.engine.converter;

import java.util.List;

import org.restlet.engine.Engine;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

/**
 * Utilities for the converter service.
 * 
 * @author Jerome Louvel
 */
public class ConverterUtils {

    /**
     * Returns the best converter helper matching the given parameters.
     * 
     * @param sourceObject
     *            The object to convert to a representation.
     * @param targetVariant
     *            The target representation variant.
     * @param resource
     *            The optional parent resource.
     * @return The matched converter helper or null.
     */
    public static ConverterHelper getHelper(Object sourceObject,
            Variant targetVariant, UniformResource resource) {

        List<Variant> variants;
        for (ConverterHelper ch : Engine.getInstance()
                .getRegisteredConverters()) {

            variants = ch.getVariants(sourceObject.getClass(), targetVariant);

            if (variants != null) {
                if (targetVariant != null) {
                    for (Variant variant : variants) {
                        if (variant.isCompatible(targetVariant)) {
                            return ch;
                        }
                    }
                } else {
                    return ch;
                }
            }
        }

        return null;
    }

    /**
     * Returns the best converter helper matching the given parameters.
     * 
     * @param <T>
     *            The target class.
     * @param sourceVariant
     *            The source representation variant.
     * @param targetClass
     *            The target class.
     * @param resource
     *            The parent resource.
     * @return The matched converter helper or null.
     */
    public static <T> ConverterHelper getHelper(Variant sourceVariant,
            Class<T> targetClass, UniformResource resource) {

        List<Class<?>> classes;
        for (ConverterHelper ch : Engine.getInstance()
                .getRegisteredConverters()) {

            classes = ch.getObjectClasses(sourceVariant);

            if (classes != null) {
                if (targetClass != null) {
                    for (Class<?> clazz : classes) {
                        if (clazz.isAssignableFrom(targetClass)) {
                            return ch;
                        }
                    }
                } else {
                    return ch;
                }
            }
        }

        return null;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private ConverterUtils() {
    }
}
