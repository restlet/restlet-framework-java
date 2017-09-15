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

import java.util.logging.Level;

import javax.naming.AuthenticationException;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.CacheDirective;
import org.restlet.data.Form;
import org.restlet.data.Status;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.engine.util.StringUtils;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.internal.AuthSession;
import org.restlet.ext.oauth.internal.AuthSessionTimeoutException;
import org.restlet.ext.oauth.internal.Client;
import org.restlet.ext.oauth.internal.Client.ClientType;
import org.restlet.ext.oauth.internal.ResourceOwnerManager;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.security.User;

/**
 * Server resource used to acquire an OAuth token. A code, or refresh token can
 * be exchanged for a working token.
 * 
 * Implements OAuth 2.0 (RFC6749)
 * 
 * Example. Attach an AccessTokenServerResource
 * 
 * <pre>
 * {
 *      &#064;code
 *      public Restlet createInboundRoot() {
 *              ...
 *              root.attach(&quot;/token&quot;, AccessTokenServerResource.class);
 *              ...
 *      }
 * }
 * </pre>
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 * @author Kristoffer Gronowski
 * 
 * @see <a href="http://tools.ietf.org/html/rfc6749#section-3.2">OAuth 2.0 (3.2. Token Endpoint)</a>
 */
public class AccessTokenServerResource extends OAuthServerResource {

    /**
     * Executes the "authorization_code" flow. (4.1.3. Access Token Request).
     * 
     * @param parameters
     * @return The JSON representation of the response of the "authorization_code" flow.
     * @throws OAuthException
     * @throws JSONException
     */
    private Representation doAuthCodeFlow(Form parameters) throws OAuthException, JSONException {
        // The flow requires authenticated client.
        Client client = getAuthenticatedClient();
        if (client == null) {
            // Use the public client. (4.1.3. Access Token Request)
            client = getClient(parameters);
        }

        ensureGrantTypeAllowed(client, GrantType.authorization_code);

        String code = getCode(parameters);

        // Ensure that the authorization code was issued to the authenticated confidential client, or if the client is
        // public, ensure that the code was issued to "client_id" in the request, (4.1.3. Access Token Request)
        AuthSession session = tokens.restoreSession(code);
        if (!client.getClientId().equals(session.getClientId())) {
            throw new OAuthException(OAuthError.invalid_grant, "The code was not issued to the client.", null);
        }

        try {
            // Ensure that the session is not timeout.
            session.updateActivity();
        } catch (AuthSessionTimeoutException ex) {
            throw new OAuthException(OAuthError.invalid_grant, "Code expired.", null);
        }

        // ensure that the "redirect_uri" parameter is present if the "redirect_uri" parameter was included in the
        // initial authorization request as described in Section 4.1.1, and if included ensure that their values are
        // identical. (4.1.3. Access Token Request)
        if (session.getRedirectionURI().isDynamicConfigured()) {
            String redirectURI = getRedirectURI(parameters);
            if (!redirectURI.equals(session.getRedirectionURI().getURI())) {
                throw new OAuthException(
                        OAuthError.invalid_grant,
                        "The redirect_uri is not identical to the one included in the initial authorization request.",
                        null);
            }
        }

        Token token = tokens.generateToken(client, session.getScopeOwner(), session.getGrantedScope());

        return responseTokenRepresentation(token, session.getRequestedScope());
    }

    /**
     * Handles errors as described in 5.2 Error Response.
     */
    @Override
    protected void doCatch(Throwable t) {
        getLogger().log(Level.SEVERE, "Error while generating a Oauth token.", t);
        final OAuthException oex = OAuthException.toOAuthException(t);
        // The authorization server responds with an HTTP 400 (Bad Request)
        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        getResponse().setEntity(responseErrorRepresentation(oex));
        // Sets the no-store Cache-Control header
        addCacheDirective(getResponse(), CacheDirective.noStore());
        getResponse().getHeaders().add(HeaderConstants.HEADER_PRAGMA, "no-cache");
    }

    /**
     * Executes the "client_credentials" flow. (4.4. Client Credentials Grant).
     * 
     * @param parameters
     * @return The JSON representation of the response of the "client_credentials" flow.
     * @throws OAuthException
     * @throws JSONException
     */
    private Representation doClientFlow(Form parameters) throws OAuthException, JSONException {
        // The flow require authenticated client.
        Client client = getAuthenticatedClient();
        if (client == null || client.getClientType() != ClientType.CONFIDENTIAL) {
            // The client credentials grant type MUST only be used by confidential clients.
            throw new OAuthException(
                    OAuthError.invalid_client,
                    "The client credentials grant type MUST only be used by confidential clients.",
                    null);
        }

        ensureGrantTypeAllowed(client, GrantType.client_credentials);

        String[] requestedScope = getScope(parameters);

        // Generate token for the client itself.
        Token token = tokens.generateToken(client, requestedScope);

        return responseTokenRepresentation(token, requestedScope);
    }

