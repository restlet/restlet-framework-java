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
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.data.ServerSentEvent;
import org.restlet.ext.graphql.internal.GraphqlJson;
import org.restlet.representation.EventStreamReader;
import org.restlet.representation.Representation;

/**
 * A cold publisher of the results of an operation sent with {@code graphql-sse} in distinct
 * connections mode. Each subscriber opens its own request, on the first demand. One virtual thread
 * per subscription reads the events and emits all the signals.
 */
final class GraphqlSsePublisher implements Publisher<ExecutionResult> {

    private final GraphqlClientAdapter adapter;

    private final String document;

    private final String operationName;

    private final Map<String, Object> variables;

    GraphqlSsePublisher(
            GraphqlClientAdapter adapter,
            String document,
            String operationName,
            Map<String, Object> variables) {
        this.adapter = adapter;
        this.document = document;
        this.operationName = operationName;
        this.variables = variables;
    }

    @Override
    public void subscribe(Subscriber<? super ExecutionResult> subscriber) {
        Objects.requireNonNull(subscriber, "subscriber");
        EventStreamSubscription subscription = new EventStreamSubscription(subscriber);
        subscriber.onSubscribe(subscription);
        if (!adapter.register(subscription)) {
            subscription.stop();
        } else if (subscription.isDone()) {
            // Cancelled or finished from onSubscribe(), before being registered.
            adapter.unregister(subscription);
        }
    }

    /** The subscription of one subscriber. */
    final class EventStreamSubscription implements Subscription {

        private final Subscriber<? super ExecutionResult> subscriber;

        private long demand;

        private boolean started;

        private volatile boolean invalidDemand;

        private volatile boolean cancelled;

        private volatile boolean finished;

        private volatile boolean stopped;

        private volatile Request request;

        EventStreamSubscription(Subscriber<? super ExecutionResult> subscriber) {
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
                subscriber.onError(invalidDemandError());
                adapter.unregister(this);
            } else if (start) {
                Thread.ofVirtual().name("restlet-graphql-sse").start(this::run);
            } else if (n <= 0) {
                // Unblocks a pending read, then the reader thread signals the error.
                Request current = request;
                if (current != null) {
                    current.abort();
                }
            }
        }

        /** Indicates if the subscription is cancelled or its reader thread is done. */
        boolean isDone() {
            return cancelled || finished;
        }

        @Override
        public void cancel() {
            synchronized (this) {
                if (cancelled) {
                    return;
                }
                cancelled = true;
                notifyAll();
            }
            Request current = request;
            if (current != null) {
                current.abort();
            }
            adapter.unregister(this);
        }

        /** Stops the subscription because the adapter stops, signalling an error. */
        void stop() {
            boolean signal;
            synchronized (this) {
                if (cancelled) {
                    return;
                }
                stopped = true;
                // Without a reader thread, signal from here and prevent one from starting.
                signal = !started;
                started = true;
            }
            if (signal) {
                subscriber.onError(stoppedError());
            }
            cancel();
        }

        private IllegalArgumentException invalidDemandError() {
            return new IllegalArgumentException("The requested number of results must be positive");
        }

        private GraphqlTransportException stoppedError() {
            return new GraphqlTransportException(
                    "The GraphQL client was stopped", null, null, null);
        }

        private void run() {
            Response response = null;
            try {
                Request current =
                        adapter.createEventStreamRequest(
                                new EventStreamRequest(), document, operationName, variables);
                request = current;
                if (cancelled) {
                    terminateIfStopped();
                    return;
                }
                response = adapter.getClient().handle(current);
                if (cancelled) {
                    terminateIfStopped();
                    return;
                }
                Representation entity = response.getEntity();
                if (entity == null
                        || entity.getMediaType() == null
                        || !MediaType.TEXT_EVENT_STREAM.equals(entity.getMediaType(), true)) {
                    ExecutionResult result = adapter.toResult(response);
                    if (awaitDemand()) {
                        subscriber.onNext(result);
                        subscriber.onComplete();
                    }
                    return;
                }
                readEvents(entity.getStream());
            } catch (GraphqlTransportException e) {
                fail(e);
            } catch (IOException e) {
                // Only thrown while reading the event stream of a response.
                fail(
                        new GraphqlTransportException(
                                "Unable to read the GraphQL event stream",
                                response.getStatus(),
                                null,
                                e));
            } catch (RuntimeException e) {
                fail(e);
            } finally {
                if (response != null && response.getEntity() != null) {
                    response.getEntity().release();
                }
                finished = true;
                adapter.unregister(this);
            }
        }

        private void readEvents(InputStream stream) throws IOException {
            BoundedInputStream input =
                    new BoundedInputStream(stream, adapter.getMaxResponseBytes());
            try (EventStreamReader reader = new EventStreamReader(input)) {
                while (!cancelled) {
                    ServerSentEvent event = reader.readEvent();
                    input.resetCount();
                    if (event == null) {
                        if (!cancelled) {
                            throw new GraphqlTransportException(
                                    "The GraphQL event stream ended without a complete event",
                                    null,
                                    null,
                                    null);
                        }
                    } else if ("next".equals(event.getEvent())) {
                        Map<String, Object> result =
                                GraphqlJson.asObject(GraphqlJson.parse(event.getData()));
                        if (result == null) {
                            throw new GraphqlTransportException(
                                    "Invalid next event", null, event.getData(), null);
                        }
                        if (!awaitDemand()) {
                            break;
                        }
                        subscriber.onNext(ExecutionResult.fromSpecification(result));
                    } else if ("complete".equals(event.getEvent())) {
                        if (!cancelled) {
                            subscriber.onComplete();
                        }
                        return;
                    }
                }
                terminateIfStopped();
            } catch (GraphqlJson.InvalidJsonException e) {
                throw new GraphqlTransportException("Invalid next event", null, null, e);
            }
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

        private void terminateIfStopped() {
            if (stopped) {
                subscriber.onError(stoppedError());
            }
        }

        private void fail(Throwable error) {
            if (stopped) {
                subscriber.onError(stoppedError());
            } else if (!cancelled) {
                cancelled = true;
                // A read aborted by invalid demand reports that demand.
                subscriber.onError(invalidDemand ? invalidDemandError() : error);
            }
        }

        /**
         * A request aborted as soon as its connector can abort it, when cancelled before the
         * exchange started.
         */
        private final class EventStreamRequest extends Request {

            @Override
            public void setAbortAction(BooleanSupplier action) {
                super.setAbortAction(action);
                if (action != null && (cancelled || invalidDemand)) {
                    action.getAsBoolean();
                }
            }
        }
    }

    /**
     * Limits the bytes read for each event. The event reader buffers ahead, so the limit is checked
     * with a margin of one buffer.
     */
    private static final class BoundedInputStream extends FilterInputStream {

        private static final int READ_AHEAD = 8192;

        private final long limit;

        private long count;

        BoundedInputStream(InputStream in, long limit) {
            super(in);
            this.limit = limit;
        }

        void resetCount() {
            count = 0;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            if (b >= 0) {
                add(1);
            }
            return b;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int n = super.read(b, off, len);
            if (n > 0) {
                add(n);
            }
            return n;
        }

        private void add(int n) throws GraphqlTransportException {
            count += n;
            if (count > limit + READ_AHEAD) {
                throw new GraphqlTransportException(
                        "A GraphQL event exceeds " + limit + " bytes", null, null, null);
            }
        }
    }
}
