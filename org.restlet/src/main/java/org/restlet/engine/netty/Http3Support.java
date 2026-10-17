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
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.http3.Http3;
import io.netty.handler.codec.http3.Http3ClientConnectionHandler;
import io.netty.handler.codec.http3.Http3FrameToHttpObjectCodec;
import io.netty.handler.codec.http3.Http3RequestStreamInitializer;
import io.netty.handler.codec.http3.Http3ServerConnectionHandler;
import io.netty.handler.codec.quic.InsecureQuicTokenHandler;
import io.netty.handler.codec.quic.Quic;
import io.netty.handler.codec.quic.QuicChannel;
import io.netty.handler.codec.quic.QuicSslContext;
import io.netty.handler.codec.quic.QuicSslContextBuilder;
import io.netty.handler.codec.quic.QuicStreamChannel;
import io.netty.handler.ssl.ClientAuth;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.KeyStore;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManagerFactory;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.engine.ssl.SslContextFactory;

/**
 * Experimental HTTP/3 support, over QUIC. All the references to the Netty QUIC and HTTP/3 classes
 * are kept in this class, so that the optional {@code io.netty:netty-codec-http3} dependency and
 * its native QUIC library are only needed when HTTP/3 is enabled.
 *
 * <p>Limits of this experimental support:
 *
 * <ul>
 *   <li>It requires the native QUIC library of Netty, available for Linux, macOS (x86_64 and
 *       aarch64) and Windows (x86_64).
 *   <li>TLS is provided by BoringSSL, configured from the key and trust stores of the {@link
 *       DefaultSslContextFactory}. Custom {@link SslContextFactory} implementations, and the
 *       enabled cipher suites and protocols settings, are not supported.
 *   <li>The client only uses HTTP/3 when asked to ("httpClientTransportMode" set to HTTP3), for
 *       HTTPS URIs and without proxy. It doesn't discover HTTP/3 with the "Alt-Svc" header.
 *   <li>Server push, 0-RTT and connection migration are not supported.
 *   <li>QUIC address validation tokens are not cryptographically protected (Netty's insecure token
 *       handler), so the server doesn't resist address spoofing as well as a production QUIC server
 *       would.
 *   <li>HTTP/3 shares the TCP connector's limits ("server.maxConnections", idle timeout), but not
 *       its accept queue or "connector.acceptQueueSize".
 * </ul>
 */
final class Http3Support {

    /** The origin of a client connection, used for SNI and the server certificate check. */
    private static final AttributeKey<InetSocketAddress> PEER =
            AttributeKey.valueOf(Http3Support.class, "peer");

    /** Default QUIC flow control windows. */
    private static final long MAX_DATA = 16L * 1024 * 1024;

    private static final long MAX_STREAM_DATA = 1024L * 1024;

    private static final long MAX_STREAMS = 100;

    private Http3Support() {}

    /**
     * Checks that the native QUIC library can be loaded. Call {@link
     * Http3Availability#ensureAvailable()} instead, which first checks the presence of the optional
     * classes.
     *
     * @throws IllegalStateException If the native QUIC library is not available.
     */
    static void ensureNativeLibrary() {
        if (!Quic.isAvailable()) {
            throw new IllegalStateException(
                    "HTTP/3 requires the native QUIC library of Netty, which is not available on"
                            + " this platform",
                    Quic.unavailabilityCause());
        }
    }

    // ------------------------------------------------------------------ server

