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

import org.restlet.AbstractRestlet;
import org.restlet.Manager;
import org.restlet.Restlet;
import org.restlet.UniformCall;
import org.restlet.component.DefaultRestletContainer;
import org.restlet.component.RestletContainer;
import org.restlet.connector.Server;
import org.restlet.data.MediaTypes;
import org.restlet.data.Protocols;

import com.noelios.restlet.data.StringRepresentation;

/**
 * Restlets servers and containers
 */
public class Tutorial05
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

         // Create a new Restlet that will display some path information.
         Restlet myRestlet = new AbstractRestlet(myContainer)
            {
               public void handle(UniformCall call)
               {
                  // Print the requested URI path
                  String output = "Resource path = " + call.getResourcePath(0, false) + '\n' +
                                  "Restlet path = " + call.getResourcePath(1, false);

                  call.setOutput(new StringRepresentation(output, MediaTypes.TEXT_PLAIN));
               }
            };

         // Then attach it to the container.
         myContainer.attach("http://localhost:8182/trace", myRestlet);

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
