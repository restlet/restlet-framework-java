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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

// [excludes gwt]
/**
 * Readable byte channel based on a source socket channel that must only be
 * partially read.
 */
public class ReadableEntityChannel extends SelectableChannel implements
        ReadableByteChannel {

    /** The byte buffer remaining from previous read processing. */
    private volatile ByteBuffer remainingBuffer;

    /** The source channel. */
    private volatile SelectableChannel source;

    /** The total size that should be read from the source channel. */
    private volatile long availableSize;

    /**
     * Constructor.
     * 
     * @param remainingBuffer
     *            The byte buffer remaining from previous read processing.
     * @param source
     *            The source channel.
     * @param availableSize
     *            The available size that can be read from the source channel.
     */
    public ReadableEntityChannel(ByteBuffer remainingBuffer,
            SelectableChannel source, long availableSize) {
        this.remainingBuffer = remainingBuffer;
        this.source = source;
        this.availableSize = availableSize;
    }

    @Override
    public Object blockingLock() {
        return getSource().blockingLock();
    }

    @Override
    public SelectableChannel configureBlocking(boolean block)
            throws IOException {
        return getSource().configureBlocking(block);
    }

    private SelectableChannel getSource() {
        return this.source;
    }

    @Override
    protected void implCloseChannel() throws IOException {
    }

    @Override
    public boolean isBlocking() {
        return getSource().isBlocking();
    }

    @Override
    public boolean isRegistered() {
        return getSource().isRegistered();
    }

    @Override
    public SelectionKey keyFor(Selector sel) {
        return getSource().keyFor(sel);
    }

    @Override
    public SelectorProvider provider() {
        return getSource().provider();
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

        if (this.availableSize > 0) {
            if ((this.remainingBuffer != null)
                    && (this.remainingBuffer.hasRemaining())) {
                // First make sure that the remaining buffer is empty
                result = Math.min(this.remainingBuffer.remaining(),
                        dst.remaining());
                byte[] src = new byte[result];
                this.remainingBuffer.get(src);
                dst.put(src);
            } else {
                // Otherwise, read data from the source channel
                result = ((ReadableByteChannel) getSource()).read(dst);
            }

            if (result > 0) {
                this.availableSize -= result;
            }
        }

        return result;
    }

    @Override
    public SelectionKey register(Selector sel, int ops, Object att)
            throws ClosedChannelException {
        return getSource().register(sel, ops, att);
    }

    @Override
    public int validOps() {
        return getSource().validOps();
    }

}
