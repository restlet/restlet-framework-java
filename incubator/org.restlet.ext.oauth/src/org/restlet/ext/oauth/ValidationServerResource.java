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

import java.io.IOException;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
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
 * @see org.restlet.ext.oauth.internal.org.restlet.ext.oauth.internal.protectedresource.LocalAuthorizer
 * @see org.restlet.ext.oauth.OAuthAuthorizer.restlet.ext.oauth.internal.protectedresource.RemoteAuthorizer
 * 
 * @author Kristoffer Gronowski
 */
public class ValidationServerResource extends OAuthServerResource {

    public static final String LOCAL_ACCESS_ONLY = "localOnly";

    @Post("json")
    public JsonRepresentation validate(Representation input)
            throws ResourceException {
        Logger log = getLogger();
        log.info("In Validator resource");
        JSONObject response = new JSONObject();
        boolean authenticated = false;
        String lo = (String) getContext().getAttributes()
                .get(LOCAL_ACCESS_ONLY);
        if (lo != null && lo.length() > 0) {
            boolean localOnly = Boolean.parseBoolean(lo);
            if (localOnly) { // Check that protocol = RIAP
                String scheme = getRequest().getOriginalRef().getScheme();
                if (!Protocol.RIAP.getSchemeName().equals(scheme)) {
                    setStatus(Status.CLIENT_ERROR_FORBIDDEN,
                            "Auth server only allows local resource validation");
                    return null;
                }
            }
        }
        try {
            String error = null;
            JsonRepresentation rest = new JsonRepresentation(input);
            JSONObject call = rest.getJsonObject();
            String token = call.get("access_token").toString();
            String uri = call.get("uri").toString();
            JSONArray scopes = null;
            if (call.has("scope"))
                scopes = call.getJSONArray("scope");
            String owner = null;
            if (call.has("owner"))
                owner = call.getString("owner");

            log.info("In Validator resource - searching for token = " + token);
            Token t = generator.findToken(token);
            if (t == null) {
                response.put("authenticated", authenticated);
                error = OAuthError.INVALID_TOKEN.name();
                // setStatus(Status.CLIENT_ERROR_FORBIDDEN);
            } else {
                log.info("In Validator resource - got token = " + t);
                if (t instanceof ExpireToken) {
                    // check that the right token was used
                    ExpireToken et = (ExpireToken) t;
                    if (!token.equals(et.getToken())) {
                        error = OAuthError.INVALID_TOKEN.name();
                        getLogger().warning(
                                "Should not use the refresh_token to sign!");
                    }
                }

                // Todo do more fine grained scope comparison.
                log.info("Received uri = " + uri);
                log.info("Received scope = " + scopes);
                log.info("Received owner = " + owner);

                AuthenticatedUser user = t.getUser();
                authenticated = (user == null) ? false : true;
                if (!authenticated) {
                    error = OAuthError.INVALID_REQUEST.name();
                }
                if (authenticated && scopes != null && scopes.length() > 0) {
                    // All scopes must match if there are any listed
                    for (int i = 0; i < scopes.length(); i++) {
                        if (scopes.isNull(i))
                            continue;
                        String scope = scopes.getString(i);
                        boolean granted = user.isGrantedScope(scope, owner);
                        getLogger().info(
                                "Granted permission : " + scope + " = "
                                        + granted);
                        if (!granted) {
                            error = OAuthError.INSUFFICIENT_SCOPE.name();
                            authenticated = false;
                            break;
                        }
                    }
                }
                // Matching on the owner if there is one and scope checke out
                if (authenticated) {
                    if (owner != null && owner.length() > 0
                            && !AUTONOMOUS_USER.equals(user.getId())
                            && !owner.equals(user.getId())) {
                        authenticated = false;
                        error = OAuthError.INVALID_REQUEST.name();
                    } else {
                        response.put("tokenOwner", user.getId());
                    }
                }

                response.put("authenticated", authenticated);

                if (error != null) {
                    response.put("error", error);
                }

                // Sets the no-store Cache-Control header
                getResponse().setCacheDirectives(noStore);
                getResponse().setCacheDirectives(noCache);
                // response.put("expires", t.getToken());
            }
        } catch (JSONException e) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Failed parse JSON", e);
        } catch (IOException e) {
            throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST,
                    "Failed parse JSON", e);
        }

        return new JsonRepresentation(response);
    }

}
