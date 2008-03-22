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
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.wrappers.MessageBodyWriter;
import org.restlet.resource.OutputRepresentation;

/**
 * This representation is used to write the Representations with a
 * {@link javax.ws.rs.ext.MessageBodyWriter}.
 * 
 * @author Stephan Koops
 * @param <T>
 *                type of the object to serialize.
 */
public class JaxRsOutputRepresentation<T> extends OutputRepresentation {

    private MessageBodyWriter<T> mbw;

    private T object;

    /** necessary, because the {@link #object} could be null */
    private Class<?> type;

    private Type genericType;

    private Annotation[] annotations;

    private MultivaluedMap<String, Object> httpHeaders;

    /**
     * Creates a new JaxRsOutputRepresentation.
     * 
     * @param object
     *                the object to serialize The generic {@link Type} to
     *                convert to.
     * @param genericType
     *                The generic {@link Type} to convert to.
     * @param mediaType
     *                the MediaType of the object. Must be concrete, see
     *                {@link MediaType#isConcrete()}.
     * @param annotations
     *                the annotations of the artefact to convert to
     * @param mbw
     *                the MessageBodyWriter which will serialize the object.
     * @param httpHeaders
     *                the mutable Map of HTTP response headers.
     */
    public JaxRsOutputRepresentation(T object, Type genericType, MediaType mediaType,
            Annotation[] annotations, MessageBodyWriter<T> mbw,
            MultivaluedMap<String, Object> httpHeaders) {
        super(mediaType, mbw.getSize(object));
        if (!mediaType.isConcrete())
            throw new IllegalArgumentException(mediaType + " is not concrete");
        this.genericType = genericType;
        this.annotations = annotations;
        this.mbw = mbw;
        this.httpHeaders = httpHeaders;
        this.object = object;
        this.type = object.getClass();
        // TODO if definitely not needed remove instance variable
    }

    /**
     * @see org.restlet.resource.Representation#write(java.io.OutputStream)
     */
    @Override
    public void write(OutputStream outputStream) throws IOException {
        this.mbw.writeTo(object, type, genericType, annotations, Converter
                .toJaxRsMediaType(getMediaType(), null), httpHeaders,
                outputStream);
    }
}