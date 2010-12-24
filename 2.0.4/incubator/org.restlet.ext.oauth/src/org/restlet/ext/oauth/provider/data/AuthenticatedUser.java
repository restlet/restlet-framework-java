/**
 * Copyright 2005-2010 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.oauth.provider.data;

/**
 * POJO for keeping a grant that a user has approved. User with a specific id
 * has granted a set of scopes.
 * 
 * Implementors should implement the storage and retrieval.
 * 
 * @author Kristoffer Gronowski
 */
public interface AuthenticatedUser {

    /**
     * @return user id of the user
     */
    String getId();

    // void addToken(Token token);

    /**
     * Add a generated code that was given out for this user
     */
    void addCode(String code);

    /**
     * Remove a generated code that was used or revoked.
     */
    void removeCode(String code);

    /**
     * Add a scope for this user given a specified owner
     */
    public void addScope(String scope, String owner);

    /**
     * Check if this user has a specific scope
     */
    public boolean isGrantedScope(String scope, String owner);

    /**
     * Remove a specific scope
     */
    public void revokeScope(String scope, String owner);

    /**
     * Get all scopes. Observe that no owner information is passed.
     */
    public String[] getGrantedScopes();

    public long getTokenExpire();

    public void setTokenExpire(long deltaTimeSec);

    /**
     * Revoke previously granted scopes.
     */
    public void revokeScopes();

}
