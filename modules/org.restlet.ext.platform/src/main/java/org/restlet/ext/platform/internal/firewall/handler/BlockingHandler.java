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

package org.restlet.ext.platform.internal.firewall.handler;

import java.util.logging.Level;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.ext.platform.internal.firewall.handler.policy.LimitPolicy;
import org.restlet.ext.platform.internal.firewall.rule.CounterResult;
import org.restlet.routing.Filter;

/**
 * {@link ThresholdHandler} that updates the response's status to
 * {@link Status#CLIENT_ERROR_TOO_MANY_REQUESTS} when the limit is reached.
 *
 * @author Guillaume Blondeau
 * @deprecated Will be removed in 2.5 release.
 */
@Deprecated
public class BlockingHandler extends ThresholdHandler {

    /**
     * Constructor.
     *
     * @param limitPolicy
     *            The limit policy.
     */
    public BlockingHandler(LimitPolicy limitPolicy) {
        super(limitPolicy);
    }

    @Override
    protected int thresholdReached(Request request, Response response,
            CounterResult counterResult) {
        Context.getCurrentLogger().log(
                Level.FINE,
                "The current request has been blocked because \""
                        + counterResult.getCountedValue()
                        + "\" issued too many requests.");

        response.setStatus(Status.CLIENT_ERROR_TOO_MANY_REQUESTS);
        return Filter.SKIP;
    }

}
