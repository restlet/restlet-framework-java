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

package org.restlet.ext.jaxrs;

import javax.ws.rs.core.UriInfo;

/**
 * This {@link UriInfo} extension returns {@link javax.ws.rs.core.UriBuilder}s
 * which contains the "file" extension given by the request. This extension will
 * also be available, if the path will be changed (e.g. replaced, removed or
 * matrix parameters added). For further information see
 * {@link ExtendedUriBuilder#extension(String)},
 * {@link ExtendedUriBuilder#extensionLanguage(String)} and
 * {@link ExtendedUriBuilder#extensionMedia(String)}. You could get it in the
 * same way as the default UriInfo, that measn annotate a field of this type
 * with &#64;{@link javax.ws.rs.core.Context}.
 * 
 * @author Stephan Koops
 * @see UriInfo
 */
public interface ExtendedUriInfo extends UriInfo {

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
    public ExtendedUriBuilder getAbsolutePathBuilder();

    /**
     * Get the base URI of the application in the form of an
     * {@link ExtendedUriBuilder}. It also includes the extension of the current
     * request.
     * 
     * @return a UriBuilder initialized with the base URI of the application and
     *         an extension according to the current chosen media type.
     * @see UriInfo#getBaseUriBuilder()
     */
    public ExtendedUriBuilder getBaseUriBuilder();

    /**
     * Get the absolute request URI in the form of an {@link ExtendedUriBuilder}
     * . It also includes the extension of the current request.
     * 
     * @return a UriBuilder initialized with the absolute request URI and an
     *         extension according to the current chosen media type
     * @throws java.lang.IllegalStateException
     *             if called outside the scope of a request
     */
    public ExtendedUriBuilder getRequestUriBuilder();
}
