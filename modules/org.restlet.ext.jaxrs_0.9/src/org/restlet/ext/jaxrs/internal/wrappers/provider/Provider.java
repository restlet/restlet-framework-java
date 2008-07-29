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
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Providers;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.InstantiateException;
import org.restlet.ext.jaxrs.ObjectFactory;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalConstrParamTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingConstructorException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil;
import org.restlet.ext.jaxrs.internal.wrappers.params.ContextInjector;
import org.restlet.ext.jaxrs.internal.wrappers.params.ParameterList;

/**
 * Wraps a JAX-RS provider, see chapter 4 of JAX-RS specification.
 * 
 * @author Stephan Koops
 * @see javax.ws.rs.ext.Provider
 */
public class Provider implements MessageBodyReader, MessageBodyWriter {
    /**
     * the mimes this MessageBodyReader consumes.
     */
    private final List<org.restlet.data.MediaType> consumedMimes;

    /**
     * the {@link ContextResolver}, if this providers is a
     * {@link ContextResolver}
     */
    private final javax.ws.rs.ext.ContextResolver<?> contextResolver;

    private final javax.ws.rs.ext.ExceptionMapper<Object> excMapper;

    private final Object jaxRsProvider;

    private final List<org.restlet.data.MediaType> producedMimes;

    /**
     * The JAX-RS {@link javax.ws.rs.ext.MessageBodyReader} this wrapper
     * represent.
     */
    private final javax.ws.rs.ext.MessageBodyReader<Object> reader;

    private final boolean singelton = true;

