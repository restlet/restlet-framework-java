/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql.internal;

import graphql.ExecutionResult;
import graphql.GraphqlErrorBuilder;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.restlet.data.ServerSentEvent;
import org.restlet.representation.EventStreamRepresentation.EventSink;
import org.restlet.representation.EventStreamRepresentation.EventWriter;

/**
 * Writes the results of an operation as a {@code graphql-sse} event stream in distinct connections
 * mode: one {@code next} event per result, then a {@code complete} event.
 *
 * <p>A subscription is consumed one result at a time: the next one is requested once the previous
 * event is written, so the connector's flow control applies to the publisher. Results that arrive
 * without being requested are queued up to a byte limit, beyond which the subscription is
 * cancelled. A failed write, such as a client disconnect, cancels the subscription.
 */
public final class GraphqlEventStream implements EventWriter {

    private static final ServerSentEvent COMPLETE =
            ServerSentEvent.builder().event("complete").data("").build();

    private static final ServerSentEvent OPEN = ServerSentEvent.builder().comment("").build();

    private final ExecutionResult result;

    private final Publisher<ExecutionResult> publisher;

    private final AtomicInteger activeSubscriptions;

    private final Set<GraphqlEventStream> openStreams;

    private final BooleanSupplier endpointStopped;

    private volatile QueueingSubscriber subscriber;

    private volatile boolean stopped;

    private final int maxSubscriptions;

    private final long maxQueuedBytes;

    private final Logger logger;

    /**
     * Creates the stream of a single result.
     *
     * @param result The result of a query or mutation.
     */
    public GraphqlEventStream(ExecutionResult result) {
        this.result = result;
        this.publisher = null;
        this.activeSubscriptions = null;
        this.openStreams = null;
        this.endpointStopped = null;
        this.maxSubscriptions = 0;
        this.maxQueuedBytes = 0;
        this.logger = null;
    }

    /**
     * Creates the stream of a subscription.
     *
     * @param publisher The publisher of the subscription results.
     * @param activeSubscriptions The counter of active subscriptions, shared by an adapter.
     * @param openStreams The open streams of an adapter, stopped with {@link #stop()}.
     * @param endpointStopped Indicates if the adapter is stopped, checked once the stream is open.
     * @param maxSubscriptions The maximum number of active subscriptions.
     * @param maxQueuedBytes The maximum number of bytes of results received but not yet written.
     * @param logger The logger of publisher failures.
     */
    public GraphqlEventStream(
            Publisher<ExecutionResult> publisher,
            AtomicInteger activeSubscriptions,
            Set<GraphqlEventStream> openStreams,
            BooleanSupplier endpointStopped,
            int maxSubscriptions,
            long maxQueuedBytes,
            Logger logger) {
        this.result = null;
        this.publisher = publisher;
        this.activeSubscriptions = activeSubscriptions;
        this.openStreams = openStreams;
        this.endpointStopped = endpointStopped;
        this.maxSubscriptions = maxSubscriptions;
        this.maxQueuedBytes = maxQueuedBytes;
        this.logger = logger;
    }

    /**
     * Returns the {@code next} event of a result.
     *
     * @param result The result.
     * @return The {@code next} event.
     */
    public static ServerSentEvent next(ExecutionResult result) {
        return next(GraphqlJson.writeString(result.toSpecification()));
    }

    private static ServerSentEvent next(String json) {
        return ServerSentEvent.builder().event("next").data(json).build();
    }

    private static ServerSentEvent error(String message) {
        return next(
                ExecutionResult.newExecutionResult()
                        .errors(
                                List.of(
                                        GraphqlErrorBuilder.newError()
                                                .message("%s", message)
                                                .build()))
                        .build());
    }

    /**
     * Stops the stream, for example because its adapter stops: the subscription is cancelled and
     * the stream ends with an error and a {@code complete} event.
     */
    public void stop() {
        stopped = true;
        QueueingSubscriber current = subscriber;
        if (current != null) {
            current.fail(new Failure("The GraphQL endpoint is stopping", null));
        }
    }

