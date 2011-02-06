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

package org.restlet.engine.component;

import java.util.Iterator;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.ChainHelper;
import org.restlet.routing.Filter;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.VirtualHost;
import org.restlet.service.Service;

/**
 * Component helper.
 * 
 * @author Jerome Louvel
 */
public class ComponentHelper extends ChainHelper<Component> {
    /** The internal client router. */
    private final ClientRouter clientRouter;

    /** The internal server router. */
    private volatile ServerRouter serverRouter;

    /**
     * Constructor.
     * 
     * @param component
     *            The helper component.
     */
    public ComponentHelper(Component component) {
        super(component);
        component.setContext(new ComponentContext(this));
        this.clientRouter = new ClientRouter(getHelped());
        this.serverRouter = new ServerRouter(getHelped());
    }

    /**
     * Check the applications attached to a virtual host.
     * 
     * @param host
     *            The parent virtual host.
     * @return True if the check succeeded.
     * @throws Exception
     */
    private boolean checkVirtualHost(VirtualHost host) throws Exception {
        boolean result = true;

        if (host != null) {
            for (TemplateRoute route : host.getRoutes()) {
                Restlet next = route.getNext();

                if (next instanceof Application) {
                    Application application = (Application) next;

                    if (application.getConnectorService() != null) {
                        if (application.getConnectorService()
                                .getClientProtocols() != null) {
                            for (Protocol clientProtocol : application
                                    .getConnectorService().getClientProtocols()) {
                                boolean clientFound = false;

                                // Try to find a client connector matching the
                                // client protocol
                                Client client;
                                for (Iterator<Client> iter = getHelped()
                                        .getClients().iterator(); !clientFound
                                        && iter.hasNext();) {
                                    client = iter.next();
                                    clientFound = client.getProtocols()
                                            .contains(clientProtocol);
                                }

                                if (!clientFound) {
                                    getLogger()
                                            .severe(
                                                    "Unable to start the application \""
                                                            + application
                                                                    .getName()
                                                            + "\". Client connector for protocol "
                                                            + clientProtocol
                                                                    .getName()
                                                            + " is missing.");
                                    result = false;
                                }
                            }
                        }

                        if (application.getConnectorService()
                                .getServerProtocols() != null) {
                            for (Protocol serverProtocol : application
                                    .getConnectorService().getServerProtocols()) {
                                boolean serverFound = false;

                                // Try to find a server connector matching the
                                // server protocol
                                Server server;
                                for (Iterator<Server> iter = getHelped()
                                        .getServers().iterator(); !serverFound
                                        && iter.hasNext();) {
                                    server = iter.next();
                                    serverFound = server.getProtocols()
                                            .contains(serverProtocol);
                                }

                                if (!serverFound) {
                                    getLogger()
                                            .severe(
                                                    "Unable to start the application \""
                                                            + application
                                                                    .getName()
                                                            + "\". Server connector for protocol "
                                                            + serverProtocol
                                                                    .getName()
                                                            + " is missing.");
                                    result = false;
                                }
                            }
                        }
                    }

                    if (result && application.isStopped()) {
                        application.start();
                    }
                }
            }
        }

        return result;
    }

    /**
     * Returns the internal client router.
     * 
     * @return the internal client router.
     */
    public ClientRouter getClientRouter() {
        return this.clientRouter;
    }

    /**
     * Returns the internal host router.
     * 
     * @return the internal host router.
     */
    public ServerRouter getServerRouter() {
        return this.serverRouter;
    }

    /**
     * Sets the internal server router.
     * 
     * @param serverRouter
     *            The internal host router.
     */
    public void setServerRouter(ServerRouter serverRouter) {
        this.serverRouter = serverRouter;
    }

    @Override
    public synchronized void start() throws Exception {
        // Checking if all applications have proper connectors
        boolean success = checkVirtualHost(getHelped().getDefaultHost());

        if (success) {
            for (VirtualHost host : getHelped().getHosts()) {
                success = success && checkVirtualHost(host);
            }
        }

        // Let's actually start the component
        if (!success) {
            getHelped().stop();
        } else {
            // Attach the service inbound filters
            Filter inboundFilter = null;

            for (Service service : getHelped().getServices()) {
                if (service.isEnabled()) {
                    inboundFilter = service.createInboundFilter(getContext()
                            .createChildContext());

                    if (inboundFilter != null) {
                        addFilter(inboundFilter);
                    }
                }
            }

            // Re-attach the original filter's attached Restlet
            setNext(getServerRouter());
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        // Stop the server's router
        getServerRouter().stop();

        // Stop all applications
        stopHostApplications(getHelped().getDefaultHost());

        for (VirtualHost host : getHelped().getHosts()) {
            stopHostApplications(host);
        }
    }

    /**
     * Stop all applications attached to a virtual host
     * 
     * @param host
     * @throws Exception
     */
    private void stopHostApplications(VirtualHost host) throws Exception {
        for (TemplateRoute route : host.getRoutes()) {
            if (route.getNext().isStarted()) {
                route.getNext().stop();
            }
        }
    }

    /**
     * Set the new server router that will compute the new routes when the first
     * request will be received (automatic start).
     */
    @Override
    public void update() throws Exception {
        // Note the old router to be able to stop it at the end
        ServerRouter oldRouter = getServerRouter();

        // Set the new server router that will compute the new routes when the
        // first request will be received (automatic start).
        setServerRouter(new ServerRouter(getHelped()));

        // Replace the old server router
        setNext(getServerRouter());

        // Stop the old server router
        if (oldRouter != null) {
            oldRouter.stop();
        }
    }

}
