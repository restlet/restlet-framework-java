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

package com.noelios.restlet.test;

import java.io.IOException;

import junit.framework.TestCase;

import org.restlet.AbstractRestlet;
import org.restlet.DefaultCall;
import org.restlet.Manager;
import org.restlet.RestletCall;
import org.restlet.Restlet;
import org.restlet.component.RestletContainer;
import org.restlet.data.MediaTypes;
import org.restlet.data.Method;
import org.restlet.data.Methods;
import org.restlet.data.Protocols;

import com.noelios.restlet.RedirectRestlet;
import com.noelios.restlet.data.StringRepresentation;

/**
 * Unit tests for the Cookie related classes.
 */
public class RedirectTest extends TestCase
{
   /**
    * Tests the cookies parsing.
    */
   public void testRedirect() throws IOException
   {
      try
      {
         // Create a new Restlet container
      	RestletContainer myContainer = new RestletContainer("My container");

         // Create the client connectors
         myContainer.addClient(Manager.createClient(Protocols.HTTP, "Test client"));
         myContainer.addClient(Manager.createClient(Protocols.HTTP, "Proxy client"));

         // Create the proxy Restlet
         String target = "http://localhost:9090${path}${if query}?${query}${end}";
         RedirectRestlet proxy = new RedirectRestlet(myContainer, target, RedirectRestlet.MODE_CONNECTOR);
         proxy.setConnectorName("Proxy client");

         // Create a new Restlet that will display some path information.
         Restlet trace = new AbstractRestlet(myContainer)
            {
               public void handle(RestletCall call)
               {
                  // Print the requested URI path
                  String output = "Resource URI:  " + call.getResourceRef() + '\n' + 
                                  "Context path:  " + call.getContextPath() + '\n' +
                                  "Resource path: " + call.getResourcePath() + '\n' +
                                  "Query string:  " + call.getResourceRef().getQuery() + '\n' + 
                                  "Method name:   " + call.getMethod() + '\n';
                  call.setOutput(new StringRepresentation(output, MediaTypes.TEXT_PLAIN));
               }
            };
         
         // Create the server connectors
         myContainer.addServer(Manager.createServer(Protocols.HTTP, "Proxy server", proxy, null, 8080));
         myContainer.addServer(Manager.createServer(Protocols.HTTP, "Origin server", trace, null, 9090));

         // Now, let's start the container!
         myContainer.start();
         
         // Tests
         String uri = "http://localhost:8080/?foo=bar";
         testCall(myContainer, Methods.GET, uri);
         testCall(myContainer, Methods.POST, uri);
         testCall(myContainer, Methods.PUT, uri);
         testCall(myContainer, Methods.DELETE, uri);

         uri = "http://localhost:8080/abcd/efgh/ijkl?foo=bar&foo=beer";
         testCall(myContainer, Methods.GET, uri);
         testCall(myContainer, Methods.POST, uri);
         testCall(myContainer, Methods.PUT, uri);
         testCall(myContainer, Methods.DELETE, uri);
         
         uri = "http://localhost:8080/v1/client/kwse/CnJlNUQV9%252BNNqbUf7Lhs2BYEK2Y%253D/user/johnm/uVGYTDK4kK4zsu96VHGeTCzfwso%253D/";
         testCall(myContainer, Methods.GET, uri);
         
         // Stop the container
         myContainer.stop();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }
   
   private void testCall(RestletContainer myContainer, Method method, String uri)
   {
      try
      {
         RestletCall call = new DefaultCall();
         call.setMethod(method);
         call.setResourceRef(uri);
         myContainer.callClient("Test client", call);
         call.getOutput().write(System.out);
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }
   
   public static void main(String[] args)
   {
      try
      {
         new RedirectTest().testRedirect();
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }
}
