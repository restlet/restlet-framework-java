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

import org.restlet.AbstractRestlet;
import org.restlet.Manager;
import org.restlet.RestletCall;
import org.restlet.Restlet;
import org.restlet.data.MediaTypes;
import org.restlet.data.Protocols;

import com.noelios.restlet.data.StringRepresentation;

/**
 * Listening to Web browsers.
 */
public class Tutorial03
{
   public static void main(String[] args)
   {
      try
      {
         // Creating a minimal Restlet returning "Hello World"
         Restlet handler = new AbstractRestlet()
         {
            public void handle(RestletCall call)
            {
               call.setOutput(new StringRepresentation("Hello World!", MediaTypes.TEXT_PLAIN));
            }
         };

         // Create the HTTP server and listen on port 8182
         Manager.createServer(Protocols.HTTP, "My server", handler, null, 8182).start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}
