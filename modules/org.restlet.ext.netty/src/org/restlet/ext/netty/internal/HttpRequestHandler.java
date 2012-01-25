/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.netty.internal;

import static org.jboss.netty.buffer.ChannelBuffers.dynamicBuffer;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;

import javax.net.ssl.SSLEngine;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandler;
import org.jboss.netty.channel.ChannelHandlerContext;
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
import org.jboss.netty.handler.stream.ChunkedStream;
import org.restlet.Response;
import org.restlet.data.Parameter;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.http.header.HeaderConstants;
import org.restlet.ext.netty.HttpsServerHelper;
import org.restlet.ext.netty.NettyServerHelper;
import org.restlet.representation.Representation;
import org.restlet.service.ConnectorService;

/**
 * HTTP request handler implementation. Pass HTTP requests to Restlet and gather
 * HTTP response from Restlet and provide it back to the client.
 * 
 * @author Gabriel Ciuloaica (gciuloaica@gmail.com)
 * @author Jerome Louvel
 */
@ChannelHandler.Sharable
public class HttpRequestHandler extends SimpleChannelUpstreamHandler {
    /**
     * Carriage return
     */
    static final byte CR = 13;

    /** Line feed character. */
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
    public void messageReceived(ChannelHandlerContext ctx,
            MessageEvent messageEvent) throws Exception {
        if (clientAddress == null) {
            clientAddress = (InetSocketAddress) messageEvent.getRemoteAddress();
        }

        boolean lastChunk = false;

        if (!readingChunks) {
            request = (HttpRequest) messageEvent.getMessage();

            if (request.isChunked()) {
                readingChunks = true;
            } else {
                content = request.getContent();
            }
        } else {
            HttpChunk chunk = (HttpChunk) messageEvent.getMessage();

            if (chunk.isLast()) {
                readingChunks = false;
                lastChunk = true;
            } else {
                content.writeBytes(chunk.getContent());
            }
        }

        if (content == null) {
            content = ChannelBuffers.dynamicBuffer();
        }

        HttpResponse nettyResponse = null;
        NettyServerCall httpCall = null;

        // Let the Restlet engine handle this call only after last chunk has
        // been read.
        if ((!request.isChunked()) || lastChunk) {
            SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
            SSLEngine sslEngine = sslHandler == null ? null : sslHandler
                    .getEngine();

            httpCall = new NettyServerCall(this.helper.getHelped(),
                    messageEvent, content, request, clientAddress,
                    (this.helper instanceof HttpsServerHelper), sslEngine);
            this.helper.handle(httpCall);
            nettyResponse = httpCall.getResponse();
            Response restletResponse = httpCall.getRestletResponse();

            if (restletResponse != null) {
                // Get the connector service to callback
                Representation responseEntity = restletResponse.getEntity();
                ConnectorService connectorService = ConnectorHelper
                        .getConnectorService();

                if (connectorService != null) {
                    connectorService.beforeSend(responseEntity);
                }

                try {
                    if (nettyResponse != null) {
                        nettyResponse.clearHeaders();
                    } else {
                        HttpResponseStatus status = new HttpResponseStatus(
                                restletResponse.getStatus().getCode(),
                                restletResponse.getStatus().getName());
                        nettyResponse = new DefaultHttpResponse(
                                HttpVersion.HTTP_1_1, status);
                    }

                    // Copy general, response and entity headers
                    for (Parameter header : httpCall.getResponseHeaders()) {
                        nettyResponse.addHeader(header.getName(),
                                header.getValue());
                    }

                    // Check if 'Transfer-Encoding' header should be set
                    if (httpCall.shouldResponseBeChunked(restletResponse)) {
                        nettyResponse.addHeader(
                                HeaderConstants.HEADER_TRANSFER_ENCODING,
                                "chunked");
                    }

                    // Write the response
                    Channel ch = messageEvent.getChannel();
                    ChannelFuture future = null;

                    if (responseEntity != null) {
                        if (nettyResponse.isChunked()) {
                            nettyResponse.setContent(null);
                            future = ch.write(nettyResponse);
                            ch.write(new ChunkedStream(restletResponse
                                    .getEntity().getStream()));
                        } else {
                            ChannelBuffer buf = dynamicBuffer();
                            buf.writeBytes(responseEntity.getStream(),
                                    (int) responseEntity.getAvailableSize());
                            nettyResponse.setContent(buf);
                            future = ch.write(nettyResponse);
                        }
                    }

                    // Close the connection after the write operation is done.
                    if (shouldCloseConnection()) {
                        future.addListener(ChannelFutureListener.CLOSE);
                    }

                } finally {
                    if (responseEntity != null) {
                        responseEntity.release();
                    }

                    if (connectorService != null) {
                        connectorService.afterSend(responseEntity);
                    }
                }
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
        response.setContent(ChannelBuffers.copiedBuffer(
                "Failure: " + status.toString() + "\r\n",
                Charset.forName("UTF-8")));

        // Close the connection as soon as the error message is sent.
        ctx.getChannel().write(response)
                .addListener(ChannelFutureListener.CLOSE);
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
