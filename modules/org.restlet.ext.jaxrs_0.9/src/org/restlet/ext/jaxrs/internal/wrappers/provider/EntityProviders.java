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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;
import org.restlet.ext.jaxrs.internal.util.Converter;

/**
 * Contains the entity providers and has some methods to pick the wished out.
 * 
 * @author Stephan Koops
 */
public class EntityProviders implements javax.ws.rs.ext.Providers,
        MessageBodyReaderSet {

    /**
     * Checks, if the given {@link javax.ws.rs.ext.MessageBodyReader} is
     * writeable for the given class, genericType and annotations. If one of the
     * arguments is null, and the MessageBodyWriter throws a
     * {@link NullPointerException} or an {@link IllegalArgumentException}, it
     * is interpreted as false.
     * 
     * @param mbr
     * @param paramType
     * @param genericType
     * @param annotations
     * @return
     * @see #isWriteable(MessageBodyWriter, Class, Type, Annotation[])
     */
    @SuppressWarnings("unchecked")
    private static boolean isReadable(MessageBodyReader mbr,
            Class<?> paramType, Type genericType, Annotation[] annotations) {
        try {
            return mbr.isReadable(paramType, genericType, annotations);
        } catch (final NullPointerException e) {
            if ((genericType == null) || (annotations == null)) {
                // interpreted as not readable for the given combination
                return false;
            }
            throw e;
        } catch (final IllegalArgumentException e) {
            if ((genericType == null) || (annotations == null)) {
                // interpreted as not readable for the given combination
                return false;
            }
            throw e;
        }
    }

    /**
     * Checks, if the given {@link javax.ws.rs.ext.MessageBodyWriter} is
     * writeable for the given class, genericType and annotations. If one of the
     * arguments is null, and the MessageBodyWriter throws a
     * {@link NullPointerException} or an {@link IllegalArgumentException}, it
     * is interpreted as false.
     * 
     * @param mbw
     * @param entityClass
     * @param genericType
     * @param annotations
     * @throws NullPointerException
     * @throws IllegalArgumentException
     * @see #isReadable(MessageBodyReader, Class, Type, Annotation[])
     */
    @SuppressWarnings("unchecked")
    private static boolean isWriteable(MessageBodyWriter mbw,
            Class<?> entityClass, Type genericType, Annotation[] annotations)
            throws NullPointerException, IllegalArgumentException {
        try {
            return mbw.isWriteable(entityClass, genericType, annotations);
        } catch (final NullPointerException e) {
            if ((genericType == null) || (annotations == null)) {
                // interpreted as not writable for the given combination
                return false;
            }
            throw e;
        } catch (final IllegalArgumentException e) {
            if ((genericType == null) || (annotations == null)) {
                // interpreted as not writable for the given combination
                return false;
            }
            throw e;
        }
    }

    private final List<MessageBodyReader<?>> messageBodyReaders;

    private final List<MessageBodyWriter<?>> messageBodyWriters;

    /**
     * Creates a new EntotProviders.
     */
    public EntityProviders() {
        this.messageBodyReaders = new CopyOnWriteArrayList<MessageBodyReader<?>>();
        this.messageBodyWriters = new CopyOnWriteArrayList<MessageBodyWriter<?>>();
    }

    /**
     * Adds the given provider to this EntityProviders. If the Provider is not
     * an entity provider, it doesn't matter.
     * 
     * @param provider
     * @param defaultProvider
     */
    public void add(Provider<?> provider, boolean defaultProvider) {
        if (provider.isWriter()) {
            if (defaultProvider) {
                this.messageBodyWriters.add(provider);
            } else {
                this.messageBodyWriters.add(0, provider);
            }
        }
        if (provider.isReader()) {
            if (defaultProvider) {
                this.messageBodyReaders.add(provider);
            } else {
                this.messageBodyReaders.add(0, provider);
            }
        }
    }

    /**
     * Returns the {@link MessageBodyReader}, that best matches the given
     * criteria.
     * 
     * @param paramType
     * @param genericType
     * @param annotations
     * @param mediaType
     *            The {@link MediaType}, that should be supported.
     * @return the {@link MessageBodyReader}, that best matches the given
     *         criteria, or null if no matching MessageBodyReader could be
     *         found.
     * @see MessageBodyReaderSet#getBestReader(Class, Type, Annotation[],
     *      MediaType)
     */
    @SuppressWarnings("unchecked")
    public MessageBodyReader<?> getBestReader(Class<?> paramType,
            Type genericType, Annotation[] annotations, MediaType mediaType) {
        // NICE optimization: may be cached for speed.
        for (final MessageBodyReader mbr : this.messageBodyReaders) {
            if (mbr.supportsRead(mediaType)) {
                if (isReadable(mbr, paramType, genericType, annotations)) {
                    return mbr;
                }
            }
        }
        return null;
    }

    /**
     * @see javax.ws.rs.ext.Providers#getContextResolver(java.lang.Class,
     *      java.lang.Class, javax.ws.rs.core.MediaType)
     */
    public <T> ContextResolver<T> getContextResolver(Class<T> contextType,
            Class<?> objectType, javax.ws.rs.core.MediaType mediaType) {
        // TODO Providers.getContextResolver()
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.ext.Providers#getExceptionMapper(java.lang.Class)
     */
    public <T> ExceptionMapper<T> getExceptionMapper(Class<T> type) {
        // TODO Providers.getExceptionMapper()
        throw new NotYetImplementedException();
    }

    /**
     * @see javax.ws.rs.ext.Providers#getMessageBodyReader(Class, Type,
     *      Annotation[], javax.ws.rs.core.MediaType)
     */
    @SuppressWarnings("unchecked")
    public <T> javax.ws.rs.ext.MessageBodyReader<T> getMessageBodyReader(
            Class<T> type, Type genericType, Annotation[] annotations,
            javax.ws.rs.core.MediaType mediaType) {
        final MediaType restletMediaType = Converter
                .toRestletMediaType(mediaType);
        MessageBodyReader<?> mbr;
        mbr = getBestReader(type, genericType, annotations, restletMediaType);
        return (javax.ws.rs.ext.MessageBodyReader) mbr.getJaxRsReader();
    }

    /**
     * @see javax.ws.rs.ext.Providers#getMessageBodyWriter(Class, Type,
     *      Annotation[], javax.ws.rs.core.MediaType)
     */
    @SuppressWarnings("unchecked")
    public <T> javax.ws.rs.ext.MessageBodyWriter<T> getMessageBodyWriter(
            Class<T> type, Type genericType, Annotation[] annotations,
            javax.ws.rs.core.MediaType mediaType) {
        final MediaType restletMediaType = Converter
                .toRestletMediaType(mediaType);
        final List<MessageBodyWriter> mbws = (List) this.messageBodyWriters;
        for (final MessageBodyWriter<T> mbw : mbws) {
            if (mbw.supportsWrite(restletMediaType)) {
                if (isWriteable(mbw, type, genericType, annotations)) {
                    return mbw.getJaxRsWriter();
                }
            }
        }
        return null;
    }

    /**
     * Returns a Collection of {@link MessageBodyWriter}s, that support the
     * given entityClass.
     * 
     * @param entityClass
     * @param genericType
     *            may be null
     * @param annotations
     *            may be null
     * @return
     * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(Class, Type,
     *      Annotation[])
     */
    @SuppressWarnings("unchecked")
    public MessageBodyWriterSubSet writerSubSet(Class<?> entityClass,
            Type genericType, Annotation[] annotations) {
        // NICE optimization: may be cached for speed.
        final List<MessageBodyWriter<?>> mbws = new ArrayList<MessageBodyWriter<?>>();
        for (final MessageBodyWriter mbw : this.messageBodyWriters) {
            if (isWriteable(mbw, entityClass, genericType, annotations)) {
                mbws.add(mbw);
            }
        }
        return new MessageBodyWriterSubSet(mbws);
    }
}