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

package org.restlet.test.ext.jaxrs.server;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * This class allows easy testing of JAX-RS implementations by starting a server
 * for a given class and access the server for a given sub pass relativ to the
 * pass of the root resource class.
 * 
 * @author Stephan Koops
 */
public class RestletServerWrapper implements ServerWrapper {

    /**
     * @author Stephan
     * 
     */
    private final class ClientConnector extends Client {
        /**
         * @param protocol
         */
        private ClientConnector(Protocol protocol) {
            super(protocol);
        }

        @Override
        public void handle(Request request, Response response) {
            request.setOriginalRef(request.getResourceRef());
            super.handle(request, response);
        }
    }

    private Component component;

    public RestletServerWrapper() {
    }

    /**
     * @see org.restlet.test.ext.jaxrs.server.ServerWrapper#getClientConnector()
     */
    public Restlet getClientConnector() {
        return new ClientConnector(Protocol.HTTP);
    }

    public int getServerPort() {
        if (this.component == null) {
            throw new IllegalStateException("the server is not started yet.");
        }
        final Server server = this.component.getServers().get(0);
        int port = server.getPort();
        if (port > 0) {
            return port;
        }
        port = server.getEphemeralPort();
        if (port > 0) {
            return port;
        }
        for (int i = 0; i < 100; i++) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                //
            }
            port = server.getEphemeralPort();
            if (port > 0) {
                return port;
            }
        }
        throw new IllegalStateException("Sorry, the port is not available");
    }

    /**
     * Starts the server with the given protocol on the given port with the
     * given Collection of root resource classes. The method {@link #setUp()}
     * will do this on every test start up.
     * 
     * @param appConfig
     * @throws Exception
     */
    public void startServer(Application application, Protocol protocol)
            throws Exception {

        final Component comp = new Component();
        comp.getServers().add(protocol, 0);

        // Attach the application to the component and start it
        comp.getDefaultHost().attach(application);
        comp.start();
        this.component = comp;
        System.out.println("listening on port " + getServerPort());
    }

    /**
     * Stops the component. The method {@link #tearDown()} do this after every
     * test.
     * 
     * @param component
     * @throws Exception
     */
    public void stopServer() throws Exception {
        if (this.component != null) {
            this.component.stop();
        }
    }
}
