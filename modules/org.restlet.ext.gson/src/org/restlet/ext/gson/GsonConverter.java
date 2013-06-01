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
 * Inspired from Jackson extension of Restlet
 * 
 * @author nealmi 
 *  
 *         Copyright 2012 Neal Mi
 * 
 *         Licensed under the Apache License, Version 2.0 (the "License"); you
 *         may not use this file except in compliance with the License. You may
 *         obtain a copy of the License at
 * 
 *         http://www.apache.org/licenses/LICENSE-2.0
 * 
 *         Unless required by applicable law or agreed to in writing, software
 *         distributed under the License is distributed on an "AS IS" BASIS,
 *         WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *         implied. See the License for the specific language governing
 *         permissions and limitations under the License.
 */
public class GsonConverter extends ConverterHelper {
    private static final VariantInfo VARIANT_JSON = new VariantInfo(MediaType.APPLICATION_JSON);

    protected <T> GsonRepresentation<T> create(Representation source, Class<T> objectClass) {
        return new GsonRepresentation<T>(source, objectClass);
    }

    protected <T> GsonRepresentation<T> create(MediaType mediaType, T source) {
        return new GsonRepresentation<T>(mediaType, source);
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
    public <T> float score(Representation source, Class<T> target, Resource resource) {
        float result = -1.0F;

        if (source instanceof GsonRepresentation<?>) {
            result = 1.0F;
        } else if ((target != null) && GsonRepresentation.class.isAssignableFrom(target)) {
            result = 1.0F;
        } else if (VARIANT_JSON.isCompatible(source)) {
            result = 0.8F;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target, Resource resource) throws IOException {
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
            if ((target != null) && GsonRepresentation.class.isAssignableFrom(target)) {
                result = gsonSource;
            } else {
                result = gsonSource.getObject();
            }
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target, Resource resource) throws IOException {
        Representation result = null;

        if (source instanceof GsonRepresentation) {
            result = (GsonRepresentation<?>) source;
        } else {
            if (target.getMediaType() == null) {
                target.setMediaType(MediaType.APPLICATION_JSON);
            }

            if (VARIANT_JSON.isCompatible(target)) {
                GsonRepresentation<Object> gsonRepresentation = create(target.getMediaType(), source);
                result = gsonRepresentation;
            }
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences, Class<T> entity) {
        updatePreferences(preferences, MediaType.APPLICATION_JSON, 1.0F);
    }

}
