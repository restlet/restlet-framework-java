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
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.connector.ConnectorCall;
import org.restlet.data.DefaultEncoding;
import org.restlet.data.DefaultLanguage;
import org.restlet.data.Parameter;
import org.restlet.data.Representation;
import org.restlet.data.RepresentationMetadata;
import org.restlet.data.Tag;

import com.noelios.restlet.data.ContentType;
import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.ReadableRepresentation;

/**
 * Base HTTP client connector call.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class HttpClientCall extends ClientCall
{
	/**
	 * Constructor setting the request address to the local host.
    * @param method The method name.
    * @param requestUri The request URI.
	 */
	public HttpClientCall(String method, String requestUri)
	{
		super(method, requestUri);
      this.requestAddress = getLocalAddress();
	}
 
   /**
    * Sends the request headers.<br/>
    * Must be called before sending the request input.
    */
   public abstract void sendRequestHeaders() throws IOException;

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
      return null;
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
      return null;
   }

   /**
    * Returns the response output representation if available. Note that no metadata is associated by default, 
    * you have to manually set them from your headers.
    * @return The response output representation if available.
    */
   public Representation getResponseOutput()
   {
   	Representation result = null;
   	
      if(getResponseStream() != null)
      {
         result = new InputRepresentation(getResponseStream(), null);
      }
      else if(getResponseChannel() != null)
      {
         result = new ReadableRepresentation(getResponseChannel(), null);
      }

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
   
}
