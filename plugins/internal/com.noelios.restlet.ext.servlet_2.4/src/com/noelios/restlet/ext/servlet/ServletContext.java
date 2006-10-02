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

import java.util.Enumeration;

import javax.servlet.Servlet;

import com.noelios.restlet.impl.component.ContainerContext;
import com.noelios.restlet.impl.component.ContainerImpl;

/**
 * Context allowing access to the container's connectors, reusing the Servlet's logging mechanism and
 * adding the Servlet's initialization parameters to the context's parameters.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ServletContext extends ContainerContext
{
	/** The parent servlet. */
	private Servlet servlet;

	/**
	 * Constructor.
	 * @param servlet The parent Servlet. 
	 * @param container The parent container.
	 */
	public ServletContext(Servlet servlet, ContainerImpl container)
	{
		super(container, new ServletLogger(servlet.getServletConfig().getServletContext()));
		this.servlet = servlet;
		
		// Copy all the servlet parameters into the context
		String initParam;

		// Copy all the Web Container initialization parameters
		javax.servlet.ServletConfig servletConfig = getServlet().getServletConfig();
		for(Enumeration enum1 = servletConfig.getInitParameterNames(); enum1.hasMoreElements(); )
		{
			initParam = (String)enum1.nextElement();
			getParameters().add(initParam, servletConfig.getInitParameter(initParam));
		}

		// Copy all the Web Application initialization parameters
		javax.servlet.ServletContext servletContext = getServlet().getServletConfig().getServletContext();
		for(Enumeration enum1 = servletContext.getInitParameterNames(); enum1.hasMoreElements(); )
		{
			initParam = (String)enum1.nextElement();
			getParameters().add(initParam, servletContext.getInitParameter(initParam));
		}
	}

	/**
	 * Returns the parent servlet.
	 * @return The parent servlet.
	 */
	private Servlet getServlet()
	{
		return this.servlet;
	}
}
