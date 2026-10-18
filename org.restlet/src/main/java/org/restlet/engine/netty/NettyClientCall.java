/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.LastHttpContent;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import org.restlet.Uniform;
import org.restlet.data.Header;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.adapter.ClientCall;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.Representation;
import org.restlet.service.ConnectorService;

/**
 * HTTP client call based on Netty. The calling thread blocks while the request is written and until
 * the response head is received; the response body is then streamed from the connection.
 *
 * <p>This class is internal to the Netty connectors.
 */
class NettyClientCall extends ClientCall {

    /** The parent helper. */
    private final NettyClientHelper helper;

    /** The request target (origin form, or absolute form for a forward proxy). */
    private final String target;

    /** The target host. */
    private final String targetHost;

    /** The target port. */
    private final int targetPort;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition headReceived = lock.newCondition();

    /** The kind of channel carrying the exchange, set by the helper when it is acquired. */
    private volatile StreamKind kind = StreamKind.HTTP1;

    /** The channel carrying the exchange. */
    private volatile Channel channel;

    /** The response body stream. */
    private volatile ChannelInputStream body;

    /** Indicates whether the response head was received. */
    private boolean head;

    /** The failure before the response head, if any. */
    private Throwable failure;

    /** Indicates whether the request was fully sent. */
    private boolean requestSent;

    /** Indicates whether the response was fully received. */
    private boolean responseComplete;

    /** Indicates whether the connection can carry another exchange. */
    private boolean reusable = true;

    /** Indicates whether the channel was released. */
    private boolean released;

    /**
     * Constructor.
     *
     * @param helper The parent HTTP client helper.
     * @param method The method name.
     * @param resourceRef The absolute target resource reference.
     * @param target The request target sent on the wire.
     * @param secure True for HTTPS.
     */
    NettyClientCall(
            NettyClientHelper helper,
            String method,
            Reference resourceRef,
            String target,
            boolean secure) {
        super(helper, method, resourceRef.toString());
        this.helper = helper;
        this.target = target;
        this.targetHost = resourceRef.getHostDomain();
        int port = resourceRef.getHostPort();
        int defaultPort = secure ? 443 : 80;
        this.targetPort = (port == -1) ? defaultPort : port;
        setConfidential(secure);
        setServerAddress(this.targetHost);
        setServerPort(this.targetPort);
    }

    /**
     * Sets the kind of channel carrying the exchange.
     *
     * @param kind The kind of channel.
     */
    void setStreamKind(StreamKind kind) {
        this.kind = kind;
    }

    String getTargetHost() {
        return targetHost;
    }

    int getTargetPort() {
        return targetPort;
    }

    @Override
    public OutputStream getRequestEntityStream() {
        return null;
    }

    @Override
    public OutputStream getRequestHeadStream() {
        return null;
    }

    @Override
    public InputStream getResponseEntityStream(long size) {
        final ChannelInputStream stream = this.body;
        if (stream == null) {
            return null;
        }
        return new FilterInputStream(stream) {
            @Override
            public void close() throws IOException {
                super.close();
                onBodyClosed();
            }
        };
    }

    @Override
    public Status sendRequest(org.restlet.Request request) {
        Status result;
        Representation entity = request.isEntityAvailable() ? request.getEntity() : null;
        ConnectorService connectorService = ConnectorHelper.getConnectorService();
        if (connectorService != null) {
            connectorService.beforeSend(entity);
        }

        try {
            this.channel = helper.acquire(this);
            writeRequest(entity);
            awaitHead();
            result = new Status(getStatusCode(), getReasonPhrase());
        } catch (ConnectException e) {
            helper.logFailure("Unable to connect to the remote HTTP server", e);
            abort();
            result = new Status(Status.CONNECTOR_ERROR_CONNECTION, e);
        } catch (IOException e) {
            helper.logFailure(
                    "An error occurred during the communication with the remote HTTP server", e);
            abort();
            result = new Status(Status.CONNECTOR_ERROR_COMMUNICATION, e);
        } catch (RuntimeException e) {
            helper.logFailure("An unexpected error occurred while sending the request", e);
            abort();
            result = new Status(Status.CONNECTOR_ERROR_INTERNAL, e);
        } finally {
            // A repeatable entity is kept for a possible redirect (307/308) and released by the
            // helper once the redirects are followed.
            if (entity != null
                    && (entity.isTransient()
                            || !helper.isFollowRedirects()
                            || request.isAsynchronous())) {
                entity.release();
            }
            if (connectorService != null) {
                connectorService.afterSend(entity);
            }
        }

        return result;
    }

