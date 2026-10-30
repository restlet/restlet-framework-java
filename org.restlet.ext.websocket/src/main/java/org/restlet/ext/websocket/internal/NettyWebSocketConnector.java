/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.websocket.internal;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketClientExtensionHandler;
import io.netty.handler.codec.http.websocketx.extensions.compression.PerMessageDeflateClientExtensionHandshaker;
import io.netty.handler.codec.http.websocketx.extensions.compression.PerMessageDeflateServerExtensionHandshaker;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.IdentityCipherSuiteFilter;
import io.netty.handler.ssl.JdkSslContext;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import org.restlet.ext.websocket.WebSocketSession;

/**
 * Netty implementation of the WebSocket client: one connection per session, HTTP/1.1 upgrade.
 *
 * <p>This class is internal to the extension; use {@link
 * org.restlet.ext.websocket.WebSocketClient}.
 */
public final class NettyWebSocketConnector implements AutoCloseable {

    private final EventLoopGroup group;

    /** Whether the event loop group was created by this connector, which then shuts it down. */
    private final boolean ownedGroup;

    /** The open connections, closed with the connector. */
    private final ChannelGroup channels =
            new DefaultChannelGroup("restlet-websocket-client", GlobalEventExecutor.INSTANCE);

    private final SslContext sslContext;

    private final long connectTimeoutMs;

    private final long idleTimeoutMs;

    private final int maxMessageSize;

    private final int maxFrameSize;

    private final boolean compression;

    private final boolean hostnameVerification;

    private final Logger logger;

    /**
     * Constructor.
     *
     * @param sharedGroup The event loop group to share, which this connector never shuts down, or
     *     null to create one.
     * @param sslContext The JDK SSL context used for "wss" URIs, or null to use the default one.
     * @param connectTimeoutMs The connect and handshake timeout in milliseconds, 0 for none.
     * @param idleTimeoutMs The idle time after which the connection is pinged, then closed, 0 for
     *     none.
     * @param maxMessageSize The maximum size in bytes of a received message.
     * @param maxFrameSize The maximum size in bytes of a received frame.
     * @param compression Whether "permessage-deflate" compression is offered to the server.
     * @param hostnameVerification Whether the server host name is verified for "wss" URIs.
     * @param logger The logger.
     * @throws IOException If the SSL context cannot be created.
     */
    public NettyWebSocketConnector(
            EventLoopGroup sharedGroup,
            SSLContext sslContext,
            long connectTimeoutMs,
            long idleTimeoutMs,
            int maxMessageSize,
            int maxFrameSize,
            boolean compression,
            boolean hostnameVerification,
            Logger logger)
            throws IOException {
        try {
            SSLContext jdkContext = (sslContext != null) ? sslContext : SSLContext.getDefault();
            this.sslContext =
                    new JdkSslContext(
                            jdkContext,
                            true,
                            null,
                            IdentityCipherSuiteFilter.INSTANCE,
                            ApplicationProtocolConfig.DISABLED,
                            ClientAuth.NONE,
                            null,
                            false);
        } catch (Exception e) {
            throw new IOException("Unable to create the WebSocket SSL context", e);
        }
        this.connectTimeoutMs = connectTimeoutMs;
        this.idleTimeoutMs = idleTimeoutMs;
        this.maxMessageSize = maxMessageSize;
        this.maxFrameSize = Math.min(maxFrameSize, maxMessageSize);
        this.compression = compression;
        this.hostnameVerification = hostnameVerification;
        this.logger = logger;
        this.ownedGroup = (sharedGroup == null);
        this.group =
                ownedGroup
                        ? new MultiThreadIoEventLoopGroup(0, NioIoHandler.newFactory())
                        : sharedGroup;
    }

    /**
     * Opens a session, blocking until the handshake completes.
     *
     * @param uri The "ws" or "wss" URI.
     * @param subprotocols The subprotocols to offer, in preference order, possibly empty.
     * @param headers Additional handshake request headers.
     * @return The open session.
     * @throws IOException If the connection or the handshake fails.
     */
    public WebSocketSession connect(URI uri, List<String> subprotocols, Map<String, String> headers)
            throws IOException {
        String scheme = uri.getScheme() == null ? "" : uri.getScheme().toLowerCase();
        boolean secure;
        if ("wss".equals(scheme)) {
            secure = true;
        } else if ("ws".equals(scheme)) {
            secure = false;
        } else {
            throw new IllegalArgumentException("Unsupported WebSocket URI scheme: " + scheme);
        }
        String host = uri.getHost();
        if (host == null) {
            throw new IllegalArgumentException("The WebSocket URI has no host: " + uri);
        }
        int defaultPort = secure ? 443 : 80;
        int port = (uri.getPort() > 0) ? uri.getPort() : defaultPort;

        if (group.isShuttingDown()) {
            throw new IOException(
                    "The shared event loop is shut down: the borrowed client was stopped");
        }

        HttpHeaders extraHeaders = new DefaultHttpHeaders();
        headers.forEach(extraHeaders::add);
        WebSocketClientHandshaker handshaker =
                WebSocketClientHandshakerFactory.newHandshaker(
                        uri,
                        WebSocketVersion.V13,
                        subprotocols.isEmpty() ? null : String.join(",", subprotocols),
                        compression,
                        extraHeaders,
                        maxFrameSize);

        Promise<NettyWebSocketSession> ready = group.next().newPromise();
        Bootstrap bootstrap =
                new Bootstrap()
                        .group(group)
                        .channel(NioSocketChannel.class)
                        .option(ChannelOption.TCP_NODELAY, true)
                        .handler(
                                new ChannelInitializer<SocketChannel>() {
                                    @Override
                                    protected void initChannel(SocketChannel ch) {
                                        channels.add(ch);
                                        initPipeline(ch, secure, host, port, handshaker, ready);
                                    }
                                });
        if (connectTimeoutMs > 0) {
            bootstrap.option(
                    ChannelOption.CONNECT_TIMEOUT_MILLIS,
                    (int) Math.min(Integer.MAX_VALUE, connectTimeoutMs));
        }
        ChannelFuture connect = bootstrap.connect(host, port);
        await(connect, "Unable to connect to " + uri);
        Channel channel = connect.channel();
        try {
            await(ready, "WebSocket handshake with " + uri);
            NettyWebSocketSession session = ready.getNow();
            session.start();
            return session;
        } catch (IOException e) {
            channel.close();
            throw e;
        }
    }

