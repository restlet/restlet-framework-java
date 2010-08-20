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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectionKey;
import java.util.logging.Level;

import org.restlet.data.Form;
import org.restlet.data.Parameter;
import org.restlet.engine.http.header.HeaderReader;
import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.ReadableRepresentation;
import org.restlet.representation.Representation;
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

    /**
     * Constructor.
     * 
     * @param connection
     *            The parent connection.
     */
    public InboundWay(Connection<?> connection) {
        super(connection);
    }

    /**
     * Returns the message entity if available.
     * 
     * @param headers
     *            The headers to use.
     * @return The inbound message if available.
     */
    public Representation createEntity(Series<Parameter> headers) {
        Representation result = null;
        long contentLength = HeaderUtils.getContentLength(headers);
        boolean chunkedEncoding = HeaderUtils.isChunkedEncoding(headers);

        // In some cases there is an entity without a content-length header
        boolean connectionClose = HeaderUtils.isConnectionClose(headers);

        // Create the representation
        if ((contentLength != Representation.UNKNOWN_SIZE && contentLength != 0)
                || chunkedEncoding || connectionClose) {
            ReadableByteChannel inboundEntityChannel = getEntityChannel(
                    contentLength, chunkedEncoding);

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
     * @return True if the message line was fully read.
     * @throws IOException
     */
    protected boolean fillLine() throws IOException {
        boolean result = false;
        int next;

        while (!result && getByteBuffer().hasRemaining()) {
            next = (int) getByteBuffer().get();

            if (HeaderUtils.isCarriageReturn(next)) {
                next = (int) getByteBuffer().get();

                if (HeaderUtils.isLineFeed(next)) {
                    result = true;
                } else {
                    throw new IOException(
                            "Missing carriage return character at the end of HTTP line");
                }
            } else if (next == -1) {
                setMessageState(MessageState.IDLE);
            } else {
                getLineBuilder().append((char) next);
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
     * Returns the inbound message entity channel if it exists.
     * 
     * @param size
     *            The expected entity size or -1 if unknown.
     * 
     * @return The inbound message entity channel if it exists.
     */
    protected ReadableByteChannel getEntityChannel(long size, boolean chunked) {
        ReadableByteChannel result = null;

        if (getByteBuffer().hasRemaining()) {
            if (chunked) {
                getLogger()
                        .warning(
                                "Chunked encoding not supported (yet) in the NIO connector.");
            } else {
                // Wraps the remaining bytes into a special entity channel
                result = new ReadableEntityChannel(getByteBuffer(),
                        getConnection().getReadableSelectionChannel(), size);
            }
        } else {
            result = getConnection().getReadableSelectionChannel();
        }

        return result;
    }

    @Override
    protected int getSocketInterestOps() {
        int result = 0;

        if (getIoState() == IoState.INTEREST) {
            result = SelectionKey.OP_READ;
        }

        return result;
    }

    @Override
    protected void onCompleted() {
        if (getLogger().isLoggable(Level.FINER)) {
            getLogger().finer("Inbound message received");
        }

        super.onCompleted();
    }

    /**
     * Callback invoked when a message has been received. Note that one the
     * start line and the headers must have been received, not the optional
     * body.
     */
    protected abstract void onReceived();

    @Override
    public void onSelected() {
        super.onSelected();

        try {
            while ((getIoState() == IoState.PROCESSING)
                    && (getMessageState() != MessageState.BODY)) {
                if (isFilling()) {
                    int result = readSocketBytes();

                    if (result == 0) {
                        // Socket channel exhausted
                        setIoState(IoState.INTEREST);
                    } else if (result == -1) {
                        // End of channel reached
                        setIoState(IoState.CANCELING);
                    } else {
                        // Parse ready lines
                        while (fillLine()) {
                            if (getMessageState() == MessageState.START_LINE) {
                                readStartLine();
                            } else if (getMessageState() == MessageState.HEADERS) {
                                Parameter header = readHeader();

                                if (header != null) {
                                    if (getHeaders() == null) {
                                        setHeaders(new Form());
                                    }

                                    getHeaders().add(header);
                                } else {
                                    onReceived();
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            getLogger()
                    .log(Level.FINE,
                            "Error while reading a message. Closing the connection.",
                            e);
            getConnection().onError();
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
        getLineBuilder().delete(0, getLineBuilder().length());
        return header;
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

}
