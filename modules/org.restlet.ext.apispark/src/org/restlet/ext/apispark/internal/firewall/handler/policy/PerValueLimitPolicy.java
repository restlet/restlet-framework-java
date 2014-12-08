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

import java.util.HashMap;
import java.util.Map;

import org.restlet.Request;

/**
 * Defines a limit for known counted values.
 * 
 * @author Guillaume Blondeau
 */
public class PerValueLimitPolicy extends LimitPolicy {

    /**
     * The default limit applies when the counted value has not been found in
     * the list of known counted values.
     */
    public int defaultLimit;

    /** Maps a counted value to a limit. */
    private Map<String, Integer> limitsPerValue;

    /**
     * Constructor.<br>
     * Defines only the {@link PerValueLimitPolicy#defaultLimit} to 0.
     */
    public PerValueLimitPolicy() {
        this(0);
    }

    /**
     * Constructor.<br>
     * Defines only the {@link PerValueLimitPolicy#defaultLimit}.
     * 
     * @param defaultLimit
     *            The default limit.
     */
    public PerValueLimitPolicy(int defaultLimit) {
        this(new HashMap<String, Integer>(), defaultLimit);
    }

    /**
     * Constructor.<br>
     * 
     * @param limitsPerValue
     *            The map of limits per counted value.
     * @param defaultLimit
     *            The default limit.
     */
    public PerValueLimitPolicy(Map<String, Integer> limitsPerValue,
            int defaultLimit) {
        this.defaultLimit = defaultLimit;
        this.limitsPerValue = limitsPerValue;
    }

    /**
     * Associates a limit to a counted value.
     * 
     * @param countedValue
     *            The counted value.
     * @param limit
     *            The associated limit.
     */
    public void addCountedValue(String countedValue, int limit) {
        limitsPerValue.put(countedValue, limit);
    }

    @Override
    public int getLimit(Request request, String countedValue) {
        int result = defaultLimit;
        if (countedValue != null) {
            if (limitsPerValue.containsKey(countedValue)) {
                result = limitsPerValue.get(countedValue);
            }
        }

        return result;
    }

}
