/*
 * Copyright 2005-2006 Jerome LOUVEL
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
import org.restlet.Manager;
import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.component.RestletContainer;
import org.restlet.connector.Server;
import org.restlet.data.Form;
import org.restlet.data.MediaTypes;
import org.restlet.data.Methods;
import org.restlet.data.Parameter;
import org.restlet.data.Protocols;
import org.restlet.data.Statuses;

import com.noelios.restlet.data.StringRepresentation;

/**
 * Simple HTTP server invoked by the simple client.
 */
public class SimpleServer
{
   public static void main(String[] args)
   {
      try
      {
         // Create a new Restlet container
         RestletContainer myContainer = new RestletContainer("My container");

         // Create the HTTP server connector, then add it as a server
         // connector to the Restlet container. Note that the container
         // is the call restlet.
         Server server = Manager.createServer(Protocols.HTTP, "My connector", myContainer, null, 9876);
         myContainer.addServer(server);

         Restlet testRestlet = new AbstractRestlet(myContainer)
         {
            public void handle(Call call)
            {
               System.out.println("Handling the call...");

               if(!call.getMethod().equals(Methods.PUT))
               {
                  System.out.println("Not a PUT!");
                  call.setStatus(Statuses.CLIENT_ERROR_METHOD_NOT_ALLOWED);
                  call.setOutput(new StringRepresentation("Sorry, only PUT is supported.", MediaTypes.TEXT_PLAIN));
                  return;
               }

               System.out.println("Trying to get the input as a form...");
               Form form = call.getInputAsForm();

               System.out.println("Trying to getParameters...");
               List<Parameter> params = form.getParameters();
               StringBuffer sb = new StringBuffer("foo");
               for(Parameter p : params)
               {
                  System.out.println(p);

                  sb.append("field name = ");
                  sb.append(p.getName());
                  sb.append("value = ");
                  sb.append(p.getValue());
                  sb.append("\n");
                  System.out.println(sb.toString());
               }

               call.setOutput(new StringRepresentation(sb.toString(), MediaTypes.TEXT_PLAIN));
            }
         };

         myContainer.attach("http://localhost:9876/test", testRestlet);
         myContainer.start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
}
