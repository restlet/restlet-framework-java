/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
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
 * Converter between the JSON API (more precisely {@link JSONArray},
 * {@link JSONObject} and {@link JSONTokener} instances) and Representation
 * classes.
 * 
 * @author Jerome Louvel
 */
public class JsonConverter extends ConverterHelper {

    private static final VariantInfo VARIANT_JSON = new VariantInfo(
            MediaType.APPLICATION_JSON);

    /**
     * Variant with media type application/*+json, such as
     * application/vnd.example+json.
     */
    private static final VariantInfo VARIANT_ALL_JSON = new VariantInfo(
            MediaType.APPLICATION_ALL_JSON);

    /**
     * Indicates if the given media type is JSON, including the
     * application/*+json media types.
     * 
     * @param mediaType
     *            The media type to test.
     * @return True if the media type is compatible with JSON.
     */
    private static boolean isJson(MediaType mediaType) {
        return MediaType.APPLICATION_JSON.isCompatible(mediaType)
                || MediaType.APPLICATION_ALL_JSON.isCompatible(mediaType);
    }

    /**
     * Indicates if the given class is one of the JSON API classes handled by
     * this converter ({@link JSONArray}, {@link JSONObject} or
     * {@link JSONTokener}).
     * 
     * @param clazz
     *            The class to test.
     * @return True if the class is a JSON API class.
     */
    private static boolean isJsonApiClass(Class<?> clazz) {
        return JSONArray.class.isAssignableFrom(clazz)
                || JSONObject.class.isAssignableFrom(clazz)
                || JSONTokener.class.isAssignableFrom(clazz);
    }

    @Override
    public List<Class<?>> getObjectClasses(Variant source) {
        List<Class<?>> result = null;

        if (VARIANT_JSON.isCompatible(source)
                || VARIANT_ALL_JSON.isCompatible(source)) {
            result = addObjectClass(result, JSONArray.class);
            result = addObjectClass(result, JSONObject.class);
            result = addObjectClass(result, JSONTokener.class);
        }

        return result;
    }

    @Override
    public List<VariantInfo> getVariants(Class<?> source) {
        List<VariantInfo> result = null;

        if (isJsonApiClass(source)) {
            result = addVariant(result, VARIANT_JSON);
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, Resource resource) {
        float result = -1.0F;

        if ((source instanceof JSONArray) || (source instanceof JSONObject)
                || (source instanceof JSONTokener)) {
            if (target == null) {
                result = 0.5F;
            } else if (isJson(target.getMediaType())) {
                result = 1.0F;
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

        if (target != null) {
            if (JsonRepresentation.class.isAssignableFrom(target)) {
                result = 1.0F;
            } else if (isJsonApiClass(target)) {
                result = isJson(source.getMediaType()) ? 1.0F : 0.5F;
            }
        }

        return result;
    }

    @Override
    public <T> T toObject(Representation source, Class<T> target,
            Resource resource) throws IOException {
        JsonRepresentation jsonSource = (source instanceof JsonRepresentation jsonRepresentation)
                ? jsonRepresentation
                : new JsonRepresentation(source);

        T result = null;
        if (target != null) {
            if (JSONArray.class.isAssignableFrom(target)) {
                result = target.cast(getJsonArray(jsonSource));
            } else if (JSONObject.class.isAssignableFrom(target)) {
                result = target.cast(getJsonObject(jsonSource));
            } else if (JSONTokener.class.isAssignableFrom(target)) {
                result = target.cast(getJsonTokener(jsonSource));
            } else if (JsonRepresentation.class.isAssignableFrom(target)) {
                result = target.cast(jsonSource);
            }
        }

        return result;
    }

    private static JSONArray getJsonArray(JsonRepresentation source)
            throws IOException {
        try {
            return source.getJsonArray();
        } catch (JSONException e) {
            throw conversionError("JSON array", e);
        }
    }

    private static JSONObject getJsonObject(JsonRepresentation source)
            throws IOException {
        try {
            return source.getJsonObject();
        } catch (JSONException e) {
            throw conversionError("JSON object", e);
        }
    }

    private static JSONTokener getJsonTokener(JsonRepresentation source)
            throws IOException {
        try {
            return source.getJsonTokener();
        } catch (JSONException e) {
            throw conversionError("JSON tokener", e);
        }
    }

    private static IOException conversionError(String target,
            JSONException cause) {
        IOException ioe = new IOException("Unable to convert to " + target);
        ioe.initCause(cause);
        return ioe;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            Resource resource) {
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
    public <T> void updatePreferences(List<Preference<MediaType>> preferences,
            Class<T> entity) {
        if (JSONArray.class.isAssignableFrom(entity)
                || JSONObject.class.isAssignableFrom(entity)
                || JSONTokener.class.isAssignableFrom(entity)) {
            updatePreferences(preferences, MediaType.APPLICATION_JSON, 1.0F);
        }
    }

}
