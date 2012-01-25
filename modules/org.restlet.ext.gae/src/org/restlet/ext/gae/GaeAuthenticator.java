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

package org.restlet.ext.gae;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.ClientInfo;
import org.restlet.security.Authenticator;
import org.restlet.security.Enroler;
import org.restlet.security.User;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

/**
 * Authenticator that provides an integration to the Google App Engine
 * UserService.
 * 
 * @author Matt Kennedy
 */
public class GaeAuthenticator extends Authenticator {
    /**
     * The GAE UserService that provides facilities to check whether a user has
     * authenticated using their Google Account
     */
    private UserService userService = UserServiceFactory.getUserService();

    /**
     * Constructor setting the mode to "required".
     * 
     * @param context
     *            The context.
     * @see #Authenticator(Context)
     */
    public GaeAuthenticator(Context context) {
        super(context);
    }

    /**
     * Constructor using the context's default enroler.
     * 
     * @param context
     *            The context.
     * @param optional
     *            The authentication mode.
     * @see #Authenticator(Context, boolean, Enroler)
     */
    public GaeAuthenticator(Context context, boolean optional) {
        super(context, optional);
    }

    /**
     * Constructor.
     * 
     * @param context
     *            The context.
     * @param optional
     *            The authentication mode.
     * @param enroler
     *            The enroler to invoke upon successful authentication.
     */
    public GaeAuthenticator(Context context, boolean optional, Enroler enroler) {
        super(context, optional, enroler);
    }

    /**
     * Integrates with Google App Engine UserService to redirect
     * non-authenticated users to the GAE login URL. Upon successful login,
     * creates a Restlet User object based values in GAE user object. The GAE
     * "nickname" property gets mapped to the Restlet "firstName" property.
     * 
     * @param request
     *            The request sent.
     * @param response
     *            The response to update.
     * @return True if the authentication succeeded.
     */
    @Override
    protected boolean authenticate(Request request, Response response) {
        ClientInfo info = request.getClientInfo();
        if (info.isAuthenticated()) {
            // The request is already authenticated.
            return true;
        } else if (userService.isUserLoggedIn()) {
            // The user is logged in, create restlet user.
            com.google.appengine.api.users.User gaeUser = userService
                    .getCurrentUser();
            User restletUser = new User(gaeUser.getUserId());
            restletUser.setEmail(gaeUser.getEmail());
            restletUser.setFirstName(gaeUser.getNickname());
            info.setUser(restletUser);
            info.setAuthenticated(true);
            return true;
        } else {
            // The GAE user service says user not logged in, let's redirect him
            // to the login page.
            String loginUrl = userService.createLoginURL(request
                    .getOriginalRef().toString());
            response.redirectTemporary(loginUrl);
            return false;
        }
    }
}
