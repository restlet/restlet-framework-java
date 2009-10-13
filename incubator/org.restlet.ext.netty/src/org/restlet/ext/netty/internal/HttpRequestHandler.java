/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.netty.internal;

import javax.net.ssl.SSLEngine;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelPipelineCoverage;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.frame.TooLongFrameException;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpChunk;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.ssl.SslHandler;
import org.restlet.ext.netty.HttpsNettyServerHelper;
import org.restlet.ext.netty.NettyServerHelper;

/**
 * HTTP request handler implementation. Pass HTTP requests to Restlet and gather
 * HTTP response from Restlet and provide it back to the client.
 * 
 * @author Gabriel Ciuloaica (gciuloaica@gmail.com)
 */
@ChannelPipelineCoverage("one")
public class HttpRequestHandler extends SimpleChannelUpstreamHandler {

    /** The server helper. */
    private volatile NettyServerHelper helper;

    /** Indicates if chunked encoding should be read. */
    private volatile boolean readingChunks;

    /** The Netty HTTP request. */
    private volatile HttpRequest request;

    /**
     * Constructor. Creates a new handler instance wrapping server helper.
     * 
     * @param serverHelper
     *            The server helper.
     */
    public HttpRequestHandler(NettyServerHelper serverHelper) {
        this.helper = serverHelper;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
            throws Exception {
        Channel ch = e.getChannel();
        Throwable cause = e.getCause();
        if (cause instanceof TooLongFrameException) {
            sendError(ctx, HttpResponseStatus.BAD_REQUEST);
            return;
        }

        cause.printStackTrace();
        if (ch.isConnected()) {
            sendError(ctx, HttpResponseStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
            throws Exception {

        boolean close = false;
        boolean isLastChunk = false;
        ChannelBuffer content = null;
        if (!readingChunks) {
            request = (HttpRequest) e.getMessage();

            if (request.isChunked()) {
                readingChunks = true;
            } else {
                content = request.getContent();
            }
        } else {
            HttpChunk chunk = (HttpChunk) e.getMessage();
            if (chunk.isLast()) {
                readingChunks = false;
                isLastChunk = true;
                close = true;
            }
            content = chunk.getContent();

        }

        // Decide whether to close the connection or not.
        close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request
                .getHeader(HttpHeaders.Names.CONNECTION))
                || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request
                        .getHeader(HttpHeaders.Names.CONNECTION));

        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                HttpResponseStatus.OK);

        if ((content != null) && (!isLastChunk)) {

            SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
            SSLEngine sslEngine = sslHandler == null ? null : sslHandler
                    .getEngine();

            NettyServerCall httpCall = new NettyServerCall(this.helper
                    .getHelped(), content, request, response,
                    (this.helper instanceof HttpsNettyServerHelper), sslEngine);
            this.helper.handle(httpCall);
        }
        Channel ch = e.getChannel();

        if (!close) {
            response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String
                    .valueOf(-1));

        }
        // Close the connection after the write operation is done if
        // necessary.
        if (request.isChunked()) {
            if (isLastChunk) {
                ChannelFuture future = ch.close();

                if (close) {
                    future.addListener(ChannelFutureListener.CLOSE);
                }
            } else {
                if (e.getMessage() instanceof HttpChunk) {
                    ch.write(response.getContent());
                } else {
                    ch.write(response);
                }
            }

        } else {
            ChannelFuture future = ch.write(response);
            future.addListener(ChannelFutureListener.CLOSE);

        }

    }

    /**
     * Sends an error to the client.
     * 
     * @param ctx
     *            The handler context.
     * @param status
     *            The HTTP status.
     */
    private void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1,
                status);
        response.setHeader(HttpHeaders.Names.CONTENT_TYPE,
                "text/plain; charset=UTF-8");
        response.setContent(ChannelBuffers.copiedBuffer("Failure: "
                + status.toString() + "\r\n", "UTF-8"));

        // Close the connection as soon as the error message is sent.
        ctx.getChannel().write(response).addListener(
                ChannelFutureListener.CLOSE);
    }
}
