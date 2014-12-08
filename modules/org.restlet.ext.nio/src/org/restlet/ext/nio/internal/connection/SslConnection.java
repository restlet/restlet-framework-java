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

package org.restlet.ext.nio.internal.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLEngineResult;
import javax.net.ssl.SSLEngineResult.HandshakeStatus;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.restlet.Connector;
import org.restlet.engine.io.ReadableSelectionChannel;
import org.restlet.engine.ssl.SslUtils;
import org.restlet.ext.nio.ConnectionHelper;
import org.restlet.ext.nio.internal.channel.ReadableSslChannel;
import org.restlet.ext.nio.internal.channel.WritableSelectionChannel;
import org.restlet.ext.nio.internal.channel.WritableSslChannel;
import org.restlet.ext.nio.internal.controller.ConnectionController;
import org.restlet.ext.nio.internal.state.IoState;

/**
 * Connection secured with SSL/TLS protocols.
 * 
 * @author Jerome Louvel
 * 
 * @param <T>
 */
public class SslConnection<T extends Connector> extends Connection<T> {

    /** Whether a handshake is in progress. */
    private volatile boolean isHandshaking;

    /** The peer address. */
    private volatile InetSocketAddress peerAddress;

    /** The engine to use for wrapping and unwrapping. */
    private volatile SSLEngine sslEngine;

