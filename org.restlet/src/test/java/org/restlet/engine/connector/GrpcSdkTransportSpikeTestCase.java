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
import io.grpc.ChannelLogger;
import io.grpc.Deadline;
import io.grpc.InternalChannelz;
import io.grpc.InternalLogId;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.ServerCall;
import io.grpc.ServerMethodDefinition;
import io.grpc.ServerServiceDefinition;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.internal.ClientStream;
import io.grpc.internal.ClientStreamListener;
import io.grpc.internal.ClientTransportFactory;
import io.grpc.internal.ConnectionClientTransport;
import io.grpc.internal.InsightBuilder;
import io.grpc.internal.ManagedChannelImplBuilder;
import io.grpc.internal.StreamListener;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.protobuf.ProtoUtils;
import io.grpc.stub.ClientCalls;
import io.grpc.stub.ServerCalls;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.util.Series;

class GrpcSdkTransportSpikeTestCase {

    private static final int MAX_MESSAGE_BYTES = 1024 * 1024;

    private static final int MAX_STREAM_BYTES = 4 * MAX_MESSAGE_BYTES;

    private static final MediaType GRPC_MEDIA_TYPE = new MediaType("application/grpc");

    private static final MethodDescriptor<StringValue, StringValue> ECHO_METHOD =
            MethodDescriptor.<StringValue, StringValue>newBuilder()
                    .setType(MethodDescriptor.MethodType.UNARY)
                    .setFullMethodName("restlet.r0.Echo/Echo")
                    .setRequestMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .build();

    private static final MethodDescriptor<StringValue, StringValue> STREAM_METHOD =
            MethodDescriptor.<StringValue, StringValue>newBuilder()
                    .setType(MethodDescriptor.MethodType.SERVER_STREAMING)
                    .setFullMethodName("restlet.r0.Echo/Stream")
                    .setRequestMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .build();

    private static final MethodDescriptor<StringValue, StringValue> CLIENT_STREAM_METHOD =
            MethodDescriptor.<StringValue, StringValue>newBuilder()
                    .setType(MethodDescriptor.MethodType.CLIENT_STREAMING)
                    .setFullMethodName("restlet.r0.Echo/ClientStream")
                    .setRequestMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .setResponseMarshaller(ProtoUtils.marshaller(StringValue.getDefaultInstance()))
                    .build();

    private Client client;

    private io.grpc.Server grpcServer;

    private ManagedChannel grpcClientChannel;

    private Server restletServer;

    private ManagedChannel channel;

    private final AtomicReference<Protocol> lastGrpcResponseProtocol = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        Engine.clearThreadLocalVariables();
        Engine.register(false);
        Engine.getInstance().getRegisteredServers().add(new HttpServerHelper(null));
        Engine.getInstance().getRegisteredClients().add(new HttpClientHelper(null));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (channel != null) {
            channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
        if (client != null) {
            client.stop();
        }
        if (grpcClientChannel != null) {
            grpcClientChannel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
        if (restletServer != null) {
            restletServer.stop();
        }
        if (grpcServer != null) {
            grpcServer.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
        }
        Engine.clearThreadLocalVariables();
    }

    @Test
    void invokesOfficialGrpcServerThroughRestletHttp2Client() throws Exception {
        ServerServiceDefinition service =
                ServerServiceDefinition.builder("restlet.r0.Echo")
                        .addMethod(
                                ECHO_METHOD,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue message,
                                                io.grpc.stub.StreamObserver<StringValue>
                                                        result) -> {
                                            if ("bad".equals(message.getValue())) {
                                                result.onError(
                                                        Status.INVALID_ARGUMENT
                                                                .withDescription("spike-error")
                                                                .asRuntimeException());
                                            } else {
                                                result.onNext(
                                                        StringValue.of(
                                                                "echo:" + message.getValue()));
                                                result.onCompleted();
                                            }
                                        }))
                        .addMethod(
                                STREAM_METHOD,
                                ServerCalls.asyncServerStreamingCall(
                                        (StringValue message,
                                                io.grpc.stub.StreamObserver<StringValue>
                                                        result) -> {
                                            java.util.concurrent.CompletableFuture.runAsync(
                                                    () -> {
                                                        try {
                                                            Thread.sleep(50);
                                                            result.onNext(
                                                                    StringValue.of(
                                                                            message.getValue()
                                                                                    + ":1"));
                                                            Thread.sleep(50);
                                                            result.onNext(
                                                                    StringValue.of(
                                                                            message.getValue()
                                                                                    + ":2"));
                                                            result.onCompleted();
                                                        } catch (InterruptedException e) {
                                                            Thread.currentThread().interrupt();
                                                            result.onError(
                                                                    Status.CANCELLED
                                                                            .withCause(e)
                                                                            .asRuntimeException());
                                                        }
                                                    });
                                        }))
                        .addMethod(
                                CLIENT_STREAM_METHOD,
                                ServerCalls.asyncClientStreamingCall(
                                        result ->
                                                new io.grpc.stub.StreamObserver<>() {
                                                    private final List<String> values =
                                                            new ArrayList<>();

                                                    @Override
                                                    public void onNext(StringValue value) {
                                                        values.add(value.getValue());
                                                    }

                                                    @Override
                                                    public void onError(Throwable error) {
                                                        result.onError(error);
                                                    }

                                                    @Override
                                                    public void onCompleted() {
                                                        result.onNext(
                                                                StringValue.of(
                                                                        String.join(",", values)));
                                                        result.onCompleted();
                                                    }
                                                }))
                        .build();
        grpcServer = NettyServerBuilder.forPort(0).addService(service).build().start();

