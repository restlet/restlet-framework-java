/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
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

    private static final String ATTRIBUTE_EPHEMERAL_PORT = "ephemeralPort";

    /**
     * Constructor.
     *
     * @param server The client to help.
     */
    public ServerHelper(Server server) {
        super(server);
        clearEphemeralPort();
    }

    /**
     * Handles a call by invoking the helped Server's {@link Server#handle(Request, Response)}
     * method.
     *
     * @param request The request to handle.
     * @param response The response to modify.
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
        // If an ephemeral port is used, make sure we update the attribute for the API
        if (getHelped().getPort() == 0) {
            getAttributes().put(ATTRIBUTE_EPHEMERAL_PORT, localPort);
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
        clearEphemeralPort();
    }

    private void clearEphemeralPort() {
        // Clear the ephemeral port
        getAttributes().put(ATTRIBUTE_EPHEMERAL_PORT, -1);
    }
}