    private final javax.ws.rs.ext.MessageBodyWriter<Object> writer;

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
     * @param allProviders
     *                all entity providers. <<<<<<< .mine =======
     * @param allResolvers
     *                all available {@link ContextResolver}s. >>>>>>> .r3440
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
     * @throws IllegalConstrParamTypeException
     * @see javax.ws.rs.ext.MessageBodyReader
     * @see javax.ws.rs.ext.MessageBodyWriter
     * @see javax.ws.rs.ext.ContextResolver
     */
    @SuppressWarnings("unchecked")
    public Provider(Class<?> jaxRsProviderClass, ObjectFactory objectFactory,
            ThreadLocalizedContext tlContext, JaxRsProviders allProviders,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, InvocationTargetException,
            MissingConstructorException, InstantiateException,
            MissingAnnotationException, WebApplicationException,
            IllegalConstrParamTypeException {
        if (jaxRsProviderClass == null) {
            throw new IllegalArgumentException(
                    "The JAX-RS provider class must not be null");
        }
        Util.checkClassConcrete(jaxRsProviderClass, "provider");
        Object jaxRsProvider = null;
        if (objectFactory != null) {
            jaxRsProvider = objectFactory.getInstance(jaxRsProviderClass);
        }
        if (jaxRsProvider == null) {
            final Constructor<?> providerConstructor = WrapperUtil
                    .findJaxRsConstructor(jaxRsProviderClass, "provider");
            jaxRsProvider = createInstance(providerConstructor,
                    jaxRsProviderClass, tlContext, allProviders,
                    extensionBackwardMapping, logger);
        }
        this.jaxRsProvider = jaxRsProvider;
        boolean isProvider = false;
        if (jaxRsProvider instanceof javax.ws.rs.ext.MessageBodyWriter) {
            this.writer = (javax.ws.rs.ext.MessageBodyWriter) jaxRsProvider;
            isProvider = true;
        } else {
            this.writer = null;
        }
        if (jaxRsProvider instanceof javax.ws.rs.ext.MessageBodyReader) {
            this.reader = (javax.ws.rs.ext.MessageBodyReader) jaxRsProvider;
            isProvider = true;
        } else {
            this.reader = null;
        }
        if (jaxRsProvider instanceof javax.ws.rs.ext.ExceptionMapper) {
            this.excMapper = (javax.ws.rs.ext.ExceptionMapper) jaxRsProvider;
            isProvider = true;
        } else {
            this.excMapper = null;
        }
        if (jaxRsProvider instanceof javax.ws.rs.ext.ContextResolver) {
            this.contextResolver = (javax.ws.rs.ext.ContextResolver) jaxRsProvider;
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
        final Consumes pm = jaxRsProvider.getClass().getAnnotation(
                Consumes.class);
        if (pm != null) {
            this.consumedMimes = WrapperUtil.convertToMediaTypes(pm.value());
        } else {
            this.consumedMimes = Collections.singletonList(MediaType.ALL);
        }

        final Produces cm = jaxRsProvider.getClass().getAnnotation(
                Produces.class);
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
     * @param allProviders
     *                all entity providers. <<<<<<< .mine =======
     * @param allResolvers
     *                all available {@link ContextResolver}s. >>>>>>> .r3440
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
     * @throws IllegalConstrParamTypeException
     *                 if one of the fields or bean setters annotated with &#64;
     *                 {@link Context} has a type that must not be annotated
     *                 with &#64;{@link Context}.
     */
    private Object createInstance(Constructor<?> providerConstructor,
            Class<?> jaxRsProviderClass, ThreadLocalizedContext tlContext,
            JaxRsProviders allProviders,
            ExtensionBackwardMapping extensionBackwardMapping, Logger logger)
            throws IllegalArgumentException, InvocationTargetException,
            InstantiateException, MissingAnnotationException,
            WebApplicationException, IllegalConstrParamTypeException {
        ParameterList parameters;
        try {
            parameters = new ParameterList(providerConstructor, tlContext,
                    false, allProviders, extensionBackwardMapping, false,
                    logger, !singelton);
        } catch (IllegalTypeException ite) {
            throw new IllegalConstrParamTypeException(ite);
        }
        try {
            final Object[] args = parameters.get();
            return WrapperUtil.createInstance(providerConstructor, args);
        } catch (final NoMessageBodyReaderException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (final ConvertRepresentationException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (final ConvertHeaderParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (final ConvertPathParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (final ConvertMatrixParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (final ConvertQueryParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (final ConvertCookieParamException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        }
    }

    @Override
    public final boolean equals(Object otherProvider) {
        if (this == otherProvider) {
            return true;
        }
        if (!(otherProvider instanceof Provider)) {
            return false;
        }
        return this.jaxRsProvider.getClass().equals(
                ((Provider) otherProvider).jaxRsProvider.getClass());
    }

    /**
     * Returns the list of produced {@link MediaType}s of the wrapped
     * {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return List of produced {@link MediaType}s.
     */
    public List<MediaType> getConsumedMimes() {
        return this.consumedMimes;
    }

    /**
     * @return the contextResolver
     */
    public javax.ws.rs.ext.ContextResolver<?> getContextResolver() {
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
    public javax.ws.rs.ext.ExceptionMapper<? extends Throwable> getExcMapper() {
        return (javax.ws.rs.ext.ExceptionMapper) this.excMapper;
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyReader#getJaxRsReader()
     */
    public javax.ws.rs.ext.MessageBodyReader<?> getJaxRsReader() {
        return this.reader;
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyWriter#getJaxRsWriter()
     */
    public javax.ws.rs.ext.MessageBodyWriter<?> getJaxRsWriter() {
        return this.writer;
    }

    /**
     * Returns the list of produced {@link MediaType}s of the wrapped
     * {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return List of produced {@link MediaType}s. If the entity provider is
     *         not annotated with &#64; {@link Produces}, '*<!---->/*' is
     *         returned.
     */
    public List<MediaType> getProducedMimes() {
        return this.producedMimes;
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
    public long getSize(Object o) {
        return this.writer.getSize(o);
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
     * @param allProviders
     *                all entity providers. <<<<<<< .mine =======
     * @param allResolvers
     *                all available {@link ContextResolver}s. >>>>>>> .r3440
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @throws InjectException
     * @throws InvocationTargetException
     *                 if a bean setter throws an exception
     * @throws IllegalTypeException
     *                 if the given class is not valid to be annotated with
     *                 &#64; {@link Context}.
     */
    public void init(ThreadLocalizedContext tlContext, Providers allProviders,
            ExtensionBackwardMapping extensionBackwardMapping)
            throws InjectException, InvocationTargetException,
            IllegalTypeException {
        injectContexts(tlContext, allProviders, extensionBackwardMapping);
    }

    /**
     * Inject the values fields for &#64;{@link Context}.
     * 
     * @param tlContext
     *                The thread local wrapped {@link CallContext}
     * @param allProviders
     *                all entity providers. <<<<<<< .mine =======
     * @param allResolvers
     *                all available {@link ContextResolver}s. >>>>>>> .r3440
     * @param extensionBackwardMapping
     *                the extension backward mapping
     * @throws InjectException
     * @throws InvocationTargetException
     *                 if a bean setter throws an exception
     * @throws IllegalTypeException
     * @throws IllegalTypeException
     *                 if the given class is not valid to be annotated with
     *                 &#64; {@link Context}.
     */
    private void injectContexts(ThreadLocalizedContext tlContext,
            Providers allProviders,
            ExtensionBackwardMapping extensionBackwardMapping)
            throws InjectException, InvocationTargetException,
            IllegalTypeException {
        final Class<? extends Object> providerClass = this.jaxRsProvider
                .getClass();
        final ContextInjector iph = new ContextInjector(providerClass,
                tlContext, allProviders, extensionBackwardMapping);
        iph.injectInto(this.jaxRsProvider, !this.singelton);
    }

    // TODO before a call of a message body reader or writer the current state
    // of the matched resources and URIs must be stored fpr the current thread.

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
        return this.excMapper != null;
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
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return this.reader.isReadable(type, genericType, annotations);
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
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations) {
        return this.writer.isWriteable(type, genericType, annotations);
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
     * @param annotations
     *                the annotations of the artefact to convert to
     * @param mediaType
     * @param httpHeaders
     * @param entityStream
     * @return
     * @throws IOException
     * @see javax.ws.rs.ext.MessageBodyReader#readFrom(Class, Type,
     *      javax.ws.rs.core.MediaType, Annotation[], MultivaluedMap,
     *      InputStream)
     */
    @SuppressWarnings("unchecked")
    public Object readFrom(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            CharacterSet characterSet, MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException {
        return this.reader.readFrom((Class)type, genericType, annotations, Converter
                .toJaxRsMediaType(mediaType), httpHeaders, entityStream);
    }

    /**
     * Checks, if this MessageBodyReader supports the given MediaType.
     * 
     * @param mediaType
     * @return
     */
    public boolean supportsRead(MediaType mediaType) {
        for (final MediaType cm : getConsumedMimes()) {
            if (cm.isCompatible(mediaType)) {
                return true;
            }
        }
        return false;
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
    public boolean supportsWrite(Iterable<MediaType> mediaTypes) {
        for (final MediaType produced : getProducedMimes()) {
            for (final MediaType requested : mediaTypes) {
                if (requested.isCompatible(produced)) {
                    return true;
                }
            }
        }
        return false;
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
    public boolean supportsWrite(MediaType requested) {
        for (final MediaType produced : getProducedMimes()) {
            if (requested.isCompatible(produced)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param exception 
     * @return 
     * @see javax.ws.rs.ext.ExceptionMapper#toResponse(java.lang.Object)
     */
    public Response toResponse(Throwable exception) {
        return this.excMapper.toResponse(exception);
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
    public void writeTo(Object object, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        this.writer.writeTo(object, type, genericType, annotations, Converter
                .toJaxRsMediaType(mediaType), httpHeaders, entityStream);
    }

    /**
     * @return the JAX-RS provider class name
     */
    public String getClassName() {
        return jaxRsProvider.getClass().getName();
    }
}