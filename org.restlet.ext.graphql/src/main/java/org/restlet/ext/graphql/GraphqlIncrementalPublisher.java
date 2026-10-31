/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

import graphql.ExecutionResult;
import graphql.GraphQLError;
import graphql.incremental.DeferPayload;
import graphql.incremental.DelayedIncrementalPartialResult;
import graphql.incremental.DelayedIncrementalPartialResultImpl;
import graphql.incremental.IncrementalExecutionResult;
import graphql.incremental.IncrementalExecutionResultImpl;
import graphql.incremental.IncrementalPayload;
import graphql.incremental.StreamPayload;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.graphql.internal.GraphqlJson;
import org.restlet.representation.MultiPartRepresentation;
import org.restlet.representation.MultipartStreamReader;

/**
 * Reads an incremental response: the first part becomes an {@link IncrementalExecutionResult} whose
 * item publisher reads the following parts. The publisher accepts a single subscriber; the response
 * is released when it completes, fails or is cancelled.
 */
final class GraphqlIncrementalPublisher implements Publisher<DelayedIncrementalPartialResult> {

    private final Response response;

    private final MultipartStreamReader reader;

    private final AtomicBoolean subscribed = new AtomicBoolean();

    private GraphqlIncrementalPublisher(Response response, MultipartStreamReader reader) {
        this.response = response;
        this.reader = reader;
    }

    /**
     * Reads the initial result of an incremental response. The response is released unless more
     * results follow.
     *
     * @param response The response, with a multipart entity.
     * @param maxPartBytes The maximum size of a part.
     * @return The initial result.
     * @throws GraphqlTransportException If the response is not a valid incremental response.
     */
    static ExecutionResult read(Response response, long maxPartBytes)
            throws GraphqlTransportException {
        Status status = response.getStatus();
        boolean keep = false;
        MultipartStreamReader reader = null;
        try {
            reader = new MultipartStreamReader(response.getEntity(), maxPartBytes);
            Map<String, Object> initial = part(reader.readPart(), status);
            boolean hasNext = Boolean.TRUE.equals(initial.get("hasNext"));
            IncrementalExecutionResultImpl.Builder result =
                    IncrementalExecutionResultImpl.fromExecutionResult(
                                    ExecutionResult.fromSpecification(initial))
                            .hasNext(hasNext)
                            .incremental(payloads(initial.get("incremental")));
            if (hasNext) {
                result.incrementalItemPublisher(new GraphqlIncrementalPublisher(response, reader));
                keep = true;
            } else {
                checkEnd(reader, status);
            }
            return result.build();
        } catch (IOException e) {
            throw new GraphqlTransportException(
                    "Unable to read the GraphQL incremental response", status, null, e);
        } finally {
            if (!keep) {
                release(response, reader);
            }
        }
    }

    /** Checks that the last part is followed by the close delimiter. */
    private static void checkEnd(MultipartStreamReader reader, Status status) throws IOException {
        if (reader.readPart() != null) {
            throw new GraphqlTransportException(
                    "The GraphQL incremental response has parts after the last result",
                    status,
                    null,
                    null);
        }
    }

    /** Closes the reader, which owns the stream of the entity, and releases the entity. */
    private static void release(Response response, MultipartStreamReader reader) {
        if (reader != null) {
            try {
                reader.close();
            } catch (IOException _) {
                // Released anyway
            }
        }
        response.getEntity().release();
    }

    private static Map<String, Object> part(MultiPartRepresentation.Part part, Status status)
            throws IOException {
        if (part == null) {
            throw new GraphqlTransportException(
                    "The GraphQL incremental response ended early", status, null, null);
        }
        Map<String, Object> result = null;
        try {
            result =
                    GraphqlJson.asObject(
                            GraphqlJson.parse(part.getContent().getStream().readAllBytes()));
        } catch (GraphqlJson.InvalidJsonException _) {
            // Reported below
        }
        if (result == null) {
            throw new GraphqlTransportException(
                    "Invalid GraphQL incremental part", status, null, null);
        }
        return result;
    }

    private static List<IncrementalPayload> payloads(Object incremental) {
        List<IncrementalPayload> payloads = new ArrayList<>();
        if (incremental instanceof List<?> items) {
            for (Object item : items) {
                Map<String, Object> map = GraphqlJson.asObject(item);
                if (map != null) {
                    payloads.add(payload(map));
                }
            }
        }
        return payloads;
    }