    /**
     * Binds an HTTP/3 server on a UDP port.
     *
     * @param group The event loop group.
     * @param address The address to bind.
     * @param factory The SSL context factory of the connector.
     * @param idleTimeoutMs The connection idle timeout, 0 for none.
     * @param admit Called with each new QUIC connection; returning false closes it.
     * @param streamHandler Supplies the handler of each request stream.
     * @return The bound UDP channel.
     * @throws Exception If the server can't be bound.
     */
    static Channel bindServer(
            EventLoopGroup group,
            InetSocketAddress address,
            SslContextFactory factory,
            long idleTimeoutMs,
            final Predicate<Channel> admit,
            final Supplier<ChannelHandler> streamHandler)
            throws Exception {
        DefaultSslContextFactory defaultFactory = toDefaultFactory(factory);
        KeyManagerFactory kmf = defaultFactory.createKeyManagerFactory();
        if (kmf == null) {
            throw new IllegalArgumentException("HTTP/3 requires a key store");
        }

        QuicSslContextBuilder builder =
                QuicSslContextBuilder.forServer(kmf, null)
                        .applicationProtocols(Http3.supportedApplicationProtocols());
        TrustManagerFactory tmf = defaultFactory.createTrustManagerFactory();
        if (tmf != null) {
            builder.trustManager(tmf);
        }
        if (defaultFactory.isNeedClientAuthentication()) {
            builder.clientAuth(ClientAuth.REQUIRE);
        } else if (defaultFactory.isWantClientAuthentication()) {
            builder.clientAuth(ClientAuth.OPTIONAL);
        }
        QuicSslContext sslContext = builder.build();

        final ChannelInitializer<QuicStreamChannel> requestStreamInitializer =
                new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(QuicStreamChannel stream) {
                        stream.pipeline().addLast(new Http3FrameToHttpObjectCodec(true));
                        stream.pipeline().addLast(Writability.HANDLER);
                        stream.pipeline().addLast(streamHandler.get());
                    }
                };

        ChannelHandler codec =
                configure(Http3.newQuicServerCodecBuilder(), idleTimeoutMs)
                        .sslContext(sslContext)
                        .tokenHandler(InsecureQuicTokenHandler.INSTANCE)
                        .handler(
                                new ChannelInitializer<QuicChannel>() {
                                    @Override
                                    protected void initChannel(QuicChannel connection) {
                                        if (!admit.test(connection)) {
                                            connection.close();
                                            return;
                                        }
                                        connection
                                                .pipeline()
                                                .addLast(
                                                        new Http3ServerConnectionHandler(
                                                                requestStreamInitializer));
                                    }
                                })
                        .build();

