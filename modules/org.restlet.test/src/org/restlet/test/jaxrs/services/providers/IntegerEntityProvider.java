/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.services.providers;

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

import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * @author Stephan Koops
 */
@Provider
public class IntegerEntityProvider implements MessageBodyReader<Integer>,
        MessageBodyWriter<Integer> {

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    public long getSize(Integer t) {
        return t.toString().length();
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyReader#isReadable(java.lang.Class,
     *      java.lang.reflect.Type, java.lang.annotation.Annotation[])
     */
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return type.equals(Integer.class);
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(java.lang.Class,
     *      java.lang.reflect.Type, java.lang.annotation.Annotation[])
     */
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return type.equals(Integer.class);
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class,
     *      java.lang.reflect.Type, java.lang.annotation.Annotation[],
     *      javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap,
     *      java.io.InputStream)
     */
    public Integer readFrom(Class<Integer> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        final String str = Util.copyToStringBuilder(entityStream).toString();
        if (str.length() == 0) {
            return null;
        }
        return new Integer(str);
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(java.lang.Object,
     *      java.lang.Class, java.lang.reflect.Type,
     *      java.lang.annotation.Annotation[], javax.ws.rs.core.MediaType,
     *      javax.ws.rs.core.MultivaluedMap, java.io.OutputStream)
     */
    public void writeTo(Integer integer, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        if (integer != null) {
            entityStream.write(integer.toString().getBytes());
        }
    }
}