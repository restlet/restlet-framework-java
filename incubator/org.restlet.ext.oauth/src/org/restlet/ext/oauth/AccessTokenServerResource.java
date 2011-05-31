/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.oauth;


import java.util.List;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;

import org.restlet.data.Status;
import org.restlet.engine.util.Base64;
import org.restlet.ext.oauth.internal.ExpireToken;
import org.restlet.ext.oauth.internal.JsonStringRepresentation;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.security.Role;

/**
 * Server resource used to acquire an OAuth token. A code, or refresh token can
 * be exchanged for a working token. This resource also supports the none flow.
 * 
 * Note: at the moment password and assertion flows are not supported.
 * Implements OAuth 2.0 draft 10
 * 
 * @author Kristoffer Gronowski
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4">OAuth
 *      2 draft 10</a>
 */
public class AccessTokenServerResource extends OAuthServerResource {

    private JSONObject createJsonToken(Token token, String scopes)
            throws ResourceException {
        JSONObject body = new JSONObject();
        
        try {
            body.put(ACCESS_TOKEN, token.getToken());
            if (token instanceof ExpireToken) {
                ExpireToken et = (ExpireToken) token;
                body.put(EXPIRES_IN, et.getExpirePeriod());
                body.put(REFRESH_TOKEN, et.getRefreshToken());
            }
            // TODO add scope
        } catch (JSONException e) {
            throw new ResourceException(Status.SERVER_ERROR_INTERNAL,
                    "Failed to generate JSON", e);
        }

        return body;
    }

    private Representation doAuthCodeFlow(String clientId, String clientSecret,
            Form params) throws IllegalArgumentException {
        String redirUri = params.getFirstValue(REDIR_URI);
        if (redirUri == null || redirUri.length() == 0) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return sendError(OAuthError.INVALID_REQUEST,
                    "Mandatory parameter redirect_uri is missing", null);
        }

