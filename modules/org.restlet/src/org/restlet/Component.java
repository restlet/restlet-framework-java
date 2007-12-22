/*
 * Copyright 2005-2007 Noelios Consulting.
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

package org.restlet;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.service.LogService;
import org.restlet.service.StatusService;
import org.restlet.util.ClientList;
import org.restlet.util.Engine;
import org.restlet.util.Helper;
import org.restlet.util.ServerList;

/**
 * Restlet managing a set of Connectors, VirtualHosts and Applications.
 * Applications are expected to be directly attached to VirtualHosts. Components
 * also expose several services: access logging and status setting. <br>
 * <br>
 * From an architectural point of view, here is the REST definition: "A
 * component is an abstract unit of software instructions and internal state
 * that provides a transformation of data via its interface." Roy T. Fielding<br>
 * <br>
 * 
 * @see <a
 *      href="http://roy.gbiv.com/pubs/dissertation/software_arch.htm#sec_1_2_1">Source
 *      dissertation</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Component extends Restlet {
    /** The modifiable list of client connectors. */
    private volatile ClientList clients;

    /** The default host. */
    private volatile VirtualHost defaultHost;

    /** The helper provided by the implementation. */
    private volatile Helper helper;

    /** The modifiable list of virtual hosts. */
    private volatile List<VirtualHost> hosts;

    /**
     * The private internal router that can be addressed via the RIAP client
     * connector.
     */
    private volatile Router internalRouter;

    /** The log service. */
    private volatile LogService logService;

    /** The modifiable list of server connectors. */
    private volatile ServerList servers;

    /** The status service. */
    private volatile StatusService statusService;

    /**
     * Constructor.
     */
    public Component() {
        super(null);

        if (Engine.getInstance() != null) {
            this.helper = Engine.getInstance().createHelper(this);

            if (this.helper != null) {
                setContext(this.helper.createContext(getClass()
                        .getCanonicalName()));
                this.hosts = new CopyOnWriteArrayList<VirtualHost>();
                this.defaultHost = new VirtualHost(getContext());
                this.internalRouter = new Router(getContext());
                this.logService = new LogService(true);
                this.logService.setLoggerName(getClass().getCanonicalName()
                        + " (" + hashCode() + ")");
                this.statusService = new StatusService(true);
                this.clients = new ClientList(getContext());
                this.servers = new ServerList(getContext(), this);
            }
        }
    }

    /**
     * Returns a modifiable list of client connectors. Creates a new instance if
     * no one has been set.
     * 
     * @return A modifiable list of client connectors.
     */
    public ClientList getClients() {
        return this.clients;
    }

    /**
     * Returns the default virtual host.
     * 
     * @return The default virtual host.
     */
    public VirtualHost getDefaultHost() {
        return this.defaultHost;
    }

    /**
     * Returns the helper provided by the implementation.
     * 
     * @return The helper provided by the implementation.
     */
    private Helper getHelper() {
        return this.helper;
    }

    /**
     * Returns the modifiable list of virtual hosts. Creates a new instance if
     * no one has been set.
     * 
     * @return The modifiable list of virtual hosts.
     */
    public List<VirtualHost> getHosts() {
        return this.hosts;
    }

    /**
     * Returns the private internal router were Restlets like Applications can
     * be attached. Those Restlets can be addressed via the
     * {@link org.restlet.data.Protocol#RIAP} (Restlet Internal Access Protocol)
     * client connector. This is used to manage private, internal and optimized
     * access to local applications.<br>
     * <br>
     * The first use case is the modularisation of a large application into
     * modules or layers. This can also be achieved using the
     * {@link Context#getServerDispatcher()} method, but the internal router is
     * easily addressable via an URI scheme and can be fully private to the
     * current Component.<br>
     * <br>
     * The second use case is the composition/mash-up of several representations
     * via the {@link org.restlet.Transformer} class for example. For this you
     * can leverage the XPath's document() function or the XSLT's include and
     * import elements with RIAP URIs.
     * 
     * @return The private internal router.
     */
    public Router getInternalRouter() {
        return internalRouter;
    }

    /**
     * Returns the global log service. On the first call, if no log service was
     * defined via the {@link #setLogService(LogService)} method, then a default
     * logger service is created. This default service is enabled by default and
     * has a logger name composed of the canonical name of the current
     * component's class or subclass, appended with the instance hash code
     * between parenthesis (eg. "com.mycompany.MyComponent(1439)").
     * 
     * @return The global log service.
     */
    public LogService getLogService() {
        return this.logService;
    }

    /**
     * Returns the modifiable list of server connectors. Creates a new instance
     * if no one has been set.
     * 
     * @return The modifiable list of server connectors.
     */
    public ServerList getServers() {
        return this.servers;
    }

    /**
     * Returns the status service, enabled by default. Creates a new instance if
     * no one has been set.
     * 
     * @return The status service.
     */
    public StatusService getStatusService() {
        return this.statusService;
    }

    @Override
    public void handle(Request request, Response response) {
        init(request, response);
        if (getHelper() != null)
            getHelper().handle(request, response);
    }

    /**
     * Sets a modifiable list of client connectors.
     * 
     * @param clients
     *                A modifiable list of client connectors.
     */
    public void setClients(ClientList clients) {
        this.clients = clients;
    }

    /**
     * Sets the default virtual host.
     * 
     * @param defaultHost
     *                The default virtual host.
     */
    public void setDefaultHost(VirtualHost defaultHost) {
        this.defaultHost = defaultHost;
    }

    /**
     * Sets the modifiable list of virtual hosts.
     * 
     * @param hosts
     *                The modifiable list of virtual hosts.
     */
    public void setHosts(List<VirtualHost> hosts) {
        this.hosts = hosts;
    }

    /**
     * Sets the private internal router were Restlets like Applications can be
     * attached.
     * 
     * @param internalRouter
     *                The private internal router.
     * @see #getInternalRouter()
     */
    public void setInternalRouter(Router internalRouter) {
        this.internalRouter = internalRouter;
    }

    /**
     * Sets the global log service.
     * 
     * @param logService
     *                The global log service.
     */
    public void setLogService(LogService logService) {
        this.logService = logService;
    }

    /**
     * Sets a modifiable list of server connectors.
     * 
     * @param servers
     *                A modifiable list of server connectors.
     */
    public void setServers(ServerList servers) {
        this.servers = servers;
    }

    /**
     * Sets the status service.
     * 
     * @param statusService
     *                The status service.
     */
    public void setStatusService(StatusService statusService) {
        this.statusService = statusService;
    }

    /**
     * Starts all the connectors then the component.
     */
    @Override
    public synchronized void start() throws Exception {
        if (isStopped()) {
            if (this.clients != null) {
                for (Client client : this.clients) {
                    client.start();
                }
            }

            if (this.servers != null) {
                for (Server server : this.servers) {
                    server.start();
                }
            }

            if (getHelper() != null)
                getHelper().start();

            super.start();
        }
    }

    /**
     * Stops the component and all its connectors.
     */
    @Override
    public synchronized void stop() throws Exception {
        if (getHelper() != null)
            getHelper().stop();

        if (this.clients != null) {
            for (Client client : this.clients) {
                client.stop();
            }
        }

        if (this.servers != null) {
            for (Server server : this.servers) {
                server.stop();
            }
        }

        super.stop();
    }

}
