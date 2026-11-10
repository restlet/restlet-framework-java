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
 * <p>Set it on a client with the {@value #ATTRIBUTE} attribute of its context. For each new
 * connection the connector asks it for a {@link Route} to the origin's host and port: with a route,
 * the connection is made with the route's channel factory and resolver, to the unresolved host and
 * port, and the HTTP and HTTPS proxies are not used; without one, the connector connects over TCP
 * as usual. TLS, HTTP/1.1 and HTTP/2 run over the channel either way. Connections are pooled per
 * origin, so an origin is always reached the same way.
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
