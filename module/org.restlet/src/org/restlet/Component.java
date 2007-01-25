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

import java.util.ArrayList;
import java.util.List;

import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.service.LogService;
import org.restlet.service.StatusService;
import org.restlet.util.ClientList;
import org.restlet.util.Engine;
import org.restlet.util.Helper;
import org.restlet.util.ServerList;

/**
 * Restlet managing a set of Clients, Servers and other Restlets. "A component
 * is an abstract unit of software instructions and internal state that provides
 * a transformation of data via its interface." Roy T. Fielding
 * 
 * Component managing a set of VirtualHosts and Applications. Applications are
 * expected to be directly attached to VirtualHosts. Components are also
 * exposing a number of services in order to control several operational
 * features in a portable way, like access log and status setting.
 * 
 * @see <a
 *      href="http://www.ics.uci.edu/~fielding/pubs/dissertation/software_arch.htm#sec_1_2_1">Source
 *      dissertation</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Component extends Restlet {
    /** The modifiable list of client connectors. */
    private ClientList clients;

    /** The modifiable list of server connectors. */
    private ServerList servers;

    /** The modifiable list of virtual hosts. */
    private List<VirtualHost> hosts;

    /** The default host. */
    private VirtualHost defaultHost;

    /** The helper provided by the implementation. */
    private Helper helper;

    /** The log service. */
    private LogService logService;

    /** The status service. */
    private StatusService statusService;

    /**
     * Constructor.
     */
    public Component() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public Component(Context context) {
        super(context);
        if (Engine.getInstance() != null) {
            this.helper = Engine.getInstance().createHelper(this);
            if (this.helper != null) {
                setContext(this.helper.createContext(getClass()
                        .getCanonicalName()));
                this.hosts = null;
                this.defaultHost = new VirtualHost(getContext());
                this.logService = null;
                this.statusService = null;
            }
        }
    }

    /**
     * Returns the modifiable list of client connectors.
     * 
     * @return The modifiable list of client connectors.
     */
    public ClientList getClients() {
        if (this.clients == null)
            this.clients = new ClientList(getContext());
        return this.clients;
    }

    /**
     * Returns the modifiable list of server connectors.
     * 
     * @return The modifiable list of server connectors.
     */
    public ServerList getServers() {
        if (this.servers == null)
            this.servers = new ServerList(getContext(), this);
        return this.servers;
    }

    /**
     * Start hook. Starts all connectors.
     */
    public void start() throws Exception {
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

    /**
     * Stop hook. Stops all connectors.
     */
    public void stop() throws Exception {
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
     * Returns the modifiable list of host routers.
     * 
     * @return The modifiable list of host routers.
     */
    public List<VirtualHost> getHosts() {
        if (this.hosts == null)
            this.hosts = new ArrayList<VirtualHost>();
        return this.hosts;
    }

    /**
     * Returns the global log service. This service is enabled by default.
     * 
     * @return The global log service.
     */
    public LogService getLogService() {
        if (this.logService == null) {
            this.logService = new LogService(true);
            this.logService.setLoggerName(getClass().getCanonicalName()
                    + " (" + hashCode() + ")");
        }

        return this.logService;
    }

    /**
     * Returns the status service. This service is enabled by default.
     * 
     * @return The status service.
     */
    public StatusService getStatusService() {
        if (this.statusService == null)
            this.statusService = new StatusService(true);
        return this.statusService;
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
        init(request, response);
        if (getHelper() != null)
            getHelper().handle(request, response);
    }

}
