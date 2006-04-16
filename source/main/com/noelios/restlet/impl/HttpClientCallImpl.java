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

package com.noelios.restlet.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ProtocolException;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

import org.restlet.Manager;
import org.restlet.connector.ClientCall;
import org.restlet.data.Parameter;
import org.restlet.data.Representation;

/**
 * Implementation of a client connector call.
 */
public class HttpClientCallImpl extends ConnectorCallImpl implements ClientCall
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.connector.HttpClientCallImpl");

   /** The wrapped HTTP URL connection. */
   protected HttpURLConnection connection;
   
   /**
    * Constructor.
    * @param method The method name.
    * @param resourceUri The resource URI.
    * @param hasInput Indicates if the call will have an input to send to the server.
    * @throws IOException
    */
   public HttpClientCallImpl(String method, String resourceUri, boolean hasInput) throws IOException
   {
      this.requestMethod = method;
      
      if(resourceUri.startsWith("http"))
      {
         URL url = new URL(resourceUri);
         this.connection = (HttpURLConnection)url.openConnection();
         this.connection.setAllowUserInteraction(false);
         this.connection.setDoOutput(hasInput);
         this.connection.setInstanceFollowRedirects(false);
         this.connection.setUseCaches(false);
         this.requestUri = resourceUri;
         this.confidential = (this.connection instanceof HttpsURLConnection);
      }
      else
      {
         throw new IllegalArgumentException("Only HTTP or HTTPS resource URIs are allowed here");
      }
      
      try
      {
         this.requestAddress = InetAddress.getLocalHost().getHostAddress();
      }
      catch(UnknownHostException e)
      {
         this.requestAddress = "127.0.0.1";
      }
   }
   
   /**
    * Returns the connection.
    * @return The connection.
    */
   public HttpURLConnection getConnection()
   {
      return this.connection;
   }

   /**
    * Sets the request method. 
    * @param method The request method.
    */
   public void setRequestMethod(String method)
   {
      this.requestMethod = method;
   }
   
   /**
    * Sends the request headers.<br/>
    * Must be called before sending the request input.
    */
   public void sendRequestHeaders()
   {
      // Set the request method
      try
      {
         getConnection().setRequestMethod(getRequestMethod());
      }
      catch(ProtocolException e)
      {
         logger.log(Level.WARNING, "Unable to set method", e);
      }

      // Set the request headers
      Parameter header;
      for(Iterator<Parameter> iter = getRequestHeaders().iterator(); iter.hasNext();)
      {
         header = iter.next();
         getConnection().addRequestProperty(header.getName(), header.getValue());
      }

      // Ensure that the connections is active
      try
      {
         getConnection().connect();
      }
      catch(IOException ioe)
      {
         logger.log(Level.WARNING, "Unable to connect to the server", ioe);
      }
   }

   /**
    * Sends the request input.
    * @param input The request input;
    */
   public void sendRequestInput(Representation input) throws IOException
   {
      if(getRequestStream() != null)
      {
         input.write(getRequestStream());
         getRequestStream().close();
      }
      else if(getRequestChannel() != null)
      {
         input.write(getRequestChannel());
         getRequestChannel().close();
      }
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

   /**
    * Returns the response address.<br/>
    * Corresponds to the IP address of the responding server.
    * @return The response address.
    */
   public String getResponseAddress()
   {
      return getConnection().getURL().getHost();
   }

   /**
    * Returns the modifiable list of response headers.
    * @return The modifiable list of response headers.
    */
   public List<Parameter> getResponseHeaders()
   {
      if(this.responseHeaders == null)
      {
         this.responseHeaders = new ArrayList<Parameter>();
         
         // Read the response headers
         int i = 1;
         String headerName = getConnection().getHeaderFieldKey(i);
         String headerValue = getConnection().getHeaderField(i);
         while(headerName != null)
         {
            this.responseHeaders.add(Manager.createParameter(headerName, headerValue));
            i++;
            headerName = getConnection().getHeaderFieldKey(i);
            headerValue = getConnection().getHeaderField(i);
         }
      }

      return this.responseHeaders;
   }
   
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
         logger.log(Level.FINE, "Unable to get the response stream", e);
         return null;
      }
   }
}
