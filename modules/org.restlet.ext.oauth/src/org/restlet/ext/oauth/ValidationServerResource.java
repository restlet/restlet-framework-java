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

import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.internal.ExpireToken;
import org.restlet.ext.oauth.internal.JsonStringRepresentation;
import org.restlet.ext.oauth.internal.Scopes;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.representation.Representation;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;

/**
 * The ValidationResource is intended to protect a Restlet and make sure that
 * correct OAuth credentials are met. It is part of the Authorization server
 * providing a REST endpoint for validation.
 * 
 * In default mode it will accept requests over HTTP so that the protected
 * resource does not have to reside in the same application or even be hosted on
 * the same HTTP server.
 * 
 * By setting the context attribute parameter "localOnly" to "true" it will only
 * allow for in application invocations. As such the AuthServer needs to be part
 * of the protected resource application. In this mode the Validator will check
 * that a validation request is performed using the RIAP protocol.
 * 
 * Example. Attach a ValidationTokenServerResource
 * <pre>
 * {
 *      &#064;code
 *      public Restlet createInboundRoot(){
 *              ...
 *              root.attach(&quot;/validate&quot;, ValidationServerResource.class);
 *              ...
 *      }
 * }
 * </pre>
 * 
 * @see org.restlet.ext.oauth.internal.org.restlet.ext.oauth.OAuthAuthorizer
 * 
 * @author Kristoffer Gronowski
 */
public class ValidationServerResource extends OAuthServerResource {

    //TODO: Move to OAuthHelper init parameters...
    public static final String LOCAL_ACCESS_ONLY = "localOnly";
    
    private boolean isLocalAcessOnly() {
        String lo = (String) getContext().getAttributes()
                .get(LOCAL_ACCESS_ONLY);
        return (lo != null) && (lo.length() > 0) && Boolean.parseBoolean(lo);
    }
    
    private Representation validate(JSONObject call) throws JSONException {
        Token t = validateToken(call);
        
        if (t == null) {
            return createError(OAuthError.invalid_token);
        }

        String uri = call.get("uri").toString();
        JSONArray scopes = null;

        if (call.has("scope")) {
            scopes = call.getJSONArray("scope");
        }

        String owner = null;

        if (call.has("owner")) {
            owner = call.getString("owner");
        }

        // Todo do more fine grained scope comparison.
        getLogger().fine("Received uri = " + uri);
        getLogger().fine("Received scope = " + scopes);
        getLogger().fine("Received owner = " + owner);

        AuthenticatedUser user = t.getUser();
        if (user == null) {
            return createError(OAuthError.invalid_request);
        }

        if ((scopes != null) && (scopes.length() > 0)) {
            // All scopes must match if there are any listed
            for (int i = 0; i < scopes.length(); i++) {
                if (scopes.isNull(i)) {
                    continue;
                }
                String scope = scopes.getString(i);
                boolean granted = user.isGrantedRole(Scopes.toRole(scope), owner);
                getLogger().fine("Granted permission : " + scope + " = " + granted);
                if (!granted) {
                    return createError(OAuthError.insufficient_scope);
                }
            }
        }

        // Matching on the owner if there is one and scope checke out
        if ((owner != null) && (owner.length() > 0)
                && !AUTONOMOUS_USER.equals(user.getId())
                && !owner.equals(user.getId())) {
            return createError(OAuthError.invalid_request);
        }
        
        JSONObject resp = new JSONObject();
        resp.put("tokenOwner", user.getId());
        resp.put("authenticated", true);

        // Sets the no-store Cache-Control header
        getResponse().setCacheDirectives(noStore);
        getResponse().setCacheDirectives(noCache);
        // response.put("expires", t.getToken());
        
        // return new JsonRepresentation(response);
        return new JsonStringRepresentation(resp);
    }
    
    private Token validateToken(JSONObject call) throws JSONException {
        String token = call.get("access_token").toString();

        getLogger().fine("In Validator resource - searching for token = " + token);
        Token t = this.generator.findToken(token);

        if (t == null) {
            return null;
            // setStatus(Status.CLIENT_ERROR_FORBIDDEN);
        }
        
        getLogger().fine("In Validator resource - got token = " + t);

        if (t instanceof ExpireToken) {
            // check that the right token was used
            ExpireToken et = (ExpireToken) t;

            if (!token.equals(et.getToken())) {
                getLogger().warning("Should not use the refresh_token to sign!");
                return null;
            }
        }
        
        return t;
    }
    
    private Representation createError(OAuthError error) throws JSONException {
        JSONObject resp = new JSONObject();
        resp.put("authenticated", false);
        resp.put("error", error.name());
        return new JsonRepresentation(resp);
    }

    @Post("json")
    public Representation validate(Representation input) throws ResourceException {
        getLogger().fine("In Validator resource");

        if (isLocalAcessOnly()) { // Check that protocol = RIAP
            String scheme = getOriginalRef().getScheme();

            if (!Protocol.RIAP.getSchemeName().equals(scheme)) {
                setStatus(Status.CLIENT_ERROR_FORBIDDEN,
                        "Auth server only allows local resource validation");
                return null;
            }
        }
        
        try {
            return validate(new JsonRepresentation(input).getJsonObject());
        } catch (JSONException e) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Failed parse JSON", e);
        } catch (IOException e) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Failed parse JSON", e);
        }
    }

}
