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

package org.restlet.util;

import org.restlet.Application;

/**
 * Service providing access logging.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class LogService extends Service
{
	/** The logger name. */
	private String loggerName;
	
	/** The format. */
	private String format;
	
	/**
	 * Constructor.
	 * @param application The parent appplication.
	 * @param enabled True if the service has been enabled.
	 */
	public LogService(Application application, boolean enabled)
	{
		super(application, enabled);
		this.loggerName = null;
		this.format = null;
	}
	
   /**
    * Returns the name of the JDK's logger to use when logging calls.
    * @return The name of the JDK's logger to use when logging calls.
    */
   public String getLoggerName()
   {
   	return this.loggerName;
   }

   /**
    * Sets the name of the JDK's logger to use when logging calls.
    * @param name The name of the JDK's logger to use when logging calls.
    */
   public void setLoggerName(String name)
   {
   	this.loggerName = name;
   }

   /**
    * Returns the format used.
    * @return The format used, or null if the default one is used.
    */
   public String getFormat()
   {
   	return this.format;
   }
   
   /**
    * Sets the format to use when logging calls. The default format matches the one of IIS 6.
    * See com.noelios.restlet.util.CallModel for format details.
    * @param format The format to use when loggin calls. 
    */
   public void setFormat(String format)
   {
   	this.format = format;
   }
  
}
