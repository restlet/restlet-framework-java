/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Lets blocking writers wait until a channel becomes writable again, instead of polling. Install
 * {@link #HANDLER} in the pipeline; it signals waiters on writability changes and on channel close.
 *
 * <p>This class is internal to the Netty connectors.
 */
final class Writability {

    private static final AttributeKey<Writability> KEY =
            AttributeKey.valueOf(Writability.class, "writability");

    /** The shared handler signalling writability changes. */
    public static final ChannelDuplexHandler HANDLER = new SignalHandler();

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition changed = lock.newCondition();

    /**
     * Returns the writability tracker of a channel, creating it if needed.
     *
     * @param channel The channel.
     * @return The writability tracker.
     */
    public static Writability of(Channel channel) {
        Writability result = channel.attr(KEY).get();
        if (result == null) {
            Writability created = new Writability();
            result = channel.attr(KEY).setIfAbsent(created);
            if (result == null) {
                result = created;
            }
        }
        return result;
    }

    /**
     * Blocks until the channel is writable.
     *
     * @param channel The channel.
     * @param timeoutMs The maximum time to wait, 0 for no limit.
     * @throws IOException If the channel closes, the wait times out or is interrupted.
     */
    public static void await(Channel channel, long timeoutMs) throws IOException {
        if (channel.isWritable()) {
            return;
        }
        channel.flush();
        of(channel).awaitWritable(channel, timeoutMs);
    }

    private void awaitWritable(Channel channel, long timeoutMs) throws IOException {
        long remaining =
                (timeoutMs > 0) ? TimeUnit.MILLISECONDS.toNanos(timeoutMs) : Long.MAX_VALUE;
        lock.lock();
        try {
            while (!channel.isWritable()) {
                if (!channel.isActive()) {
                    throw new IOException("Connection closed by peer");
                }
                if (remaining <= 0) {
                    throw new InterruptedIOException("Write timed out");
                }
                // Wake up periodically in case a signal raced with the check above.
                long slice = Math.min(remaining, TimeUnit.MILLISECONDS.toNanos(100));
                long left = changed.awaitNanos(slice);
                if (timeoutMs > 0) {
                    remaining -= (slice - Math.max(0, left));
                }
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted while writing");
        } finally {
            lock.unlock();
        }
    }

    private void signal() {
        lock.lock();
        try {
            changed.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Sharable
    private static final class SignalHandler extends ChannelDuplexHandler {
        @Override
        public void channelWritabilityChanged(ChannelHandlerContext ctx) throws Exception {
            if (ctx.channel().isWritable()) {
                of(ctx.channel()).signal();
            }
            super.channelWritabilityChanged(ctx);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            of(ctx.channel()).signal();
            super.channelInactive(ctx);
        }
    }
}
