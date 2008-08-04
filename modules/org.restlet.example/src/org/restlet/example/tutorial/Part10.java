/*
 * Copyright 2005-2007 Noelios Technologies.
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
