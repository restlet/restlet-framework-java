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

import org.restlet.Call;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.component.Container;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;

import com.noelios.restlet.DirectoryFinder;
import com.noelios.restlet.GuardFilter;
import com.noelios.restlet.HostRouter;
import com.noelios.restlet.LogFilter;
import com.noelios.restlet.StatusFilter;
import com.noelios.restlet.data.StringRepresentation;

/**
 * Routers and hierarchical URIs
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Tutorial11 implements Constants
{
   public static void main(String[] args)
   {
      try
      {
         // Create a new Restlet container
      	Container myContainer = new Container();
         Context myContext = myContainer.getContext();

         // Add an HTTP server connector to the Restlet container. 
         // Note that the container is the call restlet.
         myContainer.getServers().add(Protocol.HTTP, 8182);

         // Attach a log Filter to the container
         LogFilter log = new LogFilter(myContext, "com.noelios.restlet.example");
         myContainer.setRoot(log);

         // Attach a status Filter to the log Filter
         StatusFilter status = new StatusFilter(myContext, true, "webmaster@mysite.org", "http://www.mysite.org");
         log.setNext(status);

         // Create a host router matching calls to the server
         HostRouter host = new HostRouter(myContext, 8182);
         status.setNext(host);

         // Attach a guard Filter to secure access the the chained directory Restlet
         GuardFilter guard = new GuardFilter(myContext, "com.noelios.restlet.example", true, ChallengeScheme.HTTP_BASIC , "Restlet tutorial", true);
         guard.getAuthorizations().put("scott", "tiger");
         host.getScorers().add("/docs/", guard);

         // Create a directory Restlet able to return a deep hierarchy of Web files
         DirectoryFinder directory = new DirectoryFinder(myContext, ROOT_URI, "index.html");
         guard.setNext(directory);

         // Create the user router
         Router user = new Router(myContext);
         host.getScorers().add("/users/[a-z]+", user);

         // Create the account Restlet
         Restlet account = new Restlet()
            {
         		public void handleGet(Call call)
               {
                  // Print the requested URI path
                  String output = "Account of user named: " + call.getBaseRef().getLastSegment();
                  call.setOutput(new StringRepresentation(output, MediaType.TEXT_PLAIN));
               }
            };
         user.getScorers().add("$", account);

         // Create the orders Restlet
         Restlet orders = new Restlet(myContext)
            {
               public void handleGet(Call call)
               {
                  // Print the user name of the requested orders
                  List<String> segments = call.getBaseRef().getSegments();
                  String output = "Orders of user named: " + segments.get(segments.size() - 2);
                  call.setOutput(new StringRepresentation(output, MediaType.TEXT_PLAIN));
               }
            };
         user.getScorers().add("/orders$", orders);

         // Now, let's start the container!
         myContainer.start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}
