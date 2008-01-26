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
import java.util.logging.Level;
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
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

import com.noelios.restlet.ChainHelper;
import com.noelios.restlet.StatusFilter;

/**
 * Component helper.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ComponentHelper extends ChainHelper {
    /** The internal client router. */
    private volatile ClientRouter clientRouter;

    /** The helped component. */
    private volatile Component component;

    /** The internal server router. */
    private volatile ServerRouter serverRouter;

    /**
     * Constructor.
     * 
     * @param component
     *                The helper component.
     */
    public ComponentHelper(Component component) {
        super(null);
        this.component = component;
        this.clientRouter = new ClientRouter(getComponent());
        this.serverRouter = new ServerRouter(getComponent());
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
                        for (Iterator<Client> iter = getComponent()
                                .getClients().iterator(); !clientFound
                                && iter.hasNext();) {
                            client = iter.next();
                            clientFound = client.getProtocols().contains(
                                    clientProtocol);
                        }

                        if (!clientFound) {
                            getComponent()
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
                        for (Iterator<Server> iter = getComponent()
                                .getServers().iterator(); !serverFound
                                && iter.hasNext();) {
                            server = iter.next();
                            serverFound = server.getProtocols().contains(
                                    serverProtocol);
                        }

                        if (!serverFound) {
                            getComponent()
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

    /**
     * Creates a new context.
     * 
     * @param loggerName
     *                The JDK's logger name to use for contextual logging.
     * @return The new context.
     */
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
     * Returns the helped component.
     * 
     * @return The helped component.
     */
    protected Component getComponent() {
        return this.component;
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
     * Handles a call.
     * 
     * @param request
     *                The request to handle.
     * @param response
     *                The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        if (getFirst() != null) {
            getFirst().handle(request, response);
        } else {
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            getComponent()
                    .getLogger()
                    .log(Level.SEVERE,
                            "The component wasn't properly started, it can't handle calls.");
        }
    }

    /** Start callback. */
    @Override
    public synchronized void start() throws Exception {
        // Checking if all applications have proper connectors
        boolean success = checkVirtualHost(getComponent().getDefaultHost());
        if (success) {
            for (VirtualHost host : getComponent().getHosts()) {
                success = success && checkVirtualHost(host);
            }
        }

        // Let's actually start the component
        if (!success) {
            getComponent().stop();
        } else {
            // Logging of calls
            if (getComponent().getLogService().isEnabled()) {
                addFilter(createLogFilter(getComponent().getContext(),
                        getComponent().getLogService()));
            }

            // Addition of status pages
            if (getComponent().getStatusService().isEnabled()) {
                addFilter(createStatusFilter(getComponent()));
            }

            // Reattach the original filter's attached Restlet
            setNext(getServerRouter());
        }
    }

    /** Stop callback. */
    @Override
    public synchronized void stop() throws Exception {
        // Stop the server's router
        getServerRouter().stop();

        // Stop all applications
        stopVirtualHostApplications(getComponent().getDefaultHost());
        for (VirtualHost host : getComponent().getHosts()) {
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
        setServerRouter(new ServerRouter(getComponent()));

        // Replace the old server router
        setNext(getServerRouter());

        // Stop the old server router
        if (oldRouter != null) {
            oldRouter.stop();
        }
    }

    /**
     * Sets the internal host router.
     * 
     * @param serverRouter
     *                The internal host router.
     */
    public void setServerRouter(ServerRouter serverRouter) {
        this.serverRouter = serverRouter;
    }

}
