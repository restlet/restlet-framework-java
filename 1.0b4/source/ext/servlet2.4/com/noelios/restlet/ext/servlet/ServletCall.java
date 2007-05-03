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

package com.noelios.restlet.ext.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Manager;
import org.restlet.data.Parameter;

import com.noelios.restlet.connector.HttpServerCallImpl;

/**
 * Call that is used by the Servlet HTTP server connector.
 */
public class ServletCall extends HttpServerCallImpl
{
   /** The HTTP servlet request to wrap. */
   protected HttpServletRequest request;
   
   /** The HTTP servlet response to wrap. */
   protected HttpServletResponse response;
   
   /** The request headers. */
   protected List<Parameter> requestHeaders;

   /**
    * Constructor.
    * @param request The HTTP servlet request to wrap.
    * @param response The HTTP servlet response to wrap.
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
      String queryString = getRequest().getQueryString();

      if(queryString == null)
      {
         return getRequest().getRequestURL().toString();
      }
      else
      {
         return getRequest().getRequestURL().append('?').append(queryString).toString();
      }
   }
   
   /**
    * Returns the list of request headers.
    * @return The list of request headers.
    */
   public List<Parameter> getRequestHeaders()
   {
      if(this.requestHeaders == null)
      {
         this.requestHeaders = new ArrayList<Parameter>();

         // Copy the headers from the request object
         String headerName;
         String headerValue;
         for(Enumeration names = getRequest().getHeaderNames(); names.hasMoreElements(); )
         {
            headerName = (String)names.nextElement();
            for(Enumeration values = getRequest().getHeaders(headerName); values.hasMoreElements(); )
            {
               headerValue = (String)values.nextElement();
               this.requestHeaders.add(Manager.createParameter(headerName, headerValue));
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
    * Returns the reponse address.<br/>
    * Corresponds to the IP address of the responding server.
    * @return The reponse address.
    */
   public String getResponseAddress()
   {
      return getRequest().getLocalAddr();
   }
   
   /**
    * Returns the response status code.
    * @return The response status code.
    */
   public int getResponseStatusCode()
   {
      // Can't do anything
      return 0;
   }

   /**
    * Returns the response reason phrase.
    * @return The response reason phrase.
    */
   public String getResponseReasonPhrase()
   {
      // Can't do anything
      return null;
   }

   /**
    * Sets the response status code.
    * @param code The response status code.
    * @param reason The response reason phrase.
    */
   public void setResponseStatus(int code, String reason)
   {
      getResponse().setStatus(code);
   }

   /**
    * Sends the response headers.<br/>
    * Must be called before sending the response output.
    */
   public void sendResponseHeaders()
   {
      Parameter header;
      for(Iterator<Parameter> iter = getResponseHeaders().iterator(); iter.hasNext();)
      {
         header = iter.next();
         getResponse().addHeader(header.getName(), header.getValue());
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
