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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.apispark.internal.firewall.handler.ThresholdHandler;
import org.restlet.ext.apispark.internal.firewall.rule.counter.Counter;
import org.restlet.ext.apispark.internal.firewall.rule.policy.CountingPolicy;
import org.restlet.routing.Filter;

/**
 * A {@link FirewallCounterRule} associates:
 * <ul>
 * <li>a {@link CountingPolicy} that identifies requests through a custom
 * policy,</li>
 * <li>a {@link Counter} that extracts a counted value from a request,</li>
 * <li>and a {@link ThresholdHandler} that defines a limit and a custom action
 * when the limit is reached.</li>/
 * <ul>
 * 
 * @author Guillaume Blondeau
 */
public abstract class FirewallCounterRule extends FirewallRule {
    /**
     * Indicates if an unknown counted value should be blocked by default.
     * Default is true
     */
    private boolean blockingUnknownCountedValue = true;

    /** The associated policy. */
    private CountingPolicy countingPolicy;

    /** The list of handlers. */
    private List<ThresholdHandler> handlers;

    /**
     * Creates a {@link FirewallCounterRule} with a specified
     * {@link CountingPolicy}.
     * 
     * @param countingPolicy
     *            The {@link CountingPolicy} to attach to the
     *            {@link FirewallCounterRule}.
     */
    public FirewallCounterRule(CountingPolicy countingPolicy) {
        handlers = new ArrayList<ThresholdHandler>();
        this.setCountingPolicy(countingPolicy);
    }

    /**
     * Attaches a {@link ThresholdHandler} to the {@link FirewallCounterRule}.
     * 
     * @param handler
     *            The {@link ThresholdHandler} to attach.
     */
    public void addHandler(ThresholdHandler handler) {
        handlers.add(handler);
    }

    /**
     * By default, it decrements the counter associated to the request.
     */
    @Override
    public void afterHandle(Request request, Response response) {
        this.decrement(request);
    }

    /**
     * Determines the countedValue (value returned by the attached
     * {@link CountingPolicy}), increments the related {@link Counter} and calls
     * the attached {@link ThresholdHandler}.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     * @return The continuation status.
     */
    public int beforeHandle(Request request, Response response) {

        String countedValue = this.countingPolicy.getCountedValue(request);

        if (countedValue == null) {
            return isBlockingUnknownCountedValue() ? Filter.SKIP
                    : Filter.CONTINUE;
        }

        CounterResult counterResult = incrementCounter(countedValue);
        counterResult.setCountedValue(countedValue);

        Context.getCurrentLogger().log(
                Level.FINE,
                "Counter " + this.getClass() + " incremented for value: "
                        + countedValue);

        for (ThresholdHandler handler : handlers) {
            int result = handler.handle(request, response, counterResult);
            if (result != Filter.CONTINUE) {
                return result;
            }
        }

        return Filter.CONTINUE;
    }

    /**
     * Method called after processing. Determines the countedValue (value
     * returned by the attached {@link CountingPolicy}), decrements the related
     * {@link Counter} and calls the attached {@link ThresholdHandler}.
     * 
     * @param request
     *            The request to handle.
     */
    public void decrement(Request request) {
        String countedValue = this.countingPolicy.getCountedValue(request);
        if (countedValue != null) {
            decrementCounter(countedValue);
        }
    }

    /**
     * Method which decreases the counter related to the given countedValue
     * 
     * @param countedValue
     *            Value returned by the attached {@link CountingPolicy}
     */
    protected abstract void decrementCounter(String countedValue);

    /**
     * Method which increases the counter related to the given countedValue
     * 
     * @param countedValue
     *            Value returned by the attached {@link CountingPolicy}
     * @return The state of the counter related to the given countedValue
     */
    protected abstract CounterResult incrementCounter(String countedValue);

    /**
     * Indicates if an unknown counted value should be blocked by default.
     * 
     * @return True if an unknown counted value should be blocked by default.
     */
    public boolean isBlockingUnknownCountedValue() {
        return blockingUnknownCountedValue;
    }

    /**
     * Indicates if an unknown counted value should be blocked by default.
     * 
     * @param blockingUnknownCountedValue
     *            True if an unknown counted value should be blocked by default.
     */
    public void setBlockingUnknownCountedValue(
            boolean blockingUnknownCountedValue) {
        this.blockingUnknownCountedValue = blockingUnknownCountedValue;
    }

    /**
     * Sets the counting policy.
     * 
     * @param countingPolicy
     *            the counting policy.
     */
    public void setCountingPolicy(CountingPolicy countingPolicy) {
        this.countingPolicy = countingPolicy;
    }

}
