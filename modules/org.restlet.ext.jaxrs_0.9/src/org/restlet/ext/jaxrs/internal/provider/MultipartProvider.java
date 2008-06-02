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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.activation.DataSource;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.ByteArrayDataSource;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

/**
 * Entity Provider, that reads "multipart/form-data" to a {@link Multipart} and
 * writes vice versa.
 * <br>
 * This provider is not tested yet.
 * 
 * @author Stephan Koops
 * @see FileUploadProvider
 */
@Provider
@ConsumeMime("multipart/form-data")
@ProduceMime("multipart/form-data")
public class MultipartProvider implements MessageBodyReader<Multipart>,
        MessageBodyWriter<Multipart> {
    // NICE test MultipartProvider

    /**
     * @see javax.ws.rs.ext.MessageBodyWriter#getSize(java.lang.Object)
     */
    public long getSize(Multipart multipart) {
        return -1;
    }

    /**
     * @see MessageBodyReader#isReadable(Class, Type, Annotation[])
     */
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return type.isAssignableFrom(MimeMultipart.class);
    }

    /**
     * @see MessageBodyWriter#isWriteable(Class, Type, Annotation[])
     */
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return Multipart.class.isAssignableFrom(type);
    }

    /**
     * @see MessageBodyReader#readFrom(Class, Type, Annotation[], MediaType,
     *      MultivaluedMap, InputStream)
     */
    public Multipart readFrom(Class<Multipart> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpResponseHeaders,
            InputStream entityStream) throws IOException {
        String contentType = "multipart/form-data";
        DataSource ds = new ByteArrayDataSource(entityStream, contentType);
        try {
            return new MimeMultipart(ds);
        } catch (MessagingException e) {
            if (e.getCause() instanceof IOException)
                throw (IOException) e.getCause();
            IOException ioExc = new IOException(
                    "Could not deserialize the data to a Multipart");
            ioExc.initCause(e);
            throw ioExc;
        }
    }

    /**
     * @see MessageBodyWriter#writeTo(Object, Class, Type, Annotation[],
     *      MediaType, MultivaluedMap, OutputStream)
     */
    public void writeTo(Multipart multipart, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        try {
            multipart.writeTo(entityStream);
        } catch (MessagingException e) {
            if (e.getCause() instanceof IOException)
                throw (IOException) e.getCause();
            IOException ioExc = new IOException(
                    "Could not serialize the Multipart");
            ioExc.initCause(e);
            throw ioExc;
        }
    }
}