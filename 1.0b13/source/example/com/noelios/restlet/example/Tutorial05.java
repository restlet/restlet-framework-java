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

import org.restlet.AbstractRestlet;
import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.component.RestletContainer;
import org.restlet.connector.DefaultServer;
import org.restlet.connector.Server;
import org.restlet.data.Protocols;

import com.noelios.restlet.HostMaplet;
import com.noelios.restlet.data.StringRepresentation;

/**
 * Restlets servers and containers.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Tutorial05
{
   public static void main(String[] args)
   {
      try
      {
         // Create a new Restlet container
         RestletContainer myContainer = new RestletContainer();

         // Create the HTTP server connector, then add it to the container. 
         // Note that the container will act as the initial Restlet call's handler.
         Server server = new DefaultServer(Protocols.HTTP, "My server", myContainer, 8182);
         myContainer.addServer(server);

         // Create a host Maplet matching calls to the server
         HostMaplet rootMaplet = new HostMaplet(myContainer, 8182);
         myContainer.attach(rootMaplet);

         // Create a new Restlet that will display some path information.
         Restlet myRestlet = new AbstractRestlet(myContainer)
            {
               public void handleGet(Call call)
               {
                  // Print the requested URI path
                  String output = "Resource URI:  " + call.getResourceRef() + '\n' +
                                  "Context path:  " + call.getContextPath() + '\n' +
                                  "Resource path: " + call.getResourcePath() + '\n' +
                                  "Query string:  " + call.getResourceRef().getQuery();
                  call.setOutput(new StringRepresentation(output));
               }
            };

         // Then attach it to the root Maplet.
         rootMaplet.attach("/trace", myRestlet);

         // Now, let's start the container!
         // Note that the HTTP server connector is also automatically started.
         myContainer.start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}
