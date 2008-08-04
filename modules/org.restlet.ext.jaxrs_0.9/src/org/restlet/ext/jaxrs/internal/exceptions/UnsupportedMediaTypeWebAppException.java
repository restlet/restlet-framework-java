/*
 * Copyright 2005-2008 Noelios Technologies.
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
package org.restlet.ext.jaxrs.internal.exceptions;

import java.util.Collection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Variant;
import javax.ws.rs.core.Response.Status;

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