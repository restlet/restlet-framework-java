/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.grpc;

import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.internal.ServerImplBuilder;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.ext.grpc.internal.RestletServerHost;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

/**
 * Socketless grpc-java server on a borrowed Restlet HTTP/2 listener.
 *
 * <p>Supports unary requests and server-streamed responses, with finite message, queue, concurrency
 * and duration budgets. Does not implement TLS or physical connection controls. The host
 * application owns the HTTP listener and any supplied callback executor.
 *
 * <p>Register services before {@link #start()}, attach them to a router with {@link #attachTo}, and
 * configure the host listener for plaintext HTTP/2. This adapter is single-use. Stopping it cancels
 * active calls, waits up to the shutdown timeout for SDK and owned callback termination, and shuts
 * down only its own executor and timer, not the listener or an executor supplied with {@link
 * #setExecutor}.
 */
public final class GrpcServerAdapter extends Restlet {
    private final List<ServerServiceDefinition> services = new CopyOnWriteArrayList<>();
    private final List<ServerInterceptor> interceptors = new CopyOnWriteArrayList<>();
    private int maxMessageBytes = 1024 * 1024;
    private int maxQueuedBytes = 256 * 1024;
    private int maxConcurrentCalls = 128;
    private Duration maxCallDuration = Duration.ofSeconds(5);
    private Duration shutdownTimeout = Duration.ofSeconds(5);
    private Executor executor;
    private ExecutorService ownedExecutor;
    private io.grpc.Server sdk;
    private RestletServerHost host;
    private boolean configured;

    public GrpcServerAdapter() {
        this(new Context());
    }

    public GrpcServerAdapter(Context context) {
        super(context);
    }

    public List<ServerServiceDefinition> getServices() {
        return services;
    }

    public List<ServerInterceptor> getInterceptors() {
        return interceptors;
    }

    public synchronized void setMaxMessageBytes(int value) {
        configurable();
        if (value <= 0 || value > Integer.MAX_VALUE - 6) {
            throw new IllegalArgumentException("A positive finite message budget is required");
        }
        maxMessageBytes = value;
    }

    public synchronized int getMaxMessageBytes() {
        return maxMessageBytes;
    }

    public synchronized void setMaxQueuedBytes(int value) {
        configurable();
        if (value < 16384) {
            throw new IllegalArgumentException("Queue budget must be at least 16384 bytes");
        }
        maxQueuedBytes = value;
    }

    public synchronized int getMaxQueuedBytes() {
        return maxQueuedBytes;
    }

    public synchronized void setMaxConcurrentCalls(int value) {
        configurable();
        if (value <= 0) {
            throw new IllegalArgumentException("A positive call budget is required");
        }
        maxConcurrentCalls = value;
    }

    public synchronized int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public synchronized void setMaxCallDuration(Duration value) {
        configurable();
        maxCallDuration = positive(value);
    }

    public synchronized Duration getMaxCallDuration() {
        return maxCallDuration;
    }

    public synchronized void setShutdownTimeout(Duration value) {
        configurable();
        shutdownTimeout = positive(value);
    }

    public synchronized Duration getShutdownTimeout() {
        return shutdownTimeout;
    }

    public synchronized void setExecutor(Executor value) {
        configurable();
        executor = java.util.Objects.requireNonNull(value, "executor");
    }

    /**
     * Returns the callback executor: the supplied one, the adapter-owned one once started, or null
     * before start when none was supplied.
     */
    public synchronized Executor getExecutor() {
        return executor;
    }

    private static Duration positive(Duration value) {
        if (value.isNegative() || value.isZero()) {
            throw new IllegalArgumentException("A positive duration is required");
        }
        return Duration.ofNanos(value.toNanos());
    }

    private void configurable() {
        if (configured) {
            throw new IllegalStateException("Server adapter is single-use; configuration is fixed");
        }
    }

    /** Attaches gRPC service prefixes while leaving other routes on the same listener untouched. */
    public void attachTo(Router router) {
        for (var service : List.copyOf(services)) {
            router.attach(
                    "/" + service.getServiceDescriptor().getName() + "/",
                    this,
                    Template.MODE_STARTS_WITH);
        }
    }

    @Override
    public synchronized void start() throws Exception {
        if (isStarted()) {
            return;
        }
        configurable();
        for (var service : List.copyOf(services)) {
            for (var method : service.getMethods()) {
                var type = method.getMethodDescriptor().getType();
                if (type != io.grpc.MethodDescriptor.MethodType.UNARY
                        && type != io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING) {
                    throw new UnsupportedOperationException(
                            "Only unary and server-streaming methods are supported");
                }
            }
        }
        configured = true;
        host =
                new RestletServerHost(
                        maxMessageBytes, maxQueuedBytes, maxConcurrentCalls, maxCallDuration);
        if (executor == null) {
            ownedExecutor = Executors.newVirtualThreadPerTaskExecutor();
            executor = ownedExecutor;
        }
        var builder =
                new ServerImplBuilder(
                                (tracers, recorder) -> {
                                    host.setTracers(tracers);
                                    return host;
                                })
                        .executor(executor)
                        .maxInboundMessageSize(maxMessageBytes);
        List.copyOf(services).forEach(builder::addService);
        List.copyOf(interceptors).forEach(builder::intercept);
        try {
            sdk = builder.build().start();
            super.start();
        } catch (IOException | RuntimeException e) {
            host.close();
            if (ownedExecutor != null) {
                ownedExecutor.shutdownNow();
            }
            throw e;
        }
    }

    @Override
    public void handle(Request request, Response response) {
        RestletServerHost current;
        synchronized (this) {
            current = isStarted() ? host : null;
        }
        if (current == null) {
            response.setStatus(org.restlet.data.Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
            return;
        }
        current.handle(request, response);
    }

    @Override
    public synchronized void stop() throws Exception {
        if (sdk == null) {
            return;
        }
        sdk.shutdownNow();
        if (host != null) {
            host.close();
        }
        long started = System.nanoTime();
        try {
            if (!sdk.awaitTermination(shutdownTimeout.toNanos(), TimeUnit.NANOSECONDS)) {
                throw new IllegalStateException("gRPC server shutdown timed out");
            }
            if (ownedExecutor != null) {
                ownedExecutor.shutdown();
                long remaining = shutdownTimeout.toNanos() - (System.nanoTime() - started);
                if (!ownedExecutor.awaitTermination(Math.max(0, remaining), TimeUnit.NANOSECONDS)) {
                    throw new IllegalStateException("gRPC callback executor shutdown timed out");
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } finally {
            if (ownedExecutor != null) {
                ownedExecutor.shutdownNow();
            }
            super.stop();
        }
    }
}
