/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.nio.internal.channel;

import java.io.IOException;
import java.nio.ByteBuffer;

import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLEngineResult.Status;

import org.restlet.engine.io.SelectionChannel;
import org.restlet.engine.io.WakeupListener;
import org.restlet.ext.nio.internal.buffer.Buffer;
import org.restlet.ext.nio.internal.connection.SslConnection;
import org.restlet.ext.nio.internal.state.IoState;
import org.restlet.ext.nio.internal.util.TasksListener;

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
     * @param wakeupListener
     *            The wakeup listener that will be notified.
     */
    public WritableSslChannel(WritableSelectionChannel target,
            SslConnection<?> connection, WakeupListener wakeupListener) {
        super(new Buffer(connection.getPacketBufferSize(), connection
                .getHelper().isDirectBuffers()), target, wakeupListener);
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

        HandshakeStatus handshakeStatus = getConnection()
                .getSslHandshakeStatus();
        SSLEngineResult sslResult;

        // Empty buffers should generally only be passed to SSLEngine during the
        // handshaking process,
        // when SSLEngine will be generating handshake packets itself. The
        // behavior of SSLEngine when
        // an empty buffer is passed during normal operation varies across
        // platforms. To avoid problems
        // due to this inconsistency, we avoid calling SSLEngine with empty
        // buffers when not handshaking,
        // and return what J2SE's SSLEngine would have in that case. See the
        // following issue for more details:
        // https://github.com/restlet/restlet-framework-java/issues/852
        if (applicationBuffer.hasRemaining()
                || handshakeStatus != HandshakeStatus.NOT_HANDSHAKING) {
            sslResult = getConnection().getSslEngine().wrap(applicationBuffer,
                    buffer.getBytes());
        } else {
            sslResult = new SSLEngineResult(Status.BUFFER_OVERFLOW,
                    handshakeStatus, 0, 0);
        }

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
