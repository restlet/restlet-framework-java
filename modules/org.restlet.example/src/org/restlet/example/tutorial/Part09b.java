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

package org.restlet.example.tutorial;

import org.restlet.Client;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Authenticating to an HTTP server.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Part09b {
    public static void main(String[] args) throws Exception {
        // Prepare the request
        final Request request = new Request(Method.GET,
                "http://localhost:8182/");

        // Add the client authentication to the call
        final ChallengeScheme scheme = ChallengeScheme.HTTP_BASIC;
        final ChallengeResponse authentication = new ChallengeResponse(scheme,
                "scott", "tiger");
        request.setChallengeResponse(authentication);

        // Ask to the HTTP client connector to handle the call
        final Client client = new Client(Protocol.HTTP);
        final Response response = client.handle(request);

        if (response.getStatus().isSuccess()) {
            // Output the response entity on the JVM console
            response.getEntity().write(System.out);
        } else if (response.getStatus()
                .equals(Status.CLIENT_ERROR_UNAUTHORIZED)) {
            // Unauthorized access
            System.out
                    .println("Access authorized by the server, check your credentials");
        } else {
            // Unexpected status
            System.out.println("An unexpected status was returned: "
                    + response.getStatus());
        }
    }

}
