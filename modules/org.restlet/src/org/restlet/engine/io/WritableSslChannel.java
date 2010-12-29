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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;

import javax.net.ssl.SSLEngineResult;

import org.restlet.Context;
import org.restlet.engine.connector.SslConnection;
import org.restlet.engine.security.SslManager;

/**
 * SSL byte channel that wraps all application data using the SSL/TLS protocols.
 * It is important to implement {@link SelectionChannel} as some framework
 * classes rely on this down the processing chain.
 * 
 * @author Jerome Louvel
 */
public class WritableSslChannel extends SslChannel<WritableSelectionChannel>
        implements WritableSelectionChannel {

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
    public WritableSslChannel(WritableSelectionChannel wrappedChannel,
            SslManager manager, SslConnection<?> connection) {
        super(wrappedChannel, manager, connection);
    }

    /**
     * Flushes the packet buffer if it is in draining state.
     * 
     * @return The number of bytes flushed.
     * @throws IOException
     */
    protected int flush() throws IOException {
        int result = 0;

        if (getPacketBufferState() == BufferState.DRAINING) {
            result = getWrappedChannel().write(getPacketBuffer());

            if (getPacketBuffer().remaining() == 0) {
                setPacketBufferState(BufferState.FILLING);
                getPacketBuffer().clear();
            }
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

        // If the packet buffer isn't empty, first try to flush it
        flush();

        // Refill the packet buffer
        if (getPacketBufferState() == BufferState.FILLING) {
            int srcSize = src.remaining();

            if (srcSize > 0) {
                SSLEngineResult sslResult = getManager().getEngine().wrap(src,
                        getPacketBuffer());

                if (Context.getCurrentLogger().isLoggable(Level.INFO)) {
                    Context.getCurrentLogger().log(Level.INFO,
                            "SSL I/O result" + sslResult);
                }

                result = srcSize - src.remaining();
                getPacketBuffer().flip();
                int remaining = getPacketBuffer().remaining();

                if (remaining > 0) {
                    setPacketBufferState(BufferState.DRAINING);
                    flush();
                } else {
                    getPacketBuffer().clear();
                }

                switch (sslResult.getStatus()) {
                case BUFFER_OVERFLOW:
                    // TODO: handle
                    break;

                case BUFFER_UNDERFLOW:
                    // TODO: handle
                    break;

                case CLOSED:
                    // TODO: handle
                    break;

                case OK:
                    switch (sslResult.getHandshakeStatus()) {
                    case FINISHED:
                        // TODO: handle
                        break;

                    case NEED_TASK:
                        // TODO: handle
                        break;

                    case NEED_UNWRAP:
                        getConnection().getOutboundWay().setIoState(
                                IoState.IDLE);
                        getConnection().getInboundWay().setIoState(
                                IoState.INTEREST);
                        break;

                    case NEED_WRAP:
                        // TODO: handle
                        break;

                    case NOT_HANDSHAKING:
                        // TODO: handle
                        break;
                    }
                    break;
                }
            }
        }

        return result;
    }
}