        Context restletContext = new Context();
        restletContext.getParameters().add("httpClientTransportMode", "HTTP2");
        client = new Client(restletContext, Protocol.HTTP);
        client.start();

        String target = "http://localhost:" + grpcServer.getPort();
        String authority = "localhost:" + grpcServer.getPort();
        RestletTransportFactory transportFactory =
                new RestletTransportFactory(client, lastGrpcResponseProtocol);
        channel =
                new ManagedChannelImplBuilder(
                                new RestletSocketAddress(target),
                                authority,
                                () -> transportFactory,
                                () -> 0)
                        .build();

        assertEquals(
                "echo:hello",
                ClientCalls.blockingUnaryCall(
                                channel, ECHO_METHOD, CallOptions.DEFAULT, StringValue.of("hello"))
                        .getValue());
        StatusRuntimeException failure =
                assertThrows(
                        StatusRuntimeException.class,
                        () ->
                                ClientCalls.blockingUnaryCall(
                                        channel,
                                        ECHO_METHOD,
                                        CallOptions.DEFAULT,
                                        StringValue.of("bad")));
        assertEquals(Status.Code.INVALID_ARGUMENT, failure.getStatus().getCode());
        assertEquals("2.0", lastGrpcResponseProtocol.get().getVersion());

        var stream =
                ClientCalls.blockingServerStreamingCall(
                        channel, STREAM_METHOD, CallOptions.DEFAULT, StringValue.of("delayed"));
        assertEquals(
                List.of("delayed:1", "delayed:2"),
                java.util.stream.StreamSupport.stream(
                                java.util.Spliterators.spliteratorUnknownSize(
                                        stream, java.util.Spliterator.ORDERED),
                                false)
                        .map(StringValue::getValue)
                        .toList());

