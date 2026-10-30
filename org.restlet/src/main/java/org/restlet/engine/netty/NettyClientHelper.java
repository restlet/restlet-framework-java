/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2StreamChannel;
import io.netty.handler.codec.http2.Http2StreamChannelBootstrap;
import io.netty.handler.codec.http2.Http2StreamFrameToHttpObjectCodec;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.resolver.NoopAddressResolverGroup;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GlobalEventExecutor;
import io.netty.util.concurrent.Promise;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.GeneralSecurityException;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLParameters;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.connector.ClientHelper;
import org.restlet.engine.ssl.SslUtils;
import org.restlet.engine.util.ReferenceUtils;
import org.restlet.representation.Representation;

/**
 * Base class for the Netty HTTP client connector. Connections are pooled per origin (scheme, host
 * and port). HTTP/2 connections are shared by concurrent calls, one stream per call. Here is the
 * list of parameters that are supported. They should be set in the Client's context before it is
 * started:
 *
 * <table>
 * <caption>list of supported parameters</caption>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>bindAddress</td>
 * <td>String</td>
 * <td>null</td>
 * <td>The address to bind socket channels to. You must set <i>both</i> this and bindPort</td>
 * </tr>
 * <tr>
 * <td>bindPort</td>
 * <td>int</td>
 * <td>null</td>
 * <td>The port to bind socket channels to. You must set <i>both</i> this and bindAddress</td>
 * </tr>
 * <tr>
 * <td>connectTimeout</td>
 * <td>long</td>
 * <td>15000</td>
 * <td>The max time in milliseconds a connection can take to connect to destinations, including
 * the TLS handshake</td>
 * </tr>
 * <tr>
 * <td>eventLoop.workerThreads</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Number of event loop threads doing network I/O. When 0, Netty uses twice the number of
 * available processors.</td>
 * </tr>
 * <tr>
 * <td>followRedirects</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Whether to follow HTTP redirects (301, 302, 303, 307 and 308) of synchronous calls</td>
 * </tr>
 * <tr>
 * <td>hostnameVerification</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Whether to check that the server certificate matches the host name (HTTPS endpoint
 * identification)</td>
 * </tr>
 * <tr>
 * <td>httpClientTransportMode</td>
 * <td>String</td>
 * <td>HTTP1_1</td>
 * <td>Indicate the HTTP client transport mode among the following options: "HTTP1_1", "HTTP2"
 * (HTTP/2 only: negotiated with ALPN over TLS, prior knowledge in cleartext), "DYNAMIC" (HTTP/2
 * when negotiated with ALPN over TLS, HTTP/1.1 otherwise), "HTTP3" (<b>experimental</b>: HTTP/3
 * over QUIC for HTTPS URIs without proxy, HTTP/1.1 otherwise; requires the optional
 * {@code io.netty:netty-codec-http3} dependency with its native QUIC library, and the {@link
 * org.restlet.engine.ssl.DefaultSslContextFactory}).</td>
 * </tr>
 * <tr>
 * <td>idleTimeout</td>
 * <td>long</td>
 * <td>30000</td>
 * <td>The max time in milliseconds a connection can be idle (that is, without traffic of bytes in
 * either direction). Also the max time to wait for a response.</td>
 * </tr>
 * <tr>
 * <td>maxConnectionsPerDestination</td>
 * <td>int</td>
 * <td>64</td>
 * <td>The max number of HTTP/1.1 connections to open to each destination</td>
 * </tr>
 * <tr>
 * <td>maxRedirects</td>
 * <td>int</td>
 * <td>8</td>
 * <td>The max number of HTTP redirects that are followed</td>
 * </tr>
 * <tr>
 * <td>maxResponseHeaderSize</td>
 * <td>int</td>
 * <td>8192</td>
 * <td>The max size in bytes of the response status line and headers.</td>
 * </tr>
 * <tr>
 * <td>proxyHost</td>
 * <td>String</td>
 * <td>System property "http.proxyHost"</td>
 * <td>The host name of the HTTP proxy.</td>
 * </tr>
 * <tr>
 * <td>proxyPort</td>
 * <td>int</td>
 * <td>System property "http.proxyPort" or "3128"</td>
 * <td>The port of the HTTP proxy.</td>
 * </tr>
 * <tr>
 * <td>requestBufferSize</td>
 * <td>int</td>
 * <td>8192</td>
 * <td>The size in bytes of the buffer used to write request entities</td>
 * </tr>
 * <tr>
 * <td>userAgentField</td>
 * <td>String</td>
 * <td>null</td>
 * <td>The "User-Agent" HTTP header string; when null, uses the Restlet default</td>
 * </tr>
 * <tr>
 * <td>sslContextFactory</td>
 * <td>String</td>
 * <td>org.restlet.engine.ssl.DefaultSslContextFactory</td>
 * <td>Let you specify a {@link org.restlet.engine.ssl.SslContextFactory} qualified class name as a
 * parameter, or an instance as an attribute for a more complete and flexible SSL context
 * setting</td>
 * </tr>
 * </table>
 *
 * @author Jerome Louvel
 */
