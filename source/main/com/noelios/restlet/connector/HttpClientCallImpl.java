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
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.connector.HttpClientCall;

/**
 * Base class for HTTP based uniform calls.
 */
public class HttpClientCallImpl implements HttpClientCall
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.connector.HttpClientCall");

   protected HttpURLConnection connection;
   
   public HttpClientCallImpl(String resourceUri) throws IOException
   {
      URL url = new URL(resourceUri);
      this.connection = (HttpURLConnection)url.openConnection();
   }
   
   public HttpURLConnection getConnection()
   {
      return this.connection;
   }
   
   
   // ----------------------
   // ---  Request part  ---
   // ----------------------

   /**
    * Returns the request method. 
    * @return The request method.
    */
   public String getRequestMethod()
   {
      return getConnection().getRequestMethod();
   }

   /**
    * Sets the request method. 
    * @param method The request method.
    */
   public void setRequestMethod(String method)
   {
      try
      {
         getConnection().setRequestMethod(method);
      }
      catch(ProtocolException e)
      {
         logger.log(Level.WARNING, "Unable to set method", e);
      }
   }

   /**
    * Returns the full request URI. 
    * @return The full request URI.
    */
   public String getRequestUri()
   {
      return getConnection().getURL().toString();
   }
   
   /**
    * Returns a request header value.
    * @param name The name of the header.
    * @return A header value.
    */
   public String getRequestHeader(String name)
   {
      return getConnection().getRequestProperty(name);
   }
   
   /**
    * Returns a request header value.
    * @param name The header name.
    * @param value The header value.
    */
   public void setRequestHeader(String name, String value)
   {
      getConnection().setRequestProperty(name, value);
   }

   /**
    * Returns the request entity channel if it exists.
    * @return The request entity channel if it exists.
    */
   public WritableByteChannel getRequestChannel()
   {
      return null;
   }
   
   /**
    * Returns the request entity stream if it exists.
    * @return The request entity stream if it exists.
    */
   public OutputStream getRequestStream()
   {
      try
      {
         return getConnection().getOutputStream();
      }
      catch(IOException e)
      {
         logger.log(Level.WARNING, "Unable to get the request stream", e);
         return null;
      }
   }

   // -----------------------
   // ---  Response part  ---
   // -----------------------
   
   /**
    * Returns the response status code.
    * @return The response status code.
    */
   public int getResponseStatusCode()
   {
      try
      {
         return getConnection().getResponseCode();
      }
      catch(IOException e)
      {
         return -1;
      }
   }

   /**
    * Returns the response reason phrase.
    * @return The response reason phrase.
    */
   public String getResponseReasonPhrase()
   {
      try
      {
         return getConnection().getResponseMessage();
      }
      catch(IOException e)
      {
         return null;
      }
   }
   
   /**
    * Returns a response header value.
    * @param name The name of the header.
    * @return A header value.
    */
   public String getResponseHeader(String name)
   {
      return getConnection().getHeaderField(name);
   }
   
   /**
    * Returns a response date header value.
    * @param name The name of the header.
    * @return A header date.
    */
   public Date getResponseDateHeader(String name)
   {
      long result = getConnection().getHeaderFieldDate(name, -1);
      
      if(result != -1) 
      {
         return new Date(result);
      }
      else
      {
         return null;
      }
   }

   /**
    * Returns the response channel if it exists.
    * @return The response channel if it exists.
    */
   public ReadableByteChannel getResponseChannel()
   {
      return null;
   }
   
   /**
    * Returns the response stream if it exists.
    * @return The response stream if it exists.
    */
   public InputStream getResponseStream()
   {
      try
      {
         return getConnection().getInputStream();
      }
      catch(IOException e)
      {
         logger.log(Level.WARNING, "Unable to get the response stream", e);
         return null;
      }
   }
   
}
