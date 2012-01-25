/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.ssl.internal;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

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

    @Override
    public boolean canLoop(Buffer buffer, Object... args) {
        return getConnection().getOutboundWay().canLoop(buffer, args)
                || getConnection().isSslHandshaking();
    }

    @Override
    public boolean couldFill(Buffer buffer, Object... args) {
        return super.couldFill(buffer, args)
                && (getConnection().getSslEngineStatus() != Status.CLOSED)
                && ((getConnection().getSslHandshakeStatus() == HandshakeStatus.NOT_HANDSHAKING) || (getConnection()
                        .getSslHandshakeStatus() == HandshakeStatus.NEED_WRAP));
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
        int srcSize = buffer.remaining();
        ByteBuffer applicationBuffer = (ByteBuffer) args[0];
        SSLEngineResult sslResult = getConnection().getSslEngine().wrap(
                applicationBuffer, buffer.getBytes());
        getConnection().setSslResult(sslResult);
        return srcSize - buffer.remaining();
    }

    @Override
    public void postProcess(int drained) throws IOException {
        getConnection().handleSslResult();
    }

    @Override
    public int write(ByteBuffer sourceBuffer) throws IOException {
        return super.write(sourceBuffer);
    }
}
