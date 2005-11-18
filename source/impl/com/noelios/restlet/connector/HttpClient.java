/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.noelios.restlet.connector;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.restlet.UniformCall;
import org.restlet.connector.AbstractConnector;
import org.restlet.connector.Client;
import org.restlet.data.MediaType;
import org.restlet.data.Methods;
import org.restlet.data.Representation;

import com.noelios.restlet.UniformCallImpl;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.MediaTypeImpl;
import com.noelios.restlet.data.ReferenceImpl;
import com.noelios.restlet.data.StatusImpl;

/**
 * HTTP client base on J2SE 5.0 HttpURLConnection class.
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
    * @return            The representation returned.
    */   
   public Representation doGet(String resourceUri)
   {
      UniformCall call = new UniformCallImpl();
      call.setResourceUri(new ReferenceImpl(resourceUri));
      call.setMethod(Methods.GET);
      handle(call);
      return call.getOutput();
   }

   /**
    * Shortcut method that does a HTTP POST on a remote resource.
    * @param resourceUri The URI of the resource to post to.
    * @param input       The input representation to post.
    * @return            The representation returned.
    */   
   public Representation doPost(String resourceUri, Representation input)
   {
      UniformCall call = new UniformCallImpl();
      call.setResourceUri(new ReferenceImpl(resourceUri));
      call.setMethod(Methods.POST);
      call.setInput(input);
      handle(call);
      return call.getOutput();
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
         URL url = new URL(call.getResourceUri().toString());
         HttpURLConnection huc = (HttpURLConnection)url.openConnection();

         // Add the referrer header
         if(call.getReferrerUri() != null)
         {
            huc.setRequestProperty("Referer", call.getReferrerUri().toString());
         }

         // Add the user agent header
         if(call.getUserAgentName() != null)
         {
            huc.setRequestProperty("User-Agent", call.getUserAgentName());
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
         MediaType mediaType = new MediaTypeImpl(huc.getContentType());
         call.setStatus(new StatusImpl(huc.getResponseCode()));
         
         // Get the response output
         call.setOutput(new InputRepresentation(huc.getInputStream(), mediaType));
         
         // Get the cookie settings
         // ...
      }
      catch (MalformedURLException e)
      {
         e.printStackTrace();
      }
      catch (IOException e)
      {
         e.printStackTrace();
      }
   }
   
}
