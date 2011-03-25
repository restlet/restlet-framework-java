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

package org.restlet.ext.oauth.provider;

import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.engine.util.Base64;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.provider.OAuthError.ErrorCode;
import org.restlet.ext.oauth.provider.data.AuthenticatedUser;
import org.restlet.ext.oauth.provider.data.Client;
import org.restlet.ext.oauth.provider.data.ExpireToken;
import org.restlet.ext.oauth.provider.data.Token;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * 
 * AccessTokenResource is used to acquire an oauth token. A code, or refresh
 * token can be exchange for a working token. This resource also supports the
 * none flow.
 * 
 * At the moment password and assertion flows are not supported.
 * 
 * Implements OAuth 2.0 draft 10
 * 
 * @author Kristoffer Gronowski
 * 
 * @see <a
 *      href="http://tools.ietf.org/html/draft-ietf-oauth-v2-10#section-4">OAuth
 *      2 draft 10</a>
 */
public class AccessTokenServerResource extends OAuthServerResource {

    /**
     * @param input
     *            HTML form formated token request per oauth-v2 spec.
     * @return JSON response with token or error.
     */
    @Post("form:json")
    public Representation represent(Representation input) {
        log.info("Method = " + getRequest().getMethod().getName());
        log.info("In request : " + getOriginalRef().toString());

        Form params = new Form(input);
        String typeString = params.getFirstValue(GRANT_TYPE);
        log.info("Token Service - In service type = " + typeString);

        String clientId = params.getFirstValue(CLIENT_ID);
        String clientSecret = params.getFirstValue(CLIENT_SECRET);

        if (clientSecret == null || clientSecret.length() == 0) {
            // Check for a basic HTTP auth
            ChallengeResponse cr = getRequest().getChallengeResponse();
            if (ChallengeScheme.HTTP_BASIC.equals(cr.getScheme())) {
                String basic = new String(Base64.decode(cr.getRawValue()));
                int colon = basic.indexOf(':');
                if (colon > -1) {
                    clientSecret = basic.substring(colon + 1);
                    log.info("Found secret in BASIC Authentication : "
                            + clientSecret);
                    // Also allow for client ID to be transfered in user part
                    if (colon > 0) { // There is a user part
                        clientId = basic.substring(0, colon);
                        log.info("Found id in BASIC Authentication : "
                                + clientId);
                    }
                }
            }

        }

        if (clientId == null || clientId.length() == 0) {
            sendError(ErrorCode.invalid_request,
                    "Mandatory parameter client_id is missing", null);
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new EmptyRepresentation();
        }

        if (clientSecret == null || clientSecret.length() == 0) {
            sendError(ErrorCode.invalid_request,
                    "Mandatory parameter client_secret is missing", null);
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return new EmptyRepresentation();
        }

        // String sessionId = getCookies().getFirstValue(CookieID);
        // ConcurrentMap<String,Object> attribs = getContext().getAttributes();
        // AuthSession session = (sessionId==null)?null:(AuthSession)
        // attribs.get(sessionId);

        try {
            GrantType type = Enum.valueOf(GrantType.class, typeString);
            log.info("Found flow - " + type);

            if (Method.POST.equals(getMethod())) {
                try {
                    switch (type) {
                    case authorization_code:
                        log.info("doWebServerFlow() - flow");
                        doAuthCodeFlow(clientId, clientSecret, params);
                        break;
                    case password:
                    	doPasswordFlow(clientId, clientSecret, params);
                        break;
                    case assertion:
                        sendError(ErrorCode.unsupported_grant_type,
                                "Assertion flow not supported", null);
                        setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
                        break;
                    case refresh_token:
                        doRefreshFlow(clientId, clientSecret, params);
                        break;
                    case none:
                        doNoneFlow(clientId, clientSecret, params);
                        break;
                    default:
                        sendError(ErrorCode.unsupported_grant_type,
                                "Flow not supported", null);
                        setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
                    }
                } catch (IllegalArgumentException e) { // can not exchange code.
                    sendError(ErrorCode.invalid_grant, e.getMessage(), null);
                    setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
                }
            } else {
                sendError(ErrorCode.invalid_request,
                        "Only supporting HTTP POST.", null);
                setStatus(Status.CLIENT_ERROR_METHOD_NOT_ALLOWED);
            }
        } catch (IllegalArgumentException iae) {
            sendError(ErrorCode.unsupported_grant_type, "Flow not supported",
                    null);
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        } catch (NullPointerException npe) {
            sendError(ErrorCode.unsupported_grant_type, "Flow not supported",
                    null);
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        }

        return getResponse().getEntity();
    }

