/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
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
public class Part12 {
    public static void main(String[] args) throws Exception {
        // Create a component
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);

        // Create an application
        Application application = new Application(component.getContext()) {
            @Override
            public Restlet createRoot() {
                // Create a router
                Router router = new Router(getContext());

                // Attach the resources to the router
                router.attach("/users/{user}", UserResource.class);
                router.attach("/users/{user}/orders", OrdersResource.class);
                router.attach("/users/{user}/orders/{order}",
                        OrderResource.class);

                // Return the root router
                return router;
            }
        };

        // Attach the application to the component and start it
        component.getDefaultHost().attach("", application);
        component.start();
    }

}
