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

import java.io.IOException;

import org.restlet.Call;
import org.restlet.connector.Client;
import org.restlet.connector.DefaultClient;
import org.restlet.data.ChallengeResponse;
import org.restlet.data.ChallengeSchemes;
import org.restlet.data.Methods;
import org.restlet.data.Protocols;
import org.restlet.data.Representation;
import org.restlet.data.Statuses;

/**
 * Authenticating to an HTTP server.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Tutorial09b
{
   public static void main(String[] args)
   {
      try
      {
         // Prepare the REST call
      	Call call = new Call(Methods.GET, "http://localhost:8182/");
         
         // Add the client authentication to the call 
         ChallengeResponse authentication = new ChallengeResponse(ChallengeSchemes.HTTP_BASIC, "scott", "tiger");
         call.getSecurity().setChallengeResponse(authentication);

         // Ask to the HTTP client connector to handle the call
         Client client = new DefaultClient(Protocols.HTTP);
         client.handle(call);

         if(call.getStatus().isSuccess())
         {
            // Output the result representation on the JVM console
            Representation output = call.getOutput();
            output.write(System.out);
         }
         else if(call.getStatus().equals(Statuses.CLIENT_ERROR_UNAUTHORIZED))
         {
            // Unauthorized access
            System.out.println("Your access was not authorized by the server, check your credentials");
         }
         else
         {
            // Unexpected status
            System.out.println("An unexpected status was returned: " + call.getStatus());
         }
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }

}
