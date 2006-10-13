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

package com.noelios.restlet.example.misc;

import org.restlet.Container;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

/**
 * Simple HTTP server invoked by the simple client.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class SimpleServer
{
   public static void main(String[] args)
   {
      try
      {
         // Create a new Restlet container
      	Container myContainer = new Container();
         Context myContext = myContainer.getContext();
         
         // Create the HTTP server connector, then add it as a server
         // connector to the Restlet container. Note that the container
         // is the call restlet.
         myContainer.getServers().add(Protocol.HTTP, 9876);

         // Prepare and attach a test Restlet
         Restlet testRestlet = new Restlet(myContext)
         {
            public void handlePut(Request request, Response response)
            {
               System.out.println("Handling the call...");
               System.out.println("Trying to get the input as a form...");
               Form form = request.getEntityAsForm();

               System.out.println("Trying to getParameters...");
               StringBuffer sb = new StringBuffer("foo");
               for(Parameter p : form)
               {
                  System.out.println(p);

                  sb.append("field name = ");
                  sb.append(p.getName());
                  sb.append("value = ");
                  sb.append(p.getValue());
                  sb.append("\n");
                  System.out.println(sb.toString());
               }

               response.setEntity(sb.toString(), MediaType.TEXT_PLAIN);
            }
         };

         myContainer.getDefaultHost().attach("/test", testRestlet);

         // Now, start the container
         myContainer.start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
}
