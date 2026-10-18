/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.MultiThreadIoEventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerKeepAliveHandler;
import io.netty.handler.codec.http.HttpServerUpgradeHandler;
import io.netty.handler.codec.http2.CleartextHttp2ServerUpgradeHandler;
import io.netty.handler.codec.http2.Http2CodecUtil;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.codec.http2.Http2ServerUpgradeCodec;
import io.netty.handler.codec.http2.Http2StreamFrameToHttpObjectCodec;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AsciiString;
import io.netty.util.concurrent.GlobalEventExecutor;
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import javax.net.ssl.SSLContext;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.engine.connector.ServerHelper;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.engine.ssl.SslContextFactory;
import org.restlet.engine.ssl.SslUtils;

/**
 * Base class for the Netty HTTP and HTTPS server connectors. Here is the list of parameters that
 * are supported. They should be set in the Server's context before it is started:
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
 * <td>threadPool.virtual</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>When true, each call runs on its own virtual thread. When false, calls run on a pool of
 * platform threads configured by the other "threadPool.*" parameters.</td>
 * </tr>
 * <tr>
 * <td>threadPool.minThreads</td>
 * <td>int</td>
 * <td>8</td>
 * <td>Platform thread pool minimum threads. Ignored with virtual threads.</td>
 * </tr>
 * <tr>
 * <td>threadPool.maxThreads</td>
 * <td>int</td>
 * <td>200</td>
 * <td>Platform thread pool maximum threads. Ignored with virtual threads.</td>
 * </tr>
 * <tr>
 * <td>threadPool.threadsPriority</td>
 * <td>int</td>
 * <td>{@link Thread#NORM_PRIORITY}</td>
 * <td>Platform thread pool threads priority. Ignored with virtual threads.</td>
 * </tr>
 * <tr>
 * <td>threadPool.idleTimeout</td>
 * <td>int</td>
 * <td>60000</td>
 * <td>Platform thread pool idle timeout in milliseconds. Ignored with virtual threads.</td>
 * </tr>
 * <tr>
 * <td>server.maxConcurrentCalls</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Maximum number of calls processed concurrently; extra calls receive a 503 response. When
 * 0, there is no limit.</td>
 * </tr>
 * <tr>
 * <td>server.maxEventStreams</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Maximum number of open Server-Sent Event streams; extra streams receive a 503 response.
 * When 0, there is no limit.</td>
 * </tr>
 * <tr>
 * <td>server.maxWebSockets</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Maximum number of open WebSocket sessions; extra upgrades receive a 503 response. When 0,
 * there is no limit.</td>
 * </tr>
 * <tr>
 * <td>websocket.maxMessageSize</td>
 * <td>int</td>
 * <td>65536</td>
 * <td>Maximum size in bytes of a received WebSocket message; larger messages close the session
 * with status 1009.</td>
 * </tr>
 * <tr>
 * <td>eventLoop.bossThreads</td>
 * <td>int</td>
 * <td>1</td>
 * <td>Number of event loop threads accepting connections.</td>
 * </tr>
 * <tr>
 * <td>eventLoop.workerThreads</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Number of event loop threads doing network I/O. When 0, Netty uses twice the number of
 * available processors.</td>
 * </tr>
 * <tr>
 * <td>connector.acceptQueueSize</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Accept queue size (backlog). When 0, the operating system default is used.</td>
 * </tr>
 * <tr>
 * <td>connector.idleTimeout</td>
 * <td>int</td>
 * <td>30000</td>
 * <td>Connection idle timeout in milliseconds; idle connections are closed. When 0, there is no
 * timeout.</td>
 * </tr>
 * <tr>
 * <td>http.requestHeaderSize</td>
 * <td>int</td>
 * <td>8192</td>
 * <td>Maximum size in bytes of the request line and headers.</td>
 * </tr>
 * <tr>
 * <td>http.outputBufferSize</td>
 * <td>int</td>
 * <td>32768</td>
 * <td>Size in bytes of the response output buffer.</td>
 * </tr>
 * <tr>
 * <td>server.maxConnections</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Maximum number of open connections; extra connections are closed on accept. When 0, there
 * is no limit.</td>
 * </tr>
 * <tr>
 * <td>shutdown.gracefully</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>When true, on stop the server stops accepting connections and waits up to
 * "shutdown.timeout" for calls in progress to complete. Otherwise, connections are closed
 * immediately.</td>
 * </tr>
 * <tr>
 * <td>shutdown.timeout</td>
 * <td>long</td>
 * <td>0</td>
 * <td>Graceful shutdown timeout in milliseconds.</td>
 * </tr>
 * <tr>
 * <td>useForwardedForHeader</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Lookup the "X-Forwarded-For" header supported by popular proxies and caches and uses it to
 * populate the Request.getClientAddresses() method result. This information is only safe for
 * intermediary components within your local network. Other addresses could easily be changed by
 * setting a fake header and should not be trusted for serious security checks.</td>
 * </tr>
 * </table>
 *
 * @author Jerome Louvel
 */
