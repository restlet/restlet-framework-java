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
package org.restlet.ext.jaxrs.wrappers;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.MediaType;

/**
 * Class to wrap a {@link javax.ws.rs.ext.MessageBodyWriter}
 * 
 * @author Stephan Koops
 */
@SuppressWarnings("unchecked")
public class MessageBodyWriter {

    private List<org.restlet.data.MediaType> producedMimes;

    private javax.ws.rs.ext.MessageBodyWriter writer;

    /**
     * Construct a wrapper or a {@link javax.ws.rs.ext.MessageBodyWriter}
     * 
     * @param writer
     *                the JAX-RS {@link javax.ws.rs.ext.MessageBodyWriter} to
     *                wrap.
     */
    public MessageBodyWriter(javax.ws.rs.ext.MessageBodyWriter<?> writer) {
        if (writer == null)
            throw new IllegalArgumentException(
                    "The MessageBodyWriter must not be null");
        this.writer = writer;
    }

    /**
     * Returns the list of produced {@link MediaType}s of the wrapped
     * {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return List of produced {@link MediaType}s.
     */
    public List<MediaType> getProducedMimes() {
        if (producedMimes == null) {
            ProduceMime pm = writer.getClass().getAnnotation(ProduceMime.class);
            if (pm != null)
                this.producedMimes = ResourceMethod.convertToMediaTypes(pm
                        .value());
            else
                this.producedMimes = Collections.singletonList(MediaType.ALL);
        }
        return producedMimes;
    }

    /**
     * Checks, if the given class is supported by this MessageBodyWriter
     * 
     * @param type
     * @return
     * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(Class)
     */
    public boolean isWriteable(Class<?> type) {
        return writer.isWriteable(type);
    }

    /**
     * Checks, if the wrapped MessageBodyWriter supports at least one of the
     * requested {@link MediaType}s.
     * 
     * @param mediaTypes
     *                the {@link MediaType}s
     * @return true, if at least one of the requested {@link MediaType}s is
     *         supported, otherwise false.
     */
    public boolean supportAtLeastOne(Collection<MediaType> mediaTypes) {
        for (MediaType produced : getProducedMimes()) {
            for (MediaType requested : mediaTypes)
                if (supports(requested, produced))
                    return true;
        }
        return false;
    }

    /**
     * 
     * @param mbwMediaType
     * @param requestedMediaType
     * @return
     */
    public static boolean supports(MediaType mbwMediaType,
            MediaType requestedMediaType) {
        return mbwMediaType.includes(requestedMediaType)
                || requestedMediaType.includes(mbwMediaType);
    }

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
    public long getSize(Object t) {
        return writer.getSize(t);
    }

    /**
     * Write a type to an HTTP response. The response header map is mutable but
     * any changes must be made before writing to the output stream since the
     * headers will be flushed prior to writing the response body.
     * 
     * @param t
     *                the type to write.
     * @param mediaType
     *                the media type of the HTTP entity.
     * @param httpHeaders
     *                a mutable map of the HTTP response headers.
     * @param entityStream
     *                the {@link OutputStream} for the HTTP entity.
     * @throws java.io.IOException
     *                 if an IO error arises
     */
    public void writeTo(Object t, javax.ws.rs.core.MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        writer.writeTo(t, mediaType, httpHeaders, entityStream);
    }

    @Override
    public String toString() {
        return "MessageBodyWriter:" + writer.getClass().getName();
    }

    @Override
    public boolean equals(Object otherMbw) {
        if (this == otherMbw)
            return true;
        if (!(otherMbw instanceof MessageBodyWriter))
            return false;
        return this.writer.getClass().equals(
                ((MessageBodyWriter) otherMbw).writer.getClass());
    }

    @Override
    public int hashCode() {
        return writer.hashCode();
    }
}