/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.connector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.protobuf.StringValue;
import io.grpc.Attributes;
import io.grpc.CallOptions;
import io.grpc.InternalChannelz;
import io.grpc.InternalInstrumented;
import io.grpc.InternalLogId;
import io.grpc.InternalMetadata;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.ServerStreamTracer;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.internal.AbstractServerStream;
import io.grpc.internal.InternalServer;
import io.grpc.internal.ReadableBuffers;
import io.grpc.internal.ServerImplBuilder;
import io.grpc.internal.ServerListener;
import io.grpc.internal.ServerTransport;
import io.grpc.internal.ServerTransportListener;
import io.grpc.internal.StatsTraceContext;
import io.grpc.internal.TransportTracer;
import io.grpc.internal.WritableBuffer;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.ServerCalls;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.representation.ByteArrayRepresentation;

class GrpcSdkServerHostSpikeTestCase {

    private static final int MAX_BYTES = 1024 * 1024;

    private static final MethodDescriptor<StringValue, StringValue> ECHO =
            MethodDescriptor.<StringValue, StringValue>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName("restlet.r0.Native/Echo")
                    .setRequestMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .build();

    private static final MethodDescriptor<StringValue, StringValue> COLLECT =
            ECHO.toBuilder()
                    .setType(MethodDescriptor.MethodType.CLIENT_STREAMING)
                    .setFullMethodName("restlet.r0.Native/Collect")
                    .build();

