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

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import org.restlet.Component;
import org.restlet.Guard;
import org.restlet.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.AccessControl;
import org.restlet.ext.jaxrs.JaxRsApplication;

/**
 * <p>
 * This class shows how to use the Restlet JAX-RS extension with access control.
 * </p>
 * <p>
 * Start this class, open a browser and click <a
 * href="http://localhost:8182/easy">easy</a> or <a
 * href="http://localhost:8182/persons">persons</a> with one of the following
 * user / password combinations:
 * <ul>
 * <li>admin / adminPW</li>
 * <li>alice / alicesSecret</li>
 * <li>bob / bobsSecret</li>
 * </ul>
 * </p>
 * 
 * @author Stephan Koops
 * @see ExampleServer
 * @see ExampleAppConfig
 */
public class GuardedExample {

    /**
     * An example {@link AccessControl}. This example allows anything to user
     * admin and only read to any other user. <br>
     * This AccessControl isn't used by the resources.
     * 
     * @author Stephan Koops
     */
    private static final class ExampleAccessControl implements AccessControl {
        /**
         * @see AccessControl#isUserInRole(Principal, String)
         * @see SecurityContext#isUserInRole(String)
         */
        public boolean isUserInRole(Principal principal, String role) {
            // access database or whatever
            // example: user "admin" has all roles
            if (principal.getName().equalsIgnoreCase("admin"))
                return true;
            // example: every authenticatd user could read
            if (role.equalsIgnoreCase("read"))
                return true;
            // the normal users have no other roles.
            return false;
        }
    }

    public static void main(String[] args) throws Exception {
        // create Component (as ever for Restlet)
        Component comp = new Component();
        Server server = comp.getServers().add(Protocol.HTTP, 8182);

        // create JAX-RS runtime environment
        JaxRsApplication application = new JaxRsApplication(comp.getContext());

        // create a Guard
        Guard guard = new Guard(application.getContext(),
                ChallengeScheme.HTTP_BASIC, "JAX-RS example");
        // set valid users and thier passwords.
        guard.getSecrets().put("admin", "adminPW".toCharArray());
        guard.getSecrets().put("alice", "alicesSecret".toCharArray());
        guard.getSecrets().put("bob", "bobsSecret".toCharArray());

        // create an AccessControl (see above)
        ExampleAccessControl accessControl = new ExampleAccessControl();
        // attach Guard and AccessControl
        application.setAuthentication(guard, accessControl);

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