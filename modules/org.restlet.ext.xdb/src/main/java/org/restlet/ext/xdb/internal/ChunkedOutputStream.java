/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.xdb.internal;

import java.io.IOException;
import java.io.OutputStream;

import org.restlet.engine.header.HeaderUtils;
import org.restlet.engine.util.StringUtils;

/**
 * OutputStream to write data in the HTTP chunked encoding format to a
 * destination OutputStream. See section 3.6.1 of HTTP Protocol for more
 * information on chunked encoding.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html">HTTP/1.1
 *      Protocol</a>
 * @deprecated Not actively developed anymore.
 */
@Deprecated
public class ChunkedOutputStream extends OutputStream {

    /** Default size of a chunk. */
    private static final int DEFAULT_CHUNK_SIZE = 2048;

    /** The byte buffer. */
    private final byte[] buffer;

    /** The number of bytes written. */
    private volatile int bytesWritten;

    /** Indicate if the stream is closed. */
    private volatile boolean closed;

    /** The destination output stream. */
    private final OutputStream destination;

    /**
     * Convenience constructor to use a default chunk size size of 2048.
     * 
     * @param destination
     * @see #ChunkedOutputStream(OutputStream, int)
     */
    public ChunkedOutputStream(OutputStream destination) {
        this(destination, DEFAULT_CHUNK_SIZE);
    }

    /**
     * @param destination
     *            Outputstream to write chunked data to
     * @param chunkSize
     *            Chunk size
     */
    public ChunkedOutputStream(OutputStream destination, int chunkSize) {
        this.destination = destination;
        this.buffer = new byte[chunkSize];
        this.bytesWritten = 0;
        this.closed = false;
    }

    /**
     * @return True if the current chunk is full.
     */
    private boolean chunkFull() {
        return this.bytesWritten == this.buffer.length;
    }

    /**
     * Closes this output stream for writing but does not close the wrapped
     * stream.
     */
    @Override
    public void close() throws IOException {
        if (!this.closed) {
            writeChunk();
            writeFinalChunk();
            super.close();
            this.closed = true;
            this.destination.flush();
        }
    }

    /**
     * Writes the current chunk and flushes the wrapped stream.
     */
    @Override
    public void flush() throws IOException {
        writeChunk();
        this.destination.flush();
    }

    /**
     * Resets the internal buffer.
     */
    private void reset() {
        this.bytesWritten = 0;
    }

    @Override
    public void write(int b) throws IOException {
        if (chunkFull()) {
            writeChunk();
        }

        this.buffer[this.bytesWritten++] = (byte) b;
    }

    /**
     * Write a chunk, starting with its size in hexadecimal, followed by CRLF
     * and the actual content.
     * 
     * @throws IOException
     */
    private void writeChunk() throws IOException {
        if (this.bytesWritten > 0) {
            // Write the current position in hexadecimal format followed by CRLF
            this.destination.write(StringUtils.getAsciiBytes(Integer
                    .toHexString(this.bytesWritten)));
            HeaderUtils.writeCRLF(this.destination);

            // Write the chunk content
            this.destination.write(this.buffer, 0, this.bytesWritten);
            HeaderUtils.writeCRLF(this.destination);

            // Reset the position
            reset();
        }
    }

    /**
     * Write the closing chunk: A zero followed by two CRLF.
     * 
     * @throws IOException
     */
    private void writeFinalChunk() throws IOException {
        this.destination.write((byte) '0');
        HeaderUtils.writeCRLF(this.destination);
        HeaderUtils.writeCRLF(this.destination);
    }

}
