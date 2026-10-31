/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import org.restlet.engine.Engine;

/**
 * Inbound handler that converts Netty HTTP messages into {@link NettyServerCall}s and runs them on
 * the call executor. One instance per HTTP/1.1 connection, or per HTTP/2 or HTTP/3 stream.
 *
 * <p>HTTP/1.1 requests are processed one at a time. After a request body ends, one read observes
 * peer disconnects even while the application is idle. Pipelined messages from that read are
 * retained within finite limits until the preceding response is flushed.
 */
class NettyServerHandler extends ChannelInboundHandlerAdapter {

    static final AttributeKey<Boolean> EVENT_STREAM_ACTIVE =
            AttributeKey.valueOf("restlet.eventStreamActive");

    private final NettyServerHelper helper;

    private final boolean confidential;

    /** The kind of channel this handler is attached to. */
    private final StreamKind kind;

    /** The body stream of the request in progress, if any. */
    private ChannelInputStream currentBody;

    /** The call receiving the current request body. */
    private NettyServerCall currentCall;

    private final AtomicReference<NettyServerCall> activeCall = new AtomicReference<>();

    private final Deque<Object> pending = new ArrayDeque<>();
    private long pendingBytes;
    private static final int MAX_PENDING_MESSAGES = 1024;
    private static final int MAX_PENDING_BYTES = 1024 * 1024;

    /** Whether to read the rest of the current HTTP/1.1 request once the channel read completes. */
    private boolean readRequestBody;

    private boolean readAfterBody;

