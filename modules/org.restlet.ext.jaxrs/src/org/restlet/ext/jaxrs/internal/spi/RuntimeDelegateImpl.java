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

package org.restlet.ext.jaxrs.internal.spi;

import java.util.Date;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Variant.VariantListBuilder;

import org.restlet.ext.jaxrs.internal.core.ResponseBuilderImpl;
import org.restlet.ext.jaxrs.internal.core.UriBuilderImpl;
import org.restlet.ext.jaxrs.internal.core.VariantListBuilderImpl;

/**
 * Implementation of abstract JAX-RS class
 * {@link javax.ws.rs.ext.RuntimeDelegate}.
 * 
 * @author Stephan Koops
 */
public class RuntimeDelegateImpl extends javax.ws.rs.ext.RuntimeDelegate {
    /**
     * This method is not supported by this implementation.
     * 
     * @throws UnsupportedOperationException
     *                 ever.
     */
    @Override
    public <T> T createEndpoint(Application application, Class<T> endpointType)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "The method RuntimeDelegate.createEndpoint() is not available by the Restlet JAX-RS extension");
    }

    /**
     * Obtain an instance of a HeaderDelegate for the supplied class. An
     * implementation is required to support the following values for type:
     * Cookie, CacheControl, EntityTag, NewCookie, MediaType, Date.
     * 
     * @param type
     *                the class of the header
     * @return an instance of HeaderDelegate for the supplied type
     * @see javax.ws.rs.ext.RuntimeDelegate#createHeaderDelegate(Class)
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public <T> HeaderDelegate createHeaderDelegate(Class<T> type)
            throws IllegalArgumentException {
        if (type.equals(Cookie.class)) {
            return new CookieHeaderDelegate();
        }
        if (type.equals(CacheControl.class)) {
            return new CacheControlHeaderDelegate();
        }
        if (type.equals(EntityTag.class)) {
            return new EntityTagHeaderDelegate();
        }
        if (type.equals(NewCookie.class)) {
            return new NewCookieHeaderDelegate();
        }
        if (type.equals(MediaType.class)) {
            return new MediaTypeHeaderDelegate();
        }
        if (type.equals(Date.class)) {
            return new DateHeaderDelegate();
        }
        throw new IllegalArgumentException(
                "This method supports only the Types Cookie, CacheControl, EntityTag, NewCookie and MediaType");
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
     */
    @Override
    public UriBuilder createUriBuilder() {
        return new UriBuilderImpl();
    }

    /**
     * @see javax.ws.rs.ext.RuntimeDelegate#createVariantListBuilder()
     */
    @Override
    public VariantListBuilder createVariantListBuilder() {
        return new VariantListBuilderImpl();
    }
}