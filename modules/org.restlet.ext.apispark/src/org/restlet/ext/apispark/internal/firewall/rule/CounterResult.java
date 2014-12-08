/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark.internal.firewall.rule;

import org.restlet.ext.apispark.internal.firewall.rule.counter.ConcurrentCounter;
import org.restlet.ext.apispark.internal.firewall.rule.counter.Counter;
import org.restlet.ext.apispark.internal.firewall.rule.counter.PeriodicCounter;

/**
 * Gives the state of a {@link Counter}.
 * 
 * @author Guillaume Blondeau
 */
public class CounterResult {

    /** The number of requests done by the associated counter. */
    private int consumed;

    /** The identifier of the counter. */
    private String countedValue;

    /**
     * Time elapsed by the counter. Only available for the
     * {@link PeriodicCounter}. Will be set to 0 by the
     * {@link ConcurrentCounter}.
     */
    private long elapsed;

    /**
     * Time when the counter will be reset. Only available for the
     * {@link PeriodicCounter}. Will be set to 0 by the
     * {@link ConcurrentCounter}.
     */
    private long reset;

    /**
     * Returns the number of requests done by the associated {@link Counter}.
     * 
     * @return The number of requests done by the associated {@link Counter}.
     */
    public int getConsumed() {
        return consumed;
    }

    /**
     * Returns the identifier of the counter.
     * 
     * @return The identifier of the counter.
     */
    public String getCountedValue() {
        return countedValue;
    }

    /**
     * Returns the time elapsed by the counter. Only available for the
     * {@link PeriodicCounter}. Will be set to 0 by the
     * {@link ConcurrentCounter}.
     * 
     * @return Time elapsed by the counter. Only available for the
     *         {@link PeriodicCounter}. Will be set to 0 by the
     *         {@link ConcurrentCounter}.
     */
    public long getElapsed() {
        return elapsed;
    }

    /**
     * Returns the time when the counter will be reset. Only available for the
     * {@link PeriodicCounter}. Will be set to 0 by the
     * {@link ConcurrentCounter}.
     * 
     * @return Time when the counter will be reset. Only available for the
     *         {@link PeriodicCounter}. Will be set to 0 by the
     *         {@link ConcurrentCounter}.
     */
    public long getReset() {
        return reset;
    }

    /**
     * Sets the number of requests done by the associated {@link Counter}.
     * 
     * @param consumed
     */
    public void setConsumed(int consumed) {
        this.consumed = consumed;
    }

    /**
     * Sets the value which identifies the counter.
     * 
     * @param countedValue
     */
    public void setCountedValue(String countedValue) {
        this.countedValue = countedValue;
    }

    /**
     * Sets the time elapsed by the counter. Only available for the
     * {@link PeriodicCounter}. Will be set to 0 by the
     * {@link ConcurrentCounter}.
     * 
     * @param elapsed
     */
    public void setElapsed(long elapsed) {
        this.elapsed = elapsed;
    }

    /**
     * Sets the time when the counter will be reset. Only available for the
     * {@link PeriodicCounter}. Will be set to 0 by the
     * {@link ConcurrentCounter}.
     * 
     * @param reset
     */
    public void setReset(long reset) {
        this.reset = reset;
    }

}
