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

import org.restlet.component.RestletContainer;
import org.restlet.connector.DefaultServer;
import org.restlet.connector.Server;
import org.restlet.data.Protocols;

import com.noelios.restlet.DirectoryRestlet;
import com.noelios.restlet.HostMaplet;
import com.noelios.restlet.LogChainlet;
import com.noelios.restlet.StatusChainlet;

/**
 * Displaying error pages.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Tutorial08
{
   public static void main(String[] args)
   {
      try
      {
         // Create a new Restlet container
      	RestletContainer myContainer = new RestletContainer();

         // Create the HTTP server connector, then add it as a server connector
         // to the Restlet container. Note that the container is the call restlet.
         Server server = new DefaultServer(Protocols.HTTP, "My server", myContainer, 8182);
         myContainer.addServer(server);

         // Attach a log Chainlet to the container
         LogChainlet log = new LogChainlet(myContainer, "com.noelios.restlet.example");
         myContainer.attach(log);

         // Attach a status Chainlet to the log Chainlet
         StatusChainlet status = new StatusChainlet(myContainer, true, "webmaster@mysite.org", "http://www.mysite.org");
         log.attach(status);

         // Create a host Maplet matching calls to the server
         HostMaplet host = new HostMaplet(myContainer, 8182);
         status.attach(host);

         // Create a directory Restlet able to return a deep hierarchy of Web files
         DirectoryRestlet dirRestlet = new DirectoryRestlet(myContainer, "D:/Restlet/www/docs/api/", true, "index", true);

         // Then attach the Restlet to the status Chainlet.
         host.attach("/", dirRestlet);

         // Now, let's start the container!
         myContainer.start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}
