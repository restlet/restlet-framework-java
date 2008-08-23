/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.ext.jaxrs.internal.wrappers.provider;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ExceptionMapper;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.util.Converter;
import org.restlet.ext.jaxrs.internal.wrappers.WrapperUtil;

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

    /**
     * Creates a new wrapper for a Provider and initializes the provider. If the
     * given class is not a provider, an {@link IllegalArgumentException} is
     * thrown.
     * 
     * @param jaxRsProviderClass
     *                the JAX-RS provider class.
     * @throws IllegalArgumentException
     *                 if the class is not a valid provider, may not be
     *                 instantiated or what ever.
     * @throws WebApplicationException
     * @see javax.ws.rs.ext.MessageBodyReader
     * @see javax.ws.rs.ext.MessageBodyWriter
     * @see javax.ws.rs.ext.ContextResolver
     */
    @SuppressWarnings("unchecked")
    AbstractProviderWrapper(Class<?> jaxRsProviderClass) throws IllegalArgumentException,
            WebApplicationException {
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

    // TODO before a call of a message body reader or writer the current state
    // of the matched resources and URIs must be stored for the current thread.

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
     * @return true, if this provider is an {@link ExceptionMapper}, or false
     *         if not.
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

    public boolean supportsWrite(javax.ws.rs.core.MediaType requested) {
        return this.supportsWrite(Converter.toRestletMediaType(requested));
    }

    /**
     * Checks, if the wrapped MessageBodyWriter supports at least one of the
     * requested {@link MediaType}s.
     * 
     * @param requested
     *                the requested {@link MediaType}s
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

    /**
     * @return
     */
    public abstract MessageBodyReader getInitializedReader();

    /**
     * @return
     */
    public abstract MessageBodyWriter getInitializedWriter();
}