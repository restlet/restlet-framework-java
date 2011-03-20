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
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;

import org.restlet.Connector;
import org.restlet.engine.connector.Connection;
import org.restlet.engine.connector.ConnectionController;
import org.restlet.engine.connector.ConnectionHelper;
import org.restlet.engine.io.IoState;
import org.restlet.engine.io.ReadableSelectionChannel;
import org.restlet.engine.io.WritableSelectionChannel;

/**
 * Connection secured with SSL/TLS protocols.
 * 
 * @author Jerome Louvel
 * 
 * @param <T>
 */
public class SslConnection<T extends Connector> extends Connection<T> {

    /** The peer address. */
    private volatile InetSocketAddress peerAddress;

    /** The engine to use for wrapping and unwrapping. */
    private volatile SSLEngine sslEngine;

    /** The engine status. */
    private volatile SSLEngineResult.Status sslEngineStatus;

    /** The handshake status. */
    private volatile SSLEngineResult.HandshakeStatus sslHandshakeStatus;

    /** The global SSL state. */
    private volatile SslState sslState;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent connector helper.
     * @param socketChannel
     *            The underlying NIO socket channel.
     * @param controller
     *            The IO controller.
     * @param socketAddress
     *            The associated IP address.
     * 
     * 
     * 
     * @throws IOException
     */
    public SslConnection(ConnectionHelper<T> helper,
            SocketChannel socketChannel, ConnectionController controller,
            InetSocketAddress socketAddress, SSLEngine sslEngine)
            throws IOException {
        super(helper, socketChannel, controller, socketAddress, sslEngine
                .getSession().getApplicationBufferSize(), sslEngine
                .getSession().getApplicationBufferSize());
        this.sslEngine = sslEngine;
        this.sslState = SslState.IDLE;
        this.sslEngineStatus = SSLEngineResult.Status.OK;
        this.sslHandshakeStatus = HandshakeStatus.NOT_HANDSHAKING;
        initSslEngine();
    }

    @Override
    protected ReadableSelectionChannel createReadableSelectionChannel() {
        return new ReadableSslChannel(super.createReadableSelectionChannel(),
                this);
    }

    @Override
    protected WritableSelectionChannel createWritableSelectionChannel() {
        return new WritableSslChannel(super.createWritableSelectionChannel(),
                this);
    }

    /**
     * Returns the suggested application buffer size.
     * 
     * @return The suggested application buffer size.
     */
    public int getApplicationBufferSize() {
        return getSslSession() == null ? 0 : getSslSession()
                .getApplicationBufferSize();
    }

    @Override
    public int getInboundBufferSize() {
        return Math.max(super.getInboundBufferSize(), getSslSession()
                .getApplicationBufferSize());
    }

    @Override
    public int getOutboundBufferSize() {
        return Math.max(super.getOutboundBufferSize(), getSslSession()
                .getApplicationBufferSize());
    }

    /**
     * Returns the suggested SSL packet buffer size.
     * 
     * @return The suggested SSL packet buffer size.
     */
    public int getPacketBufferSize() {
        return getSslSession() == null ? 0 : getSslSession()
                .getPacketBufferSize();
    }

    /**
     * Returns the peer address.
     * 
     * @return The peer address.
     */
    public InetSocketAddress getPeerAddress() {
        return peerAddress;
    }

    /**
     * Returns the engine to use for wrapping and unwrapping.
     * 
     * @return The engine to use for wrapping and unwrapping.
     */
    public SSLEngine getSslEngine() {
        return sslEngine;
    }

    /**
     * Returns the engine status.
     * 
     * @return The engine status.
     */
    protected SSLEngineResult.Status getSslEngineStatus() {
        return sslEngineStatus;
    }

    /**
     * Returns the handshake status.
     * 
     * @return The handshake status.
     */
    protected SSLEngineResult.HandshakeStatus getSslHandshakeStatus() {
        return sslHandshakeStatus;
    }

    /**
     * Returns the current SSL session or null.
     * 
     * @return The current SSL session or null.
     */
    public SSLSession getSslSession() {
        return getSslEngine() == null ? null : getSslEngine().getSession();
    }

    /**
     * Returns the global SSL state.
     * 
     * @return The global SSL state.
     */
    public SslState getSslState() {
        return sslState;
    }

    /**
     * Handles the SSL handshake states based on the last result received.
     * 
     * @throws IOException
     */
    private void handleSslHandshake() throws IOException {
        switch (getSslHandshakeStatus()) {
        case FINISHED:
            onFinished();
            break;

        case NEED_TASK:
            onNeedTask();
            break;

        case NEED_UNWRAP:
            onUnwrap();
            break;

        case NEED_WRAP:
            onWrap();
            break;

        case NOT_HANDSHAKING:
            // Don't do anything
            break;
        }
    }

    /**
     * Handles the result of a previous SSL engine processing.
     * 
     * @throws IOException
     */
    public void handleSslResult() throws IOException {
        switch (getSslEngineStatus()) {
        case BUFFER_OVERFLOW:
            break;

        case BUFFER_UNDERFLOW:
            if ((getSslHandshakeStatus() == HandshakeStatus.NEED_UNWRAP)
                    && ((getInboundWay().getIoState() == IoState.IDLE) || (getInboundWay()
                            .getIoState() == IoState.READY))) {
                getInboundWay().setIoState(IoState.INTEREST);
            }
            break;

        case CLOSED:
            setSslState(SslState.CLOSED);
            close(true);
            break;

        case OK:
            handleSslHandshake();
            break;
        }
    }

