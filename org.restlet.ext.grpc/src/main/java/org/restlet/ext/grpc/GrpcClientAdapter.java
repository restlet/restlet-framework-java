/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.grpc;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.internal.ManagedChannelImplBuilder;
import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.ext.grpc.internal.RestletTransportFactory;

/**
 * Single-use native gRPC channel on a borrowed Restlet HTTP/2 client.
 *
 * <p>This initial profile is unary and plaintext only; applications must explicitly opt in with
 * {@link #setPlaintext(boolean)}. TLS/address separation, proxy CONNECT, streaming, retry,
 * keepalive and physical-connection controls are not implemented. No SDK network transport is
 * selected. The host starts and stops the borrowed connector.
 */
public final class GrpcClientAdapter implements AutoCloseable {
    private final Context context;
    private final List<ClientInterceptor> interceptors = new CopyOnWriteArrayList<>();
    private Client client;
    private String target;
    private boolean plaintext;
    private int maxMessageBytes = 1024 * 1024;
    private int maxConcurrentCalls = 128;
    private Duration maxCallDuration = Duration.ofSeconds(30);
    private Duration shutdownTimeout = Duration.ofSeconds(5);
    private Executor executor;
    private boolean configured;
    private ManagedChannel channel;
    private RestletTransportFactory transportFactory;

    public GrpcClientAdapter() {
        this(new Context());
    }

    public GrpcClientAdapter(Context context) {
        this.context = Objects.requireNonNull(context, "context");
    }

    public Context getContext() {
        return context;
    }

    public synchronized void setClient(Client value) {
        requireConfigurable();
        client = Objects.requireNonNull(value, "client");
    }

    public synchronized Client getClient() {
        return client;
    }

    /** Sets a native DNS target, for example {@code dns:///localhost:8080}. */
    public synchronized void setTarget(String value) {
        requireConfigurable();
        target = Objects.requireNonNull(value, "target");
    }

    public synchronized String getTarget() {
        return target;
    }

    public synchronized void setPlaintext(boolean value) {
        requireConfigurable();
        plaintext = value;
    }

    public synchronized boolean isPlaintext() {
        return plaintext;
    }

    public synchronized void setMaxMessageBytes(int value) {
        requireConfigurable();
        if (value <= 0 || value > Integer.MAX_VALUE - 6) {
            throw new IllegalArgumentException("A positive finite message limit is required");
        }
        maxMessageBytes = value;
    }

    public synchronized int getMaxMessageBytes() {
        return maxMessageBytes;
    }

    public synchronized void setMaxConcurrentCalls(int value) {
        requireConfigurable();
        if (value <= 0) {
            throw new IllegalArgumentException("A positive call limit is required");
        }
        maxConcurrentCalls = value;
    }

    public synchronized int getMaxConcurrentCalls() {
        return maxConcurrentCalls;
    }

    public synchronized void setMaxCallDuration(Duration value) {
        requireConfigurable();
        maxCallDuration = positive(value);
    }

    public synchronized Duration getMaxCallDuration() {
        return maxCallDuration;
    }

    public synchronized void setShutdownTimeout(Duration value) {
        requireConfigurable();
        shutdownTimeout = positive(value);
    }

    public synchronized Duration getShutdownTimeout() {
        return shutdownTimeout;
    }

    /** Sets a borrowed SDK callback executor. The adapter never shuts it down. */
    public synchronized void setExecutor(Executor value) {
        requireConfigurable();
        executor = Objects.requireNonNull(value, "executor");
    }

    public List<ClientInterceptor> getInterceptors() {
        return interceptors;
    }

    private static Duration positive(Duration value) {
        Objects.requireNonNull(value, "duration");
        if (value.isNegative() || value.isZero()) {
            throw new IllegalArgumentException("A positive finite duration is required");
        }
        return Duration.ofNanos(value.toNanos());
    }

