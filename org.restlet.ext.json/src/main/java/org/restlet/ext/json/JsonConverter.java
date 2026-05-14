/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.json;

import java.io.IOException;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

/**
 * Converter between the JSON API (more precisely {@link JSONArray}, {@link JSONObject} and {@link
 * JSONTokener} instances) and Representation classes.
 *
 * @author Jerome Louvel
 */
public class JsonConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_JSON = new VariantInfo(MediaType.APPLICATION_JSON);

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_JSON.isCompatible(source)) {
            result = addObjectClass(result, JSONArray.class);
            result = addObjectClass(result, JSONObject.class);
            result = addObjectClass(result, JSONTokener.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (JSONArray.class.isAssignableFrom(source)
                || JSONObject.class.isAssignableFrom(source)
                || JSONTokener.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_JSON);
        }

        if (result == null) {
            result = List.of();
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        float result = -1.0F;

        if ((source instanceof JSONArray)
                || (source instanceof JSONObject)
                || (source instanceof JSONTokener)) {
            if (target == null) {
                result = 0.5F;
            } else if (MediaType.APPLICATION_JSON.isCompatible(target.getMediaType())) {
                result = 1.0F;
            } else {
                result = 0.5F;
            }
        }

        return result;
    }

    @Override
    public <T> float score(Representation source, Class<T> target, Resource resource) {
        if (target == null) {
            return -1.0F;
        }

        final float result;
        if (JsonRepresentation.class.isAssignableFrom(target)) {
            result = 1.0F;
        } else if (JSONArray.class.isAssignableFrom(target)) {
            if (MediaType.APPLICATION_JSON.isCompatible(source.getMediaType())) {
                result = 1.0F;
            } else {
                result = 0.5F;
            }
        } else if (JSONObject.class.isAssignableFrom(target)) {
            if (MediaType.APPLICATION_JSON.isCompatible(source.getMediaType())) {
                result = 1.0F;
            } else {
                result = 0.5F;
            }
        } else if (JSONTokener.class.isAssignableFrom(target)) {
            if (MediaType.APPLICATION_JSON.isCompatible(source.getMediaType())) {
                result = 1.0F;
            } else {
                result = 0.5F;
            }
        } else {
            result = -1.0F;
        }

        return result;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target, Resource resource)
            throws IOException {
        if (target == null) {
            return null;
        }

        final JsonRepresentation jsonSource;
        if (source instanceof JsonRepresentation jsonRepresentation) {
            jsonSource = jsonRepresentation;
        } else {
            jsonSource = new JsonRepresentation(source);
        }

        T result = null;
        if (JSONArray.class.isAssignableFrom(target)) {
            try {
                result = target.cast(jsonSource.getJsonArray());
            } catch (JSONException e) {
                throw new IOException("Unable to convert to JSON array", e);
            }
        } else if (JSONObject.class.isAssignableFrom(target)) {
            try {
                result = target.cast(jsonSource.getJsonObject());
            } catch (JSONException e) {
                throw new IOException("Unable to convert to JSON object", e);
            }
        } else if (JSONTokener.class.isAssignableFrom(target)) {
            try {
                result = target.cast(jsonSource.getJsonTokener());
            } catch (JSONException e) {
                throw new IOException("Unable to convert to JSON tokener", e);
            }
        } else if (JsonRepresentation.class.isAssignableFrom(target)) {
            result = target.cast(jsonSource);
        }

        return result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target, Resource resource) {
        Representation result = null;

        if (source instanceof JSONArray jsonArray) {
            result = new JsonRepresentation(jsonArray);
        } else if (source instanceof JSONObject jsonObject) {
            result = new JsonRepresentation(jsonObject);
        } else if (source instanceof JSONTokener jsonTokener) {
            result = new JsonRepresentation(jsonTokener);
        }

        return result;
    }

    @Override
    public <T> void updatePreferences(List<Preference<MediaType>> preferences, Class<T> entity) {
        if (JSONArray.class.isAssignableFrom(entity)
                || JSONObject.class.isAssignableFrom(entity)
                || JSONTokener.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.APPLICATION_JSON, 1.0F);
        }
    }
}
