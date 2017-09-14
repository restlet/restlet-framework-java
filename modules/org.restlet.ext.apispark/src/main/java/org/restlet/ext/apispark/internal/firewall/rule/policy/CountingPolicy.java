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

package org.restlet.ext.apispark.internal.firewall.rule.policy;

import org.restlet.Request;
import org.restlet.ext.apispark.internal.firewall.rule.counter.Counter;

/**
 * A {@link CountingPolicy} extracts a value from a {@link Request} that will be
 * associated to a {@link Counter}.
 * 
 * @author Guillaume Blondeau
 */
public abstract class CountingPolicy {

    /**
     * Returns the value to be counted from the given {@link Request}.
     * 
     * @param request
     *            The request.
     * @return The counted value.
     */
    public abstract String getCountedValue(Request request);

}
