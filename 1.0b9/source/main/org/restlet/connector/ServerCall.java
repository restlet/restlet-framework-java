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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

import org.restlet.RestletCall;
import org.restlet.data.Representation;

/**
 * Server connector call.
 */
public interface ServerCall extends ConnectorCall
{
   /**
    * Converts to an uniform call.
    * @return An equivalent uniform call.
    */
   public RestletCall toUniform();
   
   /**
    * Sets the response from an uniform call.<br>
    * Sets the response headers and the response status. 
    * @param call The call to update from.
    */
   public void setResponse(RestletCall call);
   
   /**
    * Sends the response headers.<br/>
    * Must be called before sending the response output.
    */
   public void sendResponseHeaders();

   /**
    * Sends the response output.
    * @param output The response output;
    */
   public void sendResponseOutput(Representation output) throws IOException;

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
