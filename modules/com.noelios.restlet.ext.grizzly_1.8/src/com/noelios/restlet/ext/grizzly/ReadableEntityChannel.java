package com.noelios.restlet.ext.grizzly;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;

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

        if ((this.remainingBuffer != null)
                && (this.remainingBuffer.hasRemaining())) {
            // First make sure that the remaining buffer is empty
            final byte[] src = new byte[this.remainingBuffer.remaining()];
            this.remainingBuffer.get(src);
            dst.put(src);
        } else {
            // Otherwise, read data from the source channel
            if (this.availableSize > 0) {
                result = ((ReadableByteChannel) getSource()).read(dst);

                if (result > 0) {
                    this.availableSize = this.availableSize - result;
                }
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
