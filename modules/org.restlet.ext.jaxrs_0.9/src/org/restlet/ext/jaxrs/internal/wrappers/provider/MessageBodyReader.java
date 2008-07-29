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
package org.restlet.ext.jaxrs.internal.wrappers.provider;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;

/**
 * Class to wrap a {@link javax.ws.rs.ext.MessageBodyWriter}
 * 
 * @author Stephan Koops
 */
public interface MessageBodyReader {

    /**
     * 
     * @param type
     * @param genericType
     * @param annotations
     * @return
     * @see {@link javax.ws.rs.ext.MessageBodyReader#isReadable(Class, Type, Annotation[])}
     */
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation annotations[]);

    /**
     * 
     * @param type
     * @param genericType
     * @param annotations
     * @param mediaType
     * @param characterSet
     * @param httpHeaders
     * @param entityStream
     * @return
     * @throws IOException
     * @throws WebApplicationException
     * @see {@link javax.ws.rs.ext.MessageBodyReader#readFrom(Class, Type, Annotation[], javax.ws.rs.core.MediaType, MultivaluedMap, InputStream)}
     */
    public Object readFrom(Class<?> type, Type genericType,
            Annotation annotations[], MediaType mediaType,
            CharacterSet characterSet,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException;

    /**
     * Returns the list of produced {@link MediaType}s of the wrapped
     * {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return List of produced {@link MediaType}s.
     */
    public List<MediaType> getConsumedMimes();

    /**
     * Returns the JAX-RS {@link javax.ws.rs.ext.MessageBodyReader}.
     * 
     * @return the JAX-RS MessageBodyReader
     */
    public javax.ws.rs.ext.MessageBodyReader<?> getJaxRsReader();

    /**
     * Checks, if this MessageBodyReader supports the given MediaType.
     * 
     * @param mediaType
     * @return true, if the given media type could be read, or false if not.
     */
    public boolean supportsRead(MediaType mediaType);
}