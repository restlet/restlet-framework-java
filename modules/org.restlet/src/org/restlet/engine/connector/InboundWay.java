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
import java.nio.channels.SelectionKey;
import java.util.logging.Level;

import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderReader;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.engine.io.Buffer;
import org.restlet.engine.io.BufferState;
import org.restlet.engine.io.IoState;
import org.restlet.engine.io.ReadableChunkedChannel;
import org.restlet.engine.io.ReadableSelectionChannel;
import org.restlet.engine.io.ReadableSizedSelectionChannel;
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
     * @param bufferSize
     *            The byte buffer size.
     */
    public InboundWay(Connection<?> connection, int bufferSize) {
        super(connection, bufferSize);
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
    protected Representation createEntity(Series<Header> headers) {
        Representation result = null;
        long contentLength = HeaderUtils.getContentLength(headers);
        boolean chunkedEncoding = HeaderUtils.isChunkedEncoding(headers);

        // In some cases there is an entity without a content-length header
        boolean connectionClose = HeaderUtils.isConnectionClose(headers);

        // Create the representation
        if ((contentLength != Representation.UNKNOWN_SIZE && contentLength != 0)
                || chunkedEncoding || connectionClose) {
            ReadableSelectionChannel inboundEntityChannel = null;

            if (chunkedEncoding) {
                // Wraps the remaining bytes into a special buffer channel
                inboundEntityChannel = new ReadableChunkedChannel(this,
                        getBuffer(), getConnection()
                                .getReadableSelectionChannel());
            } else {
                // Wrap the buffer channel to control its announced size
                inboundEntityChannel = new ReadableSizedSelectionChannel(this,
                        getBuffer(), getConnection()
                                .getReadableSelectionChannel(), contentLength);
            }

            setEntityRegistration(inboundEntityChannel.getRegistration());

            if (inboundEntityChannel != null) {
                result = new ReadableRepresentation(inboundEntityChannel, null,
                        contentLength);
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
        boolean result = false;
        setLineBuilderState(getBuffer().drain(getLineBuilder(),
                getLineBuilderState()));

        if (getLineBuilderState() == BufferState.DRAINING) {
            result = true;

            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE, getLineBuilder().toString());
            }
        }

        return result;
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
    public int getInterestOperations() {
        int result = 0;

        if (getIoState() == IoState.INTEREST) {
            result = SelectionKey.OP_READ;
        }

        return result;
    }

    /**
     * Indicates if the next message line is readable.
     * 
     * @return True if the next message line is readable.
     * @throws IOException
     */
    protected boolean isLineReadable() throws IOException {
        return getBuffer().canDrain()
                && (getMessageState() != MessageState.IDLE)
                && (getMessageState() != MessageState.BODY) && fillLine();
    }

    @Override
    public void onCompleted(boolean endDetected) {
        super.onCompleted(endDetected);

        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().finer("Inbound message completed");
        }
    }

    @Override
    public int onDrain(Buffer buffer, int maxDrained, Object... args)
            throws IOException {
        int result = 0;

        // Bytes are available in the buffer
        // attempt to parse the next message
        boolean continueReading = true;
        int beforeDrain = buffer.remaining();

        while (continueReading && isLineReadable()) {
            // Parse next ready lines
            if (getMessageState() == MessageState.START) {
                if (getLineBuilder().length() == 0) {
                    // Silently eat empty lines used for keep alive purpose
                    // sometimes (SIP)
                    continueReading = false;
                } else {
                    if (getHelper().getLogger().isLoggable(Level.FINE)) {
                        getHelper().getLogger().fine(
                                "Reading message from "
                                        + getConnection().getSocketAddress());
                    }

                    readStartLine();
                }
            } else if (getMessageState() == MessageState.HEADERS) {
                Header header = readHeader();

                if (header != null) {
                    if (getHeaders() == null) {
                        setHeaders(new Series<Header>(Header.class));
                    }

                    getHeaders().add(header);
                } else {
                    // All headers received
                    onReceived();
                }
            }
        }

        result = beforeDrain - buffer.remaining();

        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().log(Level.FINER, result + " bytes read");
        }

        return result;
    }

    @Override
    public void onError(Status status) {
        getHelper().onInboundError(status, getMessage());
        setMessage(null);
    }

    @Override
    public int onFill(Buffer buffer, Object... args) throws IOException {
        int result = getBuffer().fill(
                getConnection().getReadableSelectionChannel());

        if (result == -1) {
            // End of channel detected
            getConnection().close(true);
        }

        return result;
    }

    /**
     * Called back when a fill operation returns with an EOF status.
     */
    public void onFillEof() {
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

    /**
     * Call back invoked when the message is received.
     * 
     * @param message
     *            The new message received.
     */
    protected abstract void onReceived(Response message);

    @Override
    public void onTimeOut() {
        if (getMessage() != null) {
            getHelper().onInboundError(Status.CONNECTOR_ERROR_COMMUNICATION,
                    getMessage());
        }
    }

    @Override
    public int processIoBuffer() throws IOException {
        int result = 0;

        if ((getMessageState() == MessageState.BODY)
                && (getEntityRegistration() != null)) {
            if (getLogger().isLoggable(Level.FINER)) {
                getLogger().log(
                        Level.FINER,
                        "Entity registration selected : "
                                + getRegistration().getClass());
            }

            if (getIoState() == IoState.READY) {
                if (getEntityRegistration().getListener() != null) {
                    getEntityRegistration().getListener().onSelected();
                }
            } else {
                getEntityRegistration().onSelected(
                        getRegistration().getReadyOperations());
            }
        } else {
            result = super.processIoBuffer();
        }

        return result;
    }

    /**
     * Read a message header.
     * 
     * @return The new message header or null.
     * @throws IOException
     */
    protected Header readHeader() throws IOException {
        Header header = HeaderReader.readHeader(getLineBuilder());
        clearLineBuilder();
        return header;
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

    @Override
    public void updateState() {
        if (getHelper().getLogger().isLoggable(Level.FINEST)) {
            getHelper().getLogger().log(Level.FINEST,
                    "Old inbound way NIO interest: " + getRegistration());
            getHelper().getLogger().log(
                    Level.FINEST,
                    "Old inbound entity NIO interest: "
                            + getEntityRegistration());
        }

        if (getMessageState() == MessageState.BODY) {
            if ((getEntityRegistration() != null)
                    && (getEntityRegistration().getListener() != null)) {
                getRegistration().setInterestOperations(
                        getEntityRegistration().getInterestOperations());
            } else {
                getRegistration().setInterestOperations(0);
            }
        } else {
            super.updateState();
        }

        if (getHelper().getLogger().isLoggable(Level.FINEST)) {
            getHelper().getLogger().log(
                    Level.FINEST,
                    "New inbound entity NIO interest: "
                            + getEntityRegistration());
            getHelper().getLogger().log(Level.FINEST,
                    "New inbound way NIO interest: " + getRegistration());
        }
    }
}
