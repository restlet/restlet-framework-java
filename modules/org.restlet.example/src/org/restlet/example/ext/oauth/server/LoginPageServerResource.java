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
package org.restlet.example.ext.oauth.server;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
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
            DBCollection users = OAuth2Sample.getDefaultDB().getCollection("users");
            DBObject user = users.findOne(new BasicDBObject("_id", userId).append("password", password));
            if (user != null) {
                getAuthSession().setScopeOwner(userId);
                String uri = getQueryValue("continue");
                getLogger().info("URI: " + uri);
                redirectTemporary(uri);
                return new EmptyRepresentation();
            } else {
                data.put("error", "Authentication failed.");
                data.put("error_description", "ID or Password is invalid.");
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
        config.setTemplateLoader(new ContextTemplateLoader(getContext(), "clap:///"));
        getLogger().fine("loading: " + loginPage);
        return new TemplateRepresentation(loginPage, config, MediaType.TEXT_HTML);
    }
}
