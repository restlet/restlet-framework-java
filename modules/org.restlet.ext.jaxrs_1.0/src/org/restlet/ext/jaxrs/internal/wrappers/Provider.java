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
package org.restlet.ext.jaxrs.internal.wrappers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.ContextResolver;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertCookieParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertHeaderParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertMatrixParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertPathParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertQueryParamException;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertRepresentationException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateRootRessourceException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Wraps a JAX-RS provider, see chapter 4 of JAX-RS specification.
 * 
 * @author Stephan Koops
 * @param <T>
 *                the java type to convert.
 * @see javax.ws.rs.ext.Provider
 */
public class Provider<T> implements MessageBodyReader<T>, MessageBodyWriter<T>,
        org.restlet.ext.jaxrs.internal.wrappers.ContextResolver<T> {

    /**
     * the mimes this MessageBodyReader consumes.
     */
    private List<org.restlet.data.MediaType> consumedMimes;

    /**
     * the {@link ContextResolver}, if this providers is a
     * {@link ContextResolver}
     */
    private javax.ws.rs.ext.ContextResolver<T> contextResolver;

    private List<org.restlet.data.MediaType> producedMimes;

    /**
     * The JAX-RS {@link javax.ws.rs.ext.MessageBodyReader} this wrapper
     * represent.
     */
    private javax.ws.rs.ext.MessageBodyReader<T> reader;

    private javax.ws.rs.ext.MessageBodyWriter<T> writer;

    private Object jaxRsProvider;

    /**
     * Construct a wrapper for a Provider
     * 
     * @param jaxRsProviderClass
     *                the JAX-RS provider class.
     * @throws IllegalArgumentException
     *                 if the class is not a valid provider, may not be
     *                 instantiated or what ever.
     * @throws InvocationTargetException
     *                 if the constructor throws an Throwable
     * @see javax.ws.rs.ext.MessageBodyReader
     * @see javax.ws.rs.ext.MessageBodyWriter
     * @see javax.ws.rs.ext.ContextResolver
     */
    @SuppressWarnings("unchecked")
    public Provider(Class<?> jaxRsProviderClass)
            throws IllegalArgumentException, InvocationTargetException {
        if (jaxRsProviderClass == null)
            throw new IllegalArgumentException(
                    "The JAX-RS provider class must not be null");
        RootResourceClass.checkClassConcrete(jaxRsProviderClass, "provider");
        Constructor<?> providerConstructor = RootResourceClass
                .findJaxRsConstructor(jaxRsProviderClass);
        try {
            this.jaxRsProvider = createInstance(providerConstructor,
                    jaxRsProviderClass);
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
        boolean isProvider = false;
        if (jaxRsProvider instanceof javax.ws.rs.ext.MessageBodyWriter) {
            this.writer = (javax.ws.rs.ext.MessageBodyWriter<T>) jaxRsProvider;
            isProvider = true;
        }
        if (jaxRsProvider instanceof javax.ws.rs.ext.MessageBodyReader) {
            this.reader = (javax.ws.rs.ext.MessageBodyReader<T>) jaxRsProvider;
            isProvider = true;
        }
        if (jaxRsProvider instanceof javax.ws.rs.ext.ContextResolver) {
            this.contextResolver = (javax.ws.rs.ext.ContextResolver<T>) jaxRsProvider;
            isProvider = true;
        }
        if (!isProvider) {
            throw new IllegalArgumentException(
                    "The given JAX-RS Provider is neither a MessageBodyWriter nor a MessageBodyReader nor a ContextResolver");
        }
    }

    /**
     * @param providerConstructor
     *                the constructor to use.
     * @param jaxRsProviderClass
     *                class for exception message.
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     *                 if the constructor throws an Throwable
     * @throws ConvertCookieParamException
     * @throws ConvertQueryParamException
     * @throws ConvertMatrixParamException
     * @throws ConvertPathParamException
     * @throws ConvertHeaderParamException
     * @throws ConvertRepresentationException
     */
    private Object createInstance(Constructor<?> providerConstructor,
            Class<?> jaxRsProviderClass) throws IllegalArgumentException,
            InvocationTargetException, ConvertRepresentationException,
            ConvertHeaderParamException, ConvertPathParamException,
            ConvertMatrixParamException, ConvertQueryParamException,
            ConvertCookieParamException {
        try {
            return RootResourceClass.createInstance(providerConstructor, false,
                    null, null, null);
        } catch (MissingAnnotationException e) {
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (InstantiateRootRessourceException e) {
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
        } catch (NoMessageBodyReaderException e) {
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
        if (consumedMimes == null) {
            ConsumeMime pm = reader.getClass().getAnnotation(ConsumeMime.class);
            if (pm != null)
                this.consumedMimes = ResourceMethod.convertToMediaTypes(pm
                        .value());
            else
                this.consumedMimes = Collections.singletonList(MediaType.ALL);
        }
        return consumedMimes;
    }

    public T getContext(Class<?> type) {
        return contextResolver.getContext(type);
    }

    /**
     * Returns the list of produced {@link MediaType}s of the wrapped
     * {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return List of produced {@link MediaType}s.
     */
    public List<MediaType> getProducedMimes() {
        if (producedMimes == null) {
            ProduceMime pm = writer.getClass().getAnnotation(ProduceMime.class);
            if (pm != null) {
                String[] pmStr = pm.value();
                this.producedMimes = ResourceMethod.convertToMediaTypes(pmStr);
            } else {
                this.producedMimes = Collections.singletonList(MediaType.ALL);
            }
        }
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
     * @param allResolvers
     *                all available wrapped {@link ContextResolver}s.
     * @throws InjectException
     */
    @SuppressWarnings("unused")
    public void init(
            Collection<org.restlet.ext.jaxrs.internal.wrappers.ContextResolver<?>> allResolvers)
            throws InjectException {
        injectContext(allResolvers);
    }

    /**
     * Inject the values fields for &#64;{@link Context}.
     * 
     * @param allResolvers
     *                all available wrapped {@link ContextResolver}s.
     * @throws InjectException
     */
    private void injectContext(
            Collection<org.restlet.ext.jaxrs.internal.wrappers.ContextResolver<?>> allResolvers)
            throws InjectException {
        Class<? extends Object> providerClass = this.jaxRsProvider.getClass();
        do {
            for (Field field : providerClass.getDeclaredFields()) {
                if (!field.isAnnotationPresent(Context.class))
                    continue;
                Class<?> fieldType = field.getType();
                if (fieldType.equals(ContextResolver.class)) {
                    field.setAccessible(true);
                    ContextResolver<?> injectCR;
                    jaxRsProvider.toString(); // avoid trouble, don't ask why
                    injectCR = WrapperUtil.getContextResolver(field,
                            allResolvers);
                    Util.inject(this.jaxRsProvider, field, injectCR);
                } else if (fieldType.equals(MessageBodyWorkers.class)) {
                    field.setAccessible(true);
                    Object toInject = null;
                    // TODO inject MessageBodyWorker to provider
                    Util.inject(this.jaxRsProvider, field, toInject);
                }
            }
            providerClass = providerClass.getSuperclass();
        } while (providerClass != null);
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
        return this.reader.readFrom(type, genericType, mediaType, annotations,
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
            for (MediaType requested : mediaTypes)
                if (requested.isCompatible(produced))
                    return true;
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

    /**
     * Returns the JAX-RS provider as {@link ContextResolver}, if the provider
     * is a ContextResolver, otherwise null.
     * 
     * @return
     * @see org.restlet.ext.jaxrs.internal.wrappers.ContextResolver#getJaxRsContextResolver()
     */
    public javax.ws.rs.ext.ContextResolver<?> getJaxRsContextResolver() {
        return this.contextResolver;
    }
}