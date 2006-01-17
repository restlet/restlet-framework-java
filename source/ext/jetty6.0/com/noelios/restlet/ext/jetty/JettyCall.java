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

package com.noelios.restlet.ext.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

import javax.servlet.http.Cookie;

import org.mortbay.jetty.HttpConnection;
import org.restlet.data.CookieSetting;

import com.noelios.restlet.connector.HttpServerCallImpl;

/**
 * Call that is used by the Jetty 6 HTTP server connector.
 */
public class JettyCall extends HttpServerCallImpl
{
   /** The wrapped Jetty HTTP connection. */
   protected HttpConnection connection;

   /**
    * Constructor.
    * @param connection The wrapped Jetty HTTP connection.
    */
   public JettyCall(HttpConnection connection)
   {
      this.connection = connection;
   }

   /**
    * Returns the wrapped Jetty HTTP connection.
    * @return The wrapped Jetty HTTP connection.
    */
   public HttpConnection getConnection()
   {
      return this.connection;
   }

   
   // ----------------------
   // ---  Request part  ---
   // ----------------------

   /**
    * Returns the request address.<br/>
    * Corresponds to the IP address of the requesting client.
    * @return The request address.
    */
   public String getRequestAddress()
   {
      return getConnection().getRequest().getRemoteAddr();
   }

   /**
    * Indicates if the request was made using a confidential mean.<br/>
    * @return True if the request was made using a confidential mean.<br/>
    */
   public boolean isRequestConfidential()
   {
      return getConnection().getRequest().isSecure();
   }

   /**
    * Returns the request method. 
    * @return The request method.
    */
   public String getRequestMethod()
   {
      return getConnection().getRequest().getMethod();
   }

   /**
    * Returns the full request URI. 
    * @return The full request URI.
    */
   public String getRequestUri()
   {
      String queryString = getConnection().getRequest().getQueryString();

      if(queryString == null)
      {
         return getConnection().getRequest().getRequestURL().toString();
      }
      else
      {
         return getConnection().getRequest().getRequestURL().append('?').append(queryString).toString();
      }
   }
   
   /**
    * Returns a request header value.
    * @param name The name of the header.
    * @return A header value.
    */
   public String getRequestHeader(String name)
   {
      return getConnection().getRequest().getHeader(name);
   }
   
   /**
    * Returns a request date header value.
    * @param name The name of the header.
    * @return A header date.
    */
   public Date getRequestDateHeader(String name)
   {
      return new Date(getConnection().getRequest().getDateHeader(name));
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

   // -----------------------
   // ---  Response part  ---
   // -----------------------
   
   /**
    * Returns the response status code.
    * @return The response status code.
    */
   public int getResponseStatusCode()
   {
      return getConnection().getResponse().getStatus();
   }

   /**
    * Sets the response status code.
    * @param code The response status code.
    */
   public void setResponseStatus(int code)
   {
      getConnection().getResponse().setStatus(code);
   }

   /**
    * Returns the response reason phrase.
    * @return The response reason phrase.
    */
   public String getResponseReasonPhrase()
   {
      return getConnection().getResponse().getReason();
   }

   /**
    * Sets the response reason phrase.
    * @param reason The response reason phrase.
    */
   public void setResponseReasonPhrase(String reason)
   {
      getConnection().getResponse().setStatus(getConnection().getResponse().getStatus(), reason);
   }
   
   /**
    * Returns a response header value.
    * @param name The name of the header.
    * @return A header value.
    */
   public String getResponseHeader(String name)
   {
      return getConnection().getResponseFields().getStringField(name);
   }
   
   /**
    * Returns a response date header value.
    * @param name The name of the header.
    * @return A header date.
    */
   public Date getResponseDateHeader(String name)
   {
      return new Date(getConnection().getResponseFields().getDateField(name));
   }
   
   /**
    * Sets a response header value.
    * @param name The name of the header.
    * @param value The value of the header.
    */
   public void setResponseHeader(String name, String value)
   {
      getConnection().getResponse().setHeader(name, value);
   }
   
   /**
    * Sets a response date header value.
    * @param name The name of the header.
    * @param date The value of the header.
    */
   public void setResponseDateHeader(String name, long date)
   {
      getConnection().getResponse().setDateHeader(name, date);
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
      getConnection().getResponse().addCookie(servletCookie);
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
