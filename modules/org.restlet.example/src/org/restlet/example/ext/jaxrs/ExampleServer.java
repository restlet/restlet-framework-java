/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.example.ext.jaxrs;

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
 * href="http://localhost/easy">easy</a> or <a
 * href="http://localhost/persons">persons</a>.
 * </p>
 * 
 * @author Stephan Koops
 * @see ExampleApplication
 * @see GuardedExample
 */
public class ExampleServer {

    public static void main(String[] args) throws Exception {
        // create Component (as ever for Restlet)
        final Component comp = new Component();
        final Server server = comp.getServers().add(Protocol.HTTP, 80);

        // create JAX-RS runtime environment
        final JaxRsApplication application = new JaxRsApplication(comp
                .getContext().createChildContext());

        // attach ApplicationConfig
        application.add(new ExampleApplication());

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