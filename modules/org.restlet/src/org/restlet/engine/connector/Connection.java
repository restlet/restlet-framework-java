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

package org.restlet.engine.connector;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.SocketChannel;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.restlet.Connector;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.engine.io.IoState;
import org.restlet.engine.io.ReadableSelectionChannel;
import org.restlet.engine.io.ReadableSocketChannel;
import org.restlet.engine.io.ReadableTraceChannel;
import org.restlet.engine.io.WritableSelectionChannel;
import org.restlet.engine.io.WritableSocketChannel;
import org.restlet.engine.io.WritableTraceChannel;
import org.restlet.engine.security.SslUtils;
import org.restlet.util.SelectionListener;
import org.restlet.util.SelectionRegistration;

/**
 * A network connection though which messages are exchanged by connectors.
 * Messages can be either requests or responses.
 * 
 * @param <T>
 *            The parent connector type.
 * @author Jerome Louvel
 */
public class Connection<T extends Connector> implements SelectionListener {

    /** The readable selection channel. */
    private volatile ReadableSelectionChannel readableSelectionChannel;

    /** The writable selection channel. */
    private volatile WritableSelectionChannel writableSelectionChannel;

    /** The parent connector helper. */
    private final ConnectionHelper<T> helper;

    /** The inbound way. */
    private final Way inboundWay;

    /** The timestamp of the last IO activity. */
    private volatile long lastActivity;

    /** The outbound way. */
    private final Way outboundWay;

    /** Indicates if the connection should be persisted across calls. */
    private volatile boolean persistent;

    /** Indicates if idempotent sequences of requests can be pipelined. */
    private volatile boolean pipelining;

    /** The underlying socket channel. */
    private volatile SocketChannel socketChannel;

    /** The socket address. */
    private volatile SocketAddress socketAddress;

    /**
     * The socket's NIO selection registration holding the link between the
     * channel and the connection.
     */
    private volatile SelectionRegistration registration;

    /** The state of the connection. */
    private volatile ConnectionState state;

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
     * @throws IOException
     */
    public Connection(ConnectionHelper<T> helper, SocketChannel socketChannel,
            ConnectionController controller, SocketAddress socketAddress)
            throws IOException {
        this.helper = helper;
        this.inboundWay = helper.createInboundWay(this);
        this.outboundWay = helper.createOutboundWay(this);
        reuse(socketChannel, controller, socketAddress);
    }

    /**
     * Clears the connection so it can be reused. Typically invoked by a
     * connection pool.
     */
    public void clear() {
        this.readableSelectionChannel = null;
        this.socketChannel = null;
        this.registration = null;
        this.state = null;
        this.writableSelectionChannel = null;
        this.inboundWay.clear();
        this.outboundWay.clear();
    }

    /**
     * Closes the connection. By default, set the state to
     * {@link ConnectionState#CLOSED}.
     * 
     * @param graceful
     *            Indicates if a graceful close should be attempted.
     */
    public void close(boolean graceful) {
        if (graceful) {
            if (getLogger().isLoggable(Level.FINER)) {
                getLogger().log(
                        Level.FINER,
                        "Closing connection to " + getSocketAddress()
                                + " gracefully");
            }

            if (getRegistration() != null) {
                getRegistration().setCanceling(true);
            }

            setState(ConnectionState.CLOSING);
        } else {
            if (getLogger().isLoggable(Level.FINER)) {
                getLogger().log(
                        Level.FINER,
                        "Closing connection to " + getSocketAddress()
                                + " immediately");
            }

            try {
                Socket socket = getSocket();

                if ((socket != null) && !socket.isClosed()) {
                    if (!(socket instanceof SSLSocket)) {
                        socket.shutdownInput();
                        socket.shutdownOutput();
                    }
                }
            } catch (IOException ex) {
                getLogger().log(Level.FINE,
                        "Unable to properly shutdown socket", ex);
            } finally {
                setState(ConnectionState.CLOSED);
            }

            try {
                Socket socket = getSocket();

                if ((socket != null) && !socket.isClosed()) {
                    socket.close();
                }
            } catch (IOException ex) {
                getLogger().log(Level.FINE, "Unable to properly close socket",
                        ex);
            } finally {
                setState(ConnectionState.CLOSED);
            }
        }
    }