        return new Bootstrap()
                .group(group)
                .channel(NioDatagramChannel.class)
                .handler(codec)
                .bind(address)
                .sync()
                .channel();
    }

    private static <B extends io.netty.handler.codec.quic.QuicCodecBuilder<B>> B configure(
            B builder, long idleTimeoutMs) {
        if (idleTimeoutMs > 0) {
            builder.maxIdleTimeout(idleTimeoutMs, TimeUnit.MILLISECONDS);
        }
        return builder.initialMaxData(MAX_DATA)
                .initialMaxStreamDataBidirectionalLocal(MAX_STREAM_DATA)
                .initialMaxStreamDataBidirectionalRemote(MAX_STREAM_DATA)
                .initialMaxStreamsBidirectional(MAX_STREAMS);
    }

    /**
     * Returns the UDP address of the peer of a QUIC connection.
     *
     * @param connection The QUIC connection.
     * @return The UDP address of the peer.
     */
    static SocketAddress remoteSocketAddress(Channel connection) {
        return ((QuicChannel) connection).remoteSocketAddress();
    }

    /**
     * Returns the local UDP address of a QUIC connection.
     *
     * @param connection The QUIC connection.
     * @return The local UDP address.
     */
    static SocketAddress localSocketAddress(Channel connection) {
        return ((QuicChannel) connection).localSocketAddress();
    }

    /**
     * Returns the TLS session of a QUIC connection.
     *
     * @param connection The QUIC connection.
     * @return The TLS session, or null.
     */
    static SSLSession sslSession(Channel connection) {
        return (connection instanceof QuicChannel quic && quic.sslEngine() != null)
                ? quic.sslEngine().getSession()
                : null;
    }

    // ------------------------------------------------------------------ client

    /** HTTP/3 client: one UDP channel shared by the QUIC connections of a client connector. */
    static final class Client {

        private final Channel datagramChannel;

        private Client(Channel datagramChannel) {
            this.datagramChannel = datagramChannel;
        }

        /**
         * Creates an HTTP/3 client.
         *
         * @param group The event loop group.
         * @param factory The SSL context factory of the connector.
         * @param hostnameVerification Whether to check the server certificate host name.
         * @param idleTimeoutMs The connection idle timeout, 0 for none.
         * @return The client.
         * @throws Exception If the client can't be created.
         */
        static Client create(
                EventLoopGroup group,
                SslContextFactory factory,
                boolean hostnameVerification,
                long idleTimeoutMs)
                throws Exception {
            DefaultSslContextFactory defaultFactory = toDefaultFactory(factory);
            TrustManagerFactory tmf = defaultFactory.createTrustManagerFactory();
            if (tmf == null) {
                tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init((KeyStore) null);
            }

            QuicSslContextBuilder builder =
                    QuicSslContextBuilder.forClient()
                            .trustManager(tmf)
                            .applicationProtocols(Http3.supportedApplicationProtocols());
            KeyManagerFactory kmf = defaultFactory.createKeyManagerFactory();
            if (kmf != null) {
                builder.keyManager(kmf, null);
            }
            if (hostnameVerification) {
                builder.endpointIdentificationAlgorithm("HTTPS");
            }
            final QuicSslContext sslContext = builder.build();

            ChannelHandler codec =
                    configure(Http3.newQuicClientCodecBuilder(), idleTimeoutMs)
                            .sslEngineProvider(
                                    connection -> {
                                        InetSocketAddress peer = connection.attr(PEER).get();
                                        return (peer == null)
                                                ? sslContext.newEngine(connection.alloc())
                                                : sslContext.newEngine(
                                                        connection.alloc(),
                                                        peer.getHostString(),
                                                        peer.getPort());
                                    })
                            .build();

            Channel channel =
                    new Bootstrap()
                            .group(group)
                            .channel(NioDatagramChannel.class)
                            .handler(codec)
                            .bind(0)
                            .sync()
                            .channel();
            return new Client(channel);
        }

        /**
         * Returns the UDP channel, so that the connector can close it on stop.
         *
         * @return The UDP channel.
         */
        Channel channel() {
            return datagramChannel;
        }

        /**
         * Opens a QUIC connection to an origin.
         *
         * @param host The host name.
         * @param port The port.
         * @param timeoutMs The connect timeout.
         * @return The QUIC connection.
         * @throws IOException If the connection fails.
         */
        Channel connect(String host, int port, long timeoutMs) throws IOException {
            InetSocketAddress remote = new InetSocketAddress(host, port);
            if (remote.isUnresolved()) {
                throw new java.net.UnknownHostException(host);
            }
            Future<QuicChannel> future =
                    QuicChannel.newBootstrap(datagramChannel)
                            .attr(PEER, InetSocketAddress.createUnresolved(host, port))
                            .handler(new Http3ClientConnectionHandler())
                            .remoteAddress(remote)
                            .connect();
            return NettyFutures.await(
                    future, timeoutMs, "Unable to connect with HTTP/3 to " + host + ":" + port);
        }

        /**
         * Opens a request stream on a QUIC connection.
         *
         * @param connection The QUIC connection.
         * @param handler The handler of the stream, added after the HTTP/3 to HTTP/1 codec.
         * @param decompressor The response content decompressor, or null.
         * @param timeoutMs The timeout.
         * @return The request stream.
         * @throws IOException If the stream can't be opened.
         */
        Channel openStream(
                Channel connection,
                final ChannelHandler handler,
                final ChannelHandler decompressor,
                long timeoutMs)
                throws IOException {
            Future<QuicStreamChannel> future =
                    Http3.newRequestStreamBootstrap(
                                    (QuicChannel) connection,
                                    new Http3RequestStreamInitializer() {
                                        @Override
                                        protected void initRequestStream(QuicStreamChannel ch) {
                                            ch.pipeline()
                                                    .addLast(
                                                            new Http3FrameToHttpObjectCodec(false));
                                            if (decompressor != null) {
                                                ch.pipeline().addLast(decompressor);
                                            }
                                            ch.pipeline().addLast(Writability.HANDLER);
                                            ch.pipeline().addLast(handler);
                                        }
                                    })
                            .option(ChannelOption.AUTO_READ, false)
                            .create();
            return NettyFutures.await(future, timeoutMs, "Unable to open an HTTP/3 stream");
        }
    }

    private static DefaultSslContextFactory toDefaultFactory(SslContextFactory factory) {
        if (factory instanceof DefaultSslContextFactory defaultFactory) {
            return defaultFactory;
        }
        throw new IllegalArgumentException(
                "HTTP/3 only supports the DefaultSslContextFactory, not "
                        + factory.getClass().getName());
    }
}
