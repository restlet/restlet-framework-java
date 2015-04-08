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

import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.stream.ChunkedStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.logging.Level;

import javax.net.ssl.SSLEngine;

import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Header;
import org.restlet.engine.adapter.ServerCall;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * 
 * @author Jerome Louvel
 */
public class NettyServerCall extends ServerCall {

    private final ChannelHandlerContext nettyContext;

    private volatile HttpContentInputStream nettyEntityStream;

    private final HttpRequest nettyRequest;

    private volatile HttpResponse nettyResponse;

    /** Indicates if the request headers were parsed and added. */
    private volatile boolean requestHeadersAdded;

    public NettyServerCall(Server server, ChannelHandlerContext nettyContext,
            HttpRequest httpRequest) {
        super(server);
        this.nettyContext = nettyContext;
        this.nettyRequest = httpRequest;
        this.nettyResponse = null;
        this.requestHeadersAdded = false;
    }

    @Override
    public boolean abort() {
        try {
            getNettyContext().close().sync();
            return true;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void complete() {
        getNettyContext().flush();
    }

    @Override
    public void flushBuffers() throws IOException {
        getNettyContext().flush();
    }

    @Override
    public String getClientAddress() {
        InetSocketAddress isa = (InetSocketAddress) getNettyContext().channel()
                .remoteAddress();
        return isa.getHostString();
    }

    @Override
    public int getClientPort() {
        InetSocketAddress isa = (InetSocketAddress) getNettyContext().channel()
                .remoteAddress();
        return isa.getPort();
    }

    @Override
    public String getMethod() {
        return getNettyRequest().method().name();
    }

    protected ChannelHandlerContext getNettyContext() {
        return nettyContext;
    }

    protected HttpContentInputStream getNettyEntityStream() {
        if (this.nettyEntityStream == null) {
            this.nettyEntityStream = new HttpContentInputStream(
                    getNettyContext());
        }

        return this.nettyEntityStream;
    }

    protected HttpRequest getNettyRequest() {
        return nettyRequest;
    }

    protected HttpResponse getNettyResponse() {
        return nettyResponse;
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        return getNettyEntityStream();
    }

    @Override
    public Series<Header> getRequestHeaders() {
        final Series<Header> result = super.getRequestHeaders();

        if (!this.requestHeadersAdded) {
            final Iterable<Map.Entry<String, String>> headers = getNettyRequest()
                    .headers();

            for (Map.Entry<String, String> header : headers) {
                result.add(header.getKey(), header.getValue());
            }

            this.requestHeadersAdded = true;
        }

        return result;
    }

    @Override
    public InputStream getRequestHeadStream() {
        return null;
    }

    @Override
    public String getRequestUri() {
        return getNettyRequest().uri();
    }

    @Override
    public OutputStream getResponseEntityStream() {
        return null;
    }

    @Override
    protected SSLEngine getSslEngine() {
        // TODO
        return null;
    }

    @Override
    public String getVersion() {
        String result = null;
        final int index = getNettyRequest().protocolVersion().text()
                .indexOf('/');

        if (index != -1) {
            result = getNettyRequest().protocolVersion().text()
                    .substring(index + 1);
        }

        return result;
    }

    public void onContent(HttpContent httpContent) throws IOException {
        ByteBuf content = httpContent.content();

        if (content.isReadable()) {
            getNettyEntityStream().onContent(content,
                    httpContent instanceof LastHttpContent);
        }
    }

    protected void setNettyResponse(HttpResponse nettyResponse) {
        this.nettyResponse = nettyResponse;
    }

    @Override
    protected void writeResponseBody(Representation responseEntity)
            throws IOException {
        try {
            // Send the entity to the client
            InputStream is = responseEntity.getStream();
            getNettyContext()
                    .write(new HttpChunkedInput(new ChunkedStream(is)));
            getNettyContext().writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        } catch (IOException ioe) {
            // The stream was probably already closed by the
            // connector. Probably OK, low message priority.
            getLogger().log(Level.FINE,
                    "Exception while writing the entity stream.", ioe);
        }
    }

    @Override
    public void writeResponseHead(org.restlet.Response restletResponse)
            throws IOException {
        setNettyResponse(new DefaultHttpResponse(HTTP_1_1,
                new HttpResponseStatus(getStatusCode(), getReasonPhrase())));
        HttpHeaders headers = getNettyResponse().headers();

        // this.response.clear();
        for (Header header : getResponseHeaders()) {
            headers.add(header.getName(), header.getValue());
        }

        // Decide whether to close the connection or not.
        if (isKeepAlive()) {
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            getNettyContext().write(getNettyResponse());
        } else {
            getNettyContext().writeAndFlush(getNettyResponse()).addListener(
                    ChannelFutureListener.CLOSE);
        }
    }

    @Override
    protected void writeResponseTail(Response response) {
        if (!isKeepAlive()) {
            // Close the connection once the content is fully written.
            getNettyContext().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(
                    ChannelFutureListener.CLOSE);
        }
    }

}
