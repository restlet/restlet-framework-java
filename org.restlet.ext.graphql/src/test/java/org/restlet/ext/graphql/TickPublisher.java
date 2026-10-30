/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.ext.graphql;

import java.util.concurrent.atomic.AtomicInteger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Publishes 1, 2, ... up to a count, or forever when the count is negative, honoring demand and
 * cancellation. Counts its instances and cancellations for the tests.
 */
final class TickPublisher implements Publisher<Integer> {

    static final AtomicInteger CREATED = new AtomicInteger();

    static final AtomicInteger CANCELLED = new AtomicInteger();

    private final int count;

    TickPublisher(int count) {
        this.count = count;
        CREATED.incrementAndGet();
    }

    @Override
    public void subscribe(Subscriber<? super Integer> subscriber) {
        subscriber.onSubscribe(new Ticks(subscriber));
    }

    private final class Ticks implements Subscription {

        private final Subscriber<? super Integer> subscriber;

        private volatile boolean done;

        private long demand;

        private boolean emitting;

        private int next = 1;

        Ticks(Subscriber<? super Integer> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void request(long n) {
            synchronized (this) {
                demand += n;
                if (emitting) {
                    // Requested from onNext: the running loop picks up the new demand.
                    return;
                }
                emitting = true;
            }
            while (true) {
                int value;
                synchronized (this) {
                    if (done || demand == 0 || (count >= 0 && next > count)) {
                        emitting = false;
                        break;
                    }
                    demand--;
                    value = next++;
                }
                subscriber.onNext(value);
            }
            boolean complete;
            synchronized (this) {
                complete = !done && count >= 0 && next > count;
                if (complete) {
                    done = true;
                }
            }
            if (complete) {
                subscriber.onComplete();
            }
        }

        @Override
        public void cancel() {
            if (!done) {
                done = true;
                CANCELLED.incrementAndGet();
            }
        }
    }
}
