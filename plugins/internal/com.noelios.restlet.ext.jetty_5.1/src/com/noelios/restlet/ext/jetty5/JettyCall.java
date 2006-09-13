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

package com.noelios.restlet.ext.jetty5;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Enumeration;
import java.util.Iterator;

import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.restlet.data.Parameter;
import org.restlet.data.ParameterList;
import org.restlet.data.Representation;

import com.noelios.restlet.connector.HttpServerCall;

/**
 * Call that is used by the Jetty HTTP server connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class JettyCall extends HttpServerCall
{
   /** The wrapped Jetty HTTP request. */
	private HttpRequest request;

   /** The wrapped Jetty HTTP response. */
	private HttpResponse response;

	/** Indicates if the request headers were parsed and added. */
	private boolean requestHeadersAdded;

   /**
    * Constructor.
    * @param request The Jetty HTTP request.
    * @param response The Jetty HTTP response.
    */
   public JettyCall(HttpRequest request, HttpResponse response)
   {
      this.request = request;
      this.response = response;
      this.requestHeadersAdded = false;
   }

   /**
    * Returns the HTTP Jetty request.
    * @return The HTTP Jetty request.
    */
   public HttpRequest getRequest()
   {
      return this.request;
   }

   /**
    * Returns the HTTP Jetty response.
    * @return The HTTP Jetty response.
    */
   public HttpResponse getResponse()
   {
      return this.response;
   }

   /**
    * Indicates if the request was made using a confidential mean.<br/>
    * @return True if the request was made using a confidential mean.<br/>
    */
   public boolean isConfidential()
   {
      return getRequest().isConfidential();
   }

   /**
    * Returns the request address.<br/>
    * Corresponds to the IP address of the requesting client.
    * @return The request address.
    */
   public String getRequestAddress()
   {
      return getRequest().getRemoteAddr();
   }

   /**
    * Returns the request method.
    * @return The request method.
    */
   public String getRequestMethod()
   {
      return getRequest().getMethod();
   }

   /**
    * Returns the full request URI.
    * @return The full request URI.
    */
   public String getRequestUri()
   {
   	return getRequest().getRootURL().append(getRequest().getURI().toString()).toString();
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
         for(Enumeration names = getRequest().getFieldNames(); names.hasMoreElements(); )
         {
            headerName = (String)names.nextElement();
            for(Enumeration values = getRequest().getFieldValues(headerName); values.hasMoreElements(); )
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
   public String getResponseAddress()
   {
      return getRequest().getHttpConnection().getServerAddr();
   }

   /**
    * Returns the request entity channel if it exists.
    * @return The request entity channel if it exists.
    */
   public ReadableByteChannel getRequestChannel()
   {
		// Unsupported.
      return null;
   }

   /**
    * Returns the request entity stream if it exists.
    * @return The request entity stream if it exists.
    */
   public InputStream getRequestStream()
   {
      return getRequest().getInputStream();
   }

   /**
    * Sends the response back to the client. Commits the status, headers and optional output and 
    * send them on the network. 
    * @param output The optional output representation to send.
    */
   public void sendResponse(Representation output) throws IOException
   {
   	// Set the response status
      getResponse().setStatus(getResponseStatusCode(), getResponseReasonPhrase());

      // Remove existings headers if any
      for(Enumeration fields = getResponse().getFieldNames(); fields.hasMoreElements(); )
      {
         getResponse().removeField((String)fields.nextElement());
      }
      
      // Add response headers
      Parameter header;
      for(Iterator<Parameter> iter = getResponseHeaders().iterator(); iter.hasNext();)
      {
         header = iter.next();
         getResponse().addField(header.getName(), header.getValue());
      }
      
      // Send the response output
      super.sendResponse(output);
   }

   /**
    * Returns the response channel if it exists.
    * @return The response channel if it exists.
    */
   public WritableByteChannel getResponseChannel()
   {
		// Unsupported.
      return null;
   }

   /**
    * Returns the response stream if it exists.
    * @return The response stream if it exists.
    */
   public OutputStream getResponseStream()
   {
      return getResponse().getOutputStream();
   }

}
