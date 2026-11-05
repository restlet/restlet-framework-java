/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.grpc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.DynamicMessage;
import com.google.protobuf.StringValue;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.health.v1.HealthCheckRequest;
import io.grpc.health.v1.HealthCheckResponse;
import io.grpc.health.v1.HealthGrpc;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.protobuf.services.HealthStatusManager;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.ServerCalls;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.InputRepresentation;
import org.restlet.routing.Router;

class GrpcServerAdapterTestCase {
    private static final MethodDescriptor<StringValue, StringValue> ECHO =
            MethodDescriptor.<StringValue, StringValue>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName("review.Server/Echo")
                    .setRequestMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .build();
    private static final MethodDescriptor<StringValue, StringValue> WATCH =
            ECHO.toBuilder()
                    .setType(MethodDescriptor.MethodType.SERVER_STREAMING)
                    .setFullMethodName("review.Server/Watch")
                    .build();
    private static final MethodDescriptor<StringValue, StringValue> UPLOAD =
            ECHO.toBuilder()
                    .setType(MethodDescriptor.MethodType.CLIENT_STREAMING)
                    .setFullMethodName("review.Server/Upload")
                    .build();
    private static final MethodDescriptor<StringValue, StringValue> CHAT =
            ECHO.toBuilder()
                    .setType(MethodDescriptor.MethodType.BIDI_STREAMING)
                    .setFullMethodName("review.Server/Chat")
                    .build();
    private final GrpcServerAdapter adapter = new GrpcServerAdapter();
    private Server http;
    private ManagedChannel channel;
    private final java.util.List<LogRecord> lifecycleErrors = new CopyOnWriteArrayList<>();
    private final Handler errorHandler =
            new Handler() {
                @Override
                public void publish(LogRecord record) {
                    if (record.getLevel().intValue() >= Level.SEVERE.intValue()) {
                        lifecycleErrors.add(record);
                    }
                }

                @Override
                public void flush() {}

                @Override
                public void close() {}
            };

    @BeforeEach
    void captureLifecycleErrors() {
        Logger.getLogger("io.grpc.internal").addHandler(errorHandler);
    }

    private void start(ServerServiceDefinition service) throws Exception {
        adapter.getServices().add(service);
        adapter.start();
        Router router = new Router();
        adapter.attachTo(router);
        router.attach(
                "/rest",
                new Restlet() {
                    @Override
                    public void handle(Request request, Response response) {
                        response.setEntity("rest", MediaType.TEXT_PLAIN);
                    }
                });
        Context context = new Context();
        context.getParameters().set("http.transport.protocol", "HTTP2");
        http = new Server(context, Protocol.HTTP, 0, router);
        http.start();
        channel =
                NettyChannelBuilder.forAddress("localhost", http.getActualPort())
                        .usePlaintext()
                        .build();
    }

    private String call(String value) {
        return ClientCalls.blockingUnaryCall(
                        channel,
                        ECHO,
                        CallOptions.DEFAULT.withDeadlineAfter(10, TimeUnit.SECONDS),
                        StringValue.of(value))
                .getValue();
    }

    private static ServerServiceDefinition echo() {
        return ServerServiceDefinition.builder("review.Server")
                .addMethod(
                        ECHO,
                        ServerCalls.asyncUnaryCall(
                                (StringValue request,
                                        io.grpc.stub.StreamObserver<StringValue> out) -> {
                                    out.onNext(request);
                                    out.onCompleted();
                                }))
                .build();
    }

    @AfterEach
    void cleanup() throws Exception {
        try {
            if (channel != null) {
                channel.shutdownNow();
                assertTrue(channel.awaitTermination(15, TimeUnit.SECONDS));
            }
        } finally {
            try {
                adapter.stop();
            } finally {
                if (http != null) {
                    http.stop();
                }
                Logger.getLogger("io.grpc.internal").removeHandler(errorHandler);
                assertTrue(
                        lifecycleErrors.isEmpty(),
                        () ->
                                lifecycleErrors.stream()
                                        .map(LogRecord::getMessage)
                                        .toList()
                                        .toString());
            }
        }
    }

    @Test
    void generatedAndDynamicUnaryShareListenerWithRest() throws Exception {
        adapter.getServices().add(new HealthStatusManager().getHealthService().bindService());
        start(echo());
        assertEquals("hello", call("hello"));
        assertEquals(
                HealthCheckResponse.ServingStatus.SERVING,
                HealthGrpc.newBlockingStub(channel)
                        .withDeadlineAfter(10, TimeUnit.SECONDS)
                        .check(HealthCheckRequest.getDefaultInstance())
                        .getStatus());
        var method =
                MethodDescriptor.<DynamicMessage, DynamicMessage>newBuilder()
                        .setType(MethodDescriptor.MethodType.UNARY)
                        .setFullMethodName(ECHO.getFullMethodName())
                        .setRequestMarshaller(
                                ProtoUtils.marshaller(
                                        DynamicMessage.getDefaultInstance(
                                                StringValue.getDescriptor())))
                        .setResponseMarshaller(
                                ProtoUtils.marshaller(
                                        DynamicMessage.getDefaultInstance(
                                                StringValue.getDescriptor())))
                        .build();
        var request =
                DynamicMessage.newBuilder(StringValue.getDescriptor())
                        .setField(StringValue.getDescriptor().findFieldByName("value"), "dynamic")
                        .build();
        assertEquals(
                request,
                ClientCalls.blockingUnaryCall(
                        channel,
                        method,
                        CallOptions.DEFAULT.withDeadlineAfter(10, TimeUnit.SECONDS),
                        request));
        Context context = new Context();
        context.getParameters().set("httpClientTransportMode", "HTTP2");
        Client rest = new Client(context, Protocol.HTTP);
        rest.start();
        try {
            assertEquals(
                    "rest",
                    rest.handle(
                                    new Request(
                                            org.restlet.data.Method.GET,
                                            "http://localhost:" + http.getActualPort() + "/rest"))
                            .getEntity()
                            .getText());
            adapter.stop();
            assertTrue(http.isStarted());
            assertEquals(
                    "rest",
                    rest.handle(
                                    new Request(
                                            org.restlet.data.Method.GET,
                                            "http://localhost:" + http.getActualPort() + "/rest"))
                            .getEntity()
                            .getText());
        } finally {
            rest.stop();
        }
    }

