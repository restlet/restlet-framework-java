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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.engine.connector.HttpClientHelper;
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
    void doesNotReadAfterReplayingAnUpgradeRequest() throws Exception {
        assertEquals(0, readsAfterReplaying(HttpHeaderNames.UPGRADE.toString()));
        assertTrue(readsAfterReplaying(null) > 0);
    }

    @SuppressWarnings("unchecked")
    private static int readsAfterReplaying(String header) throws Exception {
        var helper = new HttpServerHelper(null);
        var executor = NettyServerHelper.class.getDeclaredField("callExecutor");
        executor.setAccessible(true);
        executor.set(helper, new IdleExecutor());
        AtomicInteger reads = new AtomicInteger();
        var handler = new NettyServerHandler(helper, false, StreamKind.HTTP1);
        var channel =
                new EmbeddedChannel(
                        new ChannelOutboundHandlerAdapter() {
                            @Override
                            public void read(ChannelHandlerContext ctx) {
                                reads.incrementAndGet();
                                ctx.read();
                            }
                        },
                        handler);
        try {
            var active =
                    new NettyServerCall(
                            helper,
                            channel,
                            StreamKind.HTTP1,
                            new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"),
                            new ChannelInputStream(channel, 0),
                            false);
            var field = NettyServerHandler.class.getDeclaredField("activeCall");
            field.setAccessible(true);
            ((AtomicReference<NettyServerCall>) field.get(handler)).set(active);
            var queued = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/next");
            if (header != null) {
                queued.headers().set(header, "websocket");
            }
            channel.writeInbound(queued, LastHttpContent.EMPTY_LAST_CONTENT);
            reads.set(0);
            handler.responseComplete(channel.pipeline().context(handler), active);
            return reads.get();
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    @Test
    void servesH2cUpgradePipelinedBehindAnActiveExchangeOverHttp11() throws Exception {
        org.restlet.engine.Engine.register(false);
        org.restlet.engine.Engine.getInstance()
                .getRegisteredServers()
                .add(new HttpServerHelper(null));
        CountDownLatch slowStarted = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        Context context = new Context();
        context.getParameters().add("http.transport.protocol", "HTTP2");
        var server =
                new Server(
                        context,
                        Protocol.HTTP,
                        0,
                        new Restlet() {
                            @Override
                            public void handle(Request request, Response response) {
                                String path = request.getResourceRef().getPath();
                                if ("/slow".equals(path)) {
                                    slowStarted.countDown();
                                    try {
                                        release.await(5, TimeUnit.SECONDS);
                                    } catch (InterruptedException _) {
                                        Thread.currentThread().interrupt();
                                    }
                                }
                                response.setEntity(path, MediaType.TEXT_PLAIN);
                            }
                        });
        server.start();
        try (var socket = new Socket("localhost", server.getActualPort())) {
            socket.setSoTimeout(5000);
            OutputStream out = socket.getOutputStream();
            out.write(
                    ("GET /slow HTTP/1.1\r\nHost: localhost\r\n\r\n")
                            .getBytes(StandardCharsets.US_ASCII));
            out.flush();
            assertTrue(slowStarted.await(5, TimeUnit.SECONDS));
            out.write(
                    ("GET /upgrade HTTP/1.1\r\nHost: localhost\r\n"
                                    + "Connection: Upgrade, HTTP2-Settings\r\n"
                                    + "Upgrade: h2c\r\nHTTP2-Settings: AAMAAABkAAQAAP__\r\n\r\n")
                            .getBytes(StandardCharsets.US_ASCII));
            out.flush();
            Thread.sleep(200);
            release.countDown();
            var in =
                    new BufferedReader(
                            new InputStreamReader(
                                    socket.getInputStream(), StandardCharsets.US_ASCII));
            List<String> statusLines = new ArrayList<>();
            String line;
            while (statusLines.size() < 2 && (line = in.readLine()) != null) {
                int status = line.indexOf("HTTP/1.1 ");
                if (status >= 0) {
                    statusLines.add(line.substring(status));
                }
            }
            assertEquals(List.of("HTTP/1.1 200 OK", "HTTP/1.1 200 OK"), statusLines);
        } finally {
            release.countDown();
            server.stop();
        }
    }

    @Test
    void reportsFailedTerminalResponseWriteAsDisconnect() throws Exception {
        var helper = new HttpServerHelper(null);
        IOException failure = new IOException("reset while flushing");
        var channel =
                new EmbeddedChannel(
                        new io.netty.channel.ChannelOutboundHandlerAdapter() {
                            @Override
                            public void write(
                                    ChannelHandlerContext ctx,
                                    Object msg,
                                    io.netty.channel.ChannelPromise promise) {
                                io.netty.util.ReferenceCountUtil.release(msg);
                                promise.setFailure(failure);
                            }
                        });
        try {
            var call =
                    new NettyServerCall(
                            helper,
                            channel,
                            StreamKind.HTTP1,
                            new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/"),
                            new ChannelInputStream(channel, 0),
                            false);
            Request request = call.toRequest(new Context());
            call.complete();
            channel.runPendingTasks();
            assertEquals(
                    failure,
                    request.getDisconnectSignal().toCompletableFuture().get(5, TimeUnit.SECONDS));
        } finally {
            channel.finishAndReleaseAll();
        }
    }

    /** An executor that accepts calls without running them. */
    private static final class IdleExecutor extends java.util.concurrent.AbstractExecutorService {
        @Override
        public void execute(Runnable command) {}

        @Override
        public void shutdown() {}

        @Override
        public List<Runnable> shutdownNow() {
            return List.of();
        }

        @Override
        public boolean isShutdown() {
            return false;
        }

        @Override
        public boolean isTerminated() {
            return false;
        }

        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return true;
        }
    }

    @Test
    void publishesClientCompletionBeforeReleasingAnEmptyBodyConsumer() throws Exception {
        var helper = new HttpClientHelper(null);
        var trailers = new io.netty.handler.codec.http.DefaultHttpHeaders();
        new NettyClientCall(helper, "GET", new Reference("http://localhost/"), "/", false)
                .onResponseComplete(trailers);
        var call =
                new NettyClientCall(helper, "GET", new Reference("http://localhost/"), "/", false);
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
