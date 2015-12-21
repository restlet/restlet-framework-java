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

package org.restlet.example.ext.oauth.client;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.ext.oauth.OAuthProxy;
import org.restlet.routing.Router;

/**
 * Simple OAuth 2.0 facebook connect application.
 * 
 * @author Shotaro Uchida <fantom@xmaker.mx>
 */
public class SampleApplication extends Application {

    @Override
    public synchronized Restlet createInboundRoot() {
        Router router = new Router(getContext());

        OAuthProxy facebookProxy = new OAuthProxy(getContext());
        facebookProxy.setClientId("103465123140853");
        facebookProxy.setClientSecret("b97c74c5ea1b6e87256805a54b53184b");
        facebookProxy
                .setRedirectURI("http://ale.valleycampus.net:8888/sample/facebook");
        facebookProxy
                .setAuthorizationURI("https://www.facebook.com/dialog/oauth");
        facebookProxy
                .setTokenURI("https://graph.facebook.com/oauth/access_token");
        facebookProxy.setNext(FacebookMeServerResource.class);
        router.attach("/facebook", facebookProxy);

        OAuthProxy googleProxy = new OAuthProxy(getContext(), false);
        googleProxy.setClientId("69444012865.apps.googleusercontent.com");
        googleProxy.setClientSecret("SJ9XWwAY4ognMNMXmq6db_hE");
        googleProxy
                .setRedirectURI("http://ale.valleycampus.net:8888/sample/google");
        googleProxy
                .setAuthorizationURI("https://accounts.google.com/o/oauth2/auth");
        googleProxy.setTokenURI("https://accounts.google.com/o/oauth2/token");
        googleProxy
                .setScope(new String[] { "https://www.google.com/m8/feeds/" });
        googleProxy.setNext(GoogleContactsServerResource.class);
        router.attach("/google", googleProxy);

        return router;
    }
}
