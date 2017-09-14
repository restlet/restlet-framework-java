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

package org.restlet.ext.apispark;

import org.restlet.ext.apispark.internal.firewall.handler.BlockingHandler;
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
import org.restlet.security.Role;
import org.restlet.security.User;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Configuration methods for the Firewall.
 * 
 * @author Guillaume Blondeau
 */
public class FirewallConfig {

    /** The list of associated {@link FirewallRule}. */
    private List<FirewallRule> rules;

    /**
     * Private Constructor.
     */
    @SuppressWarnings("unchecked")
    public FirewallConfig(List<?> rules) {
        // does not expose in Javadoc FirewallRule
        this.rules = (List<FirewallRule>) rules;
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
     */
    public void addHostDomainConcurrencyCounter(int limit) {
        FirewallCounterRule rule = new ConcurrentFirewallCounterRule(
                new HostDomainCountingPolicy());
        rule.addHandler(new BlockingHandler(new UniqueLimitPolicy(limit)));
        add(rule);
    }

    /**
     * Adds a rule that limits the number of requests for a given period of time
     * by request's host domain.
     * 
     * @param period
     *            The period of time.
     * @param periodUnit
     *            Period time unit associated to the rule.
     * @param limit
     *            The maximum number of requests allowed by host domain for the
     *            given period of time.
     */
    public void addHostDomainPeriodicCounter(int period, TimeUnit periodUnit,
            int limit) {
        FirewallCounterRule rule = new PeriodicFirewallCounterRule(period,
                periodUnit, new HostDomainCountingPolicy());
        rule.addHandler(new BlockingHandler(new UniqueLimitPolicy(limit)));
    }

    /**
     * Adds a rule that forbids access to the given set of IP addresses.
     * 
     * @param blackList
     *            The list of rejected IP addresses.
     */
    public void addIpAddressesBlackList(List<String> blackList) {
        add(new FirewallIpFilteringRule(blackList, false));
    }

    /**
     * Adds a rule that restricts access according to the IP address of the
     * request's client. A unique limit is applied for all IP addresses.
     * 
     * @param limit
     *            The maximum number of accepted concurrent requests.
     */
    public void addIpAddressesConcurrencyCounter(int limit) {
        FirewallCounterRule rule = new ConcurrentFirewallCounterRule(
                new IpAddressCountingPolicy());
        rule.addHandler(new BlockingHandler(new UniqueLimitPolicy(limit)));
        add(rule);
    }

    /**
     * Adds a rule that restricts access by period of time according to the IP
     * address of the request's client. A unique limit is applied for all IP
     * addresses.
     * 
     * @param period
     *            The period of time.
     * @param periodUnit
     *            Period time unit associated to the rule.
     * @param limit
     *            The maximum number of accepted requests for a period of time.
     */
    public void addIpAddressesPeriodicCounter(int period, TimeUnit periodUnit,
            int limit) {
        FirewallCounterRule rule = new PeriodicFirewallCounterRule(period,
                periodUnit, new IpAddressCountingPolicy());
        rule.addHandler(new BlockingHandler(new UniqueLimitPolicy(limit)));
        add(rule);
    }

    /**
     * Adds a rule that restricts access to the given set of IP addresses.
     * 
     * @param whiteList
     *            The list of accepted IP addresses.
     */
    public void addIpAddressesWhiteList(List<String> whiteList) {
        add(new FirewallIpFilteringRule(whiteList, true));
    }

    /**
     * Adds a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case the access is forbidden.
     * 
     * @param limitsPerRole
     *            The limit assigned per role's name.
     */
    public void addRolesConcurrencyCounter(Map<String, Integer> limitsPerRole) {
        addRolesConcurrencyCounter(limitsPerRole, 0);
    }

    /**
     * Adds a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case a default limit is applied.
     * 
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * @param defaultLimit
     *            The limit assigned for any other roles, or for user without
     *            assigned role.
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
     * Adds a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of requests by period of time, in any other case the access is forbidden.
     * 
     * @param period
     *            The period of time.
     * @param periodUnit
     *            Period time unit associated to the rule.
     * @param limitsPerRole
     *            The limit assigned per role's name.
     */
    public void addRolesPeriodicCounter(int period, TimeUnit periodUnit,
            Map<String, Integer> limitsPerRole) {
        addRolesPeriodicCounter(period, periodUnit, limitsPerRole, 0);
    }

    /**
     * Adds a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case a default limit is applied.
     * 
     * @param period
     *            The period of time.
     * @param periodUnit
     *            Period time unit associated to the rule.
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * @param defaultLimit
     *            The limit assigned for any other roles, or for user without
     *            assigned role.
     */
    public void addRolesPeriodicCounter(int period, TimeUnit periodUnit,
            Map<String, Integer> limitsPerRole, int defaultLimit) {
        FirewallCounterRule rule = new PeriodicFirewallCounterRule(period,
                periodUnit, new UserCountingPolicy());
        rule.addHandler(new BlockingHandler(new RoleLimitPolicy(limitsPerRole,
                defaultLimit)));
        add(rule);
    }
}
