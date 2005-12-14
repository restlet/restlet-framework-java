/*
 * Copyright 2005 Jérôme LOUVEL
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

import java.io.IOException;

import com.noelios.restlet.connector.HttpClient;

public class Tutorial2a
{
   public static void main(String[] args)
   {
      // Registering the Restlet API implementation
      com.noelios.restlet.Engine.register();

      // Outputting the content of a Web page
      try
      {
         HttpClient client = new HttpClient("My Web client");
         client.doGet("http://restlet.org").getOutput().write(System.out);
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }

}
