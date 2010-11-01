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

package org.restlet.engine.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

// [excludes gwt]
/**
 * Readable byte channel capable of encoding chunked entities.
 */
public class ReadableChunkingChannel extends
        WrapperChannel<ReadableByteChannel> implements ReadableByteChannel {

    /** The constant chunk part containing the size of the chunk data. */
    private final int chunkSizeLength;

    private boolean lastChunkWritten;

    /**
     * Constructor.
     * 
     * @param source
     *            The source channel.
     * @param remainingChunkSize
     *            The total available size that can be read from the source
     *            channel.
     */
    public ReadableChunkingChannel(ReadableByteChannel source, int maxBufferSize) {
        super(source);

        // Compute the constant chunk part containing the size of the chunk data
        this.chunkSizeLength = Integer.toHexString(maxBufferSize).length();
        this.lastChunkWritten = false;
    }

    /**
     * Returns an hexadecimal chunk size string with a constant length, adding
     * the necessary number of leading zeroes.
     * 
     * @param dst
     *            The destination buffer.
     * @param chunkDataSize
     *            The chunk data size value.
     * @return The length of the chunk size string.
     */
    private int putChunkSizeString(ByteBuffer dst, int chunkDataSize) {
        int result = 0;
        String chunkDataSizeString = Integer.toHexString(chunkDataSize);
        result = chunkDataSizeString.length();

        // Add necessary leading zeroes
        for (int i = chunkDataSizeString.length(); i < this.chunkSizeLength; i++) {
            dst.put((byte) 48);
            result++;
        }

        dst.put(chunkDataSizeString.getBytes());
        dst.put((byte) 13);
        dst.put((byte) 10);
        result += 2;
        return result;
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

        if (this.lastChunkWritten) {
            result = -1;
        } else {
            int chunkStart = dst.position();
            int maxChunkDataSize = dst.remaining() - this.chunkSizeLength - 4;
            int chunkDataSize = 0;

            if (maxChunkDataSize > 0) {
                // Read the chunk data in the buffer
                dst.position(chunkStart + this.chunkSizeLength + 2);
                dst.limit(dst.position() + maxChunkDataSize);
                chunkDataSize = getWrappedChannel().read(dst);
                dst.limit(dst.capacity());

                if (chunkDataSize == -1) {
                    this.lastChunkWritten = true;
                    dst.position(chunkStart);

                    // Rewind and put the chunk size in the buffer
                    result = putChunkSizeString(dst, 0);

                    // End chunked entity
                    dst.put((byte) 13);
                    dst.put((byte) 10);
                    result += 2;
                } else if (chunkDataSize > 0) {
                    dst.put((byte) 13);
                    dst.put((byte) 10);
                    dst.position(chunkStart);

                    // Put the chunk size line
                    putChunkSizeString(dst, chunkDataSize);

                    // Restore buffer state
                    dst.position(dst.position() + chunkDataSize + 2);
                    result = dst.position() - chunkStart;
                } else {
                    // Nothing read on the wrapped channel. Try again later.
                    dst.position(chunkStart);
                }
            } else {
                // Not enough space in the buffer to read a chunk. Try again
                // later.
            }
        }

        return result;
    }
}
