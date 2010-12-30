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

            if (result > 0) {
                setPacketBufferState(BufferState.DRAINING);
                getPacketBuffer().flip();
            }
        }

        return result;
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
        boolean continueReading = true;
        boolean continueHandshake = true;

        while (continueReading) {
            if (getPacketBufferState() == BufferState.FILLING) {
                continueReading = (refill() > 0);
            }

            if (getPacketBufferState() == BufferState.DRAINING) {
                // Unwrap the network data into application data
                int remaining = dst.remaining();
                SSLEngineResult sslResult = getManager().getEngine().unwrap(
                        getPacketBuffer(), dst);
                result = remaining - dst.remaining();

                if (Context.getCurrentLogger().isLoggable(Level.INFO)) {
                    Context.getCurrentLogger().log(Level.INFO,
                            "SSL I/O result" + sslResult);
                }

                if (getPacketBuffer().remaining() == 0) {
                    setPacketBufferState(BufferState.FILLING);
                }

                switch (sslResult.getStatus()) {
                case BUFFER_OVERFLOW:
                    // TODO: handle
                    continueReading = false;
                    break;

                case BUFFER_UNDERFLOW:
                    // TODO: handle
                    continueReading = false;
                    break;

                case CLOSED:
                    getConnection().close(true);
                    continueReading = false;
                    break;

                case OK:
                    while (continueHandshake) {
                        switch (sslResult.getHandshakeStatus()) {
                        case FINISHED:
                            continueHandshake = false;
                            break;

                        case NEED_TASK:
                            // Delegate lengthy tasks to the connector's worker
                            // service
                            Runnable task = null;

                            while ((task = getManager().getEngine()
                                    .getDelegatedTask()) != null) {
                                getConnection().getHelper().getWorkerService()
                                        .execute(task);
                            }
                            continueHandshake = false;
                            break;

                        case NEED_UNWRAP:
                            continueHandshake = false;
                            break;

                        case NEED_WRAP:
                            // Need to write now
                            getConnection().getOutboundWay().setIoState(
                                    IoState.INTEREST);
                            getConnection().getInboundWay().setIoState(
                                    IoState.IDLE);
                            continueHandshake = false;
                            continueReading = false;
                            break;

                        case NOT_HANDSHAKING:
                            continueHandshake = false;
                            break;
                        }
                        break;
                    }
                }
            }
        }

        return result;
    }
}
