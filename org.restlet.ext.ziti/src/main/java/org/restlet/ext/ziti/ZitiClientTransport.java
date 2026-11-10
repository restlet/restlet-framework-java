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
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openziti.ZitiContext;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.engine.netty.NettyClientTransport;

/**
 * Routes the HTTP client calls to the origins an OpenZiti identity can reach over the Ziti overlay
 * network, and the others over TCP.
 *
 * <p>An origin is routed over Ziti when the context has a service intercepting its host and port
 * ({@link ZitiContext#getService(InetSocketAddress, long)}), such as {@code crm.internal:443}. Its
 * host name is then never resolved by the system DNS: the connection is a {@link ZitiSocketChannel}
 * that dials that service. Every other origin is reached over TCP.
 *
 * <p>The context loads its services after authenticating. Until it has (its status is {@link
 * ZitiContext.Status.Active}), routing waits, up to a timeout counted from the creation of the
 * transport (10 seconds by default). Past it, the origins are reached over TCP until the services
 * are loaded: create the transport early, or wait for the context to be active, so that no
 * intercepted host name goes to the DNS. The lookups of the origins are cached, and the cache is
 * cleared on each service event of the context. TLS, HTTP/1.1 and HTTP/2 run over it like over TCP:
 * an {@code https} origin is encrypted end to end on top of the overlay's own encryption.
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

    private static final Logger LOGGER = Logger.getLogger(ZitiClientTransport.class.getName());

    /** The default time to wait for the services of the context. */
    private static final Duration DEFAULT_SERVICES_TIMEOUT = Duration.ofSeconds(10);

    /** The maximum number of origins whose lookup is cached. */
    private static final int MAX_CACHED_ORIGINS = 1024;

    /** The Ziti context. */
    private final ZitiContext context;

    /** The route over Ziti, shared by every intercepted origin. */
    private final Route route;

    /** Completed once the context has loaded its services. */
    private final CompletableFuture<Void> servicesLoaded = new CompletableFuture<>();

    /** The time, in {@link System#nanoTime()}, past which routing no longer waits for them. */
    private final long servicesDeadline;

    /** Whether each origin, as host:port, is intercepted by a service. */
    private final Map<String, Boolean> intercepted = new ConcurrentHashMap<>();

    /** Counts the service events, so that a lookup made meanwhile isn't cached. */
    private final AtomicLong serviceEvents = new AtomicLong();

    /**
     * Constructor, waiting up to 10 seconds for the services of the context.
     *
     * @param context The Ziti context of the identity used to dial the services.
     */
    public ZitiClientTransport(ZitiContext context) {
        this(context, DEFAULT_SERVICES_TIMEOUT);
    }

    /**
     * Constructor.
     *
     * @param context The Ziti context of the identity used to dial the services.
     * @param servicesTimeout The time to wait for the context to load its services, from now.
     */
    public ZitiClientTransport(ZitiContext context, Duration servicesTimeout) {
        this.context = context;
        // The channel maps the host and port to the Ziti service itself.
        this.route =
                new Route(() -> new ZitiSocketChannel(context), NoopAddressResolverGroup.INSTANCE);
        this.servicesDeadline = System.nanoTime() + servicesTimeout.toNanos();
        // Both run as long as the context. The status is active once the services are loaded.
        context.onStatus(
                status -> {
                    if (status instanceof ZitiContext.Status.Active) {
                        servicesLoaded.complete(null);
                    }
                });
        context.onServiceEvent(
                event -> {
                    serviceEvents.incrementAndGet();
                    intercepted.clear();
                });
    }

    /**
     * Routes a client's calls to the origins the Ziti identity can reach over Ziti.
     *
     * @param client The client, before it is started. A context is set on it if it has none.
     * @param context The Ziti context of the identity used to dial the services.
     * @return The transport set on the client.
     */
    public static ZitiClientTransport install(Client client, ZitiContext context) {
        ZitiClientTransport transport = new ZitiClientTransport(context);
        if (client.getContext() == null) {
            client.setContext(new Context());
        }
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
        if (!awaitServices()) {
            return null;
        }

        String origin = host + ":" + port;
        Boolean routed = intercepted.get(origin);
        if (routed == null) {
            long events = serviceEvents.get();
            routed = isIntercepted(host, port);
            if ((serviceEvents.get() == events) && (intercepted.size() < MAX_CACHED_ORIGINS)) {
                intercepted.put(origin, routed);
            }
        }
        return routed ? route : null;
    }

    /**
     * Waits for the context to load its services, until the deadline.
     *
     * @return True if they are loaded.
     */
    private boolean awaitServices() {
        if (servicesLoaded.isDone()) {
            return true;
        }

        long left = servicesDeadline - System.nanoTime();
        try {
            if (left > 0) {
                servicesLoaded.get(left, TimeUnit.NANOSECONDS);
                return true;
            }
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
        } catch (TimeoutException | ExecutionException _) {
            // Not loaded in time.
        }

        LOGGER.log(
                Level.WARNING,
                "The Ziti context {0} has not loaded its services: connecting over TCP",
                context.name());
        return false;
    }

    /**
     * Indicates whether a service of the context intercepts an origin, without waiting for a
     * service event: {@link ZitiContext#getService(InetSocketAddress)} waits for one, forever if
     * the identity has no service.
     *
     * @param host The host name of the origin.
     * @param port The port of the origin.
     * @return True if a service intercepts it.
     */
    private boolean isIntercepted(String host, int port) {
        try {
            // Looks the loaded services up, then times out at once.
            return context.getService(InetSocketAddress.createUnresolved(host, port), 0L) != null;
        } catch (Exception e) {
            // Kotlin doesn't declare the TimeoutException thrown when no service intercepts it.
            if (e instanceof TimeoutException) {
                return false;
            }
            throw e;
        }
    }
}
