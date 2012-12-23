/*
 * Copyright 2012 Shotaro Uchida <fantom@xmaker.mx>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.restlet.ext.oauth.security;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.AuthenticatedUser;
import org.restlet.ext.oauth.OAuthError;
import org.restlet.ext.oauth.OAuthException;
import org.restlet.ext.oauth.OAuthServerResource;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.ext.oauth.internal.memory.ExpireToken;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * Token "Authenticate" Resource for internal use.
 *
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class TokenAuthServerResource extends OAuthServerResource {

    public static final String LOCAL_ACCESS_ONLY = "localOnly";
    
    private boolean isLocalAcessOnly() {
        String lo = (String) getContext().getAttributes()
                .get(LOCAL_ACCESS_ONLY);
        return (lo != null) && (lo.length() > 0) && Boolean.parseBoolean(lo);
    }
    
    @Override
    protected void doCatch(Throwable t) {
        final OAuthException oex = OAuthException.toOAuthException(t);
        // XXX: Generally, we only communicate with TokenVerifier. So we don't need HTTP 400 code.
//        getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        getResponse().setStatus(Status.SUCCESS_OK);
        getResponse().setEntity(responseErrorRepresentation(oex));
    }
    
    @Post("json")
    public Representation authenticate(Representation input) throws Exception {
        getLogger().fine("In Authenticate resource");

        if (isLocalAcessOnly()) { // Check that protocol = RIAP
            String scheme = getOriginalRef().getScheme();

            if (!Protocol.RIAP.getSchemeName().equals(scheme)) {
                throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                        "Auth server only allows local resource validation");
            }
        }
        
        JSONObject call = new JsonRepresentation(input).getJsonObject();
        
        if (!call.has(TOKEN_TYPE)) {
            throw new OAuthException(OAuthError.invalid_request, "No token_type", null);
        }
        String tokenType = call.getString(TOKEN_TYPE);
        
        final Token token;
        if (tokenType.equals(OAuthServerResource.TOKEN_TYPE_BEARER)) {
            token = validateBearerToken(call);
        }/* else if (tokenType.equals(OAuthServerResource.TOKEN_TYPE_MAC)) {
            // TODO
        }*/ else {
            throw new OAuthException(OAuthError.invalid_request, "Unsupported token_type", null);
        }
        
        AuthenticatedUser user = token.getUser();
        if (user == null) {
            // Revoked?
            throw new OAuthException(OAuthError.invalid_token, "AuthenticatedUser not found", null);
        }
        
        String scope = Scopes.toScope(user.getGrantedRoles());
        JSONObject resp = new JSONObject();
        resp.put(USERNAME, user.getId());
        resp.put(SCOPE, scope);
        
        return new JsonRepresentation(resp);
    }
    
    private Token validateBearerToken(JSONObject call) throws JSONException, OAuthException {
        String token = call.get(ACCESS_TOKEN).toString();

        getLogger().fine("In Validator resource - searching for token = " + token);
        Token t = this.generator.findToken(token);

        if (t == null) {
            throw new OAuthException(OAuthError.invalid_token, "Token not found.", null);
        }
        
        getLogger().fine("In Validator resource - got token = " + t);

        if (t instanceof ExpireToken) {
            // check that the right token was used
            ExpireToken et = (ExpireToken) t;

            if (!token.equals(et.getToken())) {
                getLogger().warning("Should not use the refresh_token to sign!");
                throw new OAuthException(OAuthError.invalid_token, "Invalid Token.", null);
            }
        }
        
        return t;
    }
}
