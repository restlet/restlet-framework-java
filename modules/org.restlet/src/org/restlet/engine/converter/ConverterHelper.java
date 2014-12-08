/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.restlet.data.ClientInfo;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.Helper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * Converter between Representations and regular Java objects.
 * 
 * @author Jerome Louvel
 */
public abstract class ConverterHelper extends Helper {

    /**
     * Adds an object class to the given list. Creates a new list if necessary.
     * 
     * @param objectClasses
     *            The object classes list to update or null.
     * @param objectClass
     *            The object class to add.
     * @return The input object classes list or a new one.
     */
    protected List<Class<?>> addObjectClass(List<Class<?>> objectClasses,
            Class<?> objectClass) {
        if (objectClasses == null) {
            objectClasses = new ArrayList<Class<?>>();
        }

        objectClasses.add(objectClass);
        return objectClasses;
    }

    /**
     * Adds a variant to the given list. Creates a new list if necessary.
     * 
     * @param variants
     *            The variants list to update or null.
     * @param userVariant
     *            The variant to add if not null.
     * @return The input variants list or a new one.
     */
    protected List<VariantInfo> addVariant(List<VariantInfo> variants,
            VariantInfo userVariant) {
        if (userVariant != null) {
            if (variants == null) {
                variants = new ArrayList<VariantInfo>();
            }

            variants.add(userVariant);
        }

        return variants;
    }

    /**
     * Returns the list of variants that can be converted from a given object
     * class.
     * 
     * @param sourceClass
     *            The source class.
     * @param targetVariant
     *            The expected representation metadata.
     * @param variants
     *            The variants list to update.
     * @throws IOException
     */
    public List<VariantInfo> addVariants(Class<?> sourceClass,
            Variant targetVariant, List<VariantInfo> variants)
            throws IOException {
        // List of variants that can be converted from the source class
        List<VariantInfo> helperVariants = getVariants(sourceClass);

        if (helperVariants != null) {
            // Loop over the variants list
            for (VariantInfo helperVariant : helperVariants) {
                if (targetVariant == null) {
                    variants = addVariant(variants, helperVariant);
                } else if (helperVariant.includes(targetVariant)) {
                    // Detected a more generic variant, but still
                    // consider
                    // the conversion is possible to the target variant.
                    variants = addVariant(variants, new VariantInfo(
                            targetVariant.getMediaType()));
                } else if (targetVariant.includes(helperVariant)) {
                    // Detected a more specific variant, but still
                    // consider
                    // the conversion is possible to the target variant.
                    variants = addVariant(variants, helperVariant);
                }
            }
        }

        return variants;
    }

    /**
     * Returns the list of object classes that can be converted from a given
     * variant.
     * 
     * @param source
     *            The source variant.
     * @return The list of object class that can be converted.
     */
    public abstract List<Class<?>> getObjectClasses(Variant source);

    /**
     * Returns the list of variants that can be converted from a given object
     * class. The preferred variant should be set in first position.
     * 
     * @param source
     *            The source object class.
     * @return The list of variants that can be converted.
     */
    public abstract List<VariantInfo> getVariants(Class<?> source)
            throws IOException;

    /**
     * Returns the list of variants that can be converted from a given object
     * class by a specific converter helper.
     * 
     * @param sourceClass
     *            The source class.
     * @param targetVariant
     *            The expected representation metadata.
     * @return The list of variants that can be converted.
     * @throws IOException
     */
    public List<VariantInfo> getVariants(Class<?> sourceClass,
            Variant targetVariant) throws IOException {
        return addVariants(sourceClass, targetVariant, null);
    }

    /**
     * Scores the affinity of this helper with the source class.
     * 
     * @param source
     *            The source object to convert.
     * @param target
     *            The expected representation metadata.
     * @param resource
     *            The calling resource.
     * @return The affinity score of this helper.
     */
    public abstract float score(Object source, Variant target, Resource resource);

    /**
     * Scores the affinity of this helper with the source class.
     * 
     * @param source
     *            The source representation to convert.
     * @param target
     *            The expected class of the Java object.
     * @param resource
     *            The calling resource.
     * @return The affinity score of this helper.
     */
    public abstract <T> float score(Representation source, Class<T> target,
            Resource resource);

    /**
     * Converts a Representation into a regular Java object.
     * 
     * @param <T>
     *            The expected class of the Java object.
     * @param source
     *            The source representation to convert.
     * @param target
     *            The expected class of the Java object.
     * @param resource
     *            The calling resource.
     * @return The converted Java object.
     */
    public abstract <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException;

    /**
     * Converts a regular Java object into a Representation.
     * 
     * @param source
     *            The source object to convert.
     * @param target
     *            The expected representation metadata.
     * @param resource
     *            The calling resource.
     * @return The converted representation.
     */
    public abstract Representation toRepresentation(Object source,
            Variant target, Resource resource) throws IOException;

    /**
     * Updates the preferences of the given {@link ClientInfo} object with
     * conversion capabilities for the given entity class.
     * 
     * @param preferences
     *            The media type preferences.
     * @param entity
     *            The entity class to convert.
     */
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        // Does nothing by default
    }

    /**
     * Updates the preferences of the given {@link ClientInfo} object with
     * conversion capabilities for the given entity class.
     * 
     * @param preferences
     *            The media type preferences.
     * @param mediaType
     *            The media type to update to add to the preferences.
     * @param score
     *            The media type score to use as a quality score.
     */
    public void updatePreferences(List<Preference<MediaType>> preferences,
            MediaType mediaType, float score) {
        boolean found = false;
        Preference<MediaType> preference;

        for (int i = 0; !found && (i < preferences.size()); i++) {
            preference = preferences.get(i);

            if (preference.getMetadata().equals(mediaType)
                    && (preference.getQuality() < score)) {
                preference.setQuality(score);
                found = true;
            }
        }

        if (!found) {
            preferences.add(new Preference<MediaType>(mediaType, score));
        }
    }
}
