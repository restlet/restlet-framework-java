/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jibx;

import java.io.IOException;
import java.util.List;

import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.JiBXException;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * A JiBX converter helper to convert from JiBX objects to JibxRepresentation
 * and vice versa. It only supports objects that are not bound several times
 * using several binding names.
 * 
 * @author Thierry Boileau
 */
public class JibxConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_APPLICATION_ALL_XML = new VariantInfo(
            MediaType.APPLICATION_ALL_XML);

    private static final VariantInfo VARIANT_APPLICATION_XML = new VariantInfo(
            MediaType.APPLICATION_XML);

    private static final VariantInfo VARIANT_TEXT_XML = new VariantInfo(
            MediaType.TEXT_XML);

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_APPLICATION_ALL_XML.isCompatible(source)
                || VARIANT_APPLICATION_XML.isCompatible(source)
                || VARIANT_TEXT_XML.isCompatible(source)) {
            result = addObjectClass(result, JibxRepresentation.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (isJibxBoundClass(source)
                || JibxRepresentation.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_APPLICATION_ALL_XML);
            result = addVariant(result, VARIANT_APPLICATION_XML);
            result = addVariant(result, VARIANT_TEXT_XML);
        }

        return result;
    }

    /**
     * Indicates if the class is bound by a Jibx factory.
     * 
     * @param source
     *            The class to test.
     * @return True if the class is bound by a Jibx factory.
     */
    private boolean isJibxBoundClass(Class<?> source) {
        boolean result = false;

        try {
            if ((source != null)
                    && (BindingDirectory.getFactory(source) != null)) {
                result = true;
            }
        } catch (JiBXException e) {
            // This may be caused by the fact that the source class is bound
            // several times which requires the knowledge of the binding name.
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        float result = -1.0F;

        if (source != null
                && (source instanceof JibxRepresentation<?> || isJibxBoundClass(source
                        .getClass()))) {
            if (target == null) {
                result = 0.5F;
            } else if (MediaType.APPLICATION_ALL_XML.isCompatible(target
                    .getMediaType())) {
                result = 1.0F;
            } else if (MediaType.APPLICATION_XML.isCompatible(target
                    .getMediaType())) {
                result = 1.0F;
            } else if (MediaType.TEXT_XML.isCompatible(target.getMediaType())) {
                result = 1.0F;
            } else {
                // Allow for JiBX object to be used for JSON and other
                // representations
                result = 0.5F;
            }
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target,
            Resource resource) {
        float result = -1.0F;

        if (source != null) {
            if (source instanceof JibxRepresentation<?>) {
                result = 1.0F;
            } else if (JibxRepresentation.class.isAssignableFrom(target)) {
                result = 1.0F;
            } else if (isJibxBoundClass(target)
                    || JibxRepresentation.class.isAssignableFrom(source
                            .getClass())) {
                result = 1.0F;
            }
        }

        return result;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        Object result = null;

        if (JibxRepresentation.class.isAssignableFrom(target)) {
            result = new JibxRepresentation<T>(source, target);
        } else if (isJibxBoundClass(target)) {
            try {
                result = new JibxRepresentation<T>(source, target).getObject();
            } catch (JiBXException e) {
                throw new IOException(
                        "Cannot convert the given representation to an object of this class using Jibx converter "
                                + target + " due to " + e.getMessage());
            }
        } else if (target == null) {
            if (source instanceof JibxRepresentation<?>) {
                try {
                    result = ((JibxRepresentation<?>) source).getObject();
                } catch (JiBXException e) {
                    throw new IOException(
                            "Cannot retrieve the wrapped object inside the JiBX representation due to "
                                    + e.getMessage());
                }
            }
        }

        return target.cast(result);
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) {
        Representation result = null;

        if (isJibxBoundClass(source.getClass())) {
            result = new JibxRepresentation<Object>(target.getMediaType(),
                    source);
        } else if (source instanceof JibxRepresentation<?>) {
            result = (Representation) source;
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        if (isJibxBoundClass(entity)
                || JibxRepresentation.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.APPLICATION_ALL_XML, 1.0F);
            updatePreferences(preferences, MediaType.APPLICATION_XML, 1.0F);
            updatePreferences(preferences, MediaType.TEXT_XML, 1.0F);
        }
    }

}
