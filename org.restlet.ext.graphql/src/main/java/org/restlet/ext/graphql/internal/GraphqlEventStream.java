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
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.reactivestreams.Publisher;
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

    private volatile GraphqlResultQueue subscriber;

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
        GraphqlResultQueue current = subscriber;
        if (current != null) {
            current.fail(new GraphqlResultQueue.Failure("The GraphQL endpoint is stopping", null));
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
        GraphqlResultQueue subscriber = new GraphqlResultQueue(maxQueuedBytes);
        this.subscriber = subscriber;
        openStreams.add(this);
        try {
            // An empty comment sends the response head at once, before the first result.
            sink.send(OPEN);
            // Registered before checking, so that a concurrent stop of the adapter sees it.
            if (stopped || endpointStopped.getAsBoolean()) {
                subscriber.fail(
                        new GraphqlResultQueue.Failure("The GraphQL endpoint is stopping", null));
            } else {
                publisher.subscribe(subscriber);
            }
            while (true) {
                GraphqlResultQueue.Signal signal = subscriber.take();
                if (signal instanceof GraphqlResultQueue.Next next) {
                    sink.send(next(next.json()));
                    subscriber.request();
                } else if (signal instanceof GraphqlResultQueue.Failure failure) {
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
}
