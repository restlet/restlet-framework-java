/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.example.tutorial;

import org.restlet.Client;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

/**
 * Retrieving the content of a Web page (detailled).
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Part02b
{
	public static void main(String[] args) throws Exception
	{
		// Prepare the request
		Request request = new Request(Method.GET, "http://www.restlet.org");
		request.setReferrerRef("http://www.mysite.org");

		// Handle it using an HTTP client connector
		Client client = new Client(Protocol.HTTP);
		Response response = client.handle(request);

		// Write the response entity on the console
		Representation output = response.getEntity();
		output.write(System.out);
	}

}
