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

package com.noelios.restlet.component;

import java.util.Iterator;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Server;
import org.restlet.VirtualHost;
import org.restlet.data.Protocol;

import com.noelios.restlet.ContextHelper;
import com.noelios.restlet.StatusFilter;

/**
 * Component helper.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ComponentHelper extends ContextHelper<Component> {
    /** The internal client router. */
    private volatile ClientRouter clientRouter;

    /** The internal server router. */
    private volatile ServerRouter serverRouter;

    /**
     * Constructor.
     * 
     * @param component
     *                The helper component.
     */
    public ComponentHelper(Component component) {
        super(component);
        this.clientRouter = new ClientRouter(getHelped());
        this.serverRouter = new ServerRouter(getHelped());
    }

    /**
     * Check the applications attached to a virtual host.
     * 
     * @param host
     *                The parent virtual host.
     * @return True if the check succeeded.
     * @throws Exception
     */
    private boolean checkVirtualHost(VirtualHost host) throws Exception {
        boolean result = true;

        if (host != null) {
            for (Route route : host.getRoutes()) {
                Restlet next = route.getNext();

                if (next instanceof Application) {
                    Application application = (Application) next;

                    for (Protocol clientProtocol : application
                            .getConnectorService().getClientProtocols()) {
                        boolean clientFound = false;

                        // Try to find a client connector matching the client
                        // protocol
                        Client client;
                        for (Iterator<Client> iter = getHelped().getClients()
                                .iterator(); !clientFound && iter.hasNext();) {
                            client = iter.next();
                            clientFound = client.getProtocols().contains(
                                    clientProtocol);
                        }

                        if (!clientFound) {
                            getHelped()
                                    .getLogger()
                                    .severe(
                                            "Unable to start the application \""
                                                    + application.getName()
                                                    + "\". Client connector for protocol "
                                                    + clientProtocol.getName()
                                                    + " is missing.");
                            result = false;
                        }
                    }

                    for (Protocol serverProtocol : application
                            .getConnectorService().getServerProtocols()) {
                        boolean serverFound = false;

                        // Try to find a server connector matching the server
                        // protocol
                        Server server;
                        for (Iterator<Server> iter = getHelped().getServers()
                                .iterator(); !serverFound && iter.hasNext();) {
                            server = iter.next();
                            serverFound = server.getProtocols().contains(
                                    serverProtocol);
                        }

                        if (!serverFound) {
                            getHelped()
                                    .getLogger()
                                    .severe(
                                            "Unable to start the application \""
                                                    + application.getName()
                                                    + "\". Server connector for protocol "
                                                    + serverProtocol.getName()
                                                    + " is missing.");
                            result = false;
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

    @Override
    public Context createContext(String loggerName) {
        return new ComponentContext(this, Logger.getLogger(loggerName));
    }

    /**
     * Creates a new status filter. Allows overriding.
     * 
     * @param component
     *                The parent component.
     * @return The new status filter.
     */
    protected StatusFilter createStatusFilter(Component component) {
        return new ComponentStatusFilter(component);
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
     *                The internal host router.
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
            // Logging of calls
            if (getHelped().getLogService().isEnabled()) {
                addFilter(createLogFilter(getHelped().getContext(), getHelped()
                        .getLogService()));
            }

            // Addition of status pages
            if (getHelped().getStatusService().isEnabled()) {
                addFilter(createStatusFilter(getHelped()));
            }

            // Reattach the original filter's attached Restlet
            setNext(getServerRouter());
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        // Stop the server's router
        getServerRouter().stop();

        // Stop all applications
        stopVirtualHostApplications(getHelped().getDefaultHost());
        for (VirtualHost host : getHelped().getHosts()) {
            stopVirtualHostApplications(host);
        }
    }

    /**
     * Stop all applications attached to a virtual host
     * 
     * @param host
     * @throws Exception
     */
    private void stopVirtualHostApplications(VirtualHost host) throws Exception {
        for (Route route : host.getRoutes()) {
            if (route.getNext().isStarted()) {
                route.getNext().stop();
            }
        }
    }

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
