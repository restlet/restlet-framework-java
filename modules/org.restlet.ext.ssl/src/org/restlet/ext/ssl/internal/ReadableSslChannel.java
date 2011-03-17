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
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import org.restlet.engine.io.Buffer;
import org.restlet.engine.io.IoState;
import org.restlet.engine.io.ReadableBufferedChannel;
import org.restlet.engine.io.ReadableSelectionChannel;
import org.restlet.engine.io.SelectionChannel;
import org.restlet.engine.io.WrapperSelectionChannel;

/**
 * SSL byte channel that unwraps all read data using the SSL/TLS protocols. It
 * is important to implement {@link SelectionChannel} as some framework classes
 * rely on this down the processing chain.
 * 
 * @author Jerome Louvel
 */
public class ReadableSslChannel extends
        WrapperSelectionChannel<ReadableBufferedChannel> implements
        ReadableSelectionChannel, TasksListener {

    /** The parent SSL connection. */
    private final SslConnection<?> connection;

    /**
     * Constructor.
     * 
     * @param wrappedChannel
     *            The wrapped channel.
     * @param connection
     *            The parent SSL connection.
     */
    public ReadableSslChannel(ReadableBufferedChannel wrappedChannel,
            SslConnection<?> connection) {
        super(wrappedChannel);
        this.connection = connection;
    }

    /**
     * Indicates if we can still read by refilling the packet buffer.
     * 
     * @param dst
     *            The destination buffer.
     * @return True if we can still read by refilling the packet buffer.
     */
    protected boolean canRead(ByteBuffer dst) {
        return dst.hasRemaining()
                && (getConnection().getSslState() != SslState.CLOSED)
                && getPacketBuffer().hasRemaining()
                // && (getPacketBufferState() == BufferState.FILLING)
                && (getConnection().getInboundWay().getIoState() != IoState.IDLE);
    }

    /**
     * Indicates if draining can be retried.
     * 
     * @return True if draining can be retried.
     */
    public boolean canRetry(int lastRead, ByteBuffer targetBuffer) {
        return ((lastRead > 0) || ((getConnection().getSslState() == SslState.HANDSHAKING)
                && (getConnection().getSslEngineStatus() == Status.OK) && (getConnection()
                .getHandshakeStatus() == HandshakeStatus.NEED_UNWRAP)))
                && targetBuffer.hasRemaining();
    }

    /**
     * Drains the byte buffer. By default, it decrypts the SSL data and copies
     * as many byte as possible to the target buffer, with no modification.
     * 
     * @param targetBuffer
     *            The target buffer.
     * @return The number of bytes added to the target buffer.
     * @throws IOException
     */
    public int drain(ByteBuffer targetBuffer) throws IOException {
        int result = 0;
        int dstSize = targetBuffer.remaining();
        SSLEngineResult sslResult = null;

        switch (getConnection().getSslState()) {
        case READING_APPLICATION_DATA:
        case HANDSHAKING:
            sslResult = unwrap(targetBuffer);
            getConnection().handleResult(sslResult, getPacketBuffer(),
                    targetBuffer, this);
            break;
        case CLOSED:
        case WRITING_APPLICATION_DATA:
            break;
        case CREATED:
        case IDLE:
        case REHANDSHAKING:
            throw new IOException("Unexpected SSL state");
        }

        result = dstSize - targetBuffer.remaining();
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
        return getWrappedChannel().getBuffer();
    }

    /**
     * Callback method invoked upon delegated tasks completion.
     */
    public void onCompleted() {
        if (getConnection().getInboundWay().getIoState() == IoState.IDLE) {
            getConnection().getInboundWay().setIoState(IoState.READY);
        }
    }

    /**
     * Reads the available bytes from the wrapped channel to the destination
     * buffer while unwrapping them with the SSL/TLS protocols.
     * 
     * @param targetBuffer
     *            The target buffer.
     * @return The number of bytes read.
     */
    public int read(ByteBuffer targetBuffer) throws IOException {
        int result = 0;
        boolean continueReading = true;

        if (getConnection().getSslState() == SslState.WRITING_APPLICATION_DATA) {
            getConnection().setSslState(SslState.READING_APPLICATION_DATA);
        }

        while (continueReading) {
            result += drain(targetBuffer);
            getPacketBuffer().beforeFill();
            continueReading = (getWrappedChannel().refill() > 0)
                    || (getPacketBuffer().couldDrain() && (getConnection()
                            .getSslEngineStatus() == Status.OK));
        }

        return result;
    }

    /**
     * Run the SSL engine to unwrap the packet bytes into application bytes.
     * 
     * @param applicationBuffer
     *            The target application buffer.
     * @return The SSL engine result.
     * @throws IOException
     */
    protected SSLEngineResult unwrap(ByteBuffer applicationBuffer)
            throws IOException {
        getPacketBuffer().beforeDrain();

        if (getConnection().getLogger().isLoggable(Level.FINE)) {
            getConnection()
                    .getLogger()
                    .log(Level.FINE,
                            "---------------------------------------------------------------------------------");
            getConnection().getLogger().log(Level.FINE,
                    "Unwrapping packet buffer: " + getPacketBuffer());
            getConnection().getLogger().log(Level.FINE,
                    "into application buffer: " + applicationBuffer);
        }

        if (getConnection().getLogger().isLoggable(Level.FINER)) {
            getConnection().getLogger().log(
                    Level.FINER,
                    "Application buffer suggested size: "
                            + getConnection().getSslEngine().getSession()
                                    .getApplicationBufferSize());
            getConnection().getLogger().log(
                    Level.FINER,
                    "Packet buffer suggested size: "
                            + getConnection().getSslEngine().getSession()
                                    .getPacketBufferSize());
        }

        SSLEngineResult result = getConnection().getSslEngine().unwrap(
                getPacketBuffer().getBytes(), applicationBuffer);

        // Let's fill the packet buffer
        if (getPacketBuffer().couldFill()) {
            getPacketBuffer().beforeFill();
        }

        return result;
    }
}
