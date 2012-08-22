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

package org.restlet.ext.oauth.internal;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.restlet.ext.oauth.AuthenticatedUser;
import org.restlet.ext.oauth.internal.memory.ExpireToken;
import org.restlet.ext.oauth.internal.memory.UnlimitedToken;

/**
 * Class that controls the generation of code, token and refresh token.
 * 
 * @author Kristoffer Gronowski
 */
public abstract class TokenGenerator {

    private volatile SecureRandom random;

    protected volatile long maxTokenTimeSec;

    private static final int tokens = 1000;

    private volatile int count = 0;

    public TokenGenerator() {
        try {
            random = SecureRandom.getInstance("SHA1PRNG");

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        }

    }

    /**
     * 
     * @param user
     *            the authenticated user that the code is generated for.
     * @return the generated code
     */

    public String generateCode(AuthenticatedUser user) {
        StringBuilder raw = new StringBuilder(generate(20));
        raw.append('|').append(System.currentTimeMillis());
        String code = raw.toString();

        user.setCode(code);

        return code;
    }

    /**
     * 
     * @param user
     *            the authenticated user that the token is generated for.
     * @param expire
     *            grater then zero if not unlimited time token.
     * @return generated Token or ExpireToken if expire was set.
     */

    public Token generateToken(AuthenticatedUser user, long expire) {
        long individualExp = user.getTokenExpire();
        if (individualExp > 0) {
            expire = individualExp;
        }
        expire = (expire <= maxTokenTimeSec) ? expire : maxTokenTimeSec;

        StringBuilder raw = new StringBuilder(generate(40));
        // raw.append('|').append(System.currentTimeMillis());
        String token = raw.toString();

        Token t = null;
        if (expire != Token.UNLIMITED) {
            t = new ExpireToken(token, expire, generate(20), user);
        } else { // Unlimited token
            t = new UnlimitedToken(token, user);
        }

        return t;
    }

    /**
     * Calculated the number of seconds to expirey. If unlimited token
     * Long.MAX_VALUE is returned
     * 
     * @param token
     *            to be calculated
     * @return delta time to expirey
     */

    public long expiresInSec(Token token) {
        if (token instanceof ExpireToken) {
            ExpireToken et = (ExpireToken) token;
            ScheduledFuture<?> sf = et.getFuture();
            if (sf != null)
                return sf.getDelay(TimeUnit.SECONDS);
        }
        return Long.MAX_VALUE; // TODO create unit test
    }

    /**
     * 
     * @param maxTokenTimeSec
     *            longest period a token should be valid for.
     */
    public void setMaxTokenTime(long maxTokenTimeSec) {
        this.maxTokenTimeSec = maxTokenTimeSec;
    }

    /**
     * Refreshes a token throwing away the old one and generating a new
     * 
     * @param token
     *            to be refreshed
     */

    public void refreshToken(ExpireToken token) {
        // clean up the old one
        revokeToken(token);

        token.expireToken();

        String newToken = generate(20); // generate new timed token
        token.setToken(newToken); // Add new token to token object
    }

    /**
     * Exchanges a code for a token. Used by the web server flow after callback
     * 
     * @param code
     *            to be exchange for a token.
     * @param expire
     *            if the generated token should have expiry value set.
     * @return new token
     * @throws IllegalArgumentException
     *             if the code is not valid or found.
     */
    public abstract Token exchangeForToken(String code, long expire)
            throws IllegalArgumentException;

    /**
     * Revoke the token so that it should no longer be used.
     * 
     * @param token
     *            to revoke.
     */
    public abstract void revokeToken(Token token);

    /**
     * Revoke the expire token so that it should no longer be used.
     * 
     * @param token
     *            to revoke.
     */
    public abstract void revokeExpireToken(ExpireToken token);

    /**
     * Try to find a token. Implementing class will search for it in the data
     * storage.
     * 
     * @param token
     *            string to search for.
     * @return Token or ExpireToken object if found or null.
     */

    public abstract Token findToken(String token);

    protected String generate(int len) {
        if (count++ > tokens) {
            count = 0;
            random.setSeed(random.generateSeed(20));
        }
        byte[] token = new byte[len];
        random.nextBytes(token);
        return toHex(token);
    }

    protected String toHex(byte[] input) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < input.length; i++) {
            String d = Integer
                    .toHexString(new Byte(input[i]).intValue() & 0xFF);
            if (d.length() == 1)
                sb.append('0');
            sb.append(d);
        }
        return sb.toString();
    }
}
