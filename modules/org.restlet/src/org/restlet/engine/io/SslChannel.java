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

package org.restlet.engine.io;

import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSession;

import org.restlet.engine.connector.SslConnection;

/**
 * Filter byte channel that enables secure communication using SSL/TLS
 * protocols. It is important to inherit from {@link SelectableChannel} as some
 * framework classes rely on this down the processing chain.
 * 
 * @author Jerome Louvel
 */
public class SslChannel<T extends SelectionChannel> extends
        WrapperSelectionChannel<T> {

    /** The parent SSL connection. */
    private final SslConnection<?> connection;

    /** The SSL engine to use of wrapping and unwrapping. */
    private volatile SSLEngine engine;

    /** The packet byte buffer. */
    private volatile ByteBuffer packetBuffer;

    /** The packet buffer state. */
    private volatile BufferState packetBufferState;

    /**
     * Constructor.
     * 
     * @param wrappedChannel
     *            The wrapped channel.
     * @param engine
     *            The SSL engine.
     * @param connection
     *            The parent SSL connection.
     */
    public SslChannel(T wrappedChannel, SSLEngine engine,
            SslConnection<?> connection) {
        super(wrappedChannel);
        this.engine = engine;
        this.connection = connection;

        if (engine != null) {
            SSLSession session = getEngine().getSession();
            int packetSize = session.getPacketBufferSize();
            this.packetBuffer = getConnection().createByteBuffer(packetSize);
        } else {
            this.packetBuffer = null;
        }

        this.packetBufferState = BufferState.IDLE;
    }

    /**
     * Returns the parent SSL connection.
     * 
     * @return The parent SSL connection.
     */
    protected SslConnection<?> getConnection() {
        return connection;
    }

    /**
     * Returns the SSL engine to use of wrapping and unwrapping.
     * 
     * @return The SSL engine to use of wrapping and unwrapping.
     */
    public SSLEngine getEngine() {
        return this.engine;
    }

    /**
     * Returns the SSL/TLS packet byte buffer.
     * 
     * @return The SSL/TLS packet byte buffer.
     */
    protected ByteBuffer getPacketBuffer() {
        return packetBuffer;
    }

    /**
     * Returns the byte buffer state.
     * 
     * @return The byte buffer state.
     */
    protected BufferState getPacketBufferState() {
        return packetBufferState;
    }

    /**
     * Sets the packet byte buffer.
     * 
     * @param packetBuffer
     *            The packet byte buffer.
     */
    protected void setPacketBuffer(ByteBuffer packetBuffer) {
        this.packetBuffer = packetBuffer;
    }

    /**
     * Sets the buffer state.
     * 
     * @param bufferState
     *            The buffer state.
     */
    protected void setPacketBufferState(BufferState bufferState) {
        this.packetBufferState = bufferState;
    }

}
