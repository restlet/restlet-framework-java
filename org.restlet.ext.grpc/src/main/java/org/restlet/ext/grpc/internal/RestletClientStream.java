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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
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
import org.restlet.representation.ByteArrayRepresentation;
import org.restlet.util.Series;

/** SDK-framed, bounded unary exchange. No gRPC frame/status codec is implemented here. */
final class RestletClientStream extends AbstractClientStream {
    private final SerializingExecutor serial = new SerializingExecutor(Runnable::run);
    private final State state;
    private final Client client;
    private final String target;
    private final String authority;
    private final int wireLimit;
    private final ExecutorService io;
    private final Consumer<RestletClientStream> finished;
    private final ByteArrayOutputStream outbound = new ByteArrayOutputStream();
    private final StatsTraceContext stats;
    private Metadata headers;
    private volatile Request request;
    private volatile Thread worker;
    private volatile boolean done;
    private boolean sent;

    RestletClientStream(
            Client client,
            InetSocketAddress address,
            String authority,
            MethodDescriptor<?, ?> method,
            Metadata headers,
            CallOptions options,
            ClientStreamTracer[] tracers,
            int limit,
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
        this.io = io;
        this.finished = finished;
        this.stats = stats;
        state = new State(limit, stats, tracer, options);
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
                headers = metadata;
                stats.clientOutboundHeaders();
                serial.execute(state::allocated);
            }

            @Override
            public void writeFrame(WritableBuffer frame, boolean end, boolean flush, int messages) {
                Status failure = null;
                synchronized (outbound) {
                    if (!done && frame instanceof Buffer buffer) {
                        if (buffer.readableBytes() > wireLimit - outbound.size()) {
                            failure =
                                    Status.RESOURCE_EXHAUSTED.withDescription(
                                            "Encoded unary request exceeds byte budget");
                        } else {
                            outbound.write(buffer.data, 0, buffer.size);
                        }
                    }
                    if (frame != null) {
                        frame.release();
                    }
                    if (failure == null && end && !done && !sent) {
                        sent = true;
                        try {
                            io.execute(RestletClientStream.this::exchange);
                        } catch (RejectedExecutionException e) {
                            failure = Status.UNAVAILABLE.withCause(e);
                        }
                    }
                }
                if (failure != null) {
                    fail(failure);
                }
            }

            @Override
            public void cancel(Status status) {
                fail(status);
            }
        };
    }

    private void fail(Status status) {
        serial.execute(
                () -> {
                    if (!done) {
                        state.fail(status);
                    }
                });
        Request active = request;
        if (active != null) {
            active.abort();
        }
        Thread activeWorker = worker;
        if (activeWorker != null && activeWorker != Thread.currentThread()) {
            activeWorker.interrupt();
        }
    }

    private void exchange() {
        worker = Thread.currentThread();
        Response response = null;
        try {
            if (done) {
                return;
            }
            byte[] body;
            synchronized (outbound) {
                body = outbound.toByteArray();
                outbound.reset();
            }
            Request call =
                    new Request(
                            Method.POST,
                            target,
                            new ByteArrayRepresentation(
                                    body, MediaType.valueOf("application/grpc")));
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
                    call.getHeaders()
                            .add(key, new String(metadata[i + 1], StandardCharsets.US_ASCII));
                }
            }
            call.getClientInfo().setAgent("restlet-grpc/1.85.1");
            request = call;
            if (done || Thread.currentThread().isInterrupted()) {
                call.abort();
                return;
            }
            call.getDisconnectSignal()
                    .thenAccept(
                            cause ->
                                    fail(
                                            Status.UNAVAILABLE
                                                    .withDescription("gRPC peer disconnected")
                                                    .withCause(cause)));
            response = client.handle(call);
            if (done) {
                return;
            }
            if (response.getProtocol() == null
                    || !"2.0".equals(response.getProtocol().getVersion())) {
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
                    if (input != null) {
                        byte[] bytes = input.readNBytes(wireLimit + 1);
                        if (bytes.length > wireLimit) {
                            fail(
                                    Status.RESOURCE_EXHAUSTED.withDescription(
                                            "Encoded unary response exceeds byte budget"));
                            return;
                        }
                        serial.execute(() -> state.data(bytes));
                    }
                }
            }
            Metadata trailers = metadata(response.getTrailers());
            serial.execute(() -> state.trailers(trailers));
        } catch (IOException | RuntimeException e) {
            fail(Status.UNAVAILABLE.withDescription("Restlet gRPC exchange failed").withCause(e));
        } finally {
            if (response != null && response.getEntity() != null) {
                response.getEntity().release();
            }
            worker = null;
        }
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
        private boolean started;
        private Status pendingFailure;

        private State(
                int limit, StatsTraceContext stats, TransportTracer tracer, CallOptions options) {
            super(limit, stats, tracer, options);
        }

        private void allocated() {
            started = true;
            if (!done) {
                if (pendingFailure != null) {
                    transportReportStatus(pendingFailure, true, new Metadata());
                } else {
                    onStreamAllocated();
                }
            }
        }

        private void fail(Status status) {
            if (started) {
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

        @Override
        public void runOnTransportThread(Runnable runnable) {
            serial.execute(runnable);
        }

        @Override
        public void bytesRead(int count) {
            // Unary wire input is bounded and materialized before SDK deframing.
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
