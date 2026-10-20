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
import io.netty.channel.ChannelFuture;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * Blocking output stream writing to a Netty channel. Bytes are buffered locally and written as
 * content messages when the buffer is full or on {@link #flush()}.
 *
 * <p>Back-pressure: before each write, the calling thread waits until the channel is writable, so a
 * slow peer slows down the producer instead of filling the memory.
 *
 * <p>Calling {@link #flush()} sends the buffered bytes immediately, which is what server-sent
 * events rely on. A write to a closed channel fails with an {@link IOException}, so a producer
 * notices when the peer has gone.
 *
 * <p>This class is internal to the Netty connectors.
 */
final class ChannelOutputStream extends OutputStream {

    private final Channel channel;

    private final Function<ByteBuf, Object> contentFactory;

    private final int bufferSize;

    private final long writeTimeoutMs;

    private ByteBuf buffer;

    private boolean closed;

    /** Bytes written to the channel whose write has not completed yet. */
    private final AtomicLong inFlightBytes = new AtomicLong();

    private final ReentrantLock inFlightLock = new ReentrantLock();

    private final Condition inFlightChanged = inFlightLock.newCondition();

    /** The cause of the first failed write, if any. */
    private volatile Throwable writeFailure;

    /**
     * Constructor.
     *
     * @param channel The channel to write to.
     * @param contentFactory Wraps a buffer into the message type the pipeline expects (for example
     *     an HTTP content message).
     * @param bufferSize The local buffer size in bytes.
     * @param writeTimeoutMs The maximum time to wait for the channel to become writable, 0 for no
     *     limit.
     */
    public ChannelOutputStream(
            Channel channel,
            Function<ByteBuf, Object> contentFactory,
            int bufferSize,
            long writeTimeoutMs) {
        this.channel = channel;
        this.contentFactory = contentFactory;
        this.bufferSize = Math.max(512, bufferSize);
        this.writeTimeoutMs = writeTimeoutMs;
    }

    @Override
    public void write(int b) throws IOException {
        ensureOpen();
        ensureBuffer();
        buffer.writeByte(b);
        if (buffer.writableBytes() == 0) {
            send(false);
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            ensureOpen();
            ensureBuffer();
            int n = Math.min(len, buffer.writableBytes());
            buffer.writeBytes(b, off, n);
            off += n;
            len -= n;
            if (buffer.writableBytes() == 0) {
                send(false);
            }
        }
    }

    @Override
    public void flush() throws IOException {
        ensureOpen();
        if (buffer != null && buffer.isReadable()) {
            send(true);
        } else {
            channel.flush();
        }
    }

    /**
     * Writes the remaining buffered bytes without closing the underlying exchange. The caller is
     * responsible for writing the last content message.
     *
     * @throws IOException If the channel is closed.
     */
    @Override
    public void close() throws IOException {
        if (!closed) {
            try {
                if (buffer != null && buffer.isReadable()) {
                    send(true);
                }
            } finally {
                closed = true;
                if (buffer != null) {
                    buffer.release();
                    buffer = null;
                }
            }
        }
    }

    private void ensureBuffer() {
        if (buffer == null) {
            buffer = channel.alloc().buffer(bufferSize, bufferSize);
        }
    }

    private void ensureOpen() throws IOException {
        if (closed) {
            throw new IOException("Stream closed");
        }
        checkWriteFailure();
        if (!channel.isActive()) {
            throw new IOException("Connection closed by peer");
        }
    }

    private void checkWriteFailure() throws IOException {
        Throwable cause = writeFailure;
        if (cause != null) {
            throw new IOException("Unable to write to the connection", cause);
        }
    }

    private void send(boolean flush) throws IOException {
        Writability.await(channel, writeTimeoutMs);
        awaitInFlightBytes();
        ByteBuf chunk = buffer;
        buffer = null;
        int size = chunk.readableBytes();
        inFlightBytes.addAndGet(size);
        Object msg = contentFactory.apply(chunk);
        ChannelFuture future = flush ? channel.writeAndFlush(msg) : channel.write(msg);
        future.addListener(f -> onWriteDone(size, f.cause()));
        if (future.isDone() && !future.isSuccess()) {
            throw new IOException("Unable to write to the connection", future.cause());
        }
    }

    /** Called when a write completes, possibly on the event loop. */
    private void onWriteDone(int size, Throwable cause) {
        inFlightLock.lock();
        try {
            inFlightBytes.addAndGet(-size);
            if (cause != null && writeFailure == null) {
                writeFailure = cause;
            }
            inFlightChanged.signalAll();
        } finally {
            inFlightLock.unlock();
        }
    }

    /**
     * Waits until the bytes written but not yet sent fall below the channel high water mark. Writes
     * made outside the event loop only update the channel writability once the event loop has
     * processed them, so a fast producer could otherwise queue unbounded data, and miss the failure
     * of writes to a connection closed in the meantime.
     */
    private void awaitInFlightBytes() throws IOException {
        int limit = Math.max(bufferSize, channel.config().getWriteBufferHighWaterMark());
        if (inFlightBytes.get() <= limit || channel.eventLoop().inEventLoop()) {
            checkWriteFailure();
            return;
        }

        channel.flush();
        long remaining =
                (writeTimeoutMs > 0)
                        ? TimeUnit.MILLISECONDS.toNanos(writeTimeoutMs)
                        : Long.MAX_VALUE;
        inFlightLock.lock();
        try {
            while (inFlightBytes.get() > limit && writeFailure == null) {
                if (!channel.isActive()) {
                    throw new IOException("Connection closed by peer");
                }
                if (remaining <= 0) {
                    throw new InterruptedIOException("Write timed out");
                }
                // Wake up periodically in case the channel closes without failing a write.
                long slice = Math.min(remaining, TimeUnit.MILLISECONDS.toNanos(100));
                long left = inFlightChanged.awaitNanos(slice);
                if (writeTimeoutMs > 0) {
                    remaining -= (slice - Math.max(0, left));
                }
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted while writing");
        } finally {
            inFlightLock.unlock();
        }
        checkWriteFailure();
    }
}
