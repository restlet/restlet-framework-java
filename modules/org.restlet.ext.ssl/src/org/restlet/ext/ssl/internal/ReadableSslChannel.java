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

package org.restlet.ext.ssl.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import org.restlet.engine.io.BufferState;
import org.restlet.engine.io.Drainer;
import org.restlet.engine.io.IoState;
import org.restlet.engine.io.ReadableBufferedChannel;
import org.restlet.engine.io.ReadableSelectionChannel;
import org.restlet.engine.io.SelectionChannel;

/**
 * SSL byte channel that unwraps all read data using the SSL/TLS protocols. It
 * is important to implement {@link SelectionChannel} as some framework classes
 * rely on this down the processing chain.
 * 
 * @author Jerome Louvel
 */
public class ReadableSslChannel extends SslChannel<ReadableBufferedChannel>
        implements ReadableSelectionChannel, Drainer {

    /**
     * Constructor.
     * 
     * @param wrappedChannel
     *            The wrapped channel.
     * @param manager
     *            The SSL manager.
     * @param connection
     *            The parent SSL connection.
     */
    public ReadableSslChannel(ReadableBufferedChannel wrappedChannel,
            SslManager manager, SslConnection<?> connection) {
        super(wrappedChannel, manager, connection);
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
                && (getManager().getState() != SslState.CLOSED)
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
        return ((lastRead > 0) || ((getManager().getState() == SslState.HANDSHAKING)
                && (getManager().getEngineStatus() == Status.OK) && (getManager()
                .getHandshakeStatus() == HandshakeStatus.NEED_UNWRAP)))
                && targetBuffer.hasRemaining();
    }

    /**
     * Drains the byte buffer. By default, it directly copies as many byte as
     * possible to the target buffer, with no modification.
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

        switch (getManager().getState()) {
        case READING_APPLICATION_DATA:
        case HANDSHAKING:
            sslResult = runEngine(targetBuffer);
            handleResult(sslResult, targetBuffer);
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

    @Override
    protected ByteBuffer getPacketBuffer() {
        return getWrappedChannel().getByteBuffer();
    }

    @Override
    protected BufferState getPacketBufferState() {
        return getWrappedChannel().getByteBufferState();
    }

    @Override
    protected void onDelegatedTasksCompleted() {
        if (getConnection().getInboundWay().getIoState() == IoState.IDLE) {
            getConnection().getInboundWay().setIoState(IoState.INTEREST);
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
        if (getManager().getState() == SslState.WRITING_APPLICATION_DATA) {
            getManager().setState(SslState.READING_APPLICATION_DATA);
        }

        if ((getManager().getEngineStatus() == Status.BUFFER_UNDERFLOW)
                && (getPacketBufferState() == BufferState.DRAINING)) {
            setPacketBufferState(BufferState.FILLING);
            getPacketBuffer().flip();
        }

        return getWrappedChannel().read(targetBuffer, this);
    }

    @Override
    protected SSLEngineResult runEngine(ByteBuffer applicationBuffer)
            throws IOException {
        if (getLogger().isLoggable(Level.INFO)) {
            getLogger().log(Level.INFO,
                    "Unwrapping bytes with: " + getPacketBuffer());

            getLogger().log(
                    Level.INFO,
                    "Application buffer suggested size: "
                            + getManager().getEngine().getSession()
                                    .getApplicationBufferSize());
            getLogger().log(
                    Level.INFO,
                    "Packet buffer suggested size: "
                            + getManager().getEngine().getSession()
                                    .getPacketBufferSize());
            getLogger().log(
                    Level.INFO,
                    "Application buffer remaining size: "
                            + applicationBuffer.remaining() + "/"
                            + applicationBuffer.capacity());
            getLogger().log(
                    Level.INFO,
                    "Packet buffer remaining size: "
                            + getPacketBuffer().remaining() + "/"
                            + getPacketBuffer().capacity());
        }

        SSLEngineResult result = getManager().getEngine().unwrap(
                getPacketBuffer(), applicationBuffer);
        int remaining = getPacketBuffer().remaining();

        if (remaining == 0) {
            setPacketBufferState(BufferState.FILLING);
            getPacketBuffer().clear();
        }

        return result;
    }

    @Override
    protected void setPacketBufferState(BufferState bufferState) {
        getWrappedChannel().setByteBufferState(bufferState);
    }
}
