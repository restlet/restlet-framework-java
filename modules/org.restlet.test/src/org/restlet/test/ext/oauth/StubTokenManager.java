/**
 * Copyright 2005-2013 Restlet S.A.S.
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
package org.restlet.test.ext.oauth;

import org.restlet.ext.oauth.OAuthError;
import org.restlet.ext.oauth.OAuthException;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.RedirectionURI;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.ext.oauth.internal.TokenManager;

/**
 *
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class StubTokenManager extends OAuthTestBase implements TokenManager {
    
    public Token generateToken(Client client, String username, String[] scope) throws OAuthException {
        return STUB_TOKEN;
    }

    public Token generateToken(Client client, String[] scope) throws OAuthException {
        return STUB_TOKEN;
    }

    public Token refreshToken(Client client, String refreshToken, String[] scope) throws OAuthException {
        if (!refreshToken.equals(STUB_REFRESH_TOKEN)) {
            throw new OAuthException(OAuthError.invalid_grant, "Invalid refresh token.", null);
        }
        return STUB_TOKEN;
    }

    public String storeSession(AuthSession session) throws OAuthException {return null;}

    public AuthSession restoreSession(String code) throws OAuthException {
        if (!code.equals(STUB_CODE)) {
            throw new OAuthException(OAuthError.invalid_grant, "Invalid code.", null);
        }
        AuthSession session = AuthSession.newAuthSession();
        session.setRedirectionURI(new RedirectionURI("http://localhost:8080/dummy"));
        session.setClientId(STUB_CLIENT_ID);
        session.setScopeOwner(STUB_USERNAME);
        session.setRequestedScope(Scopes.parseScope("a b c"));
        session.setGrantedScope(Scopes.parseScope("a b"));
        return session;
    }

    public Token validateToken(String accessToken) throws OAuthException {
        if (!accessToken.equals(STUB_ACCESS_TOKEN)) {
            throw new OAuthException(OAuthError.invalid_token, "Invalid Token", null);
        }
        return STUB_TOKEN;
    }

    public Token findToken(Client client, String username) {return null;}

    public Token findToken(Client client) {return null;}

    public Token[] findTokens(String username) {return null;}

    public Token[] findTokens(Client client) {return null;}

    public void revokeToken(Client client, String username) {}

    public void revokeToken(Client client) {}

    public void revokeAllTokens(String username) {}

    public void revokeAllTokens(Client client) {}
}
