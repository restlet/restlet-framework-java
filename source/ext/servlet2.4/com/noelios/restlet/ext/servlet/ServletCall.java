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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.data.CookieSetting;

import com.noelios.restlet.connector.HttpCall;

/**
 * Call that is used by the Servlet HTTP server connector.
 */
public class ServletCall extends HttpCall
{
   /** The HTTP servlet request to wrap. */
   protected HttpServletRequest request;
   
   /** The HTTP servlet response to wrap. */
   protected HttpServletResponse response;

   /**
    * Constructor.
    * @param request The HTTP servlet request to wrap.
    * @param response The HTTP servlet response to wrap.
    */
   public ServletCall(HttpServletRequest request, HttpServletResponse response)
   {
      this.request = request;
      this.response = response;
      init();
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
    * Returns a header value.
    * @param name The name of the header.
    * @return A header value.
    */
   public String getRequestHeader(String name)
   {
      return getRequest().getHeader(name);
   }
   
   /**
    * Returns the request stream.
    * @return The request stream.
    */
   protected InputStream getRequestStream()
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
    * Returns the response stream.
    * @return The response stream.
    * @throws IOException 
    */
   protected OutputStream getResponseStream() throws IOException
   {
      return getResponse().getOutputStream();
   }
   
   /**
    * Sets a response cookie.
    * @param cookie The cookie setting.
    */
   public void setResponseCookie(CookieSetting cookie)
   {
      // Convert the cookie setting into a Servlet cookie
      Cookie servletCookie = new Cookie(cookie.getName(), cookie.getValue());
      if(cookie.getComment() != null) servletCookie.setComment(cookie.getComment());
      if(cookie.getDomain() != null) servletCookie.setDomain(cookie.getDomain());
      servletCookie.setMaxAge(cookie.getMaxAge());
      if(cookie.getPath() != null) servletCookie.setPath(cookie.getPath());
      servletCookie.setSecure(cookie.isSecure());
      servletCookie.setVersion(cookie.getVersion());
            
      // Set the cookie in the response
      getResponse().addCookie(servletCookie);
   }
      
   /**
    * Sets a response header value.
    * @param name The name of the header.
    * @param value The value of the header.
    */
   public void setResponseHeader(String name, String value)
   {
      getResponse().setHeader(name, value);
   }
   
   /**
    * Sets a response header value.
    * @param name The name of the header.
    * @param date The value of the header.
    */
   public void setResponseHeader(String name, long date)
   {
      getResponse().setDateHeader(name, date);
   }

   /**
    * Sets the response's status code.
    * @param code The response's status code.
    * @param description The status code description.
    */
   public void setResponseStatus(int code, String description)
   {
      // Ignore the description parameter
      // deprecated in the Servlet API
      getResponse().setStatus(code);
   }

   /**
    * Extracts the resource URI.
    * @return The resource URI.
    */
   protected String extractResourceURI()
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
    * Extracts the call confidentiality.
    * @return True if the call is confidential. 
    */
   protected boolean extractConfidentiality()
   {
      return getRequest().isSecure();
   }
   
   /**
    * Extracts the HTTP method name.
    * @return The HTTP method name.
    */
   protected String extractMethodName()
   {
      return getRequest().getMethod();
   }
   
   /**
    * Extracts the client IP address.
    * @return The client IP address.
    */
   protected String extractClientAddress()
   {
      return getRequest().getRemoteAddr();
   }

}
