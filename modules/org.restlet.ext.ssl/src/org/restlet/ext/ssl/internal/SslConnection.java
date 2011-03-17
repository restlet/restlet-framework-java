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
import java.nio.ByteBuffer;
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
import org.restlet.engine.io.Buffer;
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

    /** The handshake status. */
    private volatile SSLEngineResult.HandshakeStatus handshakeStatus;

    /** The peer address. */
    private volatile InetSocketAddress peerAddress;

    /** The engine to use for wrapping and unwrapping. */
    private volatile SSLEngine sslEngine;

    /** The engine status. */
    private volatile SSLEngineResult.Status sslEngineStatus;

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
        this.handshakeStatus = HandshakeStatus.NOT_HANDSHAKING;
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

    /**
     * Returns the handshake status.
     * 
     * @return The handshake status.
     */
    protected SSLEngineResult.HandshakeStatus getHandshakeStatus() {
        return handshakeStatus;
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
     * Handles the handshake states.
     * 
     * @param sslResult
     *            The SSL result to handle.
     * @param applicationBuffer
     *            The related application data buffer.
     * @param listener
     *            Delegated tasks completion listener.
     * @throws IOException
     */
    private void handleHandshake(SSLEngineResult sslResult,
            ByteBuffer applicationBuffer, TasksListener listener)
            throws IOException {

        // Handle the status
        switch (sslResult.getHandshakeStatus()) {
        case FINISHED:
            onHandshakeFinished(sslResult, applicationBuffer);
            break;

        case NEED_TASK:
            runTask(sslResult, applicationBuffer, listener);
            break;

        case NEED_UNWRAP:
            onUnwrap(sslResult, applicationBuffer);
            break;

        case NEED_WRAP:
            onWrap(sslResult, applicationBuffer);
            break;

        case NOT_HANDSHAKING:
            // Don't do anything
            break;
        }
    }

    /**
     * Handles the result of a previous SSL engine processing.
     * 
     * @param sslResult
     *            The SSL result to handle.
     * @param packetBuffer
     *            The related network packet buffer.
     * @param applicationBuffer
     *            The related application data buffer.
     * @param listener
     *            Delegated tasks completion listener.
     * @throws IOException
     */
    public void handleResult(SSLEngineResult sslResult, Buffer packetBuffer,
            ByteBuffer applicationBuffer, TasksListener listener)
            throws IOException {
        if (sslResult != null) {
            log(sslResult);

            // Store the engine status
            setSslEngineStatus(sslResult.getStatus());

            // Store the handshake status
            setHandshakeStatus(sslResult.getHandshakeStatus());

            switch (sslResult.getStatus()) {
            case BUFFER_OVERFLOW:
                onBufferOverflow(sslResult, applicationBuffer);
                break;

            case BUFFER_UNDERFLOW:
                onBufferUnderflow(sslResult, packetBuffer, applicationBuffer);
                break;

            case CLOSED:
                onClosed(sslResult, applicationBuffer, listener);
                break;

            case OK:
                onOk(sslResult, applicationBuffer, listener);
                break;
            }
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
     * Logs the SSL engine result.
     * 
     * @param sslResult
     *            The SSL engine result to log.
     */
    private void log(SSLEngineResult sslResult) {
        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().log(Level.FINE, "SSL I/O result: " + sslResult);
            getLogger().log(Level.FINE, "SSL connection: " + toString());
        }
    }

    /**
     * Warns that an SSL buffer overflow exception occurred.
     * 
     * @param sslResult
     *            The SSL engine result.
     * @param applicationBuffer
     *            The related application data buffer.
     */
    private void onBufferOverflow(SSLEngineResult sslResult,
            ByteBuffer applicationBuffer) {
        // Exit so that the application buffer can be drained
    }

    /**
     * Warns that an SSL buffer underflow exception occurred.
     * 
     * @param sslResult
     *            The SSL engine result.
     * @param applicationBuffer
     *            The related application buffer.
     */
    private void onBufferUnderflow(SSLEngineResult sslResult,
            Buffer packetBuffer, ByteBuffer applicationBuffer) {
        if (packetBuffer.canDrain() && !packetBuffer.couldFill()) {
            // We need to compact the buffer to make room for more bytes
            packetBuffer.compact();
        }
    }

    /**
     * Notifies that the SSL engine has been properly closed and can no longer
     * be used.
     * 
     * @param sslResult
     *            The SSL engine result.
     * @param applicationBuffer
     *            The related application buffer.
     * @param listener
     *            Delegated tasks completion listener.
     * @throws IOException
     */
    private void onClosed(SSLEngineResult sslResult,
            ByteBuffer applicationBuffer, TasksListener listener)
            throws IOException {
        setSslState(SslState.CLOSED);
        close(true);
        handleHandshake(sslResult, applicationBuffer, listener);
    }

    /**
     * Notifies that the SSL handshake is finished. Application data can now be
     * exchanged.
     * 
     * @param sslResult
     *            The SSL engine result.
     * @param applicationBuffer
     *            The related application buffer.
     */
    private void onHandshakeFinished(SSLEngineResult sslResult,
            ByteBuffer applicationBuffer) {
        if (getSslState() == SslState.HANDSHAKING) {
            if (isClientSide()) {
                setSslState(SslState.WRITING_APPLICATION_DATA);
            } else {
                setSslState(SslState.READING_APPLICATION_DATA);
            }

            setHandshakeStatus(HandshakeStatus.NOT_HANDSHAKING);
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
     * Notifies that the SSL handling was successful.
     * 
     * @param sslResult
     *            The SSL engine result.
     * @param applicationBuffer
     *            The related application data buffer.
     * @param listener
     *            Delegated tasks completion listener.
     * @throws IOException
     */
    private void onOk(SSLEngineResult sslResult, ByteBuffer applicationBuffer,
            TasksListener listener) throws IOException {
        handleHandshake(sslResult, applicationBuffer, listener);
    }

    /**
     * Unwraps packet data into handshake or application data. Need to read
     * next.
     * 
     * @param sslResult
     *            The SSL engine result.
     * @param applicationBuffer
     *            The related application buffer.
     * @throws IOException
     */
    private void onUnwrap(SSLEngineResult sslResult,
            ByteBuffer applicationBuffer) throws IOException {
        if (getInboundWay().getIoState() != IoState.PROCESSING) {
            getInboundWay().setIoState(IoState.READY);
            getOutboundWay().setIoState(IoState.IDLE);
        }
    }

    /**
     * Wraps the handshake or application data into packet data. Need to write
     * next.
     * 
     * @param sslResult
     *            The SSL engine result.
     * @param applicationBuffer
     *            The related application buffer.
     * @throws IOException
     */
    private void onWrap(SSLEngineResult sslResult, ByteBuffer applicationBuffer)
            throws IOException {
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
     * Runs the pending lengthy task.
     * 
     * @param sslResult
     *            The SSL engine result.
     * @param applicationBuffer
     *            The related application buffer.
     * @param listener
     *            Delegated tasks completion listener.
     */
    private void runTask(SSLEngineResult sslResult,
            final ByteBuffer applicationBuffer, final TasksListener listener) {
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

                    listener.onCompleted();
                    getLogger().log(Level.FINE, "Done running delegated tasks");
                }
            });
        }
    }

    /**
     * Sets the handshake status.
     * 
     * @param handshakeStatus
     *            The handshake status.
     */
    protected void setHandshakeStatus(
            SSLEngineResult.HandshakeStatus handshakeStatus) {
        this.handshakeStatus = handshakeStatus;
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
                + " | " + getSslEngineStatus() + " | " + getHandshakeStatus();
    }

}
