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

import javax.net.ssl.SSLEngineResult;

import org.restlet.engine.io.Buffer;
import org.restlet.engine.io.IoState;
import org.restlet.engine.io.SelectionChannel;
import org.restlet.engine.io.WritableBufferedChannel;
import org.restlet.engine.io.WritableSelectionChannel;

/**
 * SSL byte channel that wraps all application data using the SSL/TLS protocols.
 * It is important to implement {@link SelectionChannel} as some framework
 * classes rely on this down the processing chain.
 * 
 * @author Jerome Louvel
 */
public class WritableSslChannel extends WritableBufferedChannel implements
        TasksListener {

    /** The parent SSL connection. */
    private final SslConnection<?> connection;

    /**
     * Constructor.
     * 
     * @param target
     *            The wrapped channel.
     * @param connection
     *            The parent SSL connection.
     */
    public WritableSslChannel(WritableSelectionChannel target,
            SslConnection<?> connection) {
        super(new Buffer(connection.getPacketBufferSize(), connection
                .getHelper().isDirectBuffers()), target);
        this.connection = connection;
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
     * Callback method invoked upon delegated tasks completion.
     */
    public void onCompleted() {
        if (getConnection().getOutboundWay().getIoState() == IoState.IDLE) {
            getConnection().getOutboundWay().setIoState(IoState.INTEREST);
        }
    }

    @Override
    public int onFill(Buffer buffer, Object... args) throws IOException {
        int result = 0;

        if (getConnection().getSslState() == SslState.READING_APPLICATION_DATA) {
            getConnection().setSslState(SslState.WRITING_APPLICATION_DATA);
        }

        // Refill the packet buffer
        if ((getBuffer().isFilling())
                || (getConnection().getSslState() == SslState.HANDSHAKING)) {
            int srcSize = buffer.remaining();

            if (srcSize > 0) {
                while (getBuffer().hasRemaining()
                        && (getConnection().getOutboundWay().getIoState() != IoState.IDLE)
                        && buffer.hasRemaining()) {
                    getBuffer().beforeFill();
                    SSLEngineResult sslResult = getConnection().getSslEngine()
                            .wrap(buffer.getBytes(), getBuffer().getBytes());

                    // Let's drain the packet buffer
                    if (getBuffer().couldDrain()) {
                        getBuffer().beforeDrain();
                    }

                    getConnection().handleResult(sslResult, getBuffer(),
                            buffer.getBytes(), this);
                    // drain();
                }

                result = srcSize - buffer.remaining();
            }
        }

        return result;
    }
}