public abstract class NettyServerHelper extends ServerHelper {

    /** The event loop group accepting connections. */
    private volatile EventLoopGroup bossGroup;

    /** The event loop group doing network I/O. */
    private volatile EventLoopGroup workerGroup;

    /** The executor running Restlet calls. */
    private volatile ExecutorService callExecutor;

    /** The server channel. */
    private volatile Channel serverChannel;

    /** All open connections. */
    private volatile ChannelGroup connections;

    /** Limits concurrent calls, or null when unlimited. */
    private volatile Semaphore callPermits;

    /** Limits concurrent SSE streams, or null when unlimited. */
    private volatile Semaphore eventStreamPermits;

    /** Limits open WebSocket sessions, or null when unlimited. */
    private volatile Semaphore webSocketPermits;

    /** The number of calls in progress. */
    private final AtomicInteger activeCalls = new AtomicInteger();

    /** Guards the wait for the calls in progress to complete, on graceful stop. */
    private final ReentrantLock activeCallsLock = new ReentrantLock();

    /** Signaled when the last call in progress completes. */
    private final Condition noActiveCalls = activeCallsLock.newCondition();

    /** The application protocols, set on start. */
    private volatile List<String> applicationProtocols = List.of(HTTP_1_1);

    /** The Netty SSL context, set on start when confidential. */
    private volatile SslContext sslContext;

    /** The UDP channel of the experimental HTTP/3 server, or null. */
    private volatile Channel http3Channel;

    /** The "Alt-Svc" header value advertising HTTP/3, or null. */
    private volatile String altSvc;

    /** Indicates whether the server is stopping. */
    private volatile boolean stopping;

    /**
     * Constructor.
     *
     * @param server The server to help.
     */
    protected NettyServerHelper(Server server) {
        super(server);
    }

    /** Name of the Restlet handler in a connection pipeline. */
    private static final String RESTLET_HANDLER = "restlet";

    /** Name of the HTTP/1.1 keep-alive handler in a connection pipeline. */
    private static final String KEEPALIVE_HANDLER = "http-keepalive";

    /** HTTP/1.1 application protocol identifier. */
    protected static final String HTTP_1_1 = "http/1.1";

    /** HTTP/2 application protocol identifier. */
    protected static final String HTTP_2 = "h2";

    /**
     * HTTP/3 application protocol identifier. Experimental: HTTP/3 is served over QUIC on the UDP
     * port having the same number as the TCP port, and advertised with the "Alt-Svc" header.
     */
    protected static final String HTTP_3 = "h3";

    /**
     * Returns the supported application protocols, in preference order, using their ALPN
     * identifiers: {@link #HTTP_1_1}, {@link #HTTP_2} and {@link #HTTP_3}. Over TLS, HTTP/1.1 and
     * HTTP/2 are negotiated with ALPN, and HTTP/3 is served over QUIC. Without TLS, {@link #HTTP_2}
     * enables cleartext HTTP/2 (h2c), with the HTTP/1.1 "Upgrade" mechanism or with prior
     * knowledge, in addition to HTTP/1.1.
     *
     * @return The supported application protocols.
     */
    protected abstract List<String> getApplicationProtocols();

