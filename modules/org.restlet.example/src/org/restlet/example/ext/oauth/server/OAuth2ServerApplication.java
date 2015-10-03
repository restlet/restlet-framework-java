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
import org.restlet.ext.oauth.AccessTokenServerResource;
import org.restlet.ext.oauth.AuthPageServerResource;
import org.restlet.ext.oauth.AuthorizationServerResource;
import org.restlet.ext.oauth.ClientVerifier;
import org.restlet.ext.oauth.HttpOAuthHelper;
import org.restlet.ext.oauth.TokenAuthServerResource;
import org.restlet.ext.oauth.internal.ClientManager;
import org.restlet.ext.oauth.internal.TokenManager;
import org.restlet.resource.Directory;
import org.restlet.routing.Router;
import org.restlet.security.ChallengeAuthenticator;

/**
 * Simple OAuth 2.0 draft30 server-side application.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class OAuth2ServerApplication extends Application {

    @Override
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());

        getContext().getAttributes().put(ClientManager.class.getName(), OAuth2Sample.getClientManager());
        getContext().getAttributes().put(TokenManager.class.getName(), OAuth2Sample.getTokenManager());

        // Setup Authorize Endpoint
        router.attach("/authorize", AuthorizationServerResource.class);
        router.attach(HttpOAuthHelper.getAuthPage(getContext()), AuthPageServerResource.class);
        HttpOAuthHelper.setAuthPageTemplate("authorize.html", getContext());
        HttpOAuthHelper.setAuthSkipApproved(true, getContext());
        HttpOAuthHelper.setErrorPageTemplate("error.html", getContext());
        router.attach(HttpOAuthHelper.getLoginPage(getContext()), LoginPageServerResource.class);

        // Setup Token Endpoint
        ChallengeAuthenticator clientAuthenticator = new ChallengeAuthenticator(getContext(), ChallengeScheme.HTTP_BASIC, "OAuth2Sample");
        ClientVerifier clientVerifier = new ClientVerifier(getContext());
        clientVerifier.setAcceptBodyMethod(true);
        clientAuthenticator.setVerifier(clientVerifier);
        clientAuthenticator.setNext(AccessTokenServerResource.class);
        router.attach("/token", clientAuthenticator);

        // Setup Token Auth for Resources Server
        router.attach("/token_auth", TokenAuthServerResource.class);

        final Directory resources = new Directory(getContext(), "clap://system/resources");

        router.attach("", resources);

        return router;
    }
}
