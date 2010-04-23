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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
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
import org.restlet.Message;
import org.restlet.Response;
import org.restlet.data.Parameter;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.engine.http.io.Notifiable;
import org.restlet.engine.io.NioUtils;
import org.restlet.engine.security.SslUtils;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.ReadableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.ConnectorService;
import org.restlet.util.Series;

/**
 * A network connection though which messages are exchanged by connectors.
 * Messages can be either requests or responses.
 * 
 * @param <T>
 *            The parent connector type.
 * @author Jerome Louvel
 */
public abstract class Connection<T extends Connector> implements Notifiable {
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
        this.inboundWay = new Way();
        this.outboundWay = new Way();
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

        // In some cases there is an entity without a content-length header
        boolean connectionClosed = HeaderUtils.isConnectionClose(headers);

        // Create the representation
        if ((contentLength != Representation.UNKNOWN_SIZE && contentLength != 0)
                || chunkedEncoding || connectionClosed) {
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
            try {
                result = HeaderUtils.copyResponseEntityHeaders(headers, result);
            } catch (Throwable t) {
                getLogger().log(Level.WARNING,
                        "Error while parsing entity headers", t);
            }
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
        return getSocketChannel();
    }

    /**
     * Returns the response entity stream if it exists.
     * 
     * @return The response entity stream if it exists.
     */
    public OutputStream getOutboundEntityStream(boolean chunked) {
        // OutputStream result = getOutboundChannel();
        //
        // if (chunked) {
        // result = new ChunkedOutputStream(result);
        // }
        //
        // return result;
        return null;
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
        setInboundBusy(false);
    }

    /**
     * Set the inbound busy state to false and the connection state to
     * {@link ConnectionState#CLOSING}.
     */
    public void onError() {
        setInboundBusy(false);
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
     * Reads available bytes from the socket channel.
     * 
     * @return The number of bytes read.
     * @throws IOException
     */
    public int readBytes() throws IOException {
        getBuffer().clear();
        int result = getSocketChannel().read(getBuffer());
        getBuffer().flip();
        return result;
    }

    /**
     * Reads the next message received via the inbound stream or channel. Note
     * that the optional entity is not fully read.
     * 
     * @throws IOException
     */
    protected abstract void readMessage() throws IOException;

    /**
     * Reads the header lines of the current message received.
     * 
     * @throws IOException
     */
    protected abstract void readMessageHeaders() throws IOException;

    /**
     * Read the current message line (start line or header line).
     * 
     * @return True if the message line was fully read.
     * @throws IOException
     */
    protected boolean readMessageLine() throws IOException {
        boolean result = false;
        int next;

        while (!result && getBuffer().hasRemaining()) {
            next = (int) getBuffer().get();

            if (HeaderUtils.isCarriageReturn(next)) {
                next = (int) getBuffer().get();

                if (HeaderUtils.isLineFeed(next)) {
                    result = true;
                } else {
                    throw new IOException(
                            "Missing carriage return character at the end of HTTP line");
                }
            } else {
                getBuilder().append((char) next);
            }
        }

        return result;
    }

    /**
     * Reads inbound messages from the socket. Only one message at a time if
     * pipelining isn't enabled.
     */
    public void readMessages() {
        try {
            if (canRead()) {
                int result = readBytes();

                while (getBuffer().hasRemaining()) {
                    readMessage();

                    if (!getBuffer().hasRemaining()) {
                        // Attempt to read more
                        result = readBytes();
                    }
                }

                if (result == -1) {
                    close(true);
                }
            }
        } catch (Exception e) {
            getLogger()
                    .log(
                            Level.INFO,
                            "Error while reading a message. Closing the connection.",
                            e);
            close(false);
        }
    }

    /**
     * Reads the start line of the current message received.
     * 
     * @throws IOException
     */
    protected abstract void readMessageStart() throws IOException;

    /**
     * Registers interest of this connection for NIO operations with the given
     * selector. If called several times, it just update the selection key with
     * the new interest operations.
     * 
     * @param selector
     *            The selector to register with.
     */
    public void registerInterest(Selector selector) {
        int socketInterest = 0;
        int entityInterest = 0;

        try {
            if (getIoState() == WayIoState.READ_INTEREST) {
                socketInterest = socketInterest | SelectionKey.OP_READ;
            }

            if (getOutboundIoState() == WayIoState.WRITE_INTEREST) {
                socketInterest = socketInterest | SelectionKey.OP_WRITE;
            } else if (getOutboundIoState() == WayIoState.READ_INTEREST) {
                entityInterest = entityInterest | SelectionKey.OP_READ;
            }

            if (socketInterest > 0) {
                getSocketChannel().register(selector, socketInterest, this);
            }

            if (entityInterest > 0) {
                Representation entity = (getOutboundMessage() == null) ? null
                        : getOutboundMessage().getEntity();

                if (entity instanceof ReadableRepresentation) {
                    ReadableRepresentation readableEntity = (ReadableRepresentation) entity;

                    if (readableEntity.getChannel() instanceof SelectableChannel) {
                        SelectableChannel selectableChannel = (SelectableChannel) readableEntity
                                .getChannel();

                        if (!selectableChannel.isBlocking()) {
                            selectableChannel.register(selector,
                                    entityInterest, this);
                        }
                    }
                }
            }
        } catch (ClosedChannelException cce) {
            getLogger()
                    .log(
                            Level.WARNING,
                            "Unable to register NIO interest operations for this connection",
                            cce);
            setState(ConnectionState.CLOSING);
        }
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

    public void updateIoStates() {

    }

    /**
     * Write the next outbound message on the socket.
     * 
     */
    protected abstract void writeMessage();

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
        writeMessageStart();

        // Write the headers
        for (Parameter header : headers) {
            HeaderUtils.writeHeaderLine(header, headStream);
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
        // writeMessageHead(message, getSocketChannel(), headers);
    }

    /**
     * Writes outbound messages to the socket. Only one response at a time if
     * pipelining isn't enabled.
     */
    public void writeMessages() {
        try {
            Response message = null;

            if (canWrite()) {
                message = getOutboundMessages().peek();
                setOutboundBusy((message != null));

                if (message != null) {
                    writeMessage(message);

                    // Try to close the connection immediately.
                    if ((getState() == ConnectionState.CLOSING) && !isBusy()) {
                        close(true);
                    }
                }
            }

        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Error while writing an HTTP message: ", e.getMessage());
            getLogger().log(Level.INFO, "Error while writing an HTTP message",
                    e);
        }
    }

    /**
     * Writes the start line of the current outbound message.
     * 
     * @throws IOException
     */
    protected abstract void writeMessageStart() throws IOException;

    /**
     * Writes the start line of the current outbound message.
     * 
     * @throws IOException
     */
    protected abstract void writeMessageStart() throws IOException;

}
