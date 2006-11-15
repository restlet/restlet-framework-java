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

package org.restlet.service;

/**
 * Service providing access logging.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class LogService
{
	/** Indicates if the service has been enabled. */
	private boolean enabled;

	/** The access logger name. */
	private String accessLoggerName;

	/** The context logger name. */
	private String contextLoggerName;

	/** The format. */
	private String format;

	/**
	 * Constructor.
	 * @param enabled True if the service has been enabled.
	 */
	public LogService(boolean enabled)
	{
		this.accessLoggerName = null;
		this.contextLoggerName = null;
		this.enabled = enabled;
		this.format = null;
	}

	/**
	 * Indicates if the service should be enabled.
	 * @return True if the service should be enabled.
	 */
	public boolean isEnabled()
	{
		return this.enabled;
	}

	/**
	 * Indicates if the service should be enabled.
	 * @param enabled True if the service should be enabled.
	 */
	public void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * @deprecated Use getAccessLoggerName() instead.
	 */
	@Deprecated
	public String getLoggerName()
	{
		return getAccessLoggerName();
	}

	/**
	 * @deprecated Use setAccessLoggerName() instead.
	 */
	@Deprecated
	public void setLoggerName(String name)
	{
		setAccessLoggerName(name);
	}

	/**
	 * Returns the name of the JDK's logger to use when logging calls.
	 * @return The name of the JDK's logger to use when logging calls.
	 */
	public String getAccessLoggerName()
	{
		return this.accessLoggerName;
	}

	/**
	 * Sets the name of the JDK's logger to use when logging calls.
	 * @param name The name of the JDK's logger to use when logging calls.
	 */
	public void setAccessLoggerName(String name)
	{
		this.accessLoggerName = name;
	}

	/**
	 * Returns the name of the JDK's logger to use when logging context messages.
	 * @return The name of the JDK's logger to use when logging context messages.
	 */
	public String getContextLoggerName()
	{
		return this.contextLoggerName;
	}

	/**
	 * Sets the name of the JDK's logger to use when logging context messages.
	 * @param name The name of the JDK's logger to use when logging context messages.
	 */
	public void setContextLoggerName(String name)
	{
		this.contextLoggerName = name;
	}

	/**
	 * @deprecated Use getAccessLogFormat() instead.
	 */
	@Deprecated
	public String getFormat()
	{
		return getAccessLogFormat();
	}

	/**
	 * @deprecated Use setAccessLogFormat() instead.
	 */
	@Deprecated
	public void setFormat(String format)
	{
		setAccessLogFormat(format);
	}

	/**
	 * Returns the format used.
	 * @return The format used, or null if the default one is used.
	 */
	public String getAccessLogFormat()
	{
		return this.format;
	}

	/**
	 * Sets the format to use when logging calls. The default format matches the one of IIS 6.
	 * See com.noelios.restlet.util.CallModel for format details.
	 * @param format The format to use when loggin calls. 
	 */
	public void setAccessLogFormat(String format)
	{
		this.format = format;
	}

}
