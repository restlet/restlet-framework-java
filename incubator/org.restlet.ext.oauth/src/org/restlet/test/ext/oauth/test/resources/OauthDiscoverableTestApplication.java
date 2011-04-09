package org.restlet.test.ext.oauth.test.resources;

import org.restlet.Restlet;
import org.restlet.ext.oauth.DiscoverableAuthServerInfo;
import org.restlet.ext.oauth.DiscoverableFilter;
import org.restlet.ext.oauth.ValidationServerResource;
import org.restlet.routing.Router;

/**
 * Test for a protected resource embedded with an authorization server
 * 
 * 
 * @author Kristoffer Gronowski
 *
 */

public class OauthDiscoverableTestApplication extends OauthTestApplication {

	public OauthDiscoverableTestApplication() {
		super(0);
	}
	
	@Override
	public synchronized Restlet createInboundRoot() {
		//Set context param to only allow local token validation.
		getContext().getAttributes().put(ValidationServerResource.LOCAL_ACCESS_ONLY, "true");
		
		DiscoverableAuthServerInfo asi = new DiscoverableAuthServerInfo("/authorize", "authenticate", "validate");
                DiscoverableFilter disc = new DiscoverableFilter(asi);
		
		Restlet r = super.createInboundRoot();
		Router router = (Router)r;
	
//		disc.setNext(DiscoverableDummyResource.class);
//		router.attach("/resource{trailing}",disc);
//		
//		return router;
		
		router.attach("/resource{trailing}", DiscoverableDummyResource.class);
		disc.setNext(router);
		return disc;
	}

}
