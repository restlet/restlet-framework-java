/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.grpc;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.StringValue;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.ServerCalls;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;

class GrpcClientAdapterTestCase {
    private static final MethodDescriptor<StringValue, StringValue> ECHO =
            MethodDescriptor.<StringValue, StringValue>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName("review.Unary/Echo")
                    .setRequestMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .build();
    private static final Metadata.Key<String> TEXT =
            Metadata.Key.of("x-review", Metadata.ASCII_STRING_MARSHALLER);
    private static final Metadata.Key<byte[]> BINARY =
            Metadata.Key.of("grpc-status-details-bin", Metadata.BINARY_BYTE_MARSHALLER);
    private final GrpcClientAdapter adapter = new GrpcClientAdapter();
    private Client client;
    private io.grpc.Server peer;

    private void start(ServerServiceDefinition service, ServerInterceptor... interceptors)
            throws Exception {
        start(service, "dns:///localhost", interceptors);
    }

    private void start(
            ServerServiceDefinition service, String targetHost, ServerInterceptor... interceptors)
            throws Exception {
        var builder =
                NettyServerBuilder.forPort(0)
                        .addService(service)
                        .addService(new HealthStatusManager().getHealthService());
        for (var interceptor : interceptors) {
            builder.intercept(interceptor);
        }
        peer = builder.build().start();
        Context context = new Context();
        context.getParameters().set("httpClientTransportMode", "HTTP2");
        context.getParameters().set("followRedirects", "false");
        client = new Client(context, Protocol.HTTP);
        client.start();
        adapter.setClient(client);
        adapter.setTarget(targetHost + ":" + peer.getPort());
        adapter.setPlaintext(true);
        adapter.start();
    }

    private static ServerServiceDefinition echo() {
        return ServerServiceDefinition.builder("review.Unary")
                .addMethod(
                        ECHO,
                        ServerCalls.asyncUnaryCall(
                                (StringValue request,
                                        io.grpc.stub.StreamObserver<StringValue> out) -> {
                                    if ("bad".equals(request.getValue())) {
                                        Metadata trailers = new Metadata();
                                        trailers.put(TEXT, "one");
                                        trailers.put(TEXT, "two");
                                        trailers.put(BINARY, new byte[] {0, (byte) 255, 7});
                                        out.onError(
                                                Status.INVALID_ARGUMENT
                                                        .withDescription(
                                                                "native % error: caf\u00e9")
                                                        .asRuntimeException(trailers));
                                    } else {
                                        out.onNext(request);
                                        out.onCompleted();
                                    }
                                }))
                .build();
    }

    @AfterEach
    void cleanup() throws Exception {
        try {
            adapter.stop();
        } finally {
            try {
                if (client != null) {
                    client.stop();
                }
            } finally {
                if (peer != null) {
                    peer.shutdownNow();
                    assertTrue(peer.awaitTermination(30, TimeUnit.SECONDS));
                }
            }
        }
    }

    private StringValue call(String value) {
        return ClientCalls.blockingUnaryCall(
                adapter.getChannel(), ECHO, CallOptions.DEFAULT, StringValue.of(value));
    }

    @Test
    void generatedAndDynamicUnaryCallsUseOfficialSdk() throws Exception {
        start(echo());
        assertEquals("hello", call("hello").getValue());
        var health =
                HealthGrpc.newBlockingStub(adapter.getChannel())
                        .check(HealthCheckRequest.getDefaultInstance());
        assertEquals(HealthCheckResponse.ServingStatus.SERVING, health.getStatus());
        var dynamic =
                MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                        .setType(MethodDescriptor.MethodType.UNARY)
                        .setFullMethodName(HealthGrpc.getCheckMethod().getFullMethodName())
                        .setRequestMarshaller(
                                ProtoUtils.marshaller(
                                        DynamicMessage.getDefaultInstance(
                                                HealthCheckRequest.getDescriptor())))
                        .setResponseMarshaller(
                                ProtoUtils.marshaller(
                                        DynamicMessage.getDefaultInstance(
                                                HealthCheckResponse.getDescriptor())))
                        .build();
        DynamicMessage result =
                ClientCalls.blockingUnaryCall(
                        adapter.getChannel(),
                        dynamic,
                        CallOptions.DEFAULT,
                        DynamicMessage.getDefaultInstance(HealthCheckRequest.getDescriptor()));
        assertEquals(
                health.getStatus().getNumber(),
                ((com.google.protobuf.Descriptors.EnumValueDescriptor)
                                result.getField(
                                        HealthCheckResponse.getDescriptor()
                                                .findFieldByName("status")))
                        .getNumber());
        assertTrue(adapter.getChannel().authority().startsWith("localhost:"));
        adapter.stop();
        assertTrue(client.isStarted());
        assertFalse(adapter.isStarted());
        assertThrows(IllegalStateException.class, adapter::getChannel);
        assertThrows(IllegalStateException.class, adapter::start);
    }

