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

package com.noelios.restlet.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

import org.restlet.connector.Client;
import org.restlet.connector.ClientCall;
import org.restlet.data.Parameter;
import org.restlet.data.Representation;

/**
 * Implementation of a client connector call.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpClientCallImpl extends ConnectorCallImpl implements ClientCall
{
   /** The wrapped HTTP URL connection. */
   protected HttpURLConnection connection;
   
   /**
    * Constructor.
    * @param client The client connector.
    * @param method The method name.
    * @param resourceUri The resource URI.
    * @param hasInput Indicates if the call will have an input to send to the server.
    * @throws IOException
    */
   public HttpClientCallImpl(Client client, String method, String resourceUri, boolean hasInput) throws IOException
   {
      this.requestMethod = method;
      
      if(resourceUri.startsWith("http"))
      {
         URL url = new URL(resourceUri);
         this.connection = (HttpURLConnection)url.openConnection();
         
         if(client.getTimeout() != -1)
         {
         	this.connection.setConnectTimeout(client.getTimeout());
         	this.connection.setReadTimeout(client.getTimeout());
         }
         
         this.connection.setAllowUserInteraction(false);
         this.connection.setDoOutput(hasInput);
         this.connection.setInstanceFollowRedirects(false);
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
   public void sendRequestHeaders() throws IOException
   {
      // Set the request method
      getConnection().setRequestMethod(getRequestMethod());

      // Set the request headers
      for(Parameter header : getRequestHeaders())
      {
         getConnection().addRequestProperty(header.getName(), header.getValue());
      }

      // Ensure that the connections is active
      getConnection().connect();
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
   public WritableByteChannel getRequestChannel() throws IOException
   {
      return null;
   }
   
   /**
    * Returns the request entity stream if it exists.
    * @return The request entity stream if it exists.
    */
   public OutputStream getRequestStream() throws IOException
   {
      return getConnection().getOutputStream();
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
            this.responseHeaders.add(new Parameter(headerName, headerValue));
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
   public ReadableByteChannel getResponseChannel() throws IOException
   {
      return null;
   }
   
   /**
    * Returns the response stream if it exists.
    * @return The response stream if it exists.
    */
   public InputStream getResponseStream() throws IOException
   {
      InputStream result = null;
      
      try
      {
      	result = getConnection().getInputStream();
      }
      catch(IOException ioe)
      {
       	result = getConnection().getErrorStream();
      }
      
      if(result == null)
      {
      	// Maybe an error stream is available instead
        	result = getConnection().getErrorStream();
      }
      
      return result;
   }
}