    @Override
    public void write(EventSink sink) throws IOException {
        if (publisher == null) {
            sink.send(next(result));
            sink.send(COMPLETE);
            return;
        }

        if (activeSubscriptions.incrementAndGet() > maxSubscriptions) {
            activeSubscriptions.decrementAndGet();
            sink.send(error("Too many active subscriptions"));
            sink.send(COMPLETE);
            return;
        }
        // An empty comment sends the response head at once, before the first result.
        sink.send(OPEN);
        QueueingSubscriber subscriber = new QueueingSubscriber(maxQueuedBytes);
        this.subscriber = subscriber;
        openStreams.add(this);
        try {
            // Registered before checking, so that a concurrent stop of the adapter sees it.
            if (stopped || endpointStopped.getAsBoolean()) {
                subscriber.fail(new Failure("The GraphQL endpoint is stopping", null));
            } else {
                publisher.subscribe(subscriber);
            }
            while (true) {
                Object signal = subscriber.take();
                if (signal instanceof Next next) {
                    sink.send(next(next.json()));
                    subscriber.request();
                } else if (signal instanceof Failure failure) {
                    if (failure.cause() != null) {
                        logger.log(Level.WARNING, "GraphQL subscription failed", failure.cause());
                    }
                    sink.send(error(failure.message()));
                    sink.send(COMPLETE);
                    return;
                } else {
                    sink.send(COMPLETE);
                    return;
                }
            }
        } catch (InterruptedException e) {
            // Interrupted by a failed heartbeat: the client is gone.
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Event stream interrupted");
        } finally {
            subscriber.cancel();
            openStreams.remove(this);
            activeSubscriptions.decrementAndGet();
        }
    }

    /** A serialized result and its size in bytes. */
    private record Next(String json, long size) {}

    /** A terminal failure of the stream. */
    private record Failure(String message, Throwable cause) {}

    /** The completion of the stream. */
    private static final Object COMPLETED = new Object();

    /** Queues the serialized results of a subscription for the writer thread. */
    private static final class QueueingSubscriber implements Subscriber<ExecutionResult> {

        private final LinkedBlockingQueue<Object> signals = new LinkedBlockingQueue<>();

        private final long maxQueuedBytes;

        private long queuedBytes;

        private volatile Subscription subscription;

        private volatile boolean cancelled;

        private boolean terminated;

        QueueingSubscriber(long maxQueuedBytes) {
            this.maxQueuedBytes = maxQueuedBytes;
        }

        @Override
        public void onSubscribe(Subscription subscription) {
            this.subscription = subscription;
            if (cancelled) {
                subscription.cancel();
            } else {
                subscription.request(1);
            }
        }

        @Override
        public void onNext(ExecutionResult item) {
            String json = GraphqlJson.writeString(item.toSpecification());
            long size = json.getBytes(StandardCharsets.UTF_8).length;
            synchronized (this) {
                if (terminated) {
                    return;
                }
                queuedBytes += size;
                if (queuedBytes > maxQueuedBytes) {
                    terminated = true;
                    signals.add(
                            new Failure("The subscription exceeded its queued bytes limit", null));
                    cancel();
                    return;
                }
                signals.add(new Next(json, size));
            }
        }

        @Override
        public void onError(Throwable throwable) {
            terminate(new Failure("The subscription failed", throwable));
        }

        @Override
        public void onComplete() {
            terminate(COMPLETED);
        }

        /** Ends the stream with a failure and cancels the subscription. */
        void fail(Failure failure) {
            terminate(failure);
            cancel();
        }

        private synchronized void terminate(Object signal) {
            if (!terminated) {
                terminated = true;
                signals.add(signal);
            }
        }

        Object take() throws InterruptedException {
            Object signal = signals.take();
            if (signal instanceof Next next) {
                synchronized (this) {
                    queuedBytes -= next.size();
                }
            }
            return signal;
        }

        void request() {
            Subscription current = subscription;
            if (current != null && !cancelled) {
                current.request(1);
            }
        }

        void cancel() {
            cancelled = true;
            Subscription current = subscription;
            if (current != null) {
                current.cancel();
            }
        }
    }
}
