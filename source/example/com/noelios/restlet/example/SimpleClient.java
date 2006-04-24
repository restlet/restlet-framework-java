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

import org.restlet.DefaultCall;
import org.restlet.Call;
import org.restlet.connector.Client;
import org.restlet.connector.GenericClient;
import org.restlet.data.Form;
import org.restlet.data.Methods;
import org.restlet.data.Protocols;
import org.restlet.data.Representation;

/**
 * Simple HTTP client calling the simple server.
 */
public class SimpleClient
{
   public static void main(String[] args)
   {
      try
      {
         // Prepare the REST call.
         Call call = new DefaultCall();

         // Identify oursevles.
         call.setReferrerRef("http://www.foo.com/");

         // Target resource.
         call.setResourceRef("http://localhost:9876/test");

         // Action: Update
         call.setMethod(Methods.PUT);

         Form form = new Form();
         form.addParameter("name", "John D. Mitchell");
         form.addParameter("email", "john@bob.net");
         form.addParameter("email2", "joe@bob.net");
         call.setInput(form.getRepresentation());

         // Prepare HTTP client connector.
         Client client = new GenericClient(Protocols.HTTP, "tester");

         // Make the call.
         client.handle(call);

         if(call.getStatus().isSuccess())
         {
            // Output the result representation on the JVM console
            Representation output = call.getOutput();
            output.write(System.out);
            System.out.println("client: success!");
         }
         else
         {
            System.out.println("client: failure!");
            System.out.println(call.getStatus().getDescription());
         }
      }
      catch(Exception ex)
      {
         ex.printStackTrace();
      }
   }
}
