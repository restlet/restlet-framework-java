/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
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

package org.restlet.ext.apispark;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.ext.apispark.internal.firewall.handler.BlockingHandler;
import org.restlet.ext.apispark.internal.firewall.handler.policy.RoleLimitPolicy;
import org.restlet.ext.apispark.internal.firewall.rule.ConcurrentFirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallRule;
import org.restlet.ext.apispark.internal.firewall.rule.PeriodicFirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.policy.UserCountingPolicy;
import org.restlet.routing.Filter;

/**
 * Filters that controls the incoming requests by applying a set of rules.
 * 
 * @author Guillaume Blondeau
 */
public class FirewallFilter extends Filter {

    /** The list of associated {@link FirewallRule}. */
    private List<FirewallRule> rules;

    /**
     * Constructor.
     */
    public FirewallFilter() {
        this(null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     */
    public FirewallFilter(Context context) {
        this(context, null);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param next
     *            The next Restlet.
     */
    public FirewallFilter(Context context, Restlet next) {
        super(context, next);
        rules = new ArrayList<FirewallRule>();
    }

    /**
     * Attach a period rate limiter to the Firewall. Default limit is set to 0.
     * 
     * @param limitsPerRole
     *            Limit assigned to users without role, or for roles with limit
     */
    public void addConcurrentRateLimit(Map<String, Integer> limitsPerRole) {
        addConcurrentRateLimit(limitsPerRole, 0);
    }

    /**
     * Attach a period rate limiter to the Firewall.
     * 
     * @param limitsPerRole
     *            Limit assigned to users without role, or for roles with limit
     * @param defaultLimit
     *            Limit assigned to users without role, or for roles with limit
     */
    public void addConcurrentRateLimit(Map<String, Integer> limitsPerRole,
            int defaultLimit) {
        FirewallCounterRule rule = new ConcurrentFirewallCounterRule(
                new UserCountingPolicy());
        rule.addHandler(new BlockingHandler(new RoleLimitPolicy(limitsPerRole,
                defaultLimit)));
        this.addCounter(rule);
    }

    /**
     * Attach a {@link FirewallCounterRule} to the {@link FirewallFilter}
     * 
     * @param module
     */
    public void addCounter(FirewallCounterRule module) {
        rules.add(module);
    }

    /**
     * Attach a rate limit to the Firewall. Default limit is set to 0.
     * 
     * @param period
     *            Period of the rate limit in seconds
     * @param limitsPerRole
     *            Map containing the limits per given group
     */
    public void addOnPeriodRateLimit(int period,
            Map<String, Integer> limitsPerRole) {
        addOnPeriodRateLimit(period, limitsPerRole, 0);
    }

    /**
     * Attach a period rate limiter to the Firewall.
     * 
     * @param period
     *            Period Period of the rate limit in seconds
     * @param limitsPerRole
     *            Map containing the limits per given group
     * @param defaultLimit
     *            Limit assigned to users without role, or for roles with limit
     */
    public void addOnPeriodRateLimit(int period,
            Map<String, Integer> limitsPerRole, int defaultLimit) {
        FirewallCounterRule rule = new PeriodicFirewallCounterRule(period,
                new UserCountingPolicy());
        rule.addHandler(new BlockingHandler(new RoleLimitPolicy(limitsPerRole,
                defaultLimit)));
        this.addCounter(rule);
    }

    @Override
    protected void afterHandle(Request request, Response response) {
        for (FirewallRule rule : rules) {
            rule.afterHandle(request, response);
        }
    }

    @Override
    public int beforeHandle(Request request, Response response) {
        int result = Filter.SKIP;
        for (FirewallRule rule : rules) {
            int value = rule.beforeHandle(request, response);
            if (value != Filter.CONTINUE) {
                return value;
            }
            result = value;
        }
        return result;
    }

    /**
     * Returns the associated set of {@link FirewallRule}.
     * 
     * @return The associated set of {@link FirewallRule}.
     */
    public List<FirewallRule> getRules() {
        return rules;
    }

    /**
     * Sets the set of {@link FirewallRule}.
     * 
     * @param rules
     *            The set of rules.
     */
    public void setRules(List<FirewallRule> rules) {
        this.rules = rules;
    }

}
