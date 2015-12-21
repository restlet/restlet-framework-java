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

package org.restlet.example.ext.oauth.mongo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.restlet.ext.oauth.OAuthError;
import org.restlet.ext.oauth.OAuthException;
import org.restlet.ext.oauth.OAuthResourceDefs;
import org.restlet.ext.oauth.internal.AbstractTokenManager;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.Token;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * MongoDB implementation of TokenManager interface.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class MongoTokenManager extends AbstractTokenManager implements
        OAuthResourceDefs {

    private DBCollection tokens;

    private DBCollection sessions;

    public MongoTokenManager(DB db) {
        tokens = db.getCollection("tokens");
        sessions = db.getCollection("sessions");
    }

    public Token generateToken(Client client, String username, String[] scope)
            throws OAuthException {
        DBObject token = tokens.findOne(createQuery(client, username));
        if (token == null) {
            token = new BasicDBObject();
            token.put(CLIENT_ID, client.getClientId());
            if (username != null) {
                token.put(USERNAME, username);
            }
        }

        token.put(SCOPE, Arrays.asList(scope));
        token.put(EXPIRES_IN, getExpirePeriod());
        token.put(TOKEN_TYPE, TOKEN_TYPE_BEARER);
        token.put(ACCESS_TOKEN, generateRawToken());
        token.put(REFRESH_TOKEN, generateRawToken());
        token.put(MongoToken.TIMESTAMP,
                (int) (System.currentTimeMillis() / 1000));

        // Perform Upsert
        tokens.ensureIndex(new BasicDBObject(ACCESS_TOKEN, "1"),
                new BasicDBObject("unique", true));
        tokens.ensureIndex(new BasicDBObject(REFRESH_TOKEN, "1"),
                new BasicDBObject("unique", true));
        tokens.save(token);

        return new MongoToken(token);
    }

    @SuppressWarnings("unchecked")
    public Token refreshToken(Client client, String refreshToken, String[] scope)
            throws OAuthException {
        DBObject token = tokens.findOne(new BasicDBObject(REFRESH_TOKEN,
                refreshToken));
        if (token == null) {
            throw new OAuthException(OAuthError.invalid_grant,
                    "Invalid refresh token.", null);
        }

        // ensure that the refresh token was issued to the authenticated client
        if (!token.get(CLIENT_ID).equals(client.getClientId())) {
            throw new OAuthException(OAuthError.invalid_grant,
                    "The refresh token was not issued to the client.", null);
        }

        /*
         * The requested scope MUST NOT include any scope not originally granted
         * by the resource owner, and if omitted is treated as equal to the
         * scope originally granted by the resource owner. (6. Refreshing an
         * Access Token)
         */
        if (scope != null && scope.length != 0) {
            List<String> newScopeList = Arrays.asList(scope);

            if (!((List<String>) token.get(SCOPE)).containsAll(newScopeList)) {
                throw new OAuthException(
                        OAuthError.invalid_scope,
                        "The requested scope is exceeds the scope granted by the resource owner.",
                        null);
            }

            token.put(SCOPE, newScopeList);
        }

        token.put(ACCESS_TOKEN, generateRawToken());

        if (isUpdateRefreshToken()) {
            token.put(REFRESH_TOKEN, generateRawToken());
        }

        // Perform Upsert
        tokens.ensureIndex(new BasicDBObject(ACCESS_TOKEN, "1"),
                new BasicDBObject("unique", true));
        tokens.ensureIndex(new BasicDBObject(REFRESH_TOKEN, "1"),
                new BasicDBObject("unique", true));
        tokens.save(token);

        return new MongoToken(token);
    }

    private DBObject createQuery(Client client, String username) {
        BasicDBObject query = new BasicDBObject(CLIENT_ID, client.getClientId());
        if (username != null) {
            query.append(USERNAME, username);
        } else {
            query.append(USERNAME, new BasicDBObject("$exists", false));
        }
        return query;
    }

    public String storeSession(AuthSession session) throws OAuthException {
        BasicDBObject sessionObj = new BasicDBObject();

        Map<String, Object> map = session.toMap();
        for (String key : map.keySet()) {
            sessionObj.put(key, map.get(key));
        }

        String code = generateRawCode();
        sessionObj.put("_id", code);

        sessions.insert(sessionObj);

        return code;
    }

    @SuppressWarnings("unchecked")
    public AuthSession restoreSession(String code) throws OAuthException {
        DBObject sessionObj = sessions.findOne(new BasicDBObject("_id", code));

        if (sessionObj == null) {
            throw new OAuthException(OAuthError.invalid_grant, "Invalid code.",
                    null);
        }

        return AuthSession.toAuthSession((Map<String, Object>) sessionObj
                .toMap());
    }

    public Token validateToken(String accessToken) throws OAuthException {
        DBObject token = tokens.findOne(new BasicDBObject(ACCESS_TOKEN,
                accessToken));

        if (token == null) {
            throw new OAuthException(OAuthError.invalid_token,
                    "The access token revoked.", null);
        }

        MongoToken tokenImpl = new MongoToken(token);

        if (tokenImpl.isExpired()) {
            throw new OAuthException(OAuthError.invalid_token,
                    "The access token expired.", null);
        }

        return tokenImpl;
    }

    public Token findToken(Client client, String username) {
        DBObject token = tokens.findOne(createQuery(client, username));
        if (token == null) {
            return null;
        }
        return new MongoToken(token);
    }

    public Token[] findTokens(String username) {
        DBCursor cursor = tokens.find(new BasicDBObject(USERNAME, username));
        ArrayList<Token> list = new ArrayList<Token>();
        while (cursor.hasNext()) {
            DBObject token = cursor.next();
            list.add(new MongoToken(token));
        }
        return list.toArray(new Token[list.size()]);
    }

    public Token[] findTokens(Client client) {
        DBCursor cursor = tokens.find(new BasicDBObject(CLIENT_ID, client
                .getClientId()));
        ArrayList<Token> list = new ArrayList<Token>();
        while (cursor.hasNext()) {
            DBObject token = cursor.next();
            list.add(new MongoToken(token));
        }
        return list.toArray(new Token[list.size()]);
    }

    public void revokeToken(Client client, String username) {
        tokens.remove(createQuery(client, username));
    }

    public void revokeAllTokens(String username) {
        tokens.remove(new BasicDBObject(USERNAME, username));
    }

    public void revokeAllTokens(Client client) {
        tokens.remove(new BasicDBObject(CLIENT_ID, client.getClientId()));
    }
}
