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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyReader;

/**
 * Contains the entity providers and has some methods to pick the wished out.
 * 
 * @author Stephan Koops
 */
public class EntityProviders implements javax.ws.rs.ext.MessageBodyWorkers,
        MessageBodyReaderSet {

    private volatile List<MessageBodyReader<?>> messageBodyReaders = new ArrayList<MessageBodyReader<?>>();
    // LATER allow concurent access.

    private volatile List<MessageBodyWriter<?>> messageBodyWriters = new ArrayList<MessageBodyWriter<?>>();
    // LATER allow concurent access.

    /**
     * Adds the given provider to this EntityProviders. If the Provider is not
     * an entity provider, it doesn't matter.
     * 
     * @param provider
     * @param defaultProvider
     */
    public void add(Provider<?> provider, boolean defaultProvider) {
        if (provider.isWriter()) {
            if (defaultProvider)
                this.messageBodyWriters.add(provider);
            else
                this.messageBodyWriters.add(0, provider);
        }
        if (provider.isReader()) {
            if (defaultProvider)
                this.messageBodyReaders.add(provider);
            else
                this.messageBodyReaders.add(0, provider);
        }
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWorkers#getMessageBodyReaders(javax.ws.rs.core.MediaType,
     *      Class, Type, Annotation[])
     */
    @SuppressWarnings("unchecked")
    public <T> javax.ws.rs.ext.MessageBodyReader<T> getMessageBodyReader(
            Class<T> type, Type genericType,
            Annotation[] annotations, javax.ws.rs.core.MediaType mediaType) {
        MediaType restletMediaType = Converter.toRestletMediaType(mediaType);
        MessageBodyReader<?> mbr;
        mbr = getBestReader(restletMediaType, type, genericType, annotations);
        return (javax.ws.rs.ext.MessageBodyReader) mbr.getJaxRsReader();
    }

    /**
     * @see javax.ws.rs.ext.MessageBodyWorkers#getMessageBodyWriters(javax.ws.rs.core.MediaType,
     *      Class, Type, Annotation[])
     */
    @SuppressWarnings("unchecked")
    public <T> javax.ws.rs.ext.MessageBodyWriter<T> getMessageBodyWriter(
            Class<T> type, Type genericType,
            Annotation[] annotations, javax.ws.rs.core.MediaType mediaType) {
        Collection<MediaType> restletMediaTypes = Collections
                .singleton(Converter.toRestletMediaType(mediaType));
        List<MessageBodyWriter> mbws = (List) this.messageBodyWriters;
        for (MessageBodyWriter<T> mbw : mbws) {
            if (mbw.supportAtLeastOne(restletMediaTypes))
                if (mbw.isWriteable(type, genericType, annotations))
                    return mbw.getJaxRsWriter();
        }
        return null;
    }

    /**
     * Returns the {@link MessageBodyReader}, that best matches the given
     * criteria.
     * 
     * @param mediaType
     *                The {@link MediaType}, that should be supported.
     * @param paramType
     * @param genericType
     * @param annotations
     * 
     * @return the {@link MessageBodyReader}, that best matches the given
     *         criteria, or null if no matching MessageBodyReader could be
     *         found.
     * @see MessageBodyReaderSet#getBestReader(MediaType, Class, Type,
     *      Annotation[])
     */
    @SuppressWarnings("unchecked")
    public MessageBodyReader<?> getBestReader(MediaType mediaType,
            Class<?> paramType, Type genericType, Annotation[] annotations) {
        // LATER optimization: may be cached for speed.
        for (MessageBodyReader mbr : this.messageBodyReaders) {
            if (mbr.supports(mediaType))
                if (mbr.isReadable(paramType, genericType, annotations))
                    return mbr;
        }
        return null;
    }

    /**
     * Returns a Collection of {@link MessageBodyWriter}s, that support the
     * given entityClass.
     * 
     * @param entityClass
     * @param genericType
     *                may be nullW
     * @param annotations
     *                may be null
     * @return
     * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(Class, Type,
     *      Annotation[])
     */
    @SuppressWarnings("unchecked")
    public MessageBodyWriterSubSet writerSubSet(Class<?> entityClass,
            Type genericType, Annotation[] annotations) {
        // LATER optimization: may be cached for speed.
        List<MessageBodyWriter<?>> mbws = new ArrayList<MessageBodyWriter<?>>();
        for (MessageBodyWriter mbw : this.messageBodyWriters) {
            try {
                if (mbw.isWriteable(entityClass, genericType, annotations))
                    mbws.add(mbw);
            } catch (NullPointerException e) {
                if (genericType != null && annotations != null)
                    throw e;
                // otherwise it's interpreted as not writable
            } catch (IllegalArgumentException e) {
                if (genericType != null && annotations != null)
                    throw e;
                // otherwise it's interpreted as not writable
            }
        }
        return new MessageBodyWriterSubSet(mbws);
    }

    /**
     * Adds all contained message body readers and writers to the given provider
     * Set.
     * 
     * @param providers
     *                the set to add the providers.
     */
    @SuppressWarnings("unchecked")
    public void addAllTo(Set<Provider<?>> providers) {
        providers.addAll((Collection) this.messageBodyReaders);
        providers.addAll((Collection) this.messageBodyWriters);
    }
}