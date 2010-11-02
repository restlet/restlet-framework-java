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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.data.Status;
import org.restlet.engine.http.header.HeaderReader;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.engine.http.io.ReadableChunkedChannel;
import org.restlet.engine.io.BufferState;
import org.restlet.engine.io.IoState;
import org.restlet.engine.io.NioUtils;
import org.restlet.engine.io.ReadableBufferedChannel;
import org.restlet.engine.io.ReadableSizedChannel;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.ReadableRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.SelectionRegistration;
import org.restlet.util.Series;

/**
 * A network connection way though which messages are received. Messages can be
 * either requests or responses.
 * 
 * @author Jerome Louvel
 */
public abstract class InboundWay extends Way {

    /** The line builder index. */
    private volatile int builderIndex;

    /** The NIO selection registration of the entity. */
    private volatile SelectionRegistration entityRegistration;

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     */
    public InboundWay(Connection<?> connection) {
        super(connection, connection.getHelper().getInboundBufferSize());
        this.builderIndex = 0;
    }

    @Override
    public void clear() {
        super.clear();
        this.builderIndex = 0;
        this.entityRegistration = null;
    }

    /**
     * Returns the message entity if available.
     * 
     * @param headers
     *            The headers to use.
     * @return The inbound message if available.
     */
    protected Representation createEntity(Series<Parameter> headers) {
        Representation result = null;
        long contentLength = HeaderUtils.getContentLength(headers);
        boolean chunkedEncoding = HeaderUtils.isChunkedEncoding(headers);

        // In some cases there is an entity without a content-length header
        boolean connectionClose = HeaderUtils.isConnectionClose(headers);

        // Create the representation
        if ((contentLength != Representation.UNKNOWN_SIZE && contentLength != 0)
                || chunkedEncoding || connectionClose) {
            ReadableByteChannel inboundEntityChannel = null;

            // Wraps the remaining bytes into a special buffer channel
            ReadableBufferedChannel rbc = new ReadableBufferedChannel(this,
                    getByteBuffer(), getConnection()
                            .getReadableSelectionChannel());

            if (chunkedEncoding) {
                // Wrap the buffer channel to decode chunks
                inboundEntityChannel = new ReadableChunkedChannel(rbc);
            } else {
                // Wrap the buffer channel to control its announced size
                inboundEntityChannel = new ReadableSizedChannel(rbc,
                        contentLength);
            }

            setEntityRegistration(rbc.getRegistration());

            if (inboundEntityChannel != null) {
                result = new ReadableRepresentation(inboundEntityChannel, null,
                        contentLength) {
                    @Override
                    public void release() {
                        super.release();
                        onCompleted();
                    }
                };

                result.setSize(contentLength);
                setMessageState(MessageState.BODY);
            }
        } else {
            result = new EmptyRepresentation();
        }

        if (headers != null) {
            try {
                result = HeaderUtils.extractEntityHeaders(headers, result);
            } catch (Throwable t) {
                getLogger().log(Level.WARNING,
                        "Error while parsing entity headers", t);
            }
        }

        return result;
    }

    /**
     * Read the current message line (start line or header line).
     * 
     * @return True if the line is ready for reading.
     * @throws IOException
     */
    protected boolean fillLine() throws IOException {
        setLineBuilderState(NioUtils.fillLine(getLineBuilder(),
                getLineBuilderState(), getByteBuffer()));
        return getLineBuilderState() == BufferState.DRAINING;
    }

    /**
     * Returns the line builder index.
     * 
     * @return The line builder index.
     */
    protected int getBuilderIndex() {
        return builderIndex;
    }

    /**
     * Returns the NIO selection registration of the entity.
     * 
     * @return The NIO selection registration of the entity.
     */
    protected SelectionRegistration getEntityRegistration() {
        return entityRegistration;
    }

