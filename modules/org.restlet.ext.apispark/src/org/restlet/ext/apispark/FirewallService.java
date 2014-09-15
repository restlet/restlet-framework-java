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

import java.util.List;
import java.util.Map;

import org.restlet.Context;
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
    /** The underlying instance of {@link FirewallFilter}. */
    private FirewallFilter firewall;
    
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
        FirewallUtils.addHostDomainConcurrencyCounter(firewall, limit);
    }

    /**
     * Adds a rule that limits the number of requests for a given period of
     * time by request's host domain.
     * 
     * @param period
     *            The period of time.
     * @param limit
     *            The maximum number of requests allowed by host domain for the
     *            given period of time.
     * @return The associated rule.
     */
    public void addHostDomainPeriodicCounter(int period, int limit) {
        FirewallUtils.addHostDomainPeriodicCounter(firewall, period, limit);
    }

    /**
     * Adds a rule that forbids access to the given set of IP addresses.
     * 
     * @param blackList
     *            The list of rejected IP adresses.
     * @return The associated rule.
     */
    public void addIpAddressesBlackList(List<String> blackList) {
        FirewallUtils.addIpAddressesBlackList(firewall, blackList);
    }

    /**
     * Adds a rule that restricts access according to the IP address of the
     * request's client. A unique limit is applied for all IP addresses.
     * 
     * @param limit
     *            The maximum number of accepted concurrent requests.
     * @return The associated rule.
     */
    public void addIpAddressesConcurrencyCounter(int limit) {
        FirewallUtils.addIpAddressesConcurrencyCounter(firewall, limit);
    }

    /**
     * Adds a rule that restricts access by period of time according to the
     * IP address of the request's client. A unique limit is applied for all IP
     * addresses.
     * 
     * @param period
     *            The period of time.
     * @param limit
     *            The maximum number of accepted requests for a period of time.
     * @return The associated rule.
     */
    public void addIpAddressesPeriodicCounter(int period, int limit) {
        FirewallUtils.addIpAddressesPeriodicCounter(firewall, period, limit);
    }

    /**
     * Adds a rule that restricts access to the given set of IP addresses.
     * 
     * @param whiteList
     *            The list of accepted IP adresses.
     * @return The associated rule.
     */
    public void addIpAddressesWhiteList(List<String> whiteList) {
        FirewallUtils.addIpAddressesWhiteList(firewall, whiteList);
    }

    /**
     * Adds a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case the access is forbidden.
     * 
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * 
     * @return The associated rule.
     */
    public void addRolesConcurrencyCounter(Map<String, Integer> limitsPerRole) {
        FirewallUtils.addRolesConcurrencyCounter(firewall, limitsPerRole);
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
     * @return The associated rule.
     */
    public void addRolesConcurrencyCounter(Map<String, Integer> limitsPerRole,
            int defaultLimit) {
        FirewallUtils.addRolesConcurrencyCounter(firewall, limitsPerRole, defaultLimit);
    }

    /**
     * Adds a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of requests by period of time, in any other case the access is forbidden.
     * 
     * @param period
     *            The period of time.
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * 
     * @return The associated rule.
     */
    public void addRolesPeriodicCounter(int period,
            Map<String, Integer> limitsPerRole) {
        FirewallUtils.addRolesPeriodicCounter(firewall, period, limitsPerRole);
    }

    /**
     * Adds a rule that restricts access according to the {@link Role} of the
     * current authenticated {@link User}. Each role is defined a limit in terms
     * of concurrent requests, in any other case a default limit is applied.
     * 
     * @param period
     *            The period of time.
     * @param limitsPerRole
     *            The limit assigned per role's name.
     * @param defaultLimit
     *            The limit assigned for any other roles, or for user without
     *            assigned role.
     * @return The associated rule.
     */
    public void addRolesPeriodicCounter(int period,
            Map<String, Integer> limitsPerRole, int defaultLimit) {
        FirewallUtils.addRolesPeriodicCounter(firewall, period, limitsPerRole, defaultLimit);
    }

    @Override
    public Filter createInboundFilter(Context context) {
        firewall = new FirewallFilter(context);
        return firewall;
    }

}
