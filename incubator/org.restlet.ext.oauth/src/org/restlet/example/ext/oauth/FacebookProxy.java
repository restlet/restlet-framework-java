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

package org.restlet.example.ext.oauth;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.OAuthParameters;
import org.restlet.ext.oauth.OAuthProxy;
import org.restlet.ext.oauth.OAuthServerResource;
import org.restlet.ext.oauth.internal.OAuthUtils;
import org.restlet.resource.ClientResource;
import org.restlet.security.User;

/**
 * Helper client class for accessing Facebook Graph API. It can request for
 * specific scopes for a developer key and secret.
 * 
 * <pre>
 * {@code
 * FacebookProxy proxy = new FacebookProxy(facebookClientId, facebookClientSecret 
 *                                          scope, getContext());
 * proxy.setNext(DummyResource.class);
 * router.attach("/protected", proxy);
 * }
 * </pre>
 * 
 * @author Kristoffer Gronowski
 * @see <a href="http://developers.facebook.com/docs/api">Facebook APIs</a>
 */
public class FacebookProxy extends OAuthProxy {

    public static final String CookieID = "_fbid";

    public static final String FB_GRAPH = "https://graph.facebook.com/";

    Map<String, String> accessTokens;

    /**
     * Create Proxy to facebook authentication
     * 
     * @param clientId
     *            facebook clientID
     * @param clientSecret
     *            facebook clientSecret
     * @param scope
     *            requested scope
     * @param ctx
     *            Restlet scope
     */
    public FacebookProxy(String clientId, String clientSecret, String scope,
            Context ctx) {
        this(clientId, clientSecret, scope,
                new ConcurrentHashMap<String, String>(), ctx);
    }

    /**
     * Create Proxy to facebook authentication
     * 
     * @param clientId
     *            facebook clientID
     * @param clientSecret
     *            facebook clientSecret
     * @param scope
     *            requested scope
     * @param accessTokens
     *            map to store access tokens
     * @param ctx
     *            Restlet scope
     */
    public FacebookProxy(String clientId, String clientSecret, String scope,
            Map<String, String> accessTokens, Context ctx) {
        super(new OAuthParameters(clientId, clientSecret, FB_GRAPH + "oauth/",
                scope), ctx);
        this.accessTokens = accessTokens;
    }

    @Override
    protected boolean authorize(Request request, Response response) {
        String myId = request.getCookies().getFirstValue(CookieID);
        Logger log = getLogger();
        log.info("In Authorize");
        if (myId != null && myId.length() > 0) { // Already know what user
            log.info("User known");
            return true;
        } else {
            User user = request.getClientInfo().getUser();
            getLogger().info("User from ClientInfo = " + user);
            boolean cont;
            if (user == null) {
                cont = super.authorize(request, response);
                if (!cont)
                    return cont;
                user = request.getClientInfo().getUser();
                getLogger().info("User from ClientInfo2 = " + user);
            }

            String accessToken = OAuthUtils.getToken(user);
            getLogger().info("AccessToken from ClientInfo = " + accessToken);

            Reference meRef = new Reference("me");
            meRef.addQueryParameter(OAuthServerResource.ACCESS_TOKEN,
                    accessToken);

            ClientResource graphResource = new ClientResource(FB_GRAPH);
            ClientResource meResource = graphResource.getChild(meRef);
            JsonRepresentation meRepr = meResource
                    .get(JsonRepresentation.class);
            if (meResource.getResponse().getStatus().isSuccess()) {
                JSONObject me;
                try {
                    me = meRepr.getJsonObject();
                    String id = me.get("id").toString();
                    log.info("Your ID = " + id);
                    accessTokens.put(id, accessToken);
                    // TODO Set Cookie
                    return true;
                } catch (JSONException e) {
                    log.log(Level.WARNING, "Failed in parsing the me object.",
                            e);
                }
            }
            meRepr.release();
            meResource.release();
            graphResource.release();
        }

        return false;
    }

    /**
     * @return map with all the authenticated FB users that has used this
     *         endpoint.
     */

    public Map<String, String> getAccessTokens() {
        return accessTokens;
    }
}
