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
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

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

        if (JSONArray.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_JSON);
        } else if (JSONObject.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_JSON);
        } else if (JSONTokener.class.isAssignableFrom(source)) {
            result = addVariant(result, VARIANT_JSON);
        }

        return result;
    }

    @Override
    public float score(Object source, Variant target, UniformResource resource) {
        float result = -1.0F;

        if ((source instanceof JSONArray) || (source instanceof JSONObject)
                || (source instanceof JSONTokener)) {
            if (target == null) {
                result = 0.5F;
            } else if (MediaType.APPLICATION_JSON.isCompatible(target
                    .getMediaType())) {
                result = 1.0F;
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
            if (JsonRepresentation.class.isAssignableFrom(target)) {
                result = 1.0F;
            } else if (JSONArray.class.isAssignableFrom(target)) {
                if (MediaType.APPLICATION_JSON.isCompatible(source
                        .getMediaType())) {
                    result = 1.0F;
                } else {
                    result = 0.5F;
                }
            } else if (JSONObject.class.isAssignableFrom(target)) {
                if (MediaType.APPLICATION_JSON.isCompatible(source
                        .getMediaType())) {
                    result = 1.0F;
                } else {
                    result = 0.5F;
                }
            } else if (JSONTokener.class.isAssignableFrom(target)) {
                if (MediaType.APPLICATION_JSON.isCompatible(source
                        .getMediaType())) {
                    result = 1.0F;
                } else {
                    result = 0.5F;
                }
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation source, Class<T> target,
            UniformResource resource) throws IOException {
        Object result = null;

        if (JSONArray.class.isAssignableFrom(target)) {
            try {
                result = new JSONArray(source.getText());
            } catch (JSONException e) {
                IOException ioe = new IOException(
                        "Unable to convert to JSON array");
                ioe.initCause(e);
            }
        } else if (JSONObject.class.isAssignableFrom(target)) {
            try {
                result = new JSONObject(source.getText());
            } catch (JSONException e) {
                IOException ioe = new IOException(
                        "Unable to convert to JSON object");
                ioe.initCause(e);
            }
        } else if (JSONTokener.class.isAssignableFrom(target)) {
            result = new JSONTokener(source.getText());
        } else if (JsonRepresentation.class.isAssignableFrom(target)) {
            result = new JsonRepresentation(source);
        }

        return (T) result;
    }

    @Override
    public Representation toRepresentation(Object source, Variant target,
            UniformResource resource) {
        Representation result = null;

        if (source instanceof JSONArray) {
            result = new StringRepresentation(((JSONArray) source).toString());
        } else if (source instanceof JSONObject) {
            result = new StringRepresentation(((JSONObject) source).toString());
        } else if (source instanceof JSONTokener) {
            result = new StringRepresentation(((JSONTokener) source).toString());
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
