package org.restlet.example.jaxrs;

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
import org.restlet.ext.jaxrs.HtmlPreferer;
import org.restlet.ext.jaxrs.JaxRsRouter;

/**
 * This class shows how to use the Restlet JAX-RS extension with access control.
 * 
 * @author Stephan Koops
 * @see ExampleServer
 */
public class GuardedExample {

    /**
     * An example {@link AccessControl}. This example allows anything to user
     * admin and nothing to anyone else.
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
            // example: user "admin" has all roles, other users have no roles.
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
                // create access controller
                AccessControl accessControl = new ExampleAccessControl();
                // create JaxRsRouter
                JaxRsRouter router = new JaxRsRouter(getContext(),
                        new ExampleAppConfig(), accessControl);

                // create Guard for authentication
                Guard guard = new Guard(getContext(),
                        ChallengeScheme.HTTP_BASIC, "persons");
                // set valid users ant it's passwords.
                guard.getSecrets().put("admin", "adminPW".toCharArray());
                guard.getSecrets().put("alice", "alicesSecret".toCharArray());
                guard.getSecrets().put("bob", "bobsSecret".toCharArray());
                // attach JaxRsRouter to the Guard
                guard.setNext(router);

                // some browser request XML with higher quality than HTML.
                // If you want to change the quality, use this HtmlPreferer
                // filter. If not, you can directly return the guard
                HtmlPreferer filter = new HtmlPreferer(getContext(), guard);

                // return the filter
                // (or directly the Guard, if you do not need the HtmlPreferer).
                return filter;
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