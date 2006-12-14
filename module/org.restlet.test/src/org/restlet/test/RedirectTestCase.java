/*
 * Copyright 2005-2006 Noelios Consulting.
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

package org.restlet.test;

import junit.framework.TestCase;

import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Redirector;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.StringRepresentation;

/**
 * Unit tests for the RedirectRestlet.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class RedirectTestCase extends TestCase {
    /**
     * Tests the cookies parsing.
     */
    public void testRedirect() throws Exception {
        // Create components
        Component clientComponent = new Component();
        Component proxyComponent = new Component();
        Component originComponent = new Component();

        // Create the client connectors
        clientComponent.getClients().add(Protocol.HTTP);
        proxyComponent.getClients().add(Protocol.HTTP);

        // Create the proxy Restlet
        String target = "http://localhost:9090${path}#[if query]?${query}#[end]";
        Redirector proxy = new Redirector(proxyComponent.getContext(), target,
                Redirector.MODE_CONNECTOR);

        // Create a new Restlet that will display some path information.
        Restlet trace = new Restlet(originComponent.getContext()) {
            public void handle(Request request, Response response) {
                // Print the requested URI path
                String message = "Resource URI:  " + request.getResourceRef()
                        + '\n' + "Base URI:      "
                        + request.getResourceRef().getBaseRef() + '\n'
                        + "Relative path: "
                        + request.getResourceRef().getRemainingPart() + '\n'
                        + "Query string:  "
                        + request.getResourceRef().getQuery() + '\n'
                        + "Method name:   " + request.getMethod() + '\n';
                response.setEntity(new StringRepresentation(message,
                        MediaType.TEXT_PLAIN));
            }
        };

        // Set the container roots
        proxyComponent.getDefaultHost().attach("", proxy);
        originComponent.getDefaultHost().attach("", trace);

        // Create the server connectors
        proxyComponent.getServers().add(Protocol.HTTP, 8080);
        originComponent.getServers().add(Protocol.HTTP, 9090);

        // Now, let's start the containers!
        originComponent.start();
        proxyComponent.start();
        clientComponent.start();

        // Tests
        Context context = clientComponent.getContext();
        String uri = "http://localhost:8080/?foo=bar";
        testCall(context, Method.GET, uri);
        testCall(context, Method.DELETE, uri);

        uri = "http://localhost:8080/abcd/efgh/ijkl?foo=bar&foo=beer";
        testCall(context, Method.GET, uri);
        testCall(context, Method.DELETE, uri);

        uri = "http://localhost:8080/v1/client/kwse/CnJlNUQV9%252BNNqbUf7Lhs2BYEK2Y%253D/user/johnm/uVGYTDK4kK4zsu96VHGeTCzfwso%253D/";
        testCall(context, Method.GET, uri);

        // Stop the containers
        clientComponent.stop();
        originComponent.stop();
        proxyComponent.stop();
    }

    private void testCall(Context context, Method method, String uri)
            throws Exception {
        Response response = context.getDispatcher().handle(
                new Request(method, uri));
        assertNotNull(response.getEntity());
        response.getEntity().write(System.out);
    }
}
