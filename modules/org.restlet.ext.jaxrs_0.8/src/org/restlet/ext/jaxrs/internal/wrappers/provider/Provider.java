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
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.MessageBodyWorkers;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.ObjectFactory;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil;
import org.restlet.ext.jaxrs.internal.wrappers.params.ContextInjector;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList;

/**
 * Wraps a JAX-RS provider, see chapter 4 of JAX-RS specification.
 * 
 * @author Stephan Koops
 * @param <T>
 *                the java type to convert.
 * @see javax.ws.rs.ext.Provider
 */
public class Provider<T> implements MessageBodyReader<T>, MessageBodyWriter<T>,
        ExceptionMapper<T> {

    /**
     * the mimes this MessageBodyReader consumes.
     */
    private final List<org.restlet.data.MediaType> consumedMimes;

    /**
     * the {@link ContextResolver}, if this providers is a
     * {@link ContextResolver}
     */
    private final javax.ws.rs.ext.ContextResolver<T> contextResolver;

    private final javax.ws.rs.ext.ExceptionMapper<T> excMapper;

    private final Object jaxRsProvider;

    private final List<org.restlet.data.MediaType> producedMimes;

    /**
     * The JAX-RS {@link javax.ws.rs.ext.MessageBodyReader} this wrapper
     * represent.
     */
    private final javax.ws.rs.ext.MessageBodyReader<T> reader;

    private final javax.ws.rs.ext.MessageBodyWriter<T> writer;

    /**
     * Creates a new wrapper for a Provider and initializes the provider. If the
     * given class is not a provider, an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @param jaxRsProviderClass
     *                the JAX-RS provider class.
     * @param objectFactory
     *                The object factory is responsible for the provider
     *                instantiation, if given.
     * @param tlContext
     *                The tread local wrapped call context
     * @param mbWorkers
     *                all entity providers.
     * @param allResolvers
     *                all available {@link ContextResolver}s.
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @param logger
     *                the logger to use.
     * @throws IllegalArgumentException
     *                 if the class is not a valid provider, may not be
     *                 instantiated or what ever.
     * @throws InvocationTargetException
     *                 if the constructor throws an Throwable
     * @throws MissingConstructorException
     *                 if no valid constructor could be found
     * @throws InstantiateException
     * @throws WebApplicationException
     * @throws MissingAnnotationException
     * @see javax.ws.rs.ext.MessageBodyReader
     * @see javax.ws.rs.ext.MessageBodyWriter
     * @see javax.ws.rs.ext.ContextResolver
     */
    @SuppressWarnings("unchecked")
    public Provider(Class<?> jaxRsProviderClass, ObjectFactory objectFactory,
            ThreadLocalizedContext tlContext, EntityProviders mbWorkers,
            Collection<ContextResolver<?>> allResolvers,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, InvocationTargetException,
            MissingConstructorException, InstantiateException,
            MissingAnnotationException, WebApplicationException {
        if (jaxRsProviderClass == null)
            throw new IllegalArgumentException(
                    "The JAX-RS provider class must not be null");
        Util.checkClassConcrete(jaxRsProviderClass, "provider");
        Object jaxRsProvider = null;
        if (objectFactory != null)
            jaxRsProvider = objectFactory.getInstance(jaxRsProviderClass);
        if (jaxRsProvider == null) {
            Constructor<?> providerConstructor = WrapperUtil
                    .findJaxRsConstructor(jaxRsProviderClass, "provider");
            jaxRsProvider = createInstance(providerConstructor,
                    jaxRsProviderClass, tlContext, mbWorkers, allResolvers,
                    extensionBackwardMapping, logger);
        }
        this.jaxRsProvider = jaxRsProvider;
        boolean isProvider = false;
        if (jaxRsProvider instanceof javax.ws.rs.ext.MessageBodyWriter) {
            this.writer = (javax.ws.rs.ext.MessageBodyWriter<T>) jaxRsProvider;
            isProvider = true;
        } else {
            this.writer = null;
        }
        if (jaxRsProvider instanceof javax.ws.rs.ext.MessageBodyReader) {
            this.reader = (javax.ws.rs.ext.MessageBodyReader<T>) jaxRsProvider;
            isProvider = true;
        } else {
            this.reader = null;
        }
        if (jaxRsProvider instanceof javax.ws.rs.ext.ExceptionMapper) {
            this.excMapper = (javax.ws.rs.ext.ExceptionMapper<T>) jaxRsProvider;
            isProvider = true;
        } else {
            this.excMapper = null;
        }
        if (jaxRsProvider instanceof javax.ws.rs.ext.ContextResolver) {
            this.contextResolver = (javax.ws.rs.ext.ContextResolver<T>) jaxRsProvider;
            isProvider = true;
        } else {
            this.contextResolver = null;
        }
        if (!isProvider) {
            logger
                    .config("The provider "
                            + jaxRsProviderClass
                            + " is neither a MessageBodyWriter nor a MessageBodyReader nor a ContextResolver nor an ExceptionMapper");
        }
        ConsumeMime pm = jaxRsProvider.getClass().getAnnotation(
                ConsumeMime.class);
        if (pm != null)
            this.consumedMimes = WrapperUtil.convertToMediaTypes(pm.value());
        else
            this.consumedMimes = Collections.singletonList(MediaType.ALL);

        ProduceMime cm = jaxRsProvider.getClass().getAnnotation(
                ProduceMime.class);
        if (cm != null) {
            this.producedMimes = WrapperUtil.convertToMediaTypes(cm.value());
        } else {
            this.producedMimes = Collections.singletonList(MediaType.ALL);
        }
    }

    /**
     * @param providerConstructor
     *                the constructor to use.
     * @param jaxRsProviderClass
     *                class for exception message.
     * @param tlContext
     *                The tread local wrapped call context
     * @param mbWorkers
     *                all entity providers.
     * @param allResolvers
     *                all available {@link ContextResolver}s.
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @param logger
     *                the logger to use
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     *                 if the constructor throws an Throwable
     * @throws InstantiateException
     * @throws MissingAnnotationException
     * @throws WebApplicationException
     */
    private Object createInstance(Constructor<?> providerConstructor,
            Class<?> jaxRsProviderClass, ThreadLocalizedContext tlContext,
            EntityProviders mbWorkers,
            Collection<ContextResolver<?>> allResolvers,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, InvocationTargetException,
            InstantiateException, MissingAnnotationException,
            WebApplicationException {
        ParameterList parameters = new ParameterList(providerConstructor,
                tlContext, false, mbWorkers, allResolvers,
                extensionBackwardMapping, false, logger);
        try {
            Object[] args = parameters.get();
            return WrapperUtil.createInstance(providerConstructor, args);
        } catch (NoMessageBodyReaderException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (ConvertRepresentationException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (ConvertHeaderParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (ConvertPathParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (ConvertMatrixParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (ConvertQueryParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (ConvertCookieParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        }
    }

    @Override
    public final boolean equals(Object otherProvider) {
        if (this == otherProvider)
            return true;
        if (!(otherProvider instanceof Provider))
            return false;
        return this.jaxRsProvider.getClass().equals(
                ((Provider<?>) otherProvider).jaxRsProvider.getClass());
    }

    /**
     * Returns the list of produced {@link MediaType}s of the wrapped
     * {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return List of produced {@link MediaType}s.
     */
    public List<MediaType> getConsumedMimes() {
        return consumedMimes;
    }

    /**
     * @return the contextResolver
     */
    public javax.ws.rs.ext.ContextResolver<T> getContextResolver() {
        return this.contextResolver;
    }

    /**
     * Returns the {@link ExceptionMapper}, or null, if this provider is not an
     * {@link ExceptionMapper}.
     * 
     * @return the {@link ExceptionMapper}, or null, if this provider is not an
     *         {@link ExceptionMapper}.
     */
    @SuppressWarnings("unchecked")
    public ExceptionMapper<? extends Throwable> getExcMapper() {
        return (ExceptionMapper) excMapper;
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyReader#getJaxRsReader()
     */
    public javax.ws.rs.ext.MessageBodyReader<T> getJaxRsReader() {
        return this.reader;
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyWriter#getJaxRsWriter()
     */
    public javax.ws.rs.ext.MessageBodyWriter<T> getJaxRsWriter() {
        return this.writer;
    }

    /**
     * Returns the list of produced {@link MediaType}s of the wrapped
     * {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return List of produced {@link MediaType}s.
     */
    public List<MediaType> getProducedMimes() {
        return producedMimes;
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
    public long getSize(T t) {
        return writer.getSize(t);
    }

    @Override
    public final int hashCode() {
        return this.jaxRsProvider.hashCode();
    }

    /**
     * Injects the supported dependencies into this provider and calls the
     * method annotated with &#64;{@link PostConstruct}.
     * 
     * @param tlContext
     *                The thread local wrapped {@link CallContext}
     * @param mbWorkers
     *                all entity providers.
     * @param allResolvers
     *                all available {@link ContextResolver}s.
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @throws InjectException
     * @throws InvocationTargetException
     *                 if a bean setter throws an exception
     */
    public void init(ThreadLocalizedContext tlContext,
            MessageBodyWorkers mbWorkers,
            Collection<ContextResolver<?>> allResolvers,
            ExtensionBackwardMapping extensionBackwardMapping)
            throws InjectException, InvocationTargetException {
        injectContexts(tlContext, mbWorkers, allResolvers,
                extensionBackwardMapping);
    }

    /**
     * Inject the values fields for &#64;{@link Context}.
     * 
     * @param tlContext
     *                The thread local wrapped {@link CallContext}
     * @param mbWorkers
     *                all entity providers.
     * @param allResolvers
     *                all available {@link ContextResolver}s.
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @throws InjectException
     * @throws InvocationTargetException
     *                 if a bean setter throws an exception
     */
    private void injectContexts(ThreadLocalizedContext tlContext,
            MessageBodyWorkers mbWorkers,
            Collection<ContextResolver<?>> allResolvers,
            ExtensionBackwardMapping extensionBackwardMapping)
            throws InjectException, InvocationTargetException {
        Class<? extends Object> providerClass = this.jaxRsProvider.getClass();
        ContextInjector iph = new ContextInjector(providerClass, tlContext,
                mbWorkers, allResolvers, extensionBackwardMapping);
        iph.injectInto(this.jaxRsProvider);
    }

    /**
     * Returns true, if this Provider is also a
     * {@link javax.ws.rs.ext.ContextResolver}, otherwise false.
     * 
     * @return true, if this Provider is also a
     *         {@link javax.ws.rs.ext.ContextResolver}, otherwise false.
     */
    public boolean isContextResolver() {
        return this.contextResolver != null;
    }

    /**
     * Checks, if this provider represents an {@link ExceptionMapper}.
     * 
     * @return true, if this provider is an {@link ExceptionMapper}, or false
     *         if not.
     */
    public boolean isExceptionMapper() {
        return excMapper != null;
    }

    /**
     * Checks, if this MessageBodyReader could read the given type.
     * 
     * @param type
     * @param genericType
     * @param annotations
     * @return
     * @see javax.ws.rs.ext.MessageBodyReader#isReadable(Class, Type,
     *      Annotation[])
     */
    public boolean isReadable(Class<T> type, Type genericType,
            Annotation[] annotations) {
        return reader.isReadable(type, genericType, annotations);
    }

    /**
     * Returns true, if this Provider is also a
     * {@link javax.ws.rs.ext.MessageBodyReader}, otherwise false.
     * 
     * @return true, if this Provider is also a
     *         {@link javax.ws.rs.ext.MessageBodyReader}, otherwise false.
     */
    public boolean isReader() {
        return this.reader != null;
    }

    /**
     * Checks, if the given class could be written by this MessageBodyWriter.
     * 
     * @param type
     * @param genericType
     * @param annotations
     * @return
     * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(Class)
     */
    public boolean isWriteable(Class<T> type, Type genericType,
            Annotation[] annotations) {
        return writer.isWriteable(type, genericType, annotations);
    }

    /**
     * Returns true, if this Provider is also a
     * {@link javax.ws.rs.ext.MessageBodyWriter}, otherwise false.
     * 
     * @return true, if this Provider is also a
     *         {@link javax.ws.rs.ext.MessageBodyWriter}, otherwise false.
     */
    public boolean isWriter() {
        return this.writer != null;
    }

    /**
     * 
     * @param type
     * @param genericType
     *                The generic {@link Type} to convert to.
     * @param mediaType
     * @param annotations
     *                the annotations of the artefact to convert to
     * @param httpHeaders
     * @param entityStream
     * @return
     * @throws IOException
     * @see javax.ws.rs.ext.MessageBodyReader#readFrom(Class, Type,
     *      javax.ws.rs.core.MediaType, Annotation[], MultivaluedMap,
     *      InputStream)
     */
    public T readFrom(Class<T> type, Type genericType,
            javax.ws.rs.core.MediaType mediaType, Annotation[] annotations,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        return this.reader.readFrom(type, genericType, annotations, mediaType,
                httpHeaders, entityStream);
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
    public boolean supportAtLeastOne(Iterable<MediaType> mediaTypes) {
        for (MediaType produced : getProducedMimes()) {
            for (MediaType requested : mediaTypes) {
                if (requested.isCompatible(produced))
                    return true;
            }
        }
        return false;
    }

    /**
     * Checks, if this MessageBodyReader supports the given MediaType.
     * 
     * @param mediaType
     * @return
     */
    public boolean supports(MediaType mediaType) {
        for (MediaType cm : getConsumedMimes()) {
            if (cm.isCompatible(mediaType))
                return true;
        }
        return false;
    }

    /**
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Object)
     */
    public Response toResponse(T exception) {
        return excMapper.toResponse(exception);
    }

    @Override
    public String toString() {
        return this.jaxRsProvider.getClass().getName();
    }

    /**
     * Write a type to an HTTP response. The response header map is mutable but
     * any changes must be made before writing to the output stream since the
     * headers will be flushed prior to writing the response body.
     * 
     * @param genericType
     *                The generic {@link Type} to convert to.
     * @param annotations
     *                the annotations of the artefact to convert to
     * @param mediaType
     *                the media type of the HTTP entity.
     * @param httpHeaders
     *                a mutable map of the HTTP response headers.
     * @param entityStream
     *                the {@link OutputStream} for the HTTP entity.
     * @param object
     *                the object to write.
     * 
     * @throws java.io.IOException
     *                 if an IO error arises
     * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(Object, Type,
     *      Annotation[], javax.ws.rs.core.MediaType, MultivaluedMap,
     *      OutputStream)
     */
    public void writeTo(T object, Class<?> type, Type genericType,
            Annotation[] annotations, javax.ws.rs.core.MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        writer.writeTo(object, type, genericType, annotations, mediaType,
                httpHeaders, entityStream);
    }
}