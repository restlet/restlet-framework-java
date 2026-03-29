/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
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
    int RESULT_INVALID = -1;

    /** No credentials provided. */
    int RESULT_MISSING = 0;

    /** Stale credentials provided. */
    int RESULT_STALE = 1;

    /** Unsupported credentials. */
    int RESULT_UNSUPPORTED = 3;

    /** Unknown user. */
    int RESULT_UNKNOWN = 5;

    /** Valid credentials provided. */
    int RESULT_VALID = 4;

    /**
     * Attempts to verify the credentials provided by the client user sending the request.
     *
     * @param request The request sent.
     * @param response The response to modify.
     * @return Result of the verification based on the RESULT_* constants.
     */
    int verify(Request request, Response response);
}
