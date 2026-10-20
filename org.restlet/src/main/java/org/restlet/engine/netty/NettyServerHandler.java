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
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import org.restlet.engine.Engine;

/**
 * Inbound handler that converts Netty HTTP messages into {@link NettyServerCall}s and runs them on
 * the call executor. One instance per HTTP/1.1 connection, or per HTTP/2 or HTTP/3 stream.
 *
 * <p>HTTP/1.1 requests on a connection are processed one at a time: reading is suspended while a
 * call is in progress and resumed when its response is complete. This keeps pipelined requests in
 * order without buffering them.
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

    /** Whether to read the rest of the current HTTP/1.1 request once the channel read completes. */
    private boolean readRequestBody;

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

    private void onRequest(ChannelHandlerContext ctx, HttpRequest request) {
        DecoderResult result = request.decoderResult();
        if (result.isFailure()) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

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
        if (kind != StreamKind.HTTP3) {
            call.setAltSvc(helper.getAltSvc());
        }

        if (!helper.tryAcquireCall()) {
            body.close();
            currentBody = null;
            sendError(ctx, HttpResponseStatus.SERVICE_UNAVAILABLE);
            return;
        }

        try {
            helper.getCallExecutor().execute(() -> runCall(call));
        } catch (RejectedExecutionException _) {
            helper.releaseCall();
            body.close();
            currentBody = null;
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
            // Do not read the next HTTP/1.1 request until the current response is complete; the
            // call requests the next read when it completes.
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
                helper.endCall();
                Engine.clearThreadLocalVariables();
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        failBody(new ClosedChannelException());
        super.channelInactive(ctx);
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
