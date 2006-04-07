/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

package com.noelios.restlet.tutorial;

import java.util.List;

import org.restlet.AbstractRestlet;
import org.restlet.DefaultMaplet;
import org.restlet.Manager;
import org.restlet.Maplet;
import org.restlet.Restlet;
import org.restlet.UniformCall;
import org.restlet.component.DefaultRestletContainer;
import org.restlet.component.RestletContainer;
import org.restlet.connector.Server;
import org.restlet.data.ChallengeSchemes;
import org.restlet.data.MediaTypes;
import org.restlet.data.Protocols;

import com.noelios.restlet.DirectoryRestlet;
import com.noelios.restlet.GuardChainlet;
import com.noelios.restlet.LogChainlet;
import com.noelios.restlet.StatusChainlet;
import com.noelios.restlet.data.StringRepresentation;

/**
 * Maplets and hierarchical URIs
 */
public class Tutorial11
{
   public static void main(String[] args)
   {
      try
      {
         // Create a new Restlet container
         RestletContainer myContainer = new DefaultRestletContainer("My container");

         // Create the HTTP server connector, then add it as a server connector
         // to the Restlet container. Note that the container is the call handler.
         Server server = Manager.createServer(Protocols.HTTP, "My server", myContainer, null, 8182);
         myContainer.addServer(server);

         // Attach a log Chainlet to the container
         LogChainlet log = new LogChainlet(myContainer, "com.noelios.restlet.tutorial");
         myContainer.attach("http://localhost:8182", log);

         // Attach a status Chainlet to the log Chainlet
         StatusChainlet status = new StatusChainlet(myContainer, true, "webmaster@mysite.org", "http://www.mysite.org");
         log.attach(status);

         // Attach a root Maplet to the status Chainlet.
         Maplet rootMaplet = new DefaultMaplet(myContainer);
         status.attach(rootMaplet);

         // Attach a guard Chainlet to secure access the the chained directory Restlet
         GuardChainlet guard = new GuardChainlet(myContainer, "com.noelios.restlet.tutorial", true, ChallengeSchemes.HTTP_BASIC , "Restlet tutorial", true)
	      	{
		      	protected boolean authorize(UniformCall call)
		         {
            		return "scott".equals(call.getSecurity().getLogin()) && 
     				 			 "tiger".equals(call.getSecurity().getPassword());
		         }
	         };

         rootMaplet.attach("/docs/", guard);

         // Create a directory Restlet able to return a deep hierarchy of Web files
         DirectoryRestlet dirRestlet = new DirectoryRestlet(myContainer, "D:/Restlet/www/docs/api/", true, "index");
         dirRestlet.addExtension("html", MediaTypes.TEXT_HTML);
         dirRestlet.addExtension("css", MediaTypes.TEXT_CSS);
         dirRestlet.addExtension("gif", MediaTypes.IMAGE_GIF);
         guard.attach(dirRestlet);

         // Create the users Maplet
         Maplet usersMaplet = new DefaultMaplet(myContainer);
         rootMaplet.attach("/users", usersMaplet);

         // Create the user Maplet
         Maplet userMaplet = new DefaultMaplet(myContainer)
            {
               public void handle(UniformCall call)
               {
                  if(call.getResourcePath().equals(""))
                  {
                     // Print the requested URI path
                     String output = "Account of user named: " + call.getHandlerRef().getLastSegment();
                     call.setOutput(new StringRepresentation(output, MediaTypes.TEXT_PLAIN));
                  }
                  else
                  {
                     // Continue processing
                     delegate(call);
                  }
               }
            };
         usersMaplet.attach("/[a-z]+", userMaplet);

         // Create the orders Restlet
         Restlet ordersRestlet = new AbstractRestlet(myContainer)
            {
               public void handle(UniformCall call)
               {
                  // Print the user name of the requested orders
                  List<String> segments = call.getHandlerRef().getSegments();
                  String output = "Orders of user named: " + segments.get(segments.size() - 2);
                  call.setOutput(new StringRepresentation(output, MediaTypes.TEXT_PLAIN));
               }
            };
         userMaplet.attach("/orders$", ordersRestlet);

         // Now, let's start the container!
         myContainer.start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}