    /**
     * Asks the server connector to immediately commit the given response
     * associated to this request, making it ready to be sent back to the
     * client. Note that all server connectors don't necessarily support this
     * feature.
     * 
     * @param response
     *            The response to commit.
     */
    public void commit(Response response) {
        getHelper().getOutboundMessages().add(response);

        // Wake up the controller if it is sleeping
        getHelper().getController().wakeup();
    }

    /**
     * Returns the socket IP address.
     * 
     * @return The socket IP address.
     */
    public String getAddress() {
        return (getSocket() == null) ? null
                : (getSocket().getInetAddress() == null) ? null : getSocket()
                        .getInetAddress().getHostAddress();
    }

    /**
     * Returns the parent connector helper.
     * 
     * @return The parent connector helper.
     */
    public ConnectionHelper<T> getHelper() {
        return helper;
    }

    /**
     * Returns the inbound way.
     * 
     * @return The inbound way.
     */
    public Way getInboundWay() {
        return inboundWay;
    }

    /**
     * Returns the logger.
     * 
     * @return The logger.
     */
    public Logger getLogger() {
        return getHelper().getLogger();
    }

    /**
     * Returns the outbound way.
     * 
     * @return The outbound way.
     */
    public Way getOutboundWay() {
        return outboundWay;
    }

    /**
     * Returns the socket port.
     * 
     * @return The socket port.
     */
    public int getPort() {
        return (getSocket() == null) ? -1 : getSocket().getPort();
    }

    /**
     * Returns the underlying socket channel as a readable selection channel.
     * 
     * @return The underlying socket channel as a readable selection channel.
     */
    protected ReadableSelectionChannel getReadableSelectionChannel() {
        return readableSelectionChannel;
    }

    /**
     * Returns the socket's NIO registration holding the link between the
     * {@link SocketChannel} and the {@link Connection}.
     * 
     * @return The socket's NIO registration holding the link between the
     *         channel and the connection.
     */
    protected SelectionRegistration getRegistration() {
        return registration;
    }

    /**
     * Returns the underlying socket.
     * 
     * @return The underlying socket.
     */
    public Socket getSocket() {
        return (getSocketChannel() == null) ? null : getSocketChannel()
                .socket();
    }

    /**
     * Returns the socket address.
     * 
     * @return The socket address.
     */
    protected SocketAddress getSocketAddress() {
        return socketAddress;
    }

    /**
     * Returns the underlying NIO socket channel.
     * 
     * @return The underlying NIO socket channel.
     */
    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    /**
     * Returns the SSL cipher suite.
     * 
     * @return The SSL cipher suite.
     */
    public String getSslCipherSuite() {
        if (getSocket() instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) getSocket();
            SSLSession sslSession = sslSocket.getSession();

            if (sslSession != null) {
                return sslSession.getCipherSuite();
            }
        }

