/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.firewall.rule.counter;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.ext.apispark.internal.firewall.rule.CounterResult;

import com.google.common.base.Stopwatch;

/**
 * {@link Counter} which counts requests on a given period.
 * 
 * @author Guillaume Blondeau
 */
public class PeriodicCounter extends Counter {

    /** The period associated to the Counter. */
    private int period;

    /** Calculates periods duration and resets them. */
    private Stopwatch stopwatch;

    /**
     * Constructor.
     * 
     * @param period
     *            The period associated to the counter.
     */
    public PeriodicCounter(int period) {
        this.period = period;
        this.stopwatch = Stopwatch.createStarted();
    }

    @Override
    public void decrement() {
    }

    @Override
    public synchronized CounterResult increment() {
        if (stopwatch.elapsed(TimeUnit.SECONDS) > period) {
            Context.getCurrentLogger().log(Level.FINE, "Period reinitialized.");
            stopwatch.reset();
            stopwatch.start();
            value = 0;
        }
        value++;
        CounterResult counterResult = new CounterResult();
        counterResult.setConsumed(value);
        counterResult.setElapsed(stopwatch.elapsed(TimeUnit.SECONDS));
        counterResult.setReset(System.currentTimeMillis() / 1000L + period
                - stopwatch.elapsed(TimeUnit.SECONDS));
        return counterResult;
    }

}
