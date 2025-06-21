/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.connector;

import org.restlet.Connector;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.engine.RestletHelper;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base connector helper.
 *
 * @author Jerome Louvel
 */
public abstract class ConnectorHelper<T extends Connector> extends RestletHelper<T> {

	/**
	 * Returns the connector service associated to a request.
	 *
	 * @return The connector service associated to a request.
	 */
	public static org.restlet.service.ConnectorService getConnectorService() {
		org.restlet.service.ConnectorService result = null;
		org.restlet.Application application = org.restlet.Application.getCurrent();

		if (application != null) {
			result = application.getConnectorService();
		} else {
			result = new org.restlet.service.ConnectorService();
		}

		return result;
	}

	/** The protocols simultaneously supported. */
	private final List<Protocol> protocols;

	/**
	 * Constructor.
	 */
	public ConnectorHelper(T connector) {
		super(connector);
		this.protocols = new CopyOnWriteArrayList<Protocol>();
	}

	/**
	 * Returns the helped Restlet context.
	 *
	 * @return The helped Restlet context.
	 */
	@Override
	public Context getContext() {
		return super.getContext();
	}

	/**
	 * Returns the protocols simultaneously supported.
	 *
	 * @return The protocols simultaneously supported.
	 */
	public List<Protocol> getProtocols() {
		return this.protocols;
	}

	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {
	}

	@Override
	public void update() throws Exception {
	}

}