        String code = params.getFirstValue(CODE);
        if (code == null || code.length() == 0) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return sendError(OAuthError.INVALID_REQUEST,
                    "Mandatory parameter code is missing", null);
        }

        Client client = validate(clientId, clientSecret);
        // null check on failed
        if (client == null) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return sendError(OAuthError.INVALID_CLIENT,
                    "Client id verification failed.", null);
        }

        // check the client secret
        if (!clientSecret.equals(client.getClientSecret())) {
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return sendError(OAuthError.INVALID_GRANT, "Client secret did not match",
                    null);
        }

        // TODO could add a cookie match on the owner but could fail if code is
        // sent to other entity
        // unauthorized_client, right now this is only performed if
        // ScopedResource getOwner returns the user

        // 5 min timeout on tokens, 0 for unlimited
        Token token = generator.exchangeForToken(code, tokenTimeSec);

        // TODO send back scopes if limited

        JSONObject body = createJsonToken(token, null);

        // Sets the no-store Cache-Control header
        getResponse().setCacheDirectives(noStore);
        return new JsonStringRepresentation(body);
    }

    private Representation doNoneFlow(String clientId, String clientSecret, Form params) {
        Client client = validate(clientId, clientSecret);

        // null check on failed
        if (client == null) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return sendError(OAuthError.INVALID_CLIENT,
                    "Client id verification failed.", null);
        }

        if (!client.containsUser(AUTONOMOUS_USER))
            client.createUser(AUTONOMOUS_USER);

        AuthenticatedUser user = client.findUser(AUTONOMOUS_USER);

        // Adding all scopes since super-user
        //String[] scopes = parseScope(params.getFirstValue(SCOPE));
        List <Role> roles = Scopes.toRoles(params.getFirstValue(SCOPE));
        for (Role r : roles) {
            getLogger().info("Requested scopes none flow = " + roles);
            user.addRole(r, "");
            getLogger().info("Adding scope = " + r.getName() + " to auto user");
        }

        Token token = generator.generateToken(user, tokenTimeSec);
        JSONObject body = createJsonToken(token, null); // Scopes N/A

        // Sets the no-store Cache-Control header
        getResponse().setCacheDirectives(noStore);
        return new JsonStringRepresentation(body);
    }

    private Representation doPasswordFlow(String clientId, String clientSecret,
            Form params) {
        Client client = validate(clientId, clientSecret);

        // null check on failed
        if (client == null) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return sendError(OAuthError.INVALID_CLIENT,
                    "Client id verification failed.", null);
        }

        String username = params.getFirstValue(USERNAME);
        AuthenticatedUser user = null;

        if (username == null || (user = client.findUser(username)) == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return sendError(OAuthError.INVALID_REQUEST,
                    "Mandatory parameter username missing.", null);
        }

        String password = params.getFirstValue(PASSWORD);

        if (password == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return sendError(OAuthError.INVALID_REQUEST,
                    "Mandatory parameter password missing.", null);
        }

        if (!password.equals(user.getPassword())) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return sendError(OAuthError.INVALID_GRANT, "Password not correct.", null);
        }

        Token token = generator.generateToken(user, tokenTimeSec);
        JSONObject body = createJsonToken(token, null); // Scopes N/A

        // Sets the no-store Cache-Control header
        getResponse().setCacheDirectives(noStore);
        return new JsonStringRepresentation(body);
    }

    private Representation doRefreshFlow(String clientId, String clientSecret, Form params) {
        String rToken = params.getFirstValue(REFRESH_TOKEN);

        if (rToken == null || rToken.length() == 0) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return sendError(OAuthError.INVALID_REQUEST,
                    "Mandatory parameter refresh_token is missing", null);
        }

        Client client = validate(clientId, clientSecret);

        // null check on failed
        if (client == null) {
            setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return sendError(OAuthError.INVALID_CLIENT,
                    "Client id verification failed.", null);
        }

        Token token = generator.findToken(rToken);

        if (token != null && (token instanceof ExpireToken)) {
            AuthenticatedUser user = token.getUser();

            // Make sure that the user owning the token is owned by this client
            if (client.containsUser(user.getId())) {
                // refresh the token
                generator.refreshToken((ExpireToken) token);

                JSONObject body = createJsonToken(token, null); // Scopes N/A

                // Sets the no-store Cache-Control header
                getResponse().setCacheDirectives(noStore);
                return new JsonStringRepresentation(body);
            } else { // error not owner
                setStatus(Status.CLIENT_ERROR_FORBIDDEN);
                return sendError(OAuthError.UNAUTHORIZED_CLIENT,
                        "User does not match.", null);
                
            }
        } else { // error no such token.
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return sendError(OAuthError.INVALID_GRANT, "Refresh token.", null);
            
        }

    }

    /**
     * @param input
     *            HTML form formated token request per oauth-v2 spec.
     * @return JSON response with token or error.
     */
    @Post("form:json")
    public Representation represent(Representation input) {
        getLogger().info("Method = " + getMethod().getName());
        getLogger().info("In request : " + getOriginalRef().toString());

        Form params = new Form(input);
        String typeString = params.getFirstValue(GRANT_TYPE);
        getLogger().info("Token Service - In service type = " + typeString);

        String clientId = params.getFirstValue(CLIENT_ID);
        String clientSecret = params.getFirstValue(CLIENT_SECRET);

        if (clientSecret == null || clientSecret.length() == 0) {
            // Check for a basic HTTP auth
            ChallengeResponse cr = getChallengeResponse();

            if (ChallengeScheme.HTTP_BASIC.equals(cr.getScheme())) {
                String basic = new String(Base64.decode(cr.getRawValue()));
                int colon = basic.indexOf(':');

                if (colon > -1) {
                    clientSecret = basic.substring(colon + 1);
                    getLogger().info(
                            "Found secret in BASIC Authentication : "
                                    + clientSecret);

                    // Also allow for client ID to be transfered in user part
                    if (colon > 0) { // There is a user part
                        clientId = basic.substring(0, colon);
                        getLogger().info(
                                "Found id in BASIC Authentication : "
                                        + clientId);
                    }
                }
            }

        }

        Representation toRet = null;
        if (clientId == null || clientId.length() == 0) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return sendError(OAuthError.INVALID_REQUEST,
                    "Mandatory parameter client_id is missing", null);
            
            //return new EmptyRepresentation();
        }

        if (clientSecret == null || clientSecret.length() == 0) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return sendError(OAuthError.INVALID_REQUEST,
                    "Mandatory parameter client_secret is missing", null);
            //return new EmptyRepresentation();
        }
        
        

        try {
            GrantType type = Enum.valueOf(GrantType.class, typeString);
            getLogger().info("Found flow - " + type);

            try {
                switch (type) {
                case authorization_code:
                    getLogger().info("doWebServerFlow() - flow");
                    toRet = doAuthCodeFlow(clientId, clientSecret, params);
                    break;
                case password:
                    toRet = doPasswordFlow(clientId, clientSecret, params);
                    break;
                case assertion:
                    sendError(OAuthError.UNSUPPORTED_GRANT_TYPE,
                            "Assertion flow not supported", null);
                    setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
                    break;
                case refresh_token:
                    toRet = doRefreshFlow(clientId, clientSecret, params);
                    break;
                case none:
                    toRet = doNoneFlow(clientId, clientSecret, params);
                    break;
                default:
                    toRet = sendError(OAuthError.UNSUPPORTED_GRANT_TYPE,
                            "Flow not supported", null);
                    setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                }
            } catch (IllegalArgumentException e) { // can not exchange code.
                toRet = sendError(OAuthError.INVALID_GRANT, e.getMessage(), null);
                setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            }
        } catch (IllegalArgumentException iae) {
            toRet = sendError(OAuthError.UNSUPPORTED_GRANT_TYPE, "Flow not supported",
                    null);
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        } catch (NullPointerException npe) {
            toRet = sendError(OAuthError.UNSUPPORTED_GRANT_TYPE, "Flow not supported",
                    null);
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }
        return toRet;
    }

    protected Representation sendError(OAuthError error, String description,
            String errorUri) {
        JSONObject result = new JSONObject();

        try {
            result.put(OAuthServerResource.ERROR, error.name());

            if (description != null && description.length() > 0) {
                result.put(OAuthServerResource.ERROR_DESC, description);
            }

            if (errorUri != null && errorUri.length() > 0) {
                result.put(OAuthServerResource.ERROR_URI, errorUri);
            }
            return new JsonStringRepresentation(result);
        } catch (JSONException e) {
            getLogger().log(Level.WARNING, "Error while sending OAuth error.",
                    e);
        }
        return null;
    }

    private Client validate(String clientId, String clientSecret) {
        Client client = clients.findById(clientId);
        getLogger().info("Client = " + client);

        if (client == null) {
            sendError(OAuthError.INVALID_CLIENT,
                    "Could not find the correct client with id : " + clientId,
                    null);
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }

        if (clientSecret == null
                || !clientSecret.equals(client.getClientSecret())) {
            sendError(OAuthError.INVALID_GRANT, "Client secret did not match",
                    null);
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            getLogger().info(
                    "Could not find or match client secret " + clientSecret
                            + " : " + client.getClientSecret());
        }

        return client;
    }

}
