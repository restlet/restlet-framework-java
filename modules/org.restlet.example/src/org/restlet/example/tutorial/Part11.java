/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.example.tutorial;

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
import static org.restlet.example.tutorial.Constants.*;

/**
 * Routers and hierarchical URIs
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Part11 {
    public static void main(String[] args) throws Exception {
        // Create a component
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);
        component.getClients().add(Protocol.FILE);

        // Create an application
        Application application = new Application(component.getContext()) {
            @Override
            public Restlet createRoot() {
                // Create a root router
                Router router = new Router(getContext());

                // Attach a guard to secure access to the directory
                Guard guard = new Guard(getContext(),
                        ChallengeScheme.HTTP_BASIC, "Restlet tutorial");
                guard.getSecrets().put("scott", "tiger".toCharArray());
                router.attach("/docs/", guard);

                // Create a directory able to expose a hierarchy of files
                Directory directory = new Directory(getContext(), ROOT_URI);
                guard.setNext(directory);

                // Create the account handler
                Restlet account = new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        // Print the requested URI path
                        String message = "Account of user \""
                                + request.getAttributes().get("user") + "\"";
                        response.setEntity(message, MediaType.TEXT_PLAIN);
                    }
                };

                // Create the orders handler
                Restlet orders = new Restlet(getContext()) {
                    @Override
                    public void handle(Request request, Response response) {
                        // Print the user name of the requested orders
                        String message = "Orders of user \""
                                + request.getAttributes().get("user") + "\"";
                        response.setEntity(message, MediaType.TEXT_PLAIN);
                    }
                };

                // Create the order handler
                Restlet order = new Restlet(getContext()) {
                    @Override
                    public void handle(Request request, Response response) {
                        // Print the user name of the requested orders
                        String message = "Order \""
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
        };

        // Attach the application to the component and start it
        component.getDefaultHost().attach(application);
        component.start();
    }

}