    @Override
    protected int getSocketInterestOps() {
        int result = 0;

        if ((getMessageState() == MessageState.BODY)
                && (getIoState() == IoState.IDLE)
                && (getEntityRegistration() != null)
                && (getEntityRegistration().getListener() != null)) {
            result = getEntityRegistration().getInterestOperations();
        } else if (getIoState() == IoState.INTEREST) {
            result = SelectionKey.OP_READ;
        }

        return result;
    }

    @Override
    protected boolean isProcessing() {
        return super.isProcessing() && (getMessageState() != MessageState.BODY);
    }

    @Override
    public void onCompleted() {
        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().finer("Inbound message fully received");
        }

        // Check if the server wants to close the connection
        if (HeaderUtils.isConnectionClose(getHeaders())) {
            getConnection().setState(ConnectionState.CLOSING);
        }

        if (getHeaders() != null) {
            getHeaders().clear();
        }

        super.onCompleted();
    }

    /**
     * Callback invoked when a message has been received. Note that one the
     * start line and the headers must have been received, not the optional
     * body.
     */
    protected void onReceived() {
        if (getLogger().isLoggable(Level.FINER)) {
            getLogger()
                    .finer("Inbound message start line and headers received");
        }
    }

    @Override
    public void onSelected(SelectionRegistration registration) {
        try {
            super.onSelected(registration);

            if ((getMessageState() == MessageState.BODY)
                    && (getEntityRegistration() != null)) {
                getEntityRegistration().onSelected(
                        registration.getReadyOperations());
            } else {
                while (isProcessing()) {
                    int result = readSocketBytes();

                    if (result == 0) {
                        // Socket channel exhausted
                        setIoState(IoState.INTEREST);
                    } else if (result == -1) {
                        // End of channel reached
                        setIoState(IoState.CANCELING);
                    } else {
                        while (isProcessing() && getByteBuffer().hasRemaining()) {
                            // Bytes are available in the buffer
                            // attempt to parse the next message
                            readMessage();
                        }
                    }
                }
            }
        } catch (Exception e) {
            getConnection().onError(
                    "Error while reading a message. Closing the connection.",
                    e, Status.CONNECTOR_ERROR_COMMUNICATION);
        }
    }

    /**
     * Read a message header.
     * 
     * @return The new message header or null.
     * @throws IOException
     */
    protected Parameter readHeader() throws IOException {
        Parameter header = HeaderReader.readHeader(getLineBuilder());
        clearLineBuilder();
        return header;
    }

    /**
     * Reads the next message if possible.
     * 
     * @throws IOException
     */
    protected void readMessage() throws IOException {
        while (isProcessing() && fillLine()) {
            // Parse next ready lines
            if (getMessageState() == MessageState.START) {
                readStartLine();
            } else if (getMessageState() == MessageState.HEADERS) {
                Parameter header = readHeader();

                if (header != null) {
                    if (getHeaders() == null) {
                        setHeaders(new Form());
                    }

                    getHeaders().add(header);
                } else {
                    // All headers received
                    onReceived();

                    if (getMessageState() == MessageState.IDLE) {
                        // Message fully received, check if another is ready
                        updateState();
                        super.onSelected(getRegistration());
                    }
                }
            }
        }
    }

    /**
     * Reads available bytes from the socket channel and fill the way's buffer.
     * 
     * @return The number of bytes read.
     * @throws IOException
     */
    protected int readSocketBytes() throws IOException {
        int result = getConnection().getReadableSelectionChannel().read(
                getByteBuffer());
        getByteBuffer().flip();
        return result;
    }

    /**
     * Read the start line of the current message received.
     * 
     * @throws IOException
     */
    protected abstract void readStartLine() throws IOException;

    /**
     * Sets the line builder index.
     * 
     * @param builderIndex
     *            The line builder index.
     */
    protected void setBuilderIndex(int builderIndex) {
        this.builderIndex = builderIndex;
    }

    /**
     * Sets the NIO selection registration of the entity.
     * 
     * @param entityRegistration
     *            The NIO selection registration of the entity.
     */
    protected void setEntityRegistration(
            SelectionRegistration entityRegistration) {
        this.entityRegistration = entityRegistration;
    }

}
