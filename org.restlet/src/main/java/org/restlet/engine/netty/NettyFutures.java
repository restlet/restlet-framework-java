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
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.util.concurrent.TimeUnit;

/** Blocking waits on Netty futures, for the calling (virtual) thread of a client call. */
final class NettyFutures {

    private NettyFutures() {}

    /**
     * Waits for a future and returns its result.
     *
     * @param future The future.
     * @param timeoutMs The timeout, 0 for none.
     * @param message The error message prefix.
     * @return The result.
     * @throws IOException If the future fails, times out or the wait is interrupted.
     */
    static <T> T await(Future<T> future, long timeoutMs, String message) throws IOException {
        try {
            boolean done =
                    (timeoutMs > 0)
                            ? future.await(timeoutMs, TimeUnit.MILLISECONDS)
                            : future.await().isDone();
            if (!done) {
                future.cancel(false);
                throw new ConnectException(message + ": timed out");
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException(message + ": interrupted");
        }
        if (!future.isSuccess()) {
            Throwable cause = future.cause();
            if (cause instanceof ConnectException ce) {
                throw ce;
            }
            ConnectException ce = new ConnectException(message + ": " + cause);
            ce.initCause(cause);
            throw ce;
        }
        return future.getNow();
    }

    /**
     * Waits for a channel future, closing the channel on failure.
     *
     * @param future The channel future.
     * @param timeoutMs The timeout, 0 for none.
     * @param message The error message prefix.
     * @return The channel.
     * @throws IOException If the future fails, times out or the wait is interrupted.
     */
    static Channel await(ChannelFuture future, long timeoutMs, String message) throws IOException {
        try {
            boolean done =
                    (timeoutMs > 0)
                            ? future.await(timeoutMs, TimeUnit.MILLISECONDS)
                            : future.await().isDone();
            if (!done) {
                future.cancel(false);
                future.channel().close();
                throw new ConnectException(message + ": timed out");
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            future.channel().close();
            throw new InterruptedIOException(message + ": interrupted");
        }
        if (!future.isSuccess()) {
            Throwable cause = future.cause();
            ConnectException ce = new ConnectException(message + ": " + cause);
            ce.initCause(cause);
            throw ce;
        }
        return future.channel();
    }
}
