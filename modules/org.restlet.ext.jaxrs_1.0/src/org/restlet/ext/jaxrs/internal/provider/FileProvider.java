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
package org.restlet.ext.jaxrs.internal.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * This Provider reads or writes {@link File}s.
 * 
 * @author Stephan Koops
 */
@Provider
public class FileProvider extends AbstractProvider<File> {

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    @Override
    public long getSize(File t) {
        return -1;
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyReader#readFrom(java.lang.Class,
     *      javax.ws.rs.core.MediaType, javax.ws.rs.core.MultivaluedMap,
     *      java.io.InputStream)
     */
    @Override
    public File readFrom(Class<File> type, Type genericType,
            MediaType mediaType, Annotation[] annotations,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        try {
            File file = File.createTempFile("FileProvider", ".tmp");
            Util.copyStream(entityStream, new FileOutputStream(file));
            return file;
        } finally {
            entityStream.close();
        }
    }

    @Override
    protected Class<?> supportedClass() {
        return File.class;
    }

    @Override
    public void writeTo(File file, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        InputStream inputStream = new FileInputStream(file);
        copyAndCloseStream(inputStream, entityStream);
    }
}