    NettyServerHandler(NettyServerHelper helper, boolean confidential, StreamKind kind) {
        this.helper = helper;
        this.confidential = confidential;
        this.kind = kind;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        // Read on demand: one request at a time per HTTP/1.1 connection, and body back-pressure
        // for both HTTP/1.1 connections and HTTP/2 streams.
        ctx.channel().config().setAutoRead(false);
        ctx.read();
        super.channelActive(ctx);
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        // Stream channels (HTTP/2, HTTP/3) are already active when the handler is added.
        if (kind.isStream() && ctx.channel().isActive()) {
            ctx.channel().config().setAutoRead(false);
            ctx.read();
        }
        super.handlerAdded(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        if (kind == StreamKind.HTTP1 && currentBody == null && activeCall.get() != null) {
            long bytes = pendingSize(msg);
            if (pending.size() >= MAX_PENDING_MESSAGES
                    || pendingBytes + bytes > MAX_PENDING_BYTES) {
                ReferenceCountUtil.release(msg);
                exceptionCaught(ctx, new IOException("HTTP/1.1 pipelined request limit exceeded"));
            } else {
                pending.addLast(msg);
                pendingBytes += bytes;
            }
            return;
        }
        try {
            if (msg instanceof HttpRequest request) {
                onRequest(ctx, request);
            }
            if (msg instanceof HttpContent content) {
                onContent(ctx, content);
            }
        } finally {
            if (!(msg instanceof HttpContent)) {
                ReferenceCountUtil.release(msg);
            }
        }
    }

    private static long pendingSize(Object msg) {
        long bytes = msg instanceof HttpContent content ? content.content().readableBytes() : 0;
        if (msg instanceof HttpRequest request) {
            bytes += 2L * request.uri().length();
            for (var header : request.headers()) {
                bytes += 2L * (header.getKey().length() + header.getValue().length());
            }
        }
        if (msg instanceof LastHttpContent content) {
            for (var header : content.trailingHeaders()) {
                bytes += 2L * (header.getKey().length() + header.getValue().length());
            }
        }
        return bytes;
    }

    /** Invoked on the event loop after the previous response has been flushed. */
    void responseComplete(ChannelHandlerContext ctx, NettyServerCall call) {
        activeCall.compareAndSet(call, null);
        while (!pending.isEmpty() && (activeCall.get() == null || currentBody != null)) {
            Object msg = pending.removeFirst();
            pendingBytes -= pendingSize(msg);
            channelRead(ctx, msg);
        }
        // Bytes following an upgrade or tunnel request belong to the replacement protocol.
        if (pending.isEmpty()
                && (activeCall.get() == null || currentBody != null || readAfterBody)) {
            ctx.read();
        }
    }

    /**
     * Indicates whether an HTTP/1.1 exchange is in progress or queued on this connection. Must be
     * called on the event loop.
     *
     * @return True if an exchange is in progress or queued.
     */
    boolean isExchangeActive() {
        return activeCall.get() != null || !pending.isEmpty();
    }

    private void onRequest(ChannelHandlerContext ctx, HttpRequest request) {
        DecoderResult result = request.decoderResult();
        if (result.isFailure()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }
        // Upgrade/tunnel bytes belong to the replacement protocol, not the HTTP decoder.
        readAfterBody =
                !request.headers().contains(HttpHeaderNames.UPGRADE)
                        && !request.method().equals(HttpMethod.CONNECT);

        if (HttpUtil.is100ContinueExpected(request)) {
            ctx.writeAndFlush(
                    new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
            request.headers().remove(HttpHeaderNames.EXPECT);
        }

        ChannelInputStream body =
                new ChannelInputStream(ctx.channel(), helper.getConnectorIdleTimeout());
        currentBody = body;
        if (kind == StreamKind.HTTP1 && !hasBody(request)) {
            // End the body now: the call could otherwise complete before the last (empty) content
            // is delivered, and close a connection that the response announced as kept alive.
            body.end();
        }

        NettyServerCall call =
                new NettyServerCall(helper, ctx.channel(), kind, request, body, confidential);
        currentCall = call;
        activeCall.set(call);
        if (kind != StreamKind.HTTP3) {
            call.setAltSvc(helper.getAltSvc());
        }

        if (!helper.tryAcquireCall()) {
            body.close();
            currentBody = null;
            currentCall = null;
            activeCall.compareAndSet(call, null);
            sendError(ctx, HttpResponseStatus.SERVICE_UNAVAILABLE);
            return;
        }

        try {
            helper.getCallExecutor().execute(() -> runCall(call));
        } catch (RejectedExecutionException _) {
            helper.releaseCall();
            body.close();
            currentBody = null;
            currentCall = null;
            activeCall.compareAndSet(call, null);
            sendError(ctx, HttpResponseStatus.SERVICE_UNAVAILABLE);
        }

        // Keep reading the body of this request (if any) without waiting for the call. On HTTP/1.1,
        // the read is deferred to channelReadComplete: the decoder may already have produced the
        // whole request, and reading now could take the next request before this response is
        // complete.
        if (kind.isStream()) {
            ctx.read();
        } else {
            readRequestBody = true;
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        if (readRequestBody) {
            readRequestBody = false;
            ctx.read();
        }
        super.channelReadComplete(ctx);
    }

    private void onContent(ChannelHandlerContext ctx, HttpContent content) {
        // Request content drives the next reads from now on.
        readRequestBody = false;
        ChannelInputStream body = currentBody;
        if (body == null) {
            content.release();
            return;
        }

        boolean readMore = true;
        if (content.content().isReadable()) {
            readMore = body.offer(content.content().retain());
        }
        boolean last = content instanceof LastHttpContent;
        if (last && currentCall != null) {
            currentCall.setRequestTrailers(((LastHttpContent) content).trailingHeaders());
            currentCall = null;
        }
        content.release();

        if (last) {
            body.end();
            currentBody = null;
            if (kind == StreamKind.HTTP1 && readAfterBody && pending.isEmpty()) {
                ctx.read();
            }
        } else if (readMore) {
            ctx.read();
        }
        // Otherwise the body stream requests the next read once the consumer catches up.
    }

    /** Indicates whether an HTTP/1.1 request has a body, according to its framing headers. */
    private static boolean hasBody(HttpRequest request) {
        return HttpUtil.isTransferEncodingChunked(request)
                || HttpUtil.getContentLength(request, 0L) != 0;
    }

    private void runCall(NettyServerCall call) {
        try {
            helper.handle(call);
        } catch (RuntimeException | LinkageError | AssertionError e) {
            // Keep the connection consistent whatever the application throws; other errors
            // (VirtualMachineError, ...) are left to propagate after the cleanup below.
            helper.getLogger().log(Level.WARNING, "Error while handling an HTTP server call", e);
            call.abort();
        } finally {
            helper.releaseCallPermit();
            try {
                call.complete();
            } finally {
                if (kind.isStream()) {
                    activeCall.compareAndSet(call, null);
                }
                helper.endCall();
                Engine.clearThreadLocalVariables();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        failBody(new ClosedChannelException());
        releasePending();
        super.channelInactive(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        releasePending();
        super.handlerRemoved(ctx);
    }

    private void releasePending() {
        while (!pending.isEmpty()) {
            ReferenceCountUtil.release(pending.removeFirst());
        }
        pendingBytes = 0;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            if (!Boolean.TRUE.equals(ctx.channel().attr(EVENT_STREAM_ACTIVE).get())) {
                ctx.close();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof IOException) {
            helper.getLogger().log(Level.FINE, "Connection error", cause);
        } else {
            helper.getLogger().log(Level.WARNING, "Unexpected error on HTTP connection", cause);
        }
        failBody(cause);
        ctx.close();
    }

    private void failBody(Throwable cause) {
        NettyServerCall call = activeCall.get();
        if (call != null) {
            call.onDisconnect(cause);
        }
        ChannelInputStream body = currentBody;
        if (body != null) {
            body.fail(cause);
            currentBody = null;
        }
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        DefaultFullHttpResponse response =
                new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
        response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        ctx.writeAndFlush(response).addListener(f -> ctx.close());
    }
}
