/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import static org.restlet.engine.header.HeaderConstants.ATTRIBUTE_HEADERS;
import static org.restlet.engine.header.HeaderConstants.ATTRIBUTE_HTTPS_KEY_SIZE;
import static org.restlet.engine.header.HeaderConstants.ATTRIBUTE_HTTPS_SSL_SESSION_ID;
import static org.restlet.engine.header.HeaderConstants.ATTRIBUTE_VERSION;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.DefaultLastHttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
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
import java.io.PushbackInputStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ClosedChannelException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.header.HeaderUtils;
import org.restlet.engine.ssl.SslUtils;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.InputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.service.ConnectorService;
import org.restlet.util.Series;

/**
 * Server exchange bridging a Netty HTTP request (HTTP/1.1, or one HTTP/2 or HTTP/3 stream converted
 * to HTTP/1 objects) to a Restlet {@link org.restlet.Request} and {@link Response}. The request
 * body is exposed as a blocking stream fed by the event loop; the response body is written through
 * a blocking stream with back-pressure.
 *
 * @author Jerome Louvel
 */
class NettyServerCall {

    /** The parent helper. */
    private final NettyServerHelper helper;

    /** The channel carrying this exchange (a child stream channel for HTTP/2 and HTTP/3). */
    private final Channel channel;

    /** The parent connection channel, used for addresses and TLS information. */
    private final Channel connection;

    /** The kind of channel carrying the exchange. */
    private final StreamKind kind;

    /** The Netty request head. */
    private final HttpRequest request;

    /** The request body stream fed by the event loop. */
    private final ChannelInputStream requestBody;

    /** Indicates whether the connection is secured with TLS. */
    private final boolean confidential;

    /** The request headers, copied once from the Netty request head. */
    private final Series<Header> requestHeaders;

    /** The response headers. */
    private final Series<Header> responseHeaders = new Series<>(Header.class);

    /** The trailing headers received with the request entity. */
    private final Series<Header> requestTrailers = new Series<>(Header.class);

    /** The trailing headers to send with the response entity. */
    private Series<Header> responseTrailers = new Series<>(Header.class);

    /** Indicates whether response trailers are valid for the response being sent. */
    private boolean responseTrailersEnabled;

    /** The response status code. */
    private volatile int statusCode = 200;

    /** The response reason phrase. */
    private volatile String reasonPhrase;

    /** Indicates whether the response head was written. */
    private volatile boolean responseHeadWritten;

    /** Indicates whether the exchange was completed. */
    private volatile boolean completed;

    /** The response body stream, if any. */
    private volatile ChannelOutputStream responseBody;

