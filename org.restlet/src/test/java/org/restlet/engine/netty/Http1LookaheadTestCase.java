/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.Test;
import org.restlet.data.Reference;
import org.restlet.engine.connector.HttpServerHelper;

class Http1LookaheadTestCase {

    @Test
    void acceptsExactlyOneMiBThenRejectsOverflowAndReleasesContent() throws Exception {
        EmbeddedChannel channel = waitingForResponse();
        var full = new DefaultHttpContent(Unpooled.buffer(1024 * 1024).writeZero(1024 * 1024));
        var overflow = new DefaultHttpContent(Unpooled.buffer(1).writeByte(1));
        try {
            channel.writeInbound(full);
            assertTrue(channel.isActive());
            assertEquals(1, full.refCnt());
            channel.writeInbound(overflow);
            assertFalse(channel.isActive());
            assertEquals(0, full.refCnt());
            assertEquals(0, overflow.refCnt());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void acceptsExactly1024MessagesThenRejectsOverflowAndReleasesContent() throws Exception {
        EmbeddedChannel channel = waitingForResponse();
        var content = new ArrayList<DefaultHttpContent>();
        try {
            for (int i = 0; i < 1024; i++) {
                var message = new DefaultHttpContent(Unpooled.buffer(1).writeByte(1));
                content.add(message);
                channel.writeInbound(message);
            }
            assertTrue(channel.isActive());
            var overflow = new DefaultHttpContent(Unpooled.buffer(1).writeByte(1));
            content.add(overflow);
            channel.writeInbound(overflow);
            assertFalse(channel.isActive());
            for (var message : content) {
                assertEquals(0, message.refCnt());
            }
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void releasesQueuedContentWhenTheHandlerIsReplacedForAnUpgrade() throws Exception {
        EmbeddedChannel channel = waitingForResponse();
        var content = new DefaultHttpContent(Unpooled.buffer(1).writeByte(1));
        try {
            channel.writeInbound(content);
            channel.pipeline().remove(NettyServerHandler.class);
            assertTrue(channel.isActive());
            assertEquals(0, content.refCnt());
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void publishesClientCompletionBeforeReleasingAnEmptyBodyConsumer() throws Exception {
        var trailers = new io.netty.handler.codec.http.DefaultHttpHeaders();
        new NettyClientCall(null, "GET", new Reference("http://localhost/"), "/", false)
                .onResponseComplete(trailers);
        var call = new NettyClientCall(null, "GET", new Reference("http://localhost/"), "/", false);
        var body = new ChannelInputStream(null, 0);
        var bodyField = NettyClientCall.class.getDeclaredField("body");
        bodyField.setAccessible(true);
        bodyField.set(call, body);
        var lockField = NettyClientCall.class.getDeclaredField("lock");
        lockField.setAccessible(true);
        ReentrantLock lock = (ReentrantLock) lockField.get(call);
        CountDownLatch completing = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            java.util.concurrent.Callable<Integer> reader = body::read;
            var read = executor.submit(reader);
            lock.lock();
            java.util.concurrent.Future<?> completion;
            try {
                completion =
                        executor.submit(
                                () -> {
                                    completing.countDown();
                                    call.onResponseComplete(trailers);
                                });
                assertTrue(completing.await(5, TimeUnit.SECONDS));
                assertThrows(
                        TimeoutException.class,
                        () -> read.get(1, TimeUnit.SECONDS),
                        "EOF must not be observable while terminal state publication is blocked");
            } finally {
                lock.unlock();
            }
            completion.get(5, TimeUnit.SECONDS);
            assertEquals(-1, read.get(5, TimeUnit.SECONDS));
        }
    }

    @SuppressWarnings("unchecked")
    private static EmbeddedChannel waitingForResponse() throws Exception {
        var helper = new HttpServerHelper(null);
        var handler = new NettyServerHandler(helper, false, StreamKind.HTTP1);
        var channel = new EmbeddedChannel(handler);
        var call =
                new NettyServerCall(
                        helper,
                        channel,
                        StreamKind.HTTP1,
                        new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"),
                        new ChannelInputStream(channel, 0),
                        false);
        var field = NettyServerHandler.class.getDeclaredField("activeCall");
        field.setAccessible(true);
        ((AtomicReference<NettyServerCall>) field.get(handler)).set(call);
        return channel;
    }
}
