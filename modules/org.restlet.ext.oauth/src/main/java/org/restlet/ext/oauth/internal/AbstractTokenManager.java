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

package org.restlet.ext.oauth.internal;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import org.restlet.ext.oauth.OAuthException;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public abstract class AbstractTokenManager implements TokenManager {

    public static final int DEFAULT_TOKEN_EXPIRE_PERIOD = 3600;

    public static final int RESEED_TOKENS = 1000;

    protected static String toHex(byte[] input) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length; i++) {
            String d = Integer
                    .toHexString(new Byte(input[i]).intValue() & 0xFF);
            if (d.length() == 1) {
                sb.append('0');
            }
            sb.append(d);
        }
        return sb.toString();
    }

    private volatile int count = 0;

    private int expirePeriod = DEFAULT_TOKEN_EXPIRE_PERIOD;

    private SecureRandom random;

    private boolean updateRefreshToken = true;

    public AbstractTokenManager() {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    public Token findToken(Client client) {
        return findToken(client, null);
    }

    protected String generate(int len) {
        if (count++ > RESEED_TOKENS) {
            count = 0;
            random.setSeed(random.generateSeed(20));
        }
        byte[] token = new byte[len];
        random.nextBytes(token);
        return toHex(token);
    }

    protected String generateRawCode() {
        StringBuilder raw = new StringBuilder(generate(20));
        raw.append('|').append(System.currentTimeMillis());
        return raw.toString();
    }

    protected String generateRawToken() {
        return generate(40);
    }

    public Token generateToken(Client client, String[] scope)
            throws OAuthException {
        return generateToken(client, null, scope);
    }

    /**
     * @return the expirePeriod
     */
    public int getExpirePeriod() {
        return expirePeriod;
    }

    /**
     * @return the updateRefreshToken
     */
    public boolean isUpdateRefreshToken() {
        return updateRefreshToken;
    }

    public void revokeToken(Client client) {
        revokeToken(client, null);
    }

    /**
     * @param expirePeriod
     *            the expirePeriod to set
     */
    public void setExpirePeriod(int expirePeriod) {
        this.expirePeriod = expirePeriod;
    }

    /**
     * @param updateRefreshToken
     *            the updateRefreshToken to set
     */
    public void setUpdateRefreshToken(boolean updateRefreshToken) {
        this.updateRefreshToken = updateRefreshToken;
    }
}
