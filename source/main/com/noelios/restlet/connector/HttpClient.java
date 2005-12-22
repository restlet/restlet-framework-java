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

package com.noelios.restlet.connector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.restlet.UniformCall;
import org.restlet.connector.AbstractConnector;
import org.restlet.connector.Client;
import org.restlet.data.Methods;
import org.restlet.data.Representation;

import com.noelios.restlet.UniformCallImpl;
import com.noelios.restlet.data.ContentType;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.ReferenceImpl;
import com.noelios.restlet.data.StatusImpl;

/**
 * HTTP client based on J2SE 5.0 HttpURLConnection class.
 */
public class HttpClient extends AbstractConnector implements Client
{
   /**
    * Constructor.
    * @param name The name of this REST connector.
    */
   public HttpClient(String name)
   {
      super(name);
   }

   /**
    * Shortcut method that does a HTTP GET on a remote resource.
    * @param resourceUri The URI of the resource to get.
    * @return The representation returned.
    */
   public UniformCall doGet(String resourceUri)
   {
      UniformCall call = new UniformCallImpl();
      call.setResourceRef(new ReferenceImpl(resourceUri));
      call.setMethod(Methods.GET);
      handle(call);
      return call;
   }

   /**
    * Shortcut method that does a HTTP POST on a remote resource.
    * @param resourceUri The URI of the resource to post to.
    * @param input The input representation to post.
    * @return The representation returned.
    */
   public UniformCall doPost(String resourceUri, Representation input)
   {
      UniformCall call = new UniformCallImpl();
      call.setResourceRef(new ReferenceImpl(resourceUri));
      call.setMethod(Methods.POST);
      call.setInput(input);
      handle(call);
      return call;
   }

   /**
    * Shortcut method that does a HTTP PUT on a remote resource.
    * @param resourceUri The URI of the resource to modify.
    * @param input The input representation to put.
    * @return The representation returned.
    */
   public UniformCall doPut(String resourceUri, Representation input)
   {
      UniformCall call = new UniformCallImpl();
      call.setResourceRef(new ReferenceImpl(resourceUri));
      call.setMethod(Methods.PUT);
      call.setInput(input);
      handle(call);
      return call;
   }

   /**
    * Shortcut method that does a HTTP DELETE on a remote resource.
    * @param resourceUri The URI of the resource to delete.
    * @return The representation returned.
    */
   public UniformCall doDelete(String resourceUri)
   {
      UniformCall call = new UniformCallImpl();
      call.setResourceRef(new ReferenceImpl(resourceUri));
      call.setMethod(Methods.DELETE);
      handle(call);
      return call;
   }

   /**
    * Handles a uniform call.
    * @param call The uniform call to handle.
    */
   public void handle(UniformCall call)
   {
      try
      {
         // Create a new HTTP connection
         URL url = new URL(call.getResourceRef().toString());
         HttpURLConnection huc = (HttpURLConnection)url.openConnection();

         // Add the referrer header
         if(call.getReferrerRef() != null)
         {
            huc.setRequestProperty("Referer", call.getReferrerRef().toString());
         }

         // Add the user agent header
         if(call.getClientName() != null)
         {
            huc.setRequestProperty("User-Agent", call.getClientName());
         }
         else
         {
            huc.setRequestProperty("User-Agent", "Noelios-Restlet-Engine/0.11b HttpURLConnection/5.0");
         }

         // Add the media type preferences
         // ...

         // Add the character set preferences
         // ...

         // Add the language preferences
         // ...

         // Set the request method
         huc.setRequestMethod(call.getMethod().getName());

         // Add the cookies
         // ...

         // Send the input representation
         if(call.getInput() != null)
         {
            call.getInput().write(huc.getOutputStream());
         }

         // Get the response status
         call.setStatus(new StatusImpl(huc.getResponseCode()));

         // Get the response output
         ContentType contentType = new ContentType(huc.getContentType());
         Representation output = new InputRepresentation(huc.getInputStream(), contentType.getMediaType());
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

}
