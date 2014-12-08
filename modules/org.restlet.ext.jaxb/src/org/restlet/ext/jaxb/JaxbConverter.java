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

package org.restlet.ext.jaxb;

import java.io.IOException;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * A JAXB Converter Helper to convert from JAXB objects to JaxbRepresentations
 * and vice versa.
 * 
 * @author Sanjay Acharya
 */
public class JaxbConverter extends ConverterHelper {
    private static final VariantInfo VARIANT_APPLICATION_ALL_XML = new VariantInfo(
            MediaType.APPLICATION_ALL_XML);

    private static final VariantInfo VARIANT_APPLICATION_XML = new VariantInfo(
            MediaType.APPLICATION_XML);

    private static final VariantInfo VARIANT_TEXT_XML = new VariantInfo(
            MediaType.TEXT_XML);

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (isCompatible(source)) {
            result = addObjectClass(result, JaxbRepresentation.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (isJaxbRootElementClass(source)
                || JaxbRepresentation.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_APPLICATION_ALL_XML);
            result = addVariant(result, VARIANT_APPLICATION_XML);
            result = addVariant(result, VARIANT_TEXT_XML);
        }

        return result;
    }

    /**
     * Indicates if the given mediaType is compatible with the media types
     * supported by this converter.
     * 
     * @param mediaType
     *            The mediaType.
     * @return True if the given mediaType is compatible with the media types
     *         supported by this converter.
     */
    protected boolean isCompatible(MediaType mediaType) {
        return (mediaType != null)
                && (MediaType.APPLICATION_ALL_XML.isCompatible(mediaType)
                        || MediaType.APPLICATION_XML.isCompatible(mediaType) || MediaType.TEXT_XML
                            .isCompatible(mediaType));
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
                && (VARIANT_APPLICATION_ALL_XML.isCompatible(variant)
                        || VARIANT_APPLICATION_XML.isCompatible(variant) || VARIANT_TEXT_XML
                            .isCompatible(variant));
    }

    /**
     * Indicates if the class has JAXB annotations.
     * 
     * @param source
     *            The class to test.
     * @return True if the class has JAXB annotations.
     */
    private boolean isJaxbRootElementClass(Class<?> source) {
        return source != null
                && source.isAnnotationPresent(XmlRootElement.class);
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        float result = -1.0F;

        if (source != null
                && (source instanceof JaxbRepresentation<?> || isJaxbRootElementClass(source
                        .getClass()))) {
            if (target == null) {
                result = 0.8F;
            } else if (isCompatible(target.getMediaType())) {
                result = 1.0F;
            } else {
                // Allow for JAXB object to be used for JSON and other
                // representations
                result = 0.7F;
            }
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            Resource resource) {
        float result = -1.0F;

        if (source != null) {
            if (source instanceof JaxbRepresentation<?>) {
                result = 1.0F;
            } else if (JaxbRepresentation.class.isAssignableFrom(source
                    .getClass())) {
                result = 1.0F;
            } else if (isJaxbRootElementClass(target)
                    || JaxbRepresentation.class.isAssignableFrom(target)) {
                if (isCompatible(source.getMediaType())) {
                    result = 1.0F;
                } else {
                    // Allow for JAXB object to be used for JSON and other
                    // representations
                    result = 0.7F;
                }
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        JaxbRepresentation<?> jaxbSource = null;
        if (source instanceof JaxbRepresentation) {
            jaxbSource = (JaxbRepresentation<?>) source;
        } else {
            jaxbSource = new JaxbRepresentation<T>(source, target);
        }

        T result = null;
        if (target == null) {
            result = (T) jaxbSource.getObject();
        } else if (JaxbRepresentation.class.isAssignableFrom(target)) {
            result = target.cast(jaxbSource);
        } else if (isJaxbRootElementClass(target)) {
            result = target.cast(jaxbSource.getObject());
        }

        return result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) {
        Representation result = null;

        if (isJaxbRootElementClass(source.getClass())) {
            result = new JaxbRepresentation<Object>(source);
        } else if (source instanceof JaxbRepresentation<?>) {
            result = (Representation) source;
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        if (JaxbRepresentation.class.isAssignableFrom(entity)
                || isJaxbRootElementClass(entity)) {
            updatePreferences(preferences, MediaType.APPLICATION_ALL_XML, 1.0F);
            updatePreferences(preferences, MediaType.APPLICATION_XML, 1.0F);
            updatePreferences(preferences, MediaType.TEXT_XML, 1.0F);
        }
    }
}