    @Test
    void preservesTrailersOnlyErrorAndRecovers() throws Exception {
        start(echo());
        StatusRuntimeException error =
                assertThrows(StatusRuntimeException.class, () -> call("bad"));
        assertEquals(Status.Code.INVALID_ARGUMENT, error.getStatus().getCode());
        assertEquals("native % error: caf\u00e9", error.getStatus().getDescription());
        assertEquals(
                List.of("one", "two"),
                com.google.common.collect.ImmutableList.copyOf(error.getTrailers().getAll(TEXT)));
        assertArrayEquals(new byte[] {0, (byte) 255, 7}, error.getTrailers().get(BINARY));
        assertEquals("recovered", call("recovered").getValue());
    }

    @Test
    void passesMetadataAndAuthorityWithoutLosingResolvedPeer() throws Exception {
        AtomicReference<String> authority = new AtomicReference<>();
        AtomicReference<Metadata> observed = new AtomicReference<>();
        start(
                echo(),
                new ServerInterceptor() {
                    @Override
                    public <Q, S> ServerCall.Listener<Q> interceptCall(
                            ServerCall<Q, S> call, Metadata headers, ServerCallHandler<Q, S> next) {
                        authority.set(call.getAuthority());
                        observed.set(headers);
                        return next.startCall(call, headers);
                    }
                });
        Metadata metadata = new Metadata();
        metadata.put(TEXT, "first");
        metadata.put(TEXT, "second");
        metadata.put(BINARY, new byte[] {1, 2, (byte) 255});
        metadata.put(BINARY, new byte[] {(byte) 254, 0});
        var channel =
                io.grpc.ClientInterceptors.intercept(
                        adapter.getChannel(),
                        io.grpc.stub.MetadataUtils.newAttachHeadersInterceptor(metadata));
        assertEquals(
                "meta",
                ClientCalls.blockingUnaryCall(
                                channel, ECHO, CallOptions.DEFAULT, StringValue.of("meta"))
                        .getValue());
        assertEquals("localhost:" + peer.getPort(), authority.get());
        assertEquals(
                List.of("first", "second"),
                com.google.common.collect.ImmutableList.copyOf(observed.get().getAll(TEXT)));
        var binary = com.google.common.collect.ImmutableList.copyOf(observed.get().getAll(BINARY));
        assertEquals(2, binary.size());
        assertArrayEquals(new byte[] {1, 2, (byte) 255}, binary.get(0));
        assertArrayEquals(new byte[] {(byte) 254, 0}, binary.get(1));
    }

    @Test
    void bareIpTargetPreservesAuthorityAndAcceptsMatchingCallAuthority() throws Exception {
        AtomicReference<String> authority = new AtomicReference<>();
        start(
                echo(),
                "127.0.0.1",
                new ServerInterceptor() {
                    @Override
                    public <Q, S> ServerCall.Listener<Q> interceptCall(
                            ServerCall<Q, S> call, Metadata headers, ServerCallHandler<Q, S> next) {
                        authority.set(call.getAuthority());
                        return next.startCall(call, headers);
                    }
                });
        String expected = "127.0.0.1:" + peer.getPort();
        assertEquals(expected, adapter.getChannel().authority());
        assertEquals(
                "ip",
                ClientCalls.blockingUnaryCall(
                                adapter.getChannel(),
                                ECHO,
                                CallOptions.DEFAULT.withAuthority(expected),
                                StringValue.of("ip"))
                        .getValue());
        assertEquals(expected, authority.get());
    }

