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

import org.restlet.UniformCall;

/**
 * Server call for the HTTP protocol.
 */
public interface HttpServerCall extends HttpCall
{
   /** 
    * Name of the system property to use in order to indicates whether the IP address contained in 
    * the "X-Forwarded-For" header should be used instead of the original client IP address.
    * This is particularly useful when the client calls are intercepted by one or more proxies
    * (such as Squid HTTP cache). Those proxies can remember the original client IP address and 
    * forward it using the special HTTP header. If multiple IP addresses are forwarded, the last 
    * one is extracted. If an "unknown" address is found, then the proxy IP address is used.
    */ 
   public static final String PROPERTY_USE_FORWARDED_FOR = "org.restlet.useForwardedFor";
   
   /**
    * Converts to an uniform call.
    * @return An equivalent uniform call.
    */
   public UniformCall toUniform();
   
   /**
    * Commits after synchronization with an uniform call.
    * @param call The call to synchronize with.
    */
   public void commitFrom(UniformCall call);

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

   /**
    * Sets the response status code.
    * @param code The response status code.
    * @param reason The response reason phrase.
    */
   public void setResponseStatus(int code, String reason);
   
   /**
    * Adds a response header.
    * @param name The header's name.
    * @param value The header's value.
    */
   public void addResponseHeader(String name, String value);

   /**
    * Commits the response headers.<br/>
    * Must be called before writing the response entity.
    */
   public void commitResponseHeaders();

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
