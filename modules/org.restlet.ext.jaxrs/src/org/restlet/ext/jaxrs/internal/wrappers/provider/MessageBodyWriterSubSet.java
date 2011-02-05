/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.jaxrs.internal.wrappers.provider;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.SortedMetadata;

/**
 * Contains a List of wrapped {@link javax.ws.rs.ext.MessageBodyWriter}s.
 * 
 * @author Stephan Koops
 */
public class MessageBodyWriterSubSet {

    private static final MessageBodyWriterSubSet EMPTY = new MessageBodyWriterSubSet(
            new ArrayList<MessageBodyWriter>(), null, null);

    /**
     * @return an empty {@link MessageBodyWriterSubSet}
     */
    public static MessageBodyWriterSubSet empty() {
        return EMPTY;
    }

    /**
     * The class supported by the contained message body writers, given by the
     * type parameter of the {@link javax.ws.rs.ext.MessageBodyWriter}. Could
     * be {@code null}.
     */
    private final Class<?> type;

    /**
     * The type supported by the contained message body writers, given by the
     * type parameter of the {@link javax.ws.rs.ext.MessageBodyWriter}. Could
     * be {@code null}.
     */
    private final Type genericType;

    private final List<MessageBodyWriter> mbws;

    MessageBodyWriterSubSet(List<MessageBodyWriter> mbws, final Class<?> type,
            final Type genericType) {
        this.mbws = mbws;
        this.genericType = genericType;
        this.type = type;
    }

    /**
     * returns a list of all producible media types.
     * 
     * @return a list of all producible media types. If this set is not empty,
     *         this result is not empty. '*<!---->/*' is returned for a message
     *         body writer with no &#64;{@link javax.ws.rs.Produces}
     *         annotation.
     */
    public Collection<MediaType> getAllProducibleMediaTypes() {
        final List<MediaType> p = new ArrayList<MediaType>();
        for (final MessageBodyWriter messageBodyWriter : this.mbws) {
            p.addAll(messageBodyWriter.getProducedMimes());
        }
        return p;
    }

    /**
     * Finds a {@link MessageBodyWriter} in this Set that best matches media
     * types of the response method and of the accepted {@link MediaType}s.
     * 
     * @param determinedResponseMediaType
     *                The {@link MediaType}s of the response, declared by the
     *                resource methods or given by the
     *                {@link javax.ws.rs.core.Response}.
     * @param annotations
     * @param accMediaTypes
     *                the accepted media types.
     * @return A {@link MessageBodyWriter} that best matches the given accepted.
     *         Returns null, if no adequate {@link MessageBodyWriter} could be
     *         found in this set.
     */
    public MessageBodyWriter getBestWriter(
            MediaType determinedResponseMediaType, Annotation[] annotations,
            SortedMetadata<MediaType> accMediaTypes) {
        final List<MessageBodyWriter> mbws = new ArrayList<MessageBodyWriter>();
        for (final MessageBodyWriter mbw : this.mbws) {
            if (mbw.supportsWrite(determinedResponseMediaType)) {
                if (mbw.isWriteable(type, genericType, annotations, Converter
                        .toJaxRsMediaType(determinedResponseMediaType))) {
                    mbws.add(mbw);
                }
            }
        }
        for (final Iterable<MediaType> amts : accMediaTypes.listOfColls()) {
            for (final MessageBodyWriter mbw : mbws) {
                if (mbw.supportsWrite(amts)) {
                    return mbw;
                }
            }
        }
        return null;
    }

    /**
     * Returns true, if this set is empty
     * 
     * @return true, if this set is empty
     */
    public boolean isEmpty() {
        return this.mbws.isEmpty();
    }
}