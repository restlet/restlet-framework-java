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

import org.json.JSONObject;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.ServerToken;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * Token "Authenticate" Resource for internal use.<br>
 * Supports the {@link OAuthResourceDefs#TOKEN_TYPE_BEARER} token type only.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class TokenAuthServerResource extends OAuthServerResource {

    public static final String LOCAL_ACCESS_ONLY = "localOnly";

    /**
     * Checks that the given JSON representation is valid, accordingly to the type of token it contains.
     * 
     * @param input
     *            The JSON representation to validate.
     * @return A JSON representation of the user name and the supported scopes.
     * @throws Exception
     */
    @Post("json")
    public Representation authenticate(Representation input) throws Exception {
        getLogger().fine("In Authenticate resource");

        if (isLocalAccessOnly()) { // Check that protocol = RIAP
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
        if (tokenType.equalsIgnoreCase(OAuthServerResource.TOKEN_TYPE_BEARER)) {
            token = tokens.validateToken(call.get(ACCESS_TOKEN).toString());
        }/*
          * else if (tokenType.equals(OAuthServerResource.TOKEN_TYPE_MAC)) { //
          * TODO }
          */else {
            throw new OAuthException(OAuthError.invalid_request, "Unsupported token_type", null);
        }

        JSONObject resp = new JSONObject();
        resp.put(USERNAME, ((ServerToken) token).getUsername());
        resp.put(SCOPE, Scopes.toString(token.getScope()));

        return new JsonRepresentation(resp);
    }

    @Override
    protected void doCatch(Throwable t) {
        final OAuthException oex = OAuthException.toOAuthException(t);
        // XXX: Generally, we only communicate with TokenVerifier. So we don't need HTTP 400 code.
        // getResponse().setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
        getResponse().setStatus(Status.SUCCESS_OK);
        getResponse().setEntity(responseErrorRepresentation(oex));
    }

    private boolean isLocalAccessOnly() {
        String lo = (String) getContext().getAttributes().get(LOCAL_ACCESS_ONLY);
        return Boolean.parseBoolean(lo);
    }
}
