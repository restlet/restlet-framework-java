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

import freemarker.template.Configuration;
import java.util.HashMap;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.ContextTemplateLoader;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.ext.oauth.AuthorizationBaseServerResource;
import org.restlet.ext.oauth.OAuthException;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.security.SecretVerifier;

/**
 * Simple login authentication resource.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class LoginPageServerResource extends AuthorizationBaseServerResource {

    @Get("html")
    @Post("html")
    public Representation getPage() throws OAuthException {
        getLogger().info("Get Login");
        String userId = getQueryValue("user_id");
        HashMap<String, Object> data = new HashMap<String, Object>();
        if (userId != null && !userId.isEmpty()) {
            String password = getQueryValue("password");
            getLogger().info("User=" + userId + ", Pass=" + password);
            SampleUser sampleUser = OAuth2Sample.getSampleUserManager()
                    .findUserById(userId);
            if (sampleUser == null) {
                data.put("error", "Authentication failed.");
                data.put("error_description", "ID is invalid.");
            } else {
                boolean result = SecretVerifier.compare(password.toCharArray(),
                        sampleUser.getPassword());
                if (result) {
                    getAuthSession().setScopeOwner(userId);
                    String uri = getQueryValue("continue");
                    getLogger().info("URI: " + uri);
                    redirectTemporary(uri);
                    return new EmptyRepresentation();
                } else {
                    data.put("error", "Authentication failed.");
                    data.put("error_description", "Password is invalid.");
                }
            }
        }

        String continueURI = getQueryValue("continue");
        TemplateRepresentation response = getLoginPage("login.html");
        data.put("continue", continueURI);
        response.setDataModel(data);

        return response;
    }

    protected TemplateRepresentation getLoginPage(String loginPage) {
        Configuration config = new Configuration();
        config.setTemplateLoader(new ContextTemplateLoader(getContext(),
                "clap:///"));
        getLogger().fine("loading: " + loginPage);
        return new TemplateRepresentation(loginPage, config,
                MediaType.TEXT_HTML);
    }
}
