/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.grpc.internal;

import io.grpc.Attributes;
import io.grpc.CallOptions;
import io.grpc.ClientStreamTracer;
import io.grpc.Deadline;
import io.grpc.InternalMetadata;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.internal.AbstractClientStream;
import io.grpc.internal.Http2ClientStreamTransportState;
import io.grpc.internal.ReadableBuffers;
import io.grpc.internal.SerializingExecutor;
import io.grpc.internal.StatsTraceContext;
import io.grpc.internal.TransportFrameUtil;
import io.grpc.internal.TransportTracer;
import io.grpc.internal.WritableBuffer;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.function.Consumer;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

/**
 * One gRPC call over one HTTP/2 exchange of a borrowed Restlet client, streaming in both
 * directions. The SDK frames and deframes messages; this stream only moves their bytes.
 *
 * <p>The exchange starts with the request headers. The request body is fed frame by frame from a
 * bounded queue, written by the client's background writer, and ends at the half-close; written
 * bytes are reported to the SDK, which derives its readiness from them. The response body is read
 * as it arrives, while the request is still written: bytes handed to the SDK but not yet consumed
 * by its deframer, which only reads the messages the application requested, stay within the queue
 * budget too, so a slow consumer stops the reads, and HTTP/2 flow control stops the server.
 */
@SuppressWarnings("null")
final class RestletClientStream extends AbstractClientStream {
    private static final MediaType GRPC = MediaType.valueOf("application/grpc");

    private final SerializingExecutor serial = new SerializingExecutor(Runnable::run);
    private final State state;
    private final Client client;
    private final String target;
    private final String authority;
    private final int wireLimit;
    private final int queueBytes;
    private final ExecutorService io;
    private final Consumer<RestletClientStream> finished;
    private final StatsTraceContext stats;

    /** Request frames waiting for the network; also guards the outbound fields below. */
    private final ArrayDeque<byte[]> outbound = new ArrayDeque<>();

    /** Guards {@link #unread}, and wakes the response reader. */
    private final Object inbound = new Object();

    private Metadata headers;
    private volatile Request request;
    private volatile Thread worker;
    private volatile Deadline deadline;
    private volatile boolean done;
    private volatile boolean failed;

    /** Whether the peer reset the stream or the connection failed. */
    private volatile boolean peerGone;

    /** Whether the request body ended, normally or not; guarded by {@link #outbound}. */
    private boolean requestEnded;

    private boolean started;

    /** Request bytes queued; guarded by {@link #outbound}. */
    private int queued;

    /** Bytes framed for the latest request message, from the frame that started it. */
    private int open;

    /** Request bytes queued when the latest message started; guarded by {@link #outbound}. */
    private int messageStart;

    /** Whether the SDK half-closed the request; guarded by {@link #outbound}. */
    private boolean halfClosed;

    /** Response bytes handed to the SDK but not consumed yet; guarded by {@link #inbound}. */
    private long unread;

    RestletClientStream(
            Client client,
            InetSocketAddress address,
            String authority,
            MethodDescriptor<?, ?> method,
            Metadata headers,
            CallOptions options,
            ClientStreamTracer[] tracers,
            int limit,
            int queueBytes,
            ExecutorService io,
            Consumer<RestletClientStream> finished) {
        this(
                client,
                address,
                authority,
                method,
                headers,
                options,
                limit,
                queueBytes,
                io,
                finished,
                StatsTraceContext.newClientContext(tracers, Attributes.EMPTY, headers),
                TransportTracer.getDefaultFactory().create());
    }

    private RestletClientStream(
            Client client,
            InetSocketAddress address,
            String authority,
            MethodDescriptor<?, ?> method,
            Metadata headers,
            CallOptions options,
            int limit,
            int queueBytes,
            ExecutorService io,
            Consumer<RestletClientStream> finished,
            StatsTraceContext stats,
            TransportTracer tracer) {
        super(
                hint -> new Buffer(Math.max(1, Math.min(hint, 16384))),
                stats,
                tracer,
                headers,
                options,
                false);
        this.client = client;
        this.authority = authority;
        String ip = address.getAddress().getHostAddress();
        if (ip.contains(":")) {
            ip = "[" + ip + "]";
        }
        target = "http://" + ip + ":" + address.getPort() + "/" + method.getFullMethodName();
        wireLimit = limit + 5;
        this.queueBytes = queueBytes;
        this.io = io;
        this.finished = finished;
        this.stats = stats;
        state = new State(limit, stats, tracer, options);
        setOnReadyThreshold(Math.max(1, queueBytes / 2));
    }

