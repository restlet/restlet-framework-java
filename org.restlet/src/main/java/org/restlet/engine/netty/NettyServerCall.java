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
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.ssl.SslHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.restlet.data.Header;
import org.restlet.engine.adapter.ServerCall;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.ssl.SslUtils;
import org.restlet.util.Series;

/**
 * Server call bridging a Netty HTTP exchange (HTTP/1.1, or one HTTP/2 stream converted to HTTP/1
 * objects) to the Restlet engine. The request body is exposed as a blocking stream fed by the event
 * loop; the response body is written through a blocking stream with back-pressure.
 *
 * @author Jerome Louvel
 */
class NettyServerCall extends ServerCall {

    /** The channel carrying this exchange (a child stream channel for HTTP/2 and HTTP/3). */
    private final Channel channel;

    /** The parent connection channel, used for addresses and TLS information. */
    private final Channel connection;

    /** The kind of channel carrying the exchange. */
    private final StreamKind kind;

    /** Indicates whether the exchange is carried by a stream of a multiplexed connection. */
    private final boolean http2;

    /** The Netty request head. */
    private final HttpRequest request;

    /** The request body stream fed by the event loop. */
    private final ChannelInputStream requestBody;

    /** The response output buffer size. */
    private final int outputBufferSize;

    /** The write timeout in milliseconds. */
    private final long writeTimeoutMs;

    /** Indicates whether the request headers were copied. */
    private volatile boolean requestHeadersAdded;

    /** Indicates whether the response head was written. */
    private volatile boolean responseHeadWritten;

    /** Indicates whether the exchange was completed. */
    private volatile boolean completed;

    /** The response body stream, if any. */
    private volatile ChannelOutputStream responseBody;

    /** The "Alt-Svc" header value advertising HTTP/3, or null. */
    private volatile String altSvc;

    /**
     * Constructor.
     *
     * @param helper The parent server helper, providing the server and the output settings.
     * @param channel The channel carrying the exchange.
     * @param kind The kind of channel carrying the exchange.
     * @param request The Netty request head.
     * @param requestBody The request body stream.
     * @param confidential Whether the connection is secured with TLS.
     */
    NettyServerCall(
            NettyServerHelper helper,
            Channel channel,
            StreamKind kind,
            HttpRequest request,
            ChannelInputStream requestBody,
            boolean confidential) {
        super(helper.getHelped());
        this.channel = channel;
        this.kind = kind;
        this.http2 = kind.isStream();
        this.connection = this.http2 ? channel.parent() : channel;
        this.request = request;
        this.requestBody = requestBody;
        this.outputBufferSize = helper.getHttpOutputBufferSize();
        this.writeTimeoutMs = helper.getConnectorIdleTimeout();
        setConfidential(confidential);
        setMethod(request.method().name());
        setRequestUri(request.uri());
        setVersion(kind.version());

        InetSocketAddress remote = toInet(remoteAddress());
        if (remote != null) {
            setClientAddress(remote.getAddress().getHostAddress());
            setClientPort(remote.getPort());
        }
        InetSocketAddress local = toInet(localAddress());
        if (local != null) {
            setServerAddress(local.getAddress().getHostAddress());
            setServerPort(local.getPort());
        }
    }

    /** The UDP address of a QUIC connection, or the TCP address of other connections. */
    private SocketAddress remoteAddress() {
        return (kind == StreamKind.HTTP3)
                ? Http3Support.remoteSocketAddress(connection)
                : connection.remoteAddress();
    }

    private SocketAddress localAddress() {
        return (kind == StreamKind.HTTP3)
                ? Http3Support.localSocketAddress(connection)
                : connection.localAddress();
    }

    private static InetSocketAddress toInet(SocketAddress address) {
        return (address instanceof InetSocketAddress inet) ? inet : null;
    }

    /**
     * Sets the "Alt-Svc" header value advertising HTTP/3, added to the response unless the
     * application sets one.
     *
     * @param altSvc The "Alt-Svc" header value, or null.
     */
    void setAltSvc(String altSvc) {
        this.altSvc = altSvc;
    }

    @Override
    public boolean abort() {
        connection.close();
        return true;
    }

    @Override
    public void complete() {
        if (completed) {
            return;
        }
        completed = true;

        try {
            if (!responseHeadWritten) {
                writeHead(true);
            }
            if (responseBody != null) {
                responseBody.close();
            }
        } catch (IOException e) {
            getLogger().fine("Unable to complete the response: " + e.getMessage());
        }

        if (!channel.isActive()) {
            requestBody.close();
            return;
        }

        boolean keepAlive = HttpUtil.isKeepAlive(request) && requestBody.isFullyConsumed();
        ChannelFuture future = channel.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT);
        requestBody.close();

