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
import io.grpc.ForwardingServerCallListener;
import io.grpc.Grpc;
import io.grpc.InternalChannelz;
import io.grpc.InternalInstrumented;
import io.grpc.InternalLogId;
import io.grpc.InternalMetadata;
import io.grpc.InternalStatus;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerStreamTracer;
import io.grpc.Status;
import io.grpc.internal.AbstractServerStream;
import io.grpc.internal.InternalServer;
import io.grpc.internal.ReadableBuffers;
import io.grpc.internal.SerializingExecutor;
import io.grpc.internal.ServerListener;
import io.grpc.internal.ServerTransport;
import io.grpc.internal.ServerTransportListener;
import io.grpc.internal.StatsTraceContext;
import io.grpc.internal.TransportFrameUtil;
import io.grpc.internal.TransportTracer;
import io.grpc.internal.WritableBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.OutputRepresentation;
import org.restlet.util.Series;

/** Pinned SDK socketless server transport; each transport represents one logical HTTP exchange. */
@SuppressWarnings("null")
public final class RestletServerHost implements InternalServer, AutoCloseable {
    /** How long a cancelled call may stay blocked in one network read or write before a reset. */
    private static final Duration STUCK_IO = Duration.ofMillis(200);

    /** The logical call of an SDK stream, among its attributes. */
    private static final Attributes.Key<Call> CALL = Attributes.Key.create("restlet-grpc-call");

    /** Metadata that would break the gRPC framing if mapped onto the HTTP response. */
    private static final Set<String> FRAMING_HEADERS =
            Set.of(
                    "content-type",
                    "content-length",
                    "content-encoding",
                    "transfer-encoding",
                    "connection");

    private final int maxBytes;
    private final int queueBytes;
    private final Duration duration;
    private final Semaphore admission;

    /**
     * Calls whose callbacks may still run. Cancelled or completed calls leave admission at once but
     * keep one of these until their callbacks exit, up to as many again as admitted calls.
     */
    private final Semaphore callbacks;

    private final Set<Call> calls = ConcurrentHashMap.newKeySet();
    private final ScheduledThreadPoolExecutor timer =
            new ScheduledThreadPoolExecutor(
                    1, Thread.ofPlatform().daemon().name("restlet-grpc-server-timer").factory());

    /**
     * Runs the scheduled work of the SDK, such as client deadlines whose cancellation listeners may
     * block, apart from the call budgets of {@link #timer}.
     */
    private final ScheduledThreadPoolExecutor sdkTimer =
            new ScheduledThreadPoolExecutor(
                    1, Thread.ofPlatform().daemon().name("restlet-grpc-sdk-timer").factory());

    private volatile ServerListener listener;
    private List<? extends ServerStreamTracer.Factory> tracers = List.of();
    private boolean closed;

    public RestletServerHost(int maxBytes, int queueBytes, int maxCalls, Duration duration) {
        this.maxBytes = maxBytes;
        this.queueBytes = queueBytes;
        this.duration = duration;
        admission = new Semaphore(maxCalls);
        callbacks = new Semaphore(maxCalls > Integer.MAX_VALUE / 2 ? maxCalls : maxCalls * 2);
        // Each completed call cancels its deadline, which must not stay queued until it expires.
        timer.setRemoveOnCancelPolicy(true);
        sdkTimer.setRemoveOnCancelPolicy(true);
    }

    public void setTracers(List<? extends ServerStreamTracer.Factory> value) {
        tracers = List.copyOf(value);
    }

    @Override
    public synchronized void start(ServerListener value) {
        listener = value;
    }

    @Override
    public synchronized void shutdown() {
        if (!closed) {
            closed = true;
            for (Call call : List.copyOf(calls)) {
                call.cancel(Status.UNAVAILABLE.withDescription("Server shutdown"));
            }
            if (listener != null) {
                listener.serverShutdown();
            }
        }
    }

    @Override
    public void close() {
        shutdown();
        calls.forEach(call -> call.cancel(Status.UNAVAILABLE.withDescription("Adapter stopped")));
        timer.shutdownNow();
        sdkTimer.shutdownNow();
    }

    @Override
    public SocketAddress getListenSocketAddress() {
        return new InetSocketAddress("localhost", 0);
    }

