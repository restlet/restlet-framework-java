/**
 * Copyright 2005-2011 Noelios Technologies.
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

import java.util.concurrent.ScheduledFuture;

/**
 * Token that can be set to expire at a given point in time. This behavior
 * should be controlled by the token generator
 * 
 * @author Kristoffer Gronowski
 * @see TokenGenerator#generateToken(AuthenticatedUser, long)
 * @see TokenGenerator#refreshToken(ExpireToken)
 */
public class ExpireToken extends Token {
    private final long expireTime;

    private final String refreshToken;
    private String token;
    
    private AuthenticatedUser user;

    private ScheduledFuture<?> future; // can be used to clean up

    public ExpireToken(String refreshToken, long expTimeSec, String token,
            AuthenticatedUser user) {
        this.refreshToken = refreshToken;
        this.token = token;
        this.user = user;
        expireTime = expTimeSec;
    }

    public void setFuture(ScheduledFuture<?> future) {
        this.future = future;
    }

    public ScheduledFuture<?> getFuture() {
        return future;
    }

    public long getExpirePeriod() {
        return expireTime;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void expireToken() {
        if (future != null)
            future.cancel(false);
        token = null;
    }
    
    public AuthenticatedUser getUser() {
		return user;
	}

	public String getToken() {
		return token;
	}
    
    void setToken(String token) {
        this.token = token;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ExpireToken) {
            ExpireToken t = (ExpireToken) obj;
            return (refreshToken.equals(t.refreshToken))
                    && (expireTime == t.expireTime);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return refreshToken.hashCode();
    }
}
