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

import org.restlet.Component;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.JaxRsApplication;

/**
 * <p>
 * This class shows how to use the Restlet JAX-RS extension without access
 * control.
 * </p>
 * <p>
 * Start this class, open a browser and click <a
 * href="http://localhost:8182/easy">easy</a> or <a
 * href="http://localhost:8182/persons">persons</a>.
 * </p>
 * 
 * @author Stephan Koops
 * @see ExampleAppConfig
 * @see GuardedExample
 */
public class ExampleServer {

    public static void main(String[] args) throws Exception {
        // create Component (as ever for Restlet)
        Component comp = new Component();
        Server server = comp.getServers().add(Protocol.HTTP, 8182);

        // create JAX-RS runtime environment
        JaxRsApplication application = new JaxRsApplication(comp.getContext());

        // attach ApplicationConfig
        application.attach(new ExampleAppConfig());

     // prefer html befor XML, if both is allowed by the request. (optional)
        application.setPreferHtml(true);

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