package org.restlet.test.ext.oauth.test.resources;

import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.ext.oauth.ScopedResource;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class Scoped3 extends ServerResource implements ScopedResource{

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
		return "user3";
	}

	public String[] getScope(Reference uri, Method method) {
		return null;
	}
}
