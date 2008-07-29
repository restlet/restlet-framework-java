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

package org.restlet.example.tutorial;

import static org.restlet.example.tutorial.Constants.ROOT_URI;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Directory;
import org.restlet.Guard;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Routers and hierarchical URIs
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Part11 extends Application {

    /**
     * Run the example as a standalone component.
     * 
     * @param args
     *            The optional arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Create a component
        final Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);
        component.getClients().add(Protocol.FILE);

        // Create an application
        final Application application = new Part11();

        // Attach the application to the component and start it
        component.getDefaultHost().attach(application);
        component.start();
    }

    @Override
    public Restlet createRoot() {
        // Create a root router
        final Router router = new Router(getContext());

        // Attach a guard to secure access to the directory
        final Guard guard = new Guard(getContext(), ChallengeScheme.HTTP_BASIC,
                "Restlet tutorial");
        guard.getSecrets().put("scott", "tiger".toCharArray());
        router.attach("/docs/", guard);

        // Create a directory able to expose a hierarchy of files
        final Directory directory = new Directory(getContext(), ROOT_URI);
        guard.setNext(directory);

        // Create the account handler
        final Restlet account = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                // Print the requested URI path
                final String message = "Account of user \""
                        + request.getAttributes().get("user") + "\"";
                response.setEntity(message, MediaType.TEXT_PLAIN);
            }
        };

        // Create the orders handler
        final Restlet orders = new Restlet(getContext()) {
            @Override
            public void handle(Request request, Response response) {
                // Print the user name of the requested orders
                final String message = "Orders of user \""
                        + request.getAttributes().get("user") + "\"";
                response.setEntity(message, MediaType.TEXT_PLAIN);
            }
        };

        // Create the order handler
        final Restlet order = new Restlet(getContext()) {
            @Override
            public void handle(Request request, Response response) {
                // Print the user name of the requested orders
                final String message = "Order \""
                        + request.getAttributes().get("order")
                        + "\" for user \""
                        + request.getAttributes().get("user") + "\"";
                response.setEntity(message, MediaType.TEXT_PLAIN);
            }
        };

        // Attach the handlers to the root router
        router.attach("/users/{user}", account);
        router.attach("/users/{user}/orders", orders);
        router.attach("/users/{user}/orders/{order}", order);

        // Return the root router
        return router;
    }

}
