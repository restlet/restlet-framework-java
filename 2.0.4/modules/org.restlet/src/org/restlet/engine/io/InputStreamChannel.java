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
    private InputStream inputStream;

    /** Indicates if the channel is blocking. */
    private boolean blocking;

    /** Optional byte array buffer. */
    private byte buffer[] = new byte[0];

    /** Indicates if the underlying stream is still open. */
    private volatile boolean open;

    /**
     * Constructor.
     * 
     * @param inputStream
     * @throws IOException
     */
    public InputStreamChannel(InputStream inputStream) {
        this.inputStream = inputStream;
        this.open = true;

        try {
            this.blocking = (inputStream.available() <= 0);
        } catch (IOException ioe) {
            this.blocking = true;
        }
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
        int result = 0;
        int available = getInputStream().available();

        if (!isBlocking()) {
            if (available > 0) {
                if (target.hasArray()) {
                    // Use directly the underlying byte array
                    byte[] byteArray = target.array();

                    result = getInputStream().read(byteArray,
                            target.position(),
                            Math.min(available, target.remaining()));

                    if (result > 0) {
                        target.position(target.position() + result);
                    } else if (result == -1) {
                        target.position(target.position() + available);
                    }
                } else {
                    // Create or reuse a specific byte array as buffer
                    result = read(target, available);
                }
            } else {
                result = -1;
            }
        } else {
            // Potentially blocking read
            result = read(target, IoUtils.BUFFER_SIZE);
        }

        return result;
    }

    /**
     * Reads a given number of bytes into a target byte buffer.
     * 
     * @param target
     *            The target byte buffer.
     * @param bytesToRead
     *            The number of bytes to read.
     * @return The number of bytes effectively read or -1 if end reached.
     * @throws IOException
     */
    private int read(ByteBuffer target, int bytesToRead) throws IOException {
        int result = 0;

        if (this.buffer.length < IoUtils.BUFFER_SIZE) {
            this.buffer = new byte[IoUtils.BUFFER_SIZE];
        }

        result = getInputStream().read(
                this.buffer,
                0,
                Math.min(Math.min(bytesToRead, IoUtils.BUFFER_SIZE), target
                        .remaining()));

        if (result > 0) {
            target.put(buffer, 0, result);
        }

        return result;
    }

}