    /** Whether this call holds an open SSE stream permit. */
    private boolean eventStreamPermit;

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
        this.helper = helper;
        this.channel = channel;
        this.kind = kind;
        this.connection = kind.isStream() ? channel.parent() : channel;
        this.request = request;
        this.requestBody = requestBody;
        this.confidential = confidential;
        this.requestHeaders = copyHeaders(request, kind.isStream());
    }

    /**
     * Copies the Netty request headers, except the HTTP/2 pseudo-headers. HTTP/2 and HTTP/3 carry
     * the host in the ":authority" pseudo-header, mapped to a "Host" header when absent.
     */
    private static Series<Header> copyHeaders(HttpRequest request, boolean lowerCaseNames) {
        Series<Header> result = new Series<>(Header.class);
        for (Map.Entry<String, String> header : request.headers()) {
            if (!header.getKey().startsWith(":")) {
                String name =
                        lowerCaseNames ? header.getKey().toLowerCase(Locale.ROOT) : header.getKey();
                result.add(name, header.getValue());
            }
        }
        if (result.getFirstValue(HeaderConstants.HEADER_HOST, true) == null) {
            String authority = request.headers().get(":authority");
            if (authority != null) {
                result.add(
                        lowerCaseNames
                                ? HeaderConstants.HEADER_HOST.toLowerCase(Locale.ROOT)
                                : HeaderConstants.HEADER_HOST,
                        authority);
            }
        }
        return result;
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

    private Logger getLogger() {
        return helper.getLogger();
    }

    // ------------------------------------------------------------------ request side

    /**
     * Creates the Restlet request of this exchange.
     *
     * @param context The context of the server connector.
     * @return The Restlet request.
     */
    NettyRequest toRequest(Context context) {
        NettyRequest result = new NettyRequest(context, this);
        result.getAttributes().put(ATTRIBUTE_HEADERS, requestHeaders);
        result.getAttributes().put(ATTRIBUTE_VERSION, kind.version());

        if (confidential) {
            List<Certificate> certificates = getCertificates();
            if (!certificates.isEmpty()) {
                result.getClientInfo().setCertificates(certificates);
            }

            String cipherSuite = getCipherSuite();
            if (cipherSuite != null) {
                result.getClientInfo().setCipherSuite(cipherSuite);
                Integer keySize = SslUtils.extractKeySize(cipherSuite);
                if (keySize != null) {
                    result.getAttributes().put(ATTRIBUTE_HTTPS_KEY_SIZE, keySize);
                }
            }

            String sslSessionId = getSslSessionId();
            if (sslSessionId != null) {
                result.getAttributes().put(ATTRIBUTE_HTTPS_SSL_SESSION_ID, sslSessionId);
            }
        }

        return result;
    }

    /**
     * Creates the Restlet response of this exchange.
     *
     * @param request The Restlet request.
     * @return The Restlet response.
     */
    Response toResponse(NettyRequest request) {
        Response result = new Response(request);
        InetSocketAddress local = getLocalAddress();
        if (local != null) {
            result.getServerInfo().setAddress(local.getAddress().getHostAddress());
            result.getServerInfo().setPort(local.getPort());
        }
        result.getServerInfo().setAgent(Engine.VERSION_HEADER);
        return result;
    }

    /** The request method name. */
    String getMethod() {
        return request.method().name();
    }

    /** The request target, "/" when empty. */
    String getRequestUri() {
        String uri = request.uri();
        return (uri == null || uri.isEmpty()) ? "/" : uri;
    }

    /** The request headers. */
    Series<Header> getRequestHeaders() {
        return requestHeaders;
    }

    /** The trailing headers received with the request entity. */
    Series<Header> getRequestTrailers() {
        return requestTrailers;
    }

    /** Copies the request trailing headers before the body stream is ended. */
    void setRequestTrailers(HttpHeaders trailers) {
        for (Map.Entry<String, String> trailer : trailers) {
            String name =
                    kind.isStream() ? trailer.getKey().toLowerCase(Locale.ROOT) : trailer.getKey();
            requestTrailers.add(name, trailer.getValue());
        }
    }

    /** The exact protocol (HTTP or HTTPS). */
    Protocol getProtocol() {
        return confidential ? Protocol.HTTPS : Protocol.HTTP;
    }

    /** The remote address, or null. */
    InetSocketAddress getRemoteAddress() {
        SocketAddress address =
                (kind == StreamKind.HTTP3)
                        ? Http3Support.remoteSocketAddress(connection)
                        : connection.remoteAddress();
        return (address instanceof InetSocketAddress inet) ? inet : null;
    }

    /** The local address, or null. */
    InetSocketAddress getLocalAddress() {
        SocketAddress address =
                (kind == StreamKind.HTTP3)
                        ? Http3Support.localSocketAddress(connection)
                        : connection.localAddress();
        return (address instanceof InetSocketAddress inet) ? inet : null;
    }

    /**
     * Returns the request entity, if the request declares a length, a chunked transfer coding, or a
     * connection close (in which case the stream is peeked to detect a body).
     *
     * @return The request entity.
     */
    Representation getRequestEntity() {
        long contentLength = HeaderUtils.getContentLength(requestHeaders);
        boolean chunked = HeaderUtils.isChunkedEncoding(requestHeaders);
        boolean connectionClosed = HeaderUtils.isConnectionClose(requestHeaders);
        Representation result = null;

        if ((contentLength != Representation.UNKNOWN_SIZE && contentLength != 0) || chunked) {
            result = new InputRepresentation(requestBody, null, contentLength);
        } else if (connectionClosed) {
            InputStream stream = peek(requestBody);
            if (stream != null) {
                result = new InputRepresentation(stream, null, contentLength);
            }
        }

        if (result == null) {
            result = new EmptyRepresentation();
        }
        return HeaderUtils.extractEntityHeaders(requestHeaders, result);
    }

    /** Returns a stream equivalent to the given one, or null if it is empty. */
    private InputStream peek(InputStream stream) {
        PushbackInputStream result = new PushbackInputStream(stream);
        try {
            int next = result.read();
            if (next != -1) {
                result.unread(next);
                return result;
            }
        } catch (IOException e) {
            getLogger().log(Level.FINE, "Unable to read the request entity", e);
        }
        return null;
    }

    // ------------------------------------------------------------------ response side

    /**
     * Commits the Restlet response: adds the response headers and sends the head and the entity.
     *
     * @param response The Restlet response.
     */
    void commit(Response response) {
        try {
            responseTrailers = response.getTrailers();
            if (isEventStream(response.getEntity())) {
                if (helper.tryAcquireEventStream()) {
                    eventStreamPermit = true;
                    channel.attr(NettyServerHandler.EVENT_STREAM_ACTIVE).set(true);
                } else {
                    response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
                    response.setEntity(null);
                }
            }
            prepareEntity(response);
            addResponseHeaders(response);
            sendResponse(response);
        } catch (IOException | RuntimeException e) {
            handleException(response, e);
        } finally {
            complete();
            if (response.getOnSent() != null) {
                response.getOnSent().handle(response.getRequest(), response);
            }
        }
    }

    private static boolean isEventStream(Representation entity) {
        return entity != null
                && entity.getMediaType() != null
                && MediaType.TEXT_EVENT_STREAM.includes(entity.getMediaType());
    }

    /** Adds the entity headers and drops the entity when the status or method forbids it. */
    private void prepareEntity(Response response) {
        Status status = response.getStatus();
        Method method = response.getRequest().getMethod();

        if (Method.HEAD.equals(method)) {
            HeaderUtils.addEntityHeaders(response.getEntity(), responseHeaders);
            response.setEntity(null);
        } else if (Method.GET.equals(method)
                && Status.SUCCESS_OK.equals(status)
                && !response.isEntityAvailable()) {
            HeaderUtils.addEntityHeaders(response.getEntity(), responseHeaders);
            getLogger()
                    .log(
                            Level.WARNING,
                            "A response with a 200 (Ok) status should have an entity. Make sure"
                                    + " that resource \"{0}\" returns one or sets the status to"
                                    + " 204 (No content).",
                            response.getRequest().getResourceRef());
        } else if (Status.SUCCESS_NO_CONTENT.equals(status)) {
            HeaderUtils.addEntityHeaders(response.getEntity(), responseHeaders);
            dropEntity(response, Level.FINE, "204 (No content)");
        } else if (Status.REDIRECTION_NOT_MODIFIED.equals(status)) {
            if (response.getEntity() != null) {
                HeaderUtils.addNotModifiedEntityHeaders(response.getEntity(), responseHeaders);
                response.setEntity(null);
            }
        } else if (Status.SUCCESS_RESET_CONTENT.equals(status)) {
            dropEntity(response, Level.WARNING, "205 (Reset content)");
        } else if (status.isInformational()) {
            dropEntity(response, Level.WARNING, "informational (1xx)");
        } else {
            HeaderUtils.addEntityHeaders(response.getEntity(), responseHeaders);
            if (!response.isEntityAvailable()) {
                if (response.getEntity() != null && response.getEntity().getSize() != 0) {
                    getLogger()
                            .log(
                                    Level.WARNING,
                                    "A response with an unavailable and potentially non empty"
                                            + " entity was returned. Ignoring the entity for"
                                            + " resource \"{0}\".",
                                    response.getRequest().getResourceRef());
                }
                response.setEntity(null);
            }
        }
    }

    private void dropEntity(Response response, Level level, String status) {
        if (response.isEntityAvailable()) {
            getLogger()
                    .log(
                            level,
                            "Responses with a {0} status can''t have an entity. Ignoring the"
                                    + " entity for resource \"{1}\".",
                            new Object[] {status, response.getRequest().getResourceRef()});
        }
        response.setEntity(null);
    }

    private void addResponseHeaders(Response response) {
        try {
            HeaderUtils.addGeneralHeaders(response, responseHeaders);
            HeaderUtils.addResponseHeaders(response, responseHeaders);
            if (response.getStatus() != null) {
                setStatus(response.getStatus().getCode(), response.getStatus().getReasonPhrase());
            }
        } catch (RuntimeException e) {
            getLogger().log(Level.WARNING, "Unable to add the response headers", e);
            setStatus(
                    Status.SERVER_ERROR_INTERNAL.getCode(),
                    Status.SERVER_ERROR_INTERNAL.getReasonPhrase());
        }
    }

    private void setStatus(int code, String reason) {
        this.statusCode = code;
        this.reasonPhrase = reason;
    }

    private void handleException(Response response, Exception exception) {
        if (isConnectionBroken(exception)) {
            getLogger()
                    .log(
                            Level.INFO,
                            "The connection was broken. It was probably closed by the client."
                                    + " Reason: {0}",
                            exception.getMessage());
        } else if (!responseHeadWritten) {
            getLogger()
                    .log(
                            Level.SEVERE,
                            "An exception occurred writing the response entity",
                            exception);
            setStatus(
                    Status.SERVER_ERROR_INTERNAL.getCode(),
                    "An exception occurred writing the response entity");
            response.setEntity(null);
            try {
                sendResponse(response);
            } catch (IOException ioe) {
                getLogger().log(Level.WARNING, "Unable to send the error response", ioe);
            }
        } else {
            // The head is already on the wire: the response can only be cut short.
            getLogger()
                    .log(
                            Level.SEVERE,
                            "An exception occurred writing the response entity",
                            exception);
            abort();
        }
    }

    /**
     * Sends the response head and entity.
     *
     * @param response The Restlet response.
     * @throws IOException If the response could not be written.
     */
    private void sendResponse(Response response) throws IOException {
        if (!channel.isActive()) {
            throw new IOException("Connection closed by peer");
        }

        Representation entity = response.getEntity();
        ConnectorService connectorService = ConnectorHelper.getConnectorService();
        if (connectorService != null) {
            connectorService.beforeSend(entity);
        }

        try {
            writeHead(entity == null);
            if (entity != null) {
                OutputStream out = getResponseEntityStream();
                entity.write(out);
                out.flush();
                out.close();
            }
        } finally {
            if (entity != null) {
                entity.release();
            }
            if (connectorService != null) {
                connectorService.afterSend(entity);
            }
        }
    }

    private OutputStream getResponseEntityStream() {
        if (responseBody == null) {
            responseBody =
                    new ChannelOutputStream(
                            channel,
                            DefaultHttpContent::new,
                            helper.getHttpOutputBufferSize(),
                            helper.getConnectorIdleTimeout());
        }
        return responseBody;
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

        int code = statusCode;
        String reason = reasonPhrase;
        HttpResponseStatus status =
                (reason != null && !reason.isEmpty())
                        ? new HttpResponseStatus(code, reason)
                        : HttpResponseStatus.valueOf(code);
        HttpResponse head = new DefaultHttpResponse(HttpVersion.HTTP_1_1, status);

        for (Header header : responseHeaders) {
            String name =
                    kind.isStream() ? header.getName().toLowerCase(Locale.ROOT) : header.getName();
            head.headers().add(name, header.getValue());
        }

        if (altSvc != null && !head.headers().contains("Alt-Svc")) {
            head.headers().set(kind.isStream() ? "alt-svc" : "Alt-Svc", altSvc);
        }

        boolean hasTrailers = !responseTrailers.isEmpty();
        boolean hasLength = head.headers().contains(HttpHeaderNames.CONTENT_LENGTH);
        boolean noBodyStatus = code == 204 || code == 304 || code < 200;

        if (noBodyStatus || "HEAD".equals(getMethod())) {
            hasTrailers = false;
        }
        responseTrailersEnabled =
                hasTrailers || (kind.isStream() && !noBodyStatus && !"HEAD".equals(getMethod()));

        if (kind.isStream()) {
            // Connection-specific headers are forbidden in HTTP/2 and HTTP/3.
            head.headers().remove(HttpHeaderNames.CONNECTION);
            head.headers().remove(HttpHeaderNames.TRANSFER_ENCODING);
            head.headers().remove("Keep-Alive");
        } else {
            if (emptyBody) {
                if (hasTrailers) {
                    head.headers().remove(HttpHeaderNames.CONTENT_LENGTH);
                    HttpUtil.setTransferEncodingChunked(head, true);
                } else if (!hasLength && !noBodyStatus && !"HEAD".equals(getMethod())) {
                    head.headers().set(HttpHeaderNames.CONTENT_LENGTH, 0);
                }
            } else if (hasTrailers) {
                head.headers().remove(HttpHeaderNames.CONTENT_LENGTH);
                HttpUtil.setTransferEncodingChunked(head, true);
            } else if (!hasLength) {
                head.headers().set(HttpHeaderNames.TRANSFER_ENCODING, HttpHeaderValues.CHUNKED);
            }
            if (hasTrailers) {
                setTrailerNames(head.headers(), responseTrailers);
            }
            if (!HttpUtil.isKeepAlive(request)) {
                head.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.CLOSE);
            }
        }

        channel.write(head);
    }

    /** Completes the exchange: ends the response and frees the connection for the next request. */
    void complete() {
        if (completed) {
            return;
        }
        completed = true;

        try {
            try {
                if (!responseHeadWritten) {
                    writeHead(true);
                }
                if (responseBody != null) {
                    responseBody.close();
                }
            } catch (IOException e) {
                getLogger().log(Level.FINE, "Unable to complete the response: {0}", e.getMessage());
            }

            if (!channel.isActive()) {
                requestBody.close();
                return;
            }

            boolean keepAlive = HttpUtil.isKeepAlive(request) && requestBody.isFullyConsumed();
            LastHttpContent lastContent = LastHttpContent.EMPTY_LAST_CONTENT;
            if (responseTrailersEnabled) {
                DefaultLastHttpContent withTrailers =
                        new DefaultLastHttpContent(Unpooled.EMPTY_BUFFER);
                copyHeaders(responseTrailers, withTrailers.trailingHeaders(), kind.isStream());
                lastContent = withTrailers;
            }
            ChannelFuture future = channel.writeAndFlush(lastContent);
            requestBody.close();

            if (!kind.isStream()) {
                if (keepAlive) {
                    // Ready for the next request on this connection.
                    future.addListener(f -> channel.read());
                } else {
                    future.addListener(ChannelFutureListener.CLOSE);
                }
            }
        } finally {
            if (eventStreamPermit) {
                channel.attr(NettyServerHandler.EVENT_STREAM_ACTIVE).set(false);
                helper.releaseEventStream();
                eventStreamPermit = false;
            }
        }
    }

    static void setTrailerNames(
            io.netty.handler.codec.http.HttpHeaders headers, Series<Header> trailers) {
        String names =
                trailers.stream()
                        .map(Header::getName)
                        .distinct()
                        .collect(java.util.stream.Collectors.joining(", "));
        headers.set("Trailer", names);
    }

    private static void copyHeaders(
            Series<Header> source,
            io.netty.handler.codec.http.HttpHeaders target,
            boolean lowerCaseNames) {
        for (Header header : source) {
            String name =
                    lowerCaseNames ? header.getName().toLowerCase(Locale.ROOT) : header.getName();
            target.add(name, header.getValue());
        }
    }

    /**
     * Aborts the exchange by closing the connection.
     *
     * @return True.
     */
    boolean abort() {
        connection.close();
        return true;
    }

    /**
     * Flushes the response head, and the buffered response body if any.
     *
     * @throws IOException If the flush fails.
     */
    void flushBuffers() throws IOException {
        if (responseBody != null) {
            responseBody.flush();
        } else {
            channel.flush();
        }
    }

    /**
     * Indicates whether the given failure is caused by a broken connection.
     *
     * @param exception The failure.
     * @return True if the failure is caused by a broken connection.
     */
    boolean isConnectionBroken(Throwable exception) {
        return (exception instanceof ClosedChannelException)
                || !channel.isActive()
                || isBroken(exception);
    }

    private static boolean isBroken(Throwable exception) {
        for (Throwable t = exception; t != null; t = t.getCause()) {
            String message = t.getMessage();
            if ((t instanceof IOException
                            && t.getClass().getName().endsWith("ClientAbortException"))
                    || (message != null
                            && (message.contains("Broken pipe")
                                    || message.contains("Connection reset")))) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------ TLS

    private SSLSession sslSession() {
        if (kind == StreamKind.HTTP3) {
            return Http3Support.sslSession(connection);
        }
        SslHandler ssl = connection.pipeline().get(SslHandler.class);
        return (ssl != null) ? ssl.engine().getSession() : null;
    }

    private List<Certificate> getCertificates() {
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

    private String getCipherSuite() {
        SSLSession session = sslSession();
        return (session != null) ? session.getCipherSuite() : null;
    }

    private String getSslSessionId() {
        SSLSession session = sslSession();
        byte[] id = (session != null) ? session.getId() : null;
        return (id != null && id.length > 0) ? HexFormat.of().formatHex(id) : null;
    }
}
