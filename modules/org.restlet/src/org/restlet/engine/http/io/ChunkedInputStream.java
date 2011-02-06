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

package org.restlet.engine.http.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;

/**
 * {@link InputStream} to wrap a source {@link InputStream} that has been
 * chunked. See section 3.6.1 of HTTP Protocol for more information on chunked
 * encoding.
 * 
 * @author <a href="mailto:kevin.a.conaway@gmail.com">Kevin Conaway</a>
 * @see <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html">HTTP/1.1
 *      Protocol</a>
 */
public class ChunkedInputStream extends InputEntityStream {

    /** Size of the push back buffer. */
    private static final int PUSHBBACK_BUFFER_SIZE = 2;

    /** Size of the current chunk. */
    private volatile long chunkSize;

    /** Indicates if the end of the source stream has been reached. */
    private volatile boolean endReached;

    /** Indicates if the chunked has been properly initialized. */
    private volatile boolean initialized;

    /** Indicates the position inside the current chunk. */
    private volatile long position;

    /** The source input stream to decode. */
    private final PushbackInputStream source;

    /**
     * Constructor.
     * 
     * @param notifiable
     *            The notifiable connection.
     * @param inboundStream
     *            The inbound stream.
     */
    public ChunkedInputStream(Notifiable notifiable, InputStream inboundStream) {
        super(notifiable, inboundStream);
        this.source = new PushbackInputStream(inboundStream,
                PUSHBBACK_BUFFER_SIZE);
        this.initialized = false;
        this.endReached = false;
        this.position = 0;
        this.chunkSize = 0;
    }

    /**
     * Indicates if the source stream can be read and prepare it if necessary.
     * 
     * @return True if the source stream can be read.
     * @throws IOException
     */
    private boolean canRead() throws IOException {
        boolean result = false;
        initialize();

        if (!this.endReached) {
            if (!chunkAvailable()) {
                initializeChunk();
            }

            result = !this.endReached;
        }

        return result;
    }

    /**
     * Checks if the source stream will return a CR+LF sequence next, without
     * actually reading it.
     * 
     * @throws IOException
     */
    private void checkCRLF() throws IOException {
        final int cr = this.source.read();
        final int lf = this.source.read();

        if ((cr != '\r') || (lf != '\n')) {
            this.source.unread(lf);
            this.source.unread(cr);
        }
    }

    /**
     * Indicates if a chunk is available or false if a new one needs to be
     * initialized.
     * 
     * @return True if a chunk is available or false if a new one needs to be
     *         initialized.
     */
    private boolean chunkAvailable() {
        return this.position < this.chunkSize;
    }

    /**
     * Closes this input stream but do not close the underlying stream.
     */
    @Override
    public void close() throws IOException {
        super.close();
        this.initialized = true;
        onEndReached();
    }

    /**
     * Initializes the stream by reading and discarding a CRLF (if present).
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
     * Initialize the next chunk in the stream.
     * 
     * @throws IOException
     */
    private void initializeChunk() throws IOException {
        this.chunkSize = readChunkSize();
        this.position = 0;

        if (this.chunkSize == 0) {
            onEndReached();

            // Read the new line after the optional (unsupported) trailer
            checkCRLF();
        }
    }

    @Override
    protected void onEndReached() {
        super.onEndReached();
        this.endReached = true;
    }

    @Override
    public int read() throws IOException {
        int result = -1;

        if (canRead()) {
            result = this.source.read();
            this.position++;
            if ((result == -1)) {
                onEndReached();
            }
        }

        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = -1;

        if (canRead()) {
            result = this.source.read(b, off,
                    Math.min(len, (int) (this.chunkSize - this.position)));
            this.position += result;

            if (len - result > 0) {
                int nextResult = read(b, off + result, len - result);

                if (nextResult > 0) {
                    result += nextResult;
                }
            }
            if (result == -1) {
                onEndReached();
            }
        }

        return result;
    }

    /**
     * Reads the chunk size from the current line.
     * 
     * @return The chunk size from the current line.
     * @throws IOException
     *             If the chunk size could not be read or was invalid.
     */
    private long readChunkSize() throws IOException {
        String line = readChunkSizeLine();
        final int index = line.indexOf(';');
        line = index == -1 ? line : line.substring(0, index);

        try {
            return Long.parseLong(line.trim(), 16);
        } catch (NumberFormatException ex) {
            throw new IOException("<" + line + "> is an invalid chunk size");
        }
    }

    /**
     * Reads a line containing a chunk size.
     * 
     * @return A line containing a chunk size.
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
                }
                throw new IOException(
                        "Invalid chunk size specified.  Expected crlf, only saw cr");
            }

            buffer.write(b);
        }

        return new String(buffer.toByteArray());
    }
}
