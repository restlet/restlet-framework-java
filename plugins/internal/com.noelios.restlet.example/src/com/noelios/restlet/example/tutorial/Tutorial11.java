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

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.component.Container;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;

import com.noelios.restlet.DirectoryFinder;
import com.noelios.restlet.GuardFilter;
import com.noelios.restlet.impl.LogFilter;
import com.noelios.restlet.impl.StatusFilter;

/**
 * Routers and hierarchical URIs
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Tutorial11 implements Constants
{
   public static void main(String[] args) throws Exception
   {
      // Create a new Restlet container
   	Container myContainer = new Container();
      Context myContext = myContainer.getContext();

      // Add an HTTP server connector to the Restlet container. 
      // Note that the container is the call restlet.
      myContainer.getServers().add(Protocol.HTTP, 8182);

      // Attach a log Filter to the container
      LogFilter log = new LogFilter(myContext, "com.noelios.restlet.example");
      myContainer.getLocalHost().attach("/", log);

      // Attach a status Filter to the log Filter
      StatusFilter status = new StatusFilter(myContext, true, "webmaster@mysite.org", "http://www.mysite.org");
      log.setNext(status);

      // Create a first Router
      Router router = new Router(myContext);
      status.setNext(router);

      // Attach a guard Filter to secure access the the chained directory Finder
      GuardFilter guard = new GuardFilter(myContext, "com.noelios.restlet.example", true, ChallengeScheme.HTTP_BASIC , "Restlet tutorial", true);
      guard.getAuthorizations().put("scott", "tiger");
      router.getScorers().add("/docs/", guard);

      // Create a directory Restlet able to return a deep hierarchy of Web files
      DirectoryFinder directory = new DirectoryFinder(myContext, ROOT_URI, "index.html");
      guard.setNext(directory);

      // Create the user router
      Router user = new Router(myContext);
      router.attach("/users/[a-z]+", user);

      // Create the account Restlet
      Restlet account = new Restlet()
         {
      		public void handleGet(Request request, Response response)
            {
               // Print the requested URI path
               String output = "Account of user named: " + request.getBaseRef().getLastSegment();
               response.setOutput(output, MediaType.TEXT_PLAIN);
            }
         };
      user.attach("$", account);

      // Create the orders Restlet
      Restlet orders = new Restlet(myContext)
         {
            public void handleGet(Request request, Response response)
            {
               // Print the user name of the requested orders
               List<String> segments = request.getBaseRef().getSegments();
               String output = "Orders of user named: " + segments.get(segments.size() - 2);
               response.setOutput(output, MediaType.TEXT_PLAIN);
            }
         };
      user.attach("/orders$", orders);

      // Now, let's start the container!
      myContainer.start();
   }

}
