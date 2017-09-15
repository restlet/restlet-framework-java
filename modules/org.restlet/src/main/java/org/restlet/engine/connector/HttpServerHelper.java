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

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import org.restlet.Server;
import org.restlet.data.Protocol;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Internal HTTP server connector.
 * 
 * @author Jerome Louvel
 */
public class HttpServerHelper extends NetServerHelper {
    /** The underlying HTTP server. */
    private volatile HttpServer server;

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public HttpServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTP);
    }

    @Override
    public void start() throws Exception {
        // Use ephemeral port
        int port = getHelped().getPort() > 0 ? getHelped().getPort() : 0;
        if (getHelped().getAddress() != null) {
            // This call may throw UnknownHostException and otherwise always
            // returns an instance of INetAddress.
            // Note: textual representation of inet addresses are supported
            InetAddress iaddr = InetAddress.getByName(getHelped().getAddress());

            // Note: the backlog of 50 is the default
            setAddress(new InetSocketAddress(iaddr, port));
        } else {
            // Listens to any local IP address
            setAddress(new InetSocketAddress(port));
        }

        this.server = HttpServer.create(getAddress(), 0);
        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange httpExchange) throws IOException {
                HttpServerHelper.this.handle(new HttpExchangeCall(getHelped(),
                        httpExchange));
            }
        });
        // creates a default executor
        server.setExecutor(createThreadPool());
        server.start();

        setConfidential(false);
        setEphemeralPort(server.getAddress().getPort());
        super.start();
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        this.server.stop(0);
    }
}
