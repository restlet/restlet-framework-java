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
 * Contains a List of wrapped {@link javax.ws.rs.ext.MessageBodyReader}s.
 * 
 * @author Stephan Koops
 */
public class MessageBodyReaderSet extends LifoSet<MessageBodyReader> {

    /**
     * Creates a new MessageBodyReaderSet
     */
    public MessageBodyReaderSet() {
    }

    /**
     * @param c
     */
    public MessageBodyReaderSet(Collection<MessageBodyReader> c) {
        super(c);
    }

    /**
     * @see LifoSet#LifoSet(List, boolean)
     */
    private MessageBodyReaderSet(List<MessageBodyReader> c, boolean useGivenList) {
        super(c, useGivenList);
    }

    @Override
    public boolean add(MessageBodyReader mbr) {
        if (mbr == null)
            throw new IllegalArgumentException(
                    "The MessageBodyReader to add must not be null");
        return super.add(mbr);
    }

    /**
     * Returns a Collection of {@link MessageBodyReader}s, that support the
     * given entityClass.
     * 
     * @param entityClass
     * @return
     */
    private MessageBodyReaderSet subSet(Class<?> entityClass) {
        List<MessageBodyReader> mbws = new ArrayList<MessageBodyReader>();
        for (MessageBodyReader mbw : this) {
            if (mbw.isReadable(entityClass))
                mbws.add(mbw);
        }
        return new MessageBodyReaderSet(mbws, true);
    }

    /**
     * Returns a Collection of {@link MessageBodyReader}s, that support the
     * given {@link MediaType}.
     * 
     * @param mediaType
     *                The {@link MediaType}, that should be supported.
     * @return Collection of {@link MessageBodyReader}s
     */
    private MessageBodyReaderSet subSet(MediaType mediaType) {
        List<MessageBodyReader> mbrs = new ArrayList<MessageBodyReader>();
        for (MessageBodyReader mbr : this) {
            if (mbr.supports(mediaType))
                mbrs.add(mbr);
        }
        return new MessageBodyReaderSet(mbrs, true);
    }

    /**
     * Returns the first {@link MessageBodyReader} in this Set.
     * 
     * @param mediaType
     *                The {@link MediaType}, that should be supported.
     * @param paramType
     * 
     * @return The first {@link MessageBodyReader} of this Set. Returns null, if
     *         this Set is empty.
     */
    public MessageBodyReader getBest(MediaType mediaType, Class<?> paramType) {
        // LATER may be cached for speed.
        MessageBodyReaderSet mbrs = this.subSet(mediaType).subSet(paramType);
        if (mbrs.isEmpty())
            return null;
        return mbrs.iterator().next();
    }
}