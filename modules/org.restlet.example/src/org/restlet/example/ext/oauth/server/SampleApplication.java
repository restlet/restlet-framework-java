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

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.TokenVerifier;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;

/**
 * Simple OAuth 2.0 protected application.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class SampleApplication extends Application {

    @Override
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());

        router.attach("/status", StatusServerResource.class);

        /*
         * Since Role#hashCode and Role#equals are not implemented,
         * RoleAuthorizer cannot be used.
         */
        // RoleAuthorizer roleAuthorizer = new RoleAuthorizer();
        // roleAuthorizer.setAuthorizedRoles(Scopes.toRoles("status"));
        // roleAuthorizer.setNext(router);

        ChallengeAuthenticator bearerAuthenticator = new ChallengeAuthenticator(
                getContext(), ChallengeScheme.HTTP_OAUTH_BEARER, "OAuth2Sample");
        bearerAuthenticator.setVerifier(new TokenVerifier(new Reference(
                "riap://component/oauth/token_auth")));
        bearerAuthenticator.setNext(router);

        return bearerAuthenticator;
    }
}
