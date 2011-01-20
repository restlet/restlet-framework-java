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
public abstract class AuthenticatedUser {

    /**
     * @return user id of the user
     */
	public abstract String getId();

	/**
	 * 
	 * @return the current oauth code if any available for exchange for a token
	 */
	public abstract String getCode();
	
    /**
     * Set a generated code that was given out for this user
     */
	public abstract void setCode(String code);

    /**
     * Remove a generated code that was used or revoked.
     */
	public abstract void clearCode();

    /**
     * Add a scope for this user given a specified owner
     */
    public abstract void addScope(String scope, String owner);

    /**
     * Check if this user has a specific scope
     */
    public abstract boolean isGrantedScope(String scope, String owner);

    /**
     * Remove a specific scope
     */
    public abstract void revokeScope(String scope, String owner);

    /**
     * Get all scopes. Observe that no owner information is passed.
     */
    public abstract String[] getGrantedScopes();
    
    /**
     * 
     * @return the default token expire time for this user
     */

    public abstract long getTokenExpire();
    
    /**
     * 
     * @param deltaTimeSec time for all token expire time for this user
     */

    public abstract void setTokenExpire(long deltaTimeSec);

    /**
     * Revoke previously granted scopes.
     */
    public abstract void revokeScopes();
    
    /**
     * 
     * @return the currently issued token for this user
     */
    
    public abstract Token getToken();
    
    /**
     * 
     * @param token sets the current issued token
     */
    
    public abstract void setToken(Token token);
    
    /**
     * Password field for the username and password oauth flow
     * 
     * @return password or null if not present
     */
    
    public abstract String getPassword();
    
    /**
     * Set the user password
     * @param password
     */
    
    public abstract void setPassword(String password);
    
    /**
     * Gets the client object that associated and created this user.
     * The CLient corresponds to a service provider that acts on 
     * behalf of a Authenticated user.
     * 
     * @return parent client instance
     */
    public abstract Client getClient();
    
    /**
     * Helper method to indicate when to checkpoint the user data.
     * If not handeling permanent persistance should return true.
     * 
     * @return true if stored - false if the caller wants to abort
     */
    public boolean persist() {
    	return true;
    }

}
