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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.restlet.Context;
import org.restlet.ext.apispark.internal.firewall.FirewallFilter;
import org.restlet.ext.apispark.internal.firewall.handler.BlockingHandler;
import org.restlet.ext.apispark.internal.firewall.handler.policy.PerValueLimitPolicy;
import org.restlet.ext.apispark.internal.firewall.handler.policy.RoleLimitPolicy;
import org.restlet.ext.apispark.internal.firewall.handler.policy.UniqueLimitPolicy;
import org.restlet.ext.apispark.internal.firewall.rule.ConcurrentFirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallIpFilteringRule;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallRule;
import org.restlet.ext.apispark.internal.firewall.rule.PeriodicFirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.policy.HostDomainCountingPolicy;
import org.restlet.ext.apispark.internal.firewall.rule.policy.IpAddressCountingPolicy;
import org.restlet.ext.apispark.internal.firewall.rule.policy.UserCountingPolicy;
import org.restlet.routing.Filter;
import org.restlet.security.Role;
import org.restlet.security.User;
import org.restlet.service.Service;

/**
 * Service that controls the incoming requests by applying a set of rules.
 * 
 * @author Guillaume Blondeau
 */
public class FirewallService extends Service {

    /** The list of associated {@link FirewallRule}. */
    private List<FirewallRule> rules;

    /**
     * Constructor. Enables the firewall by default.
     */
    public FirewallService() {
        this(true);
    }

    /**
     * Constructor.
     * 
     * @param enabled
     *            True if the service has been enabled.
     */
    public FirewallService(boolean enabled) {
        super(enabled);
        this.rules = new ArrayList<FirewallRule>();
    }

    /**
     * Adds a rule to the firewall.
     * 
     * @param rule
     *            The rule to add.
     */
    private void add(FirewallRule rule) {
        rules.add(rule);
    }

    /**
     * Adds a rule that limits the number of concurrent requests by request's
     * host domain.
     * 
     * @param limit
     *            The maximum number of requests allowed by host domain at the
     *            same time.
     * @return The associated rule.
     */
    public void addHostDomainConcurrencyCounter(int limit) {
        FirewallCounterRule rule = new ConcurrentFirewallCounterRule(
                new HostDomainCountingPolicy());
        rule.addHandler(new BlockingHandler(new UniqueLimitPolicy(limit)));
        add(rule);
    }

    /**
     * Returns a rule that limits the number of requests for a given period of
     * time by request's host domain.
     * 
     * @param period
     *            The period of time.
     * @param periodUnit
     *            Period time unit associated to the {@link FirewallCounterRule}.
     * @param limit
     *            The maximum number of requests allowed by host domain for the
     *            given period of time.
     * @return The associated rule.
     */
    public void addHostDomainPeriodicCounter(int period, TimeUnit periodUnit,
                                             int limit) {
        FirewallCounterRule rule = new PeriodicFirewallCounterRule(period,
                periodUnit, new HostDomainCountingPolicy());
        rule.addHandler(new BlockingHandler(new UniqueLimitPolicy(limit)));
    }

    /**
     * Returns a rule that forbids access to the given set of IP addresses.
     *
     * @param blackList
     *            The list of rejected IP addresses.
     * @return The associated rule.
     */
    public void addIpAddressesBlackList(List<String> blackList) {
        add(new FirewallIpFilteringRule(blackList, false));
    }

    /**
     * Returns a rule that restricts access according to the IP address of the
     * request's client. A unique limit is applied for all IP addresses.
     * 
     * @param limit
     *            The maximum number of accepted concurrent requests.
     * @return The associated rule.
     */
    public void addIpAddressesConcurrencyCounter(int limit) {
        FirewallCounterRule rule = new ConcurrentFirewallCounterRule(
                new IpAddressCountingPolicy());
        rule.addHandler(new BlockingHandler(new UniqueLimitPolicy(limit)));
        add(rule);
    }

    /**
     * Returns a rule that restricts access by period of time according to the
     * IP address of the request's client. A unique limit is applied for all IP
     * addresses.
     * 
     * @param period
     *            The period of time.
     * @param periodUnit
     *            Period time unit associated to the {@link FirewallCounterRule}.
     * @param limit
     *            The maximum number of accepted requests for a period of time.
     * @return The associated rule.
     */
    public void addIpAddressesPeriodicCounter(int period, TimeUnit periodUnit,  int limit) {
        FirewallCounterRule rule = new PeriodicFirewallCounterRule(period, periodUnit,
                new IpAddressCountingPolicy());
        rule.addHandler(new BlockingHandler(new UniqueLimitPolicy(limit)));
        add(rule);
    }

    /**
     * Returns a rule that restricts access to the given set of IP addresses.
     *
     * @param whiteList
     *            The list of accepted IP addresses.
     * @return The associated rule.
     */
    public void addIpAddressesWhiteList(List<String> whiteList) {
        add(new FirewallIpFilteringRule(whiteList, true));

    }

    /**
     * Returns a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case the access is forbidden.
     * 
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * 
     * @return The associated rule.
     */
    public void addRolesConcurrencyCounter(Map<String, Integer> limitsPerRole) {
        addRolesConcurrencyCounter(limitsPerRole, 0);
    }

    /**
     * Returns a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case a default limit is applied.
     * 
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * @param defaultLimit
     *            The limit assigned for any other roles, or for user without
     *            assigned role.
     * @return The associated rule.
     */
    public void addRolesConcurrencyCounter(Map<String, Integer> limitsPerRole,
            int defaultLimit) {
        FirewallCounterRule rule = new ConcurrentFirewallCounterRule(
                new UserCountingPolicy());
        rule.addHandler(new BlockingHandler(new RoleLimitPolicy(limitsPerRole,
                defaultLimit)));
        add(rule);
    }

    /**
     * Returns a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of requests by period of time, in any other case the access is forbidden.
     * 
     * @param period
     *            The period of time.
     * @param periodUnit
     *            Period time unit associated to the {@link FirewallCounterRule}.
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * 
     * @return The associated rule.
     */
    public void addRolesPeriodicCounter(int period, TimeUnit periodUnit,
                                        Map<String, Integer> limitsPerRole) {
        addRolesPeriodicCounter(period, periodUnit, limitsPerRole, 0);
    }

    /**
     * Returns a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case a default limit is applied.
     * 
     * @param period
     *            The period of time.
     * @param periodUnit
     *            Period time unit associated to the {@link FirewallCounterRule}.
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * @param defaultLimit
     *            The limit assigned for any other roles, or for user without
     *            assigned role.
     * @return The associated rule.
     */
    public void addRolesPeriodicCounter(int period, TimeUnit periodUnit,
                                        Map<String, Integer> limitsPerRole, int defaultLimit) {
        FirewallCounterRule rule = new PeriodicFirewallCounterRule(period,
                periodUnit, new UserCountingPolicy());
        rule.addHandler(new BlockingHandler(new RoleLimitPolicy(limitsPerRole,
                defaultLimit)));
        add(rule);
    }

    @Override
    public Filter createInboundFilter(Context context) {
        return new FirewallFilter(context, this.rules);
    }

}
