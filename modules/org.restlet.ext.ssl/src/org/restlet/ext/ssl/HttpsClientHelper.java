/**
 * Copyright 2005-2010 Noelios Technologies.
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

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.engine.connector.ClientConnectionHelper;
import org.restlet.engine.connector.Connection;
import org.restlet.engine.connector.ConnectionController;
import org.restlet.engine.connector.HttpClientInboundWay;
import org.restlet.engine.connector.HttpClientOutboundWay;
import org.restlet.engine.connector.InboundWay;
import org.restlet.engine.connector.OutboundWay;
import org.restlet.engine.security.SslContextFactory;
import org.restlet.engine.security.SslUtils;
import org.restlet.ext.ssl.internal.SslConnection;
import org.restlet.ext.ssl.internal.SslManager;

/**
 * HTTPS client helper based on NIO blocking sockets.
 * 
 * @author Jerome Louvel
 */
public class HttpsClientHelper extends ClientConnectionHelper {

    /** The SSL context. */
    private volatile SSLContext sslContext;

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public HttpsClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.HTTPS);
    }

    @Override
    protected Connection<Client> createConnection(SocketChannel socketChannel,
            ConnectionController controller, InetSocketAddress socketAddress)
            throws IOException {
        SslManager sslManager = new SslManager(getSslContext(), socketAddress,
                isClientSide());
        return new SslConnection<Client>(this, socketChannel, controller,
                socketAddress, sslManager);
    }

    @Override
    public InboundWay createInboundWay(Connection<Client> connection,
            int bufferSize) {
        return new HttpClientInboundWay(connection, bufferSize);
    }

    @Override
    public OutboundWay createOutboundWay(Connection<Client> connection,
            int bufferSize) {
        return new HttpClientOutboundWay(connection, bufferSize);
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
