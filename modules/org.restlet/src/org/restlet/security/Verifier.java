/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
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

package org.restlet.security;

import org.restlet.Filter;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Filter verifying the credentials provided by a subject sending a request.
 * 
 * @author Jerome Louvel
 */
public abstract class Verifier extends Filter {

    /** Invalid credentials provided. */
    public final static int RESULT_INVALID = -1;

    /** No credentials provided. */
    public final static int RESULT_MISSING = 0;

    /** Stale credentials provided. */
    public final static int RESULT_STALE = 1;

    /** Unsupported credentials. */
    public final static int RESULT_UNSUPPORTED = 3;

    /** Valid credentials provided. */
    public final static int RESULT_VALID = 4;

    /**
     * Attempts to verify the credentials provided by the subject sending the
     * request.
     * 
     * @param request
     *            The request sent.
     * @param response
     *            The response to update.
     * @return Result of the verification based on the RESULT_* constants.
     */
    public abstract int verify(Request request, Response response);

}
