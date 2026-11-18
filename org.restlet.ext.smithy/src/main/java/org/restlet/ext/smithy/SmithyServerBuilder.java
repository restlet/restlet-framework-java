/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.smithy;

import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.restlet.Context;
import software.amazon.smithy.java.server.Route;
import software.amazon.smithy.java.server.Server;
import software.amazon.smithy.java.server.ServerBuilder;

/**
 * Experimental builder of smithy-java servers listening on Restlet HTTP server connectors, one per
 * endpoint, each route of services hosted by a {@link SmithyServerAdapter}.
 *
 * <p>On the class path, {@code Server.builder("restlet")} finds it, and so does {@code
 * Server.builder()} when the SDK's Netty server isn't there too. It can also be created directly,
 * to set the Restlet settings that the SDK's builder lacks: the connectors' context, for example
 * with an {@code http.transport.protocol} parameter set to {@code HTTP2} or the TLS parameters of
 * {@code https} endpoints, and the adapters' limits. {@link #numberOfWorkers(int)} sets the number
 * of calls each route handles at once.
 *
 * <p>A call goes to the first route whose path prefix, host name, and connector port and scheme,
 * when set, match it, and that resolves its operation. When the routes only set path prefixes, as
 * with services added under paths, whose order the SDK doesn't keep, the longest matching prefix
 * wins. Operations are resolved on the full path, as with the SDK's Netty server.
 */
// The SDK server types come from the class path, which a named module can't export.
@SuppressWarnings("exports")
public final class SmithyServerBuilder extends ServerBuilder<SmithyServerBuilder> {
    private List<URI> endpoints = List.of(URI.create("http://localhost:8080"));
    private Context context;
    private Integer maxConcurrentCalls;
    private Long maxRequestBytes;
    private Duration maxCallDuration;
    private List<Route> routes = List.of();

    /**
     * Sets the endpoints to listen on, {@code http://localhost:8080} by default. Each needs the
     * {@code http} or {@code https} scheme, listens on the default port of its scheme when it has
     * none, and has a host but no user info, path, query or fragment.
     *
     * @param endpoints The endpoints.
     * @return This builder.
     */
    @Override
    public SmithyServerBuilder endpoints(URI... endpoints) {
        if (endpoints == null || endpoints.length == 0) {
            throw new IllegalArgumentException("At least one endpoint is required");
        }
        var checked = new ArrayList<URI>(endpoints.length);
        for (URI endpoint : endpoints) {
            Objects.requireNonNull(endpoint, "endpoint");
            String scheme =
                    endpoint.getScheme() == null
                            ? ""
                            : endpoint.getScheme().toLowerCase(Locale.ROOT);
            if (!scheme.equals("http") && !scheme.equals("https")) {
                throw new IllegalArgumentException("Not an HTTP or HTTPS endpoint: " + endpoint);
            }
            String path = endpoint.getRawPath();
            if (endpoint.getHost() == null) {
                // Opaque, or an authority that isn't a host name: never listen everywhere.
                throw new IllegalArgumentException("An endpoint needs a host: " + endpoint);
            }
            if (endpoint.getRawUserInfo() != null
                    || (path != null && !path.isEmpty() && !path.equals("/"))
                    || endpoint.getRawQuery() != null
                    || endpoint.getRawFragment() != null) {
                throw new IllegalArgumentException(
                        "An endpoint only has a scheme, a host and a port: " + endpoint);
            }
            checked.add(endpoint);
        }
        this.endpoints = List.copyOf(checked);
        return self();
    }

    /**
     * Sets the number of calls each route handles at once, like {@link
     * SmithyServerAdapter#setMaxConcurrentCalls(int)}.
     *
     * @param numberOfWorkers A positive number of calls.
     * @return This builder.
     */
    @Override
    public SmithyServerBuilder numberOfWorkers(int numberOfWorkers) {
        if (numberOfWorkers <= 0) {
            throw new IllegalArgumentException("A positive number of workers is required");
        }
        this.maxConcurrentCalls = numberOfWorkers;
        return self();
    }

    /**
     * Sets the context of the server connectors, whose parameters configure them.
     *
     * @param context The context of the server connectors.
     * @return This builder.
     */
    public SmithyServerBuilder context(Context context) {
        this.context = Objects.requireNonNull(context, "context");
        return self();
    }

    /**
     * Sets the largest request body accepted, like {@link
     * SmithyServerAdapter#setMaxRequestBytes(long)}.
     *
     * @param value A positive number of bytes.
     * @return This builder.
     */
    public SmithyServerBuilder maxRequestBytes(long value) {
        if (value <= 0) {
            throw new IllegalArgumentException("A positive request size is required");
        }
        this.maxRequestBytes = value;
        return self();
    }

    /**
     * Sets the time an operation may run, like {@link
     * SmithyServerAdapter#setMaxCallDuration(Duration)}.
     *
     * @param value A positive duration.
     * @return This builder.
     */
    public SmithyServerBuilder maxCallDuration(Duration value) {
        Objects.requireNonNull(value, "value");
        if (value.isNegative() || value.isZero()) {
            throw new IllegalArgumentException("A positive duration is required");
        }
        this.maxCallDuration = value;
        return self();
    }

    @Override
    protected SmithyServerBuilder setServerRoutes(List<Route> routes) {
        for (Route route : routes) {
            if (route.getServices() == null || route.getServices().isEmpty()) {
                throw new IllegalArgumentException("A route has no service");
            }
        }
        // The SDK collects the services added under paths in a hash map: order them so that "/"
        // doesn't take the calls of a longer prefix. Routes that also match hosts, ports or schemes
        // keep the order they were added in.
        boolean prefixesOnly =
                routes.stream()
                        .allMatch(
                                route ->
                                        route.getHostName() == null
                                                && route.getPort() == null
                                                && route.getProtocol() == null);
        this.routes =
                !prefixesOnly
                        ? List.copyOf(routes)
                        : routes.stream()
                                .sorted(
                                        Comparator.comparingInt(
                                                        (Route route) ->
                                                                route.getPathPrefix() == null
                                                                        ? 0
                                                                        : route.getPathPrefix()
                                                                                .length())
                                                .reversed())
                                .toList();
        return self();
    }

    @Override
    protected Server buildServer() {
        if (routes.isEmpty()) {
            throw new IllegalStateException("Add at least one service before building");
        }
        var hosted = new ArrayList<RestletSmithyServer.Hosted>(routes.size());
        for (Route route : routes) {
            var adapter = new SmithyServerAdapter();
            adapter.getServices().addAll(route.getServices());
            if (maxConcurrentCalls != null) {
                adapter.setMaxConcurrentCalls(maxConcurrentCalls);
            }
            if (maxRequestBytes != null) {
                adapter.setMaxRequestBytes(maxRequestBytes);
            }
            if (maxCallDuration != null) {
                adapter.setMaxCallDuration(maxCallDuration);
            }
            hosted.add(new RestletSmithyServer.Hosted(route, adapter));
        }
        return new RestletSmithyServer(
                endpoints, context == null ? new Context() : context, List.copyOf(hosted));
    }
}
