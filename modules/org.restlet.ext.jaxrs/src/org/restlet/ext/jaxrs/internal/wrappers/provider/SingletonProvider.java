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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Providers;

import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.core.CallContext;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * @author Stephan
 * 
 */
public class SingletonProvider extends AbstractProviderWrapper implements
        MessageBodyReader, MessageBodyWriter, ContextResolver {

    /**
     * the {@link ContextResolver}, if this providers is a
     * {@link ContextResolver}
     */
    private final javax.ws.rs.ext.ContextResolver<?> contextResolver;

    private final javax.ws.rs.ext.ExceptionMapper<? extends Throwable> excMapper;

    private final Object jaxRsProvider;

    /**
     * The JAX-RS {@link javax.ws.rs.ext.MessageBodyReader} this wrapper
     * represent.
     */
    private final javax.ws.rs.ext.MessageBodyReader<?> reader;

    private final javax.ws.rs.ext.MessageBodyWriter<Object> writer;

    /**
     * Creates a new wrapper for a ProviderWrapper and initializes the provider.
     * If the given class is not a provider, an {@link IllegalArgumentException}
     * is thrown.
     * 
     * @param jaxRsProvider
     *            the JAX-RS provider class.
     * @param objectFactory
     *            The object factory is responsible for the provider
     *            instantiation, if given.
     * @param allResolvers
     *            all available {@link ContextResolver}s. >>>>>>> .r3440
     * @param logger
     *            the logger to use.
     * @throws IllegalArgumentException
     *             if the class is not a valid provider, may not be instantiated
     *             or what ever.
     * @throws WebApplicationException
     * @see javax.ws.rs.ext.MessageBodyReader
     * @see javax.ws.rs.ext.MessageBodyWriter
     * @see javax.ws.rs.ext.ContextResolver
     */
    /**
     * @param jaxRsProvider
     * @param logger
     *            needed, if the provider implements no provider interface
     * @throws IllegalArgumentException
     * @throws WebApplicationException
     */
    @SuppressWarnings("unchecked")
    public SingletonProvider(Object jaxRsProvider, Logger logger)
            throws IllegalArgumentException, WebApplicationException {
        super((jaxRsProvider == null) ? null : jaxRsProvider.getClass());
        if (jaxRsProvider == null) {
            throw new IllegalArgumentException(
                    "The JAX-RS provider class must not be null");
        }
        this.jaxRsProvider = jaxRsProvider;
        boolean isProvider = false;
        if (jaxRsProvider instanceof javax.ws.rs.ext.MessageBodyWriter) {
            this.writer = (javax.ws.rs.ext.MessageBodyWriter<Object>) jaxRsProvider;
            isProvider = true;
        } else {
            this.writer = null;
        }
        if (jaxRsProvider instanceof javax.ws.rs.ext.MessageBodyReader) {
            this.reader = (javax.ws.rs.ext.MessageBodyReader<?>) jaxRsProvider;
            isProvider = true;
        } else {
            this.reader = null;
        }
        if (jaxRsProvider instanceof javax.ws.rs.ext.ExceptionMapper) {
            this.excMapper = (javax.ws.rs.ext.ExceptionMapper<? extends Throwable>) jaxRsProvider;
            isProvider = true;
        } else {
            this.excMapper = null;
        }
        if (jaxRsProvider instanceof javax.ws.rs.ext.ContextResolver) {
            this.contextResolver = (javax.ws.rs.ext.ContextResolver<?>) jaxRsProvider;
            isProvider = true;
        } else {
            this.contextResolver = null;
        }
        if (!isProvider) {
            logger.config("The provider "
                    + jaxRsProvider.getClass()
                    + " is neither a MessageBodyWriter nor a MessageBodyReader nor a ContextResolver nor an ExceptionMapper");
        }
    }

    /**
     * Checks, if this MessageBodyReader could read the given type.
     * 
     * @param type
     * @param genericType
     * @param annotations
     * @return true, if the wrapped message body reader supports reading for the
     *         given class with the given parameters.
     * @see javax.ws.rs.ext.MessageBodyReader#isReadable(Class, Type,
     *      Annotation[])
     */
    public boolean isReadable(Class<?> type, Type genericType,
            Annotation[] annotations, javax.ws.rs.core.MediaType mediaType) {
        try {
            return this.getJaxRsReader().isReadable(type, genericType,
                    annotations, mediaType);
        } catch (NullPointerException e) {
            if (genericType == null || annotations == null) {
                // interpreted as not readable for the given combination
                return false;
            }
            throw e;
        } catch (IllegalArgumentException e) {
            if (genericType == null || annotations == null) {
                // interpreted as not readable for the given combination
                return false;
            }
            throw e;
        }
    }

    /**
     * Checks, if the given class could be written by this MessageBodyWriter.
     * 
     * @param type
     * @param genericType
     * @param annotations
     * @return true, if the wrapped message writer reader supports writing for
     *         the given class with the given parameters.
     * @see javax.ws.rs.ext.MessageBodyWriter#isWriteable(Class)
     */
    public boolean isWriteable(Class<?> type, Type genericType,
            Annotation[] annotations, javax.ws.rs.core.MediaType mediaType) {
        try {
            return this.getJaxRsWriter().isWriteable(type, genericType,
                    annotations, mediaType);
        } catch (NullPointerException e) {
            if (genericType == null || annotations == null) {
                // interpreted as not writable for the given combination
                return false;
            }
            throw e;
        } catch (IllegalArgumentException e) {
            if (genericType == null || annotations == null) {
                // interpreted as not writable for the given combination
                return false;
            }
            throw e;
        }
    }

    @Override
    public final boolean equals(Object otherProvider) {
        if (this == otherProvider) {
            return true;
        }
        if (!(otherProvider instanceof SingletonProvider)) {
            return false;
        }
        return this.jaxRsProvider.getClass().equals(
                ((SingletonProvider) otherProvider).getClass());
    }

    /**
     * @return the JAX-RS provider class name
     */
    @Override
    public String getClassName() {
        return jaxRsProvider.getClass().getName();
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
    public javax.ws.rs.ext.ExceptionMapper<? extends Throwable> getExcMapper() {
        return this.excMapper;
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getInitializedReader()
     */
    public MessageBodyReader getInitializedReader() {
        return this;
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getInitializedWriter()
     */
    public MessageBodyWriter getInitializedWriter() {
        return this;
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
    public javax.ws.rs.ext.MessageBodyWriter<Object> getJaxRsWriter() {
        return this.writer;
    }

    @Override
    public final int hashCode() {
        return this.jaxRsProvider.hashCode();
    }

    /**
     * 
     * @param type
     * @param genericType
     *            The generic {@link Type} to convert to.
     * @param annotations
     *            the annotations of the artifact to convert to
     * @param mediaType
     * @param httpHeaders
     * @param entityStream
     * @return the read object
     * @throws IOException
     * @see javax.ws.rs.ext.MessageBodyReader#readFrom(Class, Type,
     *      javax.ws.rs.core.MediaType, Annotation[], MultivaluedMap,
     *      InputStream)
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public Object readFrom(Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            CharacterSet characterSet,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, InvocationTargetException {
        try {
            return this.getJaxRsReader().readFrom((Class) type, genericType,
                    annotations, Converter.toJaxRsMediaType(mediaType),
                    httpHeaders, entityStream);
        } catch (Throwable t) {
            if (t instanceof IOException)
                throw (IOException) t;
            if (t instanceof WebApplicationException)
                throw (WebApplicationException) t;

            throw new InvocationTargetException(t);
        }
    }

    /**
     * Write a type to an HTTP response. The response header map is mutable but
     * any changes must be made before writing to the output stream since the
     * headers will be flushed prior to writing the response body.
     * 
     * @param genericType
     *            The generic {@link Type} to convert to.
     * @param annotations
     *            the annotations of the artifact to convert to
     * @param mediaType
     *            the media type of the HTTP entity.
     * @param httpHeaders
     *            a mutable map of the HTTP response headers.
     * @param entityStream
     *            the {@link OutputStream} for the HTTP entity.
     * @param object
     *            the object to write.
     * 
     * @throws java.io.IOException
     *             if an IO error arises
     * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(Object, Type,
     *      Annotation[], javax.ws.rs.core.MediaType, MultivaluedMap,
     *      OutputStream)
     */
    public void writeTo(Object object, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        this.getJaxRsWriter().writeTo(object, type, genericType, annotations,
                Converter.toJaxRsMediaType(mediaType), httpHeaders,
                entityStream);
    }

    /**
     * Injects the supported dependencies into this provider and calls the
     * method annotated with &#64;{@link PostConstruct}.
     * 
     * @param tlContext
     *            The thread local wrapped {@link CallContext}
     * @param allProviders
     *            all providers.
     * @param extensionBackwardMapping
     *            the extension backward mapping
     * @throws InjectException
     * @throws InvocationTargetException
     *             if a bean setter throws an exception
     * @throws IllegalTypeException
     *             if the given class is not valid to be annotated with &#64;
     *             {@link Context}.
     * @see ProviderWrapper#initAtAppStartUp(ThreadLocalizedContext, Providers,
     *      ExtensionBackwardMapping)
     */
    public void initAtAppStartUp(ThreadLocalizedContext tlContext,
            Providers allProviders,
            ExtensionBackwardMapping extensionBackwardMapping)
            throws InjectException, InvocationTargetException,
            IllegalTypeException {
        initProvider(this.jaxRsProvider, tlContext, allProviders,
                extensionBackwardMapping);
    }

    /**
     * Returns true, if this ProviderWrapper is also a
     * {@link javax.ws.rs.ext.ContextResolver}, otherwise false.
     * 
     * @return true, if this ProviderWrapper is also a
     *         {@link javax.ws.rs.ext.ContextResolver}, otherwise false.
     */
    @Override
    public final boolean isContextResolver() {
        return this.contextResolver != null;
    }

    /**
     * Checks, if this provider represents an {@link ExceptionMapper}.
     * 
     * @return true, if this provider is an {@link ExceptionMapper}, or false if
     *         not.
     */
    @Override
    public final boolean isExceptionMapper() {
        return this.excMapper != null;
    }

    /**
     * Returns true, if this ProviderWrapper is also a
     * {@link javax.ws.rs.ext.MessageBodyReader}, otherwise false.
     * 
     * @return true, if this ProviderWrapper is also a
     *         {@link javax.ws.rs.ext.MessageBodyReader}, otherwise false.
     */
    @Override
    public final boolean isReader() {
        return this.reader != null;
    }

    /**
     * Returns true, if this ProviderWrapper is also a
     * {@link javax.ws.rs.ext.MessageBodyWriter}, otherwise false.
     * 
     * @return true, if this ProviderWrapper is also a
     *         {@link javax.ws.rs.ext.MessageBodyWriter}, otherwise false.
     */
    @Override
    public final boolean isWriter() {
        return this.writer != null;
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyWriter#getSize(java.lang.Object,
     *      Class, Type, Annotation[], MediaType)
     */
    public long getSize(Object t, Class<?> type, Type genericType,
            Annotation[] annotations, MediaType mediaType) {
        return this.writer.getSize(t, type, genericType, annotations,
                Converter.toJaxRsMediaType(mediaType));
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getInitializedCtxResolver()
     */
    public ContextResolver getInitializedCtxResolver() {
        return this;
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getInitializedExcMapper()
     */
    public ExceptionMapper<? extends Throwable> getInitializedExcMapper() {
        return excMapper;
    }

    /**
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.ProviderWrapper#getExcMapperType()
     */
    public Class<?> getExcMapperType() {
        return Util.getGenericClass(jaxRsProvider.getClass(),
                ExceptionMapper.class);
    }
}