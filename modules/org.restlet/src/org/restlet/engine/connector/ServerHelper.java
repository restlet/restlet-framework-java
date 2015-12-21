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

    // [ifndef gae] method
    /**
     * Sets the ephemeral port in the attributes map if necessary.
     * 
     * @param socket
     *            The bound server socket.
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