        if (!http2 && !keepAlive) {
            future.addListener(ChannelFutureListener.CLOSE);
        } else if (!http2) {
            // Ready for the next request on this connection.
            future.addListener(f -> channel.read());
        }
    }

    @Override
    public void flushBuffers() throws IOException {
        if (responseBody != null) {
            responseBody.flush();
        } else {
            channel.flush();
        }
    }

    @Override
    public List<Certificate> getCertificates() {
        SSLSession session = sslSession();
        if (session != null) {
            try {
                return Arrays.asList(session.getPeerCertificates());
            } catch (SSLPeerUnverifiedException _) {
                return List.of();
            }
        }
        return List.of();
    }

    @Override
    public String getCipherSuite() {
        SSLSession session = sslSession();
        return (session != null) ? session.getCipherSuite() : null;
    }

    @Override
    public InputStream getRequestEntityStream(long size) {
        return requestBody;
    }

    @Override
    public Series<Header> getRequestHeaders() {
        final Series<Header> result = super.getRequestHeaders();

        if (!this.requestHeadersAdded) {
            for (Map.Entry<String, String> header : request.headers()) {
                String name = header.getKey();
                // HTTP/2 pseudo-headers are not HTTP headers.
                if (!name.startsWith(":")) {
                    result.add(name, header.getValue());
                }
            }
            // HTTP/2 carries the host in the :authority pseudo-header, which the codec maps to a
            // regular "host" header only when absent; make sure Restlet sees a Host header.
            if (result.getFirstValue(HeaderConstants.HEADER_HOST, true) == null) {
                String authority = request.headers().get(":authority");
                if (authority != null) {
                    result.add(HeaderConstants.HEADER_HOST, authority);
                }
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
    public OutputStream getResponseEntityStream() {
        if (responseBody == null) {
            responseBody =
                    new ChannelOutputStream(
                            channel, DefaultHttpContent::new, outputBufferSize, writeTimeoutMs);
        }
        return responseBody;
    }

    @Override
    public Integer getSslKeySize() {
        String cipherSuite = getCipherSuite();
        return (cipherSuite != null) ? SslUtils.extractKeySize(cipherSuite) : null;
    }

    @Override
    protected byte[] getSslSessionIdBytes() {
        SSLSession session = sslSession();
        return (session != null) ? session.getId() : null;
    }

    @Override
    public String getSslSessionId() {
        byte[] id = getSslSessionIdBytes();
        return (id != null && id.length > 0) ? HexFormat.of().formatHex(id) : null;
    }

    @Override
    public boolean isConnectionBroken(Throwable exception) {
        return (exception instanceof ClosedChannelException)
                || !channel.isActive()
                || super.isConnectionBroken(exception);
    }

    @Override
    public void sendResponse(org.restlet.Response response) throws IOException {
        if (!channel.isActive()) {
            throw new IOException("Connection closed by peer");
        }

        boolean hasEntity = (response != null) && (response.getEntity() != null);
        writeHead(!hasEntity);
        super.sendResponse(response);
    }

    /**
     * Writes the response head. Must be called once, before any body byte.
     *
     * @param emptyBody True if no body follows.
     */
    private void writeHead(boolean emptyBody) {
        if (responseHeadWritten) {
            return;
        }
        responseHeadWritten = true;

        HttpResponseStatus status =
                (getReasonPhrase() != null && !getReasonPhrase().isEmpty())
                        ? new HttpResponseStatus(getStatusCode(), getReasonPhrase())
                        : HttpResponseStatus.valueOf(getStatusCode());
        HttpResponse head = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);

        for (Header header : getResponseHeaders()) {
            head.headers().add(header.getName(), header.getValue());
        }

        if (altSvc != null && !head.headers().contains("Alt-Svc")) {
            head.headers().set("Alt-Svc", altSvc);
        }

        boolean hasLength = head.headers().contains(HttpHeaderNames.CONTENT_LENGTH);
        boolean noBodyStatus =
                getStatusCode() == 204 || getStatusCode() == 304 || getStatusCode() < 200;

        if (http2) {
            // Connection-specific headers are forbidden in HTTP/2.
            head.headers().remove(HttpHeaderNames.CONNECTION);
            head.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
            head.headers().remove("Keep-Alive");
        } else if (emptyBody) {
            if (!hasLength && !noBodyStatus && !"HEAD".equals(getMethod())) {
                head.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
            }
        } else if (!hasLength) {
            head.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
        }

        if (!http2 && !HttpUtil.isKeepAlive(request)) {
            head.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
        }

        channel.write(head);
    }

    private SSLSession sslSession() {
        if (kind == StreamKind.HTTP3) {
            return Http3Support.sslSession(connection);
        }
        SslHandler ssl = connection.pipeline().get(SslHandler.class);
        return (ssl != null) ? ssl.engine().getSession() : null;
    }
}
