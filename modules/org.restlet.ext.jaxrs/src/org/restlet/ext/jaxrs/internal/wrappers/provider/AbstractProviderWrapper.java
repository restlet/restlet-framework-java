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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Providers;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalBeanSetterTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalFieldTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.util.Util;
import org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil;
import org.restlet.ext.jaxrs.internal.wrappers.params.ContextInjector;

/**
 * Wraps a JAX-RS provider, see chapter 4 of JAX-RS specification.
 * 
 * @author Stephan Koops
 * @see javax.ws.rs.ext.Provider
 */
abstract class AbstractProviderWrapper implements ProviderWrapper {
    /**
     * the mimes this MessageBodyReader consumes.
     */
    private final List<org.restlet.data.MediaType> consumedMimes;

    private final List<org.restlet.data.MediaType> producedMimes;

    private final Class<?> genericMbrType;

    private final Class<?> genericMbwType;

    /**
     * Creates a new wrapper for a Provider and initializes the provider. If the
     * given class is not a provider, an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @param jaxRsProviderClass
     *            the JAX-RS provider class.
     * @throws IllegalArgumentException
     *             if the class is not a valid provider, may not be instantiated
     *             or what ever.
     * @throws WebApplicationException
     * @see javax.ws.rs.ext.MessageBodyReader
     * @see javax.ws.rs.ext.MessageBodyWriter
     * @see javax.ws.rs.ext.ContextResolver
     */
    AbstractProviderWrapper(Class<?> jaxRsProviderClass)
            throws IllegalArgumentException, WebApplicationException {
        final Consumes pm = jaxRsProviderClass.getAnnotation(Consumes.class);
        if (pm != null) {
            this.consumedMimes = WrapperUtil.convertToMediaTypes(pm.value());
        } else {
            this.consumedMimes = Collections.singletonList(MediaType.ALL);
        }

        final Produces cm = jaxRsProviderClass.getAnnotation(Produces.class);
        if (cm != null) {
            this.producedMimes = WrapperUtil.convertToMediaTypes(cm.value());
        } else {
            this.producedMimes = Collections.singletonList(MediaType.ALL);
        }

        this.genericMbrType = Util.getGenericClass(jaxRsProviderClass,
                javax.ws.rs.ext.MessageBodyReader.class);
        this.genericMbwType = Util.getGenericClass(jaxRsProviderClass,
                javax.ws.rs.ext.MessageBodyWriter.class);
        // LATER use Type instead of Class here
    }

    @Override
    public abstract boolean equals(Object otherProvider);

    /**
     * @return the JAX-RS provider class name
     */
    public abstract String getClassName();

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

    @Override
    public abstract int hashCode();

    // TEST before a call of a message body reader or writer the current state
    // of the matched resources and URIs must be stored for the current thread.

    /**
     * initializes the provider (injection into annotated fields and setters).
     * 
     * @throws IllegalFieldTypeException
     * @throws IllegalBeanSetterTypeException
     * @throws InjectException
     * @throws InvocationTargetException
     */
    void initProvider(Object jaxRsProvider, ThreadLocalizedContext tlContext,
            Providers allProviders,
            ExtensionBackwardMapping extensionBackwardMapping)
            throws IllegalFieldTypeException, IllegalBeanSetterTypeException,
            InjectException, InvocationTargetException {
        final ContextInjector iph = new ContextInjector(jaxRsProvider.getClass(),
                tlContext, allProviders, extensionBackwardMapping);
        iph.injectInto(jaxRsProvider, false);
    }

    /**
     * Returns true, if this ProviderWrapper is also a
     * {@link javax.ws.rs.ext.ContextResolver}, otherwise false.
     * 
     * @return true, if this ProviderWrapper is also a
     *         {@link javax.ws.rs.ext.ContextResolver}, otherwise false.
     */
    public abstract boolean isContextResolver();

    /**
     * Checks, if this provider represents an {@link ExceptionMapper}.
     * 
     * @return true, if this provider is an {@link ExceptionMapper}, or false if
     *         not.
     */
    public abstract boolean isExceptionMapper();

    /**
     * Returns true, if this ProviderWrapper is also a
     * {@link javax.ws.rs.ext.MessageBodyReader}, otherwise false.
     * 
     * @return true, if this ProviderWrapper is also a
     *         {@link javax.ws.rs.ext.MessageBodyReader}, otherwise false.
     */
    public abstract boolean isReader();

    /**
     * Returns true, if this ProviderWrapper is also a
     * {@link javax.ws.rs.ext.MessageBodyWriter}, otherwise false.
     * 
     * @return true, if this ProviderWrapper is also a
     *         {@link javax.ws.rs.ext.MessageBodyWriter}, otherwise false.
     */
    public abstract boolean isWriter();

    /**
     * Checks, if this message body writer supports the given type (by the type
     * parameter of the {@link javax.ws.rs.ext.MessageBodyWriter})
     * 
     * @param entityClass
     *            the type
     * @param genericType
     *            the generic type
     * @return true, if this MessageBodyWriter supports the given type, false,
     *         if not.
     * @see org.restlet.ext.jaxrs.internal.wrappers.provider.MessageBodyWriter#supportsWrite(java.lang.Class,
     *      java.lang.reflect.Type)
     */
    public boolean supportsWrite(Class<?> entityClass, Type genericType) {
        if (entityClass == null) {
            return false;
        }
        if (genericType == null) {
            // LATER use Type instead of Class
        }
        if (this.genericMbwType == null) {
            return false;
        }
        final boolean supportsWrite = this.genericMbwType
                .isAssignableFrom(entityClass);
        return supportsWrite;
    }

    /**
     * Checks, if this message body reader supports the given type (by the type
     * parameter of the {@link javax.ws.rs.ext.MessageBodyWriter})
     * 
     * @param entityClass
     *            the type
     * @param genericType
     *            the generic type
     * @return true, if this MessageBodyReader supports the given type, false,
     *         if not.
     * @see MessageBodyReader#supportsRead(Class, Type)
     */
    public boolean supportsRead(Class<?> entityClass, Type genericType) {
        if (entityClass == null) {
            return false;
        }
        if (genericType == null) {
            // LATER use Type instead of Class
        }
        if (this.genericMbrType == null) {
            return false;
        }
        return this.genericMbrType.isAssignableFrom(entityClass);
    }

    /**
     * Checks, if this MessageBodyReader supports the given MediaType.
     * 
     * @param mediaType
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
     *            the {@link MediaType}s
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

    public boolean supportsWrite(javax.ws.rs.core.MediaType requested) {
        return this.supportsWrite(Converter.toRestletMediaType(requested));
    }

    /**
     * Checks, if the wrapped MessageBodyWriter supports at least one of the
     * requested {@link MediaType}s.
     * 
     * @param requested
     *            the requested {@link MediaType}s
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

    @Override
    public final String toString() {
        return this.getClassName();
    }
}