    @Override
    protected State transportState() {
        return state;
    }

    @Override
    public Attributes getAttributes() {
        return Attributes.EMPTY;
    }

    @Override
    public void setDeadline(Deadline value) {
        deadline = value;
        super.setDeadline(value);
    }

    @Override
    public void setAuthority(String value) {
        if (!authority.equals(value)) {
            throw new UnsupportedOperationException("Per-call authority override is not supported");
        }
    }

    @Override
    protected Sink abstractClientStreamSink() {
        return new Sink() {
            @Override
            public void writeHeaders(Metadata metadata, byte[] payload) {
                begin(metadata);
            }

            @Override
            public void writeFrame(WritableBuffer frame, boolean end, boolean flush, int messages) {
                send(frame, end, messages);
            }

            @Override
            public void cancel(Status status) {
                fail(status);
            }
        };
    }

    /** Starts the HTTP/2 exchange with the request headers. */
    private void begin(Metadata metadata) {
        headers = metadata;
        stats.clientOutboundHeaders();
        serial.execute(state::allocated);
        Status failure = null;
        synchronized (outbound) {
            if (!done && !started) {
                started = true;
                try {
                    io.execute(this::exchange);
                } catch (RejectedExecutionException e) {
                    failure = Status.UNAVAILABLE.withCause(e);
                }
            }
        }
        if (failure != null) {
            fail(failure);
        }
    }

    /** Queues a request frame for the writer, and records the half-close. */
    private void send(WritableBuffer frame, boolean end, int messages) {
        Status failure = null;
        synchronized (outbound) {
            if (frame instanceof Buffer buffer && !done && !halfClosed) {
                failure = enqueue(buffer, messages);
            }
            if (frame != null) {
                frame.release();
            }
            if (end) {
                halfClosed = true;
            }
            outbound.notifyAll();
        }
        if (failure != null) {
            fail(failure);
        }
    }

    /**
     * Copies a frame into the queue, within its budget; must be called holding {@link #outbound}. A
     * message may only start while the queue is within its budget, as it is for a sender that
     * respects readiness; once started, it may grow up to the message-size limit, whatever the tail
     * of the previous message still queued. The queue so stays within its budget plus one message.
     * A frame counting messages starts a new one.
     *
     * @return The failure if the budget is exceeded, null otherwise.
     */
    private Status enqueue(Buffer buffer, int messages) {
        int size = buffer.size;
        if (messages > 0) {
            messageStart = queued;
            open = 0;
        }
        int message = open + size;
        if (messageStart > queueBytes || message > wireLimit) {
            return Status.RESOURCE_EXHAUSTED.withDescription("Request queue byte budget exceeded");
        }
        if (size > 0) {
            open = message;
            outbound.addLast(Arrays.copyOf(buffer.data, size));
            queued += size;
            onSendingBytes(size);
        }
        return null;
    }

    private void fail(Status status) {
        failed = true;
        Deadline current = deadline;
        Status reported =
                (status.getCode() == Status.Code.UNAVAILABLE
                                && current != null
                                && current.isExpired())
                        // A peer whose deadline expired resets the stream with CANCEL, which
                        // Restlet reports as a closed connection. Report it like grpc-netty, so
                        // that the SDK call turns it into DEADLINE_EXCEEDED.
                        ? Status.CANCELLED
                                .withDescription("gRPC stream ended after the deadline")
                                .withCause(status.getCause())
                        : status;
        serial.execute(
                () -> {
                    if (!done) {
                        state.fail(reported);
                    }
                });
        wake();
        Request active = request;
        if (active != null) {
            active.abort();
        }
        Thread activeWorker = worker;
        if (activeWorker != null && activeWorker != Thread.currentThread()) {
            activeWorker.interrupt();
        }
    }

    /**
     * Ends a request still being written when the call ended, as when a server answers before the
     * half-close: its queued frames are dropped and the HTTP/2 stream is reset, which also releases
     * a writer blocked by flow control. The call's own status is already reported.
     */
    private void endRequest() {
        boolean unfinished;
        synchronized (outbound) {
            unfinished = started && !requestEnded;
            outbound.clear();
            queued = 0;
            requestEnded = true;
            outbound.notifyAll();
        }
        Request active = request;
        if (unfinished && active != null) {
            active.abort();
        }
    }

