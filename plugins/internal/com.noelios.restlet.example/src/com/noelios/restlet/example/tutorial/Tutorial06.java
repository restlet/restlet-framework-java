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

import org.restlet.Application;
import org.restlet.Container;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

import com.noelios.restlet.DirectoryFinder;

/**
 * Server static files using an application.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Tutorial06 implements Constants
{
   public static void main(String[] args) throws Exception
   {
		// Create a container
		Container container = new Container();
		container.getServers().add(Protocol.HTTP, 8182);

		// Create an application
		Application application = new Application(container)
		{
			public Restlet createRoot()
			{
				return new DirectoryFinder(getContext(), ROOT_URI, "index.html");
			}
		};

		// Attach the application to the container and start it
		container.getDefaultHost().attach("", application);
		container.start();
   }

}
