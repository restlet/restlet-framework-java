/**
 * Copyright 2005-2020 Talend
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
 * https://restlet.com/open-source/
 *
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.platform.internal.firewall.rule.counter;

import org.restlet.ext.platform.internal.firewall.rule.CounterResult;
import org.restlet.ext.platform.internal.firewall.rule.FirewallCounterRule;
import org.restlet.ext.platform.internal.firewall.rule.policy.CountingPolicy;

/**
 * Counts requests. Is associated to a {@link FirewallCounterRule} and a counted
 * value (identifier returned by a {@link CountingPolicy}).
 *
 * @author Guillaume Blondeau
 * @deprecated Will be removed in 2.5 release.
 */
@Deprecated
public abstract class Counter {

    /**
     * Decrements the counter.
     */
    public abstract void decrement();

    /**
     * Increments the counter value.
     *
     * @return The counter's value.
     */
    public abstract CounterResult increment();

}
