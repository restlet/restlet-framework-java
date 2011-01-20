package org.restlet.test.ext.oauth.test.resources;

import org.restlet.data.Reference;
import org.restlet.representation.EmptyRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.test.ext.oauth.provider.AuthorizationServerTest;

public class LoginPageResource extends ServerResource {
	
	@Get
	public Representation toProvider() {
		//Redirect to local openId test provider, replaces graphical page
		Reference targetUri = new Reference(getRootRef()+"/openid_login");
		targetUri.addQueryParameter("openid_identifier", 
				AuthorizationServerTest.prot+"://localhost:"
				+AuthorizationServerTest.serverPort+"/oauth/provider");
		redirectTemporary(targetUri);
		return new EmptyRepresentation();
	}
}
