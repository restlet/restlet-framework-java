/**
 * Copyright 2005-2024 Qlik
 *
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 *
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 *
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 *
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 *
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com
 *
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.platform.internal.firewall.rule.counter;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.ext.platform.internal.firewall.rule.CounterResult;

import com.google.common.base.Stopwatch;

/**
 * {@link Counter} which counts requests on a given period.
 *
 * @author Guillaume Blondeau
 * @deprecated Will be removed in 2.5 release.
 */
@Deprecated
public class PeriodicCounter extends Counter {

    /** The counter value. */
    protected final AtomicInteger counter;

    /** Next counter reset time */
    private final AtomicLong counterReset;

    /** The period associated to the Counter. */
    private final long period;

    /** Calculates periods duration and resets them. */
    private final Stopwatch stopwatch;

    /**
     * Constructor.
     *
     * @param period
     *            The period associated to the counter.
     */
    public PeriodicCounter(long period) {
        this.counter = new AtomicInteger();
        this.period = period;
        this.stopwatch = Stopwatch.createStarted();
        this.counterReset = new AtomicLong();
    }

    @Override
    public void decrement() {
    }

    @Override
    public CounterResult increment() {
        long elapsed;
        long reset;

        // if counter time is elapsed, reset it.
        synchronized (stopwatch) {
            elapsed = stopwatch.elapsed(TimeUnit.SECONDS);
            if (elapsed > period) {
                Context.getCurrentLogger().log(Level.FINE,
                        "Period reinitialized.");
                stopwatch.reset();
                stopwatch.start();
                counter.getAndSet(0);
                reset = System.currentTimeMillis() / 1000L + period;
                counterReset.getAndSet(reset);
                elapsed = 0;
            } else {
                reset = counterReset.get();
            }
        }

        int consumed = counter.incrementAndGet();
        CounterResult counterResult = new CounterResult();
        counterResult.setConsumed(consumed);
        counterResult.setElapsed(elapsed);
        counterResult.setReset(reset);
        return counterResult;
    }

}