    /** Wakes the request writer and the response reader, once the call failed or ended. */
    private void wake() {
        synchronized (outbound) {
            outbound.notifyAll();
        }
        synchronized (inbound) {
            inbound.notifyAll();
        }
    }

    private void exchange() {
        worker = Thread.currentThread();
        Response response = null;
        try {
            if (done) {
                return;
            }
            Request call = newRequest();
            request = call;
            if (done || failed || Thread.currentThread().isInterrupted()) {
                call.abort();
                return;
            }
            // A disconnect only wakes the reader and the writer: a peer that reset the stream
            // after its whole response, as a server finishing before the half-close may, leaves
            // that response to be read; one that did not fails the head or the body read.
            call.getDisconnectSignal()
                    .thenAccept(
                            cause -> {
                                peerGone = true;
                                wake();
                            });
            // Returns at the response head; the request body is still written meanwhile.
            response = client.handle(call);
            if (!done) {
                receive(response);
            }
        } catch (IOException | RuntimeException e) {
            fail(Status.UNAVAILABLE.withDescription("Restlet gRPC exchange failed").withCause(e));
        } finally {
            if (response != null && response.getEntity() != null) {
                response.getEntity().release();
            }
            worker = null;
        }
    }

    /** Returns the HTTP/2 request of the call, with the gRPC metadata as headers. */
    private Request newRequest() {
        Request call = new Request(Method.POST, target, requestEntity());
        // Resource origin selects the resolved peer; base reference selects HTTP authority.
        call.getResourceRef().setBaseRef(new Reference("http://" + authority));
        call.getHeaders().set("te", "trailers");
        byte[][] metadata = TransportFrameUtil.toHttp2Headers(headers);
        for (int i = 0; i < metadata.length; i += 2) {
            String key = new String(metadata[i], StandardCharsets.US_ASCII);
            if (!key.startsWith(":")
                    && !"content-type".equals(key)
                    && !"user-agent".equals(key)
                    && !"te".equals(key)) {
                call.getHeaders().add(key, new String(metadata[i + 1], StandardCharsets.US_ASCII));
            }
        }
        call.getClientInfo().setAgent("restlet-grpc/1.85.1");
        return call;
    }

    /** Hands the response head, body and trailers to the SDK. */
    private void receive(Response response) throws IOException {
        if (response.getProtocol() == null || !"2.0".equals(response.getProtocol().getVersion())) {
            fail(
                    Status.UNAVAILABLE
                            .withDescription("gRPC requires an HTTP/2 response")
                            .withCause(response.getStatus().getThrowable()));
            return;
        }
        Metadata initial = metadata(response.getHeaders());
        initial.put(
                InternalMetadata.keyOf(":status", Metadata.ASCII_STRING_MARSHALLER),
                Integer.toString(response.getStatus().getCode()));
        if (response.getEntity() != null && response.getEntity().getMediaType() != null) {
            initial.put(
                    Metadata.Key.of("content-type", Metadata.ASCII_STRING_MARSHALLER),
                    response.getEntity().getMediaType().toString());
        }
        if (response.getHeaders().getFirstValue("grpc-status", true) != null) {
            serial.execute(() -> state.trailers(initial));
            return;
        }
        serial.execute(() -> state.headers(initial));
        if (response.getEntity() != null) {
            try (var input = response.getEntity().getStream()) {
                if (input != null && !readAll(input)) {
                    return;
                }
            }
        }
        Metadata trailers = metadata(response.getTrailers());
        serial.execute(() -> state.trailers(trailers));
    }

    /**
     * Hands the response body to the SDK as it arrives, within the queue budget.
     *
     * @return False if the call ended before the end of the body.
     */
    private boolean readAll(InputStream input) throws IOException {
        byte[] chunk = new byte[16384];
        for (; ; ) {
            int room;
            synchronized (inbound) {
                try {
                    // Once the peer is gone, what the connector received is read regardless.
                    while (unread >= queueBytes && !done && !failed && !peerGone) {
                        inbound.wait();
                    }
                } catch (InterruptedException _) {
                    Thread.currentThread().interrupt();
                    throw new InterruptedIOException("Interrupted reading the gRPC response");
                }
                if (done || failed) {
                    return false;
                }
                room = (int) Math.max(1, Math.min(chunk.length, queueBytes - unread));
            }
            int count = input.read(chunk, 0, room);
            if (count < 0) {
                return true;
            }
            if (count > 0) {
                synchronized (inbound) {
                    unread += count;
                }
                byte[] bytes = Arrays.copyOf(chunk, count);
                serial.execute(() -> state.data(bytes));
            }
        }
    }

