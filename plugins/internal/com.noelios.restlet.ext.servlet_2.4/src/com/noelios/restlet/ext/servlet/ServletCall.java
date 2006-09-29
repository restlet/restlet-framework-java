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

package com.noelios.restlet.ext.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;
import org.restlet.data.Representation;
import org.restlet.data.Status;

import com.noelios.restlet.impl.connector.HttpServerCall;

/**
 * Call that is used by the Servlet HTTP server connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ServletCall extends HttpServerCall
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(ServletCall.class.getCanonicalName());

   /** The HTTP Servlet request to wrap. */
   private HttpServletRequest request;
   
   /** The HTTP Servlet response to wrap. */
   private HttpServletResponse response;
      
   /** The request headers. */
   private ParameterList requestHeaders;

   /**
    * Constructor.
    * @param request The HTTP Servlet request to wrap.
    * @param response The HTTP Servlet response to wrap.
    */
   public ServletCall(HttpServletRequest request, HttpServletResponse response)
   {
      this.request = request;
      this.response = response;
   }

   /**
    * Returns the HTTP Servlet request.
    * @return The HTTP Servlet request.
    */
   public HttpServletRequest getRequest()
   {
      return this.request;
   }

   /**
    * Returns the HTTP Servlet response.
    * @return The HTTP Servlet response.
    */
   public HttpServletResponse getResponse()
   {
      return this.response;
   }

   /**
    * Indicates if the request was made using a confidential mean.<br/>
    * @return True if the request was made using a confidential mean.<br/>
    */
   public boolean isConfidential()
   {
      return getRequest().isSecure();
   }

	/**
	 * Returns the request address.<br/>
	 * Corresponds to the IP address of the requesting client.
	 * @return The request address.
	 */
   public String getClientAddress()
   {
      return getRequest().getRemoteAddr();
   }

   /**
    * Returns the request method. 
    * @return The request method.
    */
   public String getMethod()
   {
      return getRequest().getMethod();
   }

   /**
    * Returns the full request URI. 
    * @return The full request URI.
    */
   public String getRequestUri()
   {
      String queryString = getRequest().getQueryString();

      if((queryString == null) || (queryString.equals("")))
      {
         return getRequest().getRequestURI();
      }
      else
      {
         return getRequest().getRequestURI() + '?' + queryString;
      }
   }
   
   /**
    * Returns the list of request headers.
    * @return The list of request headers.
    */
   public ParameterList getRequestHeaders()
   {
      if(this.requestHeaders == null)
      {
         this.requestHeaders = new ParameterList();

         // Copy the headers from the request object
         String headerName;
         String headerValue;
         for(Enumeration names = getRequest().getHeaderNames(); names.hasMoreElements(); )
         {
            headerName = (String)names.nextElement();
            for(Enumeration values = getRequest().getHeaders(headerName); values.hasMoreElements(); )
            {
               headerValue = (String)values.nextElement();
               this.requestHeaders.add(new Parameter(headerName, headerValue));
            }
         }
      }

      return this.requestHeaders;
   }

   /**
    * Returns the request entity channel if it exists.
    * @return The request entity channel if it exists.
    */
   public ReadableByteChannel getRequestChannel()
   {
      // Can't do anything
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
         return getRequest().getInputStream();
      }
      catch(IOException e)
      {
         return null;
      }
   }

   /**
    * Returns the response address.<br/>
    * Corresponds to the IP address of the responding server.
    * @return The response address.
    */
   public String getServerAddress()
   {
      return getRequest().getLocalAddr();
   }
   
   /**
    * Returns the response status code.
    * @return The response status code.
    */
   public int getStatusCode()
   {
      // Can't do anything
      return 0;
   }

   /**
    * Returns the response reason phrase.
    * @return The response reason phrase.
    */
   public String getReasonPhrase()
   {
      // Can't do anything
      return null;
   }

   /**
    * Sends the response back to the client. Commits the status, headers and optional output and 
    * send them on the network. 
    * @param output The optional output representation to send.
    */
   public void sendResponse(Representation output) throws IOException
   {
   	// Add the response headers
      Parameter header;
      for(Iterator<Parameter> iter = getResponseHeaders().iterator(); iter.hasNext();)
      {
         header = iter.next();
         getResponse().addHeader(header.getName(), header.getValue());
      }

      // Set the status code in the response. We do this after adding the headers because 
      // when we have to rely on the 'sendError' method, the Servlet containers are expected
      // to commit their response.
   	if(Status.isError(getStatusCode()) && (output == null))
   	{
   		try
			{
				getResponse().sendError(getStatusCode(), getReasonPhrase());
			}
			catch (IOException ioe)
			{
				logger.log(Level.WARNING, "Unable to set the response error status", ioe);
			}
   	}
   	else
   	{
     		// Send the response output
  		 	getResponse().setStatus(getStatusCode());
      	super.sendResponse(output);
   	}
   }

   /**
    * Returns the response channel if it exists.
    * @return The response channel if it exists.
    */
   public WritableByteChannel getResponseChannel()
   {
      // Can't do anything
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
         return getResponse().getOutputStream();
      }
      catch(IOException e)
      {
         return null;
      }
   }
      
}
