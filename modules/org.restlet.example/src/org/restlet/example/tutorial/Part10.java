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
import org.restlet.Redirector;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.data.Protocol;

/**
 * URI rewriting and redirection.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Part10 {
    public static void main(String[] args) throws Exception {
        // Create a component
        Component component = new Component();
        component.getServers().add(Protocol.HTTP, 8182);

        // Create an application
        Application application = new Application(component.getContext()) {
            @Override
            public Restlet createRoot() {
                // Create a Redirector to Google search service
                String target = "http://www.google.com/search?q=site:mysite.org+{keywords}";
                return new Redirector(getContext(), target,
                        Redirector.MODE_CLIENT_TEMPORARY);
            }
        };

        // Attach the application to the component's default host
        Route route = component.getDefaultHost().attach("/search", application);

        // While routing requests to the application, extract a query parameter
        // For instance :
        // http://localhost:8182/search?kwd=myKeyword1+myKeyword2
        // will be routed to
        // http://www.google.com/search?q=site:mysite.org+myKeyword1%20myKeyword2
        route.extractQuery("keywords", "kwd", true);

        // Start the component
        component.start();
    }

}
