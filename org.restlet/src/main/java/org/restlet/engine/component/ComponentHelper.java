/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.component;

import java.util.Iterator;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.CompositeHelper;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.VirtualHost;
import org.restlet.service.ConnectorService;
import org.restlet.service.Service;

/**
 * Component helper.
 *
 * @author Jerome Louvel
 */
public class ComponentHelper extends CompositeHelper<Component> {
    /** The internal client router. */
    private final ClientRouter clientRouter;

    /** The internal server router. */
    private volatile ServerRouter serverRouter;

    /**
     * Constructor.
     *
     * @param component The helper component.
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
     * @param host The parent virtual host.
     * @return True if the check succeeded.
     * @throws Exception
     */
    private boolean checkVirtualHost(VirtualHost host) throws Exception {
        if (host == null) {
            return true;
        }

        boolean result = true;
        for (Route route : host.getRoutes()) {
            Restlet next = route.getNext();

            if (next instanceof Application application) {
                result = checkApplication(application, result);
            }
        }

        return result;
    }

    private boolean checkApplication(final Application application, boolean result)
            throws Exception {
        final ConnectorService connectorService = application.getConnectorService();

        if (connectorService != null) {
            if (connectorService.getClientProtocols() != null) {
                for (Protocol clientProtocol : connectorService.getClientProtocols()) {
                    result = checkClientConnector(application, clientProtocol, result);
                }
            }

            if (connectorService.getServerProtocols() != null) {
                for (Protocol serverProtocol : connectorService.getServerProtocols()) {
                    result = checkServerConnector(application, serverProtocol, result);
                }
            }
        }

        if (result && application.isStopped()) {
            application.start();
        }
        return result;
    }

    private boolean checkServerConnector(
            final Application application, final Protocol serverProtocol, boolean result) {
        boolean serverFound = false;

        // Try to find a server connector matching the server protocol
        Server server;
        for (Iterator<Server> iter = getHelped().getServers().iterator();
                !serverFound && iter.hasNext(); ) {
            server = iter.next();
            serverFound = server.getProtocols().contains(serverProtocol);
        }

        if (!serverFound) {
            getLogger()
                    .severe(
                            "Unable to start the application \""
                                    + application.getName()
                                    + "\". Server connector for protocol "
                                    + serverProtocol.getName()
                                    + " is missing.");
            result = false;
        }
        return result;
    }

    private boolean checkClientConnector(
            final Application application, final Protocol clientProtocol, boolean result) {
        boolean clientFound = false;

        // Try to find a client connector matching the client protocol
        Client client;
        for (Iterator<Client> iter = getHelped().getClients().iterator();
                !clientFound && iter.hasNext(); ) {
            client = iter.next();
            clientFound = client.getProtocols().contains(clientProtocol);
        }

        if (!clientFound) {
            getLogger()
                    .severe(
                            "Unable to start the application \""
                                    + application.getName()
                                    + "\". Client connector for protocol "
                                    + clientProtocol.getName()
                                    + " is missing.");
            result = false;
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
     * @param serverRouter The internal host router.
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
        if (success) {
            attachServicesFilters();

            // Re-attach the original filter's attached Restlet
            setInboundNext(getServerRouter());
        } else {
            getHelped().stop();
        }
    }

    private void attachServicesFilters() {
        Filter filter;

        for (Service service : getHelped().getServices()) {
            if (service.isEnabled()) {
                // Attach the service inbound filters
                Context context = (getContext() == null) ? null : getContext().createChildContext();

                filter = service.createInboundFilter(context);
                if (filter != null) {
                    addInboundFilter(filter);
                }

                // Attach the service outbound filters
                context = (getContext() == null) ? null : getContext().createChildContext();

                filter = service.createOutboundFilter(context);
                if (filter != null) {
                    addOutboundFilter(filter);
                }
            }
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
        for (Route route : host.getRoutes()) {
            if (route.getNext().isStarted()) {
                route.getNext().stop();
            }
        }
    }

    /**
     * Set the new server router that will compute the new routes when the first request is received
     * (automatic start).
     */
    @Override
    public void update() throws Exception {
        // Note the old router to be able to stop it at the end
        ServerRouter oldRouter = getServerRouter();

        // Set the new server router that will compute the new routes when the
        // first request is received (automatic start).
        setServerRouter(new ServerRouter(getHelped()));

        // Replace the old server router
        setInboundNext(getServerRouter());

        // Stop the old server router
        if (oldRouter != null) {
            oldRouter.stop();
        }
    }
}
