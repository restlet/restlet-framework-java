/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.smithy.internal;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.smithy.SmithyServerAdapter;
import software.amazon.smithy.java.server.Route;
import software.amazon.smithy.java.server.Server;

/**
 * A smithy-java server built by {@code SmithyServerBuilder}: a Restlet server connector per
 * endpoint, dispatching each call to the adapter of its route. Single-use.
 */
public final class RestletSmithyServer implements Server {
    private final List<URI> endpoints;
    private final Context context;
    private final List<Route> routes;
    private final List<SmithyServerAdapter> adapters;
    private final List<org.restlet.Server> connectors = new ArrayList<>();

    /** Guards the lifecycle state. */
    private final Object lock = new Object();

    private boolean started;
    private CompletableFuture<Void> shutdown;

    public RestletSmithyServer(
            List<URI> endpoints,
            Context context,
            List<Route> routes,
            List<SmithyServerAdapter> adapters) {
        this.endpoints = List.copyOf(endpoints);
        this.context = context;
        this.routes = List.copyOf(routes);
        this.adapters = List.copyOf(adapters);
    }

    /** Starts the adapters, then a connector per endpoint. */
    @Override
    public void start() {
        synchronized (lock) {
            if (started) {
                throw new IllegalStateException("Server is single-use; already started");
            }
            started = true;
            try {
                for (SmithyServerAdapter adapter : adapters) {
                    adapter.start();
                }
                Restlet dispatcher = new Dispatcher();
                for (URI endpoint : endpoints) {
                    boolean secure = "https".equalsIgnoreCase(endpoint.getScheme());
                    Protocol protocol = secure ? Protocol.HTTPS : Protocol.HTTP;
                    int port =
                            endpoint.getPort() < 0 ? protocol.getDefaultPort() : endpoint.getPort();
                    var connector =
                            new org.restlet.Server(
                                    context, protocol, endpoint.getHost(), port, dispatcher);
                    connectors.add(connector);
                    connector.start();
                }
            } catch (Exception e) {
                stopAll();
                throw new IllegalStateException("Unable to start the Smithy server", e);
            }
        }
    }

    /** Stops the connectors, then the adapters, interrupting the operations still running. */
    @Override
    public CompletableFuture<Void> shutdown() {
        synchronized (lock) {
            if (shutdown == null) {
                started = true;
                shutdown = new CompletableFuture<>();
                CompletableFuture<Void> result = shutdown;
                Thread.ofVirtual()
                        .name("restlet-smithy-shutdown")
                        .start(
                                () -> {
                                    try {
                                        stopAll();
                                        result.complete(null);
                                    } catch (RuntimeException e) {
                                        result.completeExceptionally(e);
                                    }
                                });
            }
            return shutdown;
        }
    }

    /**
     * Returns the ports the connectors listen on, in the order of the endpoints, once started.
     *
     * @return The actual ports.
     */
    public List<Integer> getActualPorts() {
        synchronized (lock) {
            return connectors.stream().map(org.restlet.Server::getActualPort).toList();
        }
    }

    /** Stops every connector, then every adapter, keeping the first failure. */
    private void stopAll() {
        RuntimeException failure = null;
        List<Restlet> stopping = new ArrayList<>();
        synchronized (lock) {
            stopping.addAll(connectors);
        }
        stopping.addAll(adapters);
        for (Restlet restlet : stopping) {
            try {
                restlet.stop();
            } catch (Exception e) {
                if (failure == null) {
                    failure = new IllegalStateException("Unable to stop the Smithy server", e);
                } else {
                    failure.addSuppressed(e);
                }
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    /** Dispatches each call to the adapter of the first route matching its URI. */
    private final class Dispatcher extends Restlet {
        @Override
        public void handle(Request request, Response response) {
            Reference ref = request.getResourceRef();
            // The SDK resolves operations on the full path, below the host.
            ref.setBaseRef(ref.getHostIdentifier());
            for (int i = 0; i < routes.size(); i++) {
                if (matches(routes.get(i), ref)) {
                    adapters.get(i).handle(request, response);
                    return;
                }
            }
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
        }
    }

    /**
     * Indicates whether the prefix, port, scheme and host name of a route, when set, match a URI.
     *
     * @param route The route.
     * @param ref The URI of a call.
     * @return True if the route matches.
     */
    public static boolean matches(Route route, Reference ref) {
        String path = ref.getPath();
        if (route.getPathPrefix() != null
                && (path == null || !path.startsWith(route.getPathPrefix()))) {
            return false;
        }
        String scheme = ref.getScheme() == null ? "" : ref.getScheme().toLowerCase(Locale.ROOT);
        if (route.getProtocol() != null && !route.getProtocol().equalsIgnoreCase(scheme)) {
            return false;
        }
        if (route.getPort() != null) {
            int port = ref.getHostPort();
            if (port < 0 && ref.getSchemeProtocol() != null) {
                port = ref.getSchemeProtocol().getDefaultPort();
            }
            if (route.getPort() != port) {
                return false;
            }
        }
        return route.getHostName() == null
                || route.getHostName().equalsIgnoreCase(ref.getHostDomain());
    }
}
