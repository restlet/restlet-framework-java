/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.connector;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;

import org.restlet.Message;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.engine.io.BlockableChannel;
import org.restlet.engine.io.Buffer;
import org.restlet.engine.io.IoState;
import org.restlet.engine.io.ReadableChunkingChannel;
import org.restlet.engine.io.ReadableSizedChannel;
import org.restlet.engine.util.StringUtils;
import org.restlet.representation.Representation;
import org.restlet.service.ConnectorService;
import org.restlet.util.Series;

/**
 * A network connection way though which messages are sent. Messages can be
 * either requests or responses.
 * 
 * @author Jerome Louvel
 */
public abstract class OutboundWay extends Way {

    /**
     * Returns the protocol version.
     * 
     * @param request
     *            The request.
     * @return The protocol version.
     */
    protected static String getVersion(Request request) {
        Protocol protocol = request.getProtocol();
        String protocolVersion = protocol.getVersion();
        return protocol.getTechnicalName() + '/'
                + ((protocolVersion == null) ? "1.1" : protocolVersion);
    }

    /** The entity as a NIO readable byte channel. */
    private volatile ReadableByteChannel entityChannel;

    /** The type of the entity channel. */
    private volatile EntityType entityChannelType;

    /**
     * The entity's NIO selection key holding the link between the entity to be
     * written and the way.
     */
    private volatile SelectionKey entitySelectionKey;

    /** The header index. */
    private volatile int headerIndex;

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     * @param bufferSize
     *            The byte buffer size.
     */
    public OutboundWay(Connection<?> connection, int bufferSize) {
        super(connection, bufferSize);
        this.entityChannel = null;
        this.entitySelectionKey = null;
        this.headerIndex = 0;
    }

    /**
     * Adds the entity headers for the given response.
     * 
     * @param entity
     *            The entity to inspect.
     */
    protected void addEntityHeaders(Representation entity,
            Series<Header> headers) {
        HeaderUtils.addEntityHeaders(entity, headers);
    }

    /**
     * Adds the general headers from the {@link Message} to the {@link Series}.
     * 
     * @param headers
     *            The target headers {@link Series}.
     */
    protected void addGeneralHeaders(Series<Header> headers) {
        if (!getConnection().isPersistent()) {
            headers.set(HeaderConstants.HEADER_CONNECTION, "close", true);
        }

        if (shouldBeChunked(getActualMessage().getEntity())) {
            headers.add(HeaderConstants.HEADER_TRANSFER_ENCODING, "chunked");
        }

        HeaderUtils.addGeneralHeaders(getActualMessage(), headers);
    }

    /**
     * Add all the headers, including the general, the message specific and the
     * entity headers.
     * 
     * @param headers
     *            The headers to update.
     */
    protected abstract void addHeaders(Series<Header> headers);

    /**
     * Indicates if we should start processing the current message.
     * 
     * @return True if we should start processing the current message.
     */
    protected boolean canStart() {
        return (getMessageState() == MessageState.IDLE)
                && (getMessage() != null);
    }

    @Override
    public void clear() {
        super.clear();
        this.entityChannel = null;
        this.entitySelectionKey = null;
        this.headerIndex = 0;
    }

    /**
     * Returns the entity as a NIO readable byte channel.
     * 
     * @return The entity as a NIO readable byte channel.
     */
    public ReadableByteChannel getEntityChannel() {
        return entityChannel;
    }

    /**
     * Returns the type of the entity channel.
     * 
     * @return The type of the entity channel.
     */
    protected EntityType getEntityChannelType() {
        return entityChannelType;
    }

    /**
     * Returns the entity as a NIO file channel.
     * 
     * @return The entity as a NIO file channel.
     */
    public FileChannel getEntityFileChannel() {
        return (FileChannel) getEntityChannel();
    }

    /**
     * Registers interest of this way for socket NIO operations.
     * 
     * @return The operations of interest.
     */
    public int getEntityInterestOps() {
        int result = 0;

        if (getIoState() == IoState.INTEREST) {
            result = SelectionKey.OP_READ;
        }

        return result;
    }

    /**
     * Returns the entity as a NIO non-blocking selectable channel.
     * 
     * @return The entity as a NIO non-blocking selectable channel.
     */
    public SelectableChannel getEntitySelectableChannel() {
        return (SelectableChannel) getEntityChannel();
    }

    /**
     * Returns the entity's NIO selection key holding the link between the
     * entity to be written and the way.
     * 
     * @return The entity's NIO selection key.
     */
    public SelectionKey getEntitySelectionKey() {
        return entitySelectionKey;
    }

