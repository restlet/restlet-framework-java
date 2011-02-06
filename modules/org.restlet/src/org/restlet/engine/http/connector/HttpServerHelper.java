/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.http.connector;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.SocketChannel;

import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * HTTP server helper based on NIO blocking sockets.
 * 
 * @author Jerome Louvel
 */
public class HttpServerHelper extends BaseServerHelper {

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
    protected Connection<Server> createConnection(BaseHelper<Server> helper,
            Socket socket, SocketChannel socketChannel) throws IOException {
        return new ServerConnection(helper, socket, socketChannel);
    }

    @Override
    public synchronized void start() throws Exception {
        getLogger().info(
                "Starting the internal " + getProtocols().get(0).getName()
                        + " server on port " + getHelped().getPort());
        super.start();
    }

    @Override
    public synchronized void stop() throws Exception {
        getLogger().info("Stopping the internal HTTP server");
        super.stop();
    }

}
