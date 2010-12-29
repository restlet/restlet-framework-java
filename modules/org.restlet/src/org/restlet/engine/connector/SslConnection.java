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

package org.restlet.engine.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.restlet.Connector;
import org.restlet.engine.io.ReadableSelectionChannel;
import org.restlet.engine.io.ReadableSslChannel;
import org.restlet.engine.io.WritableSelectionChannel;
import org.restlet.engine.io.WritableSslChannel;

/**
 * Connection secured with SSL/TLS protocols.
 * 
 * @author Jerome Louvel
 * 
 * @param <T>
 */
public class SslConnection<T extends Connector> extends Connection<T> {

    /** The SSL context to use for SSL engine creation. */
    private final SSLContext sslContext;

    /** The SSL engine to use of wrapping and unwrapping. */
    private SSLEngine sslEngine;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent connector helper.
     * @param socketChannel
     *            The underlying NIO socket channel.
     * @param controller
     *            The IO controller.
     * @param socketAddress
     *            The associated IP address.
     * @throws IOException
     */
    public SslConnection(ConnectionHelper<T> helper,
            SocketChannel socketChannel, ConnectionController controller,
            InetSocketAddress socketAddress, SSLContext sslContext)
            throws IOException {
        super(helper, socketChannel, controller, socketAddress);
        this.sslContext = sslContext;
    }

    @Override
    protected ReadableSelectionChannel createReadableSelectionChannel() {
        ByteBuffer packetBuffer = createByteBuffer(getHelper()
                .getInboundBufferSize());
        return new ReadableSslChannel(super.createReadableSelectionChannel(),
                getSslEngine(), packetBuffer);
    }

    @Override
    protected WritableSelectionChannel createWritableSelectionChannel() {
        ByteBuffer packetBuffer = createByteBuffer(getHelper()
                .getOutboundBufferSize());
        return new WritableSslChannel(super.createWritableSelectionChannel(),
                getSslEngine(), packetBuffer);
    }

    /**
     * Returns the SSL context to use for SSL engine creation.
     * 
     * @return The SSL context to use for SSL engine creation.
     */
    protected SSLContext getSslContext() {
        return sslContext;
    }

    /**
     * Returns the SSL engine to use for wrapping and unwrapping.
     * 
     * @return The SSL engine to use for wrapping and unwrapping.
     */
    protected SSLEngine getSslEngine() {
        return this.sslEngine;
    }

    @Override
    public void reuse(SocketChannel socketChannel,
            ConnectionController controller, InetSocketAddress socketAddress)
            throws IOException {
        super.reuse(socketChannel, controller, socketAddress);

        if (getSslContext() != null) {
            this.sslEngine = getSslContext().createSSLEngine(
                    socketAddress.getHostName(), socketAddress.getPort());
        }
    }

    /**
     * Sets the SSL engine to use for wrapping and unwrapping.
     * 
     * @param engine
     *            The SSL engine to use for wrapping and unwrapping.
     */
    protected void setSslEngine(SSLEngine engine) {
        this.sslEngine = engine;
    }

}
