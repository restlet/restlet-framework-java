/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jaxrs.internal.exceptions;

import java.util.Collection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

/**
 * The server is refusing to service the request because the entity of the
 * request is in a format not accepted by the requested resource for the
 * requested method.
 * 
 * @author Stephan Koops
 * @see <a href="http://tools.ietf.org/html/rfc2616#sec10.4.16">RFC 2616,
 *      Section 10.4.16, "415 Unsupported Media Type"</a>
 */
public class UnsupportedMediaTypeWebAppException extends
        WebApplicationException {

    private static final long serialVersionUID = 767927925135821476L;

    private final Collection<Variant> accepted;

    /**
     * @param accepted
     *            the accepted Variants.
     */
    public UnsupportedMediaTypeWebAppException(Collection<Variant> accepted) {
        super(Status.UNSUPPORTED_MEDIA_TYPE);
        if (accepted == null) {
            throw new IllegalArgumentException(
                    "You have to give a collection of accepted Variants.");
        }
        this.accepted = accepted;
    }

    /**
     * Returns the accepted {@link Variant}s.
     * 
     * @return the accepted MediaTypes.
     */
    public Collection<Variant> getAccepted() {
        return this.accepted;
    }
}
