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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.util.SortedMetadata;

/**
 * Contains a List of wrapped {@link javax.ws.rs.ext.MessageBodyWriter}s.
 * 
 * @author Stephan Koops
 */
public class MessageBodyWriterSubSet {

    private List<MessageBodyWriter<?>> mbws;

    MessageBodyWriterSubSet(List<MessageBodyWriter<?>> mbws) {
        this.mbws = mbws;
    }

    /**
     * returns a list of all producible media types.
     * 
     * @return a list of all producible media types.
     */
    public Collection<MediaType> getAllProducibleMediaTypes() {
        List<MediaType> p = new ArrayList<MediaType>();
        for (MessageBodyWriter<?> messageBodyWriter : mbws)
            p.addAll(messageBodyWriter.getProducedMimes());
        return p;
    }

    /**
     * Finds a {@link MessageBodyWriter} in this Set that best matches media
     * types of the response method and of the accepted {@link MediaType}s.
     * 
     * @param responseMediaTypes
     *                The {@link MediaType}s of the response, declared by the
     *                resource methods.
     * @param accMediaTypes
     * @return A {@link MessageBodyWriter} that best matches the given accepted.
     *         Returns null, if no adequate {@link MessageBodyWriter} could be
     *         found in this set.
     */
    public MessageBodyWriter<?> getBestWriter(
            Collection<MediaType> responseMediaTypes,
            SortedMetadata<MediaType> accMediaTypes) {
        List<MessageBodyWriter<?>> mbws = new ArrayList<MessageBodyWriter<?>>();
        for (MessageBodyWriter<?> mbw : this.mbws) {
            if (mbw.supportAtLeastOne(responseMediaTypes))
                mbws.add(mbw);
        }
        for (Iterable<MediaType> amts : accMediaTypes.listOfColls())
            for (MessageBodyWriter<?> mbw : mbws)
                if (mbw.supportAtLeastOne(amts))
                    return mbw;
        return null;
    }
}