    @Test
    void preservesTrailersOnlyErrorsAndRecovers() throws Exception {
        Metadata.Key<String> key = Metadata.Key.of("x-detail", Metadata.ASCII_STRING_MARSHALLER);
        start(
                ServerServiceDefinition.builder("review.Server")
                        .addMethod(
                                ECHO,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            Metadata trailers = new Metadata();
                                            trailers.put(key, "one");
                                            trailers.put(key, "two");
                                            out.onError(
                                                    Status.INVALID_ARGUMENT
                                                            .withDescription("native % error")
                                                            .asRuntimeException(trailers));
                                        }))
                        .build());
        var error = assertThrows(StatusRuntimeException.class, () -> call("bad"));
        assertEquals(Status.Code.INVALID_ARGUMENT, error.getStatus().getCode());
        assertEquals("native % error", error.getStatus().getDescription());
        assertEquals(
                java.util.List.of("one", "two"),
                com.google.common.collect.ImmutableList.copyOf(
                        java.util.Objects.requireNonNull(error.getTrailers()).getAll(key)));
    }

    @Test
    void serverStreamingUsesReadinessAndPreservesOrder() throws Exception {
        adapter.setMaxQueuedBytes(32768);
        AtomicInteger produced = new AtomicInteger();
        start(
                ServerServiceDefinition.builder("review.Server")
                        .addMethod(
                                WATCH,
                                ServerCalls.asyncServerStreamingCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            var observer =
                                                    (ServerCallStreamObserver<StringValue>) out;
                                            observer.setOnReadyHandler(
                                                    () -> {
                                                        while (observer.isReady()
                                                                && produced.get() < 100) {
                                                            int index = produced.getAndIncrement();
                                                            observer.onNext(
                                                                    StringValue.of(
                                                                            index
                                                                                    + ":"
                                                                                    + "x"
                                                                                            .repeat(
                                                                                                    4096)));
                                                        }
                                                        if (produced.compareAndSet(100, 101)) {
                                                            observer.onCompleted();
                                                        }
                                                    });
                                        }))
                        .build());
        var responses =
                ClientCalls.blockingServerStreamingCall(
                        channel,
                        WATCH,
                        CallOptions.DEFAULT.withDeadlineAfter(15, TimeUnit.SECONDS),
                        StringValue.of("watch"));
        var values = new ArrayList<String>();
        while (responses.hasNext()) {
            values.add(responses.next().getValue());
            Thread.sleep(2);
        }
        assertEquals(100, values.size());
        for (int i = 0; i < 100; i++) {
            assertEquals(i + ":" + "x".repeat(4096), values.get(i));
        }
    }

    @Test
    void cancellationAndShutdownReleaseAdmissionWithoutStoppingListener() throws Exception {
        adapter.setMaxConcurrentCalls(1);
        adapter.setShutdownTimeout(Duration.ofMillis(100));
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch cancelled = new CountDownLatch(1);
        start(
                ServerServiceDefinition.builder("review.Server")
                        .addMethod(
                                ECHO,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            if ("block".equals(request.getValue())) {
                                                ((ServerCallStreamObserver<StringValue>) out)
                                                        .setOnCancelHandler(cancelled::countDown);
                                                entered.countDown();
                                            } else {
                                                out.onNext(request);
                                                out.onCompleted();
                                            }
                                        }))
                        .build());
        var first =
                ClientCalls.futureUnaryCall(
                        channel.newCall(ECHO, CallOptions.DEFAULT), StringValue.of("block"));
        assertTrue(entered.await(10, TimeUnit.SECONDS));
        first.cancel(true);
        assertTrue(cancelled.await(10, TimeUnit.SECONDS));
        assertEquals("next", call("next"));
        var second =
                ClientCalls.futureUnaryCall(
                        channel.newCall(ECHO, CallOptions.DEFAULT), StringValue.of("block"));
        adapter.stop();
        assertThrows(
                java.util.concurrent.ExecutionException.class,
                () -> second.get(10, TimeUnit.SECONDS));
        assertTrue(http.isStarted());
        assertFalse(adapter.isStarted());
    }

    @Test
    void requestAndResponseMessageBudgetsAreEnforced() throws Exception {
        adapter.setMaxMessageBytes(128);
        start(echo());
        assertEquals("x".repeat(126), call("x".repeat(126)));
        assertEquals(
                Status.Code.RESOURCE_EXHAUSTED,
                assertThrows(StatusRuntimeException.class, () -> call("x".repeat(127)))
                        .getStatus()
                        .getCode());
        assertEquals("next", call("next"));
    }

    @Test
    void unknownMethodTypesFailExplicitly() {
        adapter.getServices()
                .add(
                        ServerServiceDefinition.builder("review.Server")
                                .addMethod(
                                        ECHO.toBuilder()
                                                .setType(MethodDescriptor.MethodType.UNKNOWN)
                                                .build(),
                                        ServerCalls.asyncUnaryCall(
                                                (StringValue request,
                                                        io.grpc.stub.StreamObserver<StringValue>
                                                                out) -> out.onCompleted()))
                                .build());
        assertThrows(UnsupportedOperationException.class, adapter::start);
    }

    /** Counts the messages and characters of an uploaded stream. */
    private static ServerServiceDefinition upload() {
        return ServerServiceDefinition.builder("review.Server")
                .addMethod(
                        UPLOAD,
                        ServerCalls.asyncClientStreamingCall(
                                out ->
                                        new io.grpc.stub.StreamObserver<StringValue>() {
                                            private int messages;
                                            private long characters;

                                            @Override
                                            public void onNext(StringValue value) {
                                                messages++;
                                                characters += value.getValue().length();
                                            }

                                            @Override
                                            public void onError(Throwable cause) {}

                                            @Override
                                            public void onCompleted() {
                                                out.onNext(
                                                        StringValue.of(
                                                                messages + ":" + characters));
                                                out.onCompleted();
                                            }
                                        }))
                .build();
    }

    @Test
    void clientStreamingReceivesEveryMessage() throws Exception {
        start(upload());
        var result = new java.util.concurrent.CompletableFuture<String>();
        var requests =
                ClientCalls.asyncClientStreamingCall(
                        channel.newCall(
                                UPLOAD,
                                CallOptions.DEFAULT.withDeadlineAfter(15, TimeUnit.SECONDS)),
                        observer(result));
        for (int i = 0; i < 200; i++) {
            requests.onNext(StringValue.of("x".repeat(4096)));
        }
        requests.onCompleted();
        assertEquals("200:" + (200 * 4096), result.get(15, TimeUnit.SECONDS));
    }

    /** Each reply is received before the next request is sent, over one HTTP/2 stream. */
    @Test
    void bidirectionalCallsExchangeMessagesBothWays() throws Exception {
        start(
                ServerServiceDefinition.builder("review.Server")
                        .addMethod(
                                CHAT,
                                ServerCalls.asyncBidiStreamingCall(
                                        out ->
                                                new io.grpc.stub.StreamObserver<StringValue>() {
                                                    @Override
                                                    public void onNext(StringValue value) {
                                                        out.onNext(
                                                                StringValue.of(
                                                                        "re:" + value.getValue()));
                                                    }

                                                    @Override
                                                    public void onError(Throwable cause) {}

                                                    @Override
                                                    public void onCompleted() {
                                                        out.onCompleted();
                                                    }
                                                }))
                        .build());
        var replies = new java.util.concurrent.LinkedBlockingQueue<String>();
        var done = new java.util.concurrent.CompletableFuture<Void>();
        var requests =
                ClientCalls.asyncBidiStreamingCall(
                        channel.newCall(
                                CHAT, CallOptions.DEFAULT.withDeadlineAfter(15, TimeUnit.SECONDS)),
                        new io.grpc.stub.StreamObserver<StringValue>() {
                            @Override
                            public void onNext(StringValue value) {
                                replies.add(value.getValue());
                            }

                            @Override
                            public void onError(Throwable cause) {
                                done.completeExceptionally(cause);
                            }

                            @Override
                            public void onCompleted() {
                                done.complete(null);
                            }
                        });
        for (int i = 0; i < 5; i++) {
            requests.onNext(StringValue.of("ping" + i));
            assertEquals("re:ping" + i, replies.poll(10, TimeUnit.SECONDS));
        }
        requests.onCompleted();
        done.get(10, TimeUnit.SECONDS);
        assertTrue(replies.isEmpty());
    }

    /**
     * Request bytes the application didn't ask for stay within the queue budget plus HTTP/2 flow
     * control windows, so a client that respects readiness stops sending.
     */
    @Test
    void unrequestedMessagesPushBackOnTheClient() throws Exception {
        adapter.setMaxQueuedBytes(32768);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger received = new AtomicInteger();
        start(
                ServerServiceDefinition.builder("review.Server")
                        .addMethod(
                                UPLOAD,
                                ServerCalls.asyncClientStreamingCall(
                                        out -> {
                                            var observer =
                                                    (ServerCallStreamObserver<StringValue>) out;
                                            observer.disableAutoRequest();
                                            Thread.startVirtualThread(
                                                    () -> {
                                                        try {
                                                            release.await();
                                                        } catch (InterruptedException e) {
                                                            return;
                                                        }
                                                        observer.request(Integer.MAX_VALUE);
                                                    });
                                            return new io.grpc.stub.StreamObserver<StringValue>() {
                                                @Override
                                                public void onNext(StringValue value) {
                                                    received.incrementAndGet();
                                                }

                                                @Override
                                                public void onError(Throwable cause) {}

                                                @Override
                                                public void onCompleted() {
                                                    out.onNext(
                                                            StringValue.of(
                                                                    Integer.toString(
                                                                            received.get())));
                                                    out.onCompleted();
                                                }
                                            };
                                        }))
                        .build());
        int total = 1024; // 4 MiB of 4 KiB messages
        AtomicInteger sent = new AtomicInteger();
        var result = new java.util.concurrent.CompletableFuture<String>();
        ClientCall<StringValue, StringValue> call =
                channel.newCall(
                        UPLOAD, CallOptions.DEFAULT.withDeadlineAfter(30, TimeUnit.SECONDS));
        call.start(
                new ClientCall.Listener<>() {
                    @Override
                    public void onMessage(StringValue message) {
                        result.complete(message.getValue());
                    }

                    @Override
                    public void onReady() {
                        while (call.isReady() && sent.get() < total) {
                            call.sendMessage(StringValue.of("x".repeat(4096)));
                            if (sent.incrementAndGet() == total) {
                                call.halfClose();
                            }
                        }
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        if (!status.isOk()) {
                            result.completeExceptionally(status.asRuntimeException());
                        }
                    }
                },
                new Metadata());
        call.request(1);
        Thread.sleep(1000);
        int stalled = sent.get();
        assertTrue(stalled < total / 2, "sent " + stalled + " of " + total + " messages");
        assertEquals(0, received.get());
        release.countDown();
        assertEquals(Integer.toString(total), result.get(30, TimeUnit.SECONDS));
    }

    /** A requested message larger than the queue budget is consumed as it arrives. */
    @Test
    void messagesLargerThanTheQueueBudgetStreamThrough() throws Exception {
        adapter.setMaxQueuedBytes(16384);
        start(echo());
        String large = "x".repeat(512 * 1024);
        assertEquals(large, call(large));
    }

    @Test
    void cancellingAStreamingCallReleasesItsSlot() throws Exception {
        adapter.setMaxConcurrentCalls(1);
        CountDownLatch cancelled = new CountDownLatch(1);
        CountDownLatch receiving = new CountDownLatch(1);
        start(
                ServerServiceDefinition.builder("review.Server")
                        .addMethod(
                                CHAT,
                                ServerCalls.asyncBidiStreamingCall(
                                        out -> {
                                            ((ServerCallStreamObserver<StringValue>) out)
                                                    .setOnCancelHandler(cancelled::countDown);
                                            return new io.grpc.stub.StreamObserver<StringValue>() {
                                                @Override
                                                public void onNext(StringValue value) {
                                                    receiving.countDown();
                                                }

                                                @Override
                                                public void onError(Throwable cause) {}

                                                @Override
                                                public void onCompleted() {
                                                    out.onCompleted();
                                                }
                                            };
                                        }))
                        .addMethod(
                                ECHO,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            out.onNext(request);
                                            out.onCompleted();
                                        }))
                        .build());
        ClientCall<StringValue, StringValue> call =
                channel.newCall(CHAT, CallOptions.DEFAULT.withDeadlineAfter(15, TimeUnit.SECONDS));
        call.start(new ClientCall.Listener<>() {}, new Metadata());
        call.sendMessage(StringValue.of("open"));
        assertTrue(receiving.await(10, TimeUnit.SECONDS));
        call.cancel("client gave up", null);
        assertTrue(cancelled.await(10, TimeUnit.SECONDS));
        assertEquals("next", call("next"));
    }

    private static io.grpc.stub.StreamObserver<StringValue> observer(
            java.util.concurrent.CompletableFuture<String> result) {
        return new io.grpc.stub.StreamObserver<>() {
            @Override
            public void onNext(StringValue value) {
                result.complete(value.getValue());
            }

            @Override
            public void onError(Throwable cause) {
                result.completeExceptionally(cause);
            }

            @Override
            public void onCompleted() {}
        };
    }

    private static byte[] frame(String value) throws IOException {
        var bytes = new ByteArrayOutputStream();
        var data = new DataOutputStream(bytes);
        byte[] message = StringValue.of(value).toByteArray();
        data.writeByte(0);
        data.writeInt(message.length);
        data.write(message);
        return bytes.toByteArray();
    }

    /** A direct HTTP/2 gRPC request; its abort action, if any, stands for the stream reset. */
    private static Request grpcRequest(
            MethodDescriptor<StringValue, StringValue> method, InputStream body, Runnable abort) {
        Request request =
                new Request(
                        org.restlet.data.Method.POST,
                        "http://localhost/" + method.getFullMethodName()) {
                    @Override
                    public boolean abort() {
                        abort.run();
                        return true;
                    }
                };
        request.setProtocol(Protocol.valueOf("HTTP", "2.0"));
        request.setEntity(new InputRepresentation(body, MediaType.valueOf("application/grpc")));
        return request;
    }

    private Response dispatch(MethodDescriptor<StringValue, StringValue> method, String value)
            throws Exception {
        Request request = grpcRequest(method, new ByteArrayInputStream(frame(value)), () -> {});
        Response response = new Response(request);
        adapter.handle(request, response);
        return response;
    }

    private static String trailersAfterWriting(Response response) throws IOException {
        response.getEntity().write(new ByteArrayOutputStream());
        return response.getTrailers().getFirstValue("grpc-status");
    }

    private static ServerServiceDefinition counting(AtomicInteger calls) {
        return ServerServiceDefinition.builder("review.Server")
                .addMethod(
                        ECHO,
                        ServerCalls.asyncUnaryCall(
                                (StringValue request,
                                        io.grpc.stub.StreamObserver<StringValue> out) -> {
                                    calls.incrementAndGet();
                                    out.onNext(request);
                                    out.onCompleted();
                                }))
                .build();
    }

    @Test
    void dotSegmentsCannotReachAMethodOutsideTheirRoute() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        adapter.getServices().add(counting(calls));
        adapter.start();
        Request request = grpcRequest(ECHO, new ByteArrayInputStream(frame("x")), () -> {});
        request.setResourceRef("http://localhost/public.Service/../review.Server/Echo");
        Response response = new Response(request);
        adapter.handle(request, response);
        assertEquals(org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND, response.getStatus());
        assertEquals(0, calls.get());
    }

    @Test
    void grpcMediaTypeParametersAreAccepted() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        adapter.getServices().add(counting(calls));
        adapter.start();
        Request request = grpcRequest(ECHO, new ByteArrayInputStream(frame("x")), () -> {});
        request.getEntity().setMediaType(MediaType.valueOf("application/grpc; version=1"));
        Response response = new Response(request);
        adapter.handle(request, response);
        assertEquals("0", trailersAfterWriting(response));
        assertEquals(1, calls.get());
    }

    private void assertBlockedDirectCallbackEndsAtTheDeadline(boolean inInterceptor)
            throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        CountDownLatch entered = new CountDownLatch(1);
        Runnable block =
                () -> {
                    entered.countDown();
                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                };
        adapter.setExecutor(Runnable::run);
        adapter.setMaxCallDuration(Duration.ofMillis(300));
        adapter.setMaxConcurrentCalls(1);
        if (inInterceptor) {
            adapter.getServices().add(echo());
            adapter.getInterceptors()
                    .add(
                            new io.grpc.ServerInterceptor() {
                                @Override
                                public <Q, S> io.grpc.ServerCall.Listener<Q> interceptCall(
                                        io.grpc.ServerCall<Q, S> call,
                                        Metadata headers,
                                        io.grpc.ServerCallHandler<Q, S> next) {
                                    block.run();
                                    return next.startCall(call, headers);
                                }
                            });
        } else {
            adapter.getServices()
                    .add(
                            ServerServiceDefinition.builder("review.Server")
                                    .addMethod(
                                            ECHO,
                                            ServerCalls.asyncUnaryCall(
                                                    (StringValue request,
                                                            io.grpc.stub.StreamObserver<StringValue>
                                                                    out) -> {
                                                        block.run();
                                                        out.onNext(request);
                                                        out.onCompleted();
                                                    }))
                                    .build());
        }
        adapter.start();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            // The call limit is 1, so blocked callbacks may hold up to 2 callback slots.
            for (int i = 0; i < 2; i++) {
                Response response =
                        executor.submit(() -> dispatch(ECHO, "blocked")).get(10, TimeUnit.SECONDS);
                assertTrue(entered.await(10, TimeUnit.SECONDS));
                assertEquals(
                        String.valueOf(Status.Code.DEADLINE_EXCEEDED.value()),
                        trailersAfterWriting(response));
            }
            assertEquals(
                    org.restlet.data.Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                    dispatch(ECHO, "bounded").getStatus());
            release.countDown();
            long limit = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            Response admitted = dispatch(ECHO, "admitted");
            while (org.restlet.data.Status.SERVER_ERROR_SERVICE_UNAVAILABLE.equals(
                            admitted.getStatus())
                    && System.nanoTime() < limit) {
                Thread.onSpinWait();
                admitted = dispatch(ECHO, "admitted");
            }
            assertEquals("0", trailersAfterWriting(admitted));
        } finally {
            release.countDown();
        }
    }

    @Test
    void blockingInterceptorWithADirectExecutorEndsAtTheDeadline() throws Exception {
        assertBlockedDirectCallbackEndsAtTheDeadline(true);
    }

    @Test
    void blockingHandlerWithADirectExecutorEndsAtTheDeadline() throws Exception {
        assertBlockedDirectCallbackEndsAtTheDeadline(false);
    }

    @Test
    void stopReportsTheTimeoutWhenACancellationListenerBlocks() throws Exception {
        adapter.setShutdownTimeout(Duration.ofMillis(300));
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        start(
                ServerServiceDefinition.builder("review.Server")
                        .addMethod(
                                ECHO,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            io.grpc.Context.current()
                                                    .addListener(
                                                            context -> {
                                                                try {
                                                                    release.await();
                                                                } catch (InterruptedException e) {
                                                                    Thread.currentThread()
                                                                            .interrupt();
                                                                }
                                                            },
                                                            Runnable::run);
                                            entered.countDown();
                                        }))
                        .build());
        ClientCalls.futureUnaryCall(
                channel.newCall(ECHO, CallOptions.DEFAULT), StringValue.of("block"));
        assertTrue(entered.await(10, TimeUnit.SECONDS));
        try {
            long started = System.nanoTime();
            assertTrue(
                    assertThrows(IllegalStateException.class, adapter::stop)
                            .getMessage()
                            .endsWith("shutdown timed out"));
            assertTrue(System.nanoTime() - started < TimeUnit.SECONDS.toNanos(5));
            assertFalse(adapter.isStarted());
        } finally {
            release.countDown();
        }
    }

    @Test
    void consecutiveCallsAtTheLimitAreAdmittedOnceDelivered() throws Exception {
        adapter.setMaxConcurrentCalls(1);
        start(echo());
        for (int i = 0; i < 50; i++) {
            assertEquals("call-" + i, call("call-" + i));
        }
    }

    @Test
    void initialMetadataWithStandardHeaderNamesIsSent() throws Exception {
        Metadata.Key<String> cacheControl =
                Metadata.Key.of("cache-control", Metadata.ASCII_STRING_MARSHALLER);
        Metadata.Key<String> etag = Metadata.Key.of("etag", Metadata.ASCII_STRING_MARSHALLER);
        Metadata.Key<String> custom = Metadata.Key.of("x-detail", Metadata.ASCII_STRING_MARSHALLER);
        Metadata.Key<String> allow = Metadata.Key.of("allow", Metadata.ASCII_STRING_MARSHALLER);
        Metadata.Key<String> expose =
                Metadata.Key.of("access-control-expose-headers", Metadata.ASCII_STRING_MARSHALLER);
        adapter.getInterceptors()
                .add(
                        new io.grpc.ServerInterceptor() {
                            @Override
                            public <Q, S> io.grpc.ServerCall.Listener<Q> interceptCall(
                                    io.grpc.ServerCall<Q, S> call,
                                    Metadata headers,
                                    io.grpc.ServerCallHandler<Q, S> next) {
                                return next.startCall(
                                        new io.grpc.ForwardingServerCall
                                                .SimpleForwardingServerCall<>(call) {
                                            @Override
                                            public void sendHeaders(Metadata metadata) {
                                                metadata.put(cacheControl, "no-cache");
                                                metadata.put(cacheControl, "no-store");
                                                metadata.put(etag, "\"v1\"");
                                                metadata.put(custom, "kept");
                                                metadata.put(allow, "POST");
                                                metadata.put(expose, "x-detail");
                                                super.sendHeaders(metadata);
                                            }
                                        },
                                        headers);
                            }
                        });
        start(echo());
        AtomicReference<Metadata> received = new AtomicReference<>();
        AtomicReference<Metadata> trailers = new AtomicReference<>();
        var intercepted =
                io.grpc.ClientInterceptors.intercept(
                        channel,
                        io.grpc.stub.MetadataUtils.newCaptureMetadataInterceptor(
                                received, trailers));
        assertEquals(
                "meta",
                ClientCalls.blockingUnaryCall(
                                intercepted,
                                ECHO,
                                CallOptions.DEFAULT.withDeadlineAfter(10, TimeUnit.SECONDS),
                                StringValue.of("meta"))
                        .getValue());
        String directives = String.join(",", received.get().getAll(cacheControl));
        assertTrue(directives.contains("no-cache"), directives);
        assertTrue(directives.contains("no-store"), directives);
        assertEquals("\"v1\"", received.get().get(etag));
        assertEquals("kept", received.get().get(custom));
        assertEquals("POST", received.get().get(allow));
        assertEquals("x-detail", received.get().get(expose));
        var values = new ArrayList<String>();
        received.get().getAll(cacheControl).forEach(values::add);
        assertEquals(List.of("no-cache", "no-store"), values);
    }

    @Test
    void streamingErrorAfterMessagesKeepsItsStatusAndTrailers() throws Exception {
        Metadata.Key<String> key = Metadata.Key.of("x-detail", Metadata.ASCII_STRING_MARSHALLER);
        start(
                ServerServiceDefinition.builder("review.Server")
                        .addMethod(
                                WATCH,
                                ServerCalls.asyncServerStreamingCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            out.onNext(StringValue.of("first"));
                                            out.onNext(StringValue.of("second"));
                                            Metadata trailers = new Metadata();
                                            trailers.put(key, "late");
                                            out.onError(
                                                    Status.ABORTED
                                                            .withDescription("after messages")
                                                            .asRuntimeException(trailers));
                                        }))
                        .build());
        var responses =
                ClientCalls.blockingServerStreamingCall(
                        channel,
                        WATCH,
                        CallOptions.DEFAULT.withDeadlineAfter(10, TimeUnit.SECONDS),
                        StringValue.of("watch"));
        assertEquals("first", responses.next().getValue());
        assertEquals("second", responses.next().getValue());
        var error = assertThrows(StatusRuntimeException.class, responses::hasNext);
        assertEquals(Status.Code.ABORTED, error.getStatus().getCode());
        assertEquals("after messages", error.getStatus().getDescription());
        assertEquals("late", java.util.Objects.requireNonNull(error.getTrailers()).get(key));
    }

    @Test
    void transportAttributesCarryTheSocketAddresses() throws Exception {
        AtomicReference<io.grpc.Attributes> seen = new AtomicReference<>();
        adapter.getInterceptors()
                .add(
                        new io.grpc.ServerInterceptor() {
                            @Override
                            public <Q, S> io.grpc.ServerCall.Listener<Q> interceptCall(
                                    io.grpc.ServerCall<Q, S> call,
                                    Metadata headers,
                                    io.grpc.ServerCallHandler<Q, S> next) {
                                seen.set(call.getAttributes());
                                return next.startCall(call, headers);
                            }
                        });
        start(echo());
        assertEquals("peer", call("peer"));
        var remote =
                (java.net.InetSocketAddress)
                        seen.get().get(io.grpc.Grpc.TRANSPORT_ATTR_REMOTE_ADDR);
        var local =
                (java.net.InetSocketAddress) seen.get().get(io.grpc.Grpc.TRANSPORT_ATTR_LOCAL_ADDR);
        assertTrue(remote.getAddress().isLoopbackAddress(), remote::toString);
        assertTrue(remote.getPort() > 0);
        assertTrue(local.getAddress().isLoopbackAddress(), local::toString);
        assertEquals(http.getActualPort(), local.getPort());
    }

    @Test
    void blockingClientDeadlineListenerCannotDelayCallBudgets() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        adapter.setMaxCallDuration(Duration.ofSeconds(1));
        adapter.getServices()
                .add(
                        ServerServiceDefinition.builder("review.Server")
                                .addMethod(
                                        ECHO,
                                        (call, headers) -> {
                                            // Runs where the client deadline expires.
                                            io.grpc.Context.current()
                                                    .addListener(
                                                            context -> {
                                                                entered.countDown();
                                                                try {
                                                                    release.await();
                                                                } catch (InterruptedException e) {
                                                                    Thread.currentThread()
                                                                            .interrupt();
                                                                }
                                                            },
                                                            Runnable::run);
                                            call.sendHeaders(new Metadata());
                                            return new io.grpc.ServerCall.Listener<>() {};
                                        })
                                .addMethod(
                                        WATCH,
                                        (call, headers) -> {
                                            call.sendHeaders(new Metadata());
                                            return new io.grpc.ServerCall.Listener<>() {};
                                        })
                                .build());
        adapter.start();
        // Released before the executor waits for its tasks, which the listener may hold.
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            try {
                Request blocking =
                        grpcRequest(ECHO, new ByteArrayInputStream(frame("x")), () -> {});
                blocking.getHeaders().add("grpc-timeout", "100m");
                executor.submit(() -> adapter.handle(blocking, new Response(blocking)));
                assertTrue(entered.await(10, TimeUnit.SECONDS));
                Response budgeted = dispatch(WATCH, "y");
                assertEquals(
                        String.valueOf(Status.Code.DEADLINE_EXCEEDED.value()),
                        executor.submit(() -> trailersAfterWriting(budgeted))
                                .get(5, TimeUnit.SECONDS));
            } finally {
                release.countDown();
            }
        }
    }

    @Test
    void http1RequestsAreRejectedBeforeDispatch() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        adapter.getServices()
                .add(
                        ServerServiceDefinition.builder("review.Server")
                                .addMethod(
                                        ECHO,
                                        ServerCalls.asyncUnaryCall(
                                                (StringValue request,
                                                        io.grpc.stub.StreamObserver<StringValue>
                                                                out) -> {
                                                    calls.incrementAndGet();
                                                    out.onNext(request);
                                                    out.onCompleted();
                                                }))
                                .build());
        adapter.start();
        Request request = grpcRequest(ECHO, new ByteArrayInputStream(frame("x")), () -> {});
        request.setProtocol(Protocol.valueOf("HTTP", "1.1"));
        Response response = new Response(request);
        adapter.handle(request, response);
        assertEquals(
                org.restlet.data.Status.SERVER_ERROR_VERSION_NOT_SUPPORTED, response.getStatus());
        assertEquals(0, calls.get());
    }

    @Test
    void unreadableRequestStillSendsItsGrpcStatus() throws Exception {
        adapter.getServices().add(echo());
        adapter.start();
        Request request =
                grpcRequest(
                        ECHO,
                        new InputStream() {
                            @Override
                            public int read() throws IOException {
                                throw new IOException("broken body");
                            }
                        },
                        () -> {});
        Response response = new Response(request);
        adapter.handle(request, response);
        // A request stream that breaks while read is a peer reset, as grpc-netty reports it.
        assertEquals(String.valueOf(Status.Code.CANCELLED.value()), trailersAfterWriting(response));
    }

    @Test
    void stalledRequestBodyIsResetAtTheDeadline() throws Exception {
        adapter.setMaxCallDuration(Duration.ofMillis(300));
        adapter.getServices().add(echo());
        adapter.start();
        CountDownLatch reset = new CountDownLatch(1);
        InputStream stalled =
                new InputStream() {
                    @Override
                    public int read() throws IOException {
                        try {
                            reset.await();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                        throw new IOException("stream reset");
                    }
                };
        Request request = grpcRequest(ECHO, stalled, reset::countDown);
        Response response = new Response(request);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> adapter.handle(request, response)).get(10, TimeUnit.SECONDS);
        }
        // The request is read apart from the handler, which returns once the call ends; the read
        // still stuck after that is reset shortly after.
        assertTrue(reset.await(10, TimeUnit.SECONDS));
        assertEquals(
                String.valueOf(Status.Code.DEADLINE_EXCEEDED.value()),
                trailersAfterWriting(response));
    }

    @Test
    void responseWriteBlockedByThePeerIsResetAtShutdown() throws Exception {
        adapter.getServices()
                .add(
                        ServerServiceDefinition.builder("review.Server")
                                .addMethod(
                                        WATCH,
                                        ServerCalls.asyncServerStreamingCall(
                                                (StringValue request,
                                                        io.grpc.stub.StreamObserver<StringValue>
                                                                out) ->
                                                        out.onNext(StringValue.of("first"))))
                                .build());
        adapter.start();
        CountDownLatch reset = new CountDownLatch(1);
        Request request =
                grpcRequest(WATCH, new ByteArrayInputStream(frame("watch")), reset::countDown);
        Response response = new Response(request);
        adapter.handle(request, response);
        CountDownLatch writing = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var writer =
                    executor.submit(
                            () -> {
                                response.getEntity()
                                        .write(
                                                new OutputStream() {
                                                    @Override
                                                    public void write(int value)
                                                            throws IOException {
                                                        writing.countDown();
                                                        try {
                                                            reset.await();
                                                        } catch (InterruptedException e) {
                                                            Thread.currentThread().interrupt();
                                                        }
                                                        throw new IOException("stream reset");
                                                    }
                                                });
                                return null;
                            });
            assertTrue(writing.await(10, TimeUnit.SECONDS));
            adapter.stop();
            assertTrue(reset.await(10, TimeUnit.SECONDS));
            assertThrows(
                    java.util.concurrent.ExecutionException.class,
                    () -> writer.get(10, TimeUnit.SECONDS));
        }
    }

    @Test
    void callbacksMayUseTheAdapterWhileItStops() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        start(
                ServerServiceDefinition.builder("review.Server")
                        .addMethod(
                                ECHO,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            CountDownLatch cancelled = new CountDownLatch(1);
                                            io.grpc.Context.current()
                                                    .addListener(
                                                            context -> cancelled.countDown(),
                                                            Runnable::run);
                                            entered.countDown();
                                            try {
                                                cancelled.await();
                                            } catch (InterruptedException e) {
                                                Thread.currentThread().interrupt();
                                            }
                                            adapter.getMaxConcurrentCalls();
                                        }))
                        .build());
        ClientCalls.futureUnaryCall(
                channel.newCall(ECHO, CallOptions.DEFAULT), StringValue.of("block"));
        assertTrue(entered.await(10, TimeUnit.SECONDS));
        adapter.stop();
        assertFalse(adapter.isStarted());
        assertTrue(http.isStarted());
    }

    @Test
    void unaryResponseLargerThanTheQueueBudgetFitsWithADirectExecutor() throws Exception {
        adapter.setExecutor(Runnable::run);
        String large = "x".repeat(512 * 1024);
        start(
                ServerServiceDefinition.builder("review.Server")
                        .addMethod(
                                ECHO,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            out.onNext(StringValue.of(large));
                                            out.onCompleted();
                                        }))
                        .build());
        assertEquals(large, call("large"));
    }

    @Test
    void completedResponseRetainsAdmissionUntilSentAndBorrowedExecutorSurvives() throws Exception {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            adapter.setExecutor(executor);
            adapter.setMaxConcurrentCalls(1);
            adapter.getServices().add(echo());
            adapter.start();
            Response first = dispatch(ECHO, "first");
            first.getEntity().write(OutputStream.nullOutputStream());
            assertEquals(
                    org.restlet.data.Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                    dispatch(ECHO, "busy").getStatus());
            first.notifyDelivered(null);
            first.notifyDelivered(null);
            Response next = dispatch(ECHO, "next");
            next.getEntity().write(OutputStream.nullOutputStream());
            next.notifyDelivered(null);
            adapter.stop();
            adapter.stop();
            assertFalse(executor.isShutdown());
            assertEquals("still available", executor.submit(() -> "still available").get());
        }
    }

    @Test
    void stalledWriterBoundsReadinessAndResumesInOrder() throws Exception {
        int queueBudget = 32768;
        adapter.setMaxQueuedBytes(queueBudget);
        AtomicInteger produced = new AtomicInteger();
        CountDownLatch backpressured = new CountDownLatch(1);
        AtomicReference<ServerCallStreamObserver<StringValue>> observerRef =
                new AtomicReference<>();
        adapter.getServices()
                .add(
                        ServerServiceDefinition.builder("review.Server")
                                .addMethod(
                                        WATCH,
                                        ServerCalls.asyncServerStreamingCall(
                                                (StringValue request,
                                                        io.grpc.stub.StreamObserver<StringValue>
                                                                out) -> {
                                                    var observer =
                                                            (ServerCallStreamObserver<StringValue>)
                                                                    out;
                                                    observerRef.set(observer);
                                                    observer.setOnReadyHandler(
                                                            () -> {
                                                                while (observer.isReady()
                                                                        && produced.get() < 100) {
                                                                    observer.onNext(
                                                                            StringValue.of(
                                                                                    produced
                                                                                                    .getAndIncrement()
                                                                                            + ":"
                                                                                            + "x"
                                                                                                    .repeat(
                                                                                                            4096)));
                                                                }
                                                                if (produced.compareAndSet(
                                                                        100, 101)) {
                                                                    observer.onCompleted();
                                                                } else if (!observer.isReady()) {
                                                                    backpressured.countDown();
                                                                }
                                                            });
                                                }))
                                .build());
        adapter.start();
        Response response = dispatch(WATCH, "watch");
        CountDownLatch stalled = new CountDownLatch(1);
        CountDownLatch resume = new CountDownLatch(1);
        var received = new ByteArrayOutputStream();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var writer =
                    executor.submit(
                            () -> {
                                response.getEntity()
                                        .write(
                                                new OutputStream() {
                                                    @Override
                                                    public void write(int value) {
                                                        received.write(value);
                                                    }

                                                    @Override
                                                    public void write(
                                                            byte[] bytes, int offset, int length)
                                                            throws IOException {
                                                        stalled.countDown();
                                                        try {
                                                            if (!resume.await(
                                                                    10, TimeUnit.SECONDS)) {
                                                                throw new IOException(
                                                                        "Stalled writer was not released");
                                                            }
                                                        } catch (InterruptedException e) {
                                                            Thread.currentThread().interrupt();
                                                            throw new IOException(e);
                                                        }
                                                        received.write(bytes, offset, length);
                                                    }
                                                });
                                return null;
                            });
            try {
                assertTrue(stalled.await(10, TimeUnit.SECONDS));
                assertTrue(backpressured.await(10, TimeUnit.SECONDS));
                assertFalse(observerRef.get().isReady());
                // Each queued response contains at least 4096 payload bytes.
                assertTrue(produced.get() * 4096 <= queueBudget);
            } finally {
                resume.countDown();
            }
            writer.get(10, TimeUnit.SECONDS);
        }
        var input = new java.io.DataInputStream(new ByteArrayInputStream(received.toByteArray()));
        for (int i = 0; i < 100; i++) {
            assertEquals(0, input.readUnsignedByte());
            int size = input.readInt();
            assertEquals(
                    i + ":" + "x".repeat(4096),
                    StringValue.parseFrom(input.readNBytes(size)).getValue());
        }
        assertEquals(-1, input.read());
        assertEquals("0", response.getTrailers().getFirstValue("grpc-status"));
        response.notifyDelivered(null);
    }

    @Test
    void producerIgnoringReadinessFailsAtQueueBudget() throws Exception {
        adapter.setMaxQueuedBytes(16384);
        CountDownLatch produced = new CountDownLatch(1);
        adapter.getServices()
                .add(
                        ServerServiceDefinition.builder("review.Server")
                                .addMethod(
                                        WATCH,
                                        ServerCalls.asyncServerStreamingCall(
                                                (StringValue request,
                                                        io.grpc.stub.StreamObserver<StringValue>
                                                                out) -> {
                                                    for (int i = 0; i < 16; i++) {
                                                        out.onNext(
                                                                StringValue.of("x".repeat(4096)));
                                                    }
                                                    out.onCompleted();
                                                    produced.countDown();
                                                }))
                                .build());
        adapter.start();
        Response response = dispatch(WATCH, "watch");
        assertTrue(produced.await(10, TimeUnit.SECONDS));
        var bytes = new ByteArrayOutputStream();
        response.getEntity().write(bytes);
        assertEquals(0, bytes.size());
        assertEquals("8", response.getTrailers().getFirstValue("grpc-status"));
        assertEquals(
                "Response queue byte budget exceeded",
                response.getTrailers().getFirstValue("grpc-message"));
    }

    @Test
    void serverDeadlineCancelsHandlerAndReleasesAdmission() throws Exception {
        adapter.setMaxConcurrentCalls(1);
        adapter.setMaxCallDuration(Duration.ofMillis(200));
        CountDownLatch cancelled = new CountDownLatch(1);
        start(
                ServerServiceDefinition.builder("review.Server")
                        .addMethod(
                                ECHO,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            if ("block".equals(request.getValue())) {
                                                ((ServerCallStreamObserver<StringValue>) out)
                                                        .setOnCancelHandler(cancelled::countDown);
                                            } else {
                                                out.onNext(request);
                                                out.onCompleted();
                                            }
                                        }))
                        .build());
        assertEquals(
                Status.Code.DEADLINE_EXCEEDED,
                assertThrows(StatusRuntimeException.class, () -> call("block"))
                        .getStatus()
                        .getCode());
        assertTrue(cancelled.await(10, TimeUnit.SECONDS));
        assertEquals("next", call("next"));
    }

    @Test
    void oversizedResponseFailsAndNextCallSucceeds() throws Exception {
        adapter.setMaxMessageBytes(128);
        start(
                ServerServiceDefinition.builder("review.Server")
                        .addMethod(
                                ECHO,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            out.onNext(
                                                    "large".equals(request.getValue())
                                                            ? StringValue.of("x".repeat(127))
                                                            : request);
                                            out.onCompleted();
                                        }))
                        .build());
        assertEquals(
                Status.Code.RESOURCE_EXHAUSTED,
                assertThrows(StatusRuntimeException.class, () -> call("large"))
                        .getStatus()
                        .getCode());
        assertEquals("next", call("next"));
    }

    @Test
    void shutdownWakesActiveStreamWriterAndDeliversCancellation() throws Exception {
        CountDownLatch cancelled = new CountDownLatch(1);
        adapter.getServices()
                .add(
                        ServerServiceDefinition.builder("review.Server")
                                .addMethod(
                                        WATCH,
                                        ServerCalls.asyncServerStreamingCall(
                                                (StringValue request,
                                                        io.grpc.stub.StreamObserver<StringValue>
                                                                out) -> {
                                                    ((ServerCallStreamObserver<StringValue>) out)
                                                            .setOnCancelHandler(
                                                                    cancelled::countDown);
                                                    out.onNext(StringValue.of("first"));
                                                }))
                                .build());
        adapter.start();
        Response response = dispatch(WATCH, "watch");
        CountDownLatch writing = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var writer =
                    executor.submit(
                            () -> {
                                response.getEntity()
                                        .write(
                                                new OutputStream() {
                                                    @Override
                                                    public void write(int value) {
                                                        writing.countDown();
                                                    }
                                                });
                                return null;
                            });
            assertTrue(writing.await(10, TimeUnit.SECONDS));
            adapter.stop();
            writer.get(10, TimeUnit.SECONDS);
            assertTrue(cancelled.await(10, TimeUnit.SECONDS));
            assertEquals("14", response.getTrailers().getFirstValue("grpc-status"));
            assertFalse(adapter.isStarted());
        }
    }

    @Test
    void ownedCallbackShutdownTimeoutIsReportedAndInterruptsWorker() throws Exception {
        adapter.setShutdownTimeout(Duration.ofMillis(200));
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch interrupted = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        start(
                ServerServiceDefinition.builder("review.Server")
                        .addMethod(
                                ECHO,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            entered.countDown();
                                            try {
                                                release.await();
                                            } catch (InterruptedException e) {
                                                Thread.currentThread().interrupt();
                                                interrupted.countDown();
                                            }
                                        }))
                        .build());
        ClientCalls.futureUnaryCall(
                channel.newCall(ECHO, CallOptions.DEFAULT), StringValue.of("block"));
        assertTrue(entered.await(10, TimeUnit.SECONDS));
        try {
            assertEquals(
                    "gRPC callback executor shutdown timed out",
                    assertThrows(IllegalStateException.class, adapter::stop).getMessage());
            assertTrue(interrupted.await(10, TimeUnit.SECONDS));
            assertTrue(http.isStarted());
            assertFalse(adapter.isStarted());
        } finally {
            release.countDown();
        }
    }

    @Test
    void clientCancellationAfterStreamMessageReleasesAdmission() throws Exception {
        adapter.setMaxConcurrentCalls(1);
        CountDownLatch cancelled = new CountDownLatch(1);
        var service = ServerServiceDefinition.builder("review.Server");
        echo().getMethods().forEach(service::addMethod);
        start(
                service.addMethod(
                                WATCH,
                                ServerCalls.asyncServerStreamingCall(
                                        (StringValue request,
                                                io.grpc.stub.StreamObserver<StringValue> out) -> {
                                            ((ServerCallStreamObserver<StringValue>) out)
                                                    .setOnCancelHandler(cancelled::countDown);
                                            out.onNext(StringValue.of("first"));
                                        }))
                        .build());
        CountDownLatch closed = new CountDownLatch(1);
        AtomicReference<Status> status = new AtomicReference<>();
        var call =
                channel.newCall(WATCH, CallOptions.DEFAULT.withDeadlineAfter(10, TimeUnit.SECONDS));
        call.start(
                new ClientCall.Listener<>() {
                    @Override
                    public void onMessage(StringValue value) {
                        call.cancel("cancel after first message", null);
                    }

                    @Override
                    public void onClose(Status value, Metadata trailers) {
                        status.set(value);
                        closed.countDown();
                    }
                },
                new Metadata());
        call.request(1);
        call.sendMessage(StringValue.of("watch"));
        call.halfClose();
        assertTrue(closed.await(10, TimeUnit.SECONDS));
        assertEquals(Status.Code.CANCELLED, status.get().getCode());
        assertTrue(cancelled.await(10, TimeUnit.SECONDS));
        assertEquals("next", call("next"));
    }

    @Test
    void disconnectRacingResponseSentTerminatesEachTransportOnce() throws Exception {
        adapter.setMaxConcurrentCalls(1);
        adapter.getServices()
                .add(
                        ServerServiceDefinition.builder("review.Server")
                                .addMethod(
                                        WATCH,
                                        ServerCalls.asyncServerStreamingCall(
                                                (StringValue request,
                                                        io.grpc.stub.StreamObserver<StringValue>
                                                                out) -> out.onNext(request)))
                                .build());
        adapter.start();
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 32; i++) {
                Response response = dispatch(WATCH, "watch");
                assertEquals(org.restlet.data.Status.SUCCESS_OK, response.getStatus());
                var writer =
                        executor.submit(
                                () -> {
                                    response.getEntity().write(OutputStream.nullOutputStream());
                                    response.notifyDelivered(null);
                                    return null;
                                });
                var disconnect =
                        executor.submit(
                                () ->
                                        response.getRequest()
                                                .notifyDisconnect(new IOException("Peer reset")));
                disconnect.get(10, TimeUnit.SECONDS);
                writer.get(10, TimeUnit.SECONDS);
            }
        }
        adapter.stop();
    }
}
