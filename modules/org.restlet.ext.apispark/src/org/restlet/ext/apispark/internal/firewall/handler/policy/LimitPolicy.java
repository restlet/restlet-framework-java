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

package org.restlet.ext.apispark.internal.firewall.handler.policy;

import org.restlet.Request;
import org.restlet.ext.apispark.internal.firewall.rule.policy.CountingPolicy;

/**
 * Associates a limit to counted values extracted from the incoming
 * {@link Request} thanks to a {@link CountingPolicy}.
 * 
 * @author Guillaume Blondeau
 */
public abstract class LimitPolicy {

    /**
     * Returns the limit associated to the given counted value.
     * 
     * @param request
     *            The current request.
     * @param countedValue
     *            The counted value.
     * @return The limit associated to the given counted value.
     */
    public abstract int getLimit(Request request, String countedValue);

}
