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
import java.net.URL;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import javax.net.ssl.HttpsURLConnection;

import org.restlet.connector.Client;
import org.restlet.connector.ConnectorCall;
import org.restlet.data.DefaultEncoding;
import org.restlet.data.DefaultLanguage;
import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;
import org.restlet.data.Representation;
import org.restlet.data.RepresentationMetadata;
import org.restlet.data.Tag;

import com.noelios.restlet.data.ContentType;

/**
 * HTTP client connector call.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpClientCall extends ClientCallImpl 
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
   public HttpClientCall(Client client, String method, String resourceUri, boolean hasInput) throws IOException
   {
      super(method);
      
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
   	catch(IOException ioe)
   	{
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
   public ParameterList getResponseHeaders()
   {
      if(this.responseHeaders == null)
      {
         this.responseHeaders = new ParameterList();
         
         // Read the response headers
         int i = 1;
         String headerName = getConnection().getHeaderFieldKey(i);
         String headerValue = getConnection().getHeaderField(i);
         while(headerName != null)
         {
            this.responseHeaders.add(headerName, headerValue);
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
    * Returns the response output representation if available. Note that no metadata is associated by default, 
    * you have to manually set them from your headers.
    * @return The response output representation if available.
    */
   public Representation getResponseOutput()
   {
   	Representation result = super.getResponseOutput();

      if(result != null)
      {
      	RepresentationMetadata metadata = result.getMetadata();

      	for(Parameter header : getResponseHeaders())
         {
            if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_CONTENT_TYPE))
            {
               ContentType contentType = new ContentType(header.getValue());
               if(contentType != null) 
               {
               	metadata.setMediaType(contentType.getMediaType());
               	metadata.setCharacterSet(contentType.getCharacterSet());
               }
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_CONTENT_LENGTH))
            {
               result.setSize(Long.parseLong(header.getValue()));
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_EXPIRES))
            {
            	metadata.setExpirationDate(parseDate(header.getValue(), false));
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_CONTENT_ENCODING))
            {
            	metadata.setEncoding(new DefaultEncoding(header.getValue()));
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_CONTENT_LANGUAGE))
            {
            	metadata.setLanguage(new DefaultLanguage(header.getValue()));
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_LAST_MODIFIED))
            {
            	metadata.setModificationDate(parseDate(header.getValue(), false));
            }
            else if(header.getName().equalsIgnoreCase(ConnectorCall.HEADER_ETAG))
            {
            	metadata.setTag(new Tag(header.getValue()));
            }
         }
      }
   	
   	return result;
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
