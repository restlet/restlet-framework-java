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

import java.net.InetSocketAddress;
import java.util.Arrays;

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
import org.restlet.ext.netty.HttpsServerHelper;
import org.restlet.ext.netty.NettyServerHelper;

/**
 * HTTP request handler implementation. Pass HTTP requests to Restlet and gather
 * HTTP response from Restlet and provide it back to the client.
 * 
 * @author Gabriel Ciuloaica (gciuloaica@gmail.com)
 */
@ChannelPipelineCoverage("one")
public class HttpRequestHandler extends SimpleChannelUpstreamHandler {
    /**
     * Carriage return
     */
    static final byte CR = 13;

    /**
     * Line feed character
     */
    static final byte LF = 10;

    /** The server helper. */
    private volatile NettyServerHelper helper;

    /** Indicates if chunked encoding should be read. */
    private volatile boolean readingChunks;

    /** The Netty HTTP request. */
    private volatile HttpRequest request;

    /** Content accumulator. */
    private volatile ChannelBuffer content;

    /** Client address. */
    private volatile InetSocketAddress clientAddress;

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

        if (clientAddress == null) {
            clientAddress = (InetSocketAddress) e.getRemoteAddress();
        }

        boolean isLastChunk = false;

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
            }

            long chunkSize = chunk.getContent().readableBytes();

            content.writeBytes(longToHex(chunkSize));
            content.writeByte(CR);
            content.writeByte(LF);

            content.writeBytes(chunk.getContent());
            content.writeByte(CR);
            content.writeByte(LF);

        }

        if (content == null) {
            content = ChannelBuffers.dynamicBuffer();
        }

        HttpResponse response = null;

        // let restlet engine to handle this call only after last chunk has been
        // read.
        if ((!request.isChunked()) || isLastChunk) {

            SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
            SSLEngine sslEngine = sslHandler == null ? null : sslHandler
                    .getEngine();

            NettyServerCall httpCall = new NettyServerCall(this.helper
                    .getHelped(), content, request, clientAddress,
                    (this.helper instanceof HttpsServerHelper), sslEngine);
            this.helper.handle(httpCall);
            response = httpCall.getResponse();

        }

        Channel ch = e.getChannel();

        // Close the connection after the write operation is done.
        if (request.isChunked()) {
            if (isLastChunk) {
                ChannelFuture future = ch.write(response);
                future.addListener(ChannelFutureListener.CLOSE);

            }

        } else {
            ChannelFuture future = ch.write(response);
            if (shouldCloseConnection()) {
                future.addListener(ChannelFutureListener.CLOSE);
            }

        }

    }

    private boolean shouldCloseConnection() {
        boolean close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request
                .getHeader(HttpHeaders.Names.CONNECTION))
                || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request
                        .getHeader(HttpHeaders.Names.CONNECTION));
        return close;
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

    /**
     * Convert a long value to hex byte array.
     * 
     * @param l
     *            - value to be converted
     * @return hex representation
     */
    public static byte[] longToHex(final long l) {
        long v = l & 0xFFFFFFFFFFFFFFFFL;

        byte[] result = new byte[16];
        Arrays.fill(result, 0, result.length, (byte) 0);

        for (int i = 0; i < result.length; i += 2) {
            byte b = (byte) ((v & 0xFF00000000000000L) >> 56);

            byte b2 = (byte) (b & 0x0F);
            byte b1 = (byte) ((b >> 4) & 0x0F);

            if (b1 > 9)
                b1 += 39;
            b1 += 48;

            if (b2 > 9)
                b2 += 39;
            b2 += 48;

            result[i] = (byte) (b1 & 0xFF);
            result[i + 1] = (byte) (b2 & 0xFF);

            v <<= 8;
        }

        return result;
    }
}
