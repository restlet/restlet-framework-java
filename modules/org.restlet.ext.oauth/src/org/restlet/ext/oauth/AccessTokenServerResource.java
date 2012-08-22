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

package org.restlet.ext.oauth;


import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.CacheDirective;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.ext.oauth.internal.memory.ExpireToken;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.SecretVerifier;
import org.restlet.security.User;

/**
 * Server resource used to acquire an OAuth token. A code, or refresh token can
 * be exchanged for a working token. This resource also supports the none flow.
 * 
 * Note: at the moment Client Credentials Grant is not supported.
 * Implements OAuth 2.0 draft 30
 * 
 * Example. Attach an AccessTokenServerResource
 * <pre>
 * {
 *      &#064;code
 *      public Restlet createInboundRoot(){
 *              ...
 *              root.attach(&quot;/token&quot;, AccessTokenServerResource.class);
 *              ...
 *      }
 * }
 * </pre>
 * 
 * <b>Originally written by Kristoffer Gronowski, Heavily modified for update to draft30.</b>
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-30#section-3.2">OAuth
 *      2 draft 30</a>
 */
public class AccessTokenServerResource extends OAuthServerResource {

    /**
     * Handles the {@link Post} request.
     * The client MUST use the HTTP "POST" method
     * when making access token requests. (3.2. Token Endpoint)
     * 
     * @param input HTML form formated token request per oauth-v2 spec.
     * @return JSON response with token or error.
     */
    @Post("form:json")
    public Representation requestToken(Representation input) throws OAuthException {
        getLogger().fine("Grant request");
        final Form params = new Form(input);
        
        User clientCredential = getRequest().getClientInfo().getUser();
        if (clientCredential == null) {
            getLogger().warning("Client ID is missing! No Authenticator?");
            throw new OAuthException(OAuthError.server_error, "No Client Credential.", null);
        }
        
        Client client = clients.findById(clientCredential.getIdentifier());
        getLogger().fine("Requested by authenticated client " + client.getClientId());
        final GrantType grantType = getGrantType(params);
        switch (grantType) {
        case authorization_code:
            getLogger().info("Authorization Code Grant");
            return doAuthCodeFlow(client, params);
        case password:
            getLogger().info("Resource Owner Password Credentials Grant");
            return doPasswordFlow(client, params);
        case refresh_token:
            getLogger().info("Refreshing an Access Token");
            return doRefreshFlow(client, params);
        default:
            getLogger().warning("Unsupported flow: " + grantType);
            throw new OAuthException(OAuthError.unsupported_grant_type, "Flow not supported", null);
        }
    }
    
    /**
     * Handle errors as described in 5.2 Error Response.
     * @param t 
     */
    @Override
    protected void doCatch(Throwable t) {
        final OAuthException oex;
        if (t instanceof OAuthException) {
            oex = (OAuthException) t;
        } else if (t.getCause() instanceof OAuthException) {
            oex = (OAuthException) t.getCause();
        } else {
            oex = new OAuthException(OAuthError.server_error, t.getMessage(), null);
        }
        // The authorization server responds with an HTTP 400 (Bad Request)
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        getResponse().setEntity(getErrorJsonDocument(oex));
    }
    
    /**
     * Get request parameter "grant_type".
     * 
     * @param params
     * @return
     * @throws OAuthException 
     */
    protected GrantType getGrantType(Form params) throws OAuthException {
        String typeString = params.getFirstValue(GRANT_TYPE);
        getLogger().info("Type: " + typeString);
        try {
            GrantType type = Enum.valueOf(GrantType.class, typeString);
            getLogger().fine("Found flow - " + type);
            return type;
        } catch (IllegalArgumentException iae) {
            throw new OAuthException(OAuthError.unsupported_grant_type, "Unsupported flow", null);
        } catch (NullPointerException npe) {
            throw new OAuthException(OAuthError.invalid_request, "No grant_type parameter found.", null);
        }
    }
    
    /**
     * Get request parameter "code".
     * 
     * @param params
     * @return
     * @throws OAuthException 
     */
    protected String getCode(Form params) throws OAuthException {
        String code = params.getFirstValue(CODE);
        if (code == null || code.isEmpty()) {
            throw new OAuthException(OAuthError.invalid_request,
                    "Mandatory parameter code is missing", null);
        }
        return code;
    }
    
    /**
     * Get request parameter "username".
     * 
     * @param params
     * @return
     * @throws OAuthException 
     */
    protected String getUsername(Form params) throws OAuthException {
        String username = params.getFirstValue(USERNAME);
        if (username == null || username.isEmpty()) {
            throw new OAuthException(OAuthError.invalid_request,
                    "Mandatory parameter username is missing", null);
        }
        return username;
    }
    
    /**
     * Get request parameter "password".
     * 
     * @param params
     * @return
     * @throws OAuthException 
     */
    protected String getPassword(Form params) throws OAuthException {
        String password = params.getFirstValue(PASSWORD);
        if (password == null || password.isEmpty()) {
            throw new OAuthException(OAuthError.invalid_request,
                    "Mandatory parameter password is missing", null);
        }
        return password;
    }
    