    /**
     * Indicates whether connections are secured with TLS. The TLS settings come from the {@link
     * org.restlet.engine.ssl.SslContextFactory} of the connector.
     *
     * @return True if connections are secured with TLS.
     */
    protected abstract boolean isConfidential();

    /**
     * Validates the configuration before starting. Called by {@link #start()}.
     *
     * @throws IllegalArgumentException If the configuration is invalid.
     */
    protected void validateConfiguration() {}

    /**
     * Configures the pipeline of a new connection.
     *
     * @param channel The new connection.
     */
    private void initConnection(SocketChannel channel) {
        ChannelPipeline pipeline = channel.pipeline();
        final List<String> protocols = this.applicationProtocols;
        boolean h2 = protocols.contains(HTTP_2);
        SslContext ssl = this.sslContext;

        if (ssl != null) {
            pipeline.addLast("ssl", ssl.newHandler(channel.alloc()));
            if (h2) {
                pipeline.addLast("alpn", newAlpnHandler(protocols.contains(HTTP_1_1)));
            } else {
                addHttp1Handlers(pipeline);
            }
        } else if (h2) {
            addCleartextHttp2Handlers(pipeline);
        } else {
            addHttp1Handlers(pipeline);
        }
    }

    /**
     * Creates the handler that configures the pipeline once ALPN selected HTTP/2 or HTTP/1.1.
     *
     * @param http11 True if HTTP/1.1 is also supported.
     * @return The ALPN handler.
     */
    private ApplicationProtocolNegotiationHandler newAlpnHandler(final boolean http11) {
        return new ApplicationProtocolNegotiationHandler(http11 ? HTTP_1_1 : HTTP_2) {
            @Override
            protected void configurePipeline(ChannelHandlerContext ctx, String protocol) {
                if (HTTP_2.equals(protocol)) {
                    addHttp2Handlers(ctx.pipeline());
                } else if (http11 && HTTP_1_1.equals(protocol)) {
                    addHttp1Handlers(ctx.pipeline());
                } else {
                    throw new IllegalStateException(
                            "Unsupported application protocol: " + protocol);
                }
            }
        };
    }

    /**
     * Creates the Netty SSL context from the Restlet SSL context factory.
     *
     * @param protocols The application protocols to negotiate.
     * @return The Netty SSL context.
     * @throws Exception If the SSL context can't be created.
     */
    private SslContext createSslContext(List<String> protocols) throws Exception {
        SslContextFactory factory = SslUtils.getSslContextFactory(this);
        SSLContext jdkContext = factory.createSslContext();

        ClientAuth clientAuth = ClientAuth.NONE;
        if (factory instanceof DefaultSslContextFactory defaultFactory) {
            if (defaultFactory.isNeedClientAuthentication()) {
                clientAuth = ClientAuth.REQUIRE;
            } else if (defaultFactory.isWantClientAuthentication()) {
                clientAuth = ClientAuth.OPTIONAL;
            }
        }

        ApplicationProtocolConfig alpn =
                protocols.contains(HTTP_2)
                        ? NettySsl.alpn(protocols)
                        : ApplicationProtocolConfig.DISABLED;
        return NettySsl.newContext(jdkContext, false, alpn, clientAuth);
    }

    /**
     * Adds the HTTP/1.1 handlers to a connection pipeline.
     *
     * @param pipeline The pipeline.
     */
    private void addHttp1Handlers(ChannelPipeline pipeline) {
        pipeline.addLast("http-codec", newHttpServerCodec());
        pipeline.addLast(KEEPALIVE_HANDLER, new HttpServerKeepAliveHandler());
        pipeline.addLast(
                RESTLET_HANDLER, new NettyServerHandler(this, isConfidential(), StreamKind.HTTP1));
    }

    /**
     * Adds HTTP/2 handlers to a connection pipeline, after protocol negotiation (ALPN or prior
     * knowledge).
     *
     * @param pipeline The pipeline.
     */
    private void addHttp2Handlers(ChannelPipeline pipeline) {
        pipeline.addLast("h2-frame-codec", Http2FrameCodecBuilder.forServer().build());
        pipeline.addLast("h2-multiplex", new Http2MultiplexHandler(newHttp2StreamInitializer()));
    }

