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
import org.restlet.resource.UniformResource;

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
    public abstract List<VariantInfo> getVariants(Class<?> source);

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
    public abstract float score(Object source, Variant target,
            UniformResource resource);

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
            UniformResource resource);

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
            UniformResource resource) throws IOException;

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
            Variant target, UniformResource resource) throws IOException;

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
