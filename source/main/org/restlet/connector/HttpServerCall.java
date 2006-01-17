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

package org.restlet.connector;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Date;

import org.restlet.UniformCall;
import org.restlet.data.CookieSetting;

/**
 * HTTP server call.
 */
public interface HttpServerCall extends HttpCall
{
   /**
    * Converts to an uniform call.
    * @return An equivalent uniform call.
    */
   public UniformCall toUniform();
   
   /**
    * Synchronizes from an uniform call.
    * @param call The call to synchronize from.
    */
   public void fromUniform(UniformCall call);

   
   // ----------------------
   // ---  Request part  ---
   // ----------------------

   /**
    * Returns the request address.<br/>
    * Corresponds to the IP address of the requesting client.
    * @return The request address.
    */
   public String getRequestAddress();

   /**
    * Indicates if the request was made using a confidential mean.<br/>
    * @return True if the request was made using a confidential mean.<br/>
    */
   public boolean isRequestConfidential();

   /**
    * Returns the request method. 
    * @return The request method.
    */
   public String getRequestMethod();

   /**
    * Returns the full request URI. 
    * @return The full request URI.
    */
   public String getRequestUri();
   
   /**
    * Returns a request header value.
    * @param name The name of the header.
    * @return A header value.
    */
   public String getRequestHeader(String name);
   
   /**
    * Returns a request date header value.
    * @param name The name of the header.
    * @return A header date.
    */
   public Date getRequestDateHeader(String name);

   /**
    * Returns the request entity channel if it exists.
    * @return The request entity channel if it exists.
    */
   public ReadableByteChannel getRequestChannel();
   
   /**
    * Returns the request entity stream if it exists.
    * @return The request entity stream if it exists.
    */
   public InputStream getRequestStream();

   // -----------------------
   // ---  Response part  ---
   // -----------------------
   
   /**
    * Returns the response status code.
    * @return The response status code.
    */
   public int getResponseStatusCode();

   /**
    * Sets the response status code.
    * @param code The response status code.
    */
   public void setResponseStatus(int code);

   /**
    * Returns the response reason phrase.
    * @return The response reason phrase.
    */
   public String getResponseReasonPhrase();

   /**
    * Sets the response reason phrase.
    * @param reason The response reason phrase.
    */
   public void setResponseReasonPhrase(String reason);
   
   /**
    * Returns a response header value.
    * @param name The name of the header.
    * @return A header value.
    */
   public String getResponseHeader(String name);
   
   /**
    * Returns a response date header value.
    * @param name The name of the header.
    * @return A header date.
    */
   public Date getResponseDateHeader(String name);
   
   /**
    * Sets a response header value.
    * @param name The name of the header.
    * @param value The value of the header.
    */
   public void setResponseHeader(String name, String value);
   
   /**
    * Sets a response date header value.
    * @param name The name of the header.
    * @param date The value of the header.
    */
   public void setResponseDateHeader(String name, long date);
   
   /**
    * Sets a response cookie. 
    * TO BE REMOVED WHEN COOKIE_WRITER IS COMPLETE
    * @param cookie The cookie setting.
    */
   public void setResponseCookie(CookieSetting cookie);

   /**
    * Returns the response channel if it exists.
    * @return The response channel if it exists.
    */
   public WritableByteChannel getResponseChannel();
   
   /**
    * Returns the response stream if it exists.
    * @return The response stream if it exists.
    */
   public OutputStream getResponseStream();
   
}
