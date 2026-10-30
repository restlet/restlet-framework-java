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
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderResultProvider;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpStatusClass;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import java.io.IOException;

/**
 * Inbound handler feeding the response of the bound {@link NettyClientCall}. One instance per
 * HTTP/1.1 connection, bound to one call at a time, or one instance per HTTP/2 stream.
 *
 * <p>Reads happen on demand: the call requests the first read after sending its request, then this
 * handler keeps reading as long as the response body consumer keeps up.
 */
class NettyClientHandler extends ChannelInboundHandlerAdapter {

    /** Marks the connections closed because the client connector stops. */
    static final AttributeKey<Boolean> CLIENT_STOPPED =
            AttributeKey.valueOf(NettyClientHandler.class, "clientStopped");

    /** The call in progress, if any. */
    private volatile NettyClientCall call;

    /** Skips the content of an interim (1xx) response. */
    private boolean skippingInterim;

    /**
     * Binds a call to this handler.
     *
     * @param call The call.
     */
    void bind(NettyClientCall call) {
        this.skippingInterim = false;
        this.call = call;
    }

    /** Unbinds the current call. */
    void unbind() {
        this.call = null;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        NettyClientCall current = this.call;
        if (current == null) {
            // Unsolicited data on an idle connection: it can't be reused.
            ReferenceCountUtil.release(msg);
            ctx.close();
            return;
        }

        if (msg instanceof DecoderResultProvider provider && provider.decoderResult().isFailure()) {
            Throwable cause = provider.decoderResult().cause();
            ReferenceCountUtil.release(msg);
            current.onFailure(
                    new IOException("Invalid HTTP response: " + cause.getMessage(), cause));
            ctx.close();
            return;
        }

        if (msg instanceof HttpResponse response) {
            onHead(current, response);
        }

        boolean readMore = true;
        if (msg instanceof HttpContent content) {
            readMore = onContent(current, content);
        } else if (!(msg instanceof HttpResponse)) {
            ReferenceCountUtil.release(msg);
        }

        if (readMore) {
            ctx.read();
        }
    }

    /** Handles a response head, skipping interim (1xx) responses. */
    private void onHead(NettyClientCall current, HttpResponse response) {
        HttpResponseStatus status = response.status();
        skippingInterim =
                status.codeClass() == HttpStatusClass.INFORMATIONAL
                        && status.code() != HttpResponseStatus.SWITCHING_PROTOCOLS.code();
        if (!skippingInterim) {
            current.onResponseHead(response);
        }
    }

    /**
     * Handles response content, whose ownership is transferred.
     *
     * @return True if the event loop should read more content now.
     */
    private boolean onContent(NettyClientCall current, HttpContent content) {
        boolean last = content instanceof LastHttpContent;
        if (skippingInterim) {
            content.release();
            if (last) {
                skippingInterim = false;
            }
            return true;
        }
        boolean readMore = current.onResponseContent(content.content());
        if (last) {
            current.onResponseComplete(((LastHttpContent) content).trailingHeaders());
            return false;
        }
        return readMore;
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyClientCall current = this.call;
        if (current != null) {
            current.onFailure(
                    new IOException(
                            isClientStopped(ctx.channel())
                                    ? "Client stopped"
                                    : "Connection closed by peer"));
        }
        super.channelInactive(ctx);
    }

    /** Indicates whether a channel, or its parent connection, was closed by the client's stop. */
    private static boolean isClientStopped(Channel channel) {
        for (Channel c = channel; c != null; c = c.parent()) {
            if (Boolean.TRUE.equals(c.attr(CLIENT_STOPPED).get())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        NettyClientCall current = this.call;
        if (current != null) {
            current.onFailure(cause);
        }
        ctx.close();
    }
}
