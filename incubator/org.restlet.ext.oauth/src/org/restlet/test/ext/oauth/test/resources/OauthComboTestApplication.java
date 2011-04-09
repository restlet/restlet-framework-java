package org.restlet.test.ext.oauth.test.resources;

import org.restlet.Restlet;
import org.restlet.ext.oauth.ValidationServerResource;
import org.restlet.ext.oauth.internal.LocalAuthorizer;
import org.restlet.routing.Router;
import org.restlet.test.ext.oauth.provider.AuthorizationServerTest;

/**
 * Test for a protected resource embedded with an authorization server
 * 
 * 
 * @author Kristoffer Gronowski
 *
 */

public class OauthComboTestApplication extends OauthTestApplication {

	public OauthComboTestApplication(long timeout) {
		super(timeout);
	}
	
	@Override
	public synchronized Restlet createInboundRoot() {
		//Set context param to only allow local token validation.
		getContext().getAttributes().put(ValidationServerResource.LOCAL_ACCESS_ONLY, "true");
		Restlet r = super.createInboundRoot();
		Router router = (Router)r;
		
		LocalAuthorizer auth = new LocalAuthorizer(
			"/validate",
			AuthorizationServerTest.prot+"://localhost:"+
			AuthorizationServerTest.serverPort+"/combo/authorize"
			);
		auth.setNext(DummyResource.class);
		router.attach("/protected",auth);
		
		return router;
	}

}
