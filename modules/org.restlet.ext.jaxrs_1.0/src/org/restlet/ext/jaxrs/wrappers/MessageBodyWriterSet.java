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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.util.LifoSet;

/**
 * Contains a List of wrapped {@link javax.ws.rs.ext.MessageBodyWriter}s.
 * 
 * @author Stephan Koops
 */
public class MessageBodyWriterSet extends LifoSet<MessageBodyWriter> {

    /**
     * Creates a new MessageBodyWriterSet
     */
    public MessageBodyWriterSet() {
    }

    /**
     * @param c
     */
    public MessageBodyWriterSet(Collection<MessageBodyWriter> c) {
        super(c);
    }

    /**
     * @see LifoSet#LifoSet(List, boolean)
     */
    private MessageBodyWriterSet(List<MessageBodyWriter> c, boolean useGivenList) {
        super(c, useGivenList);
    }

    /**
     * Returns a Collection of {@link MessageBodyWriter}s, that support the
     * given entityClass.
     * 
     * @param entityClass
     * @return
     */
    public MessageBodyWriterSet subSet(Class<?> entityClass) {
        // LATER may be cached for speed.
        List<MessageBodyWriter> mbws = new ArrayList<MessageBodyWriter>();
        for (MessageBodyWriter mbw : this) {
            if (mbw.isWriteable(entityClass))
                mbws.add(mbw);
        }
        return new MessageBodyWriterSet(mbws, true);
    }

    /**
     * Returns a Collection of {@link MessageBodyWriter}s, that support the
     * given {@link MediaType}s.
     * 
     * @param mediaTypes
     *                The {@link MediaType}s, that should be supported.
     * @return Collection of {@link MessageBodyWriter}s
     */
    public MessageBodyWriterSet subSet(Collection<MediaType> mediaTypes) {
        List<MessageBodyWriter> mbws = new ArrayList<MessageBodyWriter>();
        for (MessageBodyWriter mbw : this) {
            if (mbw.supportAtLeastOne(mediaTypes))
                mbws.add(mbw);
        }
        return new MessageBodyWriterSet(mbws, true);
    }

    /**
     * Finds a {@link MessageBodyWriter} in this Set that best matches the given
     * accepted {@link MediaType}s.
     * 
     * @param accMediaTypes
     * @return A {@link MessageBodyWriter} that best matches the given accepted.
     *         Returns null, if no adequate {@link MessageBodyWriter} could be
     *         found in this set.
     */
    public MessageBodyWriter getBest(List<Collection<MediaType>> accMediaTypes) {
        for (Collection<MediaType> amts : accMediaTypes)
            for (MessageBodyWriter mbw : this)
                if (mbw.supportAtLeastOne(amts))
                    return mbw;
        return null;
    }
}