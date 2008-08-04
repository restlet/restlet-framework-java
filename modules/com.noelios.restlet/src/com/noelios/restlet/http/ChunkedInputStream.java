/*
 * Copyright 2005-2008 Noelios Technologies.
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

package com.noelios.restlet.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * {@link InputStream} to wrap a source {@link InputStream} that has been
 * chunked.<br>
 * <br>
 * See section 3.6.1 of HTTP Protocol for more information on chunked encoding.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html">HTTP/1.1
 *      Protocol< /a>
 */
public class ChunkedInputStream extends InputStream {

    private static final int PUSHBBACK_BUFFER_SIZE = 2;

    private volatile long chunkSize;

    private volatile boolean done;

    private volatile boolean initialized;

    private volatile long position;

    private final PushbackInputStream source;

    /**
     * Constructor.
     * 
     * @param source
     *            Source InputStream to read from
     */
    public ChunkedInputStream(InputStream source) {
        this.source = new PushbackInputStream(source, PUSHBBACK_BUFFER_SIZE);
        this.initialized = false;
        this.done = false;
        this.position = 0;
        this.chunkSize = 0;
    }

    private void checkCRLF() throws IOException {
        final int cr = this.source.read();
        final int lf = this.source.read();
        if ((cr != '\r') && (lf != '\n')) {
            this.source.unread(lf);
            this.source.unread(cr);
        }
    }

    /**
     * @return True if a chunk is available. False if a new one needs to be
     *         initialized
     */
    private boolean chunkAvailable() {
        return this.position < this.chunkSize;
    }

    /**
     * Close this input stream but do not close the underlying stream.
     */
    @Override
    public void close() throws IOException {
        super.close();
        this.initialized = true;
        this.done = true;
    }

    /**
     * Initialize the stream by reading and discarding a CRLF (if present)
     * 
     * @throws IOException
     */
    private void initialize() throws IOException {
        if (!this.initialized) {
            checkCRLF();
            this.initialized = true;
        }
    }

    /**
     * Initialize the next chunk in the stream
     * 
     * @throws IOException
     */
    private void initializeChunk() throws IOException {
        this.chunkSize = readChunkSize();
        this.position = 0;
        if (this.chunkSize == 0) {
            this.done = true;
        }
    }

    /**
     * Read a byte from the chunked stream
     */
    @Override
    public int read() throws IOException {
        int result = -1;

        initialize();

        if (!this.done) {
            if (chunkAvailable()) {
                result = this.source.read();
                this.position++;
            } else {
                initializeChunk();
                return read();
            }
        }

        return result;
    }

    /**
     * Read the chunk size from the current line
     * 
     * @return Chunk size
     * @throws IOException
     *             If the chunk size could not be read or was invalid
     */
    private long readChunkSize() throws IOException {
        String line = readChunkSizeLine();
        final int index = line.indexOf(';');
        line = index == -1 ? line : line.substring(0, index);

        try {
            return Long.parseLong(line.trim(), 16);
        } catch (final NumberFormatException ex) {
            throw new IOException("<" + line + "> is an invalid chunk size");
        }
    }

    /**
     * Read a line containing a chunk size
     * 
     * @return
     * @throws IOException
     */
    private String readChunkSizeLine() throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        checkCRLF();
        for (;;) {
            final int b = this.source.read();
            if (b == -1) {
                throw new IOException(
                        "Invalid chunk size specified. End of stream reached");
            }
            if (b == '\r') {
                final int lf = this.source.read();
                if (lf == '\n') {
                    break;
                } else {
                    throw new IOException(
                            "Invalid chunk size specified.  Expected crlf, only saw cr");
                }
            }
            buffer.write(b);
        }
        return new String(buffer.toByteArray());
    }
}
