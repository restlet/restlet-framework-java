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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.protobuf.StringValue;
import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.ServerCalls;
import io.grpc.stub.StreamObserver;
import java.time.Duration;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;

/**
 * Streaming calls of every type through {@link GrpcClientAdapter}, against grpc-java's own Netty
 * server and against a {@link GrpcServerAdapter}, so that both ends may be Restlet.
 */
class GrpcClientStreamingTestCase {
    private static final MethodDescriptor<StringValue, StringValue> ECHO =
            MethodDescriptor.<StringValue, StringValue>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName("review.Streams/Echo")
                    .setRequestMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .build();
    private static final MethodDescriptor<StringValue, StringValue> WATCH =
            ECHO.toBuilder()
                    .setType(MethodDescriptor.MethodType.SERVER_STREAMING)
                    .setFullMethodName("review.Streams/Watch")
                    .build();
    private static final MethodDescriptor<StringValue, StringValue> UPLOAD =
            ECHO.toBuilder()
                    .setType(MethodDescriptor.MethodType.CLIENT_STREAMING)
                    .setFullMethodName("review.Streams/Upload")
                    .build();
    private static final MethodDescriptor<StringValue, StringValue> CHAT =
            ECHO.toBuilder()
                    .setType(MethodDescriptor.MethodType.BIDI_STREAMING)
                    .setFullMethodName("review.Streams/Chat")
                    .build();
    private static final String BLOCK = "x".repeat(4096);

    private final GrpcClientAdapter adapter = new GrpcClientAdapter();
    private Client client;
    private io.grpc.Server nettyPeer;
    private GrpcServerAdapter restletPeer;
    private Server http;

