/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.ziti;

import io.netty.resolver.NoopAddressResolverGroup;
import java.net.InetSocketAddress;
import org.openziti.ZitiContext;
import org.restlet.Client;
import org.restlet.engine.netty.NettyClientTransport;

/**
 * Routes the HTTP client calls to the origins an OpenZiti identity can reach over the Ziti overlay
 * network, and the others over TCP.
 *
 * <p>An origin is routed over Ziti when the context has a service intercepting its host and port
 * ({@link ZitiContext#getService(InetSocketAddress)}), such as {@code crm.internal:443}. Its host
 * name is then never resolved by the system DNS: the connection is a {@link ZitiSocketChannel} that
 * dials that service. TLS, HTTP/1.1 and HTTP/2 run over it like over TCP: an {@code https} origin
 * is encrypted end to end on top of the overlay's own encryption.
 *
 * <pre>
 * ZitiContext ztx = Ziti.newContext(identityFile, new char[0]);
 * Client client = new Client(Protocol.HTTPS);
 * ZitiClientTransport.install(client, ztx);
 * </pre>
 *
 * <p>The context is not owned: destroy it after the client is stopped.
 */
public class ZitiClientTransport implements NettyClientTransport {

    /** The Ziti context. */
    private final ZitiContext context;

    /** The route over Ziti, shared by every intercepted origin. */
    private final Route route;

    /**
     * Constructor.
     *
     * @param context The Ziti context of the identity used to dial the services.
     */
    public ZitiClientTransport(ZitiContext context) {
        this.context = context;
        // The channel maps the host and port to the Ziti service itself.
        this.route =
                new Route(() -> new ZitiSocketChannel(context), NoopAddressResolverGroup.INSTANCE);
    }

    /**
     * Routes a client's calls to the origins the Ziti identity can reach over Ziti.
     *
     * @param client The client, before it is started.
     * @param context The Ziti context of the identity used to dial the services.
     * @return The transport set on the client.
     */
    public static ZitiClientTransport install(Client client, ZitiContext context) {
        ZitiClientTransport transport = new ZitiClientTransport(context);
        client.getContext().getAttributes().put(NettyClientTransport.ATTRIBUTE, transport);
        return transport;
    }

    /**
     * Returns the Ziti context.
     *
     * @return The Ziti context.
     */
    public ZitiContext getContext() {
        return context;
    }

    @Override
    public Route route(String host, int port) {
        return (context.getService(InetSocketAddress.createUnresolved(host, port)) != null)
                ? route
                : null;
    }
}
