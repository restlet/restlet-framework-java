/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2DataFrame;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.representation.OutputRepresentation;

/**
 * An HTTP/2 response completed while the request body is still being sent must not leave its stream
 * open: the request data the server no longer reads would hold the connection's flow-control
 * window, and the connection's next requests could not send their bodies (RFC 9113, section 8.1).
 */
class Http2EarlyResponseTestCase {

    /** Far more than the HTTP/2 flow-control windows, so that an unread request stalls. */
    private static final long LARGE = 8L * 1024 * 1024;

    private Server server;

    private Client client;

    private EventLoopGroup rawGroup;

    private final java.util.concurrent.atomic.AtomicInteger rawConnections =
            new java.util.concurrent.atomic.AtomicInteger();

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @BeforeEach
    void setUp() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            executor.shutdownNow();
            if (client != null) {
                client.stop();
            }
        } finally {
            try {
                if (server != null) {
                    server.stop();
                }
            } finally {
                if (rawGroup != null) {
                    rawGroup.shutdownGracefully(0, 1, TimeUnit.SECONDS).sync();
                }
                Engine.clearThreadLocalVariables();
            }
        }
    }

    /**
     * The server resets the stream of a request it answered before reading it all: the client stops
     * sending, and its next request on the connection goes through.
     */
    @Test
    void theServerResetsAnUnfinishedRequestAfterAnsweringIt() throws Exception {
        Restlet restlet =
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        if (request.getResourceRef().getPath().equals("/early")) {
                            response.setEntity("early", MediaType.TEXT_PLAIN);
                        } else {
                            response.setEntity(request.getEntityAsText(), MediaType.TEXT_PLAIN);
                        }
                    }
                };
        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocol", "HTTP2");
        server = new Server(serverContext, Protocol.HTTP, 0, restlet);
        server.start();
        String base = "http://localhost:" + server.getActualPort();
        startClient();

        var writer = new LargeBody();
        Request early = new Request(Method.POST, base + "/early", writer);
        Response response = executor.submit(() -> client.handle(early)).get(10, TimeUnit.SECONDS);
        assertEquals("early", response.getEntityAsText());

        assertTrue(writer.ended.await(10, TimeUnit.SECONDS), "the request body kept waiting");
        assertTrue(writer.written.get() < LARGE, "the whole request body was sent");
        assertEquals("next", post(base + "/echo", "next"));
    }

    /**
     * A client abandoning a request whose response it read entirely resets its stream, even against
     * a server that leaves the stream open: its next request on the connection goes through.
     */
    @Test
    void anAbortedRequestIsResetAfterItsWholeResponse() throws Exception {
        int port = startRawServer();
        String base = "http://localhost:" + port;
        startClient();

        var writer = new LargeBody();
        Request early = new Request(Method.POST, base + "/early", writer);
        Response response = executor.submit(() -> client.handle(early)).get(10, TimeUnit.SECONDS);
        assertEquals("early", response.getEntityAsText());
        early.abort();

        assertTrue(writer.ended.await(10, TimeUnit.SECONDS), "the request body kept waiting");
        assertEquals("4", post(base + "/count", "next"));
        assertEquals(1, rawConnections.get(), "the next request used another connection");
    }

    private void startClient() throws Exception {
        Context clientContext = new Context();
        clientContext.getParameters().add("httpClientTransportMode", "HTTP2");
        client = new Client(clientContext, Protocol.HTTP);
        client.start();
    }

    /** Posts a small body, whose bytes need the connection's flow-control window. */
    private String post(String uri, String text) throws Exception {
        Request request = new Request(Method.POST, uri);
        request.setEntity(text, MediaType.TEXT_PLAIN);
        return executor.submit(() -> client.handle(request).getEntityAsText())
                .get(10, TimeUnit.SECONDS);
    }

    /**
     * Starts an HTTP/2 server that answers "/early" as soon as it gets the request headers, then
     * never reads that stream again nor resets it; other paths are answered with the number of
     * request body bytes, once the request ends.
     */
    private int startRawServer() throws InterruptedException {
        rawGroup = new MultiThreadIoEventLoopGroup(1, NioIoHandler.newFactory());
        Channel listener =
                new ServerBootstrap()
                        .group(rawGroup)
                        .channel(NioServerSocketChannel.class)
                        .childHandler(
                                new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    protected void initChannel(SocketChannel ch) {
                                        rawConnections.incrementAndGet();
                                        ch.pipeline()
                                                .addLast(
                                                        Http2FrameCodecBuilder.forServer().build(),
                                                        new Http2MultiplexHandler(
                                                                new ChannelInitializer<
                                                                        Http2StreamChannel>() {
                                                                    @Override
                                                                    protected void initChannel(
                                                                            Http2StreamChannel
                                                                                    stream) {
                                                                        stream.config()
                                                                                .setAutoRead(false);
                                                                        stream.pipeline()
                                                                                .addLast(
                                                                                        new RawStream());
                                                                    }
                                                                }));
                                    }
                                })
                        .bind(new InetSocketAddress("localhost", 0))
                        .sync()
                        .channel();
        return ((InetSocketAddress) listener.localAddress()).getPort();
    }

    /** One stream of the raw server, read one frame batch at a time. */
    private static final class RawStream extends ChannelInboundHandlerAdapter {
        private long received;

        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ctx.read();
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (msg instanceof Http2HeadersFrame headers) {
                if ("/early".contentEquals(headers.headers().path())) {
                    // Answers at once, then neither reads nor resets the stream.
                    respond(ctx, "early");
                } else if (headers.isEndStream()) {
                    respond(ctx, "0");
                } else {
                    ctx.read();
                }
            } else if (msg instanceof Http2DataFrame data) {
                received += data.content().readableBytes();
                boolean end = data.isEndStream();
                data.release();
                if (end) {
                    respond(ctx, Long.toString(received));
                } else {
                    ctx.read();
                }
            } else {
                ReferenceCountUtil.release(msg);
            }
        }

        private static void respond(ChannelHandlerContext ctx, String text) {
            ctx.write(
                    new DefaultHttp2HeadersFrame(
                            new DefaultHttp2Headers()
                                    .status("200")
                                    .add("content-type", "text/plain")));
            ctx.writeAndFlush(
                    new DefaultHttp2DataFrame(
                            Unpooled.copiedBuffer(text, StandardCharsets.UTF_8), true));
        }
    }

    /** A request body far larger than the flow-control windows, written until it fails. */
    private static final class LargeBody extends OutputRepresentation {
        private final AtomicLong written = new AtomicLong();
        private final CountDownLatch ended = new CountDownLatch(1);

        private LargeBody() {
            super(MediaType.APPLICATION_OCTET_STREAM);
        }

        @Override
        public void write(OutputStream output) throws IOException {
            byte[] chunk = new byte[16 * 1024];
            try {
                while (written.get() < LARGE) {
                    output.write(chunk);
                    output.flush();
                    written.addAndGet(chunk.length);
                }
            } finally {
                ended.countDown();
            }
        }
    }
}
