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

import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Listening to Web browsers.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class Part03
{
	public static void main(String[] args) throws Exception
	{
		// Creating a minimal Restlet returning "Hello World"
		Restlet restlet = new Restlet()
		{
			@Override
			public void handle(Request request, Response response)
			{
				response.setEntity("Hello World!", MediaType.TEXT_PLAIN);
			}
		};

		// Create the HTTP server and listen on port 8182
		new Server(Protocol.HTTP, 8182, restlet).start();
	}

}
