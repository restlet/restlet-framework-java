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
import java.util.List;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Providers;

import org.restlet.data.MediaType;
import org.restlet.ext.jaxrs.internal.core.ThreadLocalizedContext;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalTypeException;
import org.restlet.ext.jaxrs.internal.exceptions.InjectException;
import org.restlet.ext.jaxrs.internal.exceptions.ProviderNotInitializableException;

/**
 * Wraps a JAX-RS provider, see chapter 4 of JAX-RS specification.
 * 
 * @author Stephan Koops
 * @see javax.ws.rs.ext.Provider
 */
public interface ProviderWrapper {

    public abstract boolean equals(Object otherProvider);

    /**
     * @return the JAX-RS provider class name
     */
    abstract String getClassName();

    /**
     * Returns the list of produced {@link MediaType}s of the wrapped
     * {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return List of produced {@link MediaType}s.
     */
    List<MediaType> getConsumedMimes();

    /**
     * @return an initialized {@link javax.ws.rs.ext.ContextResolver}
     * @throws ProviderNotInitializableException  
     */
    abstract ContextResolver getInitializedCtxResolver() throws ProviderNotInitializableException;

    /**
     * Beispiele:
     * <ul>
     * <li>ExceptionMapper&lt;IllegalArgumentException&gt; -&gt;
     * IllegalArgumentException</li>
     * <li>MessageBodyReader&lt;Integer&gt; -&gt; Integer</li>
     * </ul>
     * 
     * @return the type the wrapped exception mapper could map.
     */
    public Class<?> getExcMapperType();

    /**
     * @return the initialized exception mapper
     * @throws ProviderNotInitializableException 
     */
    abstract ExceptionMapper<? extends Throwable> getInitializedExcMapper() throws ProviderNotInitializableException;

    /**
     * @return an initialized reader
     * @throws ProviderNotInitializableException 
     */
    abstract MessageBodyReader getInitializedReader() throws ProviderNotInitializableException;

    // LATER before a call of a message body reader or writer the current state
    // of the matched resources and URIs must be stored for the current thread.

    /**
     * @return an initialized writer
     * @throws ProviderNotInitializableException
     */
    abstract MessageBodyWriter getInitializedWriter() throws ProviderNotInitializableException;

    /**
     * Returns the list of produced {@link MediaType}s of the wrapped
     * {@link javax.ws.rs.ext.MessageBodyWriter}.
     * 
     * @return List of produced {@link MediaType}s. If the entity provider is
     *         not annotated with &#64; {@link javax.ws.rs.Produces},
     *         '*<!---->/*' is returned.
     */
    List<MediaType> getProducedMimes();

    public int hashCode();

    /**
     * Initializes this entity provider at start up. If the life cycle is
     * instantiation per-request, nothing happens in this method.
     * 
     * @param tlContext
     * @param allProviders
     * @param extensionBackwardMapping
     * @throws InjectException
     * @throws InvocationTargetException
     * @throws IllegalTypeException
     */
    public void initAtAppStartUp(ThreadLocalizedContext tlContext,
            Providers allProviders,
            ExtensionBackwardMapping extensionBackwardMapping)
            throws InjectException, InvocationTargetException,
            IllegalTypeException;

    /**
     * Returns true, if this ProviderWrapper is also a
     * {@link javax.ws.rs.ext.ContextResolver}, otherwise false.
     * 
     * @return true, if this ProviderWrapper is also a
     *         {@link javax.ws.rs.ext.ContextResolver}, otherwise false.
     */
    abstract boolean isContextResolver();

    /**
     * Checks, if this provider represents an {@link ExceptionMapper}.
     * 
     * @return true, if this provider is an {@link ExceptionMapper}, or false if
     *         not.
     */
    abstract boolean isExceptionMapper();

    /**
     * Returns true, if this ProviderWrapper is also a
     * {@link javax.ws.rs.ext.MessageBodyReader}, otherwise false.
     * 
     * @return true, if this ProviderWrapper is also a
     *         {@link javax.ws.rs.ext.MessageBodyReader}, otherwise false.
     */
    abstract boolean isReader();

    /**
     * Returns true, if this ProviderWrapper is also a
     * {@link javax.ws.rs.ext.MessageBodyWriter}, otherwise false.
     * 
     * @return true, if this ProviderWrapper is also a
     *         {@link javax.ws.rs.ext.MessageBodyWriter}, otherwise false.
     */
    abstract boolean isWriter();

    /**
     * Checks, if this MessageBodyReader supports the given MediaType.
     * 
     * @param mediaType
     * @return true, if the wrapped {@link javax.ws.rs.ext.MessageBodyReader}
     *         supports the read for the given media type.
     */
    public boolean supportsRead(MediaType mediaType);

    /**
     * Checks, if the wrapped MessageBodyWriter supports at least one of the
     * requested {@link MediaType}s.
     * 
     * @param mediaTypes
     *            the {@link MediaType}s
     * @return true, if at least one of the requested {@link MediaType}s is
     *         supported, otherwise false.
     */
    public boolean supportsWrite(Iterable<MediaType> mediaTypes);

    /**
     * Checks, if the wrapped MessageBodyWriter supports at least one of the
     * requested {@link MediaType}s.
     * 
     * @param requested
     *            the requested {@link MediaType}s
     * @return true, if at least one of the requested {@link MediaType}s is
     *         supported, otherwise false.
     */
    public boolean supportsWrite(javax.ws.rs.core.MediaType requested);

    /**
     * Checks, if the wrapped MessageBodyWriter supports at least one of the
     * requested {@link MediaType}s.
     * 
     * @param requested
     *            the requested {@link MediaType}s
     * @return true, if at least one of the requested {@link MediaType}s is
     *         supported, otherwise false.
     */
    public boolean supportsWrite(MediaType requested);
}