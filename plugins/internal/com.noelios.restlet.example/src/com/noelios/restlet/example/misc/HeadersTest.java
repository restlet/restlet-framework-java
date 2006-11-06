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

package com.noelios.restlet.example.misc;

import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.ParameterList;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Display the HTTP accept header sent by the Web browsers.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HeadersTest
{
	public static void main(String[] args) throws Exception
	{
		Restlet restlet = new Restlet()
		{
			@Override
			public void handle(Request request, Response response)
			{
				// ------------------------------
				// Getting an HTTP request header
				// ------------------------------
				ParameterList headers = (ParameterList) request.getAttributes().get(
						"org.restlet.http.headers");

				// The headers list contains all received HTTP headers, in raw format.
				// Below, we simply display the standard "Accept" HTTP header.
				response.setEntity("Accept header: " + headers.getFirstValue("accept", true),
						MediaType.TEXT_PLAIN);

				// -----------------------
				// Adding response headers
				// -----------------------
				headers = new ParameterList();

				// Non-standard headers are allowed
				headers.add("X-Test", "Test value");

				// Standard HTTP headers are forbidden. If you happen to add one like the "Location" 
				// header below, it will be ignored and a warning message will be displayed in the logs.
				headers.add("Location", "http://www.restlet.org");

				// Setting the additional headers into the shared call's attribute
				response.getAttributes().put("org.restlet.http.headers", headers);
			}
		};

		// Create the HTTP server and listen on port 8182
		Server server = new Server(Protocol.HTTP, 8182, restlet);
		server.start();
	}

}
