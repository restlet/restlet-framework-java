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
import java.nio.ByteBuffer;

import org.restlet.engine.io.BufferState;
import org.restlet.engine.io.NioUtils;
import org.restlet.engine.io.ReadableSelectionChannel;

// [excludes gwt]
/**
 * Readable byte channel capable of decoding chunked entities.
 */
public class ReadableChunkedChannel extends ReadableWayChannel {

    /** The available chunk size that should be read from the source channel. */
    private volatile long availableChunkSize;

    /** The line builder to parse chunk size or trailer. */
    private final StringBuilder lineBuilder;

    /** The line builder state. */
    private volatile BufferState lineBuilderState;

    /** The chunk state. */
    private volatile int chunkState;

    /** Reading the chunk size line. */
    private static final int STATE_CHUNK_SIZE = 1;

    /** Reading the chunk data. */
    private static final int STATE_CHUNK_DATA = 2;

    /** Reading the chunk trailer. */
    private static final int STATE_CHUNK_TRAILER = 3;

    /** Reading the chunk end. */
    private static final int STATE_CHUNK_END = 4;

    /**
     * Constructor.
     * 
     * @param inboundWay
     *            The parent inbound way.
     * @param remainingBuffer
     *            The byte buffer remaining from previous read processing.
     * @param source
     *            The source channel.
     * @param availableChunkSize
     *            The total available size that can be read from the source
     *            channel.
     */
    public ReadableChunkedChannel(InboundWay inboundWay,
            ByteBuffer remainingBuffer, ReadableSelectionChannel source) {
        super(inboundWay, remainingBuffer, source);
        this.lineBuilder = new StringBuilder();
        this.lineBuilderState = BufferState.IDLE;
        this.chunkState = STATE_CHUNK_SIZE;
    }

    /**
     * Clears the line builder and adjust its state.
     */
    protected void clearLineBuilder() {
        getLineBuilder().delete(0, getLineBuilder().length());
        setLineBuilderState(BufferState.IDLE);
    }

    /**
     * Read the current message line (start line or header line).
     * 
     * @return True if the message line was fully read.
     * @throws IOException
     */
    protected boolean fillLine() throws IOException {
        boolean result = false;

        synchronized (getByteBuffer()) {
            int size = 0;

            if (getBufferState() == BufferState.DRAINING) {
                size = getByteBuffer().remaining();

                if (size == 0) {
                    setBufferState(BufferState.FILLING);
                    getByteBuffer().clear();
                }
            }

            if (getBufferState() == BufferState.FILLING) {
                // Try to refill the remaining buffer to read line
                size = getWrappedChannel().read(getByteBuffer());

                if (size > 0) {
                    setBufferState(BufferState.DRAINING);
                    getByteBuffer().flip();
                }
            }

            if (size > 0) {
                setLineBuilderState(NioUtils.fillLine(getLineBuilder(),
                        getLineBuilderState(), getByteBuffer()));
                return getLineBuilderState() == BufferState.DRAINING;
            }
        }

        return result;
    }

    /**
     * Returns the line builder to parse chunk size or trailer.
     * 
     * @return The line builder to parse chunk size or trailer.
     */
    protected StringBuilder getLineBuilder() {
        return lineBuilder;
    }

    /**
     * Returns the line builder state.
     * 
     * @return The line builder state.
     */
    protected BufferState getLineBuilderState() {
        return lineBuilderState;
    }

    /**
     * Reads some bytes and put them into the destination buffer. The bytes come
     * from the underlying channel.
     * 
     * @param dst
     *            The destination buffer.
     * @return The number of bytes read, or -1 if the end of the channel has
     *         been reached.
     */
    public int read(ByteBuffer dst) throws IOException {
        int result = 0;
        boolean tryAgain = true;

        while (tryAgain) {
            switch (this.chunkState) {

            case STATE_CHUNK_SIZE:
                if (fillLine()) {
                    System.out.print("### New chunk detected. Size: ");

                    // The chunk size line was fully read into the line builder
                    int length = getLineBuilder().length();

                    if (length == 0) {
                        throw new IOException(
                                "An empty chunk size line was detected");
                    }

                    int index = (getLineBuilder().indexOf(";"));
                    index = (index == -1) ? getLineBuilder().length() : index;

                    try {
                        this.availableChunkSize = Long
                                .parseLong(getLineBuilder().substring(0, index)
                                        .trim(), 16);

                        System.out.println(this.availableChunkSize);
                    } catch (NumberFormatException ex) {
                        throw new IOException("\"" + getLineBuilder()
                                + "\" has an invalid chunk size");
                    }

                    if (this.availableChunkSize == 0) {
                        this.chunkState = STATE_CHUNK_TRAILER;
                    } else {
                        this.chunkState = STATE_CHUNK_DATA;
                    }
                } else {
                    tryAgain = false;
                }
                break;

            case STATE_CHUNK_DATA:
                if (this.availableChunkSize > 0) {
                    if (this.availableChunkSize < dst.remaining()) {
                        dst.limit((int) (this.availableChunkSize + dst
                                .position()));
                    }

                    result = super.read(dst);
                    tryAgain = false;

                    if (result > 0) {
                        this.availableChunkSize -= result;
                    }
                }

                if (this.availableChunkSize == 0) {
                    // Try to read the end of line
                    if (fillLine()) {
                        // Done, reading the next chunk
                        this.chunkState = STATE_CHUNK_SIZE;
                        this.lineBuilder.delete(0, this.lineBuilder.length());
                    } else {
                        tryAgain = false;
                    }
                }

                break;

            case STATE_CHUNK_TRAILER:
                // TODO
                this.chunkState = STATE_CHUNK_END;
                break;

            case STATE_CHUNK_END:
                // TODO
                result = -1;
                tryAgain = false;
                break;
            }

        }

        postRead(result);
        return result;
    }

    /**
     * Sets the line builder state.
     * 
     * @param lineBuilderState
     *            The line builder state.
     */
    protected void setLineBuilderState(BufferState lineBuilderState) {
        this.lineBuilderState = lineBuilderState;
    }
}
