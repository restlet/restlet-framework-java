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

package com.noelios.restlet.example;

import java.util.List;

import org.restlet.AbstractRestlet;
import org.restlet.Call;
import org.restlet.DefaultMaplet;
import org.restlet.Maplet;
import org.restlet.Restlet;
import org.restlet.component.RestletContainer;
import org.restlet.data.ChallengeSchemes;
import org.restlet.data.MediaTypes;
import org.restlet.data.Protocols;

import com.noelios.restlet.DirectoryRestlet;
import com.noelios.restlet.GuardChainlet;
import com.noelios.restlet.HostMaplet;
import com.noelios.restlet.LogChainlet;
import com.noelios.restlet.StatusChainlet;
import com.noelios.restlet.data.StringRepresentation;

/**
 * Maplets and hierarchical URIs
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Tutorial11
{
   public static void main(String[] args)
   {
      try
      {
         // Create a new Restlet container
      	RestletContainer myContainer = new RestletContainer();

         // Add an HTTP server connector to the Restlet container. 
         // Note that the container is the call restlet.
         myContainer.addServer("HTTP Server", Protocols.HTTP, 8182);

         // Add a file client connector to the Restlet container. 
         myContainer.addClient("File Client", Protocols.FILE);

         // Attach a log Chainlet to the container
         LogChainlet log = new LogChainlet(myContainer, "com.noelios.restlet.example");
         myContainer.attach(log);

         // Attach a status Chainlet to the log Chainlet
         StatusChainlet status = new StatusChainlet(myContainer, true, "webmaster@mysite.org", "http://www.mysite.org");
         log.attach(status);

         // Create a host Maplet matching calls to the server
         HostMaplet host = new HostMaplet(myContainer, 8182);
         status.attach(host);

         // Attach a guard Chainlet to secure access the the chained directory Restlet
         GuardChainlet guard = new GuardChainlet(myContainer, "com.noelios.restlet.example", true, ChallengeSchemes.HTTP_BASIC , "Restlet tutorial", true);
         guard.getAuthorizations().put("scott", "tiger");
         host.attach("/docs/", guard);

         // Create a directory Restlet able to return a deep hierarchy of Web files
         DirectoryRestlet dirRestlet = new DirectoryRestlet(myContainer, "File Client", "D:/Restlet/www/docs/api/", true, "index");
         guard.attach(dirRestlet);

         // Create the user Maplet
         Maplet userMaplet = new DefaultMaplet(myContainer);
         host.attach("/users/[a-z]+", userMaplet);

         // Create the account Restlet
         Restlet accountRestlet = new AbstractRestlet()
            {
         		public void handleGet(Call call)
               {
                  // Print the requested URI path
                  String output = "Account of user named: " + call.getContextRef().getLastSegment();
                  call.setOutput(new StringRepresentation(output, MediaTypes.TEXT_PLAIN));
               }
            };
         userMaplet.attach("$", accountRestlet);

         // Create the orders Restlet
         Restlet ordersRestlet = new AbstractRestlet(myContainer)
            {
               public void handleGet(Call call)
               {
                  // Print the user name of the requested orders
                  List<String> segments = call.getContextRef().getSegments();
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
