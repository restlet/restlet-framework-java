/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.smithy.server.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Header;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;
import org.restlet.util.Series;
import software.amazon.smithy.java.framework.model.UnknownOperationException;
import software.amazon.smithy.java.http.api.HttpHeaders;
import software.amazon.smithy.java.io.datastream.DataStream;
import software.amazon.smithy.java.server.Service;
import software.amazon.smithy.java.server.core.Handler;
import software.amazon.smithy.java.server.core.HandlerAssembler;
import software.amazon.smithy.java.server.core.HttpJob;
import software.amazon.smithy.java.server.core.HttpRequest;
import software.amazon.smithy.java.server.core.HttpResponse;
import software.amazon.smithy.java.server.core.ProtocolResolver;
import software.amazon.smithy.java.server.core.ServiceMatcher;
import software.amazon.smithy.java.server.core.ServiceProtocolResolutionRequest;
import software.amazon.smithy.java.server.core.ServiceProtocolResolutionResult;

/**
 * Runs the calls of the smithy-java services of a started adapter: resolves their operation, runs
 * the SDK's handler chain on the request's thread, rather than the SDK's single-thread
 * orchestrator, and writes the SDK response, within the adapter's limits.
 */
public final class SmithyServerHost implements AutoCloseable {
    private final String preparedKey = SmithyServerHost.class.getName() + "@" + hashCode();
    private final ProtocolResolver resolver;
    private final List<Handler> handlers;
    private final long maxRequestBytes;
    private final Semaphore slots;
    private final Duration maxCallDuration;
    private final ScheduledThreadPoolExecutor timer;
    private final Set<Call> running = ConcurrentHashMap.newKeySet();
    private volatile boolean closed;

    public SmithyServerHost(
            List<Service> services,
            long maxRequestBytes,
            int maxConcurrentCalls,
            Duration maxCallDuration) {
        resolver =
                new ProtocolResolver(
                        new ServiceMatcher(
                                List.of(
                                        software.amazon.smithy.java.server.Route.builder()
                                                .pathPrefix("/")
                                                .services(services)
                                                .build())));
        handlers = new HandlerAssembler().assembleHandlers(services);
        this.maxRequestBytes = maxRequestBytes;
        this.slots = new Semaphore(maxConcurrentCalls);
        this.maxCallDuration = maxCallDuration;
        timer =
                new ScheduledThreadPoolExecutor(
                        1, Thread.ofPlatform().daemon().name("restlet-smithy-deadlines").factory());
        timer.setRemoveOnCancelPolicy(true);
    }

    /** A request mapped to the SDK below a routing base, with its operation, or none. */
    private record Prepared(
            String remaining, HttpRequest request, ServiceProtocolResolutionResult resolution) {}

    /**
     * Indicates whether a request calls a hosted operation.
     *
     * @param request The request.
     * @return True if the request calls a hosted operation.
     */
    public boolean resolves(Request request) {
        return prepare(request).resolution() != null;
    }

    /**
     * Maps a request to the SDK and resolves its operation, once per request and routing base: the
     * SDK resolves operations on the path below the route that matched, which nested routers move.
     */
    private Prepared prepare(Request request) {
        String remaining = request.getResourceRef().getRemainingPart(false, true);
        if (request.getAttributes().get(preparedKey) instanceof Prepared prepared
                && prepared.remaining().equals(remaining)) {
            return prepared;
        }
        var headers = HttpHeaders.ofModifiable();
        for (Header header : request.getHeaders()) {
            headers.addHeader(header.getName(), header.getValue());
        }
        Representation entity = request.getEntity();
        if (entity != null && entity.getMediaType() != null && !headers.hasHeader("content-type")) {
            headers.addHeader("content-type", entity.getMediaType().toString());
        }
        Prepared prepared;
        try {
            URI uri = URI.create(remaining.startsWith("/") ? remaining : "/" + remaining);
            var smithyRequest = new HttpRequest(headers, uri, request.getMethod().getName());
            ServiceProtocolResolutionResult resolution =
                    resolver.resolve(
                            new ServiceProtocolResolutionRequest(
                                    uri, headers, smithyRequest.context(), smithyRequest.method()));
            prepared = new Prepared(remaining, smithyRequest, resolution);
        } catch (UnknownOperationException | IllegalArgumentException e) {
            prepared = new Prepared(remaining, null, null);
        }
        request.getAttributes().put(preparedKey, prepared);
        return prepared;
    }

