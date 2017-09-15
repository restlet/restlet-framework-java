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

package org.restlet.ext.jackson;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * Converter between the JSON, JSON Smile, CSV, XML, YAML and Representation
 * classes based on Jackson.
 * 
 * @author Jerome Louvel
 * @author Thierry Boileau
 */
public class JacksonConverter extends ConverterHelper {
    // [ifndef android] instruction
    /** Variant with media type application/xml. */
    private static final VariantInfo VARIANT_APPLICATION_XML = new VariantInfo(
            MediaType.APPLICATION_XML);

    /** Variant with media type application/yaml. */
    private static final VariantInfo VARIANT_APPLICATION_YAML = new VariantInfo(
            MediaType.APPLICATION_YAML);

    /** Variant with media type application/json. */
    private static final VariantInfo VARIANT_JSON = new VariantInfo(
            MediaType.APPLICATION_JSON);

    /** Variant with media type application/x-json-smile. */
    private static final VariantInfo VARIANT_JSON_SMILE = new VariantInfo(
            MediaType.APPLICATION_JSON_SMILE);

    /** Variant with media type text/csv. */
    private static final VariantInfo VARIANT_TEXT_CSV = new VariantInfo(
            MediaType.TEXT_CSV);

    // [ifndef android] instruction
    /** Variant with media type text/xml. */
    private static final VariantInfo VARIANT_TEXT_XML = new VariantInfo(
            MediaType.TEXT_XML);

    /** Variant with media type text/yaml. */
    private static final VariantInfo VARIANT_TEXT_YAML = new VariantInfo(
            MediaType.TEXT_YAML);

    /**
     * Creates the marshaling {@link JacksonRepresentation}.
     * 
     * @param <T>
     * @param mediaType
     *            The target media type.
     * @param source
     *            The source object to marshal.
     * @return The marshaling {@link JacksonRepresentation}.
     */
    protected <T> JacksonRepresentation<T> create(MediaType mediaType, T source) {
        return new JacksonRepresentation<T>(mediaType, source);
    }

    /**
     * Creates the unmarshaling {@link JacksonRepresentation}.
     * 
     * @param <T>
     * @param source
     *            The source representation to unmarshal.
     * @param objectClass
     *            The object class to instantiate.
     * @return The unmarshaling {@link JacksonRepresentation}.
     */
    protected <T> JacksonRepresentation<T> create(Representation source,
            Class<T> objectClass) {
        return new JacksonRepresentation<T>(source, objectClass);
    }

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (isCompatible(source)) {
            result = addObjectClass(result, Object.class);
            result = addObjectClass(result, JacksonRepresentation.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (source != null) {
            result = addVariant(result, VARIANT_JSON);
            result = addVariant(result, VARIANT_JSON_SMILE);
            // [ifndef android] instruction
            result = addVariant(result, VARIANT_APPLICATION_XML);
            // [ifndef android] instruction
            result = addVariant(result, VARIANT_TEXT_XML);
            result = addVariant(result, VARIANT_APPLICATION_YAML);
            result = addVariant(result, VARIANT_TEXT_YAML);
            result = addVariant(result, VARIANT_TEXT_CSV);
        }

        return result;
    }

    /**
     * Indicates if the given variant is compatible with the media types
     * supported by this converter.
     * 
     * @param variant
     *            The variant.
     * @return True if the given variant is compatible with the media types
     *         supported by this converter.
     */
    protected boolean isCompatible(Variant variant) {
        return (variant != null)
                && (VARIANT_JSON.isCompatible(variant)
                        || VARIANT_JSON_SMILE.isCompatible(variant)
                        // [ifndef android] line
                        || VARIANT_APPLICATION_XML.isCompatible(variant)
                        // [ifndef android] line
                        || VARIANT_TEXT_XML.isCompatible(variant)
                        || VARIANT_APPLICATION_YAML.isCompatible(variant)
                        || VARIANT_TEXT_YAML.isCompatible(variant) || VARIANT_TEXT_CSV
                            .isCompatible(variant));
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        float result = -1.0F;

        if (source instanceof JacksonRepresentation<?>) {
            result = 1.0F;
        } else {
            if (target == null) {
                result = 0.5F;
            } else if (isCompatible(target)) {
                result = 0.8F;
            } else {
                result = 0.5F;
            }
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            Resource resource) {
        float result = -1.0F;

        if (source instanceof JacksonRepresentation<?>) {
            result = 1.0F;
        } else if ((target != null)
                && JacksonRepresentation.class.isAssignableFrom(target)) {
            result = 1.0F;
        } else if (isCompatible(source)) {
            result = 0.8F;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        Object result = null;

        // The source for the Jackson conversion
        JacksonRepresentation<?> jacksonSource = null;
        if (source instanceof JacksonRepresentation) {
            jacksonSource = (JacksonRepresentation<?>) source;
        } else if (isCompatible(source)) {
            jacksonSource = create(source, target);
        }

        if (jacksonSource != null) {
            // Handle the conversion
            if ((target != null)
                    && JacksonRepresentation.class.isAssignableFrom(target)) {
                result = jacksonSource;
            } else {
                result = jacksonSource.getObject();
            }
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) {
        Representation result = null;

        if (source instanceof JacksonRepresentation) {
            result = (JacksonRepresentation<?>) source;
        } else {
            if (target.getMediaType() == null) {
                target.setMediaType(MediaType.APPLICATION_JSON);
            }
            if (isCompatible(target)) {
                result = create(target.getMediaType(), source);
            }
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        updatePreferences(preferences, MediaType.APPLICATION_JSON, 1.0F);
        updatePreferences(preferences, MediaType.APPLICATION_JSON_SMILE, 1.0F);
        // [ifndef android] instruction
        updatePreferences(preferences, MediaType.APPLICATION_XML, 1.0F);
        // [ifndef android] instruction
        updatePreferences(preferences, MediaType.TEXT_XML, 1.0F);
        updatePreferences(preferences, MediaType.APPLICATION_YAML, 1.0F);
        updatePreferences(preferences, MediaType.TEXT_YAML, 1.0F);
        updatePreferences(preferences, MediaType.TEXT_CSV, 1.0F);
    }

}
