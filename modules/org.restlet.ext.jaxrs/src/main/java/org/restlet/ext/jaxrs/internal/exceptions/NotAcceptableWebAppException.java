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
     *            the supported variants, selectable by the accept headers.
     */
    public NotAcceptableWebAppException(Collection<Variant> supported) {
        super(Status.NOT_ACCEPTABLE);
        if (supported == null) {
            throw new IllegalArgumentException(
                    "The allowed variants must not be null");
        }
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
