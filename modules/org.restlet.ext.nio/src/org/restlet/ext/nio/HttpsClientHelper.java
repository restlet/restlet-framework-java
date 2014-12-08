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

package org.restlet.ext.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.engine.ssl.SslContextFactory;
import org.restlet.engine.ssl.SslUtils;
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.ext.nio.internal.connection.SslConnection;
import org.restlet.ext.nio.internal.controller.ConnectionController;
import org.restlet.ext.nio.internal.way.HttpsClientInboundWay;
import org.restlet.ext.nio.internal.way.HttpsClientOutboundWay;
import org.restlet.ext.nio.internal.way.InboundWay;
import org.restlet.ext.nio.internal.way.OutboundWay;

/**
 * HTTPS client helper based on NIO blocking sockets. Here is the list of SSL
 * related parameters that are also supported:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>sslContextFactory</td>
 * <td>String</td>
 * <td>org.restlet.engine.ssl.DefaultSslContextFactory</td>
 * <td>Let you specify a {@link SslContextFactory} qualified class name as a
 * parameter, or an instance as an attribute for a more complete and flexible
 * SSL context setting.</td>
 * </tr>
 * </table>
 * For the default SSL parameters see the Javadocs of the
 * {@link DefaultSslContextFactory} class.
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
    public Connection<Client> createConnection(SocketChannel socketChannel,
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

        return new SslConnection<Client>(this, socketChannel, controller,
                socketAddress, engine);
    }

    @Override
    public InboundWay createInboundWay(Connection<Client> connection,
            int bufferSize) {
        return new HttpsClientInboundWay(connection, bufferSize);
    }

    @Override
    public OutboundWay createOutboundWay(Connection<Client> connection,
            int bufferSize) {
        return new HttpsClientOutboundWay(connection, bufferSize);
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
