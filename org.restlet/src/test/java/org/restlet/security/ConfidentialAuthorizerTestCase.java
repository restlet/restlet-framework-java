/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Protocol;

/** Unit tests for {@link ConfidentialAuthorizer}. */
class ConfidentialAuthorizerTestCase {

    @Test
    void authorize_confidentialRequest_returnsTrue() {
        ConfidentialAuthorizer authorizer = new ConfidentialAuthorizer();
        Request request = new Request();
        request.setProtocol(Protocol.HTTPS);
        Response response = new Response(request);

        assertTrue(authorizer.authorize(request, response));
    }

    @Test
    void authorize_nonConfidentialRequest_returnsFalse() {
        ConfidentialAuthorizer authorizer = new ConfidentialAuthorizer();
        Request request = new Request();
        request.setProtocol(Protocol.HTTP);
        Response response = new Response(request);

        assertFalse(authorizer.authorize(request, response));
    }
}