    /** Starts the peer, Restlet or Netty, and the adapter calling it. */
    private void start(boolean restlet, ServerServiceDefinition service) throws Exception {
        int port;
        if (restlet) {
            restletPeer = new GrpcServerAdapter();
            // Above the 5 s default: the demand tests stall on purpose before moving 4 MiB.
            restletPeer.setMaxCallDuration(Duration.ofSeconds(30));
            restletPeer.getServices().add(service);
            restletPeer.start();
            Router router = new Router();
            restletPeer.attachTo(router);
            Context context = new Context();
            context.getParameters().set("http.transport.protocol", "HTTP2");
            http = new Server(context, Protocol.HTTP, 0, router);
            http.start();
            port = http.getActualPort();
        } else {
            // A fixed flow control window, so that the peer cannot absorb what the client holds.
            nettyPeer =
                    NettyServerBuilder.forPort(0)
                            .flowControlWindow(65536)
                            .addService(service)
                            .build()
                            .start();
            port = nettyPeer.getPort();
        }
        Context context = new Context();
        context.getParameters().set("httpClientTransportMode", "HTTP2");
        context.getParameters().set("followRedirects", "false");
        client = new Client(context, Protocol.HTTP);
        client.start();
        adapter.setClient(client);
        adapter.setTarget("dns:///localhost:" + port);
        adapter.setPlaintext(true);
        adapter.start();
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
                if (nettyPeer != null) {
                    nettyPeer.shutdownNow();
                    assertTrue(nettyPeer.awaitTermination(30, TimeUnit.SECONDS));
                }
                if (restletPeer != null) {
                    restletPeer.stop();
                }
                if (http != null) {
                    http.stop();
                }
            }
        }
    }

    private static CallOptions deadline() {
        return CallOptions.DEFAULT.withDeadlineAfter(30, TimeUnit.SECONDS);
    }

    /** A service with one method of each streaming type. */
    private static ServerServiceDefinition streams() {
        return ServerServiceDefinition.builder("review.Streams")
                .addMethod(
                        ECHO,
                        ServerCalls.asyncUnaryCall(
                                (StringValue request, StreamObserver<StringValue> out) -> {
                                    out.onNext(request);
                                    out.onCompleted();
                                }))
                .addMethod(
                        WATCH,
                        ServerCalls.asyncServerStreamingCall(
                                (StringValue request, StreamObserver<StringValue> out) -> {
                                    int count = Integer.parseInt(request.getValue());
                                    for (int i = 0; i < count; i++) {
                                        out.onNext(StringValue.of(i + ":" + BLOCK));
                                    }
                                    out.onCompleted();
                                }))
                .addMethod(
                        UPLOAD,
                        ServerCalls.asyncClientStreamingCall(
                                out ->
                                        new StreamObserver<StringValue>() {
                                            private int messages;
                                            private long characters;

                                            @Override
                                            public void onNext(StringValue value) {
                                                messages++;
                                                characters += value.getValue().length();
                                            }

                                            @Override
                                            public void onError(Throwable cause) {
                                                // The call ended; nothing to answer.
                                            }

                                            @Override
                                            public void onCompleted() {
                                                out.onNext(
                                                        StringValue.of(
                                                                messages + ":" + characters));
                                                out.onCompleted();
                                            }
                                        }))
                .addMethod(
                        CHAT,
                        ServerCalls.asyncBidiStreamingCall(
                                out ->
                                        new StreamObserver<StringValue>() {
                                            @Override
                                            public void onNext(StringValue value) {
                                                out.onNext(
                                                        StringValue.of("re:" + value.getValue()));
                                            }

                                            @Override
                                            public void onError(Throwable cause) {
                                                // The call ended; nothing to answer.
                                            }

                                            @Override
                                            public void onCompleted() {
                                                out.onCompleted();
                                            }
                                        }))
                .build();
    }

    /** The producer ignores readiness, so the stream stays within the 256 KiB queue budget. */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void serverStreamingPreservesOrder(boolean restlet) throws Exception {
        start(restlet, streams());
        var responses =
                ClientCalls.blockingServerStreamingCall(
                        adapter.getChannel(), WATCH, deadline(), StringValue.of("50"));
        var values = new ArrayList<String>();
        responses.forEachRemaining(value -> values.add(value.getValue()));
        assertEquals(50, values.size());
        for (int i = 0; i < 50; i++) {
            assertEquals(i + ":" + BLOCK, values.get(i));
        }
    }

    /** The sender ignores readiness, so the stream stays within the 256 KiB queue budget. */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void clientStreamingSendsEveryMessage(boolean restlet) throws Exception {
        start(restlet, streams());
        var result = new CompletableFuture<String>();
        var requests =
                ClientCalls.asyncClientStreamingCall(
                        adapter.getChannel().newCall(UPLOAD, deadline()), single(result));
        for (int i = 0; i < 50; i++) {
            requests.onNext(StringValue.of(BLOCK));
        }
        requests.onCompleted();
        assertEquals("50:" + (50 * BLOCK.length()), result.get(30, TimeUnit.SECONDS));
    }

    /** Each reply is received before the next request is sent, over one HTTP/2 stream. */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void bidirectionalCallsExchangeMessagesBothWays(boolean restlet) throws Exception {
        start(restlet, streams());
        var replies = new LinkedBlockingQueue<String>();
        var done = new CompletableFuture<Void>();
        var requests =
                ClientCalls.asyncBidiStreamingCall(
                        adapter.getChannel().newCall(CHAT, deadline()),
                        new StreamObserver<StringValue>() {
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
            assertEquals("re:ping" + i, replies.poll(30, TimeUnit.SECONDS));
        }
        requests.onCompleted();
        done.get(30, TimeUnit.SECONDS);
        assertTrue(replies.isEmpty());
    }

    /** A client respecting readiness stops sending while the server requests nothing. */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void requestsFollowTheServerDemand(boolean restlet) throws Exception {
        adapter.setMaxQueuedBytes(32768);
        CountDownLatch release = new CountDownLatch(1);
        AtomicInteger received = new AtomicInteger();
        start(
                restlet,
                ServerServiceDefinition.builder("review.Streams")
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
                                                        } catch (InterruptedException _) {
                                                            Thread.currentThread().interrupt();
                                                            return;
                                                        }
                                                        observer.request(Integer.MAX_VALUE);
                                                    });
                                            return new StreamObserver<StringValue>() {
                                                @Override
                                                public void onNext(StringValue value) {
                                                    received.incrementAndGet();
                                                }

                                                @Override
                                                public void onError(Throwable cause) {
                                                    // The call ended; nothing to answer.
                                                }

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
        var result = new CompletableFuture<String>();
        ClientCall<StringValue, StringValue> call =
                adapter.getChannel().newCall(UPLOAD, deadline());
        call.start(
                new ClientCall.Listener<>() {
                    @Override
                    public void onMessage(StringValue message) {
                        result.complete(message.getValue());
                    }

                    @Override
                    public void onReady() {
                        while (call.isReady() && sent.get() < total) {
                            call.sendMessage(StringValue.of(BLOCK));
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
        // Let the sender fill every buffer on the way.
        Thread.sleep(1000);
        int stalled = sent.get();
        assertTrue(stalled < total / 2, "sent " + stalled + " of " + total + " messages");
        assertEquals(0, received.get());
        release.countDown();
        assertEquals(Integer.toString(total), result.get(30, TimeUnit.SECONDS));
    }

    /** A server respecting readiness stops sending while the client requests nothing. */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void responsesFollowTheClientDemand(boolean restlet) throws Exception {
        adapter.setMaxQueuedBytes(32768);
        int total = 1024;
        AtomicInteger produced = new AtomicInteger();
        start(
                restlet,
                ServerServiceDefinition.builder("review.Streams")
                        .addMethod(
                                WATCH,
                                ServerCalls.asyncServerStreamingCall(
                                        (StringValue request, StreamObserver<StringValue> out) -> {
                                            var observer =
                                                    (ServerCallStreamObserver<StringValue>) out;
                                            observer.setOnReadyHandler(
                                                    () -> {
                                                        while (observer.isReady()
                                                                && produced.get() < total) {
                                                            produced.incrementAndGet();
                                                            observer.onNext(StringValue.of(BLOCK));
                                                        }
                                                        if (produced.compareAndSet(
                                                                total, total + 1)) {
                                                            observer.onCompleted();
                                                        }
                                                    });
                                        }))
                        .build());
        AtomicInteger received = new AtomicInteger();
        var closed = new CompletableFuture<Status>();
        ClientCall<StringValue, StringValue> call = adapter.getChannel().newCall(WATCH, deadline());
        call.start(
                new ClientCall.Listener<>() {
                    @Override
                    public void onMessage(StringValue message) {
                        received.incrementAndGet();
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        closed.complete(status);
                    }
                },
                new Metadata());
        call.sendMessage(StringValue.of("go"));
        call.halfClose();
        // Requests nothing yet: the server fills every buffer on the way, then stops.
        Thread.sleep(1000);
        int stalled = produced.get();
        assertTrue(stalled < total / 2, "produced " + stalled + " of " + total + " messages");
        assertEquals(0, received.get());
        call.request(Integer.MAX_VALUE);
        assertEquals(Status.Code.OK, closed.get(30, TimeUnit.SECONDS).getCode());
        assertEquals(total, received.get());
    }

    /** Messages larger than the queue budget still stream through, in both directions. */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void messagesLargerThanTheQueueBudgetStreamThrough(boolean restlet) throws Exception {
        adapter.setMaxQueuedBytes(16384);
        start(restlet, streams());
        String large = "x".repeat(512 * 1024);
        assertEquals(
                large,
                ClientCalls.blockingUnaryCall(
                                adapter.getChannel(), ECHO, deadline(), StringValue.of(large))
                        .getValue());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void cancellingAStreamReachesTheServerAndFreesTheSlot(boolean restlet) throws Exception {
        adapter.setMaxConcurrentCalls(1);
        CountDownLatch receiving = new CountDownLatch(1);
        CountDownLatch cancelled = new CountDownLatch(1);
        start(
                restlet,
                ServerServiceDefinition.builder("review.Streams")
                        .addMethod(
                                CHAT,
                                ServerCalls.asyncBidiStreamingCall(
                                        out -> {
                                            ((ServerCallStreamObserver<StringValue>) out)
                                                    .setOnCancelHandler(cancelled::countDown);
                                            return new StreamObserver<StringValue>() {
                                                @Override
                                                public void onNext(StringValue value) {
                                                    receiving.countDown();
                                                }

                                                @Override
                                                public void onError(Throwable cause) {
                                                    // The call ended; nothing to answer.
                                                }

                                                @Override
                                                public void onCompleted() {
                                                    out.onCompleted();
                                                }
                                            };
                                        }))
                        .addMethod(
                                ECHO,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue request, StreamObserver<StringValue> out) -> {
                                            out.onNext(request);
                                            out.onCompleted();
                                        }))
                        .build());
        var closed = new CompletableFuture<Status>();
        ClientCall<StringValue, StringValue> call = adapter.getChannel().newCall(CHAT, deadline());
        call.start(
                new ClientCall.Listener<>() {
                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        closed.complete(status);
                    }
                },
                new Metadata());
        call.sendMessage(StringValue.of("open"));
        assertTrue(receiving.await(30, TimeUnit.SECONDS));
        call.cancel("client gave up", null);
        assertEquals(Status.Code.CANCELLED, closed.get(30, TimeUnit.SECONDS).getCode());
        assertTrue(cancelled.await(30, TimeUnit.SECONDS));
        assertEquals(
                "next",
                ClientCalls.blockingUnaryCall(
                                adapter.getChannel(), ECHO, deadline(), StringValue.of("next"))
                        .getValue());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void anOpenStreamEndsAtItsDeadline(boolean restlet) throws Exception {
        start(restlet, streams());
        var requests = new CompletableFuture<StreamObserver<StringValue>>();
        var error = new CompletableFuture<Throwable>();
        requests.complete(
                ClientCalls.asyncBidiStreamingCall(
                        adapter.getChannel()
                                .newCall(
                                        CHAT,
                                        CallOptions.DEFAULT.withDeadlineAfter(
                                                500, TimeUnit.MILLISECONDS)),
                        new StreamObserver<StringValue>() {
                            @Override
                            public void onNext(StringValue value) {
                                // Replies are not checked here.
                            }

                            @Override
                            public void onError(Throwable cause) {
                                error.complete(cause);
                            }

                            @Override
                            public void onCompleted() {
                                error.complete(null);
                            }
                        }));
        // Never half-closed: only the deadline ends the call.
        requests.get().onNext(StringValue.of("open"));
        Throwable cause = error.get(30, TimeUnit.SECONDS);
        assertTrue(cause instanceof StatusRuntimeException, String.valueOf(cause));
        assertEquals(
                Status.Code.DEADLINE_EXCEEDED,
                ((StatusRuntimeException) cause).getStatus().getCode());
    }

    /**
     * A server may answer before the client half-closes, while the request is still written and
     * held by flow control: the client gets the answer and its status, and the unfinished request
     * is reset, so the only call slot is free at once.
     */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void anEarlyAnswerEndsTheCallWhileTheRequestIsStillSent(boolean restlet) throws Exception {
        adapter.setMaxConcurrentCalls(1);
        start(
                restlet,
                ServerServiceDefinition.builder("review.Streams")
                        .addMethod(
                                UPLOAD,
                                ServerCalls.asyncClientStreamingCall(
                                        out ->
                                                new StreamObserver<StringValue>() {
                                                    private boolean answered;

                                                    @Override
                                                    public void onNext(StringValue value) {
                                                        if (!answered) {
                                                            answered = true;
                                                            out.onNext(StringValue.of("enough"));
                                                            out.onCompleted();
                                                        }
                                                    }

                                                    @Override
                                                    public void onError(Throwable cause) {
                                                        // The call ended; nothing to answer.
                                                    }

                                                    @Override
                                                    public void onCompleted() {
                                                        // Already answered.
                                                    }
                                                }))
                        .addMethod(
                                ECHO,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue request, StreamObserver<StringValue> out) -> {
                                            out.onNext(request);
                                            out.onCompleted();
                                        }))
                        .build());
        var reply = new CompletableFuture<String>();
        var closed = new CompletableFuture<Status>();
        AtomicInteger sent = new AtomicInteger();
        ClientCall<StringValue, StringValue> call =
                adapter.getChannel().newCall(UPLOAD, deadline());
        call.start(
                new ClientCall.Listener<>() {
                    @Override
                    public void onMessage(StringValue message) {
                        reply.complete(message.getValue());
                    }

                    @Override
                    public void onReady() {
                        // Never half-closes: keeps sending while ready, up to 4 MiB.
                        while (call.isReady() && sent.get() < 1024 && !closed.isDone()) {
                            call.sendMessage(StringValue.of(BLOCK));
                            sent.incrementAndGet();
                        }
                    }

                    @Override
                    public void onClose(Status status, Metadata trailers) {
                        closed.complete(status);
                    }
                },
                new Metadata());
        call.request(1);
        assertEquals(Status.Code.OK, closed.get(30, TimeUnit.SECONDS).getCode());
        assertEquals("enough", reply.get(30, TimeUnit.SECONDS));
        assertEquals(
                "next",
                ClientCalls.blockingUnaryCall(
                                adapter.getChannel(), ECHO, deadline(), StringValue.of("next"))
                        .getValue());
    }

    /**
     * With a direct callback executor, readiness may return while the tail of a large message is
     * still queued: a sender respecting it may start the next large message.
     */
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void largeMessagesSentOnReadinessAreAccepted(boolean restlet) throws Exception {
        adapter.setMaxQueuedBytes(16384);
        adapter.setExecutor(Runnable::run);
        start(restlet, streams());
        String large = "y".repeat(100 * 1024);
        var result = new CompletableFuture<String>();
        AtomicInteger sent = new AtomicInteger();
        ClientCall<StringValue, StringValue> call =
                adapter.getChannel().newCall(UPLOAD, deadline());
        call.start(
                new ClientCall.Listener<>() {
                    @Override
                    public void onMessage(StringValue message) {
                        result.complete(message.getValue());
                    }

                    @Override
                    public void onReady() {
                        while (call.isReady() && sent.get() < 2) {
                            call.sendMessage(StringValue.of(large));
                            if (sent.incrementAndGet() == 2) {
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
        assertEquals("2:" + (2 * large.length()), result.get(30, TimeUnit.SECONDS));
    }

    private static StreamObserver<StringValue> single(CompletableFuture<String> result) {
        return new StreamObserver<>() {
            @Override
            public void onNext(StringValue value) {
                result.complete(value.getValue());
            }

            @Override
            public void onError(Throwable cause) {
                result.completeExceptionally(cause);
            }

            @Override
            public void onCompleted() {
                // The single reply already completed the result.
            }
        };
    }
}
