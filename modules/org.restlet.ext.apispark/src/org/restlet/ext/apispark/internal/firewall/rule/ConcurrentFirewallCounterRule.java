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

import org.restlet.ext.apispark.internal.firewall.rule.counter.ConcurrentCounter;
import org.restlet.ext.apispark.internal.firewall.rule.policy.CountingPolicy;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * {@link FirewallCounterRule} specialized in counting concurrent requests.
 * 
 * @author Guillaume Blondeau
 */
public class ConcurrentFirewallCounterRule extends FirewallCounterRule {

    /** Cache of {@link ConcurrentCounter}. */
    private LoadingCache<String, ConcurrentCounter> cache;

    /**
     * Contructor.
     * 
     * @param countingPolicy
     *            The associated counting policy.
     */
    public ConcurrentFirewallCounterRule(CountingPolicy countingPolicy) {
        super(countingPolicy);
        initializeCache();
    }

    @Override
    protected void decrementCounter(String countedValue) {
        ConcurrentCounter counter = cache.getUnchecked(countedValue);
        counter.decrement();
    }

    @Override
    protected CounterResult incrementCounter(String countedValue) {
        ConcurrentCounter counter = cache.getUnchecked(countedValue);
        return counter.increment();
    }

    private void initializeCache() {
        CacheLoader<String, ConcurrentCounter> loader = new CacheLoader<String, ConcurrentCounter>() {
            public ConcurrentCounter load(String key) {
                return new ConcurrentCounter();
            }
        };
        cache = CacheBuilder.newBuilder()
                .expireAfterAccess(2, TimeUnit.MINUTES).build(loader);
    }

}
