/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.simple;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Map;

import javax.net.ssl.SSLEngine;

import org.simpleframework.transport.Server;
import org.simpleframework.transport.Socket;

/**
 * A subclass of BufferedPipelineFactory that sets the connection socket on each
 * pipeline for later retrieval.
 * 
 * @author Jerome Louvel
 */
public class SimpleServer implements Server {
    public static final String PROPERTY_SOCKET = "org.restlet.ext.simple.socket";

    public static final String PROPERTY_ENGINE = "org.restlet.ext.simple.engine";

    /**
     * This is the server to be used.
     */
    private final Server server;

    /**
     * Constructor.
     */
    public SimpleServer(Server server) {
        this.server = server;
    }

    /**
     * Pass in the connection socket and add the engine to the pipeline
     * attributes.
     * 
     * @param socket
     *            the pipeline
     */
    @SuppressWarnings("unchecked")
    public void process(Socket socket) throws IOException {
        final Map map = socket.getAttributes();
        final SSLEngine engine = socket.getEngine();
        final SocketChannel channel = socket.getChannel();

        map.put(PROPERTY_ENGINE, engine);
        map.put(PROPERTY_SOCKET, channel);

        server.process(socket);
    }

    /**
     * This is used to stop the internal server.
     */
    public void stop() throws IOException {
        server.stop();
    }
}
