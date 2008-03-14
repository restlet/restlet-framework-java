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
package org.restlet.example.jaxrs;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.HtmlPreferer;
import org.restlet.ext.jaxrs.JaxRsRouter;

/**
 * This class shows how to use the Restlet JAX-RS extension without access
 * control.
 * 
 * @author Stephan Koops
 * @see GuardedExample for an example with authentification
 * @see ExampleAppConfig
 */
public class ExampleServer {

    /**
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // This method is build like any Restlet server starting class.
        // The only difference is in the method createRoot()
        Component comp = new Component();
        Server server = comp.getServers().add(Protocol.HTTP, 8182);

        // Create an application
        Application application = new Application(comp.getContext()) {
            @Override
            public Restlet createRoot() {
                ExampleAppConfig appConfig = new ExampleAppConfig();
                JaxRsRouter router = new JaxRsRouter(getContext(), appConfig);
                // some browser request XML with higher quality than HTML.
                // If you want to change the quality, use this HtmlPreferer
                // filter. If you do not need it, you can directly return the
                // router.
                HtmlPreferer filter = new HtmlPreferer(getContext(), router);
                return filter;
            }
        };

        // Attach the application to the component and start it
        comp.getDefaultHost().attach(application);
        comp.start();

        System.out.println("Server started on port " + server.getPort());
        System.out.println("Press key to stop server");
        System.in.read();
        System.out.println("Stopping server");
        comp.stop();
        System.out.println("Server stopped");
    }
}