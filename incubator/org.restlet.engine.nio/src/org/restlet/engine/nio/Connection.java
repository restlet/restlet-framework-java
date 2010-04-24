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

package org.restlet.engine.nio;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.Selector;
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
import org.restlet.Message;
import org.restlet.Response;
import org.restlet.data.Parameter;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.engine.http.io.Notifiable;
import org.restlet.engine.security.SslUtils;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * A network connection though which messages are exchanged by connectors.
 * Messages can be either requests or responses.
 * 
 * @param <T>
 *            The parent connector type.
 * @author Jerome Louvel
 */
public class Connection<T extends Connector> implements Notifiable {

    /** The parent connector helper. */
    private final BaseHelper<T> helper;

    /** The inbound way. */
    private final Way inboundWay;

    /** The outbound way. */
    private final Way outboundWay;

    /** Indicates if the connection should be persisted across calls. */
    private volatile boolean persistent;

    /** Indicates if idempotent sequences of requests can be pipelined. */
    private volatile boolean pipelining;

    /** The underlying socket channel. */
    private final SocketChannel socketChannel;

    /** The state of the connection. */
    private volatile ConnectionState state;

    /**
     * Constructor.
     * 
     * @param helper
     *            The parent connector helper.
     * @param socket
     *            The underlying BIO socket.
     * @param socketChannel
     *            The underlying NIO socket channel.
     * @throws IOException
     */
    public Connection(BaseHelper<T> helper, SocketChannel socketChannel)
            throws IOException {
        this.helper = helper;
        this.inboundWay = new InboundWay(this);
        this.outboundWay = new OutboundWay(this);
        this.persistent = helper.isPersistingConnections();
        this.pipelining = helper.isPipeliningConnections();
        this.state = ConnectionState.OPENING;
        this.socketChannel = socketChannel;

        if (getHelper().isTracing()) {
            // this.inboundChannel = new TraceInputStream(new InboundStream(
            // getSocket().getInputStream()));
            // this.outboundChannel = new TraceOutputStream(new OutboundStream(
            // getSocket().getOutputStream()));
        } else {
            // this.inboundChannel = new InboundStream(getSocket()
            // .getInputStream());
            // this.outboundChannel = new OutboundStream(getSocket()
            // .getOutputStream());
        }
    }

    /**
     * Adds the entity headers for the given response.
     * 
     * @param entity
     *            The entity to inspect.
     */
    protected void addEntityHeaders(Representation entity,
            Series<Parameter> headers) {
        HeaderUtils.addEntityHeaders(entity, headers);
    }

    /**
     * Adds the general headers from the {@link Message} to the {@link Series}.
     * 
     * @param message
     *            The source {@link Message}.
     * @param headers
     *            The target headers {@link Series}.
     */
    protected void addGeneralHeaders(Message message, Series<Parameter> headers) {
        if (!isPersistent()) {
            headers.set(HeaderConstants.HEADER_CONNECTION, "close", true);
        }

        if (shouldBeChunked(message.getEntity())) {
            headers.add(HeaderConstants.HEADER_TRANSFER_ENCODING, "chunked");
        }

        HeaderUtils.addGeneralHeaders(message, headers);
    }

    /**
     * Closes the connection. By default, set the state to
     * {@link ConnectionState#CLOSED}.
     * 
     * @param graceful
     *            Indicates if a graceful close should be attempted.
     */
    public void close(boolean graceful) {
        try {
            if (!getSocket().isClosed()) {
                // Flush the output stream
                getSocket().getOutputStream().flush();

                if (!(getSocket() instanceof SSLSocket)) {
                    getSocket().shutdownInput();
                    getSocket().shutdownOutput();
                }
            }
        } catch (IOException ex) {
            getLogger().log(Level.FINE, "Unable to properly shutdown socket",
                    ex);
        }

        try {
            if (!getSocket().isClosed()) {
                getSocket().close();
            }
        } catch (IOException ex) {
            getLogger().log(Level.FINE, "Unable to properly close socket", ex);
        } finally {
            setState(ConnectionState.CLOSED);
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
    }

    /**
     * Returns the socket IP address.
     * 
     * @return The socket IP address.
     */
    public String getAddress() {
        return (getSocket().getInetAddress() == null) ? null : getSocket()
                .getInetAddress().getHostAddress();
    }

    /**
     * Returns the parent connector helper.
     * 
     * @return The parent connector helper.
     */
    public BaseHelper<T> getHelper() {
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
        return getSocket().getPort();
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
     * Indicates if the connection is busy.
     * 
     * @return True if the connection is busy.
     */
    public boolean isBusy() {
        return false; // isInboundBusy() || isOutboundBusy();
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
     * Set the inbound busy state to false.
     */
    public void onEndReached() {
        // setInboundBusy(false);
    }

    /**
     * Set the inbound busy state to false and the connection state to
     * {@link ConnectionState#CLOSING}.
     */
    public void onError() {
        // setInboundBusy(false);
        setState(ConnectionState.CLOSING);
    }

    /**
     * Opens the connection. By default, set the state to
     * {@link ConnectionState#OPEN}.
     */
    public void open() {
        try {
            setState(ConnectionState.OPEN);
        } catch (Exception ex) {
            getLogger().log(Level.FINE, "Unable to properly open socket", ex);
        }
    }

    /**
     * 
     * @param selector
     */
    public void registerInterest(Selector selector) {
        getInboundWay().registerInterest(selector);
        getOutboundWay().registerInterest(selector);
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
     * Sets the state of the connection.
     * 
     * @param state
     *            The state of the connection.
     */
    public void setState(ConnectionState state) {
        this.state = state;
    }

    /**
     * Indicates if the entity should be chunked because its length is unknown.
     * 
     * @param entity
     *            The entity to analyze.
     * @return True if the entity should be chunked.
     */
    protected boolean shouldBeChunked(Representation entity) {
        return (entity != null)
                && (entity.getSize() == Representation.UNKNOWN_SIZE);
    }

}
