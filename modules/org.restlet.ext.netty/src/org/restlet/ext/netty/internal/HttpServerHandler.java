/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.netty.internal;

import static io.netty.handler.codec.http.HttpResponseStatus.CONTINUE;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.IOException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.DecoderResult;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderUtil;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;

import org.restlet.ext.netty.NettyServerHelper;

/**
 * 
 * @author Jerome Louvel
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<Object> {

    private static void appendDecoderResult(HttpObject httpObject) {
        DecoderResult result = httpObject.decoderResult();

        if (result.isSuccess()) {
            return;
        }

        // TODO: ???
    }

    private static void send100Continue(ChannelHandlerContext ctx) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                CONTINUE);
        ctx.write(response);
    }

    private volatile NettyServerCall call;

    private final NettyServerHelper serverHelper;

    public HttpServerHandler(NettyServerHelper serverHelper) {
        this.serverHelper = serverHelper;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) {
        System.out.println("Message received: " + msg);

        try {
            if (msg instanceof HttpRequest) {
                HttpRequest request = (HttpRequest) msg;

                if (HttpHeaderUtil.is100ContinueExpected(request)) {
                    send100Continue(ctx);
                }

                call = new NettyServerCall(getServerHelper().getHelped(), ctx,
                        request);
                serverHelper.handle(call);
                appendDecoderResult(request);
            } else if (msg instanceof HttpContent) {
                HttpContent httpContent = (HttpContent) msg;
                ctx.channel().config().setAutoRead(false);

                if (call != null) {
                    call.onContent(httpContent);
                } else {
                    throw new IOException(
                            "Unexpected error, content arrived before call created");
                }

                if (msg instanceof LastHttpContent) {
                    LastHttpContent trailer = (LastHttpContent) msg;

                    if (!trailer.trailingHeaders().isEmpty()) {
                        // TODO
                    }
                }
            }
        } catch (Throwable e) {
            // TODO
            e.printStackTrace();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    public NettyServerHelper getServerHelper() {
        return serverHelper;
    }

}