    private void await(Future<?> future, String message) throws IOException {
        try {
            boolean done =
                    (connectTimeoutMs > 0)
                            ? future.await(connectTimeoutMs, TimeUnit.MILLISECONDS)
                            : future.await().isDone();
            if (!done) {
                future.cancel(false);
                throw new ConnectException(message + ": timed out");
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            future.cancel(false);
            throw new InterruptedIOException(message + ": interrupted");
        }
        if (!future.isSuccess()) {
            Throwable cause = future.cause();
            if (future instanceof ChannelFuture channelFuture) {
                channelFuture.channel().close();
            }
            if (cause instanceof IOException ioe && !(future instanceof ChannelFuture)) {
                throw new IOException(message + ": " + ioe.getMessage(), ioe);
            }
            ConnectException ce = new ConnectException(message + ": " + cause);
            ce.initCause(cause);
            throw ce;
        }
    }

    private void initPipeline(
            SocketChannel ch,
            boolean secure,
            String host,
            int port,
            WebSocketClientHandshaker handshaker,
            Promise<NettyWebSocketSession> ready) {
        ChannelPipeline p = ch.pipeline();
        if (idleTimeoutMs > 0) {
            p.addLast("idle", new IdleStateHandler(0, 0, idleTimeoutMs, TimeUnit.MILLISECONDS));
        }
        if (secure) {
            SSLEngine engine = sslContext.newEngine(ch.alloc(), host, port);
            if (hostnameVerification) {
                SSLParameters parameters = engine.getSSLParameters();
                parameters.setEndpointIdentificationAlgorithm("HTTPS");
                engine.setSSLParameters(parameters);
            }
            SslHandler sslHandler = new SslHandler(engine);
            sslHandler.setHandshakeTimeoutMillis(connectTimeoutMs);
            p.addLast("ssl", sslHandler);
        }
        p.addLast("http-codec", new HttpClientCodec());
        p.addLast("http-aggregator", new HttpObjectAggregator(8192));
        if (compression) {
            // Offers permessage-deflate, then installs the compression codecs if accepted.
            // Inflated messages are capped by the message size limit.
            p.addLast(
                    "ws-compression",
                    new WebSocketClientExtensionHandler(
                            new PerMessageDeflateClientExtensionHandshaker(
                                    6,
                                    ZlibCodecFactory.isSupportingWindowSizeAndMemLevel(),
                                    PerMessageDeflateServerExtensionHandshaker.MAX_WINDOW_SIZE,
                                    false,
                                    false,
                                    maxMessageSize)));
        }
        p.addLast("ws-handshake", new HandshakeHandler(handshaker, ready));
    }

    @Override
    public void close() {
        channels.close().awaitUninterruptibly(5, TimeUnit.SECONDS);
        if (ownedGroup) {
            group.shutdownGracefully(0, 2, TimeUnit.SECONDS)
                    .awaitUninterruptibly(5, TimeUnit.SECONDS);
        }
    }

    /** Sends the handshake request and installs the session once the response is accepted. */
    private final class HandshakeHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

        private final WebSocketClientHandshaker handshaker;

        private final Promise<NettyWebSocketSession> ready;

        HandshakeHandler(
                WebSocketClientHandshaker handshaker, Promise<NettyWebSocketSession> ready) {
            this.handshaker = handshaker;
            this.ready = ready;
        }

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            handshaker
                    .handshake(ctx.channel())
                    .addListener(
                            f -> {
                                if (!f.isSuccess()) {
                                    ready.tryFailure(f.cause());
                                    ctx.close();
                                }
                            });
            super.channelActive(ctx);
        }

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse response) {
            Channel channel = ctx.channel();
            try {
                handshaker.finishHandshake(channel, response);
            } catch (RuntimeException e) {
                ready.tryFailure(
                        new IOException("WebSocket handshake rejected: " + response.status(), e));
                ctx.close();
                return;
            }
            NettyWebSocketSession session =
                    new NettyWebSocketSession(
                            channel,
                            handshaker.actualSubprotocol(),
                            maxMessageSize,
                            connectTimeoutMs,
                            logger);
            ChannelPipeline p = ctx.pipeline();
            p.addAfter(ctx.name(), "ws-aggregator", new WebSocketFrameAggregator(maxMessageSize));
            p.addAfter("ws-aggregator", "ws-session", session.newHandler());
            p.remove(this);
            if (!ready.trySuccess(session)) {
                ctx.close();
            }
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            ready.tryFailure(new IOException("Connection closed during the WebSocket handshake"));
            super.channelInactive(ctx);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            ready.tryFailure(cause);
            ctx.close();
        }
    }
}
