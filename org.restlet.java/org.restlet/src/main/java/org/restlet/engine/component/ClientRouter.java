/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.component;

import org.restlet.*;
import org.restlet.routing.Router;

import java.util.logging.Level;

/**
 * Router that collects calls from all applications and dispatches them to the
 * appropriate client connectors.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class ClientRouter extends Router {
	/** The parent component. */
	private volatile Component component;

	/**
	 * Constructor.
	 * 
	 * @param component The parent component.
	 */
	public ClientRouter(Component component) {
		super((component == null) ? null : component.getContext().createChildContext());
		this.component = component;
	}

	@Override
	protected void logRoute(org.restlet.routing.Route route) {
		if (getLogger().isLoggable(Level.FINE)) {
			if (route instanceof ClientRoute) {
				Client client = ((ClientRoute) route).getClient();

				getLogger().fine("This client was selected: \"" + client.getProtocols() + "\"");
			} else {
				super.logRoute(route);
			}
		}
	}

	@Override
	public Restlet getNext(Request request, Response response) {
		Restlet result = super.getNext(request, response);

		if (result == null) {
			getLogger().warning("The protocol used by this request is not declared in the list of client connectors. ("
					+ request.getResourceRef().getSchemeProtocol()
					+ "). In case you are using an instance of the Component class, check its \"clients\" property.");
		}
		return result;
	}

	/**
	 * Returns the parent component.
	 * 
	 * @return The parent component.
	 */
	private Component getComponent() {
		return this.component;
	}

	/** Starts the Restlet. */
	@Override
	public synchronized void start() throws Exception {
		for (final Client client : getComponent().getClients()) {
			getRoutes().add(new ClientRoute(this, client));
		}

		super.start();
	}
}
