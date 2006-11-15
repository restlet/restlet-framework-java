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

package com.noelios.restlet.ext.servlet;

import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Logger that wraps the logging methods of javax.servlet.ServletContext.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ServletLogger extends Logger
{
	/** The Servlet context to use for logging. */
	private javax.servlet.ServletContext context;

	/**
	 * Constructor.
	 * @param context The Servlet context to use.
	 */
	public ServletLogger(javax.servlet.ServletContext context)
	{
		super(null, null);
		this.context = context;
	}

	/**
	 * Log a LogRecord.
	 * @param record The LogRecord to be published
	 */
	public void log(LogRecord record)
	{
		getContext().log(record.getMessage(), record.getThrown());
	}

	/**
	 * Returns the Servlet context to use for logging.
	 * @return The Servlet context to use for logging.
	 */
	private javax.servlet.ServletContext getContext()
	{
		return this.context;
	}

}
