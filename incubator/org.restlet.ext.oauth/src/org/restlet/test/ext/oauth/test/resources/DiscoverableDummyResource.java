package org.restlet.test.ext.oauth.test.resources;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.experimental.DiscoverableResource;
import org.restlet.ext.wadl.WadlServerResource;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.test.ext.oauth.provider.AuthorizationServerTest;

public class DiscoverableDummyResource extends WadlServerResource implements DiscoverableResource{

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

	@Override
	protected Representation delete() throws ResourceException {
		return new EmptyRepresentation();
	}

	public String getOwner(Reference uri) {
		return AuthorizationServerTest.prot+"://localhost:"+
		AuthorizationServerTest.serverPort+"/oauth/provider?id=foo";
	}

	public String[] getScope(Reference uri, Method method) {
		return new String[]{"foo","bar"};
	}
}
