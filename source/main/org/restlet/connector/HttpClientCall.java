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

/**
 * HTTP client call.
 */
public interface HttpClientCall extends HttpCall
{
   /**
    * Returns the request method. 
    * @return The request method.
    */
   public String getRequestMethod();

   /**
    * Sets the request method. 
    * @param method The request method.
    */
   public void setRequestMethod(String method);

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
    * Returns a request header value.
    * @param name The header name.
    * @param value The header value.
    */
   public void setRequestHeader(String name, String value);

   /**
    * Returns the request entity channel if it exists.
    * @return The request entity channel if it exists.
    */
   public WritableByteChannel getRequestChannel();
   
   /**
    * Returns the request entity stream if it exists.
    * @return The request entity stream if it exists.
    */
   public OutputStream getRequestStream();

   
   // -----------------------
   // ---  Response part  ---
   // -----------------------
   
   /**
    * Returns the response status code.
    * @return The response status code.
    */
   public int getResponseStatusCode();

   /**
    * Returns the response reason phrase.
    * @return The response reason phrase.
    */
   public String getResponseReasonPhrase();

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
    * Returns the response channel if it exists.
    * @return The response channel if it exists.
    */
   public ReadableByteChannel getResponseChannel();
   
   /**
    * Returns the response stream if it exists.
    * @return The response stream if it exists.
    */
   public InputStream getResponseStream();
   
}
