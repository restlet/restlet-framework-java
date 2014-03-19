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
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet
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

    public static final int RESEED_TOKENS = 1000;

    public static final int DEFAULT_TOKEN_EXPIRE_PERIOD = 3600;

    private SecureRandom random;

    private int expirePeriod = DEFAULT_TOKEN_EXPIRE_PERIOD;

    private boolean updateRefreshToken = true;

    private volatile int count = 0;

    public AbstractTokenManager() {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    protected String generateRawCode() {
        StringBuilder raw = new StringBuilder(generate(20));
        raw.append('|').append(System.currentTimeMillis());
        return raw.toString();
    }

    protected String generateRawToken() {
        return generate(40);
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

    public Token generateToken(Client client, String[] scope)
            throws OAuthException {
        return generateToken(client, null, scope);
    }

    public Token findToken(Client client) {
        return findToken(client, null);
    }

    public void revokeToken(Client client) {
        revokeToken(client, null);
    }

    /**
     * @return the expirePeriod
     */
    public int getExpirePeriod() {
        return expirePeriod;
    }

    /**
     * @param expirePeriod
     *            the expirePeriod to set
     */
    public void setExpirePeriod(int expirePeriod) {
        this.expirePeriod = expirePeriod;
    }

    /**
     * @return the updateRefreshToken
     */
    public boolean isUpdateRefreshToken() {
        return updateRefreshToken;
    }

    /**
     * @param updateRefreshToken
     *            the updateRefreshToken to set
     */
    public void setUpdateRefreshToken(boolean updateRefreshToken) {
        this.updateRefreshToken = updateRefreshToken;
    }
}
