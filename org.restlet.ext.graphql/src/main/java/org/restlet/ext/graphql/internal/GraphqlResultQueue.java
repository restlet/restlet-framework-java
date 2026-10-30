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
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Subscribes to the results of a subscription and queues them, serialized, for the thread that
 * writes them. One result is requested at a time: the writer calls {@link #request()} once a result
 * is written. Results received without being requested are queued up to a byte limit, beyond which
 * the subscription is cancelled.
 */
public final class GraphqlResultQueue implements Subscriber<ExecutionResult> {

    /** A signal taken from the queue. */
    public sealed interface Signal permits Next, Failure, Completed, Cancelled {}

    /**
     * A serialized result.
     *
     * @param json The result, in JSON.
     * @param size The size of the JSON text in UTF-8 bytes.
     */
    public record Next(String json, long size) implements Signal {}

    /**
     * A terminal failure.
     *
     * @param message The message sent to the client.
     * @param cause The cause to log, or null.
     */
    public record Failure(String message, Throwable cause) implements Signal {}

    /** The completion of the subscription. */
    public record Completed() implements Signal {}

    /** The cancellation of the subscription by the client: nothing more is sent. */
    public record Cancelled() implements Signal {}

    private final LinkedBlockingQueue<Signal> signals = new LinkedBlockingQueue<>();

    private final long maxQueuedBytes;

    private long queuedBytes;

    private volatile Subscription subscription;

    private volatile boolean cancelled;

    private boolean terminated;

    /**
     * Constructor.
     *
     * @param maxQueuedBytes The maximum number of bytes of results received but not yet taken.
     */
    public GraphqlResultQueue(long maxQueuedBytes) {
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
                signals.add(new Failure("The subscription exceeded its queued bytes limit", null));
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
        terminate(new Completed());
    }

    /**
     * Ends the queue with a failure and cancels the subscription.
     *
     * @param failure The failure.
     */
    public void fail(Failure failure) {
        terminate(failure);
        cancel();
    }

    /** Ends the queue silently and cancels the subscription, as requested by the client. */
    public void cancelByClient() {
        terminate(new Cancelled());
        cancel();
    }

    private synchronized void terminate(Signal signal) {
        if (!terminated) {
            terminated = true;
            signals.add(signal);
        }
    }

    /**
     * Takes the next signal, blocking until one is available.
     *
     * @return The next signal.
     * @throws InterruptedException If interrupted while waiting.
     */
    public Signal take() throws InterruptedException {
        Signal signal = signals.take();
        if (signal instanceof Next next) {
            synchronized (this) {
                queuedBytes -= next.size();
            }
        }
        return signal;
    }

    /** Requests the next result, once the previous one is written. */
    public void request() {
        Subscription current = subscription;
        if (current != null && !cancelled) {
            current.request(1);
        }
    }

    /** Cancels the subscription. Does nothing if already cancelled. */
    public void cancel() {
        cancelled = true;
        Subscription current = subscription;
        if (current != null) {
            current.cancel();
        }
    }
}
