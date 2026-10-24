/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.grpc.internal;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.Attributes;
import io.grpc.CallOptions;
import io.grpc.ChannelCredentials;
import io.grpc.ChannelLogger;
import io.grpc.ClientStreamTracer;
import io.grpc.InternalChannelz;
import io.grpc.InternalLogId;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.internal.ClientStream;
import io.grpc.internal.ClientTransportFactory;
import io.grpc.internal.ConnectionClientTransport;
import io.grpc.internal.FailingClientStream;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.time.Duration;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import org.restlet.Client;

/** Pinned grpc-java internal SPI. A logical transport is not a physical Netty connection. */
public final class RestletTransportFactory implements ClientTransportFactory {
    private final Client client;
    private final int maxBytes;
    private final Duration shutdownTimeout;
    private final Semaphore admission;
    private final Set<Transport> transports = new HashSet<>();
    private final ExecutorService io = Executors.newVirtualThreadPerTaskExecutor();
    private final ScheduledExecutorService timer =
            Executors.newSingleThreadScheduledExecutor(
                    Thread.ofPlatform().daemon().name("restlet-grpc-timer").factory());
    private boolean closed;

    public RestletTransportFactory(Client client, int maxBytes, int maxCalls, Duration timeout) {
        this.client = client;
        this.maxBytes = maxBytes;
        admission = new Semaphore(maxCalls);
        shutdownTimeout = timeout;
    }

    @Override
    public synchronized ConnectionClientTransport newClientTransport(
            SocketAddress address, ClientTransportOptions options, ChannelLogger logger) {
        if (closed) {
            throw new IllegalStateException("gRPC transport factory is closed");
        }
        if (!(address instanceof InetSocketAddress inet)
                || inet.isUnresolved()
                || options.getHttpConnectProxiedSocketAddress() != null) {
            throw new IllegalArgumentException(
                    "Only resolved Internet addresses without proxy CONNECT are supported");
        }
        Transport transport = new Transport(inet, options.getAuthority());
        transports.add(transport);
        return transport;
    }

    @Override
    public ScheduledExecutorService getScheduledExecutorService() {
        return timer;
    }

    @Override
    public SwapChannelCredentialsResult swapChannelCredentials(ChannelCredentials credentials) {
        throw new UnsupportedOperationException("Channel credentials/TLS are not supported in R3");
    }

    @Override
    public Collection<Class<? extends SocketAddress>> getSupportedSocketAddressTypes() {
        return Set.of(InetSocketAddress.class);
    }

    @Override
    public void close() {
        Set<Transport> snapshot;
        synchronized (this) {
            if (closed) {
                return;
            }
            closed = true;
            snapshot = Set.copyOf(transports);
        }
        snapshot.forEach(t -> t.shutdownNow(Status.UNAVAILABLE.withDescription("Adapter closed")));
        timer.shutdownNow();
        io.shutdownNow();
    }

    /** Called by the facade, never from the SDK transport callback that may close the factory. */
    public void awaitTermination() {
        try {
            if (!io.awaitTermination(shutdownTimeout.toNanos(), TimeUnit.NANOSECONDS)
                    || !timer.awaitTermination(shutdownTimeout.toNanos(), TimeUnit.NANOSECONDS)) {
                throw new IllegalStateException("gRPC transport worker shutdown timed out");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted closing gRPC transport workers", e);
        }
    }

    private final class Transport implements ConnectionClientTransport {
        private final InetSocketAddress address;
        private final String authority;
        private final Set<RestletClientStream> streams = new HashSet<>();
        private final InternalLogId id = InternalLogId.allocate(Transport.class, "Restlet HTTP/2");
        private Listener listener;
        private Status shutdown;
        private boolean terminated;

        private Transport(InetSocketAddress address, String authority) {
            this.address = address;
            this.authority = authority;
        }

        @Override
        public synchronized Runnable start(Listener value) {
            listener = value;
            return () -> {
                synchronized (Transport.this) {
                    if (shutdown == null) {
                        listener.transportReady();
                    }
                }
            };
        }

        @Override
        public synchronized ClientStream newStream(
                MethodDescriptor<?, ?> method,
                Metadata headers,
                CallOptions options,
                ClientStreamTracer[] tracers) {
            Status rejected = shutdown;
            if (rejected == null
                    && options.getAuthority() != null
                    && !authority.equals(options.getAuthority())) {
                rejected =
                        Status.UNIMPLEMENTED.withDescription(
                                "Per-call authority override is not supported");
            }
            if (rejected == null && method.getType() != MethodDescriptor.MethodType.UNARY) {
                rejected = Status.UNIMPLEMENTED.withDescription("R3 supports unary methods only");
            }
            if (rejected == null
                    && options.getCompressor() != null
                    && !"identity".equals(options.getCompressor())) {
                rejected =
                        Status.UNIMPLEMENTED.withDescription(
                                "Request compression is not supported");
            }
            if (rejected != null) {
                return new FailingClientStream(rejected, tracers);
            }
            if (!admission.tryAcquire()) {
                return new FailingClientStream(
                        Status.RESOURCE_EXHAUSTED.withDescription("gRPC call limit exceeded"),
                        tracers);
            }
            RestletClientStream stream =
                    new RestletClientStream(
                            client,
                            address,
                            authority,
                            method,
                            headers,
                            options,
                            tracers,
                            maxBytes,
                            io,
                            this::finished);
            streams.add(stream);
            if (streams.size() == 1) {
                listener.transportInUse(true);
            }
            return new GuardedClientStream(stream, maxBytes);
        }

        private synchronized void finished(RestletClientStream stream) {
            if (streams.remove(stream)) {
                admission.release();
                if (streams.isEmpty()) {
                    listener.transportInUse(false);
                    terminateIfIdle();
                }
            }
        }

        @Override
        public synchronized void shutdown(Status status) {
            if (shutdown == null) {
                shutdown = status;
                listener.transportShutdown(status, null);
                terminateIfIdle();
            }
        }

        private void terminateIfIdle() {
            if (shutdown != null && streams.isEmpty() && !terminated) {
                terminated = true;
                listener.transportTerminated();
                synchronized (RestletTransportFactory.this) {
                    transports.remove(this);
                }
            }
        }

        @Override
        public void shutdownNow(Status status) {
            Set<RestletClientStream> active;
            synchronized (this) {
                shutdown(status);
                active = Set.copyOf(streams);
            }
            active.forEach(s -> s.cancel(status));
        }

        @Override
        public void ping(PingCallback callback, Executor executor) {
            executor.execute(
                    () ->
                            callback.onFailure(
                                    Status.UNIMPLEMENTED.withDescription(
                                            "Physical connection PING is not supported")));
        }

        @Override
        public ListenableFuture<InternalChannelz.SocketStats> getStats() {
            // Restlet owns the pool; exposing fabricated per-socket counters would be misleading.
            return Futures.immediateFuture(null);
        }

        @Override
        public InternalLogId getLogId() {
            return id;
        }

        @Override
        public Attributes getAttributes() {
            return Attributes.EMPTY;
        }
    }
}
