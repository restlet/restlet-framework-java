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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.restlet.ext.oauth.OAuthError;
import org.restlet.ext.oauth.OAuthException;
import org.restlet.ext.oauth.OAuthResourceDefs;
import org.restlet.ext.oauth.internal.AbstractTokenManager;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.Token;

/**
 * Memory implementation of TokenManager interface.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class MemoryTokenManager extends AbstractTokenManager {

    private final Map<String, AuthSession> sessions = new ConcurrentHashMap<String, AuthSession>();

    private final Map<String, MemoryToken> tokens = new ConcurrentHashMap<String, MemoryToken>();

    public Token findToken(Client client, String username) {
        for (MemoryToken token : tokens.values()) {
            if (token.getClientId().equals(client.getClientId())
                    && ((username == null && token.getUsername() == null) || (username != null && username
                            .equals(token.getUsername())))) {
                return token;
            }
        }
        return null;
    }

    protected MemoryToken findTokenByRefreshToken(String refreshToken) {
        for (MemoryToken token : tokens.values()) {
            if (token.getRefreshToken().equals(refreshToken)) {
                return token;
            }
        }
        return null;
    }

    public Token[] findTokens(Client client) {
        ArrayList<Token> list = new ArrayList<Token>();
        for (MemoryToken token : tokens.values()) {
            if (/* !token.isExpired() && */
            token.getClientId().equals(client.getClientId())) {
                list.add(token);
            }
        }
        return list.toArray(new Token[list.size()]);
    }

    public Token[] findTokens(String username) {
        ArrayList<Token> list = new ArrayList<Token>();
        for (MemoryToken token : tokens.values()) {
            if (/* !token.isExpired() && */
            token.getUsername() != null && token.getUsername().equals(username)) {
                list.add(token);
            }
        }
        return list.toArray(new Token[list.size()]);
    }

    public Token generateToken(Client client, String username, String[] scope)
            throws OAuthException {
        revokeToken(client, username);
        MemoryToken token = new MemoryToken();
        token.setClientId(client.getClientId());
        token.setUsername(username);
        token.setScope(scope);
        token.setExpirePeriod(getExpirePeriod());
        token.setTokenType(OAuthResourceDefs.TOKEN_TYPE_BEARER);
        token.setAccessToken(generateRawToken());
        token.setRefreshToken(generateRawToken());
        tokens.put(token.getAccessToken(), token);
        return token;
    }

    public Token refreshToken(Client client, String refreshToken, String[] scope)
            throws OAuthException {
        MemoryToken token = findTokenByRefreshToken(refreshToken);
        if (token == null) {
            throw new OAuthException(OAuthError.invalid_grant,
                    "Invalid refresh token.", null);
        }

        // ensure that the refresh token was issued to the authenticated client
        if (!token.getClientId().equals(client.getClientId())) {
            throw new OAuthException(OAuthError.invalid_grant,
                    "The refresh token was not issued to the client.", null);
        }

        String[] newScope;
        /*
         * The requested scope MUST NOT include any scope not originally granted
         * by the resource owner, and if omitted is treated as equal to the
         * scope originally granted by the resource owner. (6. Refreshing an
         * Access Token)
         */
        if (scope == null || scope.length == 0) {
            newScope = token.getScope();
        } else {
            String[] previousScope = token.getScope();
            if (!Arrays.asList(previousScope).containsAll(Arrays.asList(scope))) {
                throw new OAuthException(
                        OAuthError.invalid_scope,
                        "The requested scope is exceeds the scope granted by the resource owner.",
                        null);
            }
            newScope = scope;
        }

        MemoryToken newToken = new MemoryToken();
        newToken.setClientId(client.getClientId());
        newToken.setUsername(token.getUsername());
        newToken.setScope(newScope);
        newToken.setExpirePeriod(token.getExpirePeriod());
        newToken.setTokenType(OAuthResourceDefs.TOKEN_TYPE_BEARER);
        newToken.setAccessToken(generateRawToken());
        if (isUpdateRefreshToken()) {
            newToken.setRefreshToken(generateRawToken());
        } else {
            newToken.setRefreshToken(token.getRefreshToken());
        }

        synchronized (this) {
            if (tokens.remove(token.getAccessToken()) != null) {
                tokens.put(newToken.getAccessToken(), newToken);
                return newToken;
            }
        }

        return null; // FIXME
    }

    public AuthSession restoreSession(String code) throws OAuthException {
        AuthSession session = sessions.remove(code);
        if (session == null) {
            throw new OAuthException(OAuthError.invalid_grant, "Invalid code.",
                    null);
        }
        return session;
    }

    public void revokeAllTokens(Client client) {
        for (Token token : findTokens(client)) {
            tokens.remove(token.getAccessToken());
        }
    }

    public void revokeAllTokens(String username) {
        for (Token token : findTokens(username)) {
            tokens.remove(token.getAccessToken());
        }
    }

    public void revokeToken(Client client, String username) {
        Token token = findToken(client, username);
        if (token != null) {
            tokens.remove(token.getAccessToken());
        }
    }

    public String storeSession(AuthSession session) throws OAuthException {
        String code = generateRawCode();
        sessions.put(code, session);
        return code;
    }

    public Token validateToken(String accessToken) throws OAuthException {
        MemoryToken token = tokens.get(accessToken);
        if (token == null) {
            throw new OAuthException(OAuthError.invalid_token,
                    "The access token revoked.", null);
        }
        if (token.isExpired()) {
            throw new OAuthException(OAuthError.invalid_token,
                    "The access token expired.", null);
        }
        return token;
    }
}
