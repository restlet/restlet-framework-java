/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.util;

import java.io.IOException;
import java.io.OutputStream;

import com.noelios.restlet.http.HttpUtils;

/**
 * OutputStream to write data in the HTTP chunked encoding format to a
 * destination OutputStream.<br>
 * <br>
 * See section 3.6.1 of HTTP Protocol for more information on chunked encoding.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html">HTTP/1.1
 *      Protocol</a>
 */
public class ChunkedOutputStream extends OutputStream {

    private static final int DEFAULT_CHUNK_SIZE = 2048;

    /** The destination output stream. */
    private final OutputStream destination;

    /** The byte buffer. */
    private final byte[] buffer;

    /** The number of bytes written. */
    private int bytesWritten;

    /** Indicate if the stream is closed. */
    private boolean closed;

    /**
     * @param destination
     *                Outputstream to write chunked data to
     * @param chunkSize
     *                Chunk size
     */
    public ChunkedOutputStream(OutputStream destination, int chunkSize) {
        this.destination = destination;
        this.buffer = new byte[chunkSize];
        this.bytesWritten = 0;
        this.closed = false;
    }

    /**
     * Convenience constructor to use a default chunk size size of 2048.
     * 
     * @param destination
     * @see {@link #ChunkedOutputStream(OutputStream, int)}
     */
    public ChunkedOutputStream(OutputStream destination) {
        this(destination, DEFAULT_CHUNK_SIZE);
    }

    @Override
    public void write(int b) throws IOException {
        if (chunkFull()) {
            writeChunk();
        }
        buffer[bytesWritten++] = (byte) b;
    }

    /**
     * Closes this output stream for writing but does not close the wrapped.
     * stream
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            writeChunk();
            writeFinalChunk();
            super.close();
            closed = true;
            destination.flush();
        }
    }

    /**
     * Writes the current chunk and flushes the wrapped stream.
     */
    @Override
    public void flush() throws IOException {
        writeChunk();
        destination.flush();
    }

    /**
     * Write a chunk.
     * 
     * @throws IOException
     */
    private void writeChunk() throws IOException {
        if (bytesWritten > 0) {
            writePosition();
            writeBuffer();
            reset();
        }
    }

    /**
     * Write the closing chunk: A zero followed by crlf and another crlf.
     * 
     * @throws IOException
     */
    private void writeFinalChunk() throws IOException {
        destination.write((byte) '0');
        HttpUtils.writeCRLF(destination);
        HttpUtils.writeCRLF(destination);
    }

    /**
     * Write the current position in hexadecimal format followed by CRLF.
     * 
     * @throws IOException
     */
    private void writePosition() throws IOException {
        destination.write(Integer.toHexString(bytesWritten).getBytes());
        HttpUtils.writeCRLF(destination);
    }

    /**
     * Write the buffer contents.
     * 
     * @throws IOException
     */
    private void writeBuffer() throws IOException {
        destination.write(buffer, 0, bytesWritten);
        HttpUtils.writeCRLF(destination);
    }

    /**
     * Reset the internal buffer.
     */
    private void reset() {
        bytesWritten = 0;
    }

    /**
     * @return True if the current chunk is full.
     */
    private boolean chunkFull() {
        return bytesWritten == buffer.length;
    }
}
