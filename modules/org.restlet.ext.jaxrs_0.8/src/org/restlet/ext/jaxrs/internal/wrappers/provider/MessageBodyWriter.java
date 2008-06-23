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

import java.util.List;

import org.restlet.data.MediaType;

/**
 * Interface to access a wrapped a {@link javax.ws.rs.ext.MessageBodyWriter}
 * 
 * @author Stephan Koops
 * @param <T>
 *                the java type to convert.
 */
public interface MessageBodyWriter<T> extends
        javax.ws.rs.ext.MessageBodyWriter<T> {

    /**
     * Returns the JAX-RS {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return the JAX-RS MessageBodyWriter
     */
    public javax.ws.rs.ext.MessageBodyWriter<T> getJaxRsWriter();

    /**
     * Returns the list of produced {@link MediaType}s of the wrapped
     * {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return List of produced {@link MediaType}s. If the entity provider is
     *         not annotated with &#64; {@link javax.ws.rs.ProduceMime}, '*<!---->/*'
     *         is returned.
     */
    public List<MediaType> getProducedMimes();

    /**
     * Called before <code>writeTo</code> to ascertain the length in bytes of
     * the serialized form of <code>t</code>. A non-negative return value is
     * used in a HTTP <code>Content-Length</code> header.
     * 
     * @param t
     *                the type
     * @return length in bytes or -1 if the length cannot be determined in
     *         advance
     */
    public long getSize(T t);

    /**
     * Checks, if the wrapped MessageBodyWriter supports at least one of the
     * given {@link MediaType}s.
     * 
     * @param mediaTypes
     *                the {@link MediaType}s
     * @return true, if at least one of the requested {@link MediaType}s is
     *         supported, otherwise false.
     */
    public boolean supportsWrite(Iterable<MediaType> mediaTypes);

    /**
     * Checks, if the wrapped MessageBodyWriter supports the given
     * {@link MediaType}.
     * 
     * @param mediaType
     *                the {@link MediaType}
     * @return true, if the requested {@link MediaType} is supported, otherwise
     *         false.
     */
    public boolean supportsWrite(MediaType mediaType);
}