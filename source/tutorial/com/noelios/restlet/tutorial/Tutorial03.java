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

import org.restlet.UniformCall;
import org.restlet.UniformInterface;
import org.restlet.data.MediaTypes;

import com.noelios.restlet.data.StringRepresentation;
import com.noelios.restlet.ext.jetty.JettyServer;

/**
 * Listening to Web browsers
 */
public class Tutorial03
{
   public static void main(String[] args)
   {
      try
      {
         // Registering the Restlet API implementation
         com.noelios.restlet.Engine.register();

         // Creating a minimal handler returning "Hello World"
         UniformInterface handler = new UniformInterface()
         {
            public void handle(UniformCall call)
            {
               call.setOutput(new StringRepresentation("Hello World!", MediaTypes.TEXT_PLAIN));
            }
         };

         // Create the HTTP server and listen on port 8182
         new JettyServer("My Web server", 8182, handler).start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}
