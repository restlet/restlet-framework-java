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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.oauth;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

/**
 * Client resource used to acquire an OAuth token. Implements OAuth 2.0
 * (RFC6749)
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class AccessTokenClientResource extends ClientResource implements
        OAuthResourceDefs {

    private static class TokenResponse implements Token {

        public static TokenResponse parseResponse(JSONObject result)
                throws JSONException {
            TokenResponse token = new TokenResponse();
            token.accessToken = result.getString(ACCESS_TOKEN);
            token.tokenType = result.getString(TOKEN_TYPE);
            if (result.has(EXPIRES_IN)) {
                token.expirePeriod = result.getInt(EXPIRES_IN);
            }
            if (result.has(REFRESH_TOKEN)) {
                token.refreshToken = result.getString(REFRESH_TOKEN);
            }
            if (result.has(SCOPE)) {
                token.scope = Scopes.parseScope(result.getString(SCOPE));
            }
            return token;
        }

        private String accessToken;

        private Integer expirePeriod;

        private String refreshToken;

        private String[] scope;

        private String tokenType;

        public String getAccessToken() {
            return accessToken;
        }

        public int getExpirePeriod() {
            if (expirePeriod == null) {
                throw new IllegalStateException("expires_in not included.");
            }
            return expirePeriod;
        }

        public String getRefreshToken() {
            return refreshToken;
        }

        public String[] getScope() {
            return scope;
        }

        public String getTokenType() {
            return tokenType;
        }

        @SuppressWarnings("unused")
        public boolean isExpirePeriodAvailable() {
            return expirePeriod != null;
        }
    }

    private ChallengeScheme authenticationScheme;

    private String clientId;

    private String clientSecret;

    public AccessTokenClientResource(Reference tokenURI) {
        super(tokenURI);
        // set default scheme
        authenticationScheme = ChallengeScheme.HTTP_BASIC;
    }

    @Override
    public void doError(Status errorStatus) {
        Representation representation = getResponse().getEntity();
        if (representation.getMediaType().equals(MediaType.APPLICATION_JSON)) {
            // Do not throw an exception here.
            getLogger().fine("OAuth response is found.");
            // XXX: after #doError, the representation will disposed in
            // #handleInbound.
            return;
        }
        // ResourceException will be thrown.
        super.doError(errorStatus);
    }

    // We override to not dispose the OAuth error json body.
    @Override
    public Representation handleInbound(Response response) {
        Representation result = null;

        // Verify that the request was synchronous
        if (response.getRequest().isSynchronous()) {
            if (response.getStatus().isError()) {
                doError(response.getStatus());
                // DO NOT DISPOSE THE RESPONSE.
            }/* else { */
            result = (response == null) ? null : response.getEntity();
            /* } */
        }

        return result;
    }

    public Token requestToken(OAuthParameters parameters)
            throws OAuthException, IOException, JSONException {
        if (authenticationScheme == null) {
            // Use Body method
            setupBodyClientCredentials(parameters);
        } else {
            setChallengeResponse(authenticationScheme, clientId, clientSecret);
        }

        Representation input = parameters.toRepresentation();

        getClientInfo().getAcceptedMediaTypes().add(
                new Preference<MediaType>(MediaType.APPLICATION_JSON));

        JSONObject result = new JsonRepresentation(post(input)).getJsonObject();

        if (result.has(ERROR)) {
            throw OAuthException.toOAuthException(result);
        }

        TokenResponse token = TokenResponse.parseResponse(result);
        if (token.scope == null) {
            // Should be identical to the scope requested by the client.
            token.scope = Scopes.parseScope(parameters.toForm().getFirstValue(
                    SCOPE));
        }

        return token;
    }

    public void setAuthenticationMethod(ChallengeScheme scheme) {
        this.authenticationScheme = scheme;
    }

    public void setClientCredentials(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    protected void setupBodyClientCredentials(OAuthParameters parameters) {
        parameters.add(CLIENT_ID, clientId);
        if (clientSecret != null) {
            parameters.add(CLIENT_SECRET, clientSecret);
        }
    }
}