    private void requireConfigurable() {
        if (configured) {
            throw new IllegalStateException("gRPC adapter is single-use; configuration is fixed");
        }
    }

    public synchronized void start() {
        if (isStarted()) {
            return;
        }
        requireConfigurable();
        if (!plaintext) {
            throw new UnsupportedOperationException(
                    "R3 supports only explicitly enabled plaintext; TLS is not implemented");
        }
        Objects.requireNonNull(target, "target");
        if (client == null
                || !client.isStarted()
                || client.getContext() == null
                || !"HTTP2"
                        .equals(
                                client.getContext()
                                        .getParameters()
                                        .getFirstValue("httpClientTransportMode"))
                || !"false"
                        .equalsIgnoreCase(
                                client.getContext()
                                        .getParameters()
                                        .getFirstValue("followRedirects", "true"))) {
            throw new IllegalStateException(
                    "Borrow a started HTTP2 client with followRedirects=false");
        }
        if (!target.matches("(dns:///)?[^/?#]+:\\d+")) {
            throw new IllegalArgumentException("Expected host:port or dns:///host:port target");
        }
        configured = true;
        var factory =
                new RestletTransportFactory(
                        client, maxMessageBytes, maxConcurrentCalls, shutdownTimeout);
        try {
            var builder =
                    new ManagedChannelImplBuilder(target, () -> factory, () -> 80)
                            .disableRetry()
                            .disableServiceConfigLookUp()
                            .proxyDetector(address -> null)
                            .maxInboundMessageSize(maxMessageBytes)
                            .intercept(List.copyOf(interceptors))
                            .intercept(
                                    new ClientInterceptor() {
                                        @Override
                                        public <Q, S> ClientCall<Q, S> interceptCall(
                                                MethodDescriptor<Q, S> method,
                                                CallOptions options,
                                                Channel next) {
                                            var cap =
                                                    io.grpc.Deadline.after(
                                                            maxCallDuration.toNanos(),
                                                            TimeUnit.NANOSECONDS);
                                            var deadline = options.getDeadline();
                                            return next.newCall(
                                                    method,
                                                    options.withDeadline(
                                                            deadline == null
                                                                    ? cap
                                                                    : deadline.minimum(cap)));
                                        }
                                    });
            if (executor != null) {
                builder.executor(executor);
            }
            channel = builder.build();
            transportFactory = factory;
        } catch (RuntimeException e) {
            try {
                factory.close();
                factory.awaitTermination();
            } catch (RuntimeException cleanup) {
                e.addSuppressed(cleanup);
            }
            throw e;
        }
    }

    public synchronized boolean isStarted() {
        return channel != null && !channel.isShutdown();
    }

    public synchronized ManagedChannel getChannel() {
        if (!isStarted()) {
            throw new IllegalStateException("gRPC adapter is not started");
        }
        return channel;
    }

    public void stop() {
        ManagedChannel current;
        Duration timeout;
        synchronized (this) {
            current = channel;
            timeout = shutdownTimeout;
        }
        if (current == null) {
            return;
        }
        current.shutdown();
        RuntimeException failure = null;
        try {
            if (!current.awaitTermination(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
                current.shutdownNow();
                if (!current.awaitTermination(timeout.toNanos(), TimeUnit.NANOSECONDS)) {
                    throw new IllegalStateException("gRPC channel shutdown timed out");
                }
            }
        } catch (InterruptedException e) {
            current.shutdownNow();
            Thread.currentThread().interrupt();
            failure = new IllegalStateException("Interrupted awaiting gRPC channel shutdown", e);
        } catch (RuntimeException e) {
            failure = e;
        }
        try {
            transportFactory.close();
            transportFactory.awaitTermination();
        } catch (RuntimeException e) {
            if (failure == null) {
                failure = e;
            } else {
                failure.addSuppressed(e);
            }
        }
        if (failure != null) {
            throw failure;
        }
    }

    @Override
    public void close() {
        stop();
    }
}
