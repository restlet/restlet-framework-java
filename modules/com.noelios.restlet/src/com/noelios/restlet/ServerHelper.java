/*
 * Copyright 2005-2008 Noelios Consulting.
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

package com.noelios.restlet;

import java.net.ServerSocket;

import org.restlet.Server;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Server connector helper.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ServerHelper extends ConnectorHelper<Server> {

    /**
     * Constructor.
     * 
     * @param server
     *            The client to help.
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
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);
        getHelped().handle(request, response);
    }

    /**
     * Sets the ephemeral port in the attributes map if necessary.
     * 
     * @param localPort
     *            The ephemeral local port.
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
     * @param socket
     *            The bound server socket.
     */
    public void setEphemeralPort(ServerSocket socket) {
        setEphemeralPort(socket.getLocalPort());
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();

        // Clear the ephemeral port
        getAttributes().put("ephemeralPort", -1);
    }

}
