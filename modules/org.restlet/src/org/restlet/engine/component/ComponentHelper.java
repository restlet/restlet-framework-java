/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.component;

import java.util.Iterator;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.CompositeHelper;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.VirtualHost;
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
            for (Route route : host.getRoutes()) {
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
                                            .severe("Unable to start the application \""
                                                    + application.getName()
                                                    + "\". Client connector for protocol "
                                                    + clientProtocol.getName()
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
                                            .severe("Unable to start the application \""
                                                    + application.getName()
                                                    + "\". Server connector for protocol "
                                                    + serverProtocol.getName()
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
            Filter filter = null;

            for (Service service : getHelped().getServices()) {
                if (service.isEnabled()) {
                    // Attach the service inbound filters
                    filter = service
                            .createInboundFilter((getContext() == null) ? null
                                    : getContext().createChildContext());

                    if (filter != null) {
                        addInboundFilter(filter);
                    }

                    // Attach the service outbound filters
                    filter = service
                            .createOutboundFilter((getContext() == null) ? null
                                    : getContext().createChildContext());

                    if (filter != null) {
                        addOutboundFilter(filter);
                    }
                }
            }

            // Re-attach the original filter's attached Restlet
            setInboundNext(getServerRouter());
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
        setInboundNext(getServerRouter());

        // Stop the old server router
        if (oldRouter != null) {
            oldRouter.stop();
        }
    }

}
