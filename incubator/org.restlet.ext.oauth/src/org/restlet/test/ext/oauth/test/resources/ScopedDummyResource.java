package org.restlet.test.ext.oauth.test.resources;

import org.restlet.data.MediaType;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.ScopedResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.restlet.test.ext.oauth.provider.AuthorizationServerTest;

public class ScopedDummyResource extends ServerResource implements ScopedResource{

	@Get
	public Representation getDummy() {
		return new StringRepresentation("TestSuccessful", MediaType.TEXT_HTML);
	}
	
	@Post("form")
	public Representation postDummy(Representation input) {
		//return null;
		//return new EmptyRepresentation();
		return new StringRepresentation("ScopedDummy");
	}

	public String getOwner(Reference uri) {
		return AuthorizationServerTest.prot+"://localhost:"+
		AuthorizationServerTest.serverPort+"/oauth/provider?id=foo";
	}

	public String[] getScope(Reference uri) {
		return new String[]{"foo","bar"};
	}
}