    /**
     * Returns the header index.
     * 
     * @return The header index.
     */
    protected int getHeaderIndex() {
        return headerIndex;
    }

    @Override
    public int getInterestOperations() {
        int result = 0;

        if (getIoState() == IoState.INTEREST) {
            result = SelectionKey.OP_WRITE;
        }

        return result;
    }

    /**
     * Add a message to the outbound way.
     * 
     * @param response
     */
    protected abstract void handle(Response response);

    /**
     * Indicates if we want to be selected for IO processing when the socket is
     * ready.
     * 
     * @return True if we want to be selected for IO processing when the socket
     *         is ready.
     */
    protected boolean hasIoInterest() {
        return (getMessageState() == MessageState.START)
                || getBuffer().canDrain();
    }

    @Override
    public void onCompleted(boolean endReached) {
        if (getActualMessage() != null) {
            Representation messageEntity = getActualMessage().getEntity();

            // Release entity
            if (messageEntity != null) {
                messageEntity.release();
            }

            // Callback connector service after sending entity
            ConnectorService connectorService = ConnectorHelper
                    .getConnectorService();

            if (connectorService != null) {
                connectorService.afterSend(messageEntity);
            }
        }

        super.onCompleted(endReached);
        setHeaderIndex(0);

        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().finer("Outbound message completed");
        }
    }

    @Override
    public int onDrain(Buffer buffer, int maxDrained, Object... args)
            throws IOException {
        int result = getBuffer().drain(
                getConnection().getWritableSelectionChannel());

        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().log(Level.FINER, result + " bytes written");
        }

        if (getHelper().getThrottleTimeMs() > 0) {
            try {
                Thread.sleep(getHelper().getThrottleTimeMs());
            } catch (InterruptedException e) {
            }
        }

        if (result == 0) {
            if (getIoState() == IoState.PROCESSING) {
                // The byte buffer hasn't been written, the socket
                // channel can't write more. We needs to put the
                // byte buffer in the filling state again and
                // wait for a new NIO selection.
                setIoState(IoState.INTEREST);
            }
        }

        return result;
    }

    @Override
    public void onError(Status status) {
        getHelper().onOutboundError(status, getMessage());
        setMessage(null);
    }

    @Override
    public int onFill(Buffer buffer, Object... args) throws IOException {
        int remaining = buffer.remaining();

        // Write the message or part of it in the byte
        // buffer
        if (getMessageState() == MessageState.BODY) {
            int filled = buffer.fill(getEntityChannel());

            // Detect end of entity reached
            if (filled == -1) {
                setMessageState(MessageState.END);
            }
        } else if (getMessageState() != MessageState.END) {
            // Write the start line or the headers,
            // relying on the line builder
            if (getLineBuilder().length() == 0) {
                // A new line can be written in the builder
                writeLine();
            }

            if (getLineBuilder().length() > 0) {
                // We can fill the byte buffer with the
                // remaining line builder
                if (remaining >= getLineBuilder().length()) {
                    // Put the whole builder line in the buffer
                    buffer.fill(StringUtils.getLatin1Bytes(getLineBuilder()
                            .toString()));

                    if (getLogger().isLoggable(Level.FINE)) {
                        String line = getLineBuilder().toString();
                        line = line.substring(0, line.length() - 2);
                        getLogger().log(Level.FINE, line);
                    }

                    clearLineBuilder();
                } else {
                    // Put the maximum number of characters
                    // into the byte buffer
                    buffer.fill(StringUtils.getLatin1Bytes(getLineBuilder()
                            .substring(0, remaining)));
                    getLineBuilder().delete(0, remaining);
                }
            }
        }

        return remaining - buffer.remaining();
    }

    /**
     * Called back when a fill operation returns with an EOF status.
     */
    public void onFillEof() {
    }

    @Override
    protected void onPostProcessing() {
        if ((getMessageState() != MessageState.IDLE) || getBuffer().canDrain()) {
            super.onPostProcessing();
        }
    }

    @Override
    public void onTimeOut() {
        if (getMessage() != null) {
            getHelper().onOutboundError(Status.CONNECTOR_ERROR_COMMUNICATION,
                    getMessage());
        }
    }

    @Override
    public int processIoBuffer() throws IOException {
        int result = super.processIoBuffer();

        if (getMessage() != null) {
            if (getMessageState() == MessageState.END) {
                // Message fully written, ready for a new one
                onCompleted(false);
            } else if (getMessageState() == MessageState.IDLE) {
                // Message fully sent, check if another is ready
                updateState();
            }
        }

        return result;
    }

    /**
     * Sets the entity as a NIO readable byte channel.
     * 
     * @param entityChannel
     *            The entity as a NIO readable byte channel.
     */
    public void setEntityChannel(ReadableByteChannel entityChannel) {
        this.entityChannel = entityChannel;
    }

    /**
     * Sets the type of the entity channel.
     * 
     * @param entityChannelType
     *            The type of the entity channel.
     */
    protected void setEntityChannelType(EntityType entityChannelType) {
        this.entityChannelType = entityChannelType;
    }

    /**
     * Sets the entity's NIO selection key holding the link between the entity
     * to be written and the way.
     * 
     * @param entityKey
     *            The entity's NIO selection key.
     */
    public void setEntitySelectionKey(SelectionKey entityKey) {
        this.entitySelectionKey = entityKey;
    }

    /**
     * Sets the header index.
     * 
     * @param headerIndex
     *            The header index.
     */
    protected void setHeaderIndex(int headerIndex) {
        this.headerIndex = headerIndex;
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
                && (entity.getAvailableSize() == Representation.UNKNOWN_SIZE);
    }

    @Override
    public void updateState() {
        if (canStart()) {
            setMessageState(MessageState.START);
        }

        if (hasIoInterest()) {
            setIoState(IoState.INTEREST);
        }

        super.updateState();
    }

    /**
     * Write a new line into the line builder.
     * 
     * @throws IOException
     */
    protected void writeLine() throws IOException {
        switch (getMessageState()) {
        case START:
            if (getHelper().getLogger().isLoggable(Level.FINE)) {
                getHelper().getLogger().log(
                        Level.FINE,
                        "Writing message to "
                                + getConnection().getSocketAddress());
            }

            writeStartLine();
            setMessageState(MessageState.HEADERS);
            break;

        case HEADERS:
            if (getHeaders() == null) {
                setHeaders(new Series<Header>(Header.class));
                setHeaderIndex(0);
                addHeaders(getHeaders());
            }

            if (getHeaderIndex() < getHeaders().size()) {
                // Write header
                Header header = getHeaders().get(getHeaderIndex());
                getLineBuilder().append(header.getName());
                getLineBuilder().append(": ");
                getLineBuilder().append(header.getValue());
                getLineBuilder().append('\r'); // CR
                getLineBuilder().append('\n'); // LF

                // Move to the next header
                setHeaderIndex(getHeaderIndex() + 1);
            } else {
                // Write the end of the headers section
                getLineBuilder().append('\r'); // CR
                getLineBuilder().append('\n'); // LF

                // Prepare entity writing if available
                if (getActualMessage().isEntityAvailable()) {
                    // Callback connector service before sending entity
                    ConnectorService connectorService = ConnectorHelper
                            .getConnectorService();

                    if (connectorService != null) {
                        connectorService.afterSend(getActualMessage()
                                .getEntity());
                    }

                    setMessageState(MessageState.BODY);
                    ReadableByteChannel rbc = getActualMessage().getEntity()
                            .getChannel();

                    if (rbc instanceof FileChannel) {
                        setEntityChannelType(EntityType.TRANSFERABLE);
                    } else if (rbc instanceof BlockableChannel) {
                        BlockableChannel bc = (BlockableChannel) rbc;

                        if (bc.isBlocking()) {
                            setEntityChannelType(EntityType.BLOCKING);
                        } else {
                            setEntityChannelType(EntityType.NON_BLOCKING);
                        }
                    } else if (rbc instanceof SelectableChannel) {
                        SelectableChannel sc = (SelectableChannel) rbc;

                        if (sc.isBlocking()) {
                            setEntityChannelType(EntityType.BLOCKING);
                        } else {
                            setEntityChannelType(EntityType.NON_BLOCKING);
                        }
                    } else {
                        setEntityChannelType(EntityType.BLOCKING);
                    }

                    if (getActualMessage().getEntity().getAvailableSize() == Representation.UNKNOWN_SIZE) {
                        setEntityChannel(new ReadableChunkingChannel(rbc,
                                getBuffer().capacity()));
                    } else {
                        setEntityChannel(new ReadableSizedChannel(rbc,
                                getActualMessage().getEntity()
                                        .getAvailableSize()));
                    }

                } else {
                    setMessageState(MessageState.END);
                }
            }

            break;
        }
    }

    /**
     * Writes the start line of the current outbound message.
     * 
     * @throws IOException
     */
    protected abstract void writeStartLine() throws IOException;

}
