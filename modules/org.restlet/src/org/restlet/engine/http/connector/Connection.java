/**
 * Copyright 2005-2009 Noelios Technologies.
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

package org.restlet.engine.http.connector;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;

import org.restlet.Connector;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.security.SslUtils;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;

/**
 * A network connection though which requests and responses are exchanged by
 * connectors.
 * 
 * @author Jerome Louvel
 */
public abstract class Connection<T extends Connector> {

    private volatile boolean persistent;

    private volatile boolean pipelining;

    private final Queue<Request> requests;

    private final Queue<Response> responses;

    private volatile ConnectionState state;

    private volatile boolean inboundBusy;

    private volatile boolean outboundBusy;

    /** The inbound stream. */
    private final InputStream inboundStream;

    /** The outbound stream. */
    private final OutputStream outboundStream;

    /** The connecting user */
    private final Socket socket;

    private final ConnectorHelper<T> helper;

    /**
     * Constructor.
     * 
     * @param helper
     * @param socket
     * @throws IOException
     */
    public Connection(ConnectorHelper<T> helper, Socket socket)
            throws IOException {
        this.helper = helper;
        this.persistent = false;
        this.pipelining = false;
        this.requests = new ConcurrentLinkedQueue<Request>();
        this.responses = new ConcurrentLinkedQueue<Response>();
        this.state = ConnectionState.CLOSED;
        this.socket = socket;
        this.inboundBusy = false;
        this.inboundStream = new BufferedInputStream(this.socket
                .getInputStream());
        this.outboundBusy = false;
        this.outboundStream = new BufferedOutputStream(this.socket
                .getOutputStream());
    }

    public String getAddress() {
        return (getSocket().getInetAddress() == null) ? null : getSocket()
                .getInetAddress().getHostAddress();
    }

    public ConnectorHelper<T> getHelper() {
        return helper;
    }

    public InputStream getInboundStream() {
        return this.inboundStream;
    }

    /**
     * Returns the logger.
     * 
     * @return The logger.
     */
    public Logger getLogger() {
        return getHelper().getLogger();
    }

    public OutputStream getOutboundStream() {
        return this.outboundStream;
    }

    public int getPort() {
        return getSocket().getPort();
    }

    /**
     * Returns the representation wrapping the given stream.
     * 
     * @param stream
     *            The response input stream.
     * @return The wrapping representation.
     */
    protected Representation getRepresentation(InputStream stream) {
        return new InputRepresentation(stream, null);
    }

    // [ifndef gwt] method
    /**
     * Returns the representation wrapping the given channel.
     * 
     * @param channel
     *            The response channel.
     * @return The wrapping representation.
     */
    protected Representation getRepresentation(
            java.nio.channels.ReadableByteChannel channel) {
        return new org.restlet.representation.ReadableRepresentation(channel,
                null);
    }

    public Queue<Request> getRequests() {
        return requests;
    }

    public Queue<Response> getResponses() {
        return responses;
    }

    public Socket getSocket() {
        return socket;
    }

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
        final String sslCipherSuite = getSslCipherSuite();

        if (sslCipherSuite != null) {
            keySize = SslUtils.extractKeySize(sslCipherSuite);
        }

        return keySize;
    }

    public ConnectionState getState() {
        return state;
    }

    public boolean isInboundBusy() {
        return inboundBusy;
    }

    public boolean isOutboundBusy() {
        return outboundBusy;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public boolean isPipelining() {
        return pipelining;
    }

    public void setInboundBusy(boolean inboundBusy) {
        this.inboundBusy = inboundBusy;
    }

    public void setOutboundBusy(boolean outboundBusy) {
        this.outboundBusy = outboundBusy;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }

    public void setPipelining(boolean pipelining) {
        this.pipelining = pipelining;
    }

    public void setState(ConnectionState state) {
        this.state = state;
    }
}
