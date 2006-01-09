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

import javax.servlet.http.Cookie;

import org.mortbay.jetty.HttpConnection;
import org.restlet.data.CookieSetting;

import com.noelios.restlet.connector.HttpCall;

/**
 * Call that is used by the Jetty 6 HTTP server connector.
 */
public class JettyCall extends HttpCall
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
      init();
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
    * Returns a header value.
    * @param name The name of the header.
    * @return A header value.
    */
   public String getRequestHeader(String name)
   {
      return getConnection().getRequest().getHeader(name);
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
    * Sets a response header value.
    * @param name The name of the header.
    * @param value The value of the header.
    */
   public void setResponseHeader(String name, String value)
   {
      getConnection().getResponse().setHeader(name, value);
   }

   /**
    * Sets a response header value.
    * @param name The name of the header.
    * @param date The value of the header.
    */
   public void setResponseHeader(String name, long date)
   {
      getConnection().getResponse().setDateHeader(name, date);
   }

   /**
    * Sets the response's status code.
    * @param code The response's status code.
    * @param description The status code description.
    */
   public void setResponseStatus(int code, String description)
   {
      getConnection().getResponse().setStatus(code, description);
   }

   /**
    * Gets the response stream.
    * @return The response stream.
    * @throws IOException
    */
   protected OutputStream getResponseStream() throws IOException
   {
      return getConnection().getResponse().getOutputStream();
   }

   /**
    * Extracts the resource URI.
    * @return The resource URI.
    */
   protected String extractResourceURI()
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
    * Returns the request stream.
    * @return The request stream.
    */
   protected InputStream getRequestStream()
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
    * Extracts the call confidentiality.
    * @return True if the call is confidential.
    */
   protected boolean extractConfidentiality()
   {
      return getConnection().getRequest().isSecure();
   }

   /**
    * Extracts the HTTP method name.
    * @return The HTTP method name.
    */
   protected String extractMethodName()
   {
      return getConnection().getRequest().getMethod();
   }

   /**
    * Extracts the client IP address.
    * @return The client IP address.
    */
   protected String extractClientAddress()
   {
      return getConnection().getRequest().getRemoteAddr();
   }

}