        return null;
    }

    /**
     * Returns the list of client SSL certificates.
     * 
     * @return The list of client SSL certificates.
     */
    public List<Certificate> getSslClientCertificates() {
        if (getSocket() instanceof SSLSocket) {
            SSLSocket sslSocket = (SSLSocket) getSocket();
            SSLSession sslSession = sslSocket.getSession();

            if (sslSession != null) {
                try {
                    List<Certificate> clientCertificates = Arrays
                            .asList(sslSession.getPeerCertificates());
                    return clientCertificates;
                } catch (SSLPeerUnverifiedException e) {
                    getHelper().getLogger().log(Level.FINE,
                            "Can't get the client certificates.", e);
                }
            }
        }

        return null;
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
     * Returns the state of the connection.
     * 
     * @return The state of the connection.
     */
    public ConnectionState getState() {
        return state;
    }

    /**
     * Returns the underlying socket channel as a writable selection channel.
     * 
     * @return The underlying socket channel as a writable selection channel.
     */
    protected WritableSelectionChannel getWritableSelectionChannel() {
        return writableSelectionChannel;
    }

    /**
     * Indicates if the connection has timed out.
     * 
     * @return True if the connection has timed out.
     */
    public boolean hasTimedOut() {
        return (System.currentTimeMillis() - this.lastActivity) >= getHelper()
                .getMaxIoIdleTimeMs();
    }

    /**
     * Indicates if it is a client-side connection.
     * 
     * @return True if it is a client-side connection.
     */
    public boolean isClientSide() {
        return getHelper().isClientSide();
    }

    /**
     * Indicates if the connection is empty.
     * 
     * @return True if the connection is empty.
     */
    public boolean isEmpty() {
        return getInboundWay().getMessages().isEmpty()
                && getOutboundWay().getMessages().isEmpty();
    }

    /**
     * Indicates if the connection should be persisted across calls.
     * 
     * @return True if the connection should be persisted across calls.
     */
    public boolean isPersistent() {
        return persistent;
    }

    /**
     * Indicates if idempotent sequences of requests can be pipelined.
     * 
     * @return True requests pipelining is enabled.
     */
    public boolean isPipelining() {
        return pipelining;
    }

    /**
     * Indicates if it is a server-side connection.
     * 
     * @return True if it is a server-side connection.
     */
    public boolean isServerSide() {
        return getHelper().isServerSide();
    }

    /**
     * Called on error. By default, it calls {@link #close(boolean)} with a
     * 'false' parameter.
     * 
     * @param message
     *            The error message.
     * @param throwable
     *            The cause of the error.
     * @param status
     *            The error status.
     */
    public void onError(String message, Throwable throwable, Status status) {
        getLogger().log(Level.FINE, message, throwable);
        status = new Status(status, throwable, message);

        for (Response rsp : getInboundWay().getMessages()) {
            getInboundWay().getMessages().remove(rsp);
            getHelper().onError(status, rsp);
        }

        for (Response rsp : getOutboundWay().getMessages()) {
            getOutboundWay().getMessages().remove(rsp);
            getHelper().onError(status, rsp);
        }

        getHelper().onError(status, getInboundWay().getMessage());
        getHelper().onError(status, getOutboundWay().getMessage());
        close(false);
    }

    /**
     * Callback method invoked when the connection has been selected for IO
     * operations it registered interest in. By default it updates the timestamp
     * that allows the detection of expired connections and calls
     * {@link Way#onSelected(SelectionRegistration)} on the inbound or outbound
     * way.
     * 
     * @param registration
     *            The registered selection key.
     */
    public void onSelected(SelectionRegistration registration) {
        this.lastActivity = System.currentTimeMillis();

        if (getLogger().isLoggable(Level.FINER)) {
            String trace = null;

            if (isClientSide()) {
                trace = "Client ";
            } else {
                trace = "Server ";
            }

            getLogger().finer(
                    trace + "connection (state | inbound | outbound): "
                            + toString());
            getLogger().finer(
                    trace + "NIO selection (interest | ready | cancelled): "
                            + registration.toString());
        }

        try {
            if ((registration == null) || registration.isReadable()) {
                synchronized (getInboundWay().getByteBuffer()) {
                    getInboundWay().getRegistration().onSelected(
                            registration.getReadyOperations());
                }
            } else if (registration.isWritable()) {
                synchronized (getOutboundWay().getByteBuffer()) {
                    getOutboundWay().getRegistration().onSelected(
                            registration.getReadyOperations());
                }
            } else if (registration.isConnectable()) {
                // Client-side asynchronous connection
                try {
                    if (getSocketChannel().finishConnect()) {
                        open();
                    } else {
                        onError("Unable to establish a connection to "
                                + getSocketAddress(), null,
                                Status.CONNECTOR_ERROR_CONNECTION);
                    }
                } catch (IOException e) {
                    onError("Unable to establish a connection to "
                            + getSocketAddress(), e,
                            Status.CONNECTOR_ERROR_CONNECTION);
                }
            }
        } catch (Throwable t) {
            onError("Unexpected error detected. Closing the connection.", t,
                    Status.CONNECTOR_ERROR_INTERNAL);
        }
    }

    /**
     * Opens the connection. By default, set the IO state of the connection to
     * {@link ConnectionState#OPEN} and the IO state of the inbound way to
     * {@link IoState#INTEREST}.
     */
    public void open() {
        setState(ConnectionState.OPEN);
        updateState();
    }

    /**
     * Reuses the connection and associates it to the given socket.
     * 
     * @param socketChannel
     *            The underlying NIO socket channel.
     * @param controller
     *            The underlying IO controller.
     * @param socketAddress
     *            The associated socket address.
     * @throws IOException
     */
    public void reuse(SocketChannel socketChannel,
            ConnectionController controller, SocketAddress socketAddress)
            throws IOException {
        this.persistent = helper.isPersistingConnections();
        this.pipelining = helper.isPipeliningConnections();
        this.state = ConnectionState.OPENING;
        this.socketChannel = socketChannel;
        this.socketAddress = socketAddress;

        if ((controller != null) && (socketChannel != null)
                && (socketAddress != null)) {
            this.registration = (controller == null) ? null : controller
                    .register(socketChannel, 0, this);

            if (helper.isTracing()) {
                this.readableSelectionChannel = new ReadableTraceChannel(
                        new ReadableSocketChannel(socketChannel, registration));
                this.writableSelectionChannel = new WritableTraceChannel(
                        new WritableSocketChannel(socketChannel, registration));
            } else {
                this.readableSelectionChannel = new ReadableSocketChannel(
                        socketChannel, registration);
                this.writableSelectionChannel = new WritableSocketChannel(
                        socketChannel, registration);
            }
        }

        this.lastActivity = System.currentTimeMillis();
        this.inboundWay.reuse();
        this.outboundWay.reuse();
    }

    /**
     * Indicates if the connection should be persisted across calls.
     * 
     * @param persistent
     *            True if the connection should be persisted across calls.
     */
    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    /**
     * Indicates if idempotent sequences of requests can be pipelined.
     * 
     * @param pipelining
     *            True requests pipelining is enabled.
     */
    public void setPipelining(boolean pipelining) {
        this.pipelining = pipelining;
    }

    /**
     * Sets the socket's NIO registration holding the link between the channel
     * and the way.
     * 
     * @param registration
     *            The socket's NIO registration holding the link between the
     *            channel and the way.
     */
    protected void setRegistration(SelectionRegistration registration) {
        this.registration = registration;
    }

    /**
     * Sets the state of the connection.
     * 
     * @param state
     *            The state of the connection.
     */
    public void setState(ConnectionState state) {
        if (getLogger().isLoggable(Level.FINEST)) {
            getLogger().finest(
                    "Connection state (old | new) : " + this.state + " | "
                            + state);
        }

        this.state = state;
    }

    @Override
    public String toString() {
        return getState() + " | " + getInboundWay() + " | " + getOutboundWay();
    }

    /**
     * Updates the connection states.
     */
    public boolean updateState() {
        boolean result = false;

        if ((getState() != ConnectionState.CLOSING)
                && (getState() != ConnectionState.CLOSED)) {
            getInboundWay().updateState();
            getOutboundWay().updateState();

            // Update the registration
            result = getRegistration().setInterestOperations(
                    getInboundWay().getRegistration().getInterestOperations()
                            | getOutboundWay().getRegistration()
                                    .getInterestOperations());
        }

        return result;
    }

}
