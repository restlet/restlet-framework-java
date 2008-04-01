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
package org.restlet.ext.jaxrs.internal.wrappers;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.MediaType;

/**
 * Class to wrap a {@link javax.ws.rs.ext.MessageBodyWriter}
 * 
 * @author Stephan Koops
 * @param <T> the java type to convert.
 */
@SuppressWarnings("unchecked")
public interface MessageBodyReader<T> {

    /**
     * Checks, if this MessageBodyReader could read the given type.
     * 
     * @param type
     * @param genericType
     * @param annotations
     * @return
     * @see javax.ws.rs.ext.MessageBodyReader#isReadable(Class, Type,
     *      Annotation[])
     */
    public boolean isReadable(Class<T> type, Type genericType,
            Annotation[] annotations);

    /**
     * Reads an object of the given type from the given entityStream.
     * 
     * @param type
     * @param genericType
     *                The generic {@link Type} to convert to.
     * @param mediaType
     * @param annotations
     *                the annotations of the artefact to convert to
     * @param httpHeaders
     * @param entityStream
     * @return
     * @throws IOException
     * @see javax.ws.rs.ext.MessageBodyReader#readFrom(Class, Type,
     *      javax.ws.rs.core.MediaType, Annotation[], MultivaluedMap,
     *      InputStream)
     */
    public T readFrom(Class<T> type, Type genericType,
            javax.ws.rs.core.MediaType mediaType, Annotation[] annotations,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException;

    /**
     * Returns the list of produced {@link MediaType}s of the wrapped
     * {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return List of produced {@link MediaType}s.
     */
    public List<MediaType> getConsumedMimes();

    /**
     * Checks, if this MessageBodyReader supports the given MediaType.
     * 
     * @param mediaType
     * @return
     */
    public boolean supports(MediaType mediaType);
}