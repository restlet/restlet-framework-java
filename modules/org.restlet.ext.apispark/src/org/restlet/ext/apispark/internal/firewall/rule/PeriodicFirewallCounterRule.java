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

import java.util.concurrent.TimeUnit;

import org.restlet.ext.apispark.internal.firewall.rule.counter.PeriodicCounter;
import org.restlet.ext.apispark.internal.firewall.rule.policy.CountingPolicy;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * {@link FirewallCounterRule} specialized in counting requests on a specific
 * period of time. For each countedValue (value returned by the
 * {@link CountingPolicy}), a {@link PeriodicCounter} is associated.
 * 
 * @author Guillaume Blondeau
 */
public class PeriodicFirewallCounterRule extends FirewallCounterRule {

    /** Cache of {@link PeriodicCounter}. */
    private LoadingCache<String, PeriodicCounter> cache;

    /** Period in second associated to the {@link FirewallCounterRule} */
    private long period;

    /**
     * Contructor.
     * 
     * @param period
     *            Period associated to the {@link PeriodicFirewallCounterRule}.
     *            Each created {@link PeriodicCounter} will have this one.
     * @param periodUnit
     *            Period time unit associated to the {@link FirewallCounterRule}
     *            .
     * @param countingPolicy
     *            The associated counting policy.
     */
    public PeriodicFirewallCounterRule(int period, TimeUnit periodUnit,
            CountingPolicy countingPolicy) {
        super(countingPolicy);
        this.period = periodUnit.toSeconds(period);
        initializeCache();
    }

    /**
     * Does nothing.
     */
    @Override
    protected void decrementCounter(String countedValue) {
    }

    @Override
    protected CounterResult incrementCounter(String countedValue) {
        PeriodicCounter individualCounter = cache.getUnchecked(countedValue);
        return individualCounter.increment();
    }

    private void initializeCache() {
        CacheLoader<String, PeriodicCounter> loader = new CacheLoader<String, PeriodicCounter>() {
            public PeriodicCounter load(String key) {
                return new PeriodicCounter(period);
            }
        };

        // do not set cache expiration below 1 minute
        long cacheExpiration = 2 * period < TimeUnit.MINUTES.toSeconds(1) ? TimeUnit.MINUTES
                .toSeconds(1) : 2 * period;

        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(cacheExpiration, TimeUnit.SECONDS)
                .build(loader);
    }

}
