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

import java.io.IOException;

import org.restlet.DefaultCall;
import org.restlet.Manager;
import org.restlet.connector.Client;
import org.restlet.data.Methods;
import org.restlet.data.Protocols;
import org.restlet.data.Representation;

/**
 * Retrieving the content of a Web page (detailled).
 */
public class Tutorial02b
{
   public static void main(String[] args)
   {
      try
      {
         // Prepare the REST call
      	DefaultCall call = new DefaultCall();
         call.setResourceRef("http://www.restlet.org");
         call.setReferrerRef("http://www.mysite.org");
         call.setMethod(Methods.GET);

         // Ask to the HTTP client connector to handle the call
         Client client = Manager.createClient(Protocols.HTTP, "My client");
         client.handle(call);

         // Output the result representation on the JVM console
         Representation output = call.getOutput();
         output.write(System.out);
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }

}