public abstract class NettyClientHelper extends ClientHelper {

    /** The negotiated application protocol of a new connection. */
    private static final AttributeKey<Promise<String>> READY =
            AttributeKey.valueOf(NettyClientHelper.class, "ready");

    /** The pool owning a connection. */
    private static final AttributeKey<OriginPool> POOL =
            AttributeKey.valueOf(NettyClientHelper.class, "pool");

    /** Supported transport modes. */
    protected enum TransportMode {
        HTTP1_1,
        HTTP2,
        HTTP3,
        DYNAMIC
    }

    /** Connection pool of an origin. */
    static final class OriginPool {

        final boolean secure;

        final String host;

        final int port;

        final Deque<Channel> idle = new ConcurrentLinkedDeque<>();

        final Semaphore permits;

        /**
         * Serializes connection establishment when HTTP/2 may be negotiated, so that concurrent
         * first calls share a single HTTP/2 connection instead of opening one each.
         */
        final ReentrantLock connectLock = new ReentrantLock();

        volatile Channel http2;

        OriginPool(boolean secure, String host, int port, int maxConnections) {
            this.secure = secure;
            this.host = host;
            this.port = port;
            this.permits = new Semaphore(Math.max(1, maxConnections));
        }
    }

    /** The event loop group doing network I/O. */
    private volatile EventLoopGroup group;

    /** All open connections. */
    private volatile ChannelGroup connections;

    /** The Netty SSL context, created on first use. */
    private volatile SslContext sslContext;

    /** The experimental HTTP/3 client, created on first use, or null. */
    private volatile Http3Support.Client http3Client;

    /** The QUIC connections, per origin, for the experimental HTTP/3 support. */
    private final Map<String, Channel> http3Connections = new ConcurrentHashMap<>();

    /** The connection pools, per origin. */
    private final Map<String, OriginPool> pools = new ConcurrentHashMap<>();

    /**
     * Constructor.
     *
     * @param client The client to help.
     */
    protected NettyClientHelper(Client client) {
        super(client);
    }

    /**
     * Creates the exchange of a request.
     *
     * @param request The Restlet request.
     * @return The exchange.
     */
    NettyClientCall create(Request request) {
        Reference ref = ReferenceUtils.update(request.getResourceRef(), request);
        String scheme = ref.getSchemeProtocol() == null ? null : ref.getScheme();

        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IllegalArgumentException("Only HTTP or HTTPS resource URIs are allowed here");
        }

