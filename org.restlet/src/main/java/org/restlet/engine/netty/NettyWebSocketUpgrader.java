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
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http.websocketx.WebSocketDecoderConfig;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.restlet.engine.Engine;
import org.restlet.websocket.WebSocketHandler;
import org.restlet.websocket.WebSocketSession;
import org.restlet.websocket.WebSocketUpgrade;

/**
 * Switches an HTTP/1.1 server connection to WebSocket after a Restlet accepted the upgrade, then
 * runs the session handler on its own virtual thread.
 *
 * <p>This class is internal to the Netty connectors.
 */
final class NettyWebSocketUpgrader {

    /** Maximum time to wait for the handshake response to be written. */
    private static final long HANDSHAKE_TIMEOUT_MS = 10_000;

    private NettyWebSocketUpgrader() {}

    /**
     * Performs the server handshake and starts the session. On failure, the connection is closed.
     * The caller must have acquired a WebSocket permit, released here when the connection closes.
     *
     * @param helper The server helper.
     * @param channel The HTTP/1.1 connection.
     * @param request The upgrade request head.
     * @param confidential True if the connection uses TLS.
     * @param upgrade The accepted upgrade.
     */
    static void upgrade(
            NettyServerHelper helper,
            Channel channel,
            HttpRequest request,
            boolean confidential,
            WebSocketUpgrade upgrade) {
        channel.closeFuture().addListener(f -> helper.releaseWebSocket());
        int maxMessageSize = helper.getWebSocketMaxMessageSize();

        FullHttpRequest full =
                new DefaultFullHttpRequest(
                        request.protocolVersion(), request.method(), request.uri());
        full.headers().set(request.headers());
        String host = request.headers().get(HttpHeaderNames.HOST, "localhost");
        String location = (confidential ? "wss://" : "ws://") + host + request.uri();

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
                    .addListener(f -> channel.close());
            full.release();
            return;
        }

        ChannelPipeline pipeline = channel.pipeline();
        removeIfPresent(pipeline, HttpServerKeepAliveHandler.class);
        removeIfPresent(pipeline, HttpServerUpgradeHandler.class);
        removeIfPresent(pipeline, NettyServerHandler.class);

        NettyWebSocketSession session =
                new NettyWebSocketSession(
                        channel,
                        handshaker.selectedSubprotocol(),
                        maxMessageSize,
                        helper.getConnectorIdleTimeout(),
                        helper.getLogger());
        // Installed before the handshake so that no frame sent right after it is missed.
        pipeline.addLast("ws-aggregator", new WebSocketFrameAggregator(maxMessageSize));
        pipeline.addLast("ws-session", session.newHandler());
        try {
            ChannelFuture future = handshaker.handshake(channel, full);
            if (!future.await(HANDSHAKE_TIMEOUT_MS, TimeUnit.MILLISECONDS) || !future.isSuccess()) {
                helper.getLogger().log(Level.FINE, "WebSocket handshake failed", future.cause());
                channel.close();
                return;
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            channel.close();
            return;
        } finally {
            full.release();
        }

        session.setSubprotocol(handshaker.selectedSubprotocol());
        session.start();

        WebSocketHandler handler = upgrade.getHandler();
        Thread.ofVirtual().name("restlet-websocket").start(() -> run(helper, session, handler));
    }

    private static void run(
            NettyServerHelper helper, NettyWebSocketSession session, WebSocketHandler handler) {
        try {
            handler.handle(session);
            session.close(WebSocketSession.NORMAL_CLOSURE, null);
        } catch (Exception | LinkageError | AssertionError e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            helper.getLogger().log(Level.WARNING, "Error while handling a WebSocket session", e);
            try {
                session.close(WebSocketSession.INTERNAL_ERROR, null);
            } catch (Exception closeFailure) {
                e.addSuppressed(closeFailure);
            }
        } finally {
            Engine.clearThreadLocalVariables();
        }
    }

    private static void removeIfPresent(
            ChannelPipeline pipeline, Class<? extends io.netty.channel.ChannelHandler> type) {
        if (pipeline.get(type) != null) {
            pipeline.remove(type);
        }
    }
}
