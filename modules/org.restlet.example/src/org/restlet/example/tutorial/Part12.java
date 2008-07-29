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

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.Protocol;

/**
 * Reaching target Resources
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Part12 extends Application {

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

        // Create an application
        final Application application = new Part12();

        // Attach the application to the component and start it
        component.getDefaultHost().attachDefault(application);
        component.start();
    }

    @Override
    public Restlet createRoot() {
        // Create a router
        final Router router = new Router(getContext());

        // Attach the resources to the router
        router.attach("/users/{user}", UserResource.class);
        router.attach("/users/{user}/orders", OrdersResource.class);
        router.attach("/users/{user}/orders/{order}", OrderResource.class);

        // Return the root router
        return router;
    }

}
