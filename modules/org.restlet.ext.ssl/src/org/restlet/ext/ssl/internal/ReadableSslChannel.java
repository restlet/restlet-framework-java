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

import org.restlet.engine.io.BufferState;
import org.restlet.engine.io.IoState;
import org.restlet.engine.io.ReadableSelectionChannel;
import org.restlet.engine.io.SelectionChannel;

/**
 * SSL byte channel that unwraps all read data using the SSL/TLS protocols. It
 * is important to implement {@link SelectionChannel} as some framework classes
 * rely on this down the processing chain.
 * 
 * @author Jerome Louvel
 */
public class ReadableSslChannel extends SslChannel<ReadableSelectionChannel>
        implements ReadableSelectionChannel {

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
    public ReadableSslChannel(ReadableSelectionChannel wrappedChannel,
            SslManager manager, SslConnection<?> connection) {
        super(wrappedChannel, manager, connection);

    }

    /**
     * Reads the available bytes from the wrapped channel to the destination
     * buffer while unwrapping them with the SSL/TLS protocols.
     * 
     * @param dst
     *            The destination buffer.
     * @return The number of bytes read.
     */
    public int read(ByteBuffer dst) throws IOException {
        int result = 0;

        // If the packet buffer is empty, first try to refill it
        refill();

        if ((getPacketBufferState() == BufferState.DRAINING)
                || (getManager().getState() == SslState.HANDSHAKING)) {
            int dstSize = dst.remaining();

            if (dstSize > 0) {
                int lastRead = 0;

                while (dst.hasRemaining()
                        && (getManager().getState() != SslState.CLOSED)
                        && getPacketBuffer().hasRemaining()
                        && (getConnection().getInboundWay().getIoState() != IoState.IDLE)
                        && (lastRead != -1)) {
                    SSLEngineResult sslResult = runEngine(dst);
                    handleResult(sslResult, dst);
                    refill();
                }

                result = dstSize - dst.remaining();
            }
        }

        return result;
    }

    /**
     * Refills the byte buffer.
     * 
     * @return The number of bytes read and added to the buffer or -1 if end of
     *         channel reached.
     * @throws IOException
     */
    protected int refill() throws IOException {
        int result = 0;

        if (getPacketBufferState() == BufferState.FILLING) {
            result = getWrappedChannel().read(getPacketBuffer());

            if (getConnection().getLogger().isLoggable(Level.INFO)) {
                getConnection().getLogger().log(Level.INFO,
                        "Packet bytes read: " + result);
            }

            if (result > 0) {
                setPacketBufferState(BufferState.DRAINING);
                getPacketBuffer().flip();
            }
        }

        return result;
    }

    @Override
    protected SSLEngineResult runEngine(ByteBuffer applicationBuffer)
            throws IOException {
        if (getConnection().getLogger().isLoggable(Level.INFO)) {
            getConnection().getLogger().log(Level.INFO,
                    "Unwrapping bytes with: " + getPacketBuffer());
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
}
