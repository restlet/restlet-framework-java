/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.servlet;

import java.util.Enumeration;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.restlet.Application;
import org.restlet.Context;

import com.noelios.restlet.application.ApplicationContext;

/**
 * Context allowing access to the component's connectors, reusing the Servlet's
 * logging mechanism and adding the Servlet's initialization parameters to the
 * context's parameters.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ServletContextAdapter extends ApplicationContext {
	/** The Servlet context. */
	private ServletContext servletContext;

	/**
	 * Constructor.
	 * 
	 * @param servlet
	 *            The parent Servlet.
	 * @param parentContext
	 *            The parent context.
	 * @param application
	 *            The parent application.
	 */
	@SuppressWarnings("unchecked")
	public ServletContextAdapter(Servlet servlet, Application application,
			Context parentContext) {
		super(application, parentContext, new ServletLogger(servlet
				.getServletConfig().getServletContext()));
		this.servletContext = servlet.getServletConfig().getServletContext();

		// Set the special WAR client
		setWarClient(new ServletWarClient(parentContext, servlet
				.getServletConfig().getServletContext()));

		// Copy all the servlet parameters into the context
		String initParam;

		// Copy all the Web component initialization parameters
		javax.servlet.ServletConfig servletConfig = servlet.getServletConfig();
		for (Enumeration<String> enum1 = servletConfig.getInitParameterNames(); enum1
				.hasMoreElements();) {
			initParam = enum1.nextElement();
			getParameters().add(initParam,
					servletConfig.getInitParameter(initParam));
		}

		// Copy all the Web Application initialization parameters
		for (Enumeration<String> enum1 = getServletContext()
				.getInitParameterNames(); enum1.hasMoreElements();) {
			initParam = enum1.nextElement();
			getParameters().add(initParam,
					getServletContext().getInitParameter(initParam));
		}
	}

	/**
	 * Returns the Servlet context.
	 * 
	 * @return The Servlet context.
	 */
	public ServletContext getServletContext() {
		return this.servletContext;
	}

}
