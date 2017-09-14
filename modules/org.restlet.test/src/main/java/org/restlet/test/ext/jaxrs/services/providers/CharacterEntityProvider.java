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

package org.restlet.test.ext.jaxrs.services.providers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.restlet.engine.io.IoUtils;

/**
 * @author Stephan Koops
 */
@Provider
public class CharacterEntityProvider implements MessageBodyReader<Character>,
        MessageBodyWriter<Character> {

    /**
     * @see MessageBodyWriter#getSize(Object, Class, Type, Annotation[],
     *      MediaType)
     */
    public long getSize(Character c, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        if (c == null) {
            return 0;
        }
        return 1;
    }

    /**
     * @see MessageBodyReader#isReadable(Class, Type, Annotation[], MediaType)
     */
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return type.equals(Character.class);
    }

    /**
     * @see MessageBodyWriter#isWriteable(Class, Type, Annotation[], MediaType)
     */
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return type.equals(Character.class);
    }

    /**
     * @see MessageBodyReader#readFrom(Class, Type, Annotation[], MediaType,
     *      MultivaluedMap, InputStream)
     */
    public Character readFrom(Class<Character> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        String str = IoUtils.toString(entityStream);

        if (str.length() == 0) {
            return null;
        }

        return str.charAt(0);
    }

    /**
     * @see MessageBodyWriter#writeTo(Object, Class, Type, Annotation[],
     *      MediaType, MultivaluedMap, OutputStream)
     */
    public void writeTo(Character c, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        if (c != null) {
            entityStream.write(c.toString().getBytes());
        }
    }
}
