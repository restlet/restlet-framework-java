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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
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
import org.restlet.Response;
import org.restlet.data.Parameter;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.engine.http.io.ChunkedInputStream;
import org.restlet.engine.http.io.ChunkedOutputStream;
import org.restlet.engine.http.io.InboundStream;
import org.restlet.engine.http.io.InputEntityStream;
import org.restlet.engine.http.io.OutboundStream;
import org.restlet.engine.io.TraceInputStream;
import org.restlet.engine.io.TraceOutputStream;
import org.restlet.engine.security.SslUtils;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.ReadableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.ConnectorService;
import org.restlet.util.Series;

/**
 * A network connection though which requests and responses are exchanged by
 * connectors.
 * 
 * @param <T>
 *            The parent connector type.
 * @author Jerome Louvel
 */
public abstract class Connection<T extends Connector> {
    /** Indicates if the input of the socket is busy. */
    private volatile boolean inboundBusy;

    /** Indicates if the output of the socket is busy. */
    private volatile boolean outboundBusy;

    /** The inbound BIO stream. */
    private final InputStream inboundStream;

    /** The outbound BIO stream. */
    private final OutputStream outboundStream;

    /** Indicates if the connection should be persisted across calls. */
    private volatile boolean persistent;

    /** Indicates if idempotent sequences of requests can be pipelined. */
    private volatile boolean pipelining;

    /** The state of the connection. */
    private volatile ConnectionState state;

    /** The underlying BIO socket. */
    private final Socket socket;

    /** The underlying NIO socket channel. */
    private final SocketChannel socketChannel;

    /** The parent connector helper. */
    private final BaseHelper<T> helper;

    /** The queue of inbound messages. */
    private final Queue<Response> inboundMessages;

    /** The queue of outbound messages. */
    private final Queue<Response> outboundMessages;

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
    public Connection(BaseHelper<T> helper, Socket socket,
            SocketChannel socketChannel) throws IOException {
        this.helper = helper;
        this.inboundMessages = new ConcurrentLinkedQueue<Response>();
        this.outboundMessages = new ConcurrentLinkedQueue<Response>();
        this.persistent = helper.isPersistingConnections();
        this.pipelining = helper.isPipeliningConnections();
        this.state = ConnectionState.OPENING;
        this.socket = socket;
        this.socketChannel = socketChannel;
        this.inboundBusy = false;
        this.outboundBusy = false;

        if (getHelper().isTracing()) {
            this.inboundStream = new TraceInputStream(new InboundStream(
                    getSocket().getInputStream()));
            this.outboundStream = new TraceOutputStream(new OutboundStream(
                    getSocket().getOutputStream()));
        } else {
            this.inboundStream = new InboundStream(getSocket().getInputStream());
            this.outboundStream = new OutboundStream(getSocket()
                    .getOutputStream());
        }
    }

