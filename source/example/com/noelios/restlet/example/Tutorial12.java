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

import java.util.List;

import org.restlet.AbstractRestlet;
import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.data.ChallengeSchemes;
import org.restlet.data.MediaTypes;
import org.restlet.data.Protocols;

import com.noelios.restlet.build.RestletContainerBuilder;
import com.noelios.restlet.data.StringRepresentation;

/**
 * Using Builders to simplify configuration. 
 */
public class Tutorial12
{
   public static void main(String[] args)
   {
      try
      {
         // Create the user Restlet
         Restlet userRestlet = new AbstractRestlet()
            {
         		public void handleGet(Call call)
               {
                  // Print the requested URI path
                  String output = "Account of user named: " + call.getContextRef().getLastSegment();
                  call.setOutput(new StringRepresentation(output, MediaTypes.TEXT_PLAIN));
               }
            };

         // Create the orders Restlet
         Restlet ordersRestlet = new AbstractRestlet()
         	{
            	public void handleGet(Call call)
               {
            		// Print the user name of the requested orders
                  List<String> segments = call.getContextRef().getSegments();
                  String output = "Orders of user named: " + segments.get(segments.size() - 2);
                  call.setOutput(new StringRepresentation(output, MediaTypes.TEXT_PLAIN));
               }
            };
      	
         // Build and start the container
      	new RestletContainerBuilder("My container")
      		.addServer(Protocols.HTTP, "My server", 8182)
      		.attachLog("com.noelios.restlet.example")
      			.attachStatus(true, "webmaster@mysite.org", "http://www.mysite.org")
      				.attachHost(8182)
      					.attachGuard("/docs/", "com.noelios.restlet.example", true, ChallengeSchemes.HTTP_BASIC , "Restlet tutorial", true)
      						.authorize("scott", "tiger")
      						.attachDirectory("D:/Restlet/www/docs/api/", true, "index")
      							.addExtension("html", MediaTypes.TEXT_HTML)
      							.addExtension("css", MediaTypes.TEXT_CSS)
      							.addExtension("gif", MediaTypes.IMAGE_GIF)
      						.up()
   						.up().toMaplet()
   						.attachMaplet("/users")
   							.attachMaplet("/[a-z]+")
   								.attach("$", userRestlet)
   									.up().toMaplet()
									.attach("/orders$", ordersRestlet)
										.owner().start();
      }
      catch(Exception e)
      {
         e.printStackTrace();
      }
   }

}
