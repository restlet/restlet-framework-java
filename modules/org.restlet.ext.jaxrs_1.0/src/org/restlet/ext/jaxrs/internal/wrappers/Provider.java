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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MultivaluedMap;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.exceptions.ConvertParameterException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateProviderException;
import org.restlet.ext.jaxrs.internal.exceptions.InstantiateRootRessourceException;
import org.restlet.ext.jaxrs.internal.exceptions.MethodInvokeException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.ext.jaxrs.internal.exceptions.NoMessageBodyReaderException;
import org.restlet.ext.jaxrs.internal.util.Util;

/**
 * Wraps a JAX-RS provider.
 * 
 * @author Stephan Koops
 * @param <T>
 * @see javax.ws.rs.ext.Provider
 */
public class Provider<T> implements MessageBodyReader<T>, MessageBodyWriter<T>,
        ContextResolver<T> {

    /**
     * the mimes this MessageBodyReader consumes.
     */
    private List<org.restlet.data.MediaType> consumedMimes;

    private javax.ws.rs.ext.ContextResolver<T> contextResolver;

    private List<org.restlet.data.MediaType> producedMimes;

    /**
     * The JAX-RS {@link javax.ws.rs.ext.MessageBodyReader} this wrapper
     * represent.
     */
    private javax.ws.rs.ext.MessageBodyReader<T> reader;

    private javax.ws.rs.ext.MessageBodyWriter<T> writer;

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
        if (!jaxRsProviderClass
                .isAnnotationPresent(javax.ws.rs.ext.Provider.class))
            throw new IllegalArgumentException(
                    "A JAX-RS provider class must be annotated with @Provider");
        RootResourceClass.checkClassConcrete(jaxRsProviderClass, "provider");
        Constructor<?> providerConstructor = RootResourceClass
                .findJaxRsConstructor(jaxRsProviderClass);
        Object jaxRsProvider = createInstance(providerConstructor,
                jaxRsProviderClass);
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
     */
    private Object createInstance(Constructor<?> providerConstructor,
            Class<?> jaxRsProviderClass) throws IllegalArgumentException,
            InvocationTargetException {
        try {
            return RootResourceClass.createInstance(providerConstructor, false,
                    null, null);
        } catch (ConvertParameterException e) {
            // should be not possible here
            throw new IllegalArgumentException(
                    "Could not instantiate the Provider, class "
                            + jaxRsProviderClass.getName(), e);
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
        return this.getJaxRsProvider().getClass().equals(
                ((Provider<?>) otherProvider).getJaxRsProvider().getClass());
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

    public T getContext(Class<T> type) {
        return contextResolver.getContext(type);
    }

    private Object getJaxRsProvider() {
        if (this.reader != null)
            return this.reader;
        if (this.writer != null)
            return this.writer;
        return this.contextResolver;
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
        return getJaxRsProvider().hashCode();
    }

    /**
     * Injects the supported dependencies into this provider and calls the
     * method annotated with &#64;{@link PostConstruct}.
     * 
     * @throws InstantiateProviderException
     *                 if the provider could not be instantiated, because the
     *                 method could not be called or because of an Exception
     *                 while calling the post construct method.
     */
    @SuppressWarnings("unused")
    public void init() throws InstantiateProviderException {
        // until now no dependencies possible.
        // Perhaps later some are added.
        Object jaxRsProvider = this.getJaxRsProvider();
        Class<? extends Object> providerClass = jaxRsProvider.getClass();
        Method postConstructMethod = Util
                .findPostConstructMethod(providerClass);
        try {
            Util.invokeNoneArgMethod(jaxRsProvider, postConstructMethod);
        } catch (MethodInvokeException e) {
            InstantiateProviderException ipe = new InstantiateProviderException(
                    e.getMessage());
            ipe.setStackTrace(e.getStackTrace());
            throw ipe;
        } catch (InvocationTargetException e) {
            String message = "Exception while calling " + postConstructMethod;
            throw new InstantiateProviderException(message, e);
        }
    }

    /**
     * Invokes the method annotated with &#64;{@link PreDestroy}.
     * 
     * @throws InvocationTargetException
     * @throws MethodInvokeException
     * 
     */
    public void preDestroy() throws MethodInvokeException,
            InvocationTargetException {
        Object provider = getJaxRsProvider();
        Class<? extends Object> providerClass = provider.getClass();
        Method preDestroxMethod = Util.findPreDestroyMethod(providerClass);
        Util.invokeNoneArgMethod(provider, preDestroxMethod);
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
        return this.getJaxRsProvider().getClass().getName();
    }

    /**
     * Write a type to an HTTP response. The response header map is mutable but
     * any changes must be made before writing to the output stream since the
     * headers will be flushed prior to writing the response body.
     * 
     * REQUESTED JSR311: MessageBodyWriter: the response headers MAY BE mutable?
     * 
     * @param t
     *                the type to write.
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
     * @throws java.io.IOException
     *                 if an IO error arises
     * @see javax.ws.rs.ext.MessageBodyWriter#writeTo(Object, Type,
     *      Annotation[], javax.ws.rs.core.MediaType, MultivaluedMap,
     *      OutputStream)
     */
    public void writeTo(T t, Type genericType, Annotation[] annotations,
            javax.ws.rs.core.MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders,
            OutputStream entityStream) throws IOException {
        writer.writeTo(t, genericType, annotations, mediaType, httpHeaders,
                entityStream);
    }
}