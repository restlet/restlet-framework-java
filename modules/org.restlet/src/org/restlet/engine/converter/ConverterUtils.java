/**
 * Copyright 2005-2010 Noelios Technologies.
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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.engine.Engine;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

/**
 * Utilities for the converter service.
 * 
 * @author Jerome Louvel
 */
public class ConverterUtils {

    /**
     * Returns the list of variants that can be converted from a given object
     * class.
     * 
     * @param sourceClass
     *            The source class.
     * @param targetVariant
     *            The expected representation metadata.
     * @return The list of variants that can be converted.
     */
    public static List<VariantInfo> getVariants(Class<?> sourceClass,
            Variant targetVariant) {
        List<VariantInfo> result = null;
        List<VariantInfo> helperVariants = null;

        for (ConverterHelper ch : Engine.getInstance()
                .getRegisteredConverters()) {
            // List of variants that can be converted from the source class
            helperVariants = ch.getVariants(sourceClass);

            if (helperVariants != null) {
                // Loop over the variants list
                for (VariantInfo helperVariant : helperVariants) {
                    if (helperVariant.includes(targetVariant)) {
                        if (result == null) {
                            result = new ArrayList<VariantInfo>();
                        }

                        // Detected a more generic variant, but still consider
                        // the conversion is possible to the target variant.
                        // TODO: Add support for the other kind of metadata
                        VariantInfo newVariant = new VariantInfo(targetVariant
                                .getMediaType());

                        if (!result.contains(newVariant)) {
                            result.add(newVariant);
                        }
                    } else if (targetVariant == null
                            || targetVariant.includes(helperVariant)) {
                        // Add this variant for content negotiation.
                        if (result == null) {
                            result = new ArrayList<VariantInfo>();
                        }

                        if (!result.contains(helperVariant)) {
                            result.add(helperVariant);
                        }
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the best converter helper matching the given parameters.
     * 
     * @param source
     *            The object to convert to a representation.
     * @param target
     *            The target representation variant.
     * @param resource
     *            The optional parent resource.
     * @return The matched converter helper or null.
     */
    public static ConverterHelper getBestHelper(Object source, Variant target,
            UniformResource resource) {
        ConverterHelper result = null;
        float bestScore = -1.0F;
        float currentScore;

        for (ConverterHelper ch : Engine.getInstance()
                .getRegisteredConverters()) {
            try {
                currentScore = ch.score(source, target, resource);

                if (currentScore > bestScore) {
                    bestScore = currentScore;
                    result = ch;
                }
            } catch (Exception e) {
                Context.getCurrentLogger().log(
                        Level.SEVERE,
                        "Unable get the score of the " + ch
                                + " converter helper.", e);
            }
        }

        return result;
    }

    /**
     * Returns the best converter helper matching the given parameters.
     * 
     * @param <T>
     *            The target class.
     * @param source
     *            The source representation variant.
     * @param target
     *            The target class.
     * @param resource
     *            The parent resource.
     * @return The matched converter helper or null.
     */
    public static <T> ConverterHelper getBestHelper(Representation source,
            Class<T> target, UniformResource resource) {
        ConverterHelper result = null;
        float bestScore = -1.0F;
        float currentScore;

        for (ConverterHelper ch : Engine.getInstance()
                .getRegisteredConverters()) {
            currentScore = ch.score(source, target, resource);

            if (currentScore > bestScore) {
                bestScore = currentScore;
                result = ch;
            }
        }

        return result;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class
     * i.e. it isn't instantiable and extensible.
     */
    private ConverterUtils() {
    }
}