    /**
     * Adds handlers supporting both HTTP/1.1 and cleartext HTTP/2 (h2c), either via the HTTP/1.1
     * "Upgrade" mechanism or via prior knowledge.
     *
     * @param pipeline The pipeline.
     */
    private void addCleartextHttp2Handlers(ChannelPipeline pipeline) {
        HttpServerCodec sourceCodec = newHttpServerCodec();
        HttpServerUpgradeHandler.UpgradeCodecFactory upgradeCodecFactory =
                protocol -> {
                    if (AsciiString.contentEquals(
                            Http2CodecUtil.HTTP_UPGRADE_PROTOCOL_NAME, protocol)) {
                        return new Http2ServerUpgradeCodec(
                                Http2FrameCodecBuilder.forServer().build(),
                                new Http2MultiplexHandler(newHttp2StreamInitializer()),
                                CLEARTEXT_HTTP2_SETUP);
                    }
                    return null;
                };
        HttpServerUpgradeHandler upgradeHandler =
                new HttpServerUpgradeHandler(sourceCodec, upgradeCodecFactory);

        // Prior knowledge: the connection starts directly with the HTTP/2 preface.
        ChannelInitializer<Channel> priorKnowledge =
                new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel ch) {
                        ch.pipeline().addLast(Http2FrameCodecBuilder.forServer().build());
                        ch.pipeline()
                                .addLast(new Http2MultiplexHandler(newHttp2StreamInitializer()));
                        ch.pipeline().addLast(CLEARTEXT_HTTP2_SETUP);
                    }
                };

        pipeline.addLast(
                "h2c",
                new CleartextHttp2ServerUpgradeHandler(
                        sourceCodec, upgradeHandler, priorKnowledge));
        // Requests that are not upgraded stay on HTTP/1.1.
        pipeline.addLast(KEEPALIVE_HANDLER, new HttpServerKeepAliveHandler());
        pipeline.addLast(
                RESTLET_HANDLER, new NettyServerHandler(this, isConfidential(), StreamKind.HTTP1));
    }

    /**
     * Switches a cleartext connection from HTTP/1.1 to HTTP/2: removes the HTTP/1.1 handlers and
     * reads the connection continuously, the back-pressure being applied per stream.
     */
    private static final ChannelHandler CLEARTEXT_HTTP2_SETUP =
            new ChannelHandlerAdapter() {
                @Override
                public boolean isSharable() {
                    return true;
                }

                @Override
                public void handlerAdded(ChannelHandlerContext ctx) {
                    ChannelPipeline p = ctx.pipeline();
                    if (p.get(RESTLET_HANDLER) != null) {
                        p.remove(RESTLET_HANDLER);
                    }
                    if (p.get(KEEPALIVE_HANDLER) != null) {
                        p.remove(KEEPALIVE_HANDLER);
                    }
                    p.remove(this);
                    ctx.channel().config().setAutoRead(true);
                }
            };

    private ChannelInitializer<Channel> newHttp2StreamInitializer() {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(Channel stream) {
                stream.pipeline().addLast(new Http2StreamFrameToHttpObjectCodec(true));
                stream.pipeline().addLast(Writability.HANDLER);
                stream.pipeline()
                        .addLast(
                                new NettyServerHandler(
                                        NettyServerHelper.this,
                                        isConfidential(),
                                        StreamKind.HTTP2));
            }
        };
    }

    private HttpServerCodec newHttpServerCodec() {
        int headerSize = getHttpRequestHeaderSize();
        return new HttpServerCodec(4096, headerSize, 8192);
    }

    /**
     * Tries to acquire a permit for a new call.
     *
     * @return True if the call may proceed.
     */
    boolean tryAcquireCall() {
        if (stopping) {
            return false;
        }
        Semaphore permits = callPermits;
        if (permits != null && !permits.tryAcquire()) {
            return false;
        }
        activeCalls.incrementAndGet();
        return true;
    }

    /** Releases the permit of a completed call. */
    void releaseCall() {
        if (activeCalls.decrementAndGet() == 0) {
            activeCallsLock.lock();
            try {
                noActiveCalls.signalAll();
            } finally {
                activeCallsLock.unlock();
            }
        }
        Semaphore permits = callPermits;
        if (permits != null) {
            permits.release();
        }
    }

    /** Tries to acquire a permit for an SSE response. */
    boolean tryAcquireEventStream() {
        Semaphore permits = eventStreamPermits;
        return permits == null || permits.tryAcquire();
    }

    /** Releases an acquired SSE response permit. */
    void releaseEventStream() {
        Semaphore permits = eventStreamPermits;
        if (permits != null) {
            permits.release();
        }
    }

    /** Tries to acquire a permit for a WebSocket session. */
    boolean tryAcquireWebSocket() {
        Semaphore permits = webSocketPermits;
        return !stopping && (permits == null || permits.tryAcquire());
    }

    /** Releases an acquired WebSocket session permit. */
    void releaseWebSocket() {
        Semaphore permits = webSocketPermits;
        if (permits != null) {
            permits.release();
        }
    }

    /**
     * Returns the executor running Restlet calls.
     *
     * @return The executor running Restlet calls.
     */
    ExecutorService getCallExecutor() {
        return callExecutor;
    }

    @Override
    public synchronized void start() throws Exception {
        validateConfiguration();
        List<String> protocols = getApplicationProtocols();
        boolean http3 = protocols.contains(HTTP_3);
        // HTTP/3 is not negotiated on TCP connections.
        applicationProtocols = validateProtocols(protocols, http3);
        sslContext = isConfidential() ? createSslContext(applicationProtocols) : null;
        super.start();
        stopping = false;

        int maxCalls = getServerMaxConcurrentCalls();
        callPermits = (maxCalls > 0) ? new Semaphore(maxCalls) : null;
        int maxEventStreams = getServerMaxEventStreams();
        eventStreamPermits = (maxEventStreams > 0) ? new Semaphore(maxEventStreams) : null;
        int maxWebSockets = getServerMaxWebSockets();
        webSocketPermits = (maxWebSockets > 0) ? new Semaphore(maxWebSockets) : null;
        callExecutor =
                CallExecutors.create(
                        "restlet-server",
                        isThreadPoolVirtual(),
                        getThreadPoolMinThreads(),
                        getThreadPoolMaxThreads(),
                        getThreadPoolIdleTimeout(),
                        getThreadPoolThreadsPriority());

        bossGroup =
                new MultiThreadIoEventLoopGroup(
                        Math.max(1, getEventLoopBossThreads()), NioIoHandler.newFactory());
        workerGroup =
                new MultiThreadIoEventLoopGroup(
                        Math.max(0, getEventLoopWorkerThreads()), NioIoHandler.newFactory());
        connections = new DefaultChannelGroup("restlet-connections", GlobalEventExecutor.INSTANCE);

        final int maxConnections = getServerMaxConnections();
        final int idleTimeout = getConnectorIdleTimeout();

        ServerBootstrap bootstrap =
                new ServerBootstrap()
                        .group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .childOption(ChannelOption.TCP_NODELAY, true)
                        .childHandler(newConnectionInitializer(maxConnections, idleTimeout));

        int backlog = getConnectorAcceptQueueSize();
        if (backlog > 0) {
            bootstrap.option(ChannelOption.SO_BACKLOG, backlog);
        }

        String address = getHelped().getAddress();
        int port = getHelped().getPort();

        getLogger()
                .log(
                        Level.INFO,
                        "Starting the Netty {0} server on port {1}",
                        new Object[] {getProtocols(), port});
        try {
            bindTcpAndUdp(bootstrap, address, port, http3, maxConnections, idleTimeout);
        } catch (Exception e) {
            shutdownNow();
            throw e;
        }
    }

    /**
     * Checks the HTTP/3 requirements and returns the protocols negotiated on TCP connections.
     *
     * @param protocols The configured application protocols.
     * @param http3 True if HTTP/3 is configured.
     * @return The application protocols of the TCP connections.
     */
    private List<String> validateProtocols(List<String> protocols, boolean http3) {
        if (http3) {
            if (!isConfidential()) {
                throw new IllegalArgumentException("HTTP/3 requires TLS");
            }
            Http3Availability.ensureAvailable();
        }
        List<String> result = protocols.stream().filter(p -> !HTTP_3.equals(p)).toList();
        if (result.isEmpty()) {
            throw new IllegalArgumentException("HTTP/3 must be combined with HTTP1_1 or HTTP2");
        }
        return result;
    }

    private static InetSocketAddress newAddress(String address, int port) {
        return (address != null)
                ? new InetSocketAddress(address, port)
                : new InetSocketAddress(port);
    }

    /**
     * Creates the initializer of accepted TCP connections, enforcing "server.maxConnections".
     *
     * @param maxConnections The maximum number of connections, 0 for no limit.
     * @param idleTimeout The connection idle timeout in milliseconds, 0 for none.
     * @return The initializer.
     */
    private ChannelInitializer<SocketChannel> newConnectionInitializer(
            final int maxConnections, final int idleTimeout) {
        return new ChannelInitializer<>() {
            @Override
            protected void initChannel(SocketChannel ch) {
                if (!admitConnection(ch, maxConnections)) {
                    ch.close();
                    return;
                }
                ChannelPipeline p = ch.pipeline();
                if (idleTimeout > 0) {
                    p.addLast(
                            "idle", new IdleStateHandler(0, 0, idleTimeout, TimeUnit.MILLISECONDS));
                }
                p.addLast("writability", Writability.HANDLER);
                initConnection(ch);
            }
        };
    }

    /**
     * Registers a new connection, unless the server is stopping or the connection limit is reached.
     *
     * @param connection The new connection.
     * @param maxConnections The maximum number of connections, 0 for no limit.
     * @return True if the connection is admitted.
     */
    private boolean admitConnection(Channel connection, int maxConnections) {
        if (stopping || (maxConnections > 0 && connections.size() >= maxConnections)) {
            return false;
        }
        connections.add(connection);
        return true;
    }

    private void startHttp3(InetSocketAddress address, final int maxConnections, int idleTimeout)
            throws GeneralSecurityException, IOException, InterruptedException {
        getLogger()
                .log(
                        Level.WARNING,
                        "HTTP/3 support is experimental. Serving HTTP/3 on UDP port {0}",
                        address.getPort());
        http3Channel =
                Http3Support.bindServer(
                        workerGroup,
                        address,
                        SslUtils.getSslContextFactory(this),
                        idleTimeout,
                        connection -> admitConnection(connection, maxConnections),
                        () -> new NettyServerHandler(this, true, StreamKind.HTTP3));
        altSvc = "h3=\":" + address.getPort() + "\"; ma=86400";
    }

    /** Attempts to find a free port number for both TCP and UDP, when the port is ephemeral. */
    private static final int HTTP3_EPHEMERAL_PORT_ATTEMPTS = 5;

    /**
     * Binds the TCP server channel, and the HTTP/3 UDP channel on the same port number. With an
     * ephemeral port, the TCP port chosen by the system may already be used over UDP: the binding
     * is then retried on another ephemeral port.
     */
    private void bindTcpAndUdp(
            ServerBootstrap bootstrap,
            String address,
            int port,
            boolean http3,
            int maxConnections,
            int idleTimeout)
            throws GeneralSecurityException, IOException, InterruptedException {
        int attempts = (http3 && port == 0) ? HTTP3_EPHEMERAL_PORT_ATTEMPTS : 1;
        BindException failure = null;
        for (int attempt = 1; attempt <= attempts; attempt++) {
            serverChannel = bootstrap.bind(newAddress(address, port)).sync().channel();
            int actualPort = ((InetSocketAddress) serverChannel.localAddress()).getPort();
            try {
                if (http3) {
                    startHttp3(newAddress(address, actualPort), maxConnections, idleTimeout);
                }
                setEphemeralPort(actualPort);
                return;
            } catch (BindException e) {
                failure = e;
                serverChannel.close().awaitUninterruptibly(5, TimeUnit.SECONDS);
                serverChannel = null;
                getLogger().log(Level.FINE, "UDP port {0} already in use", actualPort);
            }
        }
        throw failure;
    }

    @Override
    public synchronized void stop() throws Exception {
        getLogger()
                .log(
                        Level.INFO,
                        "Stopping the Netty {0} server on port {1}",
                        new Object[] {getProtocols(), getHelped().getPort()});
        stopping = true;

        if (serverChannel != null) {
            serverChannel.close().awaitUninterruptibly(5, TimeUnit.SECONDS);
        }

        if (getShutdownGracefully()) {
            awaitActiveCalls(TimeUnit.MILLISECONDS.toNanos(getShutdownTimeout()));
        }

        shutdownNow();
        super.stop();
    }

    /**
     * Waits for the calls in progress to complete.
     *
     * @param timeoutNanos The maximum time to wait.
     * @throws InterruptedException If the wait is interrupted.
     */
    private void awaitActiveCalls(long timeoutNanos) throws InterruptedException {
        long remaining = timeoutNanos;
        activeCallsLock.lock();
        try {
            while (activeCalls.get() > 0 && remaining > 0) {
                remaining = noActiveCalls.awaitNanos(remaining);
            }
        } finally {
            activeCallsLock.unlock();
        }
    }

    private void shutdownNow() {
        if (serverChannel != null) {
            serverChannel.close().awaitUninterruptibly(5, TimeUnit.SECONDS);
        }
        if (http3Channel != null) {
            http3Channel.close().awaitUninterruptibly(5, TimeUnit.SECONDS);
            http3Channel = null;
            altSvc = null;
        }
        if (connections != null) {
            connections.close().awaitUninterruptibly(5, TimeUnit.SECONDS);
        }
        if (callExecutor != null) {
            callExecutor.shutdownNow();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS);
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully(0, 0, TimeUnit.MILLISECONDS);
        }
        serverChannel = null;
    }

    // ------------------------------------------------------------------ parameters

    /**
     * Whether calls run on virtual threads. Defaults to true.
     *
     * @return True if calls run on virtual threads.
     */
    public boolean isThreadPoolVirtual() {
        return Boolean.parseBoolean(
                getHelpedParameters().getFirstValue("threadPool.virtual", "true"));
    }

    /**
     * Platform thread pool minimum threads. Defaults to 8.
     *
     * @return Platform thread pool minimum threads.
     */
    public int getThreadPoolMinThreads() {
        return Integer.parseInt(getHelpedParameters().getFirstValue("threadPool.minThreads", "8"));
    }

    /**
     * Platform thread pool maximum threads. Defaults to 200.
     *
     * @return Platform thread pool maximum threads.
     */
    public int getThreadPoolMaxThreads() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("threadPool.maxThreads", "200"));
    }

    /**
     * Platform thread pool idle timeout in milliseconds. Defaults to 60000.
     *
     * @return Platform thread pool idle timeout.
     */
    public int getThreadPoolIdleTimeout() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("threadPool.idleTimeout", "60000"));
    }

    /**
     * Platform thread pool threads priority. Defaults to {@link Thread#NORM_PRIORITY}.
     *
     * @return Platform thread pool threads priority.
     */
    public int getThreadPoolThreadsPriority() {
        return Integer.parseInt(
                getHelpedParameters()
                        .getFirstValue(
                                "threadPool.threadsPriority",
                                String.valueOf(Thread.NORM_PRIORITY)));
    }

    /**
     * Maximum number of concurrent calls. Defaults to 0 (unlimited).
     *
     * @return Maximum number of concurrent calls.
     */
    public int getServerMaxConcurrentCalls() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("server.maxConcurrentCalls", "0"));
    }

    /**
     * Maximum number of open Server-Sent Event streams. Defaults to 0 (unlimited).
     *
     * @return Maximum number of open SSE streams.
     */
    public int getServerMaxEventStreams() {
        int result =
                Integer.parseInt(
                        getHelpedParameters().getFirstValue("server.maxEventStreams", "0"));
        if (result < 0) {
            throw new IllegalArgumentException("'server.maxEventStreams' cannot be negative");
        }
        return result;
    }

    /**
     * Maximum number of open WebSocket sessions. Defaults to 0 (unlimited).
     *
     * @return Maximum number of open WebSocket sessions.
     */
    public int getServerMaxWebSockets() {
        int result =
                Integer.parseInt(getHelpedParameters().getFirstValue("server.maxWebSockets", "0"));
        if (result < 0) {
            throw new IllegalArgumentException("'server.maxWebSockets' cannot be negative");
        }
        return result;
    }

    /**
     * Maximum size in bytes of a received WebSocket message. Defaults to 65536.
     *
     * @return Maximum WebSocket message size.
     */
    public int getWebSocketMaxMessageSize() {
        int result =
                Integer.parseInt(
                        getHelpedParameters().getFirstValue("websocket.maxMessageSize", "65536"));
        if (result <= 0) {
            throw new IllegalArgumentException("'websocket.maxMessageSize' must be positive");
        }
        return result;
    }

    /**
     * Number of event loop threads accepting connections. Defaults to 1.
     *
     * @return Number of boss threads.
     */
    public int getEventLoopBossThreads() {
        return Integer.parseInt(getHelpedParameters().getFirstValue("eventLoop.bossThreads", "1"));
    }

    /**
     * Number of event loop threads doing network I/O. Defaults to 0 (Netty default).
     *
     * @return Number of worker threads.
     */
    public int getEventLoopWorkerThreads() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("eventLoop.workerThreads", "0"));
    }

    /**
     * Accept queue size. Defaults to 0 (operating system default).
     *
     * @return Accept queue size.
     */
    public int getConnectorAcceptQueueSize() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("connector.acceptQueueSize", "0"));
    }

    /**
     * Connection idle timeout in milliseconds. Defaults to 30000.
     *
     * @return Connection idle timeout.
     */
    public int getConnectorIdleTimeout() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("connector.idleTimeout", "30000"));
    }

    /**
     * Maximum size in bytes of the request line and headers. Defaults to 8192.
     *
     * @return Maximum request header size.
     */
    public int getHttpRequestHeaderSize() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("http.requestHeaderSize", "8192"));
    }

    /**
     * Size in bytes of the response output buffer. Defaults to 32768.
     *
     * @return Response output buffer size.
     */
    public int getHttpOutputBufferSize() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("http.outputBufferSize", "32768"));
    }

    /**
     * Maximum number of open connections. Defaults to 0 (unlimited).
     *
     * @return Maximum number of open connections.
     */
    public int getServerMaxConnections() {
        return Integer.parseInt(getHelpedParameters().getFirstValue("server.maxConnections", "0"));
    }

    /**
     * Whether the server shuts down gracefully. Defaults to true.
     *
     * @return True if the server shuts down gracefully.
     */
    public boolean getShutdownGracefully() {
        return Boolean.parseBoolean(
                getHelpedParameters().getFirstValue("shutdown.gracefully", "true"));
    }

    /**
     * Graceful shutdown timeout in milliseconds. Defaults to 0.
     *
     * @return Graceful shutdown timeout.
     */
    public long getShutdownTimeout() {
        return Long.parseLong(getHelpedParameters().getFirstValue("shutdown.timeout", "0"));
    }

    /**
     * Returns the "Alt-Svc" header value advertising HTTP/3, or null if HTTP/3 is not served.
     *
     * @return The "Alt-Svc" header value or null.
     */
    String getAltSvc() {
        return altSvc;
    }

    /**
     * Handles an exchange: converts it into a Restlet request and response, lets the server handle
     * them, then writes the response.
     *
     * @param call The exchange.
     */
    void handle(NettyServerCall call) {
        NettyRequest request = call.toRequest(getContext());
        Response response = call.toResponse(request);
        handle(request, response);
        call.commit(response);
    }
}
