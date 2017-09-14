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

package org.restlet.ext.jaxrs.internal.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;
import org.json.JSONTokener;
import org.restlet.Request;
import org.restlet.data.CharacterSet;
import org.restlet.engine.io.IoUtils;
import org.restlet.representation.Representation;

/**
 * This Provider serializes all Objects by the package org.json. It can
 * deserialize to {@link JSONObject}, {@link JSONArray} and {@link JSONString}.
 * 
 * @author Stephan Koops
 */
@Provider
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class JsonProvider extends AbstractProvider<Object> {

    // NICE better JSON support planned for later.

    /**
     * @return the character set of the current entity, or null, if no entity or
     *         no character set is available.
     */
    private CharacterSet getCurrentRequestEntityCharacterSet() {
        Representation entity = Request.getCurrent().getEntity();

        if (entity == null)
            return null;

        return entity.getCharacterSet();
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    @Override
    public long getSize(Object t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    /**
     * @see MessageBodyReader#isReadable(Class, Type, Annotation[])
     */
    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        if (JSONObject.class.isAssignableFrom(type)) {
            return true;
        }
        if (JSONArray.class.isAssignableFrom(type)) {
            return true;
        }
        if (JSONString.class.isAssignableFrom(type)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    /**
     * @see MessageBodyReader#readFrom(Class, Type, MediaType, Annotation[],
     *      MultivaluedMap, InputStream)
     */
    @Override
    public Object readFrom(Class<Object> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        final String jsonString;
        jsonString = IoUtils.toString(entityStream,
                getCurrentRequestEntityCharacterSet());
        try {
            if (JSONObject.class.isAssignableFrom(type)) {
                return new JSONObject(jsonString);
            }
            if (JSONArray.class.isAssignableFrom(type)) {
                return new JSONArray(jsonString);
            }
        } catch (JSONException e) {
            final IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
        if (JSONString.class.isAssignableFrom(type)) {
            return new JSONString() {
                public String toJSONString() {
                    return jsonString;
                }

                @Override
                public String toString() {
                    return jsonString;
                }
            };
        }
        throw new IllegalArgumentException("the given type " + type
                + " is not supported");
    }

    /**
     * @see MessageBodyWriter#writeTo(Object, Class, Type, Annotation[],
     *      MediaType, MultivaluedMap, OutputStream)
     */
    @Override
    public void writeTo(Object object, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            final OutputStreamWriter writer = new OutputStreamWriter(
                    entityStream);
            if (object instanceof JSONString) {
                writer.write(((JSONString) object).toJSONString());
            } else if (object instanceof JSONArray) {
                writer.write(((JSONArray) object).toString());
            } else if (object instanceof CharSequence) {
                writer.write(object.toString());
            } else {
                JSONObject jsonObject;
                if (object instanceof JSONObject) {
                    jsonObject = (JSONObject) object;
                } else if (object instanceof JSONTokener) {
                    jsonObject = new JSONObject((JSONTokener) object);
                } else if (object instanceof Map<?, ?>) {
                    jsonObject = new JSONObject((Map<?, ?>) object);
                } else {
                    jsonObject = new JSONObject(object);
                }
                jsonObject.write(writer);
            }
            writer.flush();
        } catch (JSONException e) {
            final IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }
}