    @Override
    public void sendRequest(
            org.restlet.Request request, org.restlet.Response response, Uniform callback) {
        sendRequest(request);

        final Uniform onSent = request.getOnSent();
        if (onSent != null) {
            onSent.handle(request, response);
        }

        if (callback != null) {
            callback.handle(request, response);
        }
    }

    private void writeRequest(Representation entity) throws IOException {
        Channel ch = this.channel;
        HttpRequest nettyRequest = newRequestHead();
        boolean hasBody = setBodyHeaders(nettyRequest, entity);

        long timeout = helper.getIdleTimeout();
        Writability.await(ch, timeout);
        check(ch.writeAndFlush(nettyRequest));
        // Read now, so that an early response (for example an error) is not missed.
        ch.read();

        if (hasBody) {
            ChannelOutputStream out =
                    new ChannelOutputStream(
                            ch, DefaultHttpContent::new, helper.getRequestBufferSize(), timeout);
            entity.write(out);
            out.close();
        }

        Writability.await(ch, timeout);
        check(ch.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT));

        lock.lock();
        try {
            requestSent = true;
        } finally {
            lock.unlock();
        }
        maybeRelease();
    }

    /**
     * Creates the request head with the Restlet request headers, adapted to the transport.
     *
     * @return The request head.
     */
    private HttpRequest newRequestHead() {
        HttpRequest result =
                new DefaultHttpRequest(
                        HttpVersion.HTTP_1_1, HttpMethod.valueOf(getMethod()), target);
        HttpHeaders headers = result.headers();

        for (Header header : getRequestHeaders()) {
            String name = header.getName();
            if (!HeaderConstants.HEADER_CONTENT_LENGTH.equalsIgnoreCase(name)
                    && !HeaderConstants.HEADER_TRANSFER_ENCODING.equalsIgnoreCase(name)) {
                headers.add(name, header.getValue());
            }
        }

        String userAgent = helper.getUserAgentField();
        if (userAgent != null) {
            headers.set(HttpHeaderNames.USER_AGENT, userAgent);
        }

        if (kind.isStream()) {
            headers.remove(HttpHeaderNames.CONNECTION);
            headers.remove("Keep-Alive");
            // The HTTP/2 and HTTP/3 codecs read the ":scheme" pseudo-header from their extension
            // header.
            headers.set(
                    (kind == StreamKind.HTTP3) ? "x-http3-scheme" : "x-http2-scheme",
                    isConfidential() ? "https" : "http");
        } else if (!headers.contains(HttpHeaderNames.CONNECTION)) {
            headers.set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        return result;
    }

    /**
     * Sets the headers describing the request body.
     *
     * @param request The request head.
     * @param entity The request entity, or null.
     * @return True if a body must be written.
     */
    private boolean setBodyHeaders(HttpRequest request, Representation entity) {
        if (entity == null) {
            if (requiresContentLength(getMethod())) {
                HttpUtil.setContentLength(request, 0);
            }
            return false;
        }
        if (entity.getSize() >= 0) {
            HttpUtil.setContentLength(request, entity.getSize());
            return entity.getSize() > 0;
        }
        if (!kind.isStream()) {
            HttpUtil.setTransferEncodingChunked(request, true);
        }
        return true;
    }

    private static boolean requiresContentLength(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    private static void check(ChannelFuture future) throws IOException {
        if (future.isDone() && !future.isSuccess()) {
            throw new IOException("Unable to write the request", future.cause());
        }
    }

    private void awaitHead() throws IOException {
        long timeout = helper.getIdleTimeout();
        lock.lock();
        try {
            long remaining =
                    (timeout > 0) ? TimeUnit.MILLISECONDS.toNanos(timeout) : Long.MAX_VALUE;
            while (!head && failure == null) {
                if (timeout > 0) {
                    if (remaining <= 0) {
                        throw new InterruptedIOException("Timed out waiting for the response");
                    }
                    remaining = headReceived.awaitNanos(remaining);
                } else {
                    headReceived.await();
                }
            }
            if (!head) {
                throw (failure instanceof IOException ioe)
                        ? ioe
                        : new IOException("Unable to receive the response", failure);
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted while waiting for the response");
        } finally {
            lock.unlock();
        }
    }

    // ------------------------------------------------------------------ event loop callbacks

    /**
     * Called on the event loop when the response head is received.
     *
     * @param response The response head.
     */
    void onResponseHead(HttpResponse response) {
        for (Map.Entry<String, String> entry : response.headers()) {
            String name = entry.getKey();
            if (!name.regionMatches(true, 0, "x-http2-", 0, 8)
                    && !name.regionMatches(true, 0, "x-http3-", 0, 8)) {
                getResponseHeaders().add(name, entry.getValue());
            }
        }
        if (!HttpUtil.isKeepAlive(response)) {
            lock.lock();
            try {
                reusable = false;
            } finally {
                lock.unlock();
            }
        }

        setStatusCode(response.status().code());
        setReasonPhrase(response.status().reasonPhrase());
        setVersion(response.protocolVersion().text());
        this.body = new ChannelInputStream(channel, helper.getIdleTimeout());

        lock.lock();
        try {
            head = true;
            headReceived.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
     * Called on the event loop with a response content buffer, whose ownership is transferred.
     *
     * @param content The content buffer.
     * @return True if the event loop should read more content now.
     */
    boolean onResponseContent(ByteBuf content) {
        ChannelInputStream stream = this.body;
        if (stream == null) {
            content.release();
            return true;
        }
        return stream.offer(content);
    }

    /** Called on the event loop when the response is fully received. */
    void onResponseComplete() {
        ChannelInputStream stream = this.body;
        if (stream != null) {
            stream.end();
        }
        lock.lock();
        try {
            responseComplete = true;
        } finally {
            lock.unlock();
        }
        maybeRelease();
    }

    /**
     * Called on the event loop when the exchange fails.
     *
     * @param cause The failure cause.
     */
    void onFailure(Throwable cause) {
        lock.lock();
        try {
            if (responseComplete) {
                return;
            }
            reusable = false;
            if (!head) {
                failure = cause;
                headReceived.signalAll();
            }
        } finally {
            lock.unlock();
        }
        ChannelInputStream stream = this.body;
        if (stream != null) {
            stream.fail(cause);
        }
        abort();
    }

    // ------------------------------------------------------------------ connection release

    /** Called when the consumer closes the response body. */
    private void onBodyClosed() {
        lock.lock();
        try {
            if (!responseComplete) {
                // The rest of the body would have to be drained.
                reusable = false;
                responseComplete = true;
                requestSent = true;
            }
        } finally {
            lock.unlock();
        }
        maybeRelease();
    }

    private void maybeRelease() {
        boolean reuse;
        lock.lock();
        try {
            if (released || !requestSent || !responseComplete) {
                return;
            }
            released = true;
            reuse = reusable;
        } finally {
            lock.unlock();
        }
        Channel ch = this.channel;
        if (ch != null) {
            helper.release(ch, reuse);
        }
    }

    /** Releases the channel without reusing it. */
    private void abort() {
        lock.lock();
        try {
            if (released) {
                return;
            }
            released = true;
            reusable = false;
        } finally {
            lock.unlock();
        }
        Channel ch = this.channel;
        if (ch != null) {
            helper.release(ch, false);
        }
    }
}
