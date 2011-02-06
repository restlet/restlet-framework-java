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

package org.restlet.ext.jaxrs.internal.core;

import javax.ws.rs.core.UriInfo;

import org.restlet.ext.jaxrs.ExtendedUriBuilder;
import org.restlet.ext.jaxrs.ExtendedUriInfo;

/**
 * A ThreadLocalizedUriInfo is used to inject, if a {@link UriInfo} is required
 * to inject. It must be new instantiated for every place to inject.
 * 
 * @author Stephan Koops
 * @see UriInfo
 * @see ThreadLocalizedContext
 */
public class ThreadLocalizedExtendedUriInfo extends ThreadLocalizedUriInfo
        implements ExtendedUriInfo {

    /**
     * @param tlContext
     */
    public ThreadLocalizedExtendedUriInfo(ThreadLocalizedContext tlContext) {
        super(tlContext);
    }

    /**
     * Get the absolute path of the request in the form of an
     * {@link ExtendedUriBuilder}. This includes everything preceding the path
     * (host, port etc) but excludes query parameters. It also includes the
     * extension of the current request.
     * 
     * @return an ExtendedUriBuilder initialized with the absolute path of the
     *         request and an extension according to the current chosen media
     *         type.
     * @throws java.lang.IllegalStateException
     *             if called outside the scope of a request
     * @see UriInfo#getAbsolutePathBuilder()
     */
    @Override
    public ExtendedUriBuilder getAbsolutePathBuilder() {
        return getCallContext().getAbsolutePathBuilderExtended();
    }

    /**
     * Get the base URI of the application in the form of a UriBuilder. It also
     * includes the extension of the current request.
     * 
     * @return a UriBuilder initialized with the base URI of the application and
     *         an extension according to the current chosen media type.
     * @see UriInfo#getBaseUriBuilder()
     */
    @Override
    public ExtendedUriBuilder getBaseUriBuilder() {
        return getCallContext().getBaseUriBuilderExtended();
    }

    /**
     * Get the absolute request URI in the form of a UriBuilder. It also
     * includes the extension of the current request.
     * 
     * @return a UriBuilder initialized with the absolute request URI and an
     *         extension according to the current chosen media type
     * @throws java.lang.IllegalStateException
     *             if called outside the scope of a request
     */
    @Override
    public ExtendedUriBuilder getRequestUriBuilder() {
        return getCallContext().getRequestUriBuilderExtended();
    }
}