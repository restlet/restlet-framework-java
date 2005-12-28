/*
 * Copyright 2005 Jérôme LOUVEL
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

import org.restlet.AbstractRestlet;
import org.restlet.DefaultMaplet;
import org.restlet.Maplet;
import org.restlet.Restlet;
import org.restlet.RestletCall;
import org.restlet.RestletException;
import org.restlet.component.DefaultRestletContainer;
import org.restlet.component.RestletContainer;
import org.restlet.data.MediaTypes;

import com.noelios.restlet.DirectoryRestlet;
import com.noelios.restlet.LoggerChainlet;
import com.noelios.restlet.StatusChainlet;
import com.noelios.restlet.data.StringRepresentation;
import com.noelios.restlet.ext.jetty.JettyServer;

/**
 * Maplets and hierarchical URIs
 */
public class Tutorial10
{
   public static void main(String[] args)
   {
      try
      {
         // Registering the Restlet API implementation
         com.noelios.restlet.Engine.register();
      
         // Create a new Restlet container
         RestletContainer myContainer = new DefaultRestletContainer("My container");

         // Create the HTTP server connector, then add it as a server connector 
         // to the Restlet container. Note that the container is the call handler.
         JettyServer httpServer = new JettyServer("My connector", 8182, myContainer);
         myContainer.addServer(httpServer);

         // Attach a logger Chainlet to the container
         LoggerChainlet logger = new LoggerChainlet(myContainer, "com.noelios.restlet.tutorial");
         myContainer.attach("http://localhost:8182", logger);

         // Attach a status Chainlet to the logger Chainlet
         StatusChainlet status = new StatusChainlet(myContainer, true, "webmaster@mysite.org");
         logger.attach(status);

         // Attach a root Maplet to the status Chainlet.
         Maplet rootMaplet = new DefaultMaplet(myContainer);
         status.attach(rootMaplet);
         
         // Create a directory restlet able to return a deep hierarchy of Web files 
         DirectoryRestlet dirRestlet = new DirectoryRestlet(myContainer, "D:/Restlet/www/docs/api/", true, "index");
         dirRestlet.addExtension("html", MediaTypes.TEXT_HTML);
         dirRestlet.addExtension("css", MediaTypes.TEXT_CSS);
         dirRestlet.addExtension("gif", MediaTypes.IMAGE_GIF);
         rootMaplet.attach("/docs/", dirRestlet);

         // Create the users maplet
         Maplet usersMaplet = new DefaultMaplet(myContainer);
         rootMaplet.attach("/users", usersMaplet);

         // Create the user maplet
         Maplet userMaplet = new DefaultMaplet(myContainer)
            {
               public void handle(RestletCall call) throws RestletException
               {
                  // Print the requested URI path
                  String output = "Account of user named: " + call.getPath(1, true);
                  call.setOutput(new StringRepresentation(output, MediaTypes.TEXT_PLAIN));
               }
            };
         usersMaplet.attach("/[a-z]+", userMaplet);

         // Create the orders Restlet
         Restlet ordersRestlet = new AbstractRestlet(myContainer)
            {
               public void handle(RestletCall call) throws RestletException
               {
                  // Print the requested URI path
                  String output = "Orders of user named: " + call.getPath(2, true);
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
