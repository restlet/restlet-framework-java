/*
 * Copyright 2005-2006 Noelios Consulting.
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

package com.noelios.restlet.container;

import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Container;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.VirtualHost;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.util.Helper;

import com.noelios.restlet.LogFilter;
import com.noelios.restlet.StatusFilter;

/**
 * Container helper.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ContainerHelper extends Helper {
    /** The helped container. */
    private Container container;

    /** The first Restlet. */
    private Restlet first;

    /** The internal client router. */
    private ClientRouter clientRouter;

    /** The internal server router. */
    private ServerRouter serverRouter;

    /**
     * Constructor.
     */
    public ContainerHelper(Container container) {
        this.container = container;
        this.first = null;
        this.clientRouter = new ClientRouter(getContainer());
        this.serverRouter = new ServerRouter(getContainer());
    }

    /**
     * Creates a new context.
     * 
     * @return The new context.
     */
    public Context createContext() {
        return new ContainerContext(this);
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
     * Returns the helped container.
     * 
     * @return The helped container.
     */
    protected Container getContainer() {
        return this.container;
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
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    public void handle(Request request, Response response) {
        if (getFirst() != null) {
            getFirst().handle(request, response);
        } else {
            response.setStatus(Status.SERVER_ERROR_INTERNAL);
            getContainer()
                    .getLogger()
                    .log(Level.SEVERE,
                            "The container wasn't properly started, it can't handle calls.");
        }
    }

    /** Start callback. */
    public void start() throws Exception {
        // Initialization of services
        Filter lastFilter = null;

        // Checking if all applications have proper connectors
        boolean success = checkVirtualHost(getContainer().getDefaultHost());
        if (success) {
            for (VirtualHost host : getContainer().getHosts()) {
                success = success && checkVirtualHost(host);
            }
        }

        // Let's actually start the container
        if (!success) {
            getContainer().stop();
        } else {
            // Logging of calls
            if (getContainer().getLogService().isEnabled()) {
                lastFilter = createLogFilter(getContainer().getContext(),
                        getContainer().getLogService().getAccessLoggerName(),
                        getContainer().getLogService().getAccessLogFormat());
                setFirst(lastFilter);
            }

            // Addition of status pages
            if (getContainer().getStatusService().isEnabled()) {
                Filter statusFilter = createStatusFilter(getContainer());
                if (lastFilter != null)
                    lastFilter.setNext(statusFilter);
                if (getFirst() == null)
                    setFirst(statusFilter);
                lastFilter = statusFilter;
            }

            // Reattach the original filter's attached Restlet
            if (getFirst() == null) {
                setFirst(getServerRouter());
            } else {
                lastFilter.setNext(getServerRouter());
            }
        }
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
            for (Route route : host.getRoutes()) {
                Restlet next = route.getNext();

                if (next instanceof Application) {
                    Application application = (Application) next;

                    for (Protocol clientProtocol : application
                            .getConnectorService().getClientProtocols()) {
                        if (!getContainer().getClients().contains(
                                clientProtocol)) {
                            getContainer()
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
                        if (!getContainer().getServers().contains(
                                serverProtocol)) {
                            getContainer()
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
     * Creates a new log filter. Allows overriding.
     * 
     * @param context
     *            The context.
     * @param logName
     *            The log name to used in the logging.properties file.
     * @param logFormat
     *            The log format to use.
     * @return The new log filter.
     */
    protected LogFilter createLogFilter(Context context, String logName,
            String logFormat) {
        return new LogFilter(context, logName, logFormat);
    }

    /**
     * Creates a new status filter. Allows overriding.
     * 
     * @param container
     *            The parent container.
     * @return The new status filter.
     */
    protected StatusFilter createStatusFilter(Container container) {
        return new ContainerStatusFilter(container);
    }

    /** Stop callback. */
    public void stop() throws Exception {
        // Stop all applications
        for (VirtualHost host : getContainer().getHosts()) {
            for (Route route : host.getRoutes()) {
                Restlet next = route.getNext();

                if (next instanceof Application) {
                    Application application = (Application) next;

                    if (application.isStarted()) {
                        application.stop();
                    }
                }
            }
        }
    }

    /**
     * Returns the first Restlet.
     * 
     * @return the first Restlet.
     */
    private Restlet getFirst() {
        return this.first;
    }

    /**
     * Sets the first Restlet.
     * 
     * @param first
     *            The first Restlet.
     */
    private void setFirst(Restlet first) {
        this.first = first;
    }

}