        boolean secure = "https".equalsIgnoreCase(scheme);
        boolean forwardProxy = !secure && (getProxyHost() != null);
        String target = ReferenceUtils.format(request.getResourceRef(), forwardProxy, request);
        return new NettyClientCall(this, request.getMethod().toString(), ref, target, secure);
    }

    @Override
    public void handle(Request request, Response response) {
        if (!isFollowRedirects() || request.isAsynchronous()) {
            try {
                handleOnce(request, response);
            } finally {
                request.getAttributes().remove(NettyClientCall.ASYNC_REQUEST_BODY_ATTRIBUTE);
            }
            return;
        }

        Request current = request;
        int redirects = 0;

        try {
            Request next = current;
            while (next != null) {
                current = next;
                handleOnce(current, response);
                next = (redirects < getMaxRedirects()) ? nextRedirect(current, response) : null;
                if (next != null) {
                    // The redirected exchange still belongs to the caller's logical request.
                    next.getDisconnectSignal().thenAccept(request::notifyDisconnect);
                    if (response.getEntity() != null) {
                        response.getEntity().release();
                    }
                    resetResponse(response, next);
                    redirects++;
                }
            }
        } finally {
            boolean entityReleasedByWriter =
                    Boolean.TRUE.equals(
                                    request.getAttributes()
                                            .remove(NettyClientCall.ASYNC_REQUEST_BODY_ATTRIBUTE))
                            && !response.getStatus().isRedirection();
            // A full-duplex HTTP/2 body writer releases the entity once it has finished writing.
            if (request.getEntity() != null && !entityReleasedByWriter) {
                request.getEntity().release();
            }
        }

        response.setRequest(request);
    }

    /**
     * Handles one exchange, without following redirects.
     *
     * @param request The Restlet request.
     * @param response The Restlet response.
     */
    private void handleOnce(Request request, Response response) {
        try {
            create(request).handle(request, response);
        } catch (RuntimeException e) {
            getLogger().log(Level.INFO, "Error while handling an HTTP client call", e);
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, e);
        }
    }

    /**
     * Returns the request following a redirect response, or null if the response is not a redirect
     * that can be followed.
     */
    private static Request nextRedirect(Request current, Response response) {
        Status status = response.getStatus();
        Reference location = response.getLocationRef();
        if (!isRedirect(status) || location == null) {
            return null;
        }
        return newRedirectRequest(current, status, location);
    }

    private static boolean isRedirect(Status status) {
        return status.equals(Status.REDIRECTION_PERMANENT)
                || status.equals(Status.REDIRECTION_FOUND)
                || status.equals(Status.REDIRECTION_SEE_OTHER)
                || status.equals(Status.REDIRECTION_TEMPORARY)
                || status.getCode() == 308;
    }

    private static Request newRedirectRequest(Request current, Status status, Reference location) {
        Reference target =
                location.isRelative()
                        ? new Reference(current.getResourceRef(), location).getTargetRef()
                        : location;
        Request next = new Request(current);
        next.setResourceRef(target);

        boolean keepMethod = (status.getCode() == 307) || (status.getCode() == 308);
        boolean toGet =
                status.equals(Status.REDIRECTION_SEE_OTHER)
                        || (!keepMethod && Method.POST.equals(current.getMethod()));

        if (toGet) {
            if (!Method.HEAD.equals(current.getMethod())) {
                next.setMethod(Method.GET);
            }
            next.setEntity((Representation) null);
        } else if (current.getEntity() != null
                && (current.getEntity().isTransient() || !current.getEntity().isAvailable())) {
            // The entity was consumed by the first request and can't be sent again.
            return null;
        }

        // Never leak credentials to another origin.
        Reference from = current.getResourceRef();
        boolean sameOrigin =
                equalsIgnoreCase(from.getScheme(), target.getScheme())
                        && equalsIgnoreCase(from.getHostDomain(), target.getHostDomain())
                        && (from.getHostPort() == target.getHostPort());
        if (!sameOrigin) {
            next.setChallengeResponse(null);
            next.setProxyChallengeResponse(null);
        }

        next.setHostRef((Reference) null);
        return next;
    }

    private static boolean equalsIgnoreCase(String a, String b) {
        return (a == null) ? (b == null) : a.equalsIgnoreCase(b);
    }

    private static void resetResponse(Response response, Request next) {
        response.setRequest(next);
        response.setEntity(null);
        response.setLocationRef((Reference) null);
        response.setAuthenticationInfo(null);
        response.setRetryAfter(null);
        response.getChallengeRequests().clear();
        response.getProxyChallengeRequests().clear();
        response.getCookieSettings().clear();
        response.getAllowedMethods().clear();
        response.getDimensions().clear();
    }

    // ------------------------------------------------------------------ connections

    /**
     * Acquires a channel ready to carry one exchange: an idle or new HTTP/1.1 connection, or a new
     * stream of an HTTP/2 connection.
     *
     * @param call The call that will use the channel.
     * @return The channel.
     * @throws IOException If no connection could be established.
     */
    Channel acquire(NettyClientCall call) throws IOException {
        if (call.isConfidential()
                && getTransportMode() == TransportMode.HTTP3
                && getProxyHost() == null) {
            return acquireHttp3(call);
        }

        OriginPool pool =
                pools.computeIfAbsent(
                        (call.isConfidential() ? "https://" : "http://")
                                + call.getTargetHost()
                                + ":"
                                + call.getTargetPort(),
                        k ->
                                new OriginPool(
                                        call.isConfidential(),
                                        call.getTargetHost(),
                                        call.getTargetPort(),
                                        getMaxConnectionsPerDestination()));

        Channel http2 = pool.http2;
        if (http2 != null && http2.isActive()) {
            return openStream(http2, call);
        }

        Channel channel;
        while ((channel = pool.idle.pollFirst()) != null) {
            if (channel.isActive()) {
                bindHttp1(channel, call);
                return channel;
            }
        }

        if (getTransportMode() == TransportMode.HTTP1_1) {
            channel = connect(pool);
        } else {
            pool.connectLock.lock();
            try {
                http2 = pool.http2;
                if (http2 != null && http2.isActive()) {
                    return openStream(http2, call);
                }
                channel = connect(pool);
                if (channel.pipeline().get(Http2MultiplexHandler.class) != null) {
                    pool.http2 = channel;
                    return openStream(channel, call);
                }
            } finally {
                pool.connectLock.unlock();
            }
        }

        bindHttp1(channel, call);
        return channel;
    }

    /**
     * Opens an HTTP/3 request stream, on the shared QUIC connection of the origin.
     *
     * @param call The call that will use the stream.
     * @return The request stream.
     * @throws IOException If no connection or stream could be established.
     */
    private Channel acquireHttp3(NettyClientCall call) throws IOException {
        Http3Support.Client client = getHttp3Client();
        String key = call.getTargetHost() + ":" + call.getTargetPort();
        Channel connection = http3Connections.get(key);
        if (connection == null || !connection.isActive()) {
            synchronized (http3Connections) {
                connection = http3Connections.get(key);
                if (connection == null || !connection.isActive()) {
                    connection =
                            client.connect(
                                    call.getTargetHost(),
                                    call.getTargetPort(),
                                    getConnectTimeout());
                    connections.add(connection);
                    http3Connections.put(key, connection);
                }
            }
        }

        NettyClientHandler handler = new NettyClientHandler();
        handler.bind(call);
        call.setStreamKind(StreamKind.HTTP3);
        return client.openStream(
                connection, handler, new HttpContentDecompressor(false, 0), getConnectTimeout());
    }

    private Http3Support.Client getHttp3Client() throws IOException {
        Http3Support.Client result = this.http3Client;
        if (result == null) {
            synchronized (this) {
                result = this.http3Client;
                if (result == null) {
                    try {
                        result =
                                Http3Support.Client.create(
                                        group,
                                        SslUtils.getSslContextFactory(this),
                                        isHostnameVerification(),
                                        getIdleTimeout());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new InterruptedIOException(
                                "Interrupted while creating the HTTP/3 client");
                    } catch (GeneralSecurityException e) {
                        throw new IOException("Unable to create the HTTP/3 client", e);
                    }
                    connections.add(result.channel());
                    this.http3Client = result;
                }
            }
        }
        return result;
    }

    /**
     * Releases a channel after an exchange.
     *
     * @param channel The channel.
     * @param reusable True if the connection can carry another exchange.
     */
    void release(Channel channel, boolean reusable) {
        if (channel.attr(POOL).get() == null) {
            // A stream of a multiplexed connection (HTTP/2 or HTTP/3).
            channel.close();
            return;
        }

        NettyClientHandler handler = channel.pipeline().get(NettyClientHandler.class);
        if (handler != null) {
            handler.unbind();
        }

        OriginPool pool = channel.attr(POOL).get();
        if (reusable && channel.isActive() && pool != null && group != null) {
            pool.idle.offerFirst(channel);
        } else {
            channel.close();
        }
    }

    private void bindHttp1(Channel channel, NettyClientCall call) throws IOException {
        NettyClientHandler handler = channel.pipeline().get(NettyClientHandler.class);
        if (handler == null) {
            throw new IOException("Connection not ready");
        }
        handler.bind(call);
    }

    private Channel openStream(Channel parent, NettyClientCall call) throws IOException {
        final NettyClientHandler handler = new NettyClientHandler();
        handler.bind(call);
        call.setStreamKind(StreamKind.HTTP2);
        Future<Http2StreamChannel> future =
                new Http2StreamChannelBootstrap(parent)
                        .option(ChannelOption.AUTO_READ, false)
                        .handler(
                                new ChannelInitializer<Channel>() {
                                    @Override
                                    protected void initChannel(Channel stream) {
                                        ChannelPipeline p = stream.pipeline();
                                        p.addLast(new Http2StreamFrameToHttpObjectCodec(false));
                                        p.addLast(new HttpContentDecompressor(false, 0));
                                        p.addLast(Writability.HANDLER);
                                        p.addLast(handler);
                                    }
                                })
                        .open();
        return NettyFutures.await(future, getConnectTimeout(), "Unable to open an HTTP/2 stream");
    }

    private Channel connect(final OriginPool pool) throws IOException {
        long timeout = getConnectTimeout();
        try {
            if (!pool.permits.tryAcquire(timeout, TimeUnit.MILLISECONDS)) {
                throw new ConnectException(
                        "Too many connections to " + pool.host + ":" + pool.port);
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Interrupted while waiting for a connection");
        }

        boolean acquired = true;
        try {
            final SslContext ssl = pool.secure ? getSslContext() : null;
            final String proxyHost = getProxyHost();
            final InetSocketAddress proxy =
                    (proxyHost == null) ? null : new InetSocketAddress(proxyHost, getProxyPort());

            Bootstrap bootstrap =
                    new Bootstrap()
                            .group(group)
                            .channel(NioSocketChannel.class)
                            .option(ChannelOption.AUTO_READ, false)
                            .option(ChannelOption.TCP_NODELAY, true)
                            .option(
                                    ChannelOption.CONNECT_TIMEOUT_MILLIS,
                                    (int) Math.min(Integer.MAX_VALUE, timeout))
                            .handler(
                                    new ChannelInitializer<SocketChannel>() {
                                        @Override
                                        protected void initChannel(SocketChannel ch) {
                                            initConnection(
                                                    ch, pool, ssl, pool.secure ? proxy : null);
                                        }
                                    });

            SocketAddress bindAddress = getBindAddress();
            if (bindAddress != null) {
                bootstrap.localAddress(bindAddress);
            }

            SocketAddress remote;
            if (proxy != null && pool.secure) {
                // Tunnel with CONNECT, the proxy resolves the host name.
                bootstrap.resolver(NoopAddressResolverGroup.INSTANCE);
                remote = InetSocketAddress.createUnresolved(pool.host, pool.port);
            } else if (proxy != null) {
                // Forward proxy: absolute request URIs are sent to the proxy.
                remote = proxy;
            } else {
                remote = InetSocketAddress.createUnresolved(pool.host, pool.port);
            }

            ChannelFuture connectFuture = bootstrap.connect(remote);
            Channel channel =
                    NettyFutures.await(
                            connectFuture,
                            timeout,
                            "Unable to connect to " + pool.host + ":" + pool.port);
            connections.add(channel);
            channel.closeFuture().addListener(f -> pool.permits.release());
            acquired = false;

            Promise<String> ready = channel.attr(READY).get();
            try {
                NettyFutures.await(ready, timeout, "Unable to establish the connection");
            } catch (IOException e) {
                channel.close();
                throw e;
            }
            return channel;
        } finally {
            if (acquired) {
                pool.permits.release();
            }
        }
    }

    private void initConnection(
            SocketChannel ch, OriginPool pool, SslContext ssl, InetSocketAddress tunnelProxy) {
        final Promise<String> ready = ch.eventLoop().newPromise();
        ch.attr(READY).set(ready);
        ch.attr(POOL).set(pool);
        ch.closeFuture()
                .addListener(f -> ready.tryFailure(new IOException("Connection closed by peer")));

        ChannelPipeline p = ch.pipeline();
        long idleTimeout = getIdleTimeout();
        if (idleTimeout > 0) {
            p.addLast("idle", new IdleStateHandler(0, 0, idleTimeout, TimeUnit.MILLISECONDS));
        }
        p.addLast("writability", Writability.HANDLER);

        if (tunnelProxy != null) {
            HttpProxyHandler proxyHandler = new HttpProxyHandler(tunnelProxy);
            proxyHandler.setConnectTimeoutMillis(getConnectTimeout());
            p.addLast("proxy", proxyHandler);
        }

        final TransportMode mode = getTransportMode();
        if (ssl != null) {
            SSLEngine engine = ssl.newEngine(ch.alloc(), pool.host, pool.port);
            if (isHostnameVerification()) {
                SSLParameters parameters = engine.getSSLParameters();
                parameters.setEndpointIdentificationAlgorithm("HTTPS");
                engine.setSSLParameters(parameters);
            }
            SslHandler sslHandler = new SslHandler(engine);
            sslHandler.setHandshakeTimeoutMillis(getConnectTimeout());
            p.addLast("ssl", sslHandler);
            p.addLast(
                    "alpn",
                    new ApplicationProtocolNegotiationHandler(ApplicationProtocolNames.HTTP_1_1) {
                        @Override
                        protected void configurePipeline(
                                ChannelHandlerContext ctx, String protocol) {
                            if (ApplicationProtocolNames.HTTP_2.equals(protocol)
                                    && (mode == TransportMode.HTTP2
                                            || mode == TransportMode.DYNAMIC)) {
                                addHttp2Handlers(ctx.pipeline());
                            } else if (mode == TransportMode.HTTP2) {
                                ready.tryFailure(
                                        new IOException("The server doesn't support HTTP/2"));
                                ctx.close();
                                return;
                            } else {
                                addHttp1Handlers(ctx.pipeline());
                            }
                            ready.trySuccess(protocol);
                        }

                        @Override
                        protected void handshakeFailure(
                                ChannelHandlerContext ctx, Throwable cause) {
                            ready.tryFailure(cause);
                            ctx.close();
                        }
                    });
        } else if (mode == TransportMode.HTTP2) {
            addHttp2Handlers(p);
            ready.trySuccess(ApplicationProtocolNames.HTTP_2);
        } else {
            addHttp1Handlers(p);
            ready.trySuccess(ApplicationProtocolNames.HTTP_1_1);
        }
    }

    private void addHttp1Handlers(ChannelPipeline p) {
        p.addLast("http-codec", new HttpClientCodec(4096, getMaxResponseHeaderSize(), 8192));
        p.addLast("http-decompressor", new HttpContentDecompressor(false, 0));
        p.addLast("restlet", new NettyClientHandler());
    }

    private void addHttp2Handlers(ChannelPipeline p) {
        p.addLast("h2-frame-codec", Http2FrameCodecBuilder.forClient().build());
        p.addLast(
                "h2-multiplex",
                new Http2MultiplexHandler(
                        new ChannelInboundHandlerAdapter() {
                            @Override
                            public boolean isSharable() {
                                return true;
                            }
                        }));
        // Reads are not driven by a call on the parent connection.
        p.channel().config().setAutoRead(true);
    }

    private SslContext getSslContext() throws IOException {
        SslContext result = this.sslContext;
        if (result == null) {
            synchronized (this) {
                result = this.sslContext;
                if (result == null) {
                    try {
                        ApplicationProtocolConfig alpn =
                                switch (getTransportMode()) {
                                    // HTTP/3 doesn't use TCP: its fallback is HTTP/1.1.
                                    case HTTP1_1, HTTP3 -> ApplicationProtocolConfig.DISABLED;
                                    case HTTP2 -> alpn(ApplicationProtocolNames.HTTP_2);
                                    case DYNAMIC ->
                                            alpn(
                                                    ApplicationProtocolNames.HTTP_2,
                                                    ApplicationProtocolNames.HTTP_1_1);
                                };
                        result =
                                NettySsl.newContext(
                                        SslUtils.getSslContextFactory(this).createSslContext(),
                                        true,
                                        alpn,
                                        ClientAuth.NONE);
                        this.sslContext = result;
                    } catch (Exception e) {
                        throw new IOException("Unable to create the SSL context", e);
                    }
                }
            }
        }
        return result;
    }

    private static ApplicationProtocolConfig alpn(String... protocols) {
        return NettySsl.alpn(List.of(protocols));
    }

    @Override
    public synchronized void start() throws Exception {
        if (getTransportMode() == TransportMode.HTTP3) {
            Http3Availability.ensureAvailable();
            getLogger().warning("HTTP/3 support is experimental");
        }
        super.start();
        group =
                new MultiThreadIoEventLoopGroup(
                        Math.max(0, getEventLoopWorkerThreads()), NioIoHandler.newFactory());
        connections = new DefaultChannelGroup("restlet-client", GlobalEventExecutor.INSTANCE);
        getLogger().info("Starting the Netty " + getProtocols() + " client");
    }

    @Override
    public synchronized void stop() throws Exception {
        getLogger().info("Stopping the Netty " + getProtocols() + " client");
        EventLoopGroup g = group;
        group = null;
        if (connections != null) {
            connections.close().awaitUninterruptibly(5, TimeUnit.SECONDS);
        }
        pools.clear();
        http3Connections.clear();
        http3Client = null;
        if (g != null) {
            g.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS);
        }
        sslContext = null;
        super.stop();
    }

    /**
     * Logs a failure.
     *
     * @param message The message.
     * @param e The failure.
     */
    void logFailure(String message, Throwable e) {
        getLogger().log(Level.FINE, message, e);
    }

    // ------------------------------------------------------------------ parameters

    /**
     * Returns the transport mode, validating the "httpClientTransportMode" parameter.
     *
     * @return The transport mode.
     */
    protected TransportMode getTransportMode() {
        String name = getHttpClientTransportMode();
        try {
            return TransportMode.valueOf(name);
        } catch (IllegalArgumentException | NullPointerException _) {
            throw new IllegalArgumentException(
                    String.format(
                            "'%s' is not one of the supported values: %s",
                            name, java.util.Arrays.toString(TransportMode.values())));
        }
    }

    /**
     * The address to bind socket channels to. Default to null.
     *
     * @return The bind address or null.
     */
    public SocketAddress getBindAddress() {
        final String bindAddress = getHelpedParameters().getFirstValue("bindAddress", null);
        final String bindPort = getHelpedParameters().getFirstValue("bindPort", null);

        if ((bindAddress != null) && (bindPort != null)) {
            return new InetSocketAddress(bindAddress, Integer.parseInt(bindPort));
        } else {
            return null;
        }
    }

    /**
     * The max time in milliseconds a connection can take to connect to destinations. Defaults to
     * 15_000.
     *
     * @return The connect timeout.
     */
    public long getConnectTimeout() {
        return Long.parseLong(getHelpedParameters().getFirstValue("connectTimeout", "15000"));
    }

    /**
     * Number of event loop threads doing network I/O. Defaults to 0, letting Netty decide.
     *
     * @return Number of event loop threads.
     */
    public int getEventLoopWorkerThreads() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("eventLoop.workerThreads", "0"));
    }

    /**
     * Returns the event loop group doing network I/O, so that other Netty channels can share it.
     * The group belongs to this connector: it is created on start and shut down on stop, closing
     * the channels registered with it. Callers must not shut it down.
     *
     * <p>This is an engine extension point, meant for Restlet extensions.
     *
     * @return The event loop group, or null if the connector is not started.
     */
    @SuppressWarnings("exports")
    public EventLoopGroup getEventLoopGroup() {
        return group;
    }

    /**
     * Returns the HTTP client transport mode among the following options: "HTTP1_1", "HTTP2",
     * "HTTP3" (experimental) and "DYNAMIC". Default to "HTTP1_1".
     *
     * @return The HTTP client transport mode.
     */
    public String getHttpClientTransportMode() {
        return getHelpedParameters()
                .getFirstValue("httpClientTransportMode", TransportMode.HTTP1_1.name());
    }

    /**
     * The max time in milliseconds a connection can be idle (that is, without traffic of bytes in
     * either direction). Defaults to 30_000.
     *
     * @return The idle timeout.
     */
    public long getIdleTimeout() {
        return Long.parseLong(getHelpedParameters().getFirstValue("idleTimeout", "30000"));
    }

    /**
     * The max number of HTTP/1.1 connections to open to each destination. Defaults to 64.
     *
     * @return The maximum connections per destination.
     */
    public int getMaxConnectionsPerDestination() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("maxConnectionsPerDestination", "64"));
    }

    /**
     * The max number of HTTP redirects that are followed. Defaults to 8.
     *
     * @return The maximum redirects.
     */
    public int getMaxRedirects() {
        return Integer.parseInt(getHelpedParameters().getFirstValue("maxRedirects", "8"));
    }

    /**
     * Returns the max size in bytes of the response status line and headers. Defaults to 8192.
     *
     * @return The max size in bytes of the response headers.
     */
    public int getMaxResponseHeaderSize() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("maxResponseHeaderSize", "8192"));
    }

    /**
     * Returns the host name of the HTTP proxy, if specified.
     *
     * @return the host name of the HTTP proxy, if specified.
     */
    public String getProxyHost() {
        return getHelpedParameters()
                .getFirstValue("proxyHost", System.getProperty("http.proxyHost"));
    }

    /**
     * Returns the port of the HTTP proxy, if specified, 3128 otherwise.
     *
     * @return the port of the HTTP proxy.
     */
    public int getProxyPort() {
        return Integer.parseInt(
                getHelpedParameters()
                        .getFirstValue("proxyPort", System.getProperty("http.proxyPort", "3128")));
    }

    /**
     * The size in bytes of the buffer used to write request entities. Defaults to 8192.
     *
     * @return The request buffer size.
     */
    public int getRequestBufferSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue("requestBufferSize", "8192"));
    }

    /**
     * The "User-Agent" HTTP header string. When null, uses the Restlet default. Default to null.
     *
     * @return The user agent field or null.
     */
    public String getUserAgentField() {
        return getHelpedParameters().getFirstValue("userAgentField", null);
    }

    /**
     * Whether to follow HTTP redirects. Defaults to true.
     *
     * @return Whether to follow redirects.
     */
    public boolean isFollowRedirects() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue("followRedirects", "true"));
    }

    /**
     * Whether to check that the server certificate matches the host name. Defaults to true.
     *
     * @return Whether to verify host names.
     */
    public boolean isHostnameVerification() {
        return Boolean.parseBoolean(
                getHelpedParameters().getFirstValue("hostnameVerification", "true"));
    }
}
