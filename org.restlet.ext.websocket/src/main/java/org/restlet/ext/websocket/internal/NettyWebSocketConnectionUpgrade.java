/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.websocket.internal;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.compression.ZlibCodecFactory;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.websocketx.WebSocketDecoderConfig;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionData;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketExtensionUtil;
import io.netty.handler.codec.http.websocketx.extensions.WebSocketServerExtension;
import io.netty.handler.codec.http.websocketx.extensions.compression.PerMessageDeflateServerExtensionHandshaker;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.restlet.data.Parameter;
import org.restlet.engine.Engine;
import org.restlet.engine.netty.NettyConnectionUpgrade;
import org.restlet.engine.netty.NettyUpgradeContext;
import org.restlet.ext.websocket.WebSocketHandler;
import org.restlet.ext.websocket.WebSocketSession;
import org.restlet.ext.websocket.WebSocketUpgrade;
import org.restlet.util.Series;

/**
 * Switches an HTTP/1.1 server connection to WebSocket after a Restlet accepted the upgrade, then
 * runs the session handler on its own virtual thread.
 */
public final class NettyWebSocketConnectionUpgrade implements NettyConnectionUpgrade {

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
    public NettyWebSocketConnectionUpgrade(WebSocketUpgrade upgrade) {
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
        int maxFrameSize = getMaxFrameSize(parameters, maxMessageSize);
        boolean compression =
                Boolean.parseBoolean(parameters.getFirstValue("websocket.compression", "true"));
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
                                .maxFramePayloadLength(maxFrameSize)
                                .allowExtensions(compression)
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
        HttpHeaders responseHeaders = new DefaultHttpHeaders();
        if (compression) {
            WebSocketServerExtension extension = negotiateCompression(request, maxMessageSize);
            if (extension != null) {
                responseHeaders.set(
                        HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS,
                        toHeaderValue(extension.newReponseData()));
                // The frame codecs installed by the handshake pass through these positions.
                pipeline.addLast("ws-inflater", extension.newExtensionDecoder());
                pipeline.addLast("ws-deflater", extension.newExtensionEncoder());
            }
        }
        pipeline.addLast("ws-aggregator", new WebSocketFrameAggregator(maxMessageSize));
        pipeline.addLast("ws-session", session.newHandler());
        try {
            ChannelFuture future =
                    handshaker.handshake(channel, full, responseHeaders, channel.newPromise());
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

    /**
     * Accepts the first "permessage-deflate" offer of the request that can be honored.
     *
     * @param request The upgrade request.
     * @param maxMessageSize The maximum size of an inflated message.
     * @return The negotiated extension, or null.
     */
    private static WebSocketServerExtension negotiateCompression(
            HttpRequest request, int maxMessageSize) {
        String offers = request.headers().getAsString(HttpHeaderNames.SEC_WEBSOCKET_EXTENSIONS);
        if (offers == null) {
            return null;
        }
        PerMessageDeflateServerExtensionHandshaker handshaker =
                new PerMessageDeflateServerExtensionHandshaker(
                        6,
                        ZlibCodecFactory.isSupportingWindowSizeAndMemLevel(),
                        PerMessageDeflateServerExtensionHandshaker.MAX_WINDOW_SIZE,
                        false,
                        false,
                        maxMessageSize);
        for (WebSocketExtensionData offer : WebSocketExtensionUtil.extractExtensions(offers)) {
            WebSocketServerExtension extension = handshaker.handshakeExtension(offer);
            if (extension != null) {
                return extension;
            }
        }
        return null;
    }

    /** Formats extension data as a "Sec-WebSocket-Extensions" header value. */
    static String toHeaderValue(WebSocketExtensionData data) {
        StringBuilder result = new StringBuilder(data.name());
        for (Map.Entry<String, String> parameter : data.parameters().entrySet()) {
            result.append("; ").append(parameter.getKey());
            if (parameter.getValue() != null) {
                result.append('=').append(parameter.getValue());
            }
        }
        return result.toString();
    }

    private static int getMaxMessageSize(Series<Parameter> parameters) {
        int result =
                Integer.parseInt(parameters.getFirstValue("websocket.maxMessageSize", "65536"));
        if (result <= 0) {
            throw new IllegalArgumentException("'websocket.maxMessageSize' must be positive");
        }
        return result;
    }

    private static int getMaxFrameSize(Series<Parameter> parameters, int maxMessageSize) {
        String value = parameters.getFirstValue("websocket.maxFrameSize");
        if (value == null) {
            return maxMessageSize;
        }
        int result = Integer.parseInt(value);
        if (result <= 0) {
            throw new IllegalArgumentException("'websocket.maxFrameSize' must be positive");
        }
        return Math.min(result, maxMessageSize);
    }

    private static void sendError(Channel channel, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        channel.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
