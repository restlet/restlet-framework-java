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

package org.restlet.test.ext.oauth.test.resources;

import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.OAuthUser;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.restlet.security.User;

public class FacebookFeedMe extends ServerResource {

    @Get("json")
    public Representation getMe() {
        Reference meRef = new Reference("https://graph.facebook.com/me");
        User u = getRequest().getClientInfo().getUser();
        String token = OAuthUser.getToken(u);
        getLogger().info("Getting with token  = " + token);

        ClientResource meResource = new ClientResource(getContext(), meRef);
        meResource.setClientInfo(getClientInfo());

        ChallengeResponse challengeResponse = new ChallengeResponse(
                ChallengeScheme.HTTP_OAUTH);
        challengeResponse.setRawValue(token);
        meResource.setChallengeResponse(challengeResponse);

        JsonRepresentation meRepr = meResource.get(JsonRepresentation.class);
        if (meResource.getResponse().getStatus().isSuccess()) {
            JSONObject me;
            try {
                me = meRepr.getJsonObject();
                String id = me.get("id").toString();
                getLogger().info("Your ID = " + id);
                return new JsonRepresentation(me);

            } catch (JSONException e) {
                getLogger().log(Level.WARNING,
                        "Failed in parsing the me object.", e);
            }
        }
        meRepr.release();
        meResource.release();

        return null;
    }

    @Post("json")
    public Representation postFeed(Representation input) {
        Reference feedRef = new Reference("https://graph.facebook.com/me/feed");
        User u = getRequest().getClientInfo().getUser();
        String token = OAuthUser.getToken(u);
        getLogger().info("Publishing with token  = " + token);

        feedRef.addQueryParameter("access_token", token);

        ClientResource feedResource = new ClientResource(getContext(), feedRef);
        feedResource.setClientInfo(getClientInfo());

        Form form = new Form();
        form.add("message", "Doing a demo for friends!");
        JsonRepresentation feedRepr = feedResource.post(
                form.getWebRepresentation(), JsonRepresentation.class);
        if (feedResource.getResponse().getStatus().isSuccess()) {
            JSONObject feed;
            try {
                feed = feedRepr.getJsonObject();
                String id = feed.get("id").toString();
                getLogger().info("Feed ID = " + id);
                return new JsonRepresentation(feed);

            } catch (JSONException e) {
                getLogger().log(Level.WARNING,
                        "Failed in parsing the me object.", e);
            }
        }
        return null;
    }
}
