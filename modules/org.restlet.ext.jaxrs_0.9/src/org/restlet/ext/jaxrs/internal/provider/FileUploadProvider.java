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

import static javax.ws.rs.core.HttpHeaders.CONTENT_ENCODING;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.Provider;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Entity Provider, that reads "multipart/form-data" to a {@link List}&lt{@link FileItem}&gt;.
 * It is using the Apache Commons FileUpload (developed with version 1.2), which
 * must be available in the classpath, if you want to use this provider. For
 * more information see the <a
 * href="http://commons.apache.org/fileupload/">Apache FileUpload website</a>.<br>
 * This provider is not tested yet.
 * 
 * @author Stephan Koops
 * @see MultipartProvider
 */
@Provider
@ConsumeMime("multipart/form-data")
public class FileUploadProvider implements MessageBodyReader<List<FileItem>> {
    // NICE test FileUploadProvider

    private static final class RequestContext implements
            org.apache.commons.fileupload.RequestContext {

        private final InputStream entityStream;

        private final String contentEncoding;

        private final String contentType;

        private final int contentLength;

        /**
         * @param entityStream
         * @param respHeaders
         * @throws NumberFormatException
         *                 if the content length is not an int
         */
        private RequestContext(InputStream entityStream,
                MultivaluedMap<String, String> respHeaders)
                throws NumberFormatException {
            this.entityStream = entityStream;
            this.contentEncoding = respHeaders.getFirst(CONTENT_ENCODING);
            String contentLength = respHeaders.getFirst(CONTENT_LENGTH);
            this.contentLength = Integer.parseInt(contentLength);
            this.contentType = respHeaders.getFirst(CONTENT_TYPE);
        }

        public final String getCharacterEncoding() {
            return this.contentEncoding;
        }

        public final int getContentLength() {
            return this.contentLength;
        }

        public final String getContentType() {
            return this.contentType;
        }

        public final InputStream getInputStream() {
            return this.entityStream;
        }
    }

    /**
     * @see MessageBodyReader#isReadable(Class, Type, Annotation[])
     */
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        if (!type.equals(List.class))
            return false;
        Class<?> genericClass = Util.getGenericClass(genericType);
        if (genericClass == null)
            return false;
        return FileItem.class.isAssignableFrom(genericClass);
    }

    /**
     * @see MessageBodyReader#readFrom(Class, Type, Annotation[], MediaType,
     *      MultivaluedMap, InputStream)
     */
    @SuppressWarnings("unchecked")
    public List<FileItem> readFrom(Class<List<FileItem>> type,
            Type genericType, Annotation[] annotations, MediaType mediaType,
            final MultivaluedMap<String, String> respHeaders,
            final InputStream entityStream) throws IOException {
        FileUpload rfu = new FileUpload();
        RequestContext requCtx = new RequestContext(entityStream, respHeaders);
        try {
            return rfu.parseRequest(requCtx);
        } catch (FileUploadException e) {
            if (e.getCause() instanceof IOException)
                throw (IOException) e.getCause();
            IOException ioExc = new IOException(
                    "Could not read the multipart/form-data");
            ioExc.initCause(e);
            throw ioExc;
        }
    }
}