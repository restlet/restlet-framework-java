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

package com.noelios.restlet.ext.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mortbay.jetty.HttpConnection;
import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;
import org.restlet.data.Representation;
import org.restlet.data.Status;

import com.noelios.restlet.impl.connector.HttpServerCall;

/**
 * Call that is used by the Jetty 6 HTTP server connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class JettyCall extends HttpServerCall
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(JettyCall.class.getCanonicalName());

   /** The wrapped Jetty HTTP connection. */
   private HttpConnection connection;

	/** Indicates if the request headers were parsed and added. */
	private boolean requestHeadersAdded;

   /**
    * Constructor.
    * @param connection The wrapped Jetty HTTP connection.
    */
   public JettyCall(HttpConnection connection)
   {
      this.connection = connection;
      this.requestHeadersAdded = false;
   }

   /**
    * Returns the wrapped Jetty HTTP connection.
    * @return The wrapped Jetty HTTP connection.
    */
   public HttpConnection getConnection()
   {
      return this.connection;
   }

   /**
    * Indicates if the request was made using a confidential mean.<br/>
    * @return True if the request was made using a confidential mean.<br/>
    */
   public boolean isConfidential()
   {
      return getConnection().getRequest().isSecure();
   }

	/**
	 * Returns the request address.<br/>
	 * Corresponds to the IP address of the requesting client.
	 * @return The request address.
	 */
   public String getClientAddress()
   {
      return getConnection().getRequest().getRemoteAddr();
   }

   /**
    * Returns the request method.
    * @return The request method.
    */
   public String getMethod()
   {
      return getConnection().getRequest().getMethod();
   }

	/**
	 * Returns the URI on the request line (most like a relative reference, but not necessarily). 
	 * @return The URI on the request line.
	 */
   public String getRequestUri()
   {
   	return getConnection().getRequest().getUri().toString();
   }

   /**
    * Returns the list of request headers.
    * @return The list of request headers.
    */
   public ParameterList getRequestHeaders()
   {
   	ParameterList result = super.getRequestHeaders();
   	
      if(!requestHeadersAdded)
      {
         // Copy the headers from the request object
         String headerName;
         String headerValue;
         for(Enumeration names = getConnection().getRequest().getHeaderNames(); names.hasMoreElements(); )
         {
            headerName = (String)names.nextElement();
            for(Enumeration values = getConnection().getRequest().getHeaders(headerName); values.hasMoreElements(); )
            {
               headerValue = (String)values.nextElement();
               result.add(new Parameter(headerName, headerValue));
            }
         }
         
         requestHeadersAdded = true;
      }

      return result;
   }

	/**
	 * Returns the response address.<br/>
	 * Corresponds to the IP address of the responding server.
	 * @return The response address.
	 */
   public String getServerAddress()
   {
      return getConnection().getRequest().getLocalAddr();
   }

   /**
    * Returns the request entity channel if it exists.
    * @return The request entity channel if it exists.
    */
   public ReadableByteChannel getRequestChannel()
   {
      return null;
   }

   /**
    * Returns the request entity stream if it exists.
    * @return The request entity stream if it exists.
    */
   public InputStream getRequestStream()
   {
      try
      {
         return getConnection().getRequest().getInputStream();
      }
      catch(IOException e)
      {
         return null;
      }
   }

   /**
    * Sends the response back to the client. Commits the status, headers and optional output and 
    * send them on the network. 
    * @param output The optional output representation to send.
    */
   public void sendResponse(Representation output) throws IOException
   {
      // Add call headers
      Parameter header;
      for(Iterator<Parameter> iter = getResponseHeaders().iterator(); iter.hasNext();)
      {
         header = iter.next();
         getConnection().getResponse().addHeader(header.getName(), header.getValue());
      }

      // Set the status code in the response. We do this after adding the headers because 
      // when we have to rely on the 'sendError' method, the Servlet containers are expected
      // to commit their response.
   	if(Status.isError(getStatusCode()))
   	{
   		try
			{
   			getConnection().getResponse().sendError(getStatusCode(), getReasonPhrase());
			}
			catch (IOException ioe)
			{
				logger.log(Level.WARNING, "Unable to set the response error status", ioe);
			}
   	}
   	else
   	{
   		getConnection().getResponse().setStatus(getStatusCode());
   	}
      
   	// Send the response output
      super.sendResponse(output);

      // Fully complete and commit the response 
   	this.connection.completeResponse();
   	this.connection.commitResponse(true);
   }

   /**
    * Returns the response channel if it exists.
    * @return The response channel if it exists.
    */
   public WritableByteChannel getResponseChannel()
   {
      return null;
   }

   /**
    * Returns the response stream if it exists.
    * @return The response stream if it exists.
    */
   public OutputStream getResponseStream()
   {
      try
      {
         return getConnection().getResponse().getOutputStream();
      }
      catch(IOException e)
      {
         return null;
      }
   }
   
}
