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
import org.restlet.data.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.apispark.internal.firewall.handler.policy.LimitPolicy;
import org.restlet.ext.apispark.internal.firewall.rule.CounterResult;
import org.restlet.ext.apispark.internal.firewall.rule.counter.PeriodicCounter;
import org.restlet.util.Series;

/**
 * {@link BlockingHandler} that sets the Rate limitation headers to the
 * {@link Response}.<br>
 * Must be used in association with {@link PeriodicCounter}.<br>
 * <ul>
 * <li>X-RateLimit-Remaining: The approximative number of requests left for the
 * time window.</li>
 * <li>X-RateLimit-Limit: The approximative number of request limit.</li>
 * <li>X-RateLimit-Reset: The approximative remaining window before the rate
 * limit resets in UTC epoch seconds</li>
 * </ul>
 * 
 * @author Guillaume Blondeau
 */
public class RateLimitationHandler extends BlockingHandler {

    /**
     * Contructor.
     * 
     * @param limitPolicy
     */
    public RateLimitationHandler(LimitPolicy limitPolicy) {
        super(limitPolicy);
    }

    @Override
    public int handle(Request request, Response response,
            CounterResult counterResult) {
        Series<Header> headers = response.getHeaders();
        headers.set(
                "X-RateLimit-Remaining",
                Integer.toString(getLimit(request,
                        counterResult.getCountedValue())
                        - counterResult.getConsumed()));
        headers.set(
                "X-RateLimit-Limit",
                Integer.toString(getLimit(request,
                        counterResult.getCountedValue())));
        headers.set("X-RateLimit-Reset",
                Long.toString(counterResult.getReset()));
        response.getAttributes()
                .put(HeaderConstants.ATTRIBUTE_HEADERS, headers);

        return super.handle(request, response, counterResult);
    }

}
