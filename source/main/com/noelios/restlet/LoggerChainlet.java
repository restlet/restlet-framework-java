/*
 * Copyright 2005 Jérôme LOUVEL
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

package com.noelios.restlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.AbstractChainlet;
import org.restlet.RestletCall;
import org.restlet.RestletException;
import org.restlet.component.RestletContainer;

/**
 * Chainlet logging all calls after handling by the attache restlet.
 * The current format is similar to IIS 6 logs.
 * The logging is based on the java.util.logging package.
 */
public class LoggerChainlet extends AbstractChainlet
{
   /** Obtain a suitable logger. */
   protected Logger logger;
   
   /**
    * Constructor.
    * @param container The parent container.
    * @param logName   The log name to used in the logging.properties file.
    */
   public LoggerChainlet(RestletContainer container, String logName)
   {
      super(container);
      this.logger = Logger.getLogger(logName);
   }

   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    * @throws RestletException
    */
   public void handle(RestletCall call) throws RestletException
   {
      long startTime = System.currentTimeMillis();
      super.handle(call);
      long endTime = System.currentTimeMillis();
      int duration = (int)(endTime - startTime);

      StringBuilder sb = new StringBuilder();
      
      // Append the time stamp
      long currentTime = System.currentTimeMillis();
      sb.append(String.format("%tF", currentTime));
      sb.append('\t');
      sb.append(String.format("%tT", currentTime));

      // Append the method name
      sb.append('\t');
      String methodName = call.getMethod().getName();
      sb.append((methodName == null) ? "-" : methodName);

      // Append the resource path
      sb.append('\t');
      String resourcePath = call.getResourceRef().getPath();
      sb.append((resourcePath == null) ? "-" : resourcePath);

      // Append the user name
      sb.append("\t-");

      // Append the client IP address
      sb.append('\t');
      String clientAddress = call.getClientAddress();
      sb.append((clientAddress == null) ? "-" : clientAddress);

      // Append the version
      sb.append("\t-");

      // Append the client name
      sb.append('\t');
      String clientName = call.getClientName();
      sb.append((clientName == null) ? "-" : clientName);

      // Append the referrer
      sb.append('\t');
      sb.append((call.getReferrerRef() == null) ? "-" : call.getReferrerRef().getIdentifier());

      // Append the status code
      sb.append('\t');
      sb.append((call.getStatus() == null) ? "-" : Integer.toString(call.getStatus().getHttpCode()));

      // Append the returned size
      sb.append('\t');
      if(call.getOutput() == null)
      {
         sb.append('0');
      }
      else
      {
         sb.append((call.getOutput().getSize() == -1) ? "-" : Long.toString(call.getOutput().getSize()));
      }

      // Append the resource query
      sb.append('\t');
      String query = call.getResourceRef().getQuery();
      sb.append((query == null) ? "-" : query);

      // Append the virtual name
      sb.append('\t');
      sb.append((call.getResourceRef() == null) ? "-" : call.getResourceRef().getHostIdentifier());

      // Append the duration
      sb.append('\t');
      sb.append(duration);

      // Add the log entry
      this.logger.log(Level.INFO, sb.toString());
   }
   
}
