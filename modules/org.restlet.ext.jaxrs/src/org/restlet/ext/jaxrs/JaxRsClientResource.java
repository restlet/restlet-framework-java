/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jaxrs;

import javax.ws.rs.Path;

import org.restlet.Context;
import org.restlet.data.Reference;
import org.restlet.ext.jaxrs.internal.exceptions.IllegalPathException;
import org.restlet.ext.jaxrs.internal.exceptions.JaxRsException;
import org.restlet.ext.jaxrs.internal.exceptions.MissingAnnotationException;
import org.restlet.resource.ClientProxy;
import org.restlet.resource.ClientResource;

/**
 * The JAX-RS implementation of the {@link ClientResource} class.
 * 
 * @see ClientResource
 * @author Shaun Elliott
 */
public class JaxRsClientResource extends ClientResource {

    /**
     * Creates a client resource that proxy calls to the given Java interface
     * into Restlet method calls.
     * 
     * @param <T>
     * @param context
     *            The context.
     * @param reference
     *            The target reference.
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public static <T> T createJaxRsClient(Context context, Reference reference,
            Class<? extends T> resourceInterface) {
        JaxRsClientResource clientResource = new JaxRsClientResource(context,
                reference);
        return clientResource.wrap(resourceInterface);
    }

    /**
     * Creates a client resource that proxy calls to the given Java interface
     * into Restlet method calls.
     * 
     * @param <T>
     * @param baseUri
     *            The target URI.
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    public static <T> T createJaxRsClient(final String baseUri,
            final Class<? extends T> resourceInterface) throws JaxRsException {
        Path pathAnnotation = resourceInterface.getAnnotation(Path.class);
        if (pathAnnotation == null) {
            throw new MissingAnnotationException(
                    "The resource interface must have the JAX-RS path annotation.");
        }

        String path = pathAnnotation.value();
        if (path == null || path.length() == 0) {
            throw new IllegalPathException(pathAnnotation,
                    "The path annotation must have a value.");
        }

        String fullUriFromPath = baseUri + (baseUri.endsWith("/") ? "" : "/")
                + path;

        return JaxRsClientResource.createJaxRsClient(null, new Reference(
                fullUriFromPath), resourceInterface);
    }

    public JaxRsClientResource(Context context, Reference reference) {
        super(context, reference);
    }

    /**
     * Wraps the client resource to proxy calls to the given Java interface into
     * Restlet method calls.
     * 
     * @param <T>
     * @param resourceInterface
     *            The annotated resource interface class to proxy.
     * @return The proxy instance.
     */
    @SuppressWarnings("unchecked")
    public <T> T wrap(Class<? extends T> resourceInterface) {
        T result = null;

        // Create the client resource proxy
        java.lang.reflect.InvocationHandler h = new org.restlet.ext.jaxrs.internal.client.JaxRsClientInvocationHandler<T>(
                this, resourceInterface);

        // Instantiate our dynamic proxy
        result = (T) java.lang.reflect.Proxy.newProxyInstance(
                org.restlet.engine.Engine.getInstance().getClassLoader(),
                new Class<?>[] { ClientProxy.class, resourceInterface }, h);

        return result;
    }
}
