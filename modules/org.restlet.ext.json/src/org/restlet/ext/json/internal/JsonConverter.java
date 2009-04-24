/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.ext.json.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.restlet.data.MediaType;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.UniformResource;

/**
 * Converter between the JSON API and Representation classes.
 * 
 * @author Jerome Louvel
 */
public class JsonConverter extends ConverterHelper {

    private static final Variant VARIANT_JSON = new Variant(
            MediaType.APPLICATION_JSON);

    @Override
    public List<Class<?>> getObjectClasses(Variant variant) {
        List<Class<?>> result = null;

        if (variant != null) {
            if (VARIANT_JSON.isCompatible(variant)) {
                if (result == null) {
                    result = new ArrayList<Class<?>>();
                }

                result.add(JSONArray.class);
                result.add(JSONObject.class);
                result.add(JSONTokener.class);
            }
        }

        return result;
    }

    @Override
    public List<Variant> getVariants(Class<?> objectClass, Variant targetVariant) {
        List<Variant> result = new ArrayList<Variant>();
        result.add(VARIANT_JSON);
        return result;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T toObject(Representation representation, Class<T> targetClass,
            UniformResource resource) throws IOException {
        T result = null;

        if (VARIANT_JSON.isCompatible(representation)) {
            try {
                if (JSONArray.class.isAssignableFrom(targetClass)) {
                    result = (T) new JSONArray(representation.getText());
                } else if (JSONObject.class.isAssignableFrom(targetClass)) {
                    result = (T) new JSONObject(representation.getText());
                } else if (JSONTokener.class.isAssignableFrom(targetClass)) {
                    result = (T) new JSONTokener(representation.getText());
                }
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        return result;
    }

    @Override
    public Representation toRepresentation(Object object,
            Variant targetVariant, UniformResource resource) {
        Representation result = null;

        return result;
    }
}