    @SuppressWarnings("unchecked")
    private static IncrementalPayload payload(Map<String, Object> map) {
        List<GraphQLError> errors =
                map.get("errors") instanceof List<?> list
                        ? list.stream()
                                .map(GraphqlJson::asObject)
                                .filter(Objects::nonNull)
                                .map(GraphQLError::fromSpecification)
                                .toList()
                        : List.of();
        List<Object> path =
                map.get("path") instanceof List<?> list ? (List<Object>) list : List.of();
        Map<String, Object> extensions = GraphqlJson.asObject(map.get("extensions"));
        String label = map.get("label") instanceof String string ? string : null;
        if (map.get("items") instanceof List<?> items) {
            StreamPayload.Builder builder =
                    StreamPayload.newStreamedItem().items((List<Object>) items);
            builder.path(path).label(label).errors(errors);
            if (extensions != null) {
                builder.extensions(new LinkedHashMap<Object, Object>(extensions));
            }
            return builder.build();
        }
        DeferPayload.Builder builder = DeferPayload.newDeferredItem().data(map.get("data"));
        builder.path(path).label(label).errors(errors);
        if (extensions != null) {
            builder.extensions(new LinkedHashMap<Object, Object>(extensions));
        }
        return builder.build();
    }

    @Override
    public void subscribe(Subscriber<? super DelayedIncrementalPartialResult> subscriber) {
        Objects.requireNonNull(subscriber, "subscriber");
        if (!subscribed.compareAndSet(false, true)) {
            subscriber.onSubscribe(
                    new Subscription() {
                        @Override
                        public void request(long n) {}

                        @Override
                        public void cancel() {}
                    });
            subscriber.onError(
                    new IllegalStateException("The incremental results have one subscriber"));
            return;
        }
        subscriber.onSubscribe(new PartSubscription(subscriber));
    }

    /** Reads the parts on demand, on a virtual thread that emits all the signals. */
    private final class PartSubscription implements Subscription {

        private final Subscriber<? super DelayedIncrementalPartialResult> subscriber;

        private long demand;

        private boolean started;

        private volatile boolean invalidDemand;

        private volatile boolean cancelled;

        PartSubscription(Subscriber<? super DelayedIncrementalPartialResult> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void request(long n) {
            boolean signal = false;
            boolean start = false;
            synchronized (this) {
                if (cancelled) {
                    return;
                }
                if (n <= 0) {
                    invalidDemand = true;
                    // Without a reader thread, signal from here and prevent one from starting.
                    signal = !started;
                    if (signal) {
                        cancelled = true;
                    }
                } else {
                    demand = demand + n < 0 ? Long.MAX_VALUE : demand + n;
                    start = !started;
                }
                started = true;
                notifyAll();
            }
            if (signal) {
                release(response, reader);
                subscriber.onError(invalidDemandError());
            } else if (start) {
                Thread.ofVirtual().name("restlet-graphql-incremental").start(this::run);
            } else if (n <= 0) {
                // Unblocks a pending read, then the reader thread signals the error.
                response.abort();
            }
        }

        @Override
        public void cancel() {
            boolean idle;
            synchronized (this) {
                if (cancelled) {
                    return;
                }
                cancelled = true;
                // Without a reader thread to clean up, prevent one from starting.
                idle = !started;
                started = true;
                notifyAll();
            }
            if (idle) {
                release(response, reader);
            } else {
                response.abort();
            }
        }

        private IllegalArgumentException invalidDemandError() {
            return new IllegalArgumentException("The requested number of results must be positive");
        }

        private void run() {
            try {
                while (true) {
                    Map<String, Object> part = part(reader.readPart(), response.getStatus());
                    boolean hasNext = Boolean.TRUE.equals(part.get("hasNext"));
                    DelayedIncrementalPartialResultImpl.Builder result =
                            DelayedIncrementalPartialResultImpl.newIncrementalExecutionResult()
                                    .hasNext(hasNext)
                                    .incrementalItems(payloads(part.get("incremental")));
                    Map<String, Object> extensions = GraphqlJson.asObject(part.get("extensions"));
                    if (extensions != null) {
                        // Extensions can have null values.
                        result.extensions(new LinkedHashMap<Object, Object>(extensions));
                    }
                    if (!hasNext) {
                        checkEnd(reader, response.getStatus());
                    }
                    if (!awaitDemand()) {
                        return;
                    }
                    subscriber.onNext(result.build());
                    if (!hasNext) {
                        if (!cancelled) {
                            subscriber.onComplete();
                        }
                        return;
                    }
                }
            } catch (IOException | RuntimeException e) {
                if (!cancelled) {
                    cancelled = true;
                    subscriber.onError(invalidDemand ? invalidDemandError() : failure(e));
                }
            } finally {
                release(response, reader);
            }
        }

        /** Returns the error of a failed read, unless the read was aborted by invalid demand. */
        private GraphqlTransportException failure(Exception e) {
            return e instanceof GraphqlTransportException transport
                    ? transport
                    : new GraphqlTransportException(
                            "Unable to read the GraphQL incremental response",
                            response.getStatus(),
                            null,
                            e);
        }

        /** Waits for demand. Returns false if cancelled meanwhile. */
        private boolean awaitDemand() {
            synchronized (this) {
                while (!cancelled && demand == 0 && !invalidDemand) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                }
                if (cancelled) {
                    return false;
                }
                if (invalidDemand) {
                    cancelled = true;
                    subscriber.onError(invalidDemandError());
                    return false;
                }
                if (demand != Long.MAX_VALUE) {
                    demand--;
                }
                return true;
            }
        }
    }
}