    @Test
    void cancelledBeforeStartNeverReachesPeerAndHasOneTerminalCallback() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        start(
                echo(),
                new ServerInterceptor() {
                    @Override
                    public <Q, S> ServerCall.Listener<Q> interceptCall(
                            ServerCall<Q, S> call, Metadata headers, ServerCallHandler<Q, S> next) {
                        requests.incrementAndGet();
                        return next.startCall(call, headers);
                    }
                });
        ClientCall<StringValue, StringValue> call =
                adapter.getChannel().newCall(ECHO, CallOptions.DEFAULT);
        AtomicInteger terminals = new AtomicInteger();
        AtomicReference<Status> status = new AtomicReference<>();
        CountDownLatch closed = new CountDownLatch(1);
        call.cancel("cancelled before start", null);
        call.start(
                new ClientCall.Listener<>() {
                    @Override
                    public void onClose(Status value, Metadata trailers) {
                        status.set(value);
                        terminals.incrementAndGet();
                        closed.countDown();
                    }
                },
                new Metadata());
        assertTrue(closed.await(30, TimeUnit.SECONDS));
        assertEquals(Status.Code.CANCELLED, status.get().getCode());
        assertEquals(0, requests.get());
        assertEquals("next", call("next").getValue());
        assertEquals(1, requests.get());
        assertEquals(1, terminals.get());
    }

    @Test
    void cancellationAfterHeadersReleasesAdmissionAndClosesExactlyOnce() throws Exception {
        adapter.setMaxConcurrentCalls(1);
        CountDownLatch cancelled = new CountDownLatch(1);
        start(
                ServerServiceDefinition.builder("review.Unary")
                        .addMethod(
                                // Flush one response without completing the client's unary call.
                                ECHO.toBuilder()
                                        .setType(MethodDescriptor.MethodType.SERVER_STREAMING)
                                        .build(),
                                ServerCalls.asyncServerStreamingCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            if ("block".equals(request.getValue())) {
                                                ((io.grpc.stub.ServerCallStreamObserver<
                                                                        StringValue>)
                                                                out)
                                                        .setOnCancelHandler(cancelled::countDown);
                                                out.onNext(StringValue.of("pending"));
                                            } else {
                                                out.onNext(request);
                                                out.onCompleted();
                                            }
                                        }))
                        .build());
        CountDownLatch headers = new CountDownLatch(1);
        CountDownLatch closed = new CountDownLatch(1);
        AtomicInteger terminals = new AtomicInteger();
        AtomicInteger messages = new AtomicInteger();
        AtomicReference<Status> status = new AtomicReference<>();
        ClientCall<StringValue, StringValue> call =
                adapter.getChannel().newCall(ECHO, CallOptions.DEFAULT);
        call.start(
                new ClientCall.Listener<>() {
                    @Override
                    public void onHeaders(Metadata value) {
                        headers.countDown();
                    }

                    @Override
                    public void onMessage(StringValue value) {
                        messages.incrementAndGet();
                    }

                    @Override
                    public void onClose(Status value, Metadata trailers) {
                        status.set(value);
                        terminals.incrementAndGet();
                        closed.countDown();
                    }
                },
                new Metadata());
        call.request(1);
        call.sendMessage(StringValue.of("block"));
        call.halfClose();
        assertTrue(headers.await(30, TimeUnit.SECONDS));
        call.cancel("cancel after headers", null);
        call.cancel("duplicate", null);
        assertTrue(closed.await(30, TimeUnit.SECONDS));
        assertTrue(cancelled.await(30, TimeUnit.SECONDS));
        assertEquals(Status.Code.CANCELLED, status.get().getCode());
        assertEquals("recovered", call("recovered").getValue());
        assertEquals(1, terminals.get());
        assertEquals(0, messages.get());
    }

    private record Pending(CountDownLatch entered, CountDownLatch cancelled) {}

    private Pending startBlocked() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch cancelled = new CountDownLatch(1);
        var service =
                ServerServiceDefinition.builder("review.Unary")
                        .addMethod(
                                ECHO,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            if ("block".equals(request.getValue())) {
                                                ((io.grpc.stub.ServerCallStreamObserver<
                                                                        StringValue>)
                                                                out)
                                                        .setOnCancelHandler(cancelled::countDown);
                                                entered.countDown();
                                            } else {
                                                out.onNext(request);
                                                out.onCompleted();
                                            }
                                        }))
                        .build();
        start(service);
        return new Pending(entered, cancelled);
    }

    @Test
    void cancellationBeforeHeadersHasOneTerminalCallbackAndRecovery() throws Exception {
        Pending pending = startBlocked();
        AtomicInteger terminals = new AtomicInteger();
        CountDownLatch closed = new CountDownLatch(1);
        AtomicReference<Status> status = new AtomicReference<>();
        ClientCall<StringValue, StringValue> call =
                adapter.getChannel().newCall(ECHO, CallOptions.DEFAULT);
        call.start(
                new ClientCall.Listener<>() {
                    @Override
                    public void onClose(Status value, Metadata trailers) {
                        status.set(value);
                        terminals.incrementAndGet();
                        closed.countDown();
                    }
                },
                new Metadata());
        call.request(1);
        call.sendMessage(StringValue.of("block"));
        call.halfClose();
        assertTrue(pending.entered().await(30, TimeUnit.SECONDS));
        call.cancel("review cancellation", null);
        call.cancel("duplicate cancellation", null);
        assertTrue(closed.await(30, TimeUnit.SECONDS));
        assertTrue(pending.cancelled().await(30, TimeUnit.SECONDS));
        assertEquals(Status.Code.CANCELLED, status.get().getCode());
        assertEquals("next", call("next").getValue());
        assertEquals(1, terminals.get());
    }

    @Test
    void deadlineAndConcurrentAdmissionAreNativeFailures() throws Exception {
        adapter.setMaxConcurrentCalls(1);
        Pending pending = startBlocked();
        var first =
                ClientCalls.futureUnaryCall(
                        adapter.getChannel()
                                .newCall(
                                        ECHO,
                                        CallOptions.DEFAULT.withDeadlineAfter(3, TimeUnit.SECONDS)),
                        StringValue.of("block"));
        assertTrue(pending.entered().await(30, TimeUnit.SECONDS));
        assertEquals(
                Status.Code.RESOURCE_EXHAUSTED,
                assertThrows(StatusRuntimeException.class, () -> call("second"))
                        .getStatus()
                        .getCode());
        var error =
                assertThrows(
                        java.util.concurrent.ExecutionException.class,
                        () -> first.get(30, TimeUnit.SECONDS));
        assertEquals(Status.Code.DEADLINE_EXCEEDED, Status.fromThrowable(error).getCode());
        assertTrue(pending.cancelled().await(30, TimeUnit.SECONDS));
        assertEquals("recovered", call("recovered").getValue());
    }

    @Test
    void stopCancelsActiveCallsAndLeavesBorrowedConnectorRunning() throws Exception {
        adapter.setShutdownTimeout(Duration.ofMillis(100));
        Pending pending = startBlocked();
        var first =
                ClientCalls.futureUnaryCall(
                        adapter.getChannel().newCall(ECHO, CallOptions.DEFAULT),
                        StringValue.of("block"));
        assertTrue(pending.entered().await(30, TimeUnit.SECONDS));
        adapter.stop();
        assertThrows(
                java.util.concurrent.ExecutionException.class,
                () -> first.get(30, TimeUnit.SECONDS));
        assertTrue(pending.cancelled().await(30, TimeUnit.SECONDS));
        assertTrue(client.isStarted());
    }

    @Test
    void interruptedStopStillClosesOwnedChannelAndPreservesBorrowedResources() throws Exception {
        Pending pending = startBlocked();
        var channel = adapter.getChannel();
        var first =
                ClientCalls.futureUnaryCall(
                        channel.newCall(ECHO, CallOptions.DEFAULT), StringValue.of("block"));
        assertTrue(pending.entered().await(30, TimeUnit.SECONDS));
        try {
            Thread.currentThread().interrupt();
            assertThrows(IllegalStateException.class, adapter::stop);
            assertTrue(Thread.currentThread().isInterrupted());
        } finally {
            Thread.interrupted();
        }
        assertTrue(channel.awaitTermination(30, TimeUnit.SECONDS));
        assertThrows(
                java.util.concurrent.ExecutionException.class,
                () -> first.get(30, TimeUnit.SECONDS));
        assertTrue(pending.cancelled().await(30, TimeUnit.SECONDS));
        assertTrue(client.isStarted());
        adapter.stop();
    }

    @Test
    void rejectsStreamingAndOutboundLimitBeforeSending() throws Exception {
        adapter.setMaxMessageBytes(128);
        start(echo());
        assertEquals(
                Status.Code.RESOURCE_EXHAUSTED,
                assertThrows(StatusRuntimeException.class, () -> call("x".repeat(129)))
                        .getStatus()
                        .getCode());
        assertEquals("small", call("small").getValue());
        var streamMethod =
                ECHO.toBuilder().setType(MethodDescriptor.MethodType.SERVER_STREAMING).build();
        assertEquals(
                Status.Code.UNIMPLEMENTED,
                assertThrows(
                                StatusRuntimeException.class,
                                () ->
                                        ClientCalls.blockingServerStreamingCall(
                                                        adapter.getChannel(),
                                                        streamMethod,
                                                        CallOptions.DEFAULT,
                                                        StringValue.of("x"))
                                                .next())
                        .getStatus()
                        .getCode());
    }

    @Test
    void honorsExactMessageLimitAndStricterCallOptions() throws Exception {
        adapter.setMaxMessageBytes(128);
        start(echo());
        StringValue boundary = StringValue.of("x".repeat(126));
        StringValue oversized = StringValue.of("x".repeat(127));
        assertEquals(128, boundary.getSerializedSize());
        assertEquals(129, oversized.getSerializedSize());
        for (int limit : List.of(128, 4096)) {
            CallOptions options =
                    CallOptions.DEFAULT
                            .withMaxInboundMessageSize(limit)
                            .withMaxOutboundMessageSize(limit);
            assertEquals(
                    boundary,
                    ClientCalls.blockingUnaryCall(adapter.getChannel(), ECHO, options, boundary));
            assertEquals(
                    Status.Code.RESOURCE_EXHAUSTED,
                    assertThrows(
                                    StatusRuntimeException.class,
                                    () ->
                                            ClientCalls.blockingUnaryCall(
                                                    adapter.getChannel(), ECHO, options, oversized))
                            .getStatus()
                            .getCode());
        }
        for (CallOptions options :
                List.of(
                        CallOptions.DEFAULT.withMaxInboundMessageSize(64),
                        CallOptions.DEFAULT.withMaxOutboundMessageSize(64))) {
            assertEquals(
                    Status.Code.RESOURCE_EXHAUSTED,
                    assertThrows(
                                    StatusRuntimeException.class,
                                    () ->
                                            ClientCalls.blockingUnaryCall(
                                                    adapter.getChannel(), ECHO, options, boundary))
                            .getStatus()
                            .getCode());
            assertEquals("recovered", call("recovered").getValue());
        }
    }

    @Test
    void boundsInboundResponseAndDoesNotAllowCallOptionsToRaiseLimits() throws Exception {
        adapter.setMaxMessageBytes(128);
        var service =
                ServerServiceDefinition.builder("review.Unary")
                        .addMethod(
                                ECHO,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            out.onNext(StringValue.of("x".repeat(256)));
                                            out.onCompleted();
                                        }))
                        .build();
        start(service);
        assertEquals(
                Status.Code.RESOURCE_EXHAUSTED,
                assertThrows(
                                StatusRuntimeException.class,
                                () ->
                                        ClientCalls.blockingUnaryCall(
                                                adapter.getChannel(),
                                                ECHO,
                                                CallOptions.DEFAULT.withMaxInboundMessageSize(4096),
                                                StringValue.of("large")))
                        .getStatus()
                        .getCode());
        assertEquals(
                Status.Code.RESOURCE_EXHAUSTED,
                assertThrows(
                                StatusRuntimeException.class,
                                () ->
                                        ClientCalls.blockingUnaryCall(
                                                adapter.getChannel(),
                                                ECHO,
                                                CallOptions.DEFAULT.withMaxOutboundMessageSize(
                                                        4096),
                                                StringValue.of("x".repeat(256))))
                        .getStatus()
                        .getCode());
    }

    @Test
    void enforcesDefaultCallDurationAndCallerMayShortenIt() throws Exception {
        adapter.setMaxCallDuration(Duration.ofSeconds(3));
        Pending pending = startBlocked();
        var failure = assertThrows(StatusRuntimeException.class, () -> call("block"));
        assertEquals(Status.Code.DEADLINE_EXCEEDED, failure.getStatus().getCode());
        assertTrue(pending.cancelled().await(30, TimeUnit.SECONDS));
        assertEquals("next", call("next").getValue());
    }

    @Test
    void interceptorCannotExtendAdapterDuration() throws Exception {
        adapter.setMaxCallDuration(Duration.ofSeconds(3));
        adapter.getInterceptors()
                .add(
                        new io.grpc.ClientInterceptor() {
                            @Override
                            public <Q, S> ClientCall<Q, S> interceptCall(
                                    MethodDescriptor<Q, S> method,
                                    CallOptions options,
                                    io.grpc.Channel next) {
                                return next.newCall(
                                        method, options.withDeadlineAfter(1, TimeUnit.HOURS));
                            }
                        });
        Pending pending = startBlocked();
        var failure = assertThrows(StatusRuntimeException.class, () -> call("block"));
        assertEquals(Status.Code.DEADLINE_EXCEEDED, failure.getStatus().getCode());
        assertTrue(pending.cancelled().await(30, TimeUnit.SECONDS));
    }

    @Test
    void callerDeadlineCanShortenAdapterDuration() throws Exception {
        adapter.setMaxCallDuration(Duration.ofSeconds(20));
        Pending pending = startBlocked();
        var first =
                ClientCalls.futureUnaryCall(
                        adapter.getChannel()
                                .newCall(
                                        ECHO,
                                        CallOptions.DEFAULT.withDeadlineAfter(3, TimeUnit.SECONDS)),
                        StringValue.of("block"));
        assertTrue(pending.entered().await(30, TimeUnit.SECONDS));
        var error =
                assertThrows(
                        java.util.concurrent.ExecutionException.class,
                        () -> first.get(10, TimeUnit.SECONDS));
        assertEquals(Status.Code.DEADLINE_EXCEEDED, Status.fromThrowable(error).getCode());
        assertTrue(pending.cancelled().await(30, TimeUnit.SECONDS));
        assertEquals("next", call("next").getValue());
    }

    @Test
    void rejectsCompressionAndAuthorityOverridesExplicitly() throws Exception {
        start(echo());
        for (CallOptions options :
                List.of(
                        CallOptions.DEFAULT.withCompression("gzip"),
                        CallOptions.DEFAULT.withAuthority("different.example"))) {
            assertEquals(
                    Status.Code.UNIMPLEMENTED,
                    assertThrows(
                                    StatusRuntimeException.class,
                                    () ->
                                            ClientCalls.blockingUnaryCall(
                                                    adapter.getChannel(),
                                                    ECHO,
                                                    options,
                                                    StringValue.of("x")))
                            .getStatus()
                            .getCode());
        }
        assertEquals("next", call("next").getValue());
    }

    @Test
    void remoteShutdownTerminatesCallWithoutStoppingBorrowedConnector() throws Exception {
        adapter.setMaxConcurrentCalls(1);
        Pending pending = startBlocked();
        int port = peer.getPort();
        var call =
                ClientCalls.futureUnaryCall(
                        adapter.getChannel().newCall(ECHO, CallOptions.DEFAULT),
                        StringValue.of("block"));
        assertTrue(pending.entered().await(30, TimeUnit.SECONDS));
        peer.shutdownNow();
        var error =
                assertThrows(
                        java.util.concurrent.ExecutionException.class,
                        () -> call.get(30, TimeUnit.SECONDS));
        assertEquals(Status.Code.UNAVAILABLE, Status.fromThrowable(error).getCode());
        assertTrue(client.isStarted());
        assertTrue(peer.awaitTermination(30, TimeUnit.SECONDS));
        peer = NettyServerBuilder.forPort(port).addService(echo()).build().start();
        assertEquals("reconnected", call("reconnected").getValue());
    }

    @Test
    void stoppingOneAdapterLeavesSharedConnectorAndOtherChannelUsable() throws Exception {
        start(echo());
        try (var other = new GrpcClientAdapter()) {
            other.setClient(client);
            other.setTarget(adapter.getTarget());
            other.setPlaintext(true);
            other.start();
            assertEquals("first", call("first").getValue());
            adapter.stop();
            adapter.stop();
            assertTrue(client.isStarted());
            assertEquals(
                    "second",
                    ClientCalls.blockingUnaryCall(
                                    other.getChannel(),
                                    ECHO,
                                    CallOptions.DEFAULT,
                                    StringValue.of("second"))
                            .getValue());
        }
        assertTrue(client.isStarted());
    }

    @Test
    void nativeInterceptorAndBorrowedExecutorArePreserved() throws Exception {
        try (var callbacks = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            adapter.setExecutor(callbacks);
            AtomicInteger intercepted = new AtomicInteger();
            adapter.getInterceptors()
                    .add(
                            new io.grpc.ClientInterceptor() {
                                @Override
                                public <Q, S> ClientCall<Q, S> interceptCall(
                                        MethodDescriptor<Q, S> method,
                                        CallOptions options,
                                        io.grpc.Channel next) {
                                    intercepted.incrementAndGet();
                                    return next.newCall(method, options);
                                }
                            });
            start(echo());
            adapter.start();
            assertEquals("one", call("one").getValue());
            adapter.getInterceptors().clear();
            assertEquals("two", call("two").getValue());
            assertEquals(2, intercepted.get());
            assertThrows(IllegalStateException.class, () -> adapter.setMaxMessageBytes(256));
            adapter.stop();
            assertFalse(callbacks.isShutdown());
            assertSame(client, adapter.getClient());
        }
    }

    @Test
    void validatesConfigurationAndRequiresExplicitPlaintext() {
        assertFalse(adapter.isStarted());
        assertThrows(IllegalStateException.class, adapter::getChannel);
        assertThrows(UnsupportedOperationException.class, adapter::start);
        assertThrows(IllegalArgumentException.class, () -> adapter.setMaxMessageBytes(0));
        assertThrows(IllegalArgumentException.class, () -> adapter.setMaxConcurrentCalls(0));
        assertThrows(
                IllegalArgumentException.class, () -> adapter.setMaxCallDuration(Duration.ZERO));
        assertThrows(
                IllegalArgumentException.class, () -> adapter.setShutdownTimeout(Duration.ZERO));
        adapter.setPlaintext(true);
        assertThrows(NullPointerException.class, adapter::start);
        adapter.setTarget("dns:///localhost:8080");
        assertThrows(IllegalStateException.class, adapter::start);
        assertNotNull(adapter.getContext());
        assertEquals(128, adapter.getMaxConcurrentCalls());
        assertEquals(1024 * 1024, adapter.getMaxMessageBytes());
        assertEquals(Duration.ofSeconds(30), adapter.getMaxCallDuration());
        assertEquals(Duration.ofSeconds(5), adapter.getShutdownTimeout());
        assertEquals("dns:///localhost:8080", adapter.getTarget());
        assertTrue(adapter.isPlaintext());
    }
}
