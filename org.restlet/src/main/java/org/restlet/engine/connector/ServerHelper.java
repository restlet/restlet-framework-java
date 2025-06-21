/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.connector;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;

/**
 * Server connector helper.
 * 
 * @author Jerome Louvel
 */
public class ServerHelper extends ConnectorHelper<Server> {

	/**
	 * Constructor.
	 * 
	 * @param server The client to help.
	 */
	public ServerHelper(Server server) {
		super(server);

		// Clear the ephemeral port
		getAttributes().put("ephemeralPort", -1);
	}

	/**
	 * Handles a call by invoking the helped Server's
	 * {@link Server#handle(Request, Response)} method.
	 * 
	 * @param request  The request to handle.
	 * @param response The response to update.
	 */
	@Override
	public void handle(Request request, Response response) {
		super.handle(request, response);
		getHelped().handle(request, response);
	}

	/**
	 * Sets the ephemeral port in the attributes map if necessary.
	 * 
	 * @param localPort The ephemeral local port.
	 */
	public void setEphemeralPort(int localPort) {
		// If an ephemeral port is used, make sure we update the attribute for
		// the API
		if (getHelped().getPort() == 0) {
			getAttributes().put("ephemeralPort", localPort);
		}
	}

	/**
	 * Sets the ephemeral port in the attributes map if necessary.
	 * 
	 * @param socket The bound server socket.
	 */
	public void setEphemeralPort(java.net.ServerSocket socket) {
		setEphemeralPort(socket.getLocalPort());
	}

	@Override
	public synchronized void stop() throws Exception {
		super.stop();

		// Clear the ephemeral port
		getAttributes().put("ephemeralPort", -1);
	}

}
