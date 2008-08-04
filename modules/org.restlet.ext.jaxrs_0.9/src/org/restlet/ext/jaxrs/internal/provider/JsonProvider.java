/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
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
import org.restlet.ext.jaxrs.internal.util.Util;

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
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    @Override
    public long getSize(Object t) {
        return -1;
    }

    /**
     * @see MessageBodyReader#isReadable(Class, Type, Annotation[])
     */
    @Override
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations) {
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
    /*
     * @see MessageBodyWriter#isWriteable(Class, Type, Annotation[])
     */
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
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
        jsonString = Util.copyToStringBuilder(entityStream).toString();
        try {
            if (JSONObject.class.isAssignableFrom(type)) {
                return new JSONObject(jsonString);
            }
            if (JSONArray.class.isAssignableFrom(type)) {
                return new JSONArray(jsonString);
            }
        } catch (final JSONException e) {
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
                } else if (object instanceof Map) {
                    jsonObject = new JSONObject((Map<?, ?>) object);
                } else {
                    jsonObject = new JSONObject(object);
                }
                jsonObject.write(writer);
            }
            writer.flush();
        } catch (final JSONException e) {
            final IOException ioe = new IOException(e.getMessage());
            ioe.initCause(e);
            throw ioe;
        }
    }
}