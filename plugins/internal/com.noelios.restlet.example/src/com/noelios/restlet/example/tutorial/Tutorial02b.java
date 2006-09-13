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

package com.noelios.restlet.example.tutorial;

import java.io.IOException;

import org.restlet.Call;
import org.restlet.connector.Client;
import org.restlet.connector.GenericClient;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Representation;

/**
 * Retrieving the content of a Web page (detailled).
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Tutorial02b
{
   public static void main(String[] args)
   {
      try
      {
         // Prepare the REST call
      	Call call = new Call(Method.GET, "http://www.restlet.org");
         call.setReferrerRef("http://www.mysite.org");

         // Ask to the HTTP client connector to handle the call
         Client client = new GenericClient(Protocol.HTTP);
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
