/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.servlet.internal;

import java.util.logging.Logger;

import jakarta.servlet.ServletContext;

import org.restlet.engine.log.LoggerFacade;

/**
 * Restlet log facade for the {@link ServletContext#log(String, Throwable)}
 * method. In order to use Servlet as the logging facade for Restlet, you need
 * to set the "org.restlet.engine.loggerFacadeClass" system property with the
 * "org.restlet.ext.servlet.ServletLoggerFacade" value.
 * 
 * @see ServletContext#log(String, Throwable)
 * @author Jerome Louvel
 */
public class ServletLoggerFacade extends LoggerFacade {

	/**
	 * As Servlet logging doesn't use logger names, we will reuse the same logger.
	 **/
	private final ServletLogger servletLogger;

	/**
	 * Constructor.
	 * 
	 * @param context The Servlet context of the parent container.
	 */
	public ServletLoggerFacade(ServletContext context) {
		this.servletLogger = createServletLogger(context);
	}

	/**
	 * Create a new reusable {@link ServletLogger}.
	 * 
	 * @param context The Servlet context of the parent container.
	 * @return The new reusable {@link ServletLogger}.
	 */
	protected ServletLogger createServletLogger(ServletContext context) {
		return new ServletLogger(context);
	}

	@Override
	public Logger getAnonymousLogger() {
		return getServletLogger();
	}

	@Override
	public Logger getLogger(String loggerName) {
		return getServletLogger();
	}

	/**
	 * Returns the Servlet logger reused for all calls as there is no need for
	 * distinct logger with Servlet logging methods.
	 * 
	 * @return The Servlet logger reused for all calls.
	 */
	protected ServletLogger getServletLogger() {
		return servletLogger;
	}

}