    @Test
    void hostsSdkRuntimeAndFramingWithoutAnSdkListener() throws Exception {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        SocketlessHost host = new SocketlessHost();
        Context context = new Context();
        context.getParameters().add("http.transport.protocol", "HTTP2");
        Server http = new Server(context, Protocol.HTTP, 0, host);
        AtomicInteger intercepted = new AtomicInteger();
        AtomicInteger handled = new AtomicInteger();
        try (var applications = Executors.newVirtualThreadPerTaskExecutor()) {
            io.grpc.Server sdk =
                    new ServerImplBuilder(
                                    (factories, recorder) -> {
                                        host.tracers = List.copyOf(factories);
                                        return host;
                                    })
                            .executor(applications)
                            .intercept(
                                    new io.grpc.ServerInterceptor() {
                                        @Override
                                        public <Q, S> io.grpc.ServerCall.Listener<Q> interceptCall(
                                                io.grpc.ServerCall<Q, S> call,
                                                Metadata headers,
                                                io.grpc.ServerCallHandler<Q, S> next) {
                                            intercepted.incrementAndGet();
                                            assertEquals(
                                                    "localhost:" + http.getActualPort(),
                                                    call.getAuthority());
                                            return next.startCall(call, headers);
                                        }
                                    })
                            .addService(
                                    ServerServiceDefinition.builder("restlet.r0.Native")
                                            .addMethod(
                                                    ECHO,
                                                    ServerCalls.asyncUnaryCall(
                                                            (StringValue request,
                                                                    io.grpc.stub.StreamObserver<
                                                                                    StringValue>
                                                                            out) -> {
                                                                handled.incrementAndGet();
                                                                if ("bad"
                                                                        .equals(
                                                                                request
                                                                                        .getValue())) {
                                                                    out.onError(
                                                                            Status.INVALID_ARGUMENT
                                                                                    .withDescription(
                                                                                            "native % error: caf\u00e9")
                                                                                    .asRuntimeException());
                                                                } else {
                                                                    out.onNext(
                                                                            StringValue.of(
                                                                                    "native:"
                                                                                            + request
                                                                                                    .getValue()));
                                                                    out.onCompleted();
                                                                }
                                                            }))
                                            .addMethod(
                                                    COLLECT,
                                                    ServerCalls.asyncClientStreamingCall(
                                                            out -> {
                                                                handled.incrementAndGet();
                                                                return new io.grpc.stub
                                                                                .StreamObserver<
                                                                        StringValue>() {
                                                                    private final List<String>
                                                                            values =
                                                                                    new ArrayList<>();

                                                                    @Override
                                                                    public void onNext(
                                                                            StringValue value) {
                                                                        values.add(
                                                                                value.getValue());
                                                                    }

                                                                    @Override
                                                                    public void onError(
                                                                            Throwable error) {
                                                                        out.onError(error);
                                                                    }

                                                                    @Override
                                                                    public void onCompleted() {
                                                                        out.onNext(
                                                                                StringValue.of(
                                                                                        String.join(
                                                                                                ",",
                                                                                                values)));
                                                                        out.onCompleted();
                                                                    }
                                                                };
                                                            }))
                                            .build())
                            .build();
            try {
                sdk.start();
                http.start();
                var channel =
                        NettyChannelBuilder.forAddress("localhost", http.getActualPort())
                                .usePlaintext()
                                .build();
                try {
                    assertEquals(
                            "native:hello",
                            ClientCalls.blockingUnaryCall(
                                            channel,
                                            ECHO,
                                            CallOptions.DEFAULT,
                                            StringValue.of("hello"))
                                    .getValue());
                    var error =
                            assertThrows(
                                    StatusRuntimeException.class,
                                    () ->
                                            ClientCalls.blockingUnaryCall(
                                                    channel,
                                                    ECHO,
                                                    CallOptions.DEFAULT,
                                                    StringValue.of("bad")));
                    assertEquals(
                            Status.Code.INVALID_ARGUMENT,
                            error.getStatus().getCode(),
                            error.getStatus().toString());
                    assertEquals("native % error: caf\u00e9", error.getStatus().getDescription());
                    var unknown =
                            ECHO.toBuilder().setFullMethodName("restlet.r0.Native/Missing").build();
                    var missing =
                            assertThrows(
                                    StatusRuntimeException.class,
                                    () ->
                                            ClientCalls.blockingUnaryCall(
                                                    channel,
                                                    unknown,
                                                    CallOptions.DEFAULT,
                                                    StringValue.of("unknown")));
                    assertEquals(Status.Code.UNIMPLEMENTED, missing.getStatus().getCode());
                    CountDownLatch collected = new CountDownLatch(1);
                    var result = new java.util.concurrent.atomic.AtomicReference<String>();
                    var failure = new java.util.concurrent.atomic.AtomicReference<Throwable>();
                    var requests =
                            ClientCalls.asyncClientStreamingCall(
                                    channel.newCall(COLLECT, CallOptions.DEFAULT),
                                    new io.grpc.stub.StreamObserver<>() {
                                        @Override
                                        public void onNext(StringValue value) {
                                            result.set(value.getValue());
                                        }

                                        @Override
                                        public void onError(Throwable error) {
                                            failure.set(error);
                                            collected.countDown();
                                        }

                                        @Override
                                        public void onCompleted() {
                                            collected.countDown();
                                        }
                                    });
                    requests.onNext(StringValue.of("first"));
                    requests.onNext(StringValue.of("second"));
                    requests.onCompleted();
                    assertTrue(collected.await(5, TimeUnit.SECONDS));
                    org.junit.jupiter.api.Assertions.assertNull(failure.get());
                    assertEquals("first,second", result.get());
                    assertEquals(3, handled.get());
                    assertEquals(3, intercepted.get());
                } finally {
                    channel.shutdownNow();
                    assertTrue(channel.awaitTermination(5, TimeUnit.SECONDS));
                }
            } finally {
                sdk.shutdownNow();
                assertTrue(sdk.awaitTermination(5, TimeUnit.SECONDS));
                assertTrue(http.isStarted(), "SDK shutdown must not stop the borrowed HTTP server");
                http.stop();
                host.timer.shutdownNow();
                assertTrue(host.timer.awaitTermination(5, TimeUnit.SECONDS));
                for (HostTransport transport : host.transports) {
                    assertTrue(transport.events.awaitTermination(5, TimeUnit.SECONDS));
                }
            }
        } finally {
            try {
                http.stop();
            } finally {
                host.timer.shutdownNow();
                host.transports.forEach(transport -> transport.events.shutdownNow());
                Engine.clearThreadLocalVariables();
            }
        }
    }

