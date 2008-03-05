/*
 * Copyright 2005-2008 Noelios Consulting.
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
package org.restlet.ext.jaxrs.provider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 * This Provider is used to read directly from an InputStream
 * 
 * @author Stephan Koops
 */
@Provider
public class InputStreamProvider extends AbstractProvider<InputStream> {

    // TODO implement ReaderProvider
    
    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    @Override
    public long getSize(InputStream t) {
        return -1;
    }

    @Override
    protected boolean isReadableAndWriteable(Class<?> type, Type genericType, Annotation[] annotations) {
        return InputStream.class.isAssignableFrom(type);
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class,
     *      javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap,
     *      java.io.InputStream)
     */
    @Override
    public InputStream readFrom(Class<InputStream> type, Type genericType,
            MediaType mediaType, Annotation[] annotations, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        return entityStream;
    }

    @Override
    public void writeTo(InputStream inputStream, Type genericType,
            Annotation[] annotations,
            MediaType mediaType, 
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
            copyAndCloseStream(inputStream, entityStream);
    }
}