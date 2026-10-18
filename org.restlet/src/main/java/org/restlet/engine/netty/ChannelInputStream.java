/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Blocking input stream fed by a Netty event loop. The event loop offers content buffers; a
 * blocking reader (typically a virtual thread running a Restlet call) consumes them.
 *
 * <p>Back-pressure: the channel is expected to run with auto-read disabled. {@link #offer(ByteBuf)}
 * tells the event loop whether to read more; when more than {@link #HIGH_WATER_MARK} bytes are
 * buffered, reading stops until the consumer drains below {@link #LOW_WATER_MARK}, at which point
 * this stream requests the next read itself.
 *
 * <p>This class is internal to the Netty connectors.
 */
final class ChannelInputStream extends InputStream {

    /** Buffered bytes above which channel reads are paused. */
    static final int HIGH_WATER_MARK = 256 * 1024;

    /** Buffered bytes below which channel reads are resumed. */
    static final int LOW_WATER_MARK = 64 * 1024;

    private final Channel channel;

    private final long readTimeoutMs;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition available = lock.newCondition();

    private final Deque<ByteBuf> buffers = new ArrayDeque<>();

    private long bufferedBytes;

    private boolean endOfStream;

    private boolean closed;

    private IOException failure;

    private boolean readPaused;

    /**
     * Constructor.
     *
     * @param channel The channel to pause and resume for back-pressure, may be null.
     * @param readTimeoutMs The maximum time to wait for data, 0 for no limit.
     */
    public ChannelInputStream(Channel channel, long readTimeoutMs) {
        this.channel = channel;
        this.readTimeoutMs = readTimeoutMs;
    }

    /**
     * Offers a content buffer. Called on the event loop. Ownership of the buffer is transferred.
     *
     * @param buf The content buffer.
     * @return True if the event loop should read more content now, false if reading must wait until
     *     the consumer catches up (this stream will then request the read).
     */
    public boolean offer(ByteBuf buf) {
        lock.lock();
        try {
            if (closed || endOfStream || !buf.isReadable()) {
                buf.release();
                return !closed;
            }
            buffers.addLast(buf);
            bufferedBytes += buf.readableBytes();
            available.signalAll();
            if (bufferedBytes > HIGH_WATER_MARK) {
                readPaused = true;
                return false;
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    /** Signals the end of the content. Called on the event loop. */
    public void end() {
        lock.lock();
        try {
            endOfStream = true;
            available.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Signals a failure, for example a closed connection. Called on the event loop.
     *
     * @param cause The failure cause.
     */
    public void fail(Throwable cause) {
        lock.lock();
        try {
            if (!endOfStream) {
                failure =
                        (cause instanceof IOException ioe)
                                ? ioe
                                : new IOException("Connection failure", cause);
                available.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    /**
     * Indicates whether the whole content has been received and consumed.
     *
     * @return True if the whole content has been received and consumed.
     */
    public boolean isFullyConsumed() {
        lock.lock();
        try {
            return endOfStream && buffers.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int read() throws IOException {
        byte[] one = new byte[1];
        int n = read(one, 0, 1);
        return (n == -1) ? -1 : (one[0] & 0xFF);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        if (len == 0) {
            return 0;
        }
        lock.lock();
        try {
            ByteBuf head = awaitData();
            if (head == null) {
                return -1;
            }
            int n = Math.min(len, head.readableBytes());
            head.readBytes(b, off, n);
            bufferedBytes -= n;
            if (!head.isReadable()) {
                buffers.removeFirst().release();
            }
            maybeResumeReads();
            return n;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int available() {
        lock.lock();
        try {
            return (int) Math.min(Integer.MAX_VALUE, bufferedBytes);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            if (!closed) {
                closed = true;
                releaseAll();
                maybeResumeReads();
                available.signalAll();
            }
        } finally {
            lock.unlock();
        }
    }

    /** Must be called with the lock held. Returns the head buffer, or null at end of stream. */
    private ByteBuf awaitData() throws IOException {
        long remainingNanos =
                (readTimeoutMs > 0) ? TimeUnit.MILLISECONDS.toNanos(readTimeoutMs) : Long.MAX_VALUE;
        while (buffers.isEmpty()) {
            if (isEndOfStream()) {
                return null;
            }
            remainingNanos = awaitSignal(remainingNanos);
        }
        return buffers.peekFirst();
    }

    /**
     * Must be called with the lock held, when no buffer is available. Checks whether no more data
     * will come.
     *
     * @return True at the end of the stream.
     * @throws IOException If the stream is closed or failed.
     */
    private boolean isEndOfStream() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
        if (failure != null) {
            throw failure;
        }
        return endOfStream;
    }

    /**
     * Must be called with the lock held. Waits for a signal of the event loop.
     *
     * @param remainingNanos The remaining time to wait, when a read timeout is set.
     * @return The new remaining time.
     * @throws InterruptedIOException If the read times out or is interrupted.
     */
    private long awaitSignal(long remainingNanos) throws InterruptedIOException {
        try {
            if (readTimeoutMs <= 0) {
                available.await();
                return remainingNanos;
            }
            if (remainingNanos <= 0) {
                throw new InterruptedIOException("Read timed out");
            }
            return available.awaitNanos(remainingNanos);
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted while reading");
        }
    }

    /** Must be called with the lock held. */
    private void maybeResumeReads() {
        if (readPaused && bufferedBytes < LOW_WATER_MARK && channel != null) {
            readPaused = false;
            channel.read();
        }
    }

    /** Must be called with the lock held. */
    private void releaseAll() {
        ByteBuf buf;
        while ((buf = buffers.pollFirst()) != null) {
            buf.release();
        }
        bufferedBytes = 0;
    }
}
