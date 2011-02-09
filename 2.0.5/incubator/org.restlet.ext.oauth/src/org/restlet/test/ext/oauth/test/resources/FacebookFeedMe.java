package org.restlet.test.ext.oauth.test.resources;

import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Form;
import org.restlet.data.Reference;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.util.OAuthUtils;
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
        String token = OAuthUtils.getToken(u);
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
        String token = OAuthUtils.getToken(u);
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
