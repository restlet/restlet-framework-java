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

package org.restlet.engine.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * Readable byte channel wrapping an input stream.
 * 
 * @author Jerome Louvel
 */
public class InputStreamChannel implements ReadableByteChannel,
        BlockableChannel {

    /** The underlying input stream. */
    private final InputStream inputStream;

    /** Indicates if the channel is blocking. */
    private final boolean blocking;

    /** Optional byte array buffer. */
    private volatile byte buffer[] = new byte[0];

    /** Indicates if the underlying stream is still open. */
    private volatile boolean open;

    /**
     * Constructor.
     * 
     * @param inputStream
     * @throws IOException
     */
    public InputStreamChannel(InputStream inputStream) throws IOException {
        this.inputStream = inputStream;
        this.open = true;
        this.blocking = (inputStream.available() <= 0);
    }

    /**
     * Closes the underlying input stream.
     */
    public void close() throws IOException {
        getInputStream().close();
        this.open = false;
    }

    /**
     * Returns the underlying input stream.
     * 
     * @return The underlying input stream.
     */
    protected InputStream getInputStream() {
        return inputStream;
    }

    /**
     * True if the underlying input stream is able to indicate available bytes
     * upfront.
     * 
     * @return True if the channel is blocking.
     */
    public boolean isBlocking() {
        return this.blocking;
    }

    /**
     * Indicates if the channel and its underlying stream are open.
     * 
     * @return True if the channel and its underlying stream are open.
     */
    public boolean isOpen() {
        return this.open;
    }

    /**
     * Reads bytes from the underlying stream to the target buffer.
     * 
     * @param target
     *            The target byte buffer.
     * @return The number of bytes read.
     */
    public int read(ByteBuffer target) throws IOException {
        int readLength = 0;

        if (isBlocking()) {
            // Potentially blocking read
            readLength = IoUtils.BUFFER_SIZE;
        } else {
            int available = getInputStream().available();

            if (available > 0) {
                // Attempt to read only the available byte to prevent blocking
                readLength = Math.min(available, target.remaining());
            } else {
                // Attempt to read as many bytes as possible even if blocking
                // occurs
                readLength = target.remaining();
            }
        }

        // Create or reuse a specific byte array as buffer
        return read(target, readLength);
    }

    /**
     * Reads a given number of bytes into a target byte buffer.
     * 
     * @param target
     *            The target byte buffer.
     * @param readLength
     *            The maximum number of bytes to read.
     * @return The number of bytes effectively read or -1 if end reached.
     * @throws IOException
     */
    private int read(ByteBuffer target, int readLength) throws IOException {
        int result = 0;

        if (target.hasArray()) {
            // Use directly the underlying byte array
            byte[] byteArray = target.array();

            result = getInputStream().read(byteArray, target.position(),
                    Math.min(readLength, target.remaining()));

            if (result > 0) {
                target.position(target.position() + result);
            }
        } else {
            if (this.buffer.length < IoUtils.BUFFER_SIZE) {
                this.buffer = new byte[IoUtils.BUFFER_SIZE];
            }

            result = getInputStream().read(
                    this.buffer,
                    0,
                    Math.min(Math.min(readLength, IoUtils.BUFFER_SIZE),
                            target.remaining()));

            if (result > 0) {
                target.put(buffer, 0, result);
            }
        }

        return result;
    }

}
