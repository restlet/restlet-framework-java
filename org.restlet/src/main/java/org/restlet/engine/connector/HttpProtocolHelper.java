/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.connector;

import org.restlet.data.Method;

/**
 * Protocol helper for the HTTP protocol.
 * 
 * @author Thierry Boileau
 */
public class HttpProtocolHelper extends ProtocolHelper {

	@Override
	public void registerMethods() {
		Method.register(Method.ALL);
		Method.register(Method.CONNECT);
		Method.register(Method.DELETE);
		Method.register(Method.GET);
		Method.register(Method.HEAD);
		Method.register(Method.OPTIONS);
		Method.register(Method.PATCH);
		Method.register(Method.POST);
		Method.register(Method.PUT);
		Method.register(Method.TRACE);
	}

}