    /**
     * Adds additional headers if they are non-standard headers.
     * 
     * @param existingHeaders
     *            The headers to update.
     * @param additionalHeaders
     *            The headers to add.
     */
    protected void addAdditionalHeaders(Series<Parameter> existingHeaders,
            Series<Parameter> additionalHeaders) {
        if (additionalHeaders != null) {
            for (final Parameter param : additionalHeaders) {
                if (param.getName().equalsIgnoreCase(
                        HeaderConstants.HEADER_ACCEPT)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_ACCEPT_CHARSET)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_ACCEPT_ENCODING)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_ACCEPT_LANGUAGE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_ACCEPT_RANGES)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_AGE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_ALLOW)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_AUTHENTICATION_INFO)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_AUTHORIZATION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CACHE_CONTROL)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONNECTION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_DISPOSITION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_ENCODING)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_LANGUAGE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_LENGTH)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_LOCATION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_MD5)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_RANGE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_CONTENT_TYPE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_COOKIE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_DATE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_ETAG)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_EXPIRES)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_FROM)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_HOST)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_IF_MATCH)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_IF_MODIFIED_SINCE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_IF_NONE_MATCH)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_IF_RANGE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_IF_UNMODIFIED_SINCE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_LAST_MODIFIED)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_LOCATION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_PROXY_AUTHENTICATE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_PROXY_AUTHORIZATION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_RANGE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_REFERRER)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_RETRY_AFTER)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_SERVER)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_SET_COOKIE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_SET_COOKIE2)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_USER_AGENT)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_VARY)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_WARNING)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_WWW_AUTHENTICATE)) {
                    // Standard headers that can't be overridden
                    getLogger()
                            .warning(
                                    "Addition of the standard header \""
                                            + param.getName()
                                            + "\" is not allowed. Please use the equivalent property in the Restlet API.");
                } else if (param.getName().equalsIgnoreCase(
                        HeaderConstants.HEADER_EXPECT)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_PRAGMA)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_TRAILER)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_TRANSFER_ENCODING)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_TRANSFER_EXTENSION)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_UPGRADE)
                        || param.getName().equalsIgnoreCase(
                                HeaderConstants.HEADER_VIA)) {
                    // Standard headers that shouldn't be overridden
                    getLogger()
                            .info(
                                    "Addition of the standard header \""
                                            + param.getName()
                                            + "\" is discouraged as a future versions of the Restlet API will directly support it.");
                    existingHeaders.add(param);
                } else {
                    existingHeaders.add(param);
                }
            }
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
     * Adds the transport headers related to persistent connections and chunked
     * encoding.
     * 
     * @param headers
     *            The series of headers to update.
     * @param entity
     *            The optional entity sent.
     */
    protected void addTransportHeaders(Series<Parameter> headers,
            Representation entity) {

        if (!isPersistent()) {
            headers.set(HeaderConstants.HEADER_CONNECTION, "close", true);
        }

        if (shouldBeChunked(entity)) {
            headers.add(HeaderConstants.HEADER_TRANSFER_ENCODING, "chunked");
        }

    }

    /**
     * Indicates if the connection's socket can be read for inbound data.
     * 
     * @return True if the connection's socket can be read for inbound data.
     * @throws IOException
     */
    public boolean canRead() {
        return (getState() == ConnectionState.OPEN) && !isInboundBusy();
    }

    /**
     * Indicates if the connection's socket can be written for outbound data.
     * 
     * @return True if the connection's socket can be written for outbound data.
     * @throws IOException
     */
    public boolean canWrite() {
        return (getState() == ConnectionState.OPEN) && !isOutboundBusy()
                && (getOutboundMessages().size() > 0);
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
     * Returns the inbound message entity if available.
     * 
     * @param headers
     *            The headers to use.
     * @return The inbound message if available.
     */
    public Representation createInboundEntity(Series<Parameter> headers) {
        Representation result = null;
        long contentLength = HeaderUtils.getContentLength(headers);
        boolean chunkedEncoding = HeaderUtils.isChunkedEncoding(headers);

        // Create the representation
        if ((contentLength != Representation.UNKNOWN_SIZE && contentLength != 0)
                || chunkedEncoding) {
            InputStream inboundEntityStream = getInboundEntityStream(
                    contentLength, chunkedEncoding);
            ReadableByteChannel inboundEntityChannel = getInboundEntityChannel(
                    contentLength, chunkedEncoding);

            if (inboundEntityStream != null) {
                result = new InputRepresentation(inboundEntityStream, null,
                        contentLength) {

                    @Override
                    public String getText() throws IOException {
                        try {
                            return super.getText();
                        } catch (IOException ioe) {
                            throw ioe;
                        } finally {
                            release();
                        }
                    }

                    @Override
                    public void release() {
                        if (getHelper().isTracing()) {
                            synchronized (System.out) {
                                System.out.println("\n");
                            }
                        }

                        super.release();
                        setInboundBusy(false);
                    }
                };
            } else if (inboundEntityChannel != null) {
                result = new ReadableRepresentation(inboundEntityChannel, null,
                        contentLength) {
                    @Override
                    public void release() {
                        super.release();
                        setInboundBusy(false);
                    }
                };
            }

            result.setSize(contentLength);
        } else {
            result = new EmptyRepresentation();

            // Mark the inbound as free so new messages can be read if possible
            setInboundBusy(false);
        }

        if (headers != null) {
            result = HeaderUtils.copyResponseEntityHeaders(headers, result);
        }

        return result;
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
     * Returns the inbound message entity channel if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * 
     * @return The inbound message entity channel if it exists.
     */
    public ReadableByteChannel getInboundEntityChannel(long size,
            boolean chunked) {
        return null;
    }

    /**
     * Returns the inbound message entity stream if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * 
     * @return The inbound message entity stream if it exists.
     */
    public InputStream getInboundEntityStream(long size, boolean chunked) {
        InputStream result = null;

        if (chunked) {
            result = new ChunkedInputStream(getInboundStream());
        } else if (size >= 0) {
            result = new InputEntityStream(getInboundStream(), size);
        } else {
            result = getInboundStream();
        }

        return result;
    }

    /**
     * Returns the queue of inbound messages.
     * 
     * @return The queue of inbound messages.
     */
    public Queue<Response> getInboundMessages() {
        return inboundMessages;
    }

    /**
     * Returns the inbound stream.
     * 
     * @return The inbound stream.
     */
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

    /**
     * Returns the response channel if it exists.
     * 
     * @return The response channel if it exists.
     */
    public WritableByteChannel getOutboundEntityChannel(boolean chunked) {
        return null;
    }

    /**
     * Returns the response entity stream if it exists.
     * 
     * @return The response entity stream if it exists.
     */
    public OutputStream getOutboundEntityStream(boolean chunked) {
        OutputStream result = getOutboundStream();

        if (chunked) {
            result = new ChunkedOutputStream(result);
        }

        return result;
    }

    /**
     * Returns the queue of outbound messages.
     * 
     * @return The queue of outbound messages.
     */
    public Queue<Response> getOutboundMessages() {
        return outboundMessages;
    }

    /**
     * Returns the outbound stream.
     * 
     * @return The outbound stream.
     */
    public OutputStream getOutboundStream() {
        return this.outboundStream;
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

    /**
     * Returns the underlying socket.
     * 
     * @return The underlying socket.
     */
    public Socket getSocket() {
        return socket;
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
        return isInboundBusy() || isOutboundBusy();
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
     * Indicates if the input of the socket is busy.
     * 
     * @return True if the input of the socket is busy.
     */
    public boolean isInboundBusy() {
        return inboundBusy;
    }

    /**
     * Indicates if the output of the socket is busy.
     * 
     * @return True if the output of the socket is busy.
     */
    public boolean isOutboundBusy() {
        return outboundBusy;
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
     * Reads the next message received via the inbound stream or channel. Note
     * that the optional entity is not fully read.
     * 
     * @throws IOException
     */
    protected abstract void readMessage() throws IOException;

    /**
     * Reads inbound messages from the socket. Only one message at a time if
     * pipelining isn't enabled.
     */
    public void readMessages() {
        try {
            boolean doRead = false;

            // We want to make sure that messages are read by one thread at a
            // time without blocking other concurrent threads during the reading
            synchronized (this) {
                if (canRead()) {
                    doRead = true;
                    setInboundBusy(true);
                }
            }

            if (doRead) {
                readMessage();
            }
        } catch (Exception e) {
            getLogger()
                    .log(
                            Level.FINE,
                            "Error while reading an HTTP message. Closing the connection: ",
                            e.getMessage());
            getLogger()
                    .log(
                            Level.FINE,
                            "Error while reading an HTTP message. Closing the connection.",
                            e);
            close(false);
        }

        // Immediately attempt to handle the next pending message, trying to
        // prevent a thread context switch.
        getHelper().handleNextInbound();
    }

    /**
     * Indicates if the input of the socket is busy.
     * 
     * @param inboundBusy
     *            True if the input of the socket is busy.
     */
    protected void setInboundBusy(boolean inboundBusy) {
        this.inboundBusy = inboundBusy;
    }

    /**
     * Indicates if the output of the socket is busy.
     * 
     * @param outboundBusy
     *            True if the output of the socket is busy.
     */
    protected void setOutboundBusy(boolean outboundBusy) {
        this.outboundBusy = outboundBusy;
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
     * @param response
     *            The response to analyze.
     * @return True if the response should be chunked.
     */
    protected boolean shouldBeChunked(Representation entity) {
        return (entity != null)
                && (entity.getSize() == Representation.UNKNOWN_SIZE);
    }

    /**
     * Write the given message on the socket.
     * 
     * @param message
     *            The message to write.
     */
    protected abstract void writeMessage(Response message);

    /**
     * Writes the message and its headers.
     * 
     * @param message
     *            The message to write.
     * @throws IOException
     *             if the Response could not be written to the network.
     */
    protected void writeMessage(Response message, Series<Parameter> headers)
            throws IOException {
        if (message != null) {
            // Get the connector service to callback
            Representation entity = isClientSide() ? message.getRequest()
                    .getEntity() : message.getEntity();

            if (entity != null) {
                entity = entity.isAvailable() ? entity : null;
            }

            ConnectorService connectorService = ConnectorHelper
                    .getConnectorService();

            if (connectorService != null) {
                connectorService.beforeSend(entity);
            }

            try {
                writeMessageHead(message, headers);

                if (entity != null) {
                    // In order to workaround bug #6472250
                    // (http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6472250),
                    // it is very important to reuse that exact same
                    // "entityStream" reference when manipulating the entity
                    // stream, otherwise "insufficient data sent" exceptions
                    // will occur in "fixedLengthMode"
                    boolean chunked = HeaderUtils.isChunkedEncoding(headers);
                    WritableByteChannel entityChannel = getOutboundEntityChannel(chunked);
                    OutputStream entityStream = getOutboundEntityStream(chunked);
                    writeMessageBody(entity, entityChannel, entityStream);

                    if (entityStream != null) {
                        entityStream.flush();
                        entityStream.close();
                    }
                }
            } catch (IOException ioe) {
                // The stream was probably already closed by the
                // connector. Probably OK, low message priority.
                getLogger()
                        .log(
                                Level.FINE,
                                "Exception while flushing and closing the entity stream.",
                                ioe);
            } finally {
                if (entity != null) {
                    entity.release();
                }

                if (connectorService != null) {
                    connectorService.afterSend(entity);
                }
            }
        }
    }

    /**
     * Effectively writes the message body. The entity to write is guaranteed to
     * be non null. Attempts to write the entity on the outbound channel or
     * outbound stream by default.
     * 
     * @param entity
     *            The representation to write as entity of the body.
     * @param entityChannel
     *            The outbound entity channel or null if a stream is used.
     * @param entityStream
     *            The outbound entity stream or null if a channel is used.
     * @throws IOException
     */
    protected void writeMessageBody(Representation entity,
            WritableByteChannel entityChannel, OutputStream entityStream)
            throws IOException {
        if (entityChannel != null) {
            entity.write(entityChannel);
        } else if (entityStream != null) {
            entity.write(entityStream);
            entityStream.flush();

            if (getHelper().isTracing()) {
                synchronized (System.out) {
                    System.out.println("\n");
                }
            }
        }
    }

    /**
     * Writes the message head to the given output stream.
     * 
     * @param message
     *            The source message.
     * @param headStream
     *            The target stream.
     * @throws IOException
     */
    protected void writeMessageHead(Response message, OutputStream headStream,
            Series<Parameter> headers) throws IOException {

        // Write the head line
        writeMessageHeadLine(message, headStream);

        // Write the headers
        for (Parameter header : headers) {
            HeaderUtils.writeHeader(header, headStream);
        }

        // Write the end of the headers section
        headStream.write(13); // CR
        headStream.write(10); // LF
        headStream.flush();
    }

    /**
     * Writes the message head.
     * 
     * @param message
     *            The message.
     * @param headers
     *            The series of headers to write.
     * @throws IOException
     */
    protected void writeMessageHead(Response message, Series<Parameter> headers)
            throws IOException {
        writeMessageHead(message, getOutboundStream(), headers);
    }

    /**
     * Writes the message head line to the given output stream.
     * 
     * @param message
     *            The source message.
     * @param headStream
     *            The target stream.
     * @throws IOException
     */
    protected abstract void writeMessageHeadLine(Response message,
            OutputStream headStream) throws IOException;

    /**
     * Writes outbound messages to the socket. Only one response at a time if
     * pipelining isn't enabled.
     */
    public void writeMessages() {
        try {
            Response message = null;

            // We want to make sure that responses are written in order without
            // blocking other concurrent threads during the writing
            synchronized (this) {
                if (canWrite()) {
                    message = getOutboundMessages().peek();
                    setOutboundBusy((message != null));
                }
            }

            if (message != null) {
                writeMessage(message);

                // Try to close the connection immediately.
                if ((getState() == ConnectionState.CLOSING) && !isBusy()) {
                    close(true);
                }
            }
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Error while writing an HTTP message: ", e.getMessage());
            getLogger().log(Level.INFO, "Error while writing an HTTP message",
                    e);
        }
    }

}
