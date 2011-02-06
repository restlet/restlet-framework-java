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

package org.restlet.ext.xstream;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

/**
 * Converter between the XML/JSON and Representation classes based on XStream.
 * 
 * @author Jerome Louvel
 */
public class XstreamConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_APPLICATION_ALL_XML = new VariantInfo(
            MediaType.APPLICATION_ALL_XML);

    private static final VariantInfo VARIANT_APPLICATION_XML = new VariantInfo(
            MediaType.APPLICATION_XML);

    private static final VariantInfo VARIANT_JSON = new VariantInfo(
            MediaType.APPLICATION_JSON);

    private static final VariantInfo VARIANT_TEXT_XML = new VariantInfo(
            MediaType.TEXT_XML);

    /**
     * Creates the marshaling {@link XstreamRepresentation}.
     * 
     * @param <T>
     * @param mediaType
     *            The target media type.
     * @param source
     *            The source object to marshal.
     * @return The marshaling {@link XstreamRepresentation}.
     */
    protected <T> XstreamRepresentation<T> create(MediaType mediaType, T source) {
        return new XstreamRepresentation<T>(mediaType, source);
    }

    /**
     * Creates the unmarshaling {@link XstreamRepresentation}.
     * 
     * @param <T>
     * @param source
     *            The source representation to unmarshal.
     * @return The unmarshaling {@link XstreamRepresentation}.
     */
    protected <T> XstreamRepresentation<T> create(Representation source) {
        return new XstreamRepresentation<T>(source);
    }

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_JSON.isCompatible(source)
                || VARIANT_APPLICATION_ALL_XML.isCompatible(source)
                || VARIANT_APPLICATION_XML.isCompatible(source)
                || VARIANT_TEXT_XML.isCompatible(source)) {
            result = addObjectClass(result, Object.class);
            result = addObjectClass(result, XstreamRepresentation.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (source != null) {
            result = addVariant(result, VARIANT_JSON);
            result = addVariant(result, VARIANT_APPLICATION_ALL_XML);
            result = addVariant(result, VARIANT_APPLICATION_XML);
            result = addVariant(result, VARIANT_TEXT_XML);
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        float result = -1.0F;

        if (source instanceof XstreamRepresentation<?>) {
            result = 1.0F;
        } else {
            if (target == null) {
                result = 0.5F;
            } else if (VARIANT_JSON.isCompatible(target)) {
                result = 0.8F;
            } else if (VARIANT_APPLICATION_ALL_XML.isCompatible(target)
                    || VARIANT_APPLICATION_XML.isCompatible(target)
                    || VARIANT_TEXT_XML.isCompatible(target)) {
                result = 0.8F;
            } else {
                result = 0.5F;
            }
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            UniformResource resource) {
        float result = -1.0F;

        if (target != null) {
            if (source instanceof XstreamRepresentation<?>) {
                result = 1.0F;
            } else if (XstreamRepresentation.class.isAssignableFrom(target)) {
                result = 1.0F;
            } else if (VARIANT_JSON.isCompatible(source)) {
                result = 0.8F;
            } else if (VARIANT_APPLICATION_ALL_XML.isCompatible(source)
                    || VARIANT_APPLICATION_XML.isCompatible(source)
                    || VARIANT_TEXT_XML.isCompatible(source)) {
                result = 0.8F;
            }
        } else {
            result = 0.5F;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target,
            UniformResource resource) throws IOException {
        Object result = null;

        // The source for the XStream conversion
        XstreamRepresentation<?> xstreamSource = null;

        if (source instanceof XstreamRepresentation) {
            xstreamSource = (XstreamRepresentation<?>) source;
        } else if (VARIANT_JSON.isCompatible(source)) {
            xstreamSource = create(source);
        } else if (VARIANT_APPLICATION_ALL_XML.isCompatible(source)
                || VARIANT_APPLICATION_XML.isCompatible(source)
                || VARIANT_TEXT_XML.isCompatible(source)) {
            xstreamSource = create(source);
        }

        if (xstreamSource != null) {
            // Handle the conversion
            if ((target != null)
                    && XstreamRepresentation.class.isAssignableFrom(target)) {
                result = xstreamSource;
            } else {
                if (target != null) {
                    // XStream 1.3.1 does not process annotations when called
                    // using fromXML(InputStream) despite autoProcessAnnotations
                    // being set to "true". This call forces the processing.
                    xstreamSource.getXstream().processAnnotations(target);
                }

                result = xstreamSource.getObject();
            }
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            UniformResource resource) {
        Representation result = null;

        if (source instanceof XstreamRepresentation) {
            result = (XstreamRepresentation<?>) source;
        } else {
            if (target.getMediaType() == null) {
                target.setMediaType(MediaType.TEXT_XML);
            }

            if (VARIANT_JSON.isCompatible(target)) {
                XstreamRepresentation<Object> xstreamRepresentation = create(
                        target.getMediaType(), source);
                result = xstreamRepresentation;
            } else if (VARIANT_APPLICATION_ALL_XML.isCompatible(target)
                    || VARIANT_APPLICATION_XML.isCompatible(target)
                    || VARIANT_TEXT_XML.isCompatible(target)) {
                result = create(target.getMediaType(), source);
            }
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        updatePreferences(preferences, MediaType.APPLICATION_ALL_XML, 1.0F);
        updatePreferences(preferences, MediaType.APPLICATION_JSON, 1.0F);
        updatePreferences(preferences, MediaType.APPLICATION_XML, 1.0F);
        updatePreferences(preferences, MediaType.TEXT_XML, 1.0F);
    }

}
