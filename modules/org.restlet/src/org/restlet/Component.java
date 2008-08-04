/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
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
		super(null);

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
	 * Starts the component and all its connectors.
	 */
	@Override
	public void start() throws Exception {
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
		if (this.logService == null) {
			this.logService = new LogService(true);
			this.logService.setLoggerName(getClass().getCanonicalName() + " ("
					+ hashCode() + ")");
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
	@Override
	public void handle(Request request, Response response) {
		init(request, response);
		if (getHelper() != null)
			getHelper().handle(request, response);
	}

	/**
	 * Sets the global log service.
	 * 
	 * @param logService
	 *            The global log service.
	 */
	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	/**
	 * Sets the status service.
	 * 
	 * @param statusService
	 *            The status service.
	 */
	public void setStatusService(StatusService statusService) {
		this.statusService = statusService;
	}

}
