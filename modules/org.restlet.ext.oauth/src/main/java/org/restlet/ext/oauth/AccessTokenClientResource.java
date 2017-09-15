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

package org.restlet.ext.oauth;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Response;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

/**
 * Client resource used to acquire an OAuth token. Implements OAuth 2.0
 * (RFC6749).<br>
 * Use or override the {@link #requestToken(OAuthParameters)} method to retrieve the token.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class AccessTokenClientResource extends ClientResource implements OAuthResourceDefs {

    private static class TokenResponse implements Token {

        public static TokenResponse parseResponse(JSONObject result) throws JSONException {
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

    public AccessTokenClientResource(Reference tokenUri) {
        super(tokenUri);
        // set default scheme
        authenticationScheme = ChallengeScheme.HTTP_BASIC;
    }

    /**
     * Handles the inbound call. Note that only synchronous calls are processed.<br>
     * Overrides the {@link ClientResource#handleInbound(Response)} method in order to return the OAuth error entity.
     * That is to say in case of OAuth error response, no {@link ResourceException} is thrown, whereas the initial
     * behaviour of {@link ClientResource} is to throw such exception.
     * 
     * @param response
     * @return The response's entity, if any.
     */
    @Override
    public Representation handleInbound(Response response) {
        try {
            return super.handleInbound(response);
        } catch (ResourceException e) {
            if (getResponse().isEntityAvailable()
                    && MediaType.APPLICATION_JSON.equals(getResponseEntity().getMediaType())) {
                // Do not throw an exception here.
                getLogger().fine("OAuth response is found.");
                return getResponseEntity();
            }

            throw e;
        }
    }

    /**
     * Returns the OAuth token by requesting the remote authorization server.
     * 
     * @param parameters
     *            The credentials, in case of they are sent via the entity of the request.
     * @return The OAuth token.
     * @throws OAuthException
     * @throws IOException
     * @throws JSONException
     */
    public Token requestToken(OAuthParameters parameters)
            throws OAuthException, IOException, JSONException {
        if (authenticationScheme == null) {
            // Use Body method
            setupBodyClientCredentials(parameters);
        } else {
            setChallengeResponse(authenticationScheme, clientId, clientSecret);
        }

        Representation input = parameters.toRepresentation();

        accept(MediaType.APPLICATION_JSON);

        JSONObject result = new JsonRepresentation(post(input)).getJsonObject();

        if (result.has(ERROR)) {
            throw OAuthException.toOAuthException(result);
        }

        TokenResponse token = TokenResponse.parseResponse(result);
        if (token.scope == null) {
            // Should be identical to the scope requested by the client.
            token.scope = Scopes.parseScope(parameters.toForm().getFirstValue(SCOPE));
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

    /**
     * By default adds the clientId and clientSecret (if any) values to the set of Oauth parameters.<br>
     * Called from {@link #requestToken(OAuthParameters)} method.
     * 
     * @param parameters
     *            The set of Oauth parameters to complete.
     */
    protected void setupBodyClientCredentials(OAuthParameters parameters) {
        parameters.add(CLIENT_ID, clientId);
        if (clientSecret != null) {
            parameters.add(CLIENT_SECRET, clientSecret);
        }
    }
}
