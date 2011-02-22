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

package org.restlet.ext.ssl.internal;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLSession;

import org.restlet.Connector;
import org.restlet.engine.connector.Connection;
import org.restlet.engine.connector.ConnectionController;
import org.restlet.engine.connector.ConnectionHelper;
import org.restlet.engine.io.Buffer;
import org.restlet.engine.io.ReadableBufferedChannel;
import org.restlet.engine.io.ReadableSelectionChannel;
import org.restlet.engine.io.WritableSelectionChannel;

/**
 * Connection secured with SSL/TLS protocols.
 * 
 * @author Jerome Louvel
 * 
 * @param <T>
 */
public class SslConnection<T extends Connector> extends Connection<T> {

    /** The SSL manager wrapping the SSL context and engine. */
    private final SslManager sslManager;

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
            InetSocketAddress socketAddress, SslManager sslManager)
            throws IOException {
        super(helper, socketChannel, controller, socketAddress, sslManager
                .getApplicationBufferSize(), sslManager
                .getApplicationBufferSize());
        this.sslManager = sslManager;
    }

    @Override
    public void reuse(SocketChannel socketChannel,
            ConnectionController controller, InetSocketAddress socketAddress)
            throws IOException {
        getSslManager().setPeerAddress(socketAddress);
        getSslManager().initEngine();
        super.reuse(socketChannel, controller, socketAddress);
    }

    @Override
    protected ReadableSelectionChannel createReadableSelectionChannel() {
        if (getSslManager() != null) {
            SSLSession session = getSslManager().getSession();
            int packetSize = session.getPacketBufferSize();
            ReadableBufferedChannel rbc = new ReadableBufferedChannel(
                    getInboundWay(), new Buffer(packetSize, getHelper().isDirectBuffers()),
                    super.createReadableSelectionChannel());
            return new ReadableSslChannel(rbc, getSslManager(), this);
        } else {
            return null;
        }
    }

    @Override
    protected WritableSelectionChannel createWritableSelectionChannel() {
        return new WritableSslChannel(super.createWritableSelectionChannel(),
                getSslManager(), this);
    }

    @Override
    public int getInboundBufferSize() {
        return Math.max(super.getInboundBufferSize(), getSslManager()
                .getSession().getApplicationBufferSize());
    }

    @Override
    public int getOutboundBufferSize() {
        return Math.max(super.getOutboundBufferSize(), getSslManager()
                .getSession().getApplicationBufferSize());
    }

    /**
     * Returns the SSL manager wrapping the SSL context and engine.
     * 
     * @return The SSL manager wrapping the SSL context and engine.
     */
    protected SslManager getSslManager() {
        return sslManager;
    }

}
