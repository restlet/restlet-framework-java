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

package org.restlet.example.ext.oauth.server;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ServerResource;
import org.restlet.security.User;

/**
 * Simple user's status resource.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class StatusServerResource extends ServerResource {

    @Get("json")
    public Representation getUserStatus() throws JSONException {
        User user = getRequest().getClientInfo().getUser();
        getLogger().info("getUserStatus: " + user.getIdentifier());

        SampleUser sampleUser = OAuth2Sample.getSampleUserManager()
                .findUserById(user.getIdentifier());

        if (sampleUser == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }

        JSONObject result = new JSONObject();
        Object status = sampleUser.getStatus();
        if (status != null) {
            result.put("status", status);
        } else {
            result.put("status", "");
        }

        return new JsonRepresentation(result);
    }

    @Put("json")
    public Representation updateUserStatus(Representation representation)
            throws IOException, JSONException {
        JSONObject request = new JsonRepresentation(representation)
                .getJsonObject();
        Object status = request.get("status");

        if (status == null) {
            setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
            return null;
        }

        User user = getRequest().getClientInfo().getUser();
        getLogger().info("updateUserStatus: " + user.getIdentifier());

        SampleUser sampleUser = OAuth2Sample.getSampleUserManager()
                .findUserById(user.getIdentifier());
        if (sampleUser != null) {
            sampleUser.setStatus(status.toString());
        }

        JSONObject result = new JSONObject();
        result.put("status", status);
        return new JsonRepresentation(result);
    }
}
