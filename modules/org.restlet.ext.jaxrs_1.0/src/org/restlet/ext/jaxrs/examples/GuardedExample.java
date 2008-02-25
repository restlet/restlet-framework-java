package org.restlet.ext.jaxrs.examples;

import java.security.Principal;

import javax.ws.rs.core.SecurityContext;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Guard;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.AccessControl;
import org.restlet.ext.jaxrs.JaxRsRouter;

/**
 * This class contains some example code to show how to use the Restlet JAX-RS
 * extension.
 * 
 * @author Stephan Koops
 */
public class GuardedExample {

    /**
     * an example {@link AccessControl}
     * 
     * @author Stephan Koops
     */
    private static final class ExampleAccessControl implements AccessControl {
        /**
         * @see org.restlet.ext.jaxrs.AccessControl#isUserInRole(java.security.Principal,
         *      java.lang.String)
         * @see SecurityContext#isUserInRole(String)
         */
        public boolean isUserInRole(Principal principal, String role) {
            // access database or whatever
            // example: user with name "admin" has all roles, other users have
            // no roles.
            if (principal.getName().equalsIgnoreCase("admin"))
                return true;
            return false;
        }
    }

    /**
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        Component comp = new Component();
        Server server = comp.getServers().add(Protocol.HTTP, 8182);

        // Create an application
        Application application = new Application(comp.getContext()) {
            @Override
            public Restlet createRoot() {
                AccessControl accessControl = new ExampleAccessControl();
                Guard guard = new Guard(getContext(),
                        ChallengeScheme.HTTP_BASIC, "persons");
                guard.getSecrets().put("admin", "adminPW".toCharArray());
                guard.getSecrets().put("alice", "alicesSecret".toCharArray());
                guard.getSecrets().put("bob", "bobsSecret".toCharArray());
                JaxRsRouter router = new JaxRsRouter(getContext(),
                        new ExampleAppConfig(), accessControl);
                guard.setNext(router);
                return guard;
            }
        };

        // Attach the application to the component and start it
        comp.getDefaultHost().attach(application);
        comp.start();

        System.out.println("Server stated on port " + server.getPort());
        System.out.println("Press key to stop server");
        System.in.read();
        System.out.println("Stopping server");
        comp.stop();
    }
}