    /**
     * Get request parameter "refresh_token".
     * 
     * @param params
     * @return
     * @throws OAuthException 
     */
    protected String getRefreshToken(Form params) throws OAuthException {
        String token = params.getFirstValue(REFRESH_TOKEN);
        if (token == null || token.isEmpty()) {
            throw new OAuthException(OAuthError.invalid_request,
                    "Mandatory parameter refresh_token is missing", null);
        }
        return token;
    }
    
    /**
     * Converts a {@link Token} to its equivalent as a {@link JSONObject}.
     * 
     * @param token
     *            The token.
     * @param scopes
     *            The list of scopes.
     * @return An instance of {@link Token} equivalent to the given token.
     * @throws ResourceException
     */
    private JSONObject createJsonToken(Token token, String scopes)
            throws ResourceException {
        JSONObject body = new JSONObject();

        try {
            body.put(TOKEN_TYPE, TOKEN_TYPE_BEARER);
            body.put(ACCESS_TOKEN, token.getToken());
            long expiresIn = token.getExpirePeriod();
            if (expiresIn != Token.UNLIMITED) {
                body.put(EXPIRES_IN, expiresIn);
                body.put(REFRESH_TOKEN, token.getRefreshToken());
            }
            if (scopes != null && !scopes.isEmpty()) {
                body.put(SCOPE, scopes);
            }
        } catch (JSONException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Failed to generate JSON", e);
        }

        return body;
    }

    /**
     * Executes the authentication flow.
     * 
     * @param clientId
     *            The client identifier.
     * @param clientSecret
     *            The client's secret.
     * @param params
     *            The authentication parameters.
     * @return The result of the flow.
     * @throws IllegalArgumentException
     */
    private Representation doAuthCodeFlow(Client client, Form params) throws IllegalArgumentException, OAuthException {
        // TODO could add a cookie match on the owner but could fail if code is
        // sent to other entity
        // unauthorized_client, right now this is only performed if
        // ScopedResource getOwner returns the user
        String code = getCode(params);
        // TODO: ensure the authorization code was issued to the client.
        // TODO: redirect_uri

        // 5 min timeout on tokens, 0 for unlimited
        Token token = generator.exchangeForToken(code, tokenTimeSec);

        // TODO send back scopes if limited

        JSONObject body = createJsonToken(token, null);

        // Sets the no-store Cache-Control header
        addCacheDirective(getResponse(), CacheDirective.noStore());
        // TODO: Set Pragma: no-cache
        return new JsonRepresentation(body);
    }

    /**
     * Executes the "password" flow.
     * 
     * @param clientId
     *            The client identifier.
     * @param clientSecret
     *            The client's secret.
     * @param params
     *            The authentication parameters.
     * @return The result of the flow.
     */
    private Representation doPasswordFlow(Client client, Form params) throws OAuthException {
        AuthenticatedUser user = client.findUser(getUsername(params));
        if (user == null) {
            throw new OAuthException(OAuthError.invalid_request,
                    "Authenticated user not found.", null);
        }

        String password = getPassword(params);
        if (!SecretVerifier.compare(user.getPassword(), password.toCharArray())) {
            throw new OAuthException(OAuthError.invalid_grant,
                    "Password not correct.", null);
        }

        Token token = this.generator.generateToken(user, this.tokenTimeSec);
        JSONObject body = createJsonToken(token, null); // Scopes N/A

        // Sets the no-store Cache-Control header
        addCacheDirective(getResponse(), CacheDirective.noStore());
        // TODO: Set Pragma: no-cache
        return new JsonRepresentation(body);
    }

    /**
     * Executes the "refresh token" flow.
     * 
     * @param clientId
     *            The client identifier.
     * @param clientSecret
     *            The client's secret.
     * @param params
     *            The authentication parameters.
     * @return The result of the flow.
     */
    private Representation doRefreshFlow(Client client, Form params) throws OAuthException {
        Token token = generator.findToken(getRefreshToken(params));

        if ((token != null) && (token instanceof ExpireToken)) {
            AuthenticatedUser user = token.getUser();

            // Make sure that the user owning the token is owned by this client
            if (client.containsUser(user.getId())) {
                // refresh the token
                generator.refreshToken((ExpireToken) token);

                JSONObject body = createJsonToken(token, null); // Scopes N/A

                // Sets the no-store Cache-Control header
                addCacheDirective(getResponse(), CacheDirective.noStore());
                // TODO: Set Pragma: no-cache
                return new JsonRepresentation(body);
            } else { // error not owner
                // XXX:
                throw new OAuthException(OAuthError.unauthorized_client, "User does not match.", null);

            }
        } else { // error no such token.
            // XXX:
            throw new OAuthException(OAuthError.invalid_grant, "Refresh token.", null);
        }
    }
}
