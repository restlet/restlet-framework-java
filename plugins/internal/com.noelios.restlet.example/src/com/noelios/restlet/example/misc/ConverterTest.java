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

import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.connector.GenericServer;
import org.restlet.data.Protocol;

import com.noelios.restlet.data.StringRepresentation;

/**
 * Display the HTTP accept header sent by the Web browsers.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ConverterTest
{
   public static void main(String[] args)
   {
      try
      {
         Restlet handler = new Restlet()
         {
            public void handleGet(Call call)
            {
            	String acceptHeader = (String)call.getAttributes().get("Accept");
               call.setOutput(new StringRepresentation("Accept header: " + acceptHeader));
            }
         };

         // Create the HTTP server and listen on port 8182
         GenericServer server = new GenericServer(Protocol.HTTP, 8182, handler);
         server.getContext().getParameters().add("converter", "com.noelios.restlet.example.misc.ConverterExample");
         server.start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}
