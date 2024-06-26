/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.example.ext.oauth.client;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.ext.oauth.ProtectedClientResource;
import org.restlet.ext.oauth.internal.Token;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

/**
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class FacebookMeServerResource extends ServerResource {

    @Get
    public Representation getMe() throws IOException, JSONException {
        Token token = (Token) getRequest().getAttributes().get(
                Token.class.getName());
        if (token == null) {
            return new StringRepresentation("Token not found!");
        }

        ProtectedClientResource me = new ProtectedClientResource(
                "https://graph.facebook.com/me");
        me.setUseBodyMethod(true);
        me.setToken(token);

        JSONObject result = new JsonRepresentation(me.get()).getJsonObject();

        return new StringRepresentation("Hello " + result.getString("name"));
    }
}
