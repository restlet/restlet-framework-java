/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.websocket;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketDecoderConfig;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.restlet.data.Parameter;
import org.restlet.engine.Engine;
import org.restlet.engine.netty.NettyConnectionUpgrade;
import org.restlet.engine.netty.NettyUpgradeContext;
import org.restlet.util.Series;

/**
 * Switches an HTTP/1.1 server connection to WebSocket after a Restlet accepted the upgrade, then
 * runs the session handler on its own virtual thread.
 */
final class NettyWebSocketUpgrader implements NettyConnectionUpgrade {

    /** Name of the server connector attribute holding the session permits. */
    private static final String PERMITS_ATTRIBUTE = "org.restlet.ext.websocket.permits";

    /** Maximum time to wait for the handshake response to be written. */
    private static final long HANDSHAKE_TIMEOUT_MS = 10_000;

    private final WebSocketUpgrade upgrade;

    /**
     * Constructor.
     *
     * @param upgrade The accepted upgrade.
     */
    NettyWebSocketUpgrader(WebSocketUpgrade upgrade) {
        this.upgrade = upgrade;
    }

    @Override
    public void upgrade(NettyUpgradeContext context) throws Exception {
        Channel channel = context.getChannel();
        Series<Parameter> parameters = context.getParameters();
        Semaphore permits = getPermits(context, parameters);
        if (permits != null && !permits.tryAcquire()) {
            sendError(channel, HttpResponseStatus.SERVICE_UNAVAILABLE);
            return;
        }
        if (permits != null) {
            channel.closeFuture().addListener(f -> permits.release());
        }

        int maxMessageSize = getMaxMessageSize(parameters);
        HttpRequest request = context.getRequest();
        FullHttpRequest full =
                new DefaultFullHttpRequest(
                        request.protocolVersion(), request.method(), request.uri());
        full.headers().set(request.headers());
        String host = request.headers().get(HttpHeaderNames.HOST, "localhost");
        String location = (context.isConfidential() ? "wss://" : "ws://") + host + request.uri();

        WebSocketServerHandshakerFactory factory =
                new WebSocketServerHandshakerFactory(
                        location,
                        upgrade.getSubprotocol(),
                        WebSocketDecoderConfig.newBuilder()
                                .maxFramePayloadLength(maxMessageSize)
                                .allowExtensions(false)
                                .build());
        WebSocketServerHandshaker handshaker = factory.newHandshaker(full);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel)
                    .addListener(ChannelFutureListener.CLOSE);
            full.release();
            return;
        }

        NettyWebSocketSession session =
                new NettyWebSocketSession(
                        channel,
                        handshaker.selectedSubprotocol(),
                        maxMessageSize,
                        context.getIdleTimeout(),
                        context.getLogger());
        // Installed before the handshake so that no frame sent right after it is missed.
        ChannelPipeline pipeline = channel.pipeline();
        pipeline.addLast("ws-aggregator", new WebSocketFrameAggregator(maxMessageSize));
        pipeline.addLast("ws-session", session.newHandler());
        try {
            ChannelFuture future = handshaker.handshake(channel, full);
            if (!future.await(HANDSHAKE_TIMEOUT_MS, TimeUnit.MILLISECONDS) || !future.isSuccess()) {
                context.getLogger().log(Level.FINE, "WebSocket handshake failed", future.cause());
                channel.close();
                return;
            }
        } finally {
            full.release();
        }

        session.setSubprotocol(handshaker.selectedSubprotocol());
        session.start();

        WebSocketHandler handler = upgrade.getHandler();
        Thread.ofVirtual().name("restlet-websocket").start(() -> run(context, session, handler));
    }

    private static void run(
            NettyUpgradeContext context, NettyWebSocketSession session, WebSocketHandler handler) {
        try {
            handler.handle(session);
            session.close(WebSocketSession.NORMAL_CLOSURE, null);
        } catch (Exception | LinkageError | AssertionError e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            context.getLogger().log(Level.WARNING, "Error while handling a WebSocket session", e);
            try {
                session.close(WebSocketSession.INTERNAL_ERROR, null);
            } catch (Exception closeFailure) {
                e.addSuppressed(closeFailure);
            }
        } finally {
            Engine.clearThreadLocalVariables();
        }
    }

    private static Semaphore getPermits(NettyUpgradeContext context, Series<Parameter> parameters) {
        int max = Integer.parseInt(parameters.getFirstValue("websocket.maxSessions", "0"));
        if (max < 0) {
            throw new IllegalArgumentException("'websocket.maxSessions' cannot be negative");
        }
        if (max == 0) {
            return null;
        }
        return (Semaphore)
                context.getAttributes().computeIfAbsent(PERMITS_ATTRIBUTE, k -> new Semaphore(max));
    }

    private static int getMaxMessageSize(Series<Parameter> parameters) {
        int result =
                Integer.parseInt(parameters.getFirstValue("websocket.maxMessageSize", "65536"));
        if (result <= 0) {
            throw new IllegalArgumentException("'websocket.maxMessageSize' must be positive");
        }
        return result;
    }

    private static void sendError(Channel channel, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
