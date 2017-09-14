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

package org.restlet.ext.apispark.internal.firewall.handler;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.apispark.internal.firewall.handler.policy.LimitPolicy;
import org.restlet.ext.apispark.internal.firewall.rule.CounterResult;
import org.restlet.ext.apispark.internal.firewall.rule.policy.CountingPolicy;
import org.restlet.routing.Filter;

/**
 * Thanks to its {@link LimitPolicy} a {@link ThresholdHandler} throws an event
 * when the limit is reached.
 * 
 * @author Guillaume Blondeau
 */
public abstract class ThresholdHandler {

    /** The {@link LimitPolicy} associated to the {@link ThresholdHandler}. */
    private LimitPolicy limitPolicy;

    /**
     * Constructor.
     * 
     * @param limitPolicy
     *            The associated limit policy.
     */
    public ThresholdHandler(LimitPolicy limitPolicy) {
        this.setLimitPolicy(limitPolicy);
    }

    /**
     * Returns the limit associated to the given value.
     * 
     * @param request
     *            The request to handle.
     * @param countedValue
     *            The value returned by a {@link CountingPolicy}.
     * @return Limit associated to the given value.
     */
    public int getLimit(Request request, String countedValue) {
        return this.limitPolicy.getLimit(request, countedValue);
    }

    /**
     * Default implementation checks whether the limit is reached. If so, it
     * calls
     * {@link ThresholdHandler#thresholdReached(Request, Response, CounterResult)}
     * .
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @param counterResult
     *            The state of the counter.
     * @return The continuation status.
     */
    public int handle(Request request, Response response,
            CounterResult counterResult) {
        if (getLimit(request, counterResult.getCountedValue()) < counterResult
                .getConsumed()) {
            return thresholdReached(request, response, counterResult);
        }
        return Filter.CONTINUE;
    }

    /**
     * Sets the {@link LimitPolicy}.
     * 
     * @param limitPolicy
     *            The limit policy.
     */
    public void setLimitPolicy(LimitPolicy limitPolicy) {
        this.limitPolicy = limitPolicy;
    }

    /**
     * Method called when the defined limit is reached.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @param counterResult
     *            The state of the counter.
     * @return The continuation status.
     */
    protected abstract int thresholdReached(Request request, Response response,
            CounterResult counterResult);

}