    private static final class SocketlessHost extends org.restlet.Restlet
            implements InternalServer {

        private final ScheduledExecutorService timer = Executors.newSingleThreadScheduledExecutor();

        private final List<HostTransport> transports =
                new java.util.concurrent.CopyOnWriteArrayList<>();

        private volatile ServerListener listener;

        private List<? extends ServerStreamTracer.Factory> tracers = List.of();

        @Override
        public void start(ServerListener listener) {
            this.listener = listener;
        }

        @Override
        public void shutdown() {
            listener.serverShutdown();
        }

        @Override
        public SocketAddress getListenSocketAddress() {
            return new InetSocketAddress("localhost", 0);
        }

        @Override
        public List<? extends SocketAddress> getListenSocketAddresses() {
            return List.of(getListenSocketAddress());
        }

        @Override
        public InternalInstrumented<InternalChannelz.SocketStats> getListenSocketStats() {
            return null;
        }

        @Override
        public List<InternalInstrumented<InternalChannelz.SocketStats>> getListenSocketStatsList() {
            return List.of();
        }

        @Override
        public void handle(Request request, Response response) {
            String method = request.getResourceRef().getPath().substring(1);
            Metadata headers = fromHttp(request);
            HostTransport transport = new HostTransport(timer);
            transports.add(transport);
            transport.listener = listener.transportCreated(transport);
            Attributes attributes = transport.listener.transportReady(Attributes.EMPTY);
            SdkStream stream =
                    new SdkStream(
                            transport,
                            attributes,
                            request.getHostRef().getAuthority(),
                            StatsTraceContext.newServerContext(tracers, method, headers));
            transport.stream = stream;
            response.setOnSent((sentRequest, sentResponse) -> transport.finish());
            try {
                transport.listener.streamCreated(stream, method, headers);
                byte[] bytes;
                try (var input = request.getEntity().getStream()) {
                    bytes = input.readNBytes(MAX_BYTES + 1);
                }
                if (bytes.length > MAX_BYTES) {
                    throw new IOException("Native SDK host request exceeds its R0 byte budget");
                }
                transport.events.execute(
                        () -> {
                            stream.state.onStreamAllocated();
                            stream.state.inboundDataReceived(ReadableBuffers.wrap(bytes), true);
                        });
                if (!stream.done.await(5, TimeUnit.SECONDS)) {
                    throw new IOException("Native SDK host did not complete");
                }
                if (stream.body.size() == 0 && !stream.headersSent) {
                    var empty = new org.restlet.representation.EmptyRepresentation();
                    empty.setMediaType(new MediaType("application/grpc"));
                    response.setEntity(empty);
                } else {
                    response.setEntity(
                            new ByteArrayRepresentation(
                                    stream.body.toByteArray(), new MediaType("application/grpc")));
                }
                toHttp(stream.headers, response.getHeaders());
                toHttp(
                        stream.trailers,
                        stream.headersSent ? response.getTrailers() : response.getHeaders());
            } catch (IOException e) {
                stream.cancel(Status.INTERNAL.withCause(e));
                throw new IllegalStateException("Native SDK host failed", e);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                stream.cancel(Status.CANCELLED.withCause(e));
                throw new IllegalStateException("Native SDK host interrupted", e);
            }
        }
    }

    private static final class HostTransport implements ServerTransport {

        private final ScheduledExecutorService timer;

        private final ExecutorService events =
                Executors.newSingleThreadExecutor(Thread.ofVirtual().factory());

        private final InternalLogId id =
                InternalLogId.allocate(HostTransport.class, "R0 logical call");

        private ServerTransportListener listener;

        private SdkStream stream;

        private boolean finishing;

        private HostTransport(ScheduledExecutorService timer) {
            this.timer = timer;
        }

        @Override
        public void shutdown() {}

        @Override
        public synchronized void shutdownNow(Status reason) {
            if (!finishing && stream != null) {
                stream.cancel(reason);
            }
        }

        private synchronized void finish() {
            if (finishing) {
                return;
            }
            finishing = true;
            events.execute(
                    () -> {
                        try {
                            stream.state.complete();
                            listener.transportTerminated();
                        } finally {
                            events.shutdown();
                        }
                    });
        }

        @Override
        public ScheduledExecutorService getScheduledExecutorService() {
            return timer;
        }

        @Override
        public ListenableFuture<InternalChannelz.SocketStats> getStats() {
            return Futures.immediateFuture(null);
        }

        @Override
        public InternalLogId getLogId() {
            return id;
        }
    }

    private static final class SdkStream extends AbstractServerStream {

        private final ByteArrayOutputStream body = new ByteArrayOutputStream();

