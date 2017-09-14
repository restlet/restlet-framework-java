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
import java.util.List;
import java.util.Map;

import org.restlet.Request;
import org.restlet.security.Authenticator;
import org.restlet.security.Enroler;
import org.restlet.security.Role;

/**
 * Defines a limit to authenticated users based on their {@link Role}. Use this
 * policy in conjunction with an {@link Enroler} (cf {@link Authenticator}) in
 * order to associate roles to users.
 * 
 * @author Guillaume Blondeau
 */
public class RoleLimitPolicy extends LimitPolicy {

    /**
     * The default limit applied when the request's user has no role or his
     * roles are not contained in {@link RoleLimitPolicy#limitsPerRole}.
     */
    private int defaultLimit;

    /** Maps a role name to a limit. */
    private Map<String, Integer> limitsPerRole;

    /**
     * Constructor.<br>
     * Defines only the {@link RoleLimitPolicy#defaultLimit} to 0.
     */
    public RoleLimitPolicy() {
        this(new HashMap<String, Integer>(), 0);
    }

    /**
     * Constructor.<br>
     * Defines only the {@link RoleLimitPolicy#defaultLimit}.
     * 
     * @param defaultLimit
     *            The default limit.
     */
    public RoleLimitPolicy(int defaultLimit) {
        this(new HashMap<String, Integer>(), defaultLimit);
    }

    /**
     * Constructor.<br>
     * Set the {@link RoleLimitPolicy#defaultLimit} to 0.
     * 
     * @param limitsPerRole
     *            Maps role's name to a limit.
     */
    public RoleLimitPolicy(Map<String, Integer> limitsPerRole) {
        this(limitsPerRole, 0);
    }

    /**
     * Constructor.
     * 
     * @param limitsPerRole
     *            Maps role's name to a limit.
     * @param defaultLimit
     *            The default limit applied when the incoming user has no role
     *            or any of his roles has been associated to a limit.
     */
    public RoleLimitPolicy(Map<String, Integer> limitsPerRole, int defaultLimit) {
        this.limitsPerRole = limitsPerRole;
        this.defaultLimit = defaultLimit;
    }

    /**
     * Specifies a limit for a role.
     * 
     * @param role
     *            The name of the role.
     * @param limit
     *            The associated limit.
     */
    public void addRole(String role, int limit) {
        limitsPerRole.put(role, limit);
    }

    /**
     * Returns the policy's default limit.
     * 
     * @return Policy's default limit.
     */
    public int getDefaultLimit() {
        return defaultLimit;
    }

    /**
     * Returns the highest limit associated to the user's roles.
     */
    @Override
    public int getLimit(Request request, String countedValue) {
        // TODO we don't rely on the counted value?
        int result = 0;
        List<Role> roles = request.getClientInfo().getRoles();
        // iterate over user's roles
        for (Role role : roles) {
            if (limitsPerRole.containsKey(role.getName())
                    && (limitsPerRole.get(role.getName()) > result)) {
                result = limitsPerRole.get(role.getName());
            }
        }

        if (result == 0) {
            result = defaultLimit;
        }

        return result;
    }

    /**
     * Returns the {@link Map} defining limits corresponding to different
     * {@link Role}
     * 
     * @return Limits corresponding to different {@link Role}
     */
    public Map<String, Integer> getLimitsPerRole() {
        return limitsPerRole;
    }

    /**
     * Set the policy's default limit.
     * 
     * @param defaultLimit
     *            Policy's default limit.
     */
    public void setDefaultLimit(int defaultLimit) {
        this.defaultLimit = defaultLimit;
    }

    /**
     * Set the {@link Map} defining limits corresponding to different
     * {@link Role}
     * 
     * @param limitsPerRole
     *            {@link Map} defining limits corresponding to different
     *            {@link Role}
     */
    public void setLimitsPerRole(Map<String, Integer> limitsPerRole) {
        this.limitsPerRole = limitsPerRole;
    }

}