        assertClientStreaming(channel);
    }

    @Test
    void hostsOfficialGrpcServiceThroughRestletHttp2Server() throws Exception {
        ServerServiceDefinition service =
                ServerServiceDefinition.builder("restlet.r0.Echo")
                        .addMethod(
                                ECHO_METHOD,
                                ServerCalls.asyncUnaryCall(
                                        (StringValue message,
                                                io.grpc.stub.StreamObserver<StringValue>
                                                        result) -> {
                                            if ("bad".equals(message.getValue())) {
                                                result.onError(
                                                        Status.INVALID_ARGUMENT
                                                                .withDescription("spike-error")
                                                                .asRuntimeException());
                                            } else {
                                                result.onNext(
                                                        StringValue.of(
                                                                "echo:" + message.getValue()));
                                                result.onCompleted();
                                            }
                                        }))
                        .addMethod(
                                STREAM_METHOD,
                                ServerCalls.asyncServerStreamingCall(
                                        (StringValue message,
                                                io.grpc.stub.StreamObserver<StringValue>
                                                        result) -> {
                                            result.onNext(
                                                    StringValue.of(message.getValue() + ":1"));
                                            result.onNext(
                                                    StringValue.of(message.getValue() + ":2"));
                                            result.onCompleted();
                                        }))
                        .addMethod(
                                CLIENT_STREAM_METHOD,
                                ServerCalls.asyncClientStreamingCall(
                                        result ->
                                                new io.grpc.stub.StreamObserver<>() {
                                                    private final List<String> values =
                                                            new ArrayList<>();

                                                    @Override
                                                    public void onNext(StringValue value) {
                                                        values.add(value.getValue());
                                                    }

                                                    @Override
                                                    public void onError(Throwable error) {
                                                        result.onError(error);
                                                    }

                                                    @Override
                                                    public void onCompleted() {
                                                        result.onNext(
                                                                StringValue.of(
                                                                        String.join(",", values)));
                                                        result.onCompleted();
                                                    }
                                                }))
                        .build();
        Context serverContext = new Context();
        serverContext.getParameters().add("http.transport.protocol", "HTTP2");
        RestletGrpcServer adapter = new RestletGrpcServer(service);
        restletServer = new Server(serverContext, Protocol.HTTP, 0, adapter);
        restletServer.start();
        grpcClientChannel =
                NettyChannelBuilder.forAddress("localhost", restletServer.getActualPort())
                        .usePlaintext()
                        .build();

        assertEquals(
                "echo:hello",
                ClientCalls.blockingUnaryCall(
                                grpcClientChannel,
                                ECHO_METHOD,
                                CallOptions.DEFAULT,
                                StringValue.of("hello"))
                        .getValue());
        assertEquals("HTTP/2.0", adapter.lastRequestProtocol.get());
        StatusRuntimeException failure =
                assertThrows(
                        StatusRuntimeException.class,
                        () ->
                                ClientCalls.blockingUnaryCall(
                                        grpcClientChannel,
                                        ECHO_METHOD,
                                        CallOptions.DEFAULT,
                                        StringValue.of("bad")));
        assertEquals(Status.Code.INVALID_ARGUMENT, failure.getStatus().getCode());
        var stream =
                ClientCalls.blockingServerStreamingCall(
                        grpcClientChannel,
                        STREAM_METHOD,
                        CallOptions.DEFAULT,
                        StringValue.of("stream"));
        assertEquals(
                List.of("stream:1", "stream:2"),
                java.util.stream.StreamSupport.stream(
                                java.util.Spliterators.spliteratorUnknownSize(
                                        stream, java.util.Spliterator.ORDERED),
                                false)
                        .map(StringValue::getValue)
                        .toList());

        assertClientStreaming(grpcClientChannel);
    }

    private static void assertClientStreaming(ManagedChannel channel) throws InterruptedException {
        CountDownLatch closed = new CountDownLatch(1);
        AtomicReference<StringValue> response = new AtomicReference<>();
        AtomicReference<Throwable> failure = new AtomicReference<>();
        var request =
                ClientCalls.asyncClientStreamingCall(
                        channel.newCall(CLIENT_STREAM_METHOD, CallOptions.DEFAULT),
                        new io.grpc.stub.StreamObserver<>() {
                            @Override
                            public void onNext(StringValue value) {
                                response.set(value);
                            }

                            @Override
                            public void onError(Throwable error) {
                                failure.set(error);
                                closed.countDown();
                            }

                            @Override
                            public void onCompleted() {
                                closed.countDown();
                            }
                        });
        request.onNext(StringValue.of("first"));
        request.onNext(StringValue.of("second"));
        request.onCompleted();
        assertTrue(closed.await(5, TimeUnit.SECONDS));
        if (failure.get() != null) {
            throw new AssertionError("gRPC client-streaming request failed", failure.get());
        }
        assertEquals("first,second", response.get().getValue());
    }

    private static final class RestletGrpcServer extends org.restlet.Restlet {

        private final ServerServiceDefinition service;

        private final java.util.concurrent.atomic.AtomicReference<String> lastRequestProtocol =
                new java.util.concurrent.atomic.AtomicReference<>();

        private RestletGrpcServer(ServerServiceDefinition service) {
            this.service = service;
        }

        @Override
        public void handle(Request request, Response response) {
            try {
                lastRequestProtocol.set(
                        (String) request.getAttributes().get(HeaderConstants.ATTRIBUTE_VERSION));
                if (!Method.POST.equals(request.getMethod())) {
                    response.setStatus(org.restlet.data.Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                    return;
                }
                String path = request.getResourceRef().getPath();
                String fullMethodName = path.startsWith("/") ? path.substring(1) : path;
                ServerMethodDefinition<?, ?> definition = service.getMethod(fullMethodName);
                if (definition == null) {
                    setGrpcStatus(response, Status.UNIMPLEMENTED);
                    return;
                }
                if (!request.isEntityAvailable()) {
                    setGrpcStatus(response, Status.INVALID_ARGUMENT);
                    return;
                }

                byte[] frames;
                try (InputStream input = request.getEntity().getStream()) {
                    frames = input.readNBytes(MAX_STREAM_BYTES + 1);
                }
                if (frames.length > MAX_STREAM_BYTES) {
                    throw new IOException(
                            "gRPC request stream exceeds " + MAX_STREAM_BYTES + " bytes");
                }
                dispatch(definition, request, response, unframeMessages(frames, MAX_MESSAGE_BYTES));
            } catch (Exception e) {
                setGrpcStatus(response, Status.fromThrowable(e));
            }
        }

        private <RequestT, ResponseT> void dispatch(
                ServerMethodDefinition<RequestT, ResponseT> definition,
                Request request,
                Response response,
                List<byte[]> payloads)
                throws IOException {
            MethodDescriptor<RequestT, ResponseT> method = definition.getMethodDescriptor();
            Metadata requestHeaders = readMetadata(request.getHeaders());
            SpikeServerCall<RequestT, ResponseT> serverCall = new SpikeServerCall<>(method);
            ServerCall.Listener<RequestT> listener =
                    definition.getServerCallHandler().startCall(serverCall, requestHeaders);
            if (listener == null) {
                throw new IOException("gRPC SDK returned no call listener");
            }
            for (byte[] payload : payloads) {
                listener.onMessage(method.parseRequest(new ByteArrayInputStream(payload)));
            }
            listener.onHalfClose();
            if (!serverCall.awaitClose()) {
                throw new IOException("Timed out waiting for SDK gRPC server call to close");
            }
            serverCall.writeResponse(response);
        }
    }

    private static final class SpikeServerCall<RequestT, ResponseT>
            extends ServerCall<RequestT, ResponseT> {

        private final MethodDescriptor<RequestT, ResponseT> method;

        private final List<ResponseT> responseMessages = new ArrayList<>();

        private final Metadata responseHeaders = new Metadata();

        private final CountDownLatch closed = new CountDownLatch(1);

        private Status status;

        private Metadata trailers = new Metadata();

        private SpikeServerCall(MethodDescriptor<RequestT, ResponseT> method) {
            this.method = method;
        }

        @Override
        public void request(int numMessages) {}

        @Override
        public void sendHeaders(Metadata headers) {
            responseHeaders.merge(headers);
        }

        @Override
        public void sendMessage(ResponseT message) {
            responseMessages.add(message);
        }

        @Override
        public void close(Status status, Metadata trailers) {
            this.status = status;
            this.trailers = trailers;
            closed.countDown();
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public MethodDescriptor<RequestT, ResponseT> getMethodDescriptor() {
            return method;
        }

        private boolean awaitClose() throws IOException {
            try {
                return closed.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for gRPC call completion", e);
            }
        }

        private void writeResponse(Response response) throws IOException {
            if (status == null) {
                status = Status.INTERNAL.withDescription("SDK call did not close");
                trailers = new Metadata();
            }
            response.setStatus(org.restlet.data.Status.SUCCESS_OK);
            writeMetadata(responseHeaders, response.getHeaders());
            if (!responseMessages.isEmpty() && status.isOk()) {
                ByteArrayOutputStream framedMessages = new ByteArrayOutputStream();
                for (ResponseT responseMessage : responseMessages) {
                    try (InputStream payload = method.streamResponse(responseMessage)) {
                        byte[] bytes = payload.readNBytes(MAX_MESSAGE_BYTES + 1);
                        if (bytes.length > MAX_MESSAGE_BYTES) {
                            throw new IOException(
                                    "gRPC response exceeds " + MAX_MESSAGE_BYTES + " bytes");
                        }
                        framedMessages.write(frame(bytes));
                        if (framedMessages.size() > MAX_STREAM_BYTES) {
                            throw new IOException(
                                    "gRPC response stream exceeds " + MAX_STREAM_BYTES + " bytes");
                        }
                    }
                }
                response.setEntity(
                        new ByteArrayRepresentation(framedMessages.toByteArray(), GRPC_MEDIA_TYPE));
            } else {
                response.setEntity(new ByteArrayRepresentation(new byte[0], GRPC_MEDIA_TYPE));
            }
            response.getTrailers().add("grpc-status", Integer.toString(status.getCode().value()));
            if (status.getDescription() != null) {
                response.getTrailers().add("grpc-message", status.getDescription());
            }
            writeMetadata(trailers, response.getTrailers());
        }
    }

    private static void setGrpcStatus(Response response, Status status) {
        response.setStatus(org.restlet.data.Status.SUCCESS_OK);
        response.setEntity(new ByteArrayRepresentation(new byte[0], GRPC_MEDIA_TYPE));
        response.getTrailers().add("grpc-status", Integer.toString(status.getCode().value()));
    }

    private static final class RestletSocketAddress extends SocketAddress {

        private static final long serialVersionUID = 1L;

        private final String target;

        private RestletSocketAddress(String target) {
            this.target = target;
        }
    }

    private static final class RestletTransportFactory implements ClientTransportFactory {

        private final Client client;

        private final AtomicReference<Protocol> lastResponseProtocol;

        private final ScheduledExecutorService scheduledExecutor =
                Executors.newSingleThreadScheduledExecutor(
                        runnable -> {
                            Thread thread = new Thread(runnable, "restlet-grpc-r0-timer");
                            thread.setDaemon(true);
                            return thread;
                        });

        private RestletTransportFactory(
                Client client, AtomicReference<Protocol> lastResponseProtocol) {
            this.client = client;
            this.lastResponseProtocol = lastResponseProtocol;
        }

        @Override
        public ConnectionClientTransport newClientTransport(
                SocketAddress address,
                ClientTransportOptions options,
                ChannelLogger channelLogger) {
            if (!(address instanceof RestletSocketAddress restletAddress)) {
                throw new IllegalArgumentException("Unsupported address: " + address);
            }
            return new RestletClientTransport(
                    client, restletAddress, options.getAuthority(), lastResponseProtocol);
        }

        @Override
        public ScheduledExecutorService getScheduledExecutorService() {
            return scheduledExecutor;
        }

        @Override
        public SwapChannelCredentialsResult swapChannelCredentials(
                io.grpc.ChannelCredentials channelCredentials) {
            throw new UnsupportedOperationException(
                    "R0 spike does not support channel credentials");
        }

        @Override
        public void close() {
            scheduledExecutor.shutdownNow();
        }

        @Override
        public Collection<Class<? extends SocketAddress>> getSupportedSocketAddressTypes() {
            return Set.of(RestletSocketAddress.class);
        }
    }

    private static final class RestletClientTransport implements ConnectionClientTransport {

        private final Client client;

        private final RestletSocketAddress address;

        private final String authority;

        private final AtomicReference<Protocol> lastResponseProtocol;

        private final InternalLogId logId =
                InternalLogId.allocate(RestletClientTransport.class, "Restlet HTTP/2");

        private final AtomicInteger activeStreams = new AtomicInteger();

        private final java.util.concurrent.atomic.AtomicBoolean terminated =
                new java.util.concurrent.atomic.AtomicBoolean();

        private volatile Listener listener;

        private volatile boolean shutdown;

        private RestletClientTransport(
                Client client,
                RestletSocketAddress address,
                String authority,
                AtomicReference<Protocol> lastResponseProtocol) {
            this.client = client;
            this.address = address;
            this.authority = authority;
            this.lastResponseProtocol = lastResponseProtocol;
        }

        @Override
        public Runnable start(Listener listener) {
            this.listener = listener;
            return listener::transportReady;
        }

        @Override
        public void shutdown(Status status) {
            if (!shutdown) {
                shutdown = true;
                Listener currentListener = listener;
                if (currentListener != null) {
                    currentListener.transportShutdown(status, null);
                    terminateIfIdle();
                }
            }
        }

        @Override
        public void shutdownNow(Status status) {
            shutdown(status);
        }

        @Override
        public ClientStream newStream(
                MethodDescriptor<?, ?> method,
                Metadata headers,
                CallOptions callOptions,
                io.grpc.ClientStreamTracer[] tracers) {
            if (shutdown) {
                throw new IllegalStateException("gRPC transport is shut down");
            }
            if (method.getType() != MethodDescriptor.MethodType.UNARY
                    && method.getType() != MethodDescriptor.MethodType.SERVER_STREAMING
                    && method.getType() != MethodDescriptor.MethodType.CLIENT_STREAMING) {
                throw new UnsupportedOperationException(
                        "R0 SPI probe does not support bidirectional streaming");
            }
            if (callOptions.getCredentials() != null || callOptions.getCompressor() != null) {
                throw new UnsupportedOperationException(
                        "R0 SPI probe does not support call credentials or compression");
            }
            return new RestletClientStream(
                    client,
                    address,
                    authority,
                    method,
                    headers,
                    lastResponseProtocol,
                    this::streamStarted,
                    this::streamFinished);
        }

        @Override
        public void ping(PingCallback callback, java.util.concurrent.Executor executor) {
            executor.execute(
                    () ->
                            callback.onFailure(
                                    Status.UNIMPLEMENTED.withDescription("PING unsupported")));
        }

        @Override
        public ListenableFuture<InternalChannelz.SocketStats> getStats() {
            InternalChannelz.TransportStats transportStats =
                    new InternalChannelz.TransportStats(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            return Futures.immediateFuture(
                    new InternalChannelz.SocketStats(transportStats, address, address, null, null));
        }

        @Override
        public InternalLogId getLogId() {
            return logId;
        }

        @Override
        public Attributes getAttributes() {
            return Attributes.EMPTY;
        }

        private void streamStarted() {
            if (activeStreams.getAndIncrement() == 0) {
                Listener currentListener = listener;
                if (currentListener != null) {
                    currentListener.transportInUse(true);
                }
            }
        }

        private void streamFinished() {
            if (activeStreams.decrementAndGet() == 0) {
                Listener currentListener = listener;
                if (currentListener != null) {
                    currentListener.transportInUse(false);
                }
                terminateIfIdle();
            }
        }

        private void terminateIfIdle() {
            Listener currentListener = listener;
            if (shutdown
                    && activeStreams.get() == 0
                    && currentListener != null
                    && terminated.compareAndSet(false, true)) {
                currentListener.transportTerminated();
            }
        }
    }

    private static final class RestletClientStream implements ClientStream {

        private final Client client;

        private final RestletSocketAddress address;

        private final String authority;

        private final MethodDescriptor<?, ?> method;

        private final Metadata requestMetadata;

        private final AtomicReference<Protocol> lastResponseProtocol;

        private final Runnable onStart;

        private final Runnable onClose;

        private volatile ClientStreamListener listener;

        private volatile Request activeRequest;

        private final List<byte[]> requestPayloads = new ArrayList<>();

        private int requestStreamBytes;

        private int maxInboundMessageSize = MAX_MESSAGE_BYTES;

        private int maxOutboundMessageSize = MAX_MESSAGE_BYTES;

        private volatile boolean closed;

        private RestletClientStream(
                Client client,
                RestletSocketAddress address,
                String authority,
                MethodDescriptor<?, ?> method,
                Metadata requestMetadata,
                AtomicReference<Protocol> lastResponseProtocol,
                Runnable onStart,
                Runnable onClose) {
            this.client = client;
            this.address = address;
            this.authority = authority;
            this.method = method;
            this.requestMetadata = requestMetadata;
            this.lastResponseProtocol = lastResponseProtocol;
            this.onStart = onStart;
            this.onClose = onClose;
        }

        @Override
        public void start(ClientStreamListener listener) {
            this.listener = listener;
            onStart.run();
            listener.onReady();
        }

        @Override
        public void request(int numMessages) {
            if (numMessages <= 0) {
                throw new IllegalArgumentException("numMessages must be positive");
            }
        }

        @Override
        public void cancel(Status reason) {
            Request request = activeRequest;
            if (request != null) {
                request.abort();
            }
            close(reason, new Metadata());
        }

        @Override
        public void writeMessage(InputStream message) {
            if (closed) {
                throw new IllegalStateException("gRPC stream is already closed");
            }
            if (method.getType() == MethodDescriptor.MethodType.UNARY
                    && !requestPayloads.isEmpty()) {
                throw new IllegalStateException("R0 unary probe accepts one request message");
            }
            byte[] payload;
            try (InputStream input = message) {
                payload = input.readNBytes(maxOutboundMessageSize + 1);
            } catch (IOException e) {
                close(Status.INTERNAL.withCause(e), new Metadata());
                return;
            }
            if (payload.length > maxOutboundMessageSize) {
                close(
                        Status.RESOURCE_EXHAUSTED.withDescription(
                                "gRPC request exceeds " + maxOutboundMessageSize + " bytes"),
                        new Metadata());
                return;
            }
            requestStreamBytes += payload.length + 5;
            if (requestStreamBytes > MAX_STREAM_BYTES) {
                close(
                        Status.RESOURCE_EXHAUSTED.withDescription(
                                "gRPC request stream exceeds " + MAX_STREAM_BYTES + " bytes"),
                        new Metadata());
                return;
            }
            requestPayloads.add(payload);
        }

        @Override
        public void halfClose() {
            if (closed) {
                return;
            }
            if (method.getType() == MethodDescriptor.MethodType.UNARY
                    && requestPayloads.size() != 1) {
                close(
                        Status.INTERNAL.withDescription("gRPC unary request message is missing"),
                        new Metadata());
                return;
            }
            try {
                ByteArrayOutputStream framedRequest = new ByteArrayOutputStream();
                for (byte[] payload : requestPayloads) {
                    framedRequest.write(frame(payload));
                }
                Request request =
                        new Request(
                                Method.POST,
                                address.target + "/" + method.getFullMethodName(),
                                new ByteArrayRepresentation(
                                        framedRequest.toByteArray(), GRPC_MEDIA_TYPE));
                activeRequest = request;
                request.getHeaders().add("te", "trailers");
                writeMetadata(requestMetadata, request.getHeaders());
                Response response = client.handle(request);
                lastResponseProtocol.set(response.getProtocol());
                byte[] responsePayload = null;
                if (response.getEntity() != null) {
                    InputStream responseStream = response.getEntity().getStream();
                    if (responseStream != null) {
                        try (InputStream input = responseStream) {
                            byte[] framedResponse = input.readNBytes(MAX_STREAM_BYTES + 1);
                            if (framedResponse.length > MAX_STREAM_BYTES) {
                                throw new IOException(
                                        "gRPC response stream exceeds "
                                                + MAX_STREAM_BYTES
                                                + " bytes");
                            }
                            responsePayload = framedResponse;
                        }
                    }
                }
                Metadata trailers = readMetadata(response.getTrailers());
                String statusText = response.getTrailers().getFirstValue("grpc-status", true);
                if (statusText == null) {
                    statusText = response.getHeaders().getFirstValue("grpc-status", true);
                }
                if (statusText == null) {
                    close(Status.UNKNOWN.withDescription("Missing grpc-status"), trailers);
                    return;
                }
                Status status;
                try {
                    status = Status.fromCodeValue(Integer.parseInt(statusText));
                } catch (NumberFormatException e) {
                    status = Status.UNKNOWN.withDescription("Invalid grpc-status: " + statusText);
                }
                String statusMessage = response.getTrailers().getFirstValue("grpc-message", true);
                if (statusMessage == null) {
                    statusMessage = response.getHeaders().getFirstValue("grpc-message", true);
                }
                if (statusMessage != null) {
                    status = status.withDescription(statusMessage);
                }
                if (!status.isOk()) {
                    close(status, trailers);
                    return;
                }
                if (responsePayload == null) {
                    throw new IOException("gRPC response did not contain a message");
                }
                listener.headersRead(readMetadata(response.getHeaders()));
                List<byte[]> messages = unframeMessages(responsePayload, maxInboundMessageSize);
                listener.messagesAvailable(
                        new StreamListener.MessageProducer() {
                            private int next;

                            @Override
                            public InputStream next() {
                                if (next == messages.size()) {
                                    return null;
                                }
                                return new ByteArrayInputStream(messages.get(next++));
                            }
                        });
                close(status, ClientStreamListener.RpcProgress.PROCESSED, trailers);
            } catch (Exception e) {
                close(
                        Status.UNAVAILABLE.withCause(e),
                        ClientStreamListener.RpcProgress.PROCESSED,
                        new Metadata());
            }
        }

        private synchronized void close(Status status, Metadata trailers) {
            close(status, ClientStreamListener.RpcProgress.PROCESSED, trailers);
        }

        private synchronized void close(
                Status status, ClientStreamListener.RpcProgress progress, Metadata trailers) {
            if (!closed && listener != null) {
                closed = true;
                try {
                    listener.closed(status, progress, trailers);
                } finally {
                    onClose.run();
                }
            }
        }

        @Override
        public void flush() {}

        @Override
        public boolean isReady() {
            return !closed;
        }

        @Override
        public void optimizeForDirectExecutor() {}

        @Override
        public void setCompressor(io.grpc.Compressor compressor) {
            if (!"identity".equalsIgnoreCase(compressor.getMessageEncoding())) {
                throw new UnsupportedOperationException(
                        "R0 SPI probe does not support compression");
            }
        }

        @Override
        public void setMessageCompression(boolean enabled) {
            if (enabled) {
                throw new UnsupportedOperationException(
                        "R0 SPI probe does not support compression");
            }
        }

        @Override
        public void setAuthority(String authority) {
            if (authority != null && !authority.equals(this.authority)) {
                throw new UnsupportedOperationException(
                        "R0 SPI probe does not support authority overrides");
            }
        }

        @Override
        public void setFullStreamDecompression(boolean enabled) {
            if (enabled) {
                throw new UnsupportedOperationException(
                        "R0 SPI probe does not support decompression");
            }
        }

        @Override
        public void setDecompressorRegistry(io.grpc.DecompressorRegistry registry) {}

        @Override
        public void setMaxInboundMessageSize(int maxSize) {
            maxInboundMessageSize = Math.min(maxSize, MAX_MESSAGE_BYTES);
        }

        @Override
        public void setMaxOutboundMessageSize(int maxSize) {
            maxOutboundMessageSize = Math.min(maxSize, MAX_MESSAGE_BYTES);
        }

        @Override
        public void setDeadline(Deadline deadline) {
            if (deadline != null) {
                throw new UnsupportedOperationException("R0 SPI probe does not support deadlines");
            }
        }

        @Override
        public Attributes getAttributes() {
            return Attributes.EMPTY;
        }

        @Override
        public void appendTimeoutInsight(InsightBuilder insight) {}
    }

    private static byte[] frame(byte[] payload) {
        ByteBuffer buffer = ByteBuffer.allocate(payload.length + 5).order(ByteOrder.BIG_ENDIAN);
        buffer.put((byte) 0);
        buffer.putInt(payload.length);
        buffer.put(payload);
        return buffer.array();
    }

    private static byte[] unframe(byte[] frame) throws IOException {
        return unframe(frame, MAX_MESSAGE_BYTES);
    }

    private static byte[] unframe(byte[] frame, int maxMessageSize) throws IOException {
        if (frame.length < 5 || frame[0] != 0) {
            throw new IOException("Invalid or compressed gRPC message frame");
        }
        int length = ByteBuffer.wrap(frame, 1, 4).order(ByteOrder.BIG_ENDIAN).getInt();
        if (length < 0 || length > maxMessageSize || frame.length != length + 5) {
            throw new IOException("Invalid gRPC message length: " + length);
        }
        return java.util.Arrays.copyOfRange(frame, 5, frame.length);
    }

    private static List<byte[]> unframeMessages(byte[] bytes, int maxMessageSize)
            throws IOException {
        List<byte[]> messages = new ArrayList<>();
        int offset = 0;
        while (offset < bytes.length) {
            if (bytes.length - offset < 5 || bytes[offset] != 0) {
                throw new IOException("Invalid or compressed gRPC message frame");
            }
            int length = ByteBuffer.wrap(bytes, offset + 1, 4).order(ByteOrder.BIG_ENDIAN).getInt();
            if (length < 0 || length > maxMessageSize || bytes.length - offset < length + 5) {
                throw new IOException("Invalid gRPC message length: " + length);
            }
            messages.add(java.util.Arrays.copyOfRange(bytes, offset + 5, offset + length + 5));
            offset += length + 5;
        }
        return messages;
    }

    private static void writeMetadata(Metadata metadata, Series<Header> headers) {
        for (String name : metadata.keys()) {
            if (name.startsWith("grpc-")) {
                continue;
            }
            if (name.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                Metadata.Key<byte[]> key = Metadata.Key.of(name, Metadata.BINARY_BYTE_MARSHALLER);
                Iterable<byte[]> values = metadata.getAll(key);
                if (values != null) {
                    for (byte[] value : values) {
                        headers.add(name, Base64.getEncoder().encodeToString(value));
                    }
                }
            } else {
                Metadata.Key<String> key = Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
                Iterable<String> values = metadata.getAll(key);
                if (values != null) {
                    for (String value : values) {
                        headers.add(name, value);
                    }
                }
            }
        }
    }

    private static Metadata readMetadata(Series<Header> headers) {
        Metadata metadata = new Metadata();
        Set<String> excluded =
                Set.of(
                        "content-type",
                        "content-length",
                        "te",
                        "date",
                        "server",
                        "grpc-status",
                        "grpc-message",
                        "grpc-encoding",
                        "grpc-accept-encoding");
        for (Header header : headers) {
            String name = header.getName().toLowerCase(java.util.Locale.ROOT);
            if (excluded.contains(name) || name.startsWith("grpc-")) {
                continue;
            }
            if (name.endsWith(Metadata.BINARY_HEADER_SUFFIX)) {
                Metadata.Key<byte[]> key = Metadata.Key.of(name, Metadata.BINARY_BYTE_MARSHALLER);
                metadata.put(
                        key,
                        Base64.getDecoder()
                                .decode(header.getValue().getBytes(StandardCharsets.US_ASCII)));
            } else {
                Metadata.Key<String> key = Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER);
                metadata.put(key, header.getValue());
            }
        }
        return metadata;
    }
}