    /**
     * Executes the "password" flow. (4.3. Resource Owner Password Credentials
     * Grant).
     * 
     * @param parameters
     * @return The JSON representation of the response of the "password" flow.
     * @throws OAuthException
     * @throws JSONException
     */
    private Representation doPasswordFlow(Form parameters) throws OAuthException,
            JSONException {
        Object users = getContext().getAttributes().get(ResourceOwnerManager.class.getName());
        if (users == null) {
            throw new OAuthException(OAuthError.unsupported_grant_type, "'password' flow is not supported.", null);
        }

        // The flow require authenticated client.
        Client client = getAuthenticatedClient();
        if (client == null) {
            // XXX: 'password' flow MAY use the public client. (3.2.1 Client Authentication)
            client = getClient(parameters);
        }

        ensureGrantTypeAllowed(client, GrantType.password);

        String username = getUsername(parameters);
        String password = getPassword(parameters);
        String identifier;

        try {
            identifier = ((ResourceOwnerManager) users).authenticate(username, password.toCharArray());
        } catch (AuthenticationException ex) {
            throw new OAuthException(OAuthError.invalid_grant, ex.getExplanation(), null);
        }

        String[] requestedScope = getScope(parameters);

        // Generate token for the resource owner.
        Token token = tokens.generateToken(client, identifier, requestedScope);

        return responseTokenRepresentation(token, requestedScope);
    }

    /**
     * Executes the "refresh_token" flow. (6. Refreshing an Access Token)
     * 
     * @param parameters
     * @return The JSON representation of the response of the "refresh_token" flow.
     * @throws OAuthException
     * @throws JSONException
     */
    private Representation doRefreshFlow(Form parameters) throws OAuthException, JSONException {
        // The flow require authenticated client.
        Client client = getAuthenticatedClient();
        if (client == null) {
            // XXX: 'refresh' flow MAY use the public client. (3.2.1 Client Authentication)
            client = getClient(parameters);
        }

        ensureGrantTypeAllowed(client, GrantType.refresh_token);

        String refreshToken = getRefreshToken(parameters);

        String[] requestedScope = null;

        String scope = parameters.getFirstValue(SCOPE);
        if (!StringUtils.isNullOrEmpty(scope)) {
            requestedScope = Scopes.parseScope(scope);
        }

        Token token = tokens.refreshToken(client, refreshToken, requestedScope);

        return responseTokenRepresentation(token, requestedScope);
    }

    protected void ensureGrantTypeAllowed(Client client, GrantType grantType)
            throws OAuthException {
        if (!client.isGrantTypeAllowed(grantType)) {
            throw new OAuthException(OAuthError.unauthorized_client,
                    "Unauthorized grant type.", null);
        }
    }

    protected Client getAuthenticatedClient() throws OAuthException {
        User authenticatedClient = getRequest().getClientInfo().getUser();
        if (authenticatedClient == null) {
            getLogger().warning("Authenticated client_id is missing.");
            return null;
        }
        // XXX: We 'know' the client was authenticated before, 'client' should not be null.
        Client client = clients.findById(authenticatedClient.getIdentifier());
        getLogger().fine("Requested by authenticated client " + client.getClientId());
        return client;
    }

    @Override
    protected Client getClient(Form params) throws OAuthException {
        Client client = super.getClient(params);

        if (client.getClientType() == Client.ClientType.CONFIDENTIAL) {
            throw new OAuthException(OAuthError.invalid_client, "Unauthenticated confidential client.", null);
        } else if (client.getClientSecret() != null) {
            throw new OAuthException(OAuthError.invalid_client, "Unauthenticated public client.", null);
        }
        return client;
    }

    /**
     * Returns the value of the request parameter "code".
     * 
     * @param parameters
     * @return The value of the request parameter "code".
     * @throws OAuthException
     *             In case the parameter is not set.
     */
    protected String getCode(Form parameters) throws OAuthException {
        return getParameter(parameters, CODE);
    }

    /**
     * Returns the value of the request parameter "grant_type".
     * 
     * @param parameters
     * @return The value of the request parameter "grant_type".
     * @throws OAuthException
     *             In case the parameter is not set or is an invalid value.
     */
    protected GrantType getGrantType(Form parameters) throws OAuthException {
        String typeString = getParameter(parameters, GRANT_TYPE);
        try {
            getLogger().info("Type: " + typeString);
            GrantType type = Enum.valueOf(GrantType.class, typeString);
            getLogger().fine("Found flow - " + type);
            return type;
        } catch (IllegalArgumentException iae) {
            throw new OAuthException(OAuthError.unsupported_grant_type, "Unsupported flow", null);
        }
    }

