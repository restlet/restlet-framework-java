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
 * @author Jerome Louvel
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
