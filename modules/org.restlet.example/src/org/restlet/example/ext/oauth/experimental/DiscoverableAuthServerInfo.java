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

package org.restlet.example.ext.oauth.experimental;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * EXPERIMENTAL, and not part of the OAuth specification Implementation might
 * change in future releases.
 * 
 * This class provides the Authorization Server Specific metadata. It is used by
 * the OAuth discovery service in order to convey a programatic interface for
 * server discovery.
 * 
 * All values should be quite static once a server is up and running.
 * 
 * Note that the URL's are relative since they will be concatenated in runtime
 * based on the current base URI that they were accessed from.
 * 
 * @author Kristoffer Gronowski
 */
public class DiscoverableAuthServerInfo {

    public enum GrantType {
        authorization_code, password, assertion, refresh_token, none
    }

    public enum AuthParam {
        header, query, form, basic
    }

    public enum IdTech {
        openid, digest, form, basic, saml
    }

    // Proposed standard parameters
    private static final String VERSION = "2.0";

    private String authorizationUrl;

    private String accessTokenUrl;

    private List<String> flows;

    private List<String> authParams;

    // Proposed extensions
    private String validationUrl; // Auth Server <-> Protected Resource

    // Interface

    private String revocationUrl; // OAuth Admin page

    private String clientSignupUrl; // Dev portal

    private String userSignupUrl; // User portal

    private List<String> idTechnologies; // Supported ways to login

    public DiscoverableAuthServerInfo(String authorizationUrl,
            String accessTokenUrl, String validationUrl) {
        this.authorizationUrl = authorizationUrl;
        this.accessTokenUrl = accessTokenUrl;
        this.validationUrl = validationUrl;

        flows = new ArrayList<String>();
        flows.add(GrantType.authorization_code.name());
        // flows.add(GrantType.password.name()); //Implemented but no data BE
        flows.add(GrantType.refresh_token.name());
        flows.add(GrantType.none.name());

        authParams = new ArrayList<String>();
        authParams.add(AuthParam.header.name());
        authParams.add(AuthParam.query.name());
        authParams.add(AuthParam.form.name());

        idTechnologies = new ArrayList<String>();
        idTechnologies.add(IdTech.openid.name());
    }

    public void setRevocationUrl(String url) {
        revocationUrl = url;
    }

    public void setClientSignupUrl(String url) {
        clientSignupUrl = url;
    }

    public void setUserSignupUrl(String url) {
        userSignupUrl = url;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("version", VERSION);
        json.put("authorization_url", authorizationUrl);
        json.put("access_token_url", accessTokenUrl);

        JSONArray a = new JSONArray();
        for (String flow : flows) {
            a.put(flow);
        }
        json.put("flows", a);

        a = new JSONArray();
        for (String param : authParams) {
            a.put(param);
        }
        json.put("auth_parameters", a);

        // Extended
        json.put("x_validation_url", validationUrl);

        if (revocationUrl != null && revocationUrl.length() > 0) {
            json.put("x_revocation_url", revocationUrl);
        }

        if (clientSignupUrl != null && clientSignupUrl.length() > 0) {
            json.put("x_client_signup_url", clientSignupUrl);
        }

        if (userSignupUrl != null && userSignupUrl.length() > 0) {
            json.put("x_user_signup_url", userSignupUrl);
        }

        a = new JSONArray();
        for (String id : idTechnologies) {
            a.put(id);
        }
        json.put("x_id_technology", a);

        return json;
    }
}
