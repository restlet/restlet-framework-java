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

package org.restlet.ext.simple.internal;

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
 * @deprecated Will be removed to favor lower-level network extensions such as
 *             Jetty and Netty, allowing more control at the Restlet API level.
 */
@Deprecated
public class SimpleServer implements Server {
    public static final String PROPERTY_ENGINE = "org.restlet.ext.simple.engine";

    public static final String PROPERTY_SOCKET = "org.restlet.ext.simple.socket";

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
        Map<String, Object> map = socket.getAttributes();
        SSLEngine engine = socket.getEngine();
        SocketChannel channel = socket.getChannel();

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
