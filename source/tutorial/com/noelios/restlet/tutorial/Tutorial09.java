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

import org.restlet.component.DefaultRestletContainer;
import org.restlet.component.RestletContainer;

import com.noelios.restlet.RedirectRestlet;
import com.noelios.restlet.ext.jetty.JettyServer;

/**
 * URI rewriting and redirection
 */
public class Tutorial09
{
   public static void main(String[] args)
   {
      try
      {
         // Registering the Restlet API implementation
         com.noelios.restlet.Engine.register();
      
         // Create a new restlet container
         RestletContainer myContainer = new DefaultRestletContainer("My container");

         // Create the HTTP server connector, then add it as a server connector 
         // to the restlet container. Note that the container is the call handler.
         JettyServer httpServer = new JettyServer("My connector", 8182, myContainer);
         myContainer.addServer(httpServer);
         
         // Create a directory restlet able to return a deep hierarchy of Web files 
         // (HTML pages, CSS stylesheets or GIF images) from a local directory.
         String target = "http://www.google.com/search?q=site:mysite.org+${query[\"query\"]}";
         RedirectRestlet searchRedirect = new RedirectRestlet(myContainer, target, RedirectRestlet.MODE_CLIENT_TEMPORARY);
         myContainer.attach("http://localhost:8182/search", searchRedirect);
            
         // Now, let's start the container! Note that the HTTP server connector is
         // also automatically started.
         myContainer.start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}