        private final Metadata headers = new Metadata();

        private final CountDownLatch done = new CountDownLatch(1);

        private final HostTransport transport;

        private final Attributes attributes;

        private final String authority;

        private final State state;

        private volatile Metadata trailers = new Metadata();

        private boolean headersSent;

        private SdkStream(
                HostTransport transport,
                Attributes attributes,
                String authority,
                StatsTraceContext stats) {
            super(hint -> new Buffer(Math.max(1, Math.min(hint, 16 * 1024))), stats);
            this.transport = transport;
            this.attributes = attributes;
            this.authority = authority;
            state = new State(stats);
        }

        @Override
        protected TransportState transportState() {
            return state;
        }

        @Override
        public Attributes getAttributes() {
            return attributes;
        }

        @Override
        public String getAuthority() {
            return authority;
        }

        @Override
        public int streamId() {
            return -1;
        }

        @Override
        protected Sink abstractServerStreamSink() {
            return new Sink() {
                @Override
                public void writeHeaders(Metadata metadata, boolean flush) {
                    headers.merge(metadata);
                    headersSent = true;
                }

                @Override
                public void writeFrame(WritableBuffer frame, boolean flush, int messages) {
                    Buffer buffer = (Buffer) frame;
                    if (body.size() + buffer.readableBytes() > MAX_BYTES) {
                        throw new IllegalStateException(
                                "Native SDK host response exceeds R0 budget");
                    }
                    body.write(buffer.bytes, 0, buffer.used);
                    buffer.release();
                }

                @Override
                public void writeTrailers(Metadata metadata, boolean sent, Status status) {
                    trailers = metadata;
                    done.countDown();
                }

                @Override
                public void cancel(Status status) {
                    state.runOnTransportThread(() -> state.transportReportStatus(status));
                    Metadata metadata = new Metadata();
                    metadata.put(io.grpc.InternalStatus.CODE_KEY, status);
                    trailers = metadata;
                    done.countDown();
                }
            };
        }

        private final class State extends AbstractServerStream.TransportState {
            private State(StatsTraceContext stats) {
                super(MAX_BYTES, stats, new TransportTracer());
            }

            @Override
            public void runOnTransportThread(Runnable task) {
                transport.events.execute(task);
            }

            @Override
            public void bytesRead(int bytes) {}

            @Override
            public void deframeFailed(Throwable cause) {
                SdkStream.this.cancel(Status.fromThrowable(cause));
            }
        }
    }

    private static final class Buffer implements WritableBuffer {

        private final byte[] bytes;

        private int used;

        private Buffer(int capacity) {
            bytes = new byte[capacity];
        }

        @Override
        public void write(byte value) {
            bytes[used++] = value;
        }

        @Override
        public void write(byte[] source, int offset, int length) {
            System.arraycopy(source, offset, bytes, used, length);
            used += length;
        }

        @Override
        public int writableBytes() {
            return bytes.length - used;
        }

        @Override
        public int readableBytes() {
            return used;
        }

        @Override
        public void release() {}
    }

    private static Metadata fromHttp(Request request) {
        List<byte[]> serialized = new ArrayList<>();
        for (var header : request.getHeaders()) {
            String name = header.getName().toLowerCase(java.util.Locale.ROOT);
            serialized.add(name.getBytes(StandardCharsets.US_ASCII));
            serialized.add(
                    name.endsWith("-bin")
                            ? Base64.getDecoder().decode(header.getValue())
                            : header.getValue().getBytes(StandardCharsets.US_ASCII));
        }
        return InternalMetadata.newMetadata(serialized.toArray(byte[][]::new));
    }

    private static void toHttp(
            Metadata metadata, org.restlet.util.Series<org.restlet.data.Header> headers) {
        byte[][] serialized = InternalMetadata.serialize(metadata);
        if (serialized == null) {
            return;
        }
        for (int i = 0; i < serialized.length; i += 2) {
            String name = new String(serialized[i], StandardCharsets.US_ASCII);
            headers.add(
                    name,
                    name.endsWith("-bin")
                            ? Base64.getEncoder().withoutPadding().encodeToString(serialized[i + 1])
                            : new String(serialized[i + 1], StandardCharsets.US_ASCII));
        }
    }
}
