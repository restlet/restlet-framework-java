/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.smithy;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Status;
import org.restlet.ext.smithy.internal.SmithyServerHost;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import software.amazon.smithy.java.server.Service;

/**
 * Experimental host of smithy-java server services, generated or built from a model, on Restlet
 * HTTP connectors.
 *
 * <p>The SDK's protocols (such as restJson1 or RPCv2 CBOR, whose modules the application adds)
 * resolve each operation and serialize its input, output and errors; the SDK validates the input.
 * Restlet owns the HTTP exchange: each call runs the SDK's handler chain on the request's thread,
 * streams the request body to the SDK and the response body back, over HTTP/1.1 or HTTP/2.
 *
 * <p>Register services before {@link #start()}, then {@link #attachTo(Router) attach} the adapter
 * to a router, beside other routes. Calls are bounded: a request body larger than {@link
 * #setMaxRequestBytes} gets a 413 response, calls beyond {@link #setMaxConcurrentCalls} get a 503
 * response (a call counts while its operation runs and its response body is written), and an
 * operation still running after {@link #setMaxCallDuration} is interrupted, its call getting a 503
 * response. A client disconnecting also interrupts its operation, and stopping the adapter
 * interrupts every running one: the SDK has no cancellation of its own. This adapter is single-use.
 *
 * <p>{@link SmithyServerBuilder} builds SDK servers of such adapters, listening on Restlet server
 * connectors.
 *
 * <p>The smithy-java server API is unstable ({@code @SmithyUnstableApi}) in 1.8.0, and its modules
 * split a package, so that they only run on the class path. The optional {@code server-core}
 * dependency of this extension must then be added to the class path. A named module using the
 * server reads it, as this module does, with {@code --add-reads
 * org.restlet.ext.smithy=ALL-UNNAMED}, when compiled and run. Event streams are not supported.
 */
public final class SmithyServerAdapter extends Restlet {
    private final List<Service> services = new CopyOnWriteArrayList<>();

    /** Guards the configuration and lifecycle state. */
    private final Object lock = new Object();

    private long maxRequestBytes = 10L * 1024 * 1024;
    private int maxConcurrentCalls = 128;
    private Duration maxCallDuration = Duration.ofSeconds(30);
    private boolean configured;
    private volatile SmithyServerHost host;

    public SmithyServerAdapter() {
        this(new Context());
    }

    public SmithyServerAdapter(Context context) {
        super(context);
    }

    /**
     * Returns the hosted services, to register before {@link #start()}.
     *
     * @return The modifiable list of services.
     */
    // Service comes from the SDK server on the class path, which a named module can't export.
    @SuppressWarnings("exports")
    public List<Service> getServices() {
        return services;
    }

    /**
     * Sets the largest request body accepted, 10 MiB by default.
     *
     * @param value A positive number of bytes.
     */
    public void setMaxRequestBytes(long value) {
        synchronized (lock) {
            configurable();
            if (value <= 0) {
                throw new IllegalArgumentException("A positive request size is required");
            }
            maxRequestBytes = value;
        }
    }

    public long getMaxRequestBytes() {
        synchronized (lock) {
            return maxRequestBytes;
        }
    }

    /**
     * Sets the number of calls handled at once, 128 by default. A call holds its slot until its
     * response is sent.
     *
     * @param value A positive number of calls.
     */
    public void setMaxConcurrentCalls(int value) {
        synchronized (lock) {
            configurable();
            if (value <= 0) {
                throw new IllegalArgumentException("A positive call limit is required");
            }
            maxConcurrentCalls = value;
        }
    }

    public int getMaxConcurrentCalls() {
        synchronized (lock) {
            return maxConcurrentCalls;
        }
    }

    /**
     * Sets the time an operation may run, 30 seconds by default. Streaming its response body
     * afterwards is not bounded.
     *
     * @param value A positive duration.
     */
    public void setMaxCallDuration(Duration value) {
        synchronized (lock) {
            configurable();
            if (value.isNegative() || value.isZero()) {
                throw new IllegalArgumentException("A positive duration is required");
            }
            maxCallDuration = value;
        }
    }

    public Duration getMaxCallDuration() {
        synchronized (lock) {
            return maxCallDuration;
        }
    }

    /** Requires the configuration to be still open; called with {@link #lock} held. */
    private void configurable() {
        if (configured) {
            throw new IllegalStateException("Server adapter is single-use; configuration is fixed");
        }
    }

    /**
     * Attaches the adapter to a router with a route that only matches the calls of a hosted
     * operation, so that other routes of the router keep their calls. Operations are resolved on
     * the path below the router's own base, once the adapter is started.
     *
     * @param router The router.
     */
    public void attachTo(Router router) {
        router.getRoutes()
                .add(
                        new Route(router, this) {
                            @Override
                            public float score(Request request, Response response) {
                                return resolves(request) ? 1F : 0F;
                            }
                        });
    }

    /**
     * Indicates whether the started adapter resolves the operation of a call.
     *
     * @param request The call's request.
     * @return True if a hosted operation takes the call.
     */
    boolean resolves(Request request) {
        SmithyServerHost current = host;
        return current != null && current.resolves(request);
    }

    @Override
    public synchronized void start() throws Exception {
        synchronized (lock) {
            if (isStarted()) {
                return;
            }
            configurable();
            if (services.isEmpty()) {
                throw new IllegalStateException("Register at least one service before starting");
            }
            configured = true;
            host =
                    new SmithyServerHost(
                            List.copyOf(services),
                            maxRequestBytes,
                            maxConcurrentCalls,
                            maxCallDuration);
            super.start();
        }
    }

    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);
        SmithyServerHost current = host;
        if (current == null || !isStarted()) {
            response.setStatus(Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
            return;
        }
        current.handle(request, response);
    }

    /** Stops the adapter, interrupting the operations still running. */
    @Override
    public synchronized void stop() throws Exception {
        SmithyServerHost stopping;
        synchronized (lock) {
            stopping = host;
            host = null;
        }
        try {
            if (stopping != null) {
                stopping.close();
            }
        } finally {
            super.stop();
        }
    }
}