    /**
     * Handles a call.
     *
     * @param request The request.
     * @param response The response.
     */
    public void handle(Request request, Response response) {
        Prepared prepared = prepare(request);
        if (prepared.resolution() == null) {
            response.setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return;
        }
        Representation entity = request.getEntity();
        if (entity != null && entity.getSize() > maxRequestBytes) {
            response.setStatus(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE);
            return;
        }
        if (!slots.tryAcquire()) {
            response.setStatus(
                    Status.SERVER_ERROR_SERVICE_UNAVAILABLE, "Too many concurrent Smithy calls");
            return;
        }
        Exchange exchange = new Exchange();
        // Runs on a connector I/O thread: a body neither written nor released, as when the peer
        // left before the response was committed, is closed elsewhere.
        response.getDeliverySignal()
                .whenComplete(
                        (failure, error) -> {
                            exchange.release();
                            DataStream unsent = exchange.take();
                            if (unsent != null) {
                                Thread.ofVirtual()
                                        .name("restlet-smithy-body-close")
                                        .start(unsent::close);
                            }
                        });
        try {
            serve(prepared, request, response, exchange);
        } finally {
            // A bodyless response holds no resource of the call once it returns.
            if (response.getEntity() == null) {
                exchange.release();
            }
        }
    }

    /**
     * The resources a call holds after its operation returned: its slot, and the SDK body of its
     * response until written or closed. Each is given up once, by the first of the response's
     * write, its release and its delivery.
     */
    private final class Exchange {
        private final AtomicBoolean held = new AtomicBoolean(true);
        private final AtomicReference<DataStream> body = new AtomicReference<>();

        void release() {
            if (held.getAndSet(false)) {
                slots.release();
            }
        }

        /** Takes the body, which the caller then writes or closes; null if already taken. */
        DataStream take() {
            return body.getAndSet(null);
        }
    }

