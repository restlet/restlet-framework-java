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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.restlet.ext.apispark.internal.firewall.handler.BlockingHandler;
import org.restlet.ext.apispark.internal.firewall.handler.policy.PerValueLimitPolicy;
import org.restlet.ext.apispark.internal.firewall.handler.policy.RoleLimitPolicy;
import org.restlet.ext.apispark.internal.firewall.handler.policy.UniqueLimitPolicy;
import org.restlet.ext.apispark.internal.firewall.rule.ConcurrentFirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.FirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.PeriodicFirewallCounterRule;
import org.restlet.ext.apispark.internal.firewall.rule.policy.HostDomainCountingPolicy;
import org.restlet.ext.apispark.internal.firewall.rule.policy.IpAddressCountingPolicy;
import org.restlet.ext.apispark.internal.firewall.rule.policy.UserCountingPolicy;
import org.restlet.security.Role;
import org.restlet.security.User;

/**
 * Helps to generate firewall rules.
 * 
 * @author Guillaume Blondeau
 */
public class FirewallUtils {

    /**
     * Adds a rule that limits the number of concurrent requests by request's
     * host domain.
     * 
     * @param firewall
     *            The firewall.
     * @param limit
     *            The maximum number of requests allowed by host domain at the
     *            same time.
     * @return The associated rule.
     */
    public static void addHostDomainConcurrencyCounter(FirewallFilter firewall,
            int limit) {
        FirewallCounterRule rule = new ConcurrentFirewallCounterRule(
                new HostDomainCountingPolicy());
        rule.addHandler(new BlockingHandler(new UniqueLimitPolicy(limit)));
        firewall.add(rule);
    }

    /**
     * Returns a rule that limits the number of requests for a given period of
     * time by request's host domain.
     * 
     * @param firewall
     *            The firewall.
     * @param period
     *            The period of time.
     * @param limit
     *            The maximum number of requests allowed by host domain for the
     *            given period of time.
     * @return The associated rule.
     */
    public static void addHostDomainPeriodicCounter(FirewallFilter firewall,
            int period, int limit) {
        FirewallCounterRule rule = new PeriodicFirewallCounterRule(period,
                new HostDomainCountingPolicy());
        rule.addHandler(new BlockingHandler(new UniqueLimitPolicy(limit)));
        firewall.add(rule);
    }

    /**
     * Returns a rule that forbids access to the given set of IP addresses.
     * 
     * @param firewall
     *            The firewall.
     * @param blackList
     *            The list of rejected IP adresses.
     * @return The associated rule.
     */
    public static void addIpAddressesBlackList(FirewallFilter firewall,
            List<String> blackList) {
        FirewallCounterRule rule = new ConcurrentFirewallCounterRule(
                new IpAddressCountingPolicy());
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (String ip : blackList) {
            map.put(ip, 0);
        }
        rule.addHandler(new BlockingHandler(new PerValueLimitPolicy(map,
                Integer.MAX_VALUE)));
        firewall.add(rule);
    }

    /**
     * Returns a rule that restricts access according to the IP address of the
     * request's client. A unique limit is applied for all IP addresses.
     * 
     * @param firewall
     *            The firewall.
     * @param limit
     *            The maximum number of accepted concurrent requests.
     * @return The associated rule.
     */
    public static void addIpAddressesConcurrencyCounter(
            FirewallFilter firewall, int limit) {
        FirewallCounterRule rule = new ConcurrentFirewallCounterRule(
                new IpAddressCountingPolicy());
        rule.addHandler(new BlockingHandler(new UniqueLimitPolicy(limit)));
        firewall.add(rule);
    }

    /**
     * Returns a rule that restricts access by period of time according to the
     * IP address of the request's client. A unique limit is applied for all IP
     * addresses.
     * 
     * @param firewall
     *            The firewall.
     * @param period
     *            The period of time.
     * @param limit
     *            The maximum number of accepted requests for a period of time.
     * @return The associated rule.
     */
    public static void addIpAddressesPeriodicCounter(FirewallFilter firewall,
            int period, int limit) {
        FirewallCounterRule rule = new PeriodicFirewallCounterRule(period,
                new IpAddressCountingPolicy());
        rule.addHandler(new BlockingHandler(new UniqueLimitPolicy(limit)));
        firewall.add(rule);
    }

    /**
     * Returns a rule that restricts access to the given set of IP addresses.
     * 
     * @param firewall
     *            The firewall.
     * @param whiteList
     *            The list of accepted IP adresses.
     * @return The associated rule.
     */
    public static void addIpAddressesWhiteList(FirewallFilter firewall,
            List<String> whiteList) {
        FirewallCounterRule rule = new ConcurrentFirewallCounterRule(
                new IpAddressCountingPolicy());
        Map<String, Integer> map = new HashMap<String, Integer>();
        for (String ip : whiteList) {
            map.put(ip, Integer.MAX_VALUE);
        }
        rule.addHandler(new BlockingHandler(new PerValueLimitPolicy(map, 0)));
        firewall.add(rule);
    }

    /**
     * Returns a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case the access is forbidden.
     * 
     * @param firewall
     *            The firewall.
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * 
     * @return The associated rule.
     */
    public static void addRolesConcurrencyCounter(FirewallFilter firewall,
            Map<String, Integer> limitsPerRole) {
        addRolesConcurrencyCounter(firewall, limitsPerRole, 0);
    }

    /**
     * Returns a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case a default limit is applied.
     * 
     * @param firewall
     *            The firewall.
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * @param defaultLimit
     *            The limit assigned for any other roles, or for user without
     *            assigned role.
     * @return The associated rule.
     */
    public static void addRolesConcurrencyCounter(FirewallFilter firewall,
            Map<String, Integer> limitsPerRole, int defaultLimit) {
        FirewallCounterRule rule = new ConcurrentFirewallCounterRule(
                new UserCountingPolicy());
        rule.addHandler(new BlockingHandler(new RoleLimitPolicy(limitsPerRole,
                defaultLimit)));
        firewall.add(rule);
    }

    /**
     * Returns a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of requests by period of time, in any other case the access is forbidden.
     * 
     * @param firewall
     *            The firewall.
     * @param period
     *            The period of time.
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * 
     * @return The associated rule.
     */
    public static void addRolesPeriodicCounter(FirewallFilter firewall,
            int period, Map<String, Integer> limitsPerRole) {
        addRolesPeriodicCounter(firewall, period, limitsPerRole, 0);
    }

    /**
     * Returns a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case a default limit is applied.
     * 
     * @param firewall
     *            The firewall.
     * @param period
     *            The period of time.
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * @param defaultLimit
     *            The limit assigned for any other roles, or for user without
     *            assigned role.
     * @return The associated rule.
     */
    public static void addRolesPeriodicCounter(FirewallFilter firewall,
            int period, Map<String, Integer> limitsPerRole, int defaultLimit) {
        FirewallCounterRule rule = new PeriodicFirewallCounterRule(period,
                new UserCountingPolicy());
        rule.addHandler(new BlockingHandler(new RoleLimitPolicy(limitsPerRole,
                defaultLimit)));
        firewall.add(rule);
    }

}
