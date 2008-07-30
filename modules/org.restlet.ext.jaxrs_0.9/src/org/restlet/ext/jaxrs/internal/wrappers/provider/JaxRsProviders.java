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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Providers;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.ImplementationException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.JaxRsRuntimeException;
import org.restlet.ext.jaxrs.internal.util.Converter;

/**
 * Contains the entity providers and has some methods to pick the wished out.
 * 
 * @author Stephan Koops
 */
public class JaxRsProviders implements javax.ws.rs.ext.Providers,
        MessageBodyReaderSet {

    /**
     * @author Stephan
     * 
     */
    private static final class ErrorMapper implements ExceptionMapper<Error> {
        public Response toResponse(Error error) {
            throw error;
        }
    }

    /**
     * @author Stephan
     * 
     */
    private static final class RuntimeExcMapper implements
            ExceptionMapper<RuntimeException> {
        public Response toResponse(RuntimeException runtimeException) {
            throw runtimeException;
        }
    }

    /**
     * @author Stephan
     * 
     */
    private static final class ThrowableMapper implements
            ExceptionMapper<Throwable> {
        public Response toResponse(Throwable exception) {
            throw new JaxRsRuntimeException(exception);
        }
    }

    private static final Logger localLogger = Logger
            .getLogger("ExceptionsMapper");

    /**
     * Returns the generic class of the given {@link ContextResolver} class.
     * 
     * @param crClaz
     */
    private static Class<?> getCtxResGenClass(Class<?> crClaz) {
        Type[] crIfTypes = crClaz.getGenericInterfaces();
        for (Type crIfType : crIfTypes) {
            if (!(crIfType instanceof ParameterizedType))
                continue;
            Type t = ((ParameterizedType) crIfType).getActualTypeArguments()[0];
            if (!(t instanceof Class))
                continue;
            return (Class<?>) t;
        }
        return null;
    }

    private final Set<Provider> all;

    /**
     * This {@link Set} contains all available
     * {@link javax.ws.rs.ext.ContextResolver}s.<br>
     * This field is final, because it is shared with other objects.
     */
    private final Collection<ContextResolver> contextResolvers;

    private final Map<Class<? extends Throwable>, ExceptionMapper<? extends Throwable>> excMappers;

    private final List<MessageBodyReader> messageBodyReaders;

    private final List<MessageBodyWriter> messageBodyWriters;

    /**
     * Creates a new JaxRsProviders.
     */
    public JaxRsProviders() {
        this.all = new CopyOnWriteArraySet<Provider>();
        this.messageBodyReaders = new CopyOnWriteArrayList<MessageBodyReader>();
        this.messageBodyWriters = new CopyOnWriteArrayList<MessageBodyWriter>();
        this.contextResolvers = new CopyOnWriteArraySet<ContextResolver>();
        this.excMappers = new ConcurrentHashMap<Class<? extends Throwable>, ExceptionMapper<? extends Throwable>>();
        this.add(new ThrowableMapper());
        this.add(new ErrorMapper());
        this.add(new RuntimeExcMapper());
    }

    /**
     * Adds the given {@link ExceptionMapper} to this ExceptionMappers.
     * 
     * @param excMapper
     * @return true, if the providers was an ExceptionMapper and added,
     *         otherwise false.
     * @throws NullPointerException
     *                 if null is given
     */
    public boolean add(ExceptionMapper<? extends Throwable> excMapper) {
        boolean added = false;
        Type[] gis = excMapper.getClass().getGenericInterfaces();
        for (Type gi : gis) {
            if (gi instanceof ParameterizedType) {
                ParameterizedType ifpt = (ParameterizedType) gi;
                if (ifpt.getRawType().equals(ExceptionMapper.class)) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Throwable> excClass = (Class) ifpt
                            .getActualTypeArguments()[0];
                    excMappers.put(excClass, excMapper);
                    added = true;
                }
            }
        }
        return added;
    }

    /**
     * Adds the given {@link Provider} to this ExceptionMappers.
     * 
     * @param exceptionMapper
     * @return true, if the providers was an ExceptionMapper and added,
     *         otherwise false.
     * @throws NullPointerException
     *                 if <code>null</code> is given
     */
    public boolean add(Provider exceptionMapper) {
        if (!exceptionMapper.isExceptionMapper())
            return false;
        return add(exceptionMapper.getExcMapper());
    }

    /**
     * Adds the given provider to this JaxRsProviders. If the Provider is not an
     * entity provider, it doesn't matter.
     * 
     * @param provider
     * @param defaultProvider
     */
    public void add(Provider provider, boolean defaultProvider) {
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
        if (provider.isContextResolver())
            this.contextResolvers.add(provider);
        if (provider.isExceptionMapper())
            this.add(provider.getExcMapper());
        this.all.add(provider);
    }

    /**
     * converts the cause of the given InvocationTargetException to a
     * {@link Response}, if an {@link ExceptionMapper} could be found.<br>
     * Otherwise this method returns an Response with an internal server error.
     * 
     * @param cause
     *                the thrown exception (was wrapped by an
     *                {@link InvocationTargetException})
     * @return the created Response
     * @throws NullPointerException
     *                 if <code>null</code> is given
     */
    @SuppressWarnings("unchecked")
    public Response convert(Throwable cause) {
        ExceptionMapper mapper = getExceptionMapper(cause.getClass());
        if (mapper == null) {
            String entity = "No ExceptionMapper was found, but must be found";
            return Response.serverError().entity(entity).type(
                    javax.ws.rs.core.MediaType.TEXT_PLAIN_TYPE).build();
        }
        Response response = mapper.toResponse(cause);
        if (response == null) {
            String message = "The ExceptionMapper returned null";
            localLogger.log(Level.WARNING, message);
            return Response.serverError().entity(message).build();
        }
        return response;
    }

    /**
     * Returns the {@link MessageBodyReader}, that best matches the given
     * criteria.
     * 
     * @param paramType
     * @param genericType
     * @param annotations
     * @param mediaType
     *                The {@link MediaType}, that should be supported.
     * @return the {@link MessageBodyReader}, that best matches the given
     *         criteria, or null if no matching MessageBodyReader could be
     *         found.
     * @see MessageBodyReaderSet#getBestReader(Class, Type, Annotation[],
     *      MediaType)
     */
    public MessageBodyReader getBestReader(Class<?> paramType,
            Type genericType, Annotation[] annotations, MediaType mediaType) {
        // NICE optimization: may be cached for speed.
        for (MessageBodyReader mbr : this.messageBodyReaders) {
            if (mbr.supportsRead(mediaType))
                if (mbr.isReadable(paramType, genericType, annotations))
                    return mbr;
        }
        return null;
    }

    /**
     * @see Providers#getContextResolver(Class, Class,
     *      javax.ws.rs.core.MediaType)
     */
    @SuppressWarnings("unchecked")
    public <T> javax.ws.rs.ext.ContextResolver<T> getContextResolver(
            Class<T> contextType, Class<?> objectType,
            javax.ws.rs.core.MediaType mediaType) {
        // LATER test JaxRsProviders.getContextResolver
        for (ContextResolver crWrapper : this.contextResolvers) {
            final javax.ws.rs.ext.ContextResolver<?> cr;
            cr = crWrapper.getContextResolver();
            final Class<?> crClaz = cr.getClass();
            final Class<?> genClass = getCtxResGenClass(crClaz);
            if (genClass == null || !genClass.equals(contextType)) {
                continue;
            }
            if (!crWrapper.supportsWrite(mediaType)) {
                continue;
            }
            try {
                Method getContext = crClaz.getMethod("getContext", Class.class);
                if (getContext.getReturnType().equals(contextType)) {
                    return (javax.ws.rs.ext.ContextResolver<T>) cr;
                }
            } catch (SecurityException e) {
                throw new RuntimeException(
                        "sorry, the method getContext(Class) of ContextResolver "
                                + crClaz + " is not accessible");
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(
                        "The ContextResolver "
                                + crClaz
                                + " is not valid, because it has no method getContext(Class)");
            }
        }
        return null;
    }

    /**
     * @param causeClass
     * @return the ExceptionMapper for the given Throwable class. Never returns
     *         null.
     */
    @SuppressWarnings("unchecked")
    public <T> ExceptionMapper<T> getExceptionMapper(Class<T> causeClass) {
        if (causeClass == null)
            throw new ImplementationException(
                    "The call of an exception mapper with null is not allowed");
        ExceptionMapper<T> mapper = (ExceptionMapper) this.excMappers
                .get(causeClass);
        if (mapper == null) {
            Class superclass = causeClass.getSuperclass();
            if (superclass == null || superclass.equals(Object.class))
                throw new ImplementationException("Why is the superclass of "
                        + causeClass + " " + causeClass + "?");
            mapper = getExceptionMapper(superclass);
            // disabled caching, because adding of new ExceptionMappers could
            // cause trouble.
            // this.excMappers.put(superclass, mapper);
        }
        return mapper;
    }

    /**
     * @see javax.ws.rs.ext.Providers#getMessageBodyReader(Class, Type,
     *      Annotation[], javax.ws.rs.core.MediaType)
     */
    @SuppressWarnings("unchecked")
    public <T> javax.ws.rs.ext.MessageBodyReader<T> getMessageBodyReader(
            Class<T> type, Type genericType, Annotation[] annotations,
            javax.ws.rs.core.MediaType mediaType) {
        MediaType restletMediaType = Converter.toRestletMediaType(mediaType);
        final MessageBodyReader mbr;
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
        MediaType restletMediaType = Converter.toRestletMediaType(mediaType);
        List<MessageBodyWriter> mbws = this.messageBodyWriters;
        for (MessageBodyWriter mbw : mbws) {
            if (mbw.supportsWrite(restletMediaType))
                if (mbw.isWriteable(type, genericType, annotations))
                    return (javax.ws.rs.ext.MessageBodyWriter<T>) mbw
                            .getJaxRsWriter();
        }
        return null;
    }

    /**
     * Init all providers. If an error for one provider occurs, this provider is
     * ignored and the next provider initialized.
     * 
     * @param tlContext
     * @param extensionBackwardMapping
     */
    public void initAll(ThreadLocalizedContext tlContext,
            ExtensionBackwardMapping extensionBackwardMapping) {
        for (final Provider provider : new ArrayList<Provider>(this.all)) {
            try {
                provider.init(tlContext, this, extensionBackwardMapping);
            } catch (InjectException e) {
                localLogger.log(Level.WARNING, "The provider "
                        + provider.getClassName() + " could not be used", e);
                this.remove(provider);
            } catch (IllegalTypeException e) {
                localLogger.log(Level.WARNING, "The provider "
                        + provider.getClassName() + " could not be used", e);
                this.remove(provider);
            } catch (InvocationTargetException e) {
                localLogger.log(Level.WARNING, "The provider "
                        + provider.getClassName() + " could not be used", e
                        .getCause());
                this.remove(provider);
            }
        }
    }

    /**
     * @param provider
     */
    private void remove(Provider provider) {
        this.all.remove(provider);
        this.contextResolvers.remove(provider);
        this.messageBodyReaders.remove(provider);
        this.messageBodyWriters.remove(provider);
        if (provider.getExcMapper() != null)
            this.excMappers.remove(provider.getExcMapper());
    }

    /**
     * Returns a Collection of {@link MessageBodyWriter}s, that support the
     * given entityClass.
     * 
     * @param entityClass
     * @param genericType
     *                may be null
     * @param annotations
     *                may be null
     * @return
     * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(Class, Type,
     *      Annotation[])
     */
    public MessageBodyWriterSubSet writerSubSet(Class<?> entityClass,
            Type genericType, Annotation[] annotations) {
        // NICE optimization: may be cached for speed.
        final List<MessageBodyWriter> mbws = new ArrayList<MessageBodyWriter>();
        for (MessageBodyWriter mbw : this.messageBodyWriters) {
            if (mbw.isWriteable(entityClass, genericType, annotations))
                mbws.add(mbw);
        }
        return new MessageBodyWriterSubSet(mbws);
    }
}