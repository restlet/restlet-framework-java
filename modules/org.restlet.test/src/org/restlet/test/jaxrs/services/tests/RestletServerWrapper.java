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

package org.restlet.test.jaxrs.services.tests;

import java.util.Collection;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.AllowAllAuthenticator;
import org.restlet.ext.jaxrs.Authenticator;
import org.restlet.ext.jaxrs.JaxRsGuard;
import org.restlet.ext.jaxrs.JaxRsRouter;

/**
 * This class allows easy testing of JAX-RS implementations by starting a server
 * for a given class and access the server for a given sub pass relativ to the
 * pass of the root resource class.
 * 
 * @author Stephan
 * 
 */
public class RestletServerWrapper implements ServerWrapper {

    private Authenticator authenticator;

    private Component component;

    public RestletServerWrapper() {
        this.authenticator = AllowAllAuthenticator.getInstance();
    }

    /**
     * @return the authenticator
     */
    public Authenticator getAuthorizator() {
        return authenticator;
    }

    /**
     * @param authenticator
     *                the authenticator to set. Must not be null
     * @throws IllegalArgumentException
     */
    public void setAuthorizator(Authenticator authenticator)
            throws IllegalArgumentException {
        if (authenticator == null)
            throw new IllegalArgumentException(
                    "The authenticator must not be null");
        this.authenticator = authenticator;
    }

    /**
     * Starts the server with the given protocol on the given port with the
     * given Collection of root resource classes. The method {@link #setUp()}
     * will do this on every test start up.
     * 
     * @param rootResourceClasses
     * @param port
     * @return Returns the started component. Should be stopped with
     *         {@link #stopServer(Component)}
     * @throws Exception
     */
    public void startServer(final Collection<Class<?>> rootResourceClasses,
            Protocol protocol, int port, final ChallengeScheme challengeScheme, Parameter contextParameter)
            throws Exception {
        Component comp = new Component();
        if (contextParameter != null)
            comp.getContext().getParameters().add(contextParameter);
        comp.getServers().add(protocol, port);

        // Create an application
        Application application = new Application(comp.getContext()) {
            @Override
            public Restlet createRoot() {
                JaxRsGuard router = JaxRsRouter.getGuarded(getContext(),
                        challengeScheme, "", authenticator);
                Collection<Class<?>> rrcs = rootResourceClasses;
                for (Class<?> cl : rrcs) {
                    router.attach(cl);
                }
                return router;
            }
        };

        // Attach the application to the component and start it
        comp.getDefaultHost().attach(application);
        comp.start();
        this.component = comp;
    }

    /**
     * Stops the component. The method {@link #tearDown()} do this after every
     * test.
     * 
     * @param component
     * @throws Exception
     */
    public void stopServer() throws Exception {
        if (this.component != null)
            this.component.stop();
    }
}