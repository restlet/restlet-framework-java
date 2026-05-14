/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.converter;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.restlet.Context;
import org.restlet.engine.Engine;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * Utilities for the converter service.
 *
 * @author Jerome Louvel
 */
public class ConverterUtils {

    /**
     * Returns the best converter helper matching the given parameters.
     *
     * @param source The object to convert to a representation.
     * @param target The target representation variant.
     * @param resource The optional parent resource.
     * @return The matched converter helper or null.
     */
    public static ConverterHelper getBestHelper(Object source, Variant target, Resource resource) {
        ConverterHelper result = null;
        float bestScore = -1.0F;
        float currentScore;

        for (ConverterHelper ch : Engine.getInstance().getRegisteredConverters()) {
            if (ch != null) {
                try {
                    currentScore = ch.score(source, target, resource);

                    if (currentScore > bestScore) {
                        bestScore = currentScore;
                        result = ch;
                    }
                } catch (Exception e) {
                    Context.getCurrentLogger()
                            .log(
                                    Level.SEVERE,
                                    e,
                                    () ->
                                            "Unable get the score of the "
                                                    + ch
                                                    + " converter helper.");
                }
            }
        }

        return result;
    }

    /**
     * Returns the best converter helper matching the given parameters.
     *
     * @param <T> The target class.
     * @param source The source representation variant.
     * @param target The target class.
     * @param resource The parent resource.
     * @return The matched converter helper or null.
     */
    public static <T> ConverterHelper getBestHelper(
            Representation source, Class<T> target, Resource resource) {
        ConverterHelper result = null;
        float bestScore = -1.0F;
        float currentScore;

        for (ConverterHelper ch : Engine.getInstance().getRegisteredConverters()) {
            if (ch != null) {
                currentScore = ch.score(source, target, resource);

                if (currentScore > bestScore) {
                    bestScore = currentScore;
                    result = ch;
                }
            }
        }

        return result;
    }

    /**
     * Returns the list of variants that can be converted from a given object class.
     *
     * @param sourceClass The source class.
     * @param targetVariant The expected representation metadata.
     * @return The list of variants that can be converted.
     */
    public static List<VariantInfo> getVariants(Class<?> sourceClass, Variant targetVariant) {
        List<VariantInfo> result = null;

        for (ConverterHelper ch : Engine.getInstance().getRegisteredConverters()) {
            if (ch != null) {
                try {
                    result = ch.addVariants(sourceClass, targetVariant, result);
                } catch (IOException e) {
                    Context.getCurrentLogger()
                            .log(
                                    Level.FINE,
                                    e,
                                    () ->
                                            "Unable get the variants of the "
                                                    + ch
                                                    + " converter helper.");
                }
            }
        }

        return result;
    }

    /**
     * Private constructor to ensure that the class acts as a true utility class i.e., it isn't
     * instantiable and extensible.
     */
    private ConverterUtils() {}
}
