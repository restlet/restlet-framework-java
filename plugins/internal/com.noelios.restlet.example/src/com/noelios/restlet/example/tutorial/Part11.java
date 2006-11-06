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

import java.util.List;

import org.restlet.Application;
import org.restlet.Container;
import org.restlet.Directory;
import org.restlet.Guard;
import org.restlet.Handler;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Routers and hierarchical URIs
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Part11 implements Constants
{
	public static void main(String[] args) throws Exception
	{
		// Create a container
		Container container = new Container();
		container.getServers().add(Protocol.HTTP, 8182);
		container.getClients().add(Protocol.FILE);

		// Create an application
		Application application = new Application(container)
		{
			@Override
			public Restlet createRoot()
			{
				// Create a root Router
				Router router = new Router(getContext());

				// Attach a Guard to secure access to the chained directory handler
				Guard guard = new Guard(getContext(), ChallengeScheme.HTTP_BASIC,
						"Restlet tutorial");
				guard.getAuthorizations().put("scott", "tiger");
				router.getScorers().add("/docs/", guard);

				// Create a Directory able to return a deep hierarchy of Web files
				Directory directory = new Directory(getContext(), ROOT_URI, "index.html");
				guard.setNext(directory);

				// Create the user Router
				Router user = new Router(getContext());
				router.attach("/users/[a-z]+", user);

				// Create the account Handler
				Handler account = new Handler()
				{
					@Override
					public void handleGet(Request request, Response response)
					{
						// Print the requested URI path
						String message = "Account of user named: "
								+ request.getBaseRef().getLastSegment();
						response.setEntity(message, MediaType.TEXT_PLAIN);
					}
				};
				user.attach("$", account);

				// Create the orders Handler
				Handler orders = new Handler(getContext())
				{
					@Override
					public void handleGet(Request request, Response response)
					{
						// Print the user name of the requested orders
						List<String> segments = request.getBaseRef().getSegments();
						String message = "Orders of user named: "
								+ segments.get(segments.size() - 2);
						response.setEntity(message, MediaType.TEXT_PLAIN);
					}
				};
				user.attach("/orders$", orders);

				// Return the root router
				return router;
			}
		};

		// Attach the application to the container and start it
		container.getDefaultHost().attach("", application);
		container.start();
	}

}
