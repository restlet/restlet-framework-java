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
package org.restlet.ext.jaxrs.internal.exceptions;

import java.util.Collection;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

/**
 * The resource identified by the request is only capable of generating response
 * entities which have content characteristics not acceptable according to the
 * accept headers sent in the request.
 * 
 * @author Stephan Koops
 * @see <a href="http://tools.ietf.org/html/rfc2616#section-10.4.7">RFC 2616,
 *      Section 10.4.7, "406 Not Acceptable"</a>
 */
public class NotAcceptableWebAppException extends WebApplicationException {

    private static final long serialVersionUID = 6895779829973209211L;

    private final Collection<Variant> supported;

    /**
     * @param supported
     *                the supported variants, selectable by the accept headers.
     */
    public NotAcceptableWebAppException(Collection<Variant> supported) {
        super(Status.NOT_ACCEPTABLE);
        if (supported == null)
            throw new IllegalArgumentException(
                    "The allowed variants must not be null");
        this.supported = supported;
    }

    /**
     * Returns the supported {@link Variant}s.
     * 
     * @return the supported variants.
     */
    public Collection<Variant> getSupported() {
        return this.supported;
    }
}