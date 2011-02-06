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

package org.restlet.engine.io;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

// [excludes gwt]
/**
 * Input stream connected to a non-blocking readable channel.
 * 
 * @author Jerome Louvel
 */
public class NbChannelInputStream extends InputStream {

    /** The internal byte buffer. */
    private final ByteBuffer bb;

    /** The channel to read from. */
    private final ReadableByteChannel channel;

    /** Indicates if further reads can be attempted. */
    private volatile boolean endReached;

    /** The selectable channel to read from. */
    private final SelectableChannel selectableChannel;

    /**
     * Constructor.
     * 
     * @param channel
     *            The channel to read from.
     */
    public NbChannelInputStream(ReadableByteChannel channel) {
        this.channel = channel;

        if (channel instanceof SelectableChannel) {
            this.selectableChannel = (SelectableChannel) channel;
        } else {
            this.selectableChannel = null;
        }

        this.bb = ByteBuffer.allocate(IoUtils.BUFFER_SIZE);
        this.bb.flip();
        this.endReached = false;
    }

    @Override
    public int read() throws IOException {
        int result = -1;

        if (!this.endReached) {
            if (!this.bb.hasRemaining()) {
                // Let's refill
                refill();
            }

            if (!this.endReached) {
                // Let's return the next one
                result = this.bb.get() & 0xff;
            }
        }

        return result;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int result = -1;

        if (!this.endReached) {
            if (!this.bb.hasRemaining()) {
                // Let's try to refill
                refill();
            }

            if (!this.endReached) {
                // Let's return the next ones
                result = Math.min(len, this.bb.remaining());
                this.bb.get(b, off, result);
            }
        }

        return result;
    }

    /**
     * Reads the available bytes from the channel into the byte buffer.
     * 
     * @return The number of bytes read or -1 if the end of channel has been
     *         reached.
     * @throws IOException
     */
    private int readChannel() throws IOException {
        int result = 0;
        this.bb.clear();
        result = this.channel.read(this.bb);
        this.bb.flip();
        return result;
    }

    /**
     * Refill the byte buffer by attempting to read the channel.
     * 
     * @throws IOException
     */
    private void refill() throws IOException {
        // No, let's try to read more
        Selector selector = null;
        SelectionKey selectionKey = null;

        try {
            int bytesRead = readChannel();

            // If no bytes were read, try to register a select key to
            // get more
            if ((bytesRead == 0) && (selectableChannel != null)) {
                selector = SelectorFactory.getSelector();

                if (selector != null) {
                    selectionKey = this.selectableChannel.register(selector,
                            SelectionKey.OP_READ);
                    selector.select(NioUtils.NIO_TIMEOUT);
                }

                bytesRead = readChannel();
            } else if (bytesRead == -1) {
                this.endReached = true;
            }
        } finally {
            NioUtils.release(selector, selectionKey);
        }
    }
}