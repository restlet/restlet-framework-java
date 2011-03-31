package org.restlet.test.ext.oauth.test.resources;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.ext.oauth.OAuthAuthorizer;
import org.restlet.routing.Router;
import org.restlet.test.ext.oauth.provider.AuthorizationServerTest;

public class OauthProtectedTestApplication extends Application {
	@Override
	public synchronized Restlet createInboundRoot() {
		Context ctx = getContext();
		Router router = new Router(ctx);
		
		OAuthAuthorizer auth = new OAuthAuthorizer(
				AuthorizationServerTest.prot+"://localhost:"
				+AuthorizationServerTest.serverPort+
			"/oauth/validate",
			AuthorizationServerTest.prot+"://localhost:"+
			AuthorizationServerTest.serverPort+"/oauth/authorize"
			);
		auth.setNext(DummyResource.class);
		router.attach("/protected",auth);
		
		OAuthAuthorizer auth2 = new OAuthAuthorizer(
				AuthorizationServerTest.prot+"://localhost:"
				+AuthorizationServerTest.serverPort+
			"/oauth/validate",
			AuthorizationServerTest.prot+"://localhost:"+
			AuthorizationServerTest.serverPort+"/oauth/authorize"
			);
		auth2.setNext(ScopedDummyResource.class);
		router.attach("/scoped",auth2);
		
		return router;
	}

}
