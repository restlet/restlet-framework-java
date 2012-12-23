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

package org.restlet.ext.oauth.internal.memory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.restlet.ext.oauth.AuthenticatedUser;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.ext.oauth.internal.TokenGenerator;

/**
 * TokenGenerator implementation that keeps tokens in memory. Note that tokens
 * generated will not be persisted after a JVM restart.
 * 
 * @author Kristoffer Gronowski
 */
public class MemTokenGenerator extends TokenGenerator {

    private final Map<String, AuthenticatedUser> codeStore = new ConcurrentHashMap<String, AuthenticatedUser>();

    // Used only to store tokens
    private final Map<String, Token> tokenStore = new ConcurrentHashMap<String, Token>();

    private final ScheduledThreadPoolExecutor timers;

    public MemTokenGenerator(ScheduledThreadPoolExecutor executor) {
        timers = executor;
    }

    @Override
    public String generateCode(AuthenticatedUser user) {
        String code = super.generateCode(user);

        // Store the code for later use
        AuthenticatedUser oldValue = codeStore.put(code, user);
        // Something is wrong in the code generation!
        // log("WARNIG - bad generation ALG!");
        if (oldValue != null)
            oldValue.setCode(code);

        return code;
    }

    @Override
    public Token generateToken(AuthenticatedUser user, long expire) {
        Token t = super.generateToken(user, expire);
        if (expire != Token.UNLIMITED) {
            ExpireToken et = (ExpireToken) t;
            // RefreshToken is stored twice for faster lookup
            // One key is the token while the other the refreshToken
            tokenStore.put(et.getRefreshToken(), et);
            // Add Token to timeout mechanism
            scheduleCleanup(et, expire);
        }

        tokenStore.put(t.getToken(), t);

        return t;
    }

    @Override
    public Token exchangeForToken(String code, long expire)
            throws IllegalArgumentException {
        // TODO handle exp tokens
        AuthenticatedUser user = codeStore.remove(code);
        // if( expire > 0 ) user.setExpire(expire);
        if (user == null)
            throw new IllegalArgumentException("Code not valid");
        Token t = generateToken(user, expire);
        user.clearCode(); // TODO could also match if the user code matches
                          // codestore
        return t;
    }

    @Override
    public void revokeToken(Token token) {
        String id = token.getToken();
        if (id != null && tokenStore.containsKey(id)) {
            tokenStore.remove(token.getToken());
            // t.getUser().removeToken(t);
        }
    }

    @Override
    public void revokeExpireToken(ExpireToken token) {
        if (tokenStore.containsKey(token.getRefreshToken())) {
            Token t = tokenStore.remove(token.getRefreshToken());
            revokeToken(t); // Also clean pending tokens
            // t.getUser().removeToken(t);
        }
    }

    @Override
    public Token findToken(String token) {
        return tokenStore.get(token);
    }

    @Override
    public void refreshToken(ExpireToken token) {
        super.refreshToken(token);

        scheduleCleanup(token, token.getExpirePeriod());
        // Store the new generated token in DB
        tokenStore.put(token.getToken(), token);
    }

    private void scheduleCleanup(final ExpireToken et, long expire) {
        Runnable r = new Runnable() {
            public void run() {
                String token = et.getToken();
                // Remove the binding
                tokenStore.remove(token);
                // Set the token to null until refreshed
                et.expireToken();

            }
        };
        ScheduledFuture<?> future = timers
                .schedule(r, expire, TimeUnit.SECONDS);
        et.setFuture(future);
    }
}
