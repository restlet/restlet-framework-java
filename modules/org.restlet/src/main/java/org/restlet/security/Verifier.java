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

package org.restlet.security;

import org.restlet.Request;
import org.restlet.Response;

/**
 * Verifies the credentials provided by a client user sending a request.
 * 
 * @author Jerome Louvel
 */
public interface Verifier {

    /** Invalid credentials provided. */
    public final static int RESULT_INVALID = -1;

    /** No credentials provided. */
    public final static int RESULT_MISSING = 0;

    /** Stale credentials provided. */
    public final static int RESULT_STALE = 1;

    /** Unsupported credentials. */
    public final static int RESULT_UNSUPPORTED = 3;

    /** Unknown user. */
    public final static int RESULT_UNKNOWN = 5;

    /** Valid credentials provided. */
    public final static int RESULT_VALID = 4;

    /**
     * Attempts to verify the credentials provided by the client user sending
     * the request.
     * 
     * @param request
     *            The request sent.
     * @param response
     *            The response to update.
     * @return Result of the verification based on the RESULT_* constants.
     */
    int verify(Request request, Response response);

}
