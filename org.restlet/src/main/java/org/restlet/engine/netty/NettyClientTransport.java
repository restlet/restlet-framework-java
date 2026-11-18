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
import io.netty.channel.ChannelFactory;
import io.netty.resolver.AddressResolverGroup;
import java.net.SocketAddress;

/**
 * Chooses how the Netty HTTP client connector reaches an origin, so that an extension can route
 * some origins over another network, such as an overlay network (OpenZiti), instead of TCP.
 *
 * <p>Set it on a client with the {@value #ATTRIBUTE} attribute of its context. For each request the
 * connector asks it for a {@link Route} to the origin's host and port. With a route, the request is
 * sent in origin form without the proxy credentials, over a connection made with the route's
 * channel factory and resolver to the unresolved host and port: the HTTP, HTTPS and SOCKS proxies
 * and HTTP/3 are not used. Without one, the connector connects over TCP as usual. TLS, HTTP/1.1 and
 * HTTP/2 run over the channel either way. Routed and TCP connections are pooled apart, so a
 * connection is only reused for requests routed the same way.
 *
 * <p>Since it is asked for each request, {@link #route(String, int)} must be fast. It may only
 * block for a bounded time, for example while an overlay network loads its services.
 *
 * <p>This is a connector SPI: it exposes Netty types and is meant for extensions that depend on
 * Netty themselves. The {@code org.restlet} module still requires Netty without {@code requires
 * transitive}, so applications that don't use it see no Netty type.
 */
@SuppressWarnings("exports")
public interface NettyClientTransport {

    /** The name of the client context attribute holding the transport. */
    String ATTRIBUTE = "org.restlet.engine.netty.clientTransport";

    /**
     * How to connect to an origin.
     *
     * @param channelFactory Creates the channel of each connection. Its channels must accept any
     *     event loop of the connector, which runs on NIO.
     * @param resolver Resolves the unresolved host and port given to the channel.
     */
    record Route(
            ChannelFactory<? extends Channel> channelFactory,
            AddressResolverGroup<? extends SocketAddress> resolver) {}

    /**
     * Returns the route to an origin, or null to connect to it over TCP.
     *
     * @param host The host name of the origin, unresolved.
     * @param port The port of the origin.
     * @return The route, or null.
     */
    Route route(String host, int port);
}
