/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.test;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.representation.StringRepresentation;
import org.restlet.routing.Redirector;

/**
 * Unit tests for the RedirectRestlet.
 * 
 * @author Jerome Louvel
 */
public class RedirectTestCase extends RestletTestCase {
    private void testCall(Context context, Method method, String uri)
            throws Exception {
        final Response response = context.getClientDispatcher().handle(
                new Request(method, uri));
        assertNotNull(response.getEntity());
        response.getEntity().write(System.out);
    }

    /**
     * Tests the cookies parsing.
     */
    public void testRedirect() throws Exception {
        // Create components
        final Component clientComponent = new Component();
        final Component proxyComponent = new Component();
        final Component originComponent = new Component();

        // Create the client connectors
        clientComponent.getClients().add(Protocol.HTTP);
        proxyComponent.getClients().add(Protocol.HTTP);

        // Create the proxy Restlet
        final String target = "http://localhost:" + (TEST_PORT + 1) + "{rr}";
        final Redirector proxy = new Redirector(proxyComponent.getContext()
                .createChildContext(), target, Redirector.MODE_DISPATCHER);

        // Create a new Restlet that will display some path information.
        final Restlet trace = new Restlet(originComponent.getContext()
                .createChildContext()) {
            @Override
            public void handle(Request request, Response response) {
                // Print the requested URI path
                final String message = "Resource URI:  "
                        + request.getResourceRef() + '\n' + "Base URI:      "
                        + request.getResourceRef().getBaseRef() + '\n'
                        + "Remaining part: "
                        + request.getResourceRef().getRemainingPart() + '\n'
                        + "Method name:   " + request.getMethod() + '\n';
                response.setEntity(new StringRepresentation(message,
                        MediaType.TEXT_PLAIN));
            }
        };

        // Set the component roots
        proxyComponent.getDefaultHost().attach("", proxy);
        originComponent.getDefaultHost().attach("", trace);

        // Create the server connectors
        proxyComponent.getServers().add(Protocol.HTTP, TEST_PORT);
        originComponent.getServers().add(Protocol.HTTP, TEST_PORT + 1);

        // Now, let's start the components!
        originComponent.start();
        proxyComponent.start();
        clientComponent.start();

        // Tests
        final Context context = clientComponent.getContext();
        String uri = "http://localhost:" + TEST_PORT + "/?foo=bar";
        testCall(context, Method.GET, uri);
        testCall(context, Method.DELETE, uri);

        uri = "http://localhost:" + TEST_PORT
                + "/abcd/efgh/ijkl?foo=bar&foo=beer";
        testCall(context, Method.GET, uri);
        testCall(context, Method.DELETE, uri);

        uri = "http://localhost:" + TEST_PORT
                + "/v1/client/kwse/CnJlNUQV9%252BNNqbUf7Lhs2BYEK2Y%253D"
                + "/user/johnm/uVGYTDK4kK4zsu96VHGeTCzfwso%253D/";
        testCall(context, Method.GET, uri);

        // Stop the components
        clientComponent.stop();
        originComponent.stop();
        proxyComponent.stop();
    }
}
