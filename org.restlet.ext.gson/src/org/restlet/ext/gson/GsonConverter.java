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

package org.restlet.ext.gson;

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
 * Converter between the JSON and Representation classe based on Gson library.
 * 
 * @author Neal Mi
 */
public class GsonConverter extends ConverterHelper {

    /** Variant with media type application/json. */
    private static final VariantInfo VARIANT_JSON = new VariantInfo(
            MediaType.APPLICATION_JSON);

    /**
     * Creates the unmarshaling {@link GsonRepresentation}.
     * 
     * @param <T>
     * @param source
     *            The source representation to unmarshal.
     * @param objectClass
     *            The object class to instantiate.
     * @return The unmarshaling {@link GsonRepresentation}.
     */
    protected <T> GsonRepresentation<T> create(Representation source,
            Class<T> objectClass) {
        return new GsonRepresentation<T>(source, objectClass);
    }

    /**
     * Creates the marshaling {@link GsonRepresentation}.
     * 
     * @param <T>
     * @param source
     *            The source object to marshal.
     * @return The marshaling {@link GsonRepresentation}.
     */
    protected <T> GsonRepresentation<T> create(T source) {
        return new GsonRepresentation<T>(source);
    }

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_JSON.isCompatible(source)) {
            result = addObjectClass(result, Object.class);
            result = addObjectClass(result, GsonRepresentation.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (source != null) {
            result = addVariant(result, VARIANT_JSON);
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        float result = -1.0F;

        if (source instanceof GsonRepresentation<?>) {
            result = 1.0F;
        } else {
            if (target == null) {
                result = 0.5F;
            } else if (VARIANT_JSON.isCompatible(target)) {
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

        if (source instanceof GsonRepresentation<?>) {
            result = 1.0F;
        } else if ((target != null)
                && GsonRepresentation.class.isAssignableFrom(target)) {
            result = 1.0F;
        } else if (VARIANT_JSON.isCompatible(source)) {
            result = 0.8F;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        Object result = null;

        // The source for the gson conversion
        GsonRepresentation<?> gsonSource = null;

        if (source instanceof GsonRepresentation) {
            gsonSource = (GsonRepresentation<?>) source;
        } else if (VARIANT_JSON.isCompatible(source)) {
            gsonSource = create(source, target);
        }

        if (gsonSource != null) {
            // Handle the conversion
            if ((target != null)
                    && GsonRepresentation.class.isAssignableFrom(target)) {
                result = gsonSource;
            } else {
                result = gsonSource.getObject();
            }
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) throws IOException {
        Representation result = null;

        if (source instanceof GsonRepresentation) {
            result = (GsonRepresentation<?>) source;
        } else {
            if (target.getMediaType() == null) {
                target.setMediaType(MediaType.APPLICATION_JSON);
            }

            if (VARIANT_JSON.isCompatible(target)) {
                GsonRepresentation<Object> gsonRepresentation = create(source);
                result = gsonRepresentation;
            }
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        updatePreferences(preferences, MediaType.APPLICATION_JSON, 1.0F);
    }

}
