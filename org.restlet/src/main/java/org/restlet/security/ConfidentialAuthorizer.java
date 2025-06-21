/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.security;

import org.restlet.Request;
import org.restlet.Response;

/**
 * Authorizer allowing only confidential calls. Confidential calls typically
 * come through HTTPS server connectors.
 * 
 * @author Jerome Louvel
 */
public class ConfidentialAuthorizer extends Authorizer {

	/**
	 * Authorizes the request only if its method is one of the authorized methods.
	 * 
	 * @param request  The request sent.
	 * @param response The response to update.
	 * @return True if the authorization succeeded.
	 */
	@Override
	public boolean authorize(Request request, Response response) {
		return request.isConfidential();
	}

}