    @Override
    public List<? extends SocketAddress> getListenSocketAddresses() {
        return List.of();
    }

    @Override
    public InternalInstrumented<InternalChannelz.SocketStats> getListenSocketStats() {
        return null;
    }

    @Override
    public List<InternalInstrumented<InternalChannelz.SocketStats>> getListenSocketStatsList() {
        return List.of();
    }

    public void handle(Request request, Response response) {
        if (!Method.POST.equals(request.getMethod())) {
            response.setStatus(org.restlet.data.Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            return;
        }
        if (!isGrpc(request.getEntity() == null ? null : request.getEntity().getMediaType())) {
            response.setStatus(org.restlet.data.Status.CLIENT_ERROR_UNSUPPORTED_MEDIA_TYPE);
            return;
        }
        String method = method(request);
        if (method == null) {
            response.setStatus(org.restlet.data.Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }
        // Over HTTP/1.1, Netty decides on trailers before the entity can add grpc-status.
        Protocol protocol = request.getProtocol();
        if (protocol == null || !"2.0".equals(protocol.getVersion())) {
            response.setStatus(org.restlet.data.Status.SERVER_ERROR_VERSION_NOT_SUPPORTED);
            return;
        }
        Call call;
        synchronized (this) {
            if (closed) {
                response.setStatus(org.restlet.data.Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
                return;
            }
            if (!admission.tryAcquire()) {
                response.setStatus(org.restlet.data.Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
                return;
            }
            if (!callbacks.tryAcquire()) {
                admission.release();
                response.setStatus(org.restlet.data.Status.SERVER_ERROR_SERVICE_UNAVAILABLE);
                return;
            }
            call = new Call(request, response, method);
            calls.add(call);
            call.transportListener = listener.transportCreated(call);
        }
        try {
            call.begin();
        } catch (IOException | RuntimeException e) {
            call.cancel(
                    Status.INVALID_ARGUMENT.withDescription("Invalid gRPC request").withCause(e));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            call.cancel(Status.CANCELLED.withCause(e));
        }
    }

    /**
     * Returns the interceptor holding a call's callback slot until the SDK reports its end to the
     * application, after its earlier callbacks exited. It must be the outermost interceptor.
     */
    public ServerInterceptor callbackBudget() {
        return new ServerInterceptor() {
            @Override
            public <Q, S> ServerCall.Listener<Q> interceptCall(
                    ServerCall<Q, S> call, Metadata headers, ServerCallHandler<Q, S> next) {
                Call owner = call.getAttributes().get(CALL);
                if (owner == null) {
                    return next.startCall(call, headers);
                }
                if (!owner.callbacksStarted()) {
                    // The call ended and released its slot before its callbacks could start.
                    call.close(
                            Status.CANCELLED.withDescription("gRPC call ended before it started"),
                            new Metadata());
                    return new ServerCall.Listener<>() {};
                }
                ServerCall.Listener<Q> listener;
                try {
                    listener = next.startCall(call, headers);
                } catch (RuntimeException | Error e) {
                    owner.callbacksEnded();
                    throw e;
                }
                return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(
                        listener) {
                    @Override
                    public void onComplete() {
                        try {
                            super.onComplete();
                        } finally {
                            owner.callbacksEnded();
                        }
                    }

                    @Override
                    public void onCancel() {
                        try {
                            super.onCancel();
                        } finally {
                            owner.callbacksEnded();
                        }
                    }
                };
            }
        };
    }

    /** Accepts application/grpc and application/grpc+*, whatever their parameters. */
    private static boolean isGrpc(MediaType type) {
        return type != null
                && "application".equalsIgnoreCase(type.getMainType())
                && type.getSubType() != null
                && (type.getSubType().equalsIgnoreCase("grpc")
                        || type.getSubType()
                                .toLowerCase(java.util.Locale.ROOT)
                                .startsWith("grpc+"));
    }

    /**
     * Returns the gRPC method of the raw path that the router matched, or null unless it is exactly
     * "/service/method" without dot segments: a path normalized differently than routed could
     * otherwise reach a method outside its route.
     */
    private static String method(Request request) {
        String path = request.getResourceRef() == null ? null : request.getResourceRef().getPath();
        if (path == null || !path.startsWith("/")) {
            return null;
        }
        String[] segments = path.substring(1).split("/", -1);
        if (segments.length != 2) {
            return null;
        }
        for (String segment : segments) {
            if (segment.isEmpty() || ".".equals(segment) || "..".equals(segment)) {
                return null;
            }
        }
        return path.substring(1);
    }

    private final class Call implements ServerTransport {
        private final Request request;
        private final Response response;
        private final String method;

        /**
         * Runs SDK notifications, and application callbacks with a direct executor, in virtual
         * threads: they never block the HTTP dispatch, nor its cancellation.
         */
        private final SerializingExecutor serial =
                new SerializingExecutor(
                        task -> Thread.ofVirtual().name("restlet-grpc-server-call").start(task));

        private final InternalLogId id = InternalLogId.allocate(Call.class, "Restlet logical call");
        private final ArrayDeque<byte[]> queue = new ArrayDeque<>();

        /** Guards {@link #unread}, and wakes the request reader. */
        private final Object inbound = new Object();

        private final CountDownLatch head = new CountDownLatch(1);
        private final AtomicBoolean finished = new AtomicBoolean();
        private final AtomicLong operations = new AtomicLong();

        /** 0 before the application callbacks, 1 while they may run, 2 once released. */
        private final AtomicInteger callbackState = new AtomicInteger();

        /**
         * The network read in progress, 0 if none. Reads and writes run concurrently, so each has
         * its own marker: the end of one must not hide the other from {@link #abortIfStuck}.
         */
        private final AtomicLong reading = new AtomicLong();

        /** The network write in progress, 0 if none. */
        private final AtomicLong writing = new AtomicLong();

        private ServerTransportListener transportListener;
        private SdkStream stream;
        private volatile ScheduledFuture<?> deadline;
        private Metadata trailers;
        private int queued;

        /** Request bytes handed to the SDK but not consumed yet; guarded by {@link #inbound}. */
        private long unread;

        /** Bytes framed for the latest message, from the frame that started it. */
        private int open;

        private boolean terminal;

        /** Whether the end of the call was reported to the SDK; confined to {@link #serial}. */
        private boolean reported;

        private Call(Request request, Response response, String method) {
            this.request = request;
            this.response = response;
            this.method = method;
        }

        private void begin() throws IOException, InterruptedException {
            // Installed first, so that a call failing below still sends its grpc-status.
            response.setEntity(responseEntity());
            Metadata headers = fromHttp(request.getHeaders());
            // Runs on the connector's I/O thread as soon as the terminal write is done.
            response.getDeliverySignal()
                    .thenAccept(
                            failure -> {
                                // An undelivered response leaves the client without the status.
                                if (failure != null) {
                                    cancel(
                                            Status.CANCELLED
                                                    .withDescription("gRPC response not delivered")
                                                    .withCause(failure));
                                } else if (claim()) {
                                    serial.execute(() -> finish(null));
                                }
                            });
            // Bounded before the SDK sees the stream, which may run application code.
            synchronized (RestletServerHost.this) {
                if (!closed) {
                    deadline =
                            timer.schedule(
                                    () ->
                                            cancel(
                                                    Status.DEADLINE_EXCEEDED.withDescription(
                                                            "Call duration exceeded")),
                                    duration.toNanos(),
                                    TimeUnit.NANOSECONDS);
                    if (finished.get()) {
                        deadline.cancel(false);
                    }
                }
            }
            request.getDisconnectSignal()
                    .thenAccept(cause -> cancel(Status.CANCELLED.withCause(cause)));
            serial.execute(
                    () -> {
                        if (reported) {
                            return;
                        }
                        Attributes attributes =
                                transportListener.transportReady(transportAttributes()).toBuilder()
                                        .set(CALL, this)
                                        .build();
                        stream =
                                new SdkStream(
                                        this,
                                        attributes,
                                        request.getHostRef() == null
                                                ? request.getResourceRef().getAuthority()
                                                : request.getHostRef().getAuthority(),
                                        StatsTraceContext.newServerContext(
                                                tracers, method, headers));
                        transportListener.streamCreated(stream, method, headers);
                        stream.state.onStreamAllocated();
                    });
            InputStream input = request.getEntity().getStream();
            if (input == null) {
                deliver(new byte[0], true);
            } else {
                // Read apart, so that the response can start while the client still sends.
                Thread.ofVirtual().name("restlet-grpc-server-read").start(() -> readAll(input));
            }
            if (!head.await(duration.toNanos(), TimeUnit.NANOSECONDS)) {
                cancel(Status.DEADLINE_EXCEEDED.withDescription("Response headers timed out"));
            }
        }

        /** Returns the standard transport attributes: the peer and local socket addresses. */
        private Attributes transportAttributes() {
            Attributes.Builder result = Attributes.newBuilder();
            SocketAddress remote =
                    address(
                            request.getClientInfo().getAddress(),
                            request.getClientInfo().getPort());
            if (remote != null) {
                result.set(Grpc.TRANSPORT_ATTR_REMOTE_ADDR, remote);
            }
            SocketAddress local =
                    address(
                            response.getServerInfo().getAddress(),
                            response.getServerInfo().getPort());
            if (local != null) {
                result.set(Grpc.TRANSPORT_ATTR_LOCAL_ADDR, local);
            }
            return result.build();
        }

        /**
         * Hands the request body to the SDK as it arrives. Bytes handed over but not yet consumed
         * by the SDK's deframer, which only reads the messages the application requested, stay
         * within the queue budget: a slow consumer stops the reads, and so the client.
         */
        private void readAll(InputStream input) {
            try (input) {
                byte[] chunk = new byte[16384];
                for (; ; ) {
                    int room;
                    synchronized (inbound) {
                        while (unread >= queueBytes && !finished.get()) {
                            inbound.wait();
                        }
                        if (finished.get()) {
                            return;
                        }
                        room = (int) Math.min(chunk.length, queueBytes - unread);
                    }
                    int count;
                    enterIo(reading);
                    try {
                        count = input.read(chunk, 0, room);
                    } finally {
                        reading.set(0);
                    }
                    if (count < 0) {
                        deliver(new byte[0], true);
                        return;
                    }
                    if (count > 0) {
                        synchronized (inbound) {
                            unread += count;
                        }
                        deliver(java.util.Arrays.copyOf(chunk, count), false);
                    }
                }
            } catch (InterruptedException _) {
                Thread.currentThread().interrupt();
                cancel(Status.CANCELLED.withDescription("gRPC request read interrupted"));
            } catch (IOException | RuntimeException e) {
                synchronized (queue) {
                    if (terminal) {
                        // The response ended, so the server closed the unread request body; the
                        // delivery or disconnect signal settles the call, not this read.
                        return;
                    }
                }
                cancel(Status.CANCELLED.withDescription("gRPC request read failed").withCause(e));
            }
        }

        /** Hands request bytes to the SDK, in {@link #serial} after the stream's creation. */
        private void deliver(byte[] bytes, boolean end) {
            serial.execute(
                    () -> {
                        if (!reported) {
                            stream.state.inboundDataReceived(ReadableBuffers.wrap(bytes), end);
                        }
                    });
        }

        /** Frees inbound budget once the SDK's deframer consumed request bytes. */
        private void consumed(int count) {
            synchronized (inbound) {
                unread -= count;
                inbound.notifyAll();
            }
        }

        private OutputRepresentation responseEntity() {
            return new OutputRepresentation(MediaType.valueOf("application/grpc")) {
                @Override
                public void write(OutputStream output) throws IOException {
                    try {
                        for (; ; ) {
                            byte[] data;
                            synchronized (queue) {
                                while (queue.isEmpty() && !terminal) {
                                    queue.wait();
                                }
                                data = queue.pollFirst();
                                if (data == null) {
                                    toHttp(trailers, response.getTrailers());
                                    return;
                                }
                                queued -= data.length;
                            }
                            enterIo(writing);
                            try {
                                output.write(data);
                                output.flush();
                            } finally {
                                writing.set(0);
                            }
                            int sent = data.length;
                            serial.execute(() -> stream.state.onSentBytes(sent));
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        cancel(Status.CANCELLED.withCause(e));
                        throw new IOException("Interrupted writing gRPC response", e);
                    } catch (IOException e) {
                        cancel(Status.CANCELLED.withCause(e));
                        throw e;
                    }
                }
            };
        }

        /** Marks the start of a network read or write, which only the peer may otherwise end. */
        private void enterIo(AtomicLong marker) {
            marker.set(operations.incrementAndGet());
            if (finished.get()) {
                abortIfStuck();
            }
        }

        /**
         * Resets the HTTP stream if the call stays in the same network read or write after it was
         * ended, so a peer that stops sending or reading can't hold the handler. A call blocked
         * elsewhere was already woken by the end of the call.
         */
        private void abortIfStuck() {
            long read = reading.get();
            long write = writing.get();
            if (read == 0 && write == 0) {
                return;
            }
            Thread.startVirtualThread(
                    () -> {
                        try {
                            Thread.sleep(STUCK_IO);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                        if ((read != 0 && reading.get() == read)
                                || (write != 0 && writing.get() == write)) {
                            request.abort();
                        }
                    });
        }

        private void end(Metadata metadata) {
            synchronized (queue) {
                if (!terminal) {
                    terminal = true;
                    trailers = metadata;
                    head.countDown();
                    queue.notifyAll();
                }
            }
        }

        private void cancel(Status status) {
            if (!claim()) {
                return;
            }
            abortIfStuck();
            // The response ends at once; only the SDK notification waits for earlier callbacks.
            synchronized (queue) {
                queue.clear();
                queued = 0;
                open = 0;
                terminal = true;
                trailers = new Metadata();
                trailers.put(InternalStatus.CODE_KEY, status);
                if (status.getDescription() != null) {
                    trailers.put(InternalStatus.MESSAGE_KEY, status.getDescription());
                }
                head.countDown();
                queue.notifyAll();
            }
            serial.execute(() -> finish(status));
        }

        /**
         * Claims the end of the call and frees its slot at once, in the thread that ends it.
         * Another thread may be draining {@link #serial}, so a slot released by {@link #finish}
         * could still be held when the client sends its next call.
         *
         * @return True if this is the first claim.
         */
        private boolean claim() {
            if (!finished.compareAndSet(false, true)) {
                return false;
            }
            calls.remove(this);
            admission.release();
            // Callbacks not started by now never will, so their slot is free at once.
            if (callbackState.compareAndSet(0, 2)) {
                callbacks.release();
            }
            ScheduledFuture<?> timeout = deadline;
            if (timeout != null) {
                timeout.cancel(false);
            }
            synchronized (inbound) {
                inbound.notifyAll();
            }
            return true;
        }

        /** Marks the start of the application callbacks; false if the call already ended. */
        private boolean callbacksStarted() {
            return callbackState.compareAndSet(0, 1);
        }

        /** Releases the callback slot once, after the application callbacks exited. */
        private void callbacksEnded() {
            if (callbackState.getAndSet(2) != 2) {
                callbacks.release();
            }
        }

        /** Reports the end of a claimed call to the SDK, in {@link #serial}. */
        private void finish(Status failure) {
            reported = true;
            if (deadline != null) {
                deadline.cancel(false);
            }
            try {
                if (stream != null) {
                    if (failure == null) {
                        stream.state.complete();
                    } else {
                        stream.state.transportReportStatus(failure);
                    }
                }
            } finally {
                transportListener.transportTerminated();
            }
        }

        @Override
        public void shutdown() {}

        @Override
        public void shutdownNow(Status reason) {
            cancel(reason);
        }

        @Override
        public ScheduledExecutorService getScheduledExecutorService() {
            return sdkTimer;
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

    private final class SdkStream extends AbstractServerStream {
        private final Call call;
        private final Attributes attributes;
        private final String authority;
        private final State state;

        private SdkStream(
                Call call, Attributes attributes, String authority, StatsTraceContext stats) {
            super(hint -> new Buffer(Math.max(1, Math.min(hint, 16384))), stats);
            this.call = call;
            this.attributes = attributes;
            this.authority = authority;
            state = new State(stats);
            framer().setMaxOutboundMessageSize(maxBytes);
            setOnReadyThreshold(Math.max(1, queueBytes / 2));
        }

        @Override
        protected State transportState() {
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
                public void writeHeaders(Metadata headers, boolean flush) {
                    synchronized (call.queue) {
                        if (!call.terminal) {
                            toResponse(headers, call.response);
                            call.head.countDown();
                        }
                    }
                }

                @Override
                public void writeFrame(WritableBuffer frame, boolean flush, int messages) {
                    if (frame == null) {
                        return;
                    }
                    var buffer = (Buffer) frame;
                    boolean overflow;
                    synchronized (call.queue) {
                        if (call.terminal) {
                            return;
                        }
                        // One message is framed whole, possibly before the writer starts: while the
                        // queue holds only that message, it may grow up to one framed message. A
                        // frame counting messages starts a new one.
                        int open = messages > 0 ? buffer.size : call.open + buffer.size;
                        boolean others = call.queued + buffer.size > open;
                        overflow =
                                buffer.size > queueBytes - call.queued
                                        && (others || open > maxBytes + 5);
                        if (!overflow) {
                            call.open = open;
                            byte[] bytes = java.util.Arrays.copyOf(buffer.bytes, buffer.size);
                            onSendingBytes(bytes.length);
                            call.queue.addLast(bytes);
                            call.queued += bytes.length;
                            call.queue.notifyAll();
                        }
                    }
                    if (overflow) {
                        call.cancel(
                                Status.RESOURCE_EXHAUSTED.withDescription(
                                        "Response queue byte budget exceeded"));
                    }
                }

                @Override
                public void writeTrailers(Metadata trailers, boolean headersSent, Status status) {
                    call.end(trailers);
                }

                @Override
                public void cancel(Status status) {
                    call.cancel(status);
                }
            };
        }

        private final class State extends AbstractServerStream.TransportState {
            private State(StatsTraceContext stats) {
                super(maxBytes, stats, new TransportTracer());
            }

            @Override
            public void runOnTransportThread(Runnable task) {
                call.serial.execute(task);
            }

            @Override
            public void bytesRead(int count) {
                call.consumed(count);
            }

            @Override
            public void deframeFailed(Throwable cause) {
                call.cancel(Status.fromThrowable(cause));
            }
        }
    }

    private static final class Buffer implements WritableBuffer {
        private final byte[] bytes;
        private int size;

        private Buffer(int capacity) {
            bytes = new byte[capacity];
        }

        @Override
        public void write(byte value) {
            bytes[size++] = value;
        }

        @Override
        public void write(byte[] source, int offset, int count) {
            System.arraycopy(source, offset, bytes, size, count);
            size += count;
        }

        @Override
        public int writableBytes() {
            return bytes.length - size;
        }

        @Override
        public int readableBytes() {
            return size;
        }

        @Override
        public void release() {}
    }

    /** Returns the socket address of an IP literal and port, or null if unknown. */
    private static SocketAddress address(String host, int port) {
        if (host == null || port < 0) {
            return null;
        }
        try {
            return new InetSocketAddress(InetAddress.ofLiteral(host), port);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static Metadata fromHttp(Series<Header> headers) {
        var values = new ArrayList<byte[]>();
        for (Header header : headers) {
            values.add(
                    header.getName()
                            .toLowerCase(java.util.Locale.ROOT)
                            .getBytes(StandardCharsets.US_ASCII));
            values.add(header.getValue().getBytes(StandardCharsets.US_ASCII));
        }
        return InternalMetadata.newMetadata(
                TransportFrameUtil.toRawSerializedHeaders(values.toArray(byte[][]::new)));
    }

    /**
     * Maps initial metadata onto the response as raw headers, written as-is whether or not their
     * names are standard HTTP ones, with their repeated values.
     */
    private static void toResponse(Metadata metadata, Response response) {
        var raw = new Series<Header>(Header.class);
        toHttp(metadata, raw);
        raw.removeIf(header -> FRAMING_HEADERS.contains(header.getName()));
        if (!raw.isEmpty()) {
            response.getAttributes().put(HeaderConstants.ATTRIBUTE_RAW_RESPONSE_HEADERS, raw);
        }
    }

    private static void toHttp(Metadata metadata, Series<Header> headers) {
        if (metadata == null) {
            return;
        }
        byte[][] values = TransportFrameUtil.toHttp2Headers(metadata);
        for (int i = 0; i < values.length; i += 2) {
            String name = new String(values[i], StandardCharsets.US_ASCII);
            if (!name.startsWith(":") && !"content-type".equals(name)) {
                headers.add(name, new String(values[i + 1], StandardCharsets.US_ASCII));
            }
        }
    }
}
