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

import com.noelios.restlet.impl.HttpServerCallImpl;

/**
 * Call that is used by the Jetty HTTP server connector.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class JettyCall extends HttpServerCallImpl
{
   /** The wrapped Jetty HTTP request. */
   protected HttpRequest request;

   /** The wrapped Jetty HTTP response. */
   protected HttpResponse response;
   
   /** The request headers. */
   protected ParameterList requestHeaders;
   
   /**
    * Constructor.
    * @param request The Jetty HTTP request.
    * @param response The Jetty HTTP response.
    */
   public JettyCall(HttpRequest request, HttpResponse response)
   {
      this.request = request;
      this.response = response;
      this.requestHeaders = null;
      this.responseHeaders = null;
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
      if(this.requestHeaders == null)
      {
         this.requestHeaders = new ParameterList();

         // Copy the headers from the request object
         String headerName;
         String headerValue;
         for(Enumeration names = getRequest().getFieldNames(); names.hasMoreElements(); )
         {
            headerName = (String)names.nextElement();
            for(Enumeration values = getRequest().getFieldValues(headerName); values.hasMoreElements(); )
            {
               headerValue = (String)values.nextElement();
               this.requestHeaders.add(new Parameter(headerName, headerValue));
            }
         }
      }

      return this.requestHeaders;
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
    * Returns the response status code.
    * @return The response status code.
    */
   public int getResponseStatusCode()
   {
      return getResponse().getStatus();
   }

   /**
    * Returns the response reason phrase.
    * @return The response reason phrase.
    */
   public String getResponseReasonPhrase()
   {
      return getResponse().getReason();
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
      return getRequest().getInputStream();
   }

   /**
    * Sets the response status code.
    * @param code The response status code.
    * @param reason The response reason phrase.
    */
   public void setResponseStatus(int code, String reason)
   {
      getResponse().setStatus(code, reason);
   }

   /**
    * Sends the response headers.<br/>
    * Must be called before sending the response output.
    */
   public void sendResponseHeaders()
   {
      // Remove existings headers
      for(Enumeration fields = getResponse().getFieldNames(); fields.hasMoreElements(); )
      {
         getResponse().removeField((String)fields.nextElement());
      }
      
      // Add call headers
      Parameter header;
      for(Iterator<Parameter> iter = getResponseHeaders().iterator(); iter.hasNext();)
      {
         header = iter.next();
         getResponse().addField(header.getName(), header.getValue());
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
      return getResponse().getOutputStream();
   }

}
