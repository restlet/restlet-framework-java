package org.restlet.test.ext.oauth.test.resources;

import org.restlet.data.MediaType;
import org.restlet.ext.oauth.OAuthUser;
import org.restlet.ext.oauth.OAuthUtils;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class DummyResource extends ServerResource {

	@Get
	public Representation getDummy() {
	    org.restlet.security.User u = getRequest().getClientInfo().getUser();
	    if(u != null && u instanceof OAuthUser)
	        OauthClientTestApplication.user = (OAuthUser) u;
	    return new StringRepresentation("TestSuccessful", MediaType.TEXT_HTML);
	}
	
	@Post("form")
	public Representation postDummy(Representation input) {
	    org.restlet.security.User u = getRequest().getClientInfo().getUser();
            if(u != null && u instanceof OAuthUser)
                OauthClientTestApplication.user = (OAuthUser) u;
            return new StringRepresentation("Dummy");
	}
}