    private void serve(Prepared prepared, Request request, Response response, Exchange exchange) {
        Call call = new Call(Thread.currentThread());
        running.add(call);
        if (closed) {
            call.interrupt();
        }
        request.getDisconnectSignal().thenAccept(cause -> call.interrupt());
        ScheduledFuture<?> deadline;
        try {
            deadline =
                    timer.schedule(call::expire, maxCallDuration.toNanos(), TimeUnit.NANOSECONDS);
        } catch (RejectedExecutionException e) {
            // The host stopped while this call started.
            call.finish();
            running.remove(call);
            response.setStatus(
                    Status.SERVER_ERROR_SERVICE_UNAVAILABLE, "The Smithy server stopped");
            return;
        }
        var job =
                new HttpJob(
                        prepared.resolution().operation(),
                        prepared.resolution().protocol(),
                        prepared.request(),
                        new HttpResponse(HttpHeaders.ofModifiable()));
        LimitedInputStream body = null;
        boolean expired;
        try {
            Representation entity = request.getEntity();
            if (entity == null || !entity.isAvailable()) {
                prepared.request().setDataStream(DataStream.ofEmpty());
            } else {
                body = new LimitedInputStream(entity.getStream(), maxRequestBytes);
                prepared.request()
                        .setDataStream(
                                DataStream.ofInputStream(
                                        body,
                                        prepared.request().headers().contentType(),
                                        entity.getSize()));
            }
            run(job);
        } catch (IOException e) {
            response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST, e);
            return;
        } finally {
            deadline.cancel(false);
            expired = call.finish();
            running.remove(call);
        }
        if (body != null && body.exceeded) {
            close(job.response().getSerializedValue());
            response.setStatus(Status.CLIENT_ERROR_REQUEST_ENTITY_TOO_LARGE);
        } else if (expired) {
            close(job.response().getSerializedValue());
            response.setStatus(
                    Status.SERVER_ERROR_SERVICE_UNAVAILABLE,
                    "The operation exceeded its maximum duration");
        } else {
            write(job, response, exchange);
        }
    }

    /**
     * Runs the handlers as the SDK's orchestrator does, but joining on this thread. The protocol
     * handler, first in the chain, serializes a failure in its after step.
     */
    private void run(HttpJob job) {
        var done = new ArrayDeque<Handler>();
        for (Handler handler : handlers) {
            if (job.isFailure() || job.isCompleted()) {
                break;
            }
            done.push(handler);
            join(job, () -> handler.before(job));
        }
        while (!done.isEmpty()) {
            Handler handler = done.pop();
            join(job, () -> handler.after(job));
        }
    }

    /**
     * Runs a step, which may also fail synchronously, recording its failure in the job. An
     * interrupted wait cancels the pending step and keeps the thread interrupted, so the waits of
     * the remaining steps end at once too.
     */
    private static void join(HttpJob job, Supplier<CompletableFuture<Void>> step) {
        CompletableFuture<Void> future;
        try {
            future = step.get();
        } catch (RuntimeException e) {
            job.setFailure(e);
            return;
        }
        try {
            future.get();
        } catch (ExecutionException e) {
            job.setFailure(e.getCause());
        } catch (InterruptedException e) {
            future.cancel(true);
            job.setFailure(e);
            Thread.currentThread().interrupt();
        } catch (CancellationException e) {
            job.setFailure(e);
        }
    }

    private static void write(HttpJob job, Response response, Exchange exchange) {
        var smithyResponse = job.response();
        response.setStatus(Status.valueOf(smithyResponse.getStatusCode()));
        var raw = new Series<Header>(Header.class);
        smithyResponse.headers().forEachEntry(raw::add);
        response.getAttributes().put(HeaderConstants.ATTRIBUTE_RAW_RESPONSE_HEADERS, raw);
        DataStream body = smithyResponse.getSerializedValue();
        if (body == null || body.contentLength() == 0) {
            close(body);
        } else {
            exchange.body.set(body);
            response.setEntity(
                    new OutputRepresentation(
                            body.contentType() == null
                                    ? null
                                    : MediaType.valueOf(body.contentType()),
                            body.contentLength() < 0
                                    ? Representation.UNKNOWN_SIZE
                                    : body.contentLength()) {
                        @Override
                        public void write(OutputStream out) throws IOException {
                            DataStream owned = exchange.take();
                            try (InputStream in =
                                    owned == null
                                            ? InputStream.nullInputStream()
                                            : owned.asInputStream()) {
                                in.transferTo(out);
                            } finally {
                                close(owned);
                                exchange.release();
                            }
                        }

                        @Override
                        public void release() {
                            try {
                                super.release();
                                close(exchange.take());
                            } finally {
                                exchange.release();
                            }
                        }
                    });
        }
    }

    /** Closes an SDK body that is not written, or no longer needed. */
    private static void close(DataStream body) {
        if (body != null) {
            body.close();
        }
    }

    /** Interrupts the operations still running, and stops the deadline timer. */
    @Override
    public void close() {
        closed = true;
        running.forEach(Call::interrupt);
        timer.shutdownNow();
    }

    /** A running call, interrupted only while its handler chain runs. */
    private static final class Call {
        private final Thread thread;
        private boolean active = true;
        private boolean expired;

        private Call(Thread thread) {
            this.thread = thread;
        }

        synchronized void interrupt() {
            if (active) {
                thread.interrupt();
            }
        }

        synchronized void expire() {
            if (active) {
                expired = true;
                thread.interrupt();
            }
        }

        /** Ends the call on its thread, clearing any interrupt; returns whether it expired. */
        synchronized boolean finish() {
            active = false;
            Thread.interrupted();
            return expired;
        }
    }

    /** A request body that fails once it exceeds the adapter's limit. */
    private static final class LimitedInputStream extends FilterInputStream {
        private final long limit;
        private long count;
        private volatile boolean exceeded;

        private LimitedInputStream(InputStream in, long limit) {
            super(in);
            this.limit = limit;
        }

        @Override
        public int read() throws IOException {
            int result = super.read();
            if (result >= 0) {
                count(1);
            }
            return result;
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            int result = super.read(buffer, offset, length);
            if (result > 0) {
                count(result);
            }
            return result;
        }

        @Override
        public long skip(long n) throws IOException {
            // At most one byte beyond the limit, so that an oversized body fails as it is consumed.
            long result = super.skip(Math.min(n, limit - count + 1));
            if (result > 0) {
                count(result);
            }
            return result;
        }

        private void count(long read) throws IOException {
            count += read;
            if (count > limit) {
                exceeded = true;
                throw new IOException("Request body larger than " + limit + " bytes");
            }
        }
    }
}
