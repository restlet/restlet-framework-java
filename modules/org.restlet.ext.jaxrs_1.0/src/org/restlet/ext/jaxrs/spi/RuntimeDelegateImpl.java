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

package org.restlet.ext.jaxrs.spi;

import javax.ws.rs.core.ApplicationConfig;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;

import org.restlet.ext.jaxrs.core.JaxRsUriBuilder;
import org.restlet.ext.jaxrs.core.ResponseBuilderImpl;
import org.restlet.ext.jaxrs.core.VariantListBuilderImpl;

/**
 * Implementation of abstract class {@link javax.ws.rs.ext.RuntimeDelegate}
 * 
 * @author Stephan Koops
 * 
 */
public class RuntimeDelegateImpl extends javax.ws.rs.ext.RuntimeDelegate {
    /**
     * Obtain an instance of a HeaderDelegate for the supplied class. An
     * implementation is required to support the following classes: Cookie,
     * CacheControl, EntityTag, NewCookie, MediaType.<br>
     * Will be called by this classes one times for each class.
     * 
     * @see javax.ws.rs.ext.RuntimeDelegate#createHeaderDelegate(Class)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> HeaderDelegate createHeaderDelegate(Class<T> type)
            throws IllegalArgumentException {
        if (type.equals(Cookie.class))
            return new CookieHeaderDelegate();
        if (type.equals(CacheControl.class))
            return new CacheControlHeaderDelegate();
        if (type.equals(EntityTag.class))
            return new EntityTagHeaderDelegate();
        if (type.equals(NewCookie.class))
            return new NewCookieHeaderDelegate();
        if (type.equals(MediaType.class))
            return new MediaTypeHeaderDelegate();
        throw new IllegalArgumentException(
                "This method support only the Types Cookie, CacheControl, EntityTag, NewCookie and MediaType");
    }

    /**
     * @see javax.ws.rs.ext.RuntimeDelegate#createResponseBuilder()
     */
    @Override
    public ResponseBuilder createResponseBuilder() {
        return new ResponseBuilderImpl();
    }

    /**
     * @see javax.ws.rs.ext.RuntimeDelegate#createUriBuilder()
     * REQUEST javadoc: encoding an
     */
    @Override
    public UriBuilder createUriBuilder() {
        return new JaxRsUriBuilder();
    }

    /**
     * @see javax.ws.rs.ext.RuntimeDelegate#createVariantListBuilder()
     */
    @Override
    public VariantListBuilder createVariantListBuilder() {
        return new VariantListBuilderImpl();
    }

    /**
     * This method is not supported by this implementation.
     * 
     * @throws UnsupportedOperationException
     *                 ever.
     */
    @Override
    public <T> T createEndpoint(ApplicationConfig applicationConfig,
            Class<T> endpointType) throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "The method RuntimeDelegate.createEndpoint() is not available by the Restlet JAX-RS extension");
        // REQUEST allow officially UnsupportedOperationException
    }
}
