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
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.restlet.connector.ClientCall;
import org.restlet.data.Representation;

import com.noelios.restlet.data.InputRepresentation;
import com.noelios.restlet.data.ReadableRepresentation;

/**
 * Client connector call implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class ClientCallImpl extends ConnectorCallImpl implements ClientCall
{
	/**
	 * Constructor setting the request address to the local host.
    * @param method The method name.
    * @param requestUri The request URI.
	 */
	public ClientCallImpl(String method, String requestUri)
	{
		this.requestMethod = method;
		this.requestUri = requestUri;
      this.requestAddress = getLocalAddress();
	}

	/**
	 * Returns the local IP address or 127.0.0.1 if the resolution fails.
	 * @return The local IP address or 127.0.0.1 if the resolution fails.
	 */
	public static String getLocalAddress()
	{
      try
      {
         return InetAddress.getLocalHost().getHostAddress();
      }
      catch(UnknownHostException e)
      {
         return "127.0.0.1";
      }
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
      
      return result;
   }
}
