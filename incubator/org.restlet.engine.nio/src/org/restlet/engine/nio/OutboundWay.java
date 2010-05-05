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
import java.io.OutputStream;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.WritableByteChannel;
import java.util.logging.Level;

import org.restlet.Message;
import org.restlet.Response;
import org.restlet.data.CharacterSet;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.engine.util.StringUtils;
import org.restlet.representation.ReadableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.ConnectorService;
import org.restlet.util.Series;

public class OutboundWay extends Way {

    /**
     * Constructor.
     * 
     * @param connection
     */
    public OutboundWay(Connection<?> connection) {
        super(connection);
        // this.outboundChannel = new OutboundStream(getSocket()
        // .getOutputStream());

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
        if (!getConnection().isPersistent()) {
            headers.set(HeaderConstants.HEADER_CONNECTION, "close", true);
        }

        if (shouldBeChunked(message.getEntity())) {
            headers.add(HeaderConstants.HEADER_TRANSFER_ENCODING, "chunked");
        }

        HeaderUtils.addGeneralHeaders(message, headers);
    }

    /**
     * Adds the response headers.
     * 
     * @param response
     *            The response to inspect.
     * @param headers
     *            The headers series to update.
     */
    protected void addResponseHeaders(Response response,
            Series<Parameter> headers) {
        HeaderUtils.addResponseHeaders(response, headers);
    }

    /**
     * Returns the response channel if it exists.
     * 
     * @return The response channel if it exists.
     */
    public WritableByteChannel getOutboundEntityChannel(boolean chunked) {
        return getConnection().getSocketChannel();
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

    @Override
    public void onSelected(SelectionKey key) {
        // TODO Auto-generated method stub

    }

    @Override
    public void registerInterest(Selector selector) {
        int socketInterest = 0;
        int entityInterest = 0;

        try {
            if (getIoState() == WayIoState.WRITE_INTEREST) {
                socketInterest = socketInterest | SelectionKey.OP_WRITE;
            } else if (getIoState() == WayIoState.READ_INTEREST) {
                entityInterest = entityInterest | SelectionKey.OP_READ;
            }

            if (socketInterest > 0) {
                getConnection().getSocketChannel().register(selector,
                        socketInterest, this);
            }

            if (entityInterest > 0) {
                Representation entity = (getMessage() == null) ? null
                        : getMessage().getEntity();

                if (entity instanceof ReadableRepresentation) {
                    ReadableRepresentation readableEntity = (ReadableRepresentation) entity;

                    try {
                        if (readableEntity.getChannel() instanceof SelectableChannel) {
                            SelectableChannel selectableChannel = (SelectableChannel) readableEntity
                                    .getChannel();

                            if (!selectableChannel.isBlocking()) {
                                selectableChannel.register(selector,
                                        entityInterest, this);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (ClosedChannelException cce) {
            getLogger()
                    .log(
                            Level.WARNING,
                            "Unable to register NIO interest operations for this connection",
                            cce);
            getConnection().setState(ConnectionState.CLOSING);
        }
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

    /**
     * Write the given response on the socket.
     * 
     * @param response
     *            The response to write.
     */
    protected void writeMessage(Response response) {
        if (getMessageState() == null) {
            setMessageState(WayMessageState.START_LINE);
            getBuilder().delete(0, getBuilder().length());
        }

        while (getBuffer().hasRemaining()) {
            if (getMessageState() == WayMessageState.START_LINE) {
                writeMessageStart();
            } else if (getMessageState() == WayMessageState.HEADERS) {
                readMessageHeaders();
            }
        }
    }

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
            Representation entity = getConnection().isClientSide() ? message
                    .getRequest().getEntity() : message.getEntity();

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
                message = getMessages().peek();
                setBusy((message != null));

                if (message != null) {
                    writeMessage(message);

                    // Try to close the connection immediately.
                    if ((getConnection().getState() == ConnectionState.CLOSING)
                            && !isBusy()) {
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
    protected void writeMessageStart() throws IOException {
        getBuilder().delete(0, getBuilder().length());

        Protocol protocol = getMessage().getRequest().getProtocol();
        String protocolVersion = protocol.getVersion();
        String version = protocol.getTechnicalName() + '/'
                + ((protocolVersion == null) ? "1.1" : protocolVersion);
        getBuilder()
                .append(version.getBytes(CharacterSet.ISO_8859_1.getName()));
        getBuilder().append(' ');
        getBuilder().append(
                StringUtils.getAsciiBytes(Integer.toString(getMessage()
                        .getStatus().getCode())));
        getBuilder().append(' ');

        if (getMessage().getStatus().getDescription() != null) {
            getBuilder().append(
                    StringUtils.getLatin1Bytes(getMessage().getStatus()
                            .getDescription()));
        } else {
            getBuilder().append(
                    StringUtils.getAsciiBytes(("Status " + getMessage()
                            .getStatus().getCode())));
        }

        getBuilder().append("\r\n");
    }

}