    /**
     * Initializes the SSL engine with the current SSL context and socket
     * address.
     * 
     * @throws SSLException
     */
    public void initSslEngine() throws SSLException {
        setSslState(SslState.CREATED);
        getSslEngine().setUseClientMode(isClientSide());
        getSslEngine().beginHandshake();
        setSslState(SslState.HANDSHAKING);
    }

    /**
     * Notifies that the SSL handshake is finished. Application data can now be
     * exchanged.
     */
    private void onFinished() {
        if (getSslState() == SslState.HANDSHAKING) {
            if (isClientSide()) {
                setSslState(SslState.WRITING_APPLICATION_DATA);
            } else {
                setSslState(SslState.READING_APPLICATION_DATA);
            }

            setSslHandshakeStatus(HandshakeStatus.NOT_HANDSHAKING);
        }

        if (isClientSide()) {
            getInboundWay().setIoState(IoState.IDLE);
            getOutboundWay().setIoState(IoState.INTEREST);
        } else {
            getInboundWay().setIoState(IoState.INTEREST);
            getOutboundWay().setIoState(IoState.IDLE);
        }
    }

    /**
     * Runs the pending lengthy task.
     */
    private void onNeedTask() {
        // Delegate lengthy tasks to the connector's worker
        // service before checking again
        final Runnable task = getSslEngine().getDelegatedTask();

        if (task != null) {
            // Suspend IO processing until the task completes
            getInboundWay().setIoState(IoState.IDLE);
            getOutboundWay().setIoState(IoState.IDLE);

            // Runs the pending lengthy task.
            getHelper().getWorkerService().execute(new Runnable() {
                public void run() {
                    getLogger().log(Level.FINE, "Running delegated tasks...");
                    task.run();

                    // Check if a next task is pending
                    Runnable nextTask = getSslEngine().getDelegatedTask();

                    // Run any pending task sequentially
                    while (nextTask != null) {
                        nextTask.run();
                        nextTask = getSslEngine().getDelegatedTask();
                    }

                    // onCompleted();
                    getLogger().log(Level.FINE, "Done running delegated tasks");
                }
            });
        }
    }

    /**
     * Unwraps packet data into handshake or application data. Need to read
     * next.
     * 
     * @throws IOException
     */
    private void onUnwrap() throws IOException {
        if (getInboundWay().getIoState() != IoState.PROCESSING) {
            getInboundWay().setIoState(IoState.READY);
            getOutboundWay().setIoState(IoState.IDLE);
        }
    }

    /**
     * Wraps the handshake or application data into packet data. Need to write
     * next.
     * 
     * @throws IOException
     */
    private void onWrap() throws IOException {
        if (getOutboundWay().getIoState() == IoState.IDLE) {
            getInboundWay().setIoState(IoState.IDLE);
            getOutboundWay().setIoState(IoState.READY);
        }
    }

    @Override
    public void reuse(SocketChannel socketChannel,
            ConnectionController controller, InetSocketAddress socketAddress)
            throws IOException {
        setPeerAddress(socketAddress);
        initSslEngine();
        super.reuse(socketChannel, controller, socketAddress);
    }

    /**
     * Sets the peer address.
     * 
     * @param peerAddress
     *            The peer address.
     */
    public void setPeerAddress(InetSocketAddress peerAddress) {
        this.peerAddress = peerAddress;
    }

    /**
     * Sets the engine to use for wrapping and unwrapping.
     * 
     * @param engine
     *            The engine to use for wrapping and unwrapping.
     */
    public void setSslEngine(SSLEngine engine) {
        this.sslEngine = engine;
    }

    /**
     * Sets the engine status.
     * 
     * @param engineStatus
     *            The engine status.
     */
    protected void setSslEngineStatus(SSLEngineResult.Status engineStatus) {
        this.sslEngineStatus = engineStatus;
    }

    /**
     * Sets the handshake status.
     * 
     * @param handshakeStatus
     *            The handshake status.
     */
    protected void setSslHandshakeStatus(
            SSLEngineResult.HandshakeStatus handshakeStatus) {
        this.sslHandshakeStatus = handshakeStatus;
    }

    /**
     * Saves the result of a previous SSL engine processing.
     * 
     * @param sslResult
     *            The SSL result to handle.
     * @throws IOException
     */
    public void setSslResult(SSLEngineResult sslResult) throws IOException {
        if (sslResult != null) {
            // Logs the result
            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE, "SSL engine result: " + sslResult);
                getLogger().log(Level.FINE, "SSL connection: " + toString());
            }

            // Store the engine status
            setSslEngineStatus(sslResult.getStatus());

            // Store the handshake status
            setSslHandshakeStatus(sslResult.getHandshakeStatus());
        }
    }

    /**
     * Sets the global state.
     * 
     * @param sslState
     *            The global state.
     */
    public void setSslState(SslState sslState) {
        this.sslState = sslState;
    }

    @Override
    public String toString() {
        return super.toString() + " | " + getState() + " | " + getSslEngine()
                + " | " + getSslEngineStatus() + " | "
                + getSslHandshakeStatus();
    }

}
