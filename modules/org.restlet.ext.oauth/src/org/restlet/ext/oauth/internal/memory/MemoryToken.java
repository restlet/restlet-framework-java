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

package org.restlet.ext.oauth.internal.memory;

import org.restlet.ext.oauth.internal.ServerToken;

/**
 * Memory implementation of Token interface.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class MemoryToken implements ServerToken {

    private String accessToken;

    private String clientId;

    private int expirePeriod;

    private String refreshToken;

    private String[] scope;

    private final long timestamp;

    private String tokenType;

    private String username;

    protected MemoryToken() {
        timestamp = System.currentTimeMillis();
    }

    /**
     * @return the accessToken
     */
    public String getAccessToken() {
        return accessToken;
    }

    /**
     * @return the clientId
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * @return the expirePeriod
     */
    public int getExpirePeriod() {
        return expirePeriod;
    }

    /**
     * @return the refreshToken
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    /**
     * @return the scope
     */
    public String[] getScope() {
        return scope;
    }

    /**
     * @return the tokenType
     */
    public String getTokenType() {
        return tokenType;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    public boolean isExpired() {
        long elapsedTime = System.currentTimeMillis() - timestamp;
        long timeout = expirePeriod;
        if ((elapsedTime / 1000) > timeout) {
            return true;
        }
        return false;
    }

    /**
     * @param accessToken
     *            the accessToken to set
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    /**
     * @param clientId
     *            the clientId to set
     */
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    /**
     * @param expirePeriod
     *            the expirePeriod to set
     */
    public void setExpirePeriod(int expirePeriod) {
        this.expirePeriod = expirePeriod;
    }

    /**
     * @param refreshToken
     *            the refreshToken to set
     */
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    /**
     * @param scope
     *            the scope to set
     */
    public void setScope(String[] scope) {
        this.scope = scope;
    }

    /**
     * @param tokenType
     *            the tokenType to set
     */
    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    /**
     * @param username
     *            the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }
}
