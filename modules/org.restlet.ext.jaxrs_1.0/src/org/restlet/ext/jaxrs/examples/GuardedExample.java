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
import org.restlet.ext.jaxrs.Authenticator;
import org.restlet.ext.jaxrs.JaxRsGuard;
import org.restlet.ext.jaxrs.JaxRsRouter;

/**
 * This class contains some example code to show how to use the Restlet JAX-RS
 * extension.
 * 
 * @author Stephan Koops
 */
public class GuardedExample {

    /**
     * an example {@link Authenticator}
     * 
     * @author Stephan Koops
     */
    private static final class ExampleAuthenticator implements Authenticator {
        /**
         * Checks the password for a given user name. Must work for the given
         * {@link ChallengeScheme}.
         * 
         * @see org.restlet.ext.jaxrs.Authenticator#checkSecret(java.lang.String,
         *      char[])
         * @see Guard#checkSecret(org.restlet.data.Request, String, char[])
         */
        public boolean checkSecret(String identifier, char[] secret) {
            // access database or whatever
            // here an example:
            if (secret[0] == '1')
                return true;
            return false;
        }

        /**
         * @see org.restlet.ext.jaxrs.Authenticator#isUserInRole(java.security.Principal,
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
        comp.getServers().add(Protocol.HTTP, 8182);

        // Create an application
        Application application = new Application(comp.getContext()) {
            @Override
            public Restlet createRoot() {
                Authenticator authenticator = new ExampleAuthenticator();
                JaxRsGuard jaxRsRestlet = JaxRsRouter.getGuarded(getContext(),
                        new ExampleAppConfig(), authenticator,
                        ChallengeScheme.HTTP_BASIC, "persons");
                return jaxRsRestlet;
            }
        };

        // Attach the application to the component and start it
        comp.getDefaultHost().attach(application);
        comp.start();

        Server server = comp.getServers().get(0);
        System.out.println("Server stated on port " + server.getPort());
        System.out.println("Press key to stop server");
        System.in.read();
        System.out.println("Stopping server");
        comp.stop();
    }
}