    /** Returns the request entity, written by the client's background writer from the queue. */
    private Representation requestEntity() {
        return new OutputRepresentation(GRPC, Representation.UNKNOWN_SIZE) {
            @Override
            public void write(OutputStream output) throws IOException {
                for (; ; ) {
                    byte[] data;
                    synchronized (outbound) {
                        try {
                            while (outbound.isEmpty()
                                    && !halfClosed
                                    && !done
                                    && !failed
                                    && !peerGone) {
                                outbound.wait();
                            }
                        } catch (InterruptedException _) {
                            requestEnded = true;
                            Thread.currentThread().interrupt();
                            throw new InterruptedIOException(
                                    "Interrupted writing the gRPC request");
                        }
                        if (failed || peerGone) {
                            requestEnded = true;
                            throw new IOException("gRPC request not completed");
                        }
                        data = outbound.pollFirst();
                        if (data == null) {
                            // Half-closed, or the call ended first: the request body ends.
                            requestEnded = true;
                            return;
                        }
                        queued -= data.length;
                    }
                    output.write(data);
                    output.flush();
                    int sent = data.length;
                    serial.execute(() -> state.sent(sent));
                }
            }
        };
    }

    private static Metadata metadata(Series<Header> headers) {
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

    private final class State extends Http2ClientStreamTransportState {
        private boolean allocatedOnce;
        private Status pendingFailure;

        private State(
                int limit, StatsTraceContext stats, TransportTracer tracer, CallOptions options) {
            super(limit, stats, tracer, options);
        }

        private void allocated() {
            allocatedOnce = true;
            if (!done) {
                if (pendingFailure != null) {
                    transportReportStatus(pendingFailure, true, new Metadata());
                } else {
                    onStreamAllocated();
                }
            }
        }

        private void fail(Status status) {
            if (allocatedOnce) {
                transportReportStatus(status, true, new Metadata());
            } else if (pendingFailure == null) {
                // Shutdown may arrive after stream creation but before SDK listener installation.
                pendingFailure = status;
            }
        }

        private void headers(Metadata metadata) {
            if (!done) {
                transportHeadersReceived(metadata);
            }
        }

        private void data(byte[] bytes) {
            if (!done) {
                transportDataReceived(ReadableBuffers.wrap(bytes), false);
            }
        }

        private void trailers(Metadata metadata) {
            if (!done) {
                transportTrailersReceived(metadata);
            }
        }

        private void sent(int count) {
            if (!done) {
                onSentBytes(count);
            }
        }

        @Override
        public void runOnTransportThread(Runnable runnable) {
            serial.execute(runnable);
        }

        @Override
        public void bytesRead(int count) {
            // The deframer consumed response bytes: free that much of the inbound budget.
            synchronized (inbound) {
                unread -= count;
                inbound.notifyAll();
            }
        }

        @Override
        public void deframeFailed(Throwable cause) {
            // Also abort the HTTP exchange, whose worker may be blocked reading the body.
            RestletClientStream.this.fail(Status.fromThrowable(cause));
        }

        @Override
        protected void http2ProcessingFailed(
                Status status, boolean stopDelivery, Metadata trailers) {
            transportReportStatus(status, stopDelivery, trailers);
        }

        @Override
        public void deframerClosed(boolean partial) {
            // Release the call slot first: the SDK notifies the caller from super.deframerClosed(),
            // and a caller that starts its next call right away must not find the slot still taken.
            if (!done) {
                done = true;
                wake();
                endRequest();
                finished.accept(RestletClientStream.this);
            }
            super.deframerClosed(partial);
        }
    }

    private static final class Buffer implements WritableBuffer {
        private final byte[] data;
        private int size;

        private Buffer(int capacity) {
            data = new byte[capacity];
        }

        @Override
        public void write(byte[] source, int offset, int length) {
            System.arraycopy(source, offset, data, size, length);
            size += length;
        }

        @Override
        public void write(byte value) {
            data[size++] = value;
        }

        @Override
        public int writableBytes() {
            return data.length - size;
        }

        @Override
        public int readableBytes() {
            return size;
        }

        @Override
        public void release() {
            Arrays.fill(data, (byte) 0);
        }
    }
}