    /** The engine result. */
    private volatile SSLEngineResult sslEngineResult;

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
        this.sslEngineResult = null;
        getSslEngine().setUseClientMode(isClientSide());
        initSslEngine();
    }

    @Override
    protected ReadableSelectionChannel createReadableSelectionChannel() {
        return new ReadableSslChannel(super.createReadableSelectionChannel(),
                this, getRegistration().getWakeupListener());
    }

    @Override
    protected WritableSelectionChannel createWritableSelectionChannel() {
        return new WritableSslChannel(super.createWritableSelectionChannel(),
                this, getRegistration().getWakeupListener());
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
     * Returns the SSL cipher suite.
     * 
     * @return The SSL cipher suite.
     */
    public String getSslCipherSuite() {
        SSLSession sslSession = getSslSession();

        if (sslSession != null) {
            return sslSession.getCipherSuite();
        }

        return null;
    }

    /**
     * Returns the list of client SSL certificates.
     * 
     * @return The list of client SSL certificates.
     */
    public List<Certificate> getSslClientCertificates() {
        SSLSession sslSession = getSslSession();

        if (sslSession != null) {
            try {
                List<Certificate> clientCertificates = Arrays.asList(sslSession
                        .getPeerCertificates());
                return clientCertificates;
            } catch (SSLPeerUnverifiedException e) {
                getLogger().log(Level.FINE,
                        "Can't get the client certificates.", e);
            }
        }

        return null;
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
     * Returns the engine result.
     * 
     * @return The engine result.
     */
    public SSLEngineResult getSslEngineResult() {
        return sslEngineResult;
    }

    /**
     * Returns the latest SSL engine status, or
     * {@link SSLEngineResult.Status#OK} otherwise.
     * 
     * @return The latest SSL engine status.
     */
    public SSLEngineResult.Status getSslEngineStatus() {
        return (getSslEngineResult() == null) ? SSLEngineResult.Status.OK
                : getSslEngineResult().getStatus();
    }

    /**
     * Returns the SSL handshake status, either from the latest engine result or
     * from the SSL engine.
     * 
     * @return The SSL handshake status.
     */
    public HandshakeStatus getSslHandshakeStatus() {
        return (getSslEngineResult() == null) ? getSslEngine()
                .getHandshakeStatus() : getSslEngineResult()
                .getHandshakeStatus();
    }

    /**
     * Returns the SSL key size, if available and accessible.
     * 
     * @return The SSL key size, if available and accessible.
     */
    public Integer getSslKeySize() {
        Integer keySize = null;
        String sslCipherSuite = getSslCipherSuite();

        if (sslCipherSuite != null) {
            keySize = SslUtils.extractKeySize(sslCipherSuite);
        }

        return keySize;
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
     * Handles the SSL handshake states based on the last result received.
     * 
     * @throws IOException
     */
    private void handleSslHandshake() throws IOException {
        HandshakeStatus hs = getSslHandshakeStatus();

        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().log(Level.FINER, "Handling SSL handshake: " + hs);
        }

        if (isHandshaking && hs == HandshakeStatus.NOT_HANDSHAKING) {
            // In some cases, on some platforms, the engine can go directly from
            // a handshaking state to NOT_HANDSHAKING.
            // We handle this situation as if it had returned FINISHED. See the
            // following issues:
            // https://github.com/restlet/restlet-framework-java/issues/852
            // and
            // https://github.com/restlet/restlet-framework-java/issues/862
            hs = HandshakeStatus.FINISHED;

            if (getLogger().isLoggable(Level.FINER)) {
                getLogger().log(
                        Level.FINER,
                        "SSLEngine went directly from handshaking to NOT_HANDSHAKING, "
                                + "treating as FINISHED.");
            }

        }

        if (hs != HandshakeStatus.NOT_HANDSHAKING) {
            isHandshaking = true;

            switch (hs) {
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
    }

    /**
     * Handles the result of a previous SSL engine processing.
     * 
     * @throws IOException
     */
    public synchronized void handleSslResult() throws IOException {
        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().log(Level.FINER,
                    "Handling SSL result: " + getSslEngineStatus());
        }

        switch (getSslEngineStatus()) {
        case BUFFER_OVERFLOW:
            if (getLogger().isLoggable(Level.FINER)) {
                getLogger()
                        .log(Level.FINER,
                                "SSL buffer overflow state detected. Application buffer needs to be consumed or compacted before retrying.");
            }
            break;

        case BUFFER_UNDERFLOW:
            if (getLogger().isLoggable(Level.FINER)) {
                getLogger()
                        .log(Level.FINER,
                                "SSL buffer underflow state detected. Network buffer needs to be consumed or compacted before retrying.");
            }

            if ((getSslHandshakeStatus() == HandshakeStatus.NEED_UNWRAP)
                    && ((getInboundWay().getIoState() == IoState.IDLE) || (getInboundWay()
                            .getIoState() == IoState.READY))) {
                getInboundWay().setIoState(IoState.INTEREST);
            }
            break;

        case CLOSED:
            close(true);
            break;

        case OK:
            handleSslHandshake();
            break;
        }

        // Reset the result
        setSslEngineResult(null);
    }

    /**
     * Initializes the SSL engine with the current SSL context and socket
     * address.
     * 
     * @throws SSLException
     */
    public void initSslEngine() throws SSLException {
        getSslEngine().beginHandshake();
    }

    /**
     * Indicates if the SSL handshake is going on.
     * 
     * @return True if the SSL handshake is going on.
     */
    public boolean isSslHandshaking() {
        return getSslHandshakeStatus() != HandshakeStatus.NOT_HANDSHAKING;
    }

    /**
     * Notifies that the SSL handshake is finished. Application data can now be
     * exchanged.
     */
    private void onFinished() {
        isHandshaking = false;

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
                    getLogger().log(Level.FINER, "Running delegated tasks...");
                    task.run();

                    // Check if a next task is pending
                    Runnable nextTask = getSslEngine().getDelegatedTask();

                    // Run any pending task sequentially
                    while (nextTask != null) {
                        nextTask.run();
                        nextTask = getSslEngine().getDelegatedTask();
                    }

                    if (getLogger().isLoggable(Level.FINER)) {
                        getLogger().log(Level.FINER,
                                "Done running delegated tasks");
                    }

                    try {
                        handleSslResult();
                    } catch (IOException e) {
                        getLogger().log(Level.INFO,
                                "Unable to handle SSL handshake", e);
                    }

                    getHelper().getController().wakeup();
                }
            });
        }
    }

    /**
     * Callback invoked when the SSL handshake requires unwrapping.
     * 
     * @throws IOException
     */
    private void onUnwrap() throws IOException {
        getOutboundWay().setIoState(IoState.IDLE);

        if (getInboundWay().getIoState() != IoState.PROCESSING) {
            getInboundWay().setIoState(IoState.READY);
        }
    }

    /**
     * Callback invoked when the SSL handshake requires wrapping.
     * 
     * @throws IOException
     */
    private void onWrap() throws IOException {
        getInboundWay().setIoState(IoState.IDLE);

        if (getOutboundWay().getIoState() == IoState.IDLE) {
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
     * Sets the engine result.
     * 
     * @param engineResult
     *            The engine result.
     */
    protected void setSslEngineResult(SSLEngineResult engineResult) {
        this.sslEngineResult = engineResult;
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
            if (getLogger().isLoggable(Level.FINER)) {
                getLogger().log(Level.FINER, "SSL engine result: " + sslResult);
                getLogger().log(Level.FINER, "SSL connection: " + toString());
            }

            // Store the engine result
            setSslEngineResult(sslResult);
        }
    }

    @Override
    protected void shutdown(Socket socket) throws IOException {
        if (!(socket instanceof SSLSocket)) {
            super.shutdown(socket);
        }
    }

    @Override
    public String toString() {
        return super.toString() + " | " + getSslEngine() + " | "
                + getSslEngineResult();
    }

}
