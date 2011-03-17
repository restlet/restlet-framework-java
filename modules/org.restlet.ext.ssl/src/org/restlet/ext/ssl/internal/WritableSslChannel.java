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
import java.nio.ByteBuffer;
import java.util.logging.Level;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLSession;

import org.restlet.engine.io.Buffer;
import org.restlet.engine.io.IoState;
import org.restlet.engine.io.SelectionChannel;
import org.restlet.engine.io.WrapperSelectionChannel;
import org.restlet.engine.io.WritableSelectionChannel;

/**
 * SSL byte channel that wraps all application data using the SSL/TLS protocols.
 * It is important to implement {@link SelectionChannel} as some framework
 * classes rely on this down the processing chain.
 * 
 * @author Jerome Louvel
 */
public class WritableSslChannel extends
        WrapperSelectionChannel<WritableSelectionChannel> implements
        WritableSelectionChannel, TasksListener {

    /** The parent SSL connection. */
    private final SslConnection<?> connection;

    /** The packet IO buffer. */
    private volatile Buffer packetBuffer;

    /**
     * Constructor.
     * 
     * @param wrappedChannel
     *            The wrapped channel.
     * @param connection
     *            The parent SSL connection.
     */
    public WritableSslChannel(WritableSelectionChannel wrappedChannel,
            SslConnection<?> connection) {
        super(wrappedChannel);
        this.connection = connection;

        if (connection != null) {
            SSLSession session = connection.getSslSession();
            int packetSize = session.getPacketBufferSize();
            this.packetBuffer = new Buffer(packetSize, getConnection()
                    .getHelper().isDirectBuffers());
        } else {
            this.packetBuffer = null;
        }
    }

    /**
     * Drains the packet buffer if it is in draining state.
     * 
     * @return The number of bytes flushed.
     * @throws IOException
     */
    protected int drain() throws IOException {
        int result = 0;

        if (getPacketBuffer().isDraining()) {
            if (getWrappedChannel().isOpen()) {
                result = getPacketBuffer().drain(getWrappedChannel());

                if (getConnection().getLogger().isLoggable(Level.FINE)) {
                    getConnection().getLogger().log(Level.FINE,
                            "Packet bytes written: " + result);
                }

                if (!getPacketBuffer().hasRemaining()) {
                    getPacketBuffer().clear();
                }
            }
        }

        return result;
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
     * Returns the SSL/TLS packet IO buffer.
     * 
     * @return The SSL/TLS packet IO buffer.
     */
    protected Buffer getPacketBuffer() {
        return packetBuffer;
    }

    /**
     * Callback method invoked upon delegated tasks completion.
     */
    public void onCompleted() {
        if (getConnection().getOutboundWay().getIoState() == IoState.IDLE) {
            getConnection().getOutboundWay().setIoState(IoState.INTEREST);
        }
    }

    /**
     * Run the SSL engine to wrap the application bytes into packet bytes.
     * 
     * @param applicationBuffer
     *            The target application buffer.
     * @return The SSL engine result.
     * @throws IOException
     */
    protected SSLEngineResult wrap(ByteBuffer applicationBuffer)
            throws IOException {
        SSLEngineResult result = null;
        getPacketBuffer().beforeFill();

        if (getConnection().getLogger().isLoggable(Level.FINE)) {
            getConnection()
                    .getLogger()
                    .log(Level.FINE,
                            "---------------------------------------------------------------------------------");
            getConnection().getLogger().log(Level.FINE,
                    "Wrapping application buffer: " + applicationBuffer);
            getConnection().getLogger().log(Level.FINE,
                    "into packet buffer: " + getPacketBuffer());
        }

        result = getConnection().getSslEngine().wrap(applicationBuffer,
                getPacketBuffer().getBytes());

        // Let's drain the packet buffer
        if (getPacketBuffer().couldDrain()) {
            getPacketBuffer().beforeDrain();
        }

        return result;
    }

    /**
     * Writes the available bytes to the wrapped channel by wrapping them with
     * the SSL/TLS protocols.
     * 
     * @param src
     *            The source buffer.
     * @return The number of bytes written.
     */
    public int write(ByteBuffer src) throws IOException {
        int result = 0;

        if (getConnection().getSslState() == SslState.READING_APPLICATION_DATA) {
            getConnection().setSslState(SslState.WRITING_APPLICATION_DATA);
        }

        // If the packet buffer isn't empty, first try to flush it
        drain();

        // Refill the packet buffer
        if ((getPacketBuffer().isFilling())
                || (getConnection().getSslState() == SslState.HANDSHAKING)) {
            int srcSize = src.remaining();

            if (srcSize > 0) {
                while (getPacketBuffer().hasRemaining()
                        && (getConnection().getOutboundWay().getIoState() != IoState.IDLE)
                        && src.hasRemaining()) {
                    SSLEngineResult sslResult = wrap(src);
                    getConnection().handleResult(sslResult, getPacketBuffer(),
                            src, this);
                    drain();
                }

                result = srcSize - src.remaining();
            }
        }

        return result;
    }
}
