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

package com.noelios.restlet;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.Context;
import org.restlet.Filter;

import com.noelios.restlet.util.CallModel;
import com.noelios.restlet.util.StringTemplate;

/**
 * Filter logging all calls after their handling by the target Restlet. The current format 
 * is similar to IIS 6 logs. The logging is based on the java.util.logging package.
 * @see <a href="http://www.restlet.org/tutorial#part07">Tutorial: Filters and call logging</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class LogFilter extends Filter
{
   /** Obtain a suitable logger. */
	private Logger logger;

   /** 
    * The log template to use. 
    * @see com.noelios.restlet.util.CallModel
    * @see com.noelios.restlet.util.StringTemplate
    */
   protected StringTemplate logTemplate;

   /**
    * Constructor using the default format. Here is the default format using the 
    * <a href="http://analog.cx/docs/logfmt.html">Analog syntax</a>: 
    * %Y-%m-%d\t%h:%n:%j\t%j\t%r\t%u\t%s\t%j\t%B\t%f\t%c\t%b\t%q\t%v\t%T
    * @param context The context.
    * @param logName The log name to used in the logging.properties file.
    */
   public LogFilter(Context context, String logName)
   {
      super(context);
      this.logger = Logger.getLogger(logName);
      this.logTemplate = null;
   }

   /**
    * Constructor.
    * @param context The context.
    * @param logName The log name to used in the logging.properties file.
    * @param logFormat The log format to use.
    * @see com.noelios.restlet.util.CallModel
    * @see com.noelios.restlet.util.StringTemplate
    */
   public LogFilter(Context context, String logName, String logFormat)
   {
      super(context);
      this.logger = Logger.getLogger(logName);
      this.logTemplate = new StringTemplate(logFormat);
   }

   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
   	beforeHandle(call);
   	
      long startTime = System.currentTimeMillis();
      super.handle(call);
      int duration = (int)(System.currentTimeMillis() - startTime);

      // Format the call into a log entry
      if(this.logTemplate != null)
      {
         this.logger.log(Level.INFO, format(call));
      }
      else
      {
         this.logger.log(Level.INFO, formatDefault(call, duration));
      }
      
      afterHandle(call);
   }

   /**
    * Format a log entry using the default format.
    * @param call The call to log.
    * @param duration The call duration.
    * @return The formatted log entry.
    */
   protected String formatDefault(Call call, int duration)
   {
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
      String clientAddress = call.getClient().getAddress();
      sb.append((clientAddress == null) ? "-" : clientAddress);

      // Append the version
      sb.append("\t-");

      // Append the client name
      sb.append('\t');
      String clientName = call.getClient().getName();
      sb.append((clientName == null) ? "-" : clientName);

      // Append the referrer
      sb.append('\t');
      sb.append((call.getReferrerRef() == null) ? "-" : call.getReferrerRef().getIdentifier());

      // Append the status code
      sb.append('\t');
      sb.append((call.getStatus() == null) ? "-" : Integer.toString(call.getStatus().getCode()));

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

      return sb.toString();
   }

   /**
    * Format a log entry.
    * @param call The call to log.
    * @return The formatted log entry.
    */
   protected String format(Call call)
   {
      return this.logTemplate.process(new CallModel(call, "-"));
   }

}