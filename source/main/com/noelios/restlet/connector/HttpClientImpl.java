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

package com.noelios.restlet.connector;

import java.io.IOException;
import java.net.MalformedURLException;

import org.restlet.UniformCall;
import org.restlet.connector.AbstractClient;
import org.restlet.connector.HttpCall;
import org.restlet.connector.HttpClient;
import org.restlet.connector.HttpClientCall;
import org.restlet.data.Representation;

import com.noelios.restlet.data.ContentType;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.StatusImpl;

/**
 * HTTP client based on J2SE 5.0 HttpURLConnection class.
 */
public class HttpClientImpl extends AbstractClient implements HttpClient
{
   /**
    * Constructor.
    * @param name The name of this REST connector.
    */
   public HttpClientImpl(String name)
   {
      super(name);
   }

   /**
    * Handles a uniform call.
    * @param call The uniform call to handle.
    */
   public void handle(UniformCall call)
   {
      try
      {
         // Create a new HTTP client call
         HttpClientCall clientCall = createCall(call.getResourceRef().toString());
         
         // Add the referrer header
         if(call.getReferrerRef() != null)
         {
            clientCall.setRequestHeader(HttpCall.HEADER_REFERRER, call.getReferrerRef().toString());
         }

         // Add the user agent header
         if(call.getClientName() != null)
         {
            clientCall.setRequestHeader(HttpCall.HEADER_USER_AGENT, call.getClientName());
         }
         else
         {
            clientCall.setRequestHeader(HttpCall.HEADER_USER_AGENT, "HttpURLConnection/5.0");
         }

         // Add the media type preferences
         // ...

         // Add the character set preferences
         // ...

         // Add the language preferences
         // ...

         // Set the request method
         clientCall.setRequestMethod(call.getMethod().getName());

         // Add the cookies
         // ...

         // Send the input representation
         if(call.getInput() != null)
         {
            call.getInput().write(clientCall.getRequestStream());
         }

         // Get the response status
         call.setStatus(new StatusImpl(clientCall.getResponseStatusCode()));

         // Get the response output
         ContentType contentType = new ContentType(clientCall.getResponseHeader(HttpCall.HEADER_CONTENT_TYPE));
         Representation output = new InputRepresentation(clientCall.getResponseStream(), contentType.getMediaType());
         output.getMetadata().setCharacterSet(contentType.getCharacterSet());
         call.setOutput(output);

         // Get the cookie settings
         // ...
      }
      catch(MalformedURLException e)
      {
         e.printStackTrace();
      }
      catch(IOException e)
      {
         e.printStackTrace();
      }
   }

   /**
    * Returns a new HTTP protocol call.
    * @return A new HTTP protocol call.
    */
   public HttpClientCall createCall(String resourceUri)
   {
      try
      {
         return new HttpClientCallImpl(resourceUri);
      }
      catch(IOException e)
      {
         return null;
      }
   }

}
