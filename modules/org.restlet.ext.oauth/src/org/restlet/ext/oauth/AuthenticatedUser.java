/**
 * Copyright 2005-2012 Restlet S.A.S.
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
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.oauth;

import java.util.List;

import org.restlet.ext.oauth.internal.Token;
import org.restlet.security.Role;

/**
 * POJO for keeping a grant that a user has approved. User with a specific id
 * has granted a set of scopes.
 * 
 * Implementors should implement the storage and retrieval.
 * 
 * @author Kristoffer Gronowski
 */
public abstract class AuthenticatedUser {

    /**
     * Adds a scope for this user given a specified owner.
     */
    // @Deprecated
    // public abstract void addScope(String scope, String owner);

    public abstract void addRole(Role r, String owner);

    /**
     * Removes a generated code that was used or revoked.
     */
    public abstract void clearCode();

    /**
     * Gets the client object that associated and created this user. The Client
     * corresponds to a service provider that acts on behalf of a Authenticated
     * user.
     * 
     * @return The parent client instance.
     */
    public abstract Client getClient();

    /**
     * Returns the current oauth code if any available for exchange for a token.
     * 
     * @return the current oauth code if any available for exchange for a token.
     */
    public abstract String getCode();

    /**
     * Gets all scopes. Observe that no owner information is passed.
     */
    public abstract List<Role> getGrantedRoles();

    /**
     * Returns the identifier of the user.
     * 
     * @return The identifier of the user.
     */
    public abstract String getId();

    /**
     * Password field for the username and password oauth flow.
     * 
     * @return password or null if not present
     */
    // TODO should be a char[]
    // cf http://restlet.tigris.org/issues/show_bug.cgi?id=276
    public abstract String getPassword();

    /**
     * Returns the currently issued token for this user.
     * 
     * @return The currently issued token for this user.
     */
    public abstract Token getToken();

    /**
     * Returns the default token expire time for this user.
     * 
     * @return The default token expire time for this user.
     */

    public abstract long getTokenExpire();

    /**
     * Checks if this user has a specific scope.
     * 
     * @param role
     *            The scope to check.
     * @param owner
     *            The owner.
     * @return True if this user has the scope.
     */
    public abstract boolean isGrantedRole(Role role, String owner);

    /**
     * Helper method to indicate when to checkpoint the user data. If not
     * handling permanent persistence should return true.
     * 
     * @return true if stored - false if the caller wants to abort
     */
    public boolean persist() {
        return true;
    }

    /**
     * Removes a specific scope.
     * 
     * @param role
     *            The scope to be removed.
     * @param owner
     *            The scope owner.
     */
    public abstract void revokeRole(Role role, String owner);

    /**
     * Revokes previously granted scopes.
     */
    public abstract void revokeRoles();

    /**
     * Sets a generated code that was given out for this user.
     * 
     * @param code
     *            The generated code.
     */
    public abstract void setCode(String code);

    /**
     * Sets the user password
     * 
     * @param password
     *            The user password.
     */
    // TODO should be a char[]
    // cf http://restlet.tigris.org/issues/show_bug.cgi?id=276
    public abstract void setPassword(String password);

    /**
     * Sets the current issued token.
     * 
     * @param token
     *            The current issued token.
     */
    public abstract void setToken(Token token);

    /**
     * Sets the time for all token expire time for this user.
     * 
     * @param deltaTimeSec
     *            The time for all token expire time for this user.
     */
    public abstract void setTokenExpire(long deltaTimeSec);

}
