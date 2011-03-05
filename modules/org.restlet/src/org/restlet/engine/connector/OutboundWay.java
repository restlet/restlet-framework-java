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

package org.restlet.engine.connector;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Message;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.engine.io.BlockableChannel;
import org.restlet.engine.io.Buffer;
import org.restlet.engine.io.IoState;
import org.restlet.engine.io.ReadableChunkingChannel;
import org.restlet.engine.io.ReadableSizedChannel;
import org.restlet.engine.util.StringUtils;
import org.restlet.representation.Representation;
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
            Series<Parameter> headers) {
        HeaderUtils.addEntityHeaders(entity, headers);
    }

    /**
     * Adds the general headers from the {@link Message} to the {@link Series}.
     * 
     * @param headers
     *            The target headers {@link Series}.
     */
    protected void addGeneralHeaders(Series<Parameter> headers) {
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
    protected abstract void addHeaders(Series<Parameter> headers);

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

    @Override
    public void onCompleted(boolean endReached) {
        super.onCompleted(endReached);
        setHeaderIndex(0);

        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().finer("Outbound message completed");
        }
    }

    @Override
    public void updateState() {
        if (getBuffer().canDrain()) {
            setIoState(IoState.INTEREST);
        }

        super.updateState();
    }

    @Override
    public int onDrain(Buffer buffer, int maxDrained, Object... args)
            throws IOException {
        int result = getBuffer().drain(
                getConnection().getWritableSelectionChannel());

        if (getLogger().isLoggable(Level.FINE)) {
            getLogger().log(Level.FINE, result + " bytes written");
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

    @Override
    public int processIoBuffer() throws IOException {
        int result = 0;
        result = super.processIoBuffer();

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

    @Override
    public void setIoState(IoState ioState) {
        if (Context.getCurrentLogger().isLoggable(Level.FINER)) {
            Context.getCurrentLogger().log(Level.FINER,
                    "Outbound way: " + ioState);
        }

        super.setIoState(ioState);
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

    /**
     * Write a new line into the line builder.
     * 
     * @throws IOException
     */
    protected void writeLine() throws IOException {
        switch (getMessageState()) {
        case START:
            if (getHelper().getLogger().isLoggable(Level.FINE)) {
                getHelper().getLogger().fine(
                        "Writing message to "
                                + getConnection().getSocketAddress());
            }

            writeStartLine();
            setMessageState(MessageState.HEADERS);
            break;

        case HEADERS:
            if (getHeaders() == null) {
                setHeaders(new Form());
                setHeaderIndex(0);
                addHeaders(getHeaders());
            }

            if (getHeaderIndex() < getHeaders().size()) {
                // Write header
                Parameter header = getHeaders().get(getHeaderIndex());
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
