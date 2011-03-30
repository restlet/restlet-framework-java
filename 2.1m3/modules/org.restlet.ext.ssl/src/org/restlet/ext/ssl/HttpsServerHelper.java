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

package org.restlet.ext.ssl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.connector.Connection;
import org.restlet.engine.connector.ConnectionController;
import org.restlet.engine.connector.HttpServerHelper;
import org.restlet.engine.connector.HttpServerInboundWay;
import org.restlet.engine.connector.HttpServerOutboundWay;
import org.restlet.engine.connector.OutboundWay;
import org.restlet.engine.connector.ServerInboundWay;
import org.restlet.engine.security.SslContextFactory;
import org.restlet.engine.security.SslUtils;
import org.restlet.ext.ssl.internal.SslConnection;

/**
 * HTTPS server helper based on NIO blocking sockets.
 * 
 * @author Jerome Louvel
 */
public class HttpsServerHelper extends HttpServerHelper {

    /** The SSL context. */
    private volatile SSLContext sslContext;

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public HttpsServerHelper(Server server) {
        super(server, Protocol.HTTPS);
    }

    @Override
    protected Connection<Server> createConnection(SocketChannel socketChannel,
            ConnectionController controller, InetSocketAddress socketAddress)
            throws IOException {
        // Create the SSL engine
        SSLEngine engine;

        if (socketAddress != null) {
            engine = getSslContext().createSSLEngine(
                    socketAddress.getHostName(), socketAddress.getPort());
        } else {
            engine = getSslContext().createSSLEngine();
        }

        return new SslConnection<Server>(this, socketChannel, controller,
                socketAddress, engine);
    }

    @Override
    public ServerInboundWay createInboundWay(Connection<Server> connection,
            int bufferSize) {
        return new HttpServerInboundWay(connection, bufferSize);
    }

    @Override
    public OutboundWay createOutboundWay(Connection<Server> connection,
            int bufferSize) {
        return new HttpServerOutboundWay(connection, bufferSize);
    }

    /**
     * Returns the SSL context.
     * 
     * @return The SSL context.
     */
    protected SSLContext getSslContext() {
        return sslContext;
    }

    /**
     * Sets the SSL context.
     * 
     * @param sslContext
     *            The SSL context.
     */
    protected void setSslContext(SSLContext sslContext) {
        this.sslContext = sslContext;
    }

    @Override
    public synchronized void start() throws Exception {
        SslContextFactory factory = SslUtils.getSslContextFactory(this);
        setSslContext(factory.createSslContext());
        super.start();
    }

}