    private void doAuthCodeFlow(String clientId, String clientSecret,
            Form params) throws IllegalArgumentException {
        String redirUri = params.getFirstValue(REDIR_URI);
        if (redirUri == null || redirUri.length() == 0) {
            sendError(ErrorCode.invalid_request,
                    "Mandatory parameter redirect_uri is missing", null);
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }

        String code = params.getFirstValue(CODE);
        if (code == null || code.length() == 0) {
            sendError(ErrorCode.invalid_request,
                    "Mandatory parameter code is missing", null);
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }

        Client client = validate(clientId, clientSecret);
        // null check on failed
        if (client == null) {
        	sendError(ErrorCode.invalid_client,"Client id verification failed.",null);
        	setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        	return;
        }

        // check the client secret
        if (!clientSecret.equals(client.getClientSecret())) {
            sendError(ErrorCode.invalid_grant, "Client secret did not match",
                    null);
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            return;
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

        getResponse().setEntity(new JsonRepresentation(body));
    }

    private void doNoneFlow(String clientId, String clientSecret, Form params) {
        Client client = validate(clientId, clientSecret);
        // null check on failed
        if (client == null) {
        	sendError(ErrorCode.invalid_client,"Client id verification failed.",null);
        	setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        	return;
        }

        if (!client.containsUser(AUTONOMOUS_USER))
            client.createUser(AUTONOMOUS_USER);
        AuthenticatedUser user = client.findUser(AUTONOMOUS_USER);

        // Adding all scopes since super-user
        String[] scopes = parseScope(params.getFirstValue(SCOPE));
        for (String scope : scopes) {
            log.info("Requested scopes none flow = " + scope);
            user.addScope(scope, "");
            log.info("Adding scope = " + scope + " to auto user");
        }

        Token token = generator.generateToken(user, tokenTimeSec);

        JSONObject body = createJsonToken(token, null); // Scopes N/A

        // Sets the no-store Cache-Control header
        getResponse().setCacheDirectives(noStore);

        getResponse().setEntity(new JsonRepresentation(body));
    }
    
    private void doPasswordFlow(String clientId, String clientSecret, Form params) {
    	Client client = validate(clientId, clientSecret);
        // null check on failed
        if (client == null) {
        	sendError(ErrorCode.invalid_client,"Client id verification failed.",null);
        	setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        	return;
        }
            
        
        String username = params.getFirstValue(USERNAME);
        AuthenticatedUser user = null;
        if( username == null || (user = client.findUser(username)) == null ) {
        	sendError(ErrorCode.invalid_request,"Mandatory parameter username missing.",null);
        	setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        String password = params.getFirstValue(PASSWORD);
        if( password == null ) {
        	sendError(ErrorCode.invalid_request,"Mandatory parameter password missing.",null);
        	setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }
        
        if( !password.equals(user.getPassword()) ) {
        	sendError(ErrorCode.invalid_grant,"Password not correct.",null);
        	setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            return;
        }
        
        Token token = generator.generateToken(user, tokenTimeSec);

        JSONObject body = createJsonToken(token, null); // Scopes N/A

        // Sets the no-store Cache-Control header
        getResponse().setCacheDirectives(noStore);

        getResponse().setEntity(new JsonRepresentation(body));
    	
    }

    private void doRefreshFlow(String clientId, String clientSecret, Form params) {
        String rToken = params.getFirstValue(REFRESH_TOKEN);
        if (rToken == null || rToken.length() == 0) {
            sendError(ErrorCode.invalid_request,
                    "Mandatory parameter refresh_token is missing", null);
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return;
        }

        Client client = validate(clientId, clientSecret);

        // null check on failed
        if (client == null) {
        	sendError(ErrorCode.invalid_client,"Client id verification failed.",null);
        	setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        	return;
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

                getResponse().setEntity(new JsonRepresentation(body));
            } else { // error not owner
                sendError(ErrorCode.unauthorized_client,
                        "User does not match.", null);
                setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            }
        } else { // error no such token.
            sendError(ErrorCode.invalid_grant, "Refresh token.", null);
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
        }

    }

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

    private Client validate(String clientId, String clientSecret) {
        Client client = clients.findById(clientId);
        log.info("Client = " + client);
        if (client == null) {
            sendError(ErrorCode.invalid_client,
                    "Could not find the correct client with id : " + clientId,
                    null);
            setStatus(Status.CLIENT_ERROR_NOT_FOUND);
            return null;
        }

        if (clientSecret == null
                || !clientSecret.equals(client.getClientSecret())) {
            sendError(ErrorCode.invalid_grant, "Client secret did not match",
                    null);
            setStatus(Status.CLIENT_ERROR_UNAUTHORIZED);
            log.info("Could not find or match client secret " + clientSecret
                    + " : " + client.getClientSecret());
        }

        return client;
    }

    public void sendError(OAuthError.ErrorCode error, String description,
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
            JsonRepresentation repr = new JsonRepresentation(result);
            getResponse().setEntity(repr);
        } catch (JSONException e) {
            log.log(Level.WARNING, "Error while sending OAuth error.", e);
        }
    }

}