    /**
     * Return the value of the given parameter, and throws an OAuthException in case the parameter is not set.
     * 
     * @param parameters
     *            The set of parameters.
     * @param key
     *            The name of the parameter to return.
     * @return The value of the given parameter.
     * @throws OAuthException
     */
    private String getParameter(Form parameters, String key) throws OAuthException {
        String value = parameters.getFirstValue(key);

        if (StringUtils.isNullOrEmpty(value)) {
            throw new OAuthException(OAuthError.invalid_request, "The following parameter is missing: " + key, null);
        }
        return value;
    }

    /**
     * Returns the value of the request parameter "password".
     * 
     * @param parameters
     * @return The value of the request parameter "password".
     * @throws OAuthException
     *             In case the parameter is not set.
     */
    protected String getPassword(Form parameters) throws OAuthException {
        return getParameter(parameters, PASSWORD);
    }

    /**
     * Returns the value of the request parameter "redirect_uri".
     * 
     * @param parameters
     * @return The value of the request parameter "redirect_uri".
     * @throws OAuthException
     *             In case the parameter is not set.
     */
    protected String getRedirectURI(Form parameters) throws OAuthException {
        return getParameter(parameters, REDIR_URI);
    }

    /**
     * Returns the value of the request parameter "refresh_token".
     * 
     * @param parameters
     * @return The value of the request parameter "refresh_token".
     * @throws OAuthException
     *             In case the parameter is not set.
     */
    protected String getRefreshToken(Form parameters) throws OAuthException {
        return getParameter(parameters, REFRESH_TOKEN);
    }

    /**
     * Returns the value of the request parameter "username".
     * 
     * @param parameters
     * @return The value of the request parameter "username".
     * @throws OAuthException
     *             In case the parameter is not set.
     */
    protected String getUsername(Form parameters) throws OAuthException {
        return getParameter(parameters, USERNAME);
    }

    /**
     * Handles the {@link Post} request. The client MUST use the HTTP "POST"
     * method when making access token requests. (3.2. Token Endpoint)
     * 
     * @param input
     *            HTML form formated token request per oauth-v2 spec.
     * @return JSON response with token or error.
     */
    @Post("form:json")
    public Representation requestToken(Representation input) throws OAuthException, JSONException {
        getLogger().fine("Grant request");
        final Form parameters = new Form(input);

        final GrantType grantType = getGrantType(parameters);

        switch (grantType) {
        case authorization_code:
            getLogger().info("Authorization Code Grant");
            return doAuthCodeFlow(parameters);
        case password:
            getLogger().info("Resource Owner Password Credentials Grant");
            return doPasswordFlow(parameters);
        case client_credentials:
            getLogger().info("Client Credentials Grant");
            return doClientFlow(parameters);
        case refresh_token:
            getLogger().info("Refreshing an Access Token");
            return doRefreshFlow(parameters);
        default:
            getLogger().warning("Unsupported flow: " + grantType);
            throw new OAuthException(OAuthError.unsupported_grant_type, "Flow not supported", null);
        }
    }

    /**
     * Generates a JSON document with valid token. The format of the JSON document is according to 5.1. Successful
     * Response.
     * 
     * @param token
     *            The token generated by the client.
     * @param requestedScope
     *            The scope originally requested by the client.
     * @return The token representation as described in RFC6749 5.1.
     * @throws JSONException
     */
    protected Representation responseTokenRepresentation(Token token, String[] requestedScope) throws JSONException {
        JSONObject response = new JSONObject();

        response.put(TOKEN_TYPE, token.getTokenType());
        response.put(ACCESS_TOKEN, token.getAccessToken());
        response.put(EXPIRES_IN, token.getExpirePeriod());

        String refreshToken = token.getRefreshToken();
        if (!StringUtils.isNullOrEmpty(refreshToken)) {
            response.put(REFRESH_TOKEN, refreshToken);
        }

        String[] scope = token.getScope();
        if (requestedScope == null || !Scopes.isIdentical(scope, requestedScope)) {
            // OPTIONAL, if identical to the requested scope, otherwise REQUIRED. (5.1. Successful Response)
            response.put(SCOPE, Scopes.toString(scope));
        }

        /*
         * The authorization server MUST include the HTTP "Cache-Control"
         * response header field [RFC2616] with a value of "no-store" in any
         * response containing tokens, credentials, or other sensitive
         * information, as well as the "Pragma" response header field [RFC2616]
         * with a value of "no-cache". (5.1. Successful Response)
         */
        addCacheDirective(getResponse(), CacheDirective.noStore());
        getResponse().getHeaders().add(HeaderConstants.HEADER_PRAGMA, "no-cache");

        return new JsonRepresentation(response);
    }
}
