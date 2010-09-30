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

import org.restlet.engine.http.header.HeaderUtils;
import org.restlet.engine.io.NioUtils;
import org.restlet.engine.io.ReadableSelectionChannel;

// [excludes gwt]
/**
 * Readable byte channel capable of decoding chunked entities.
 */
public class ReadableChunkedChannel extends ReadableWayChannel {

    /** The available chunk size that should be read from the source channel. */
    private volatile long chunkSize;

    /** The line builder to parse chunk size or trailer. */
    private final StringBuilder lineBuilder;

    /** The reading state. */
    private volatile int state;

    /** */
    private static final int STATE_CHUNK_SIZE = 1;

    /** */
    private static final int STATE_CHUNK_DATA = 2;

    /** */
    private static final int STATE_CHUNK_TRAILER = 3;

    /**
     * Constructor.
     * 
     * @param inboundWay
     *            The parent inbound way.
     * @param remainingBuffer
     *            The byte buffer remaining from previous read processing.
     * @param source
     *            The source channel.
     * @param chunkSize
     *            The total available size that can be read from the source
     *            channel.
     */
    public ReadableChunkedChannel(InboundWay inboundWay,
            ByteBuffer remainingBuffer, ReadableSelectionChannel source) {
        super(inboundWay, remainingBuffer, source);
        this.lineBuilder = new StringBuilder();
        this.state = STATE_CHUNK_SIZE;
    }

    /**
     * Read the current message line (start line or header line).
     * 
     * @return True if the message line was fully read.
     * @throws IOException
     */
    protected boolean fillLine() throws IOException {
        boolean result = false;
        int remaining = getRemainingBuffer().remaining();

        if (remaining == 0) {
            // Try to refill the remaining buffer to read line
            remaining = getWrappedChannel().read(getRemainingBuffer());
        }

        if (remaining > 0) {
            result = NioUtils.fillLine(getLineBuilder(), getRemainingBuffer());
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
     * Reads some bytes and put them into the destination buffer. The bytes come
     * from the underlying channel.
     * 
     * @param dst
     *            The destination buffer.
     * @return The number of bytes read, or -1 if the end of the channel has
     *         been reached.
     */
    public int read(ByteBuffer dst) throws IOException {
        int result = -1;

        switch (this.state) {
        case STATE_CHUNK_SIZE:
            if (fillLine()) {
                // The chunk size line was fully read into the line builder
                int length = getLineBuilder().length();

                if (length == 0) {
                    throw new IOException(
                            "An empty chunk size line was detected");
                }

                int index = (getLineBuilder().indexOf(";"));
                index = (index == -1) ? getLineBuilder().length() - 1 : index;

                try {
                    this.chunkSize = Long.parseLong(
                            getLineBuilder().substring(0, index).trim(), 16);
                } catch (NumberFormatException ex) {
                    throw new IOException("\"" + getLineBuilder()
                            + "\" has an invalid chunk size");
                }

                if (this.chunkSize == 0) {
                    result = -1;
                }
            }
            break;
        case STATE_CHUNK_DATA:
            if (this.chunkSize > 0) {
                if (this.chunkSize < dst.remaining()) {
                    dst.limit((int) (this.chunkSize + dst.position()));
                }

                result = super.read(dst);
            }
            break;
        case STATE_CHUNK_TRAILER:
            break;
        }

        if (result > 0) {
            this.chunkSize -= result;
        }

        postRead(result);
        return result;
    }
}
