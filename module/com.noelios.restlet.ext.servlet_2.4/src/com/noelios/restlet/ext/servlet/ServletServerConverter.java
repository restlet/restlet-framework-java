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

import org.restlet.Context;
import org.restlet.data.Request;

import javax.servlet.ServletContext;

import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.http.HttpServerConverter;

/**
 * Servlet server converter. Adds an attribute named "org.restlet.http.servlet.context" to each request 
 * containing the ServletContext instance.
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ServletServerConverter extends HttpServerConverter
{
	/** The Servlet context. */ 
	private ServletContext servletContext;
	
	/**
	 * Constructor.
	 * @param context The client context.
	 * @param servletContext The Servlet context.
	 */
	public ServletServerConverter(Context context, ServletContext servletContext)
	{
		super(context);
		this.servletContext = servletContext;
	}

	/**
	 * Converts a low-level HTTP call into a high-level uniform request.
	 * @param httpCall The low-level HTTP call.
	 * @return A new high-level uniform request.
	 */
	public Request toRequest(HttpServerCall httpCall)
	{
		Request result = super.toRequest(httpCall);
		result.getAttributes().put("org.restlet.http.servlet.context", getServletContext());
		return result;
	}

	/**
	 * Returns the 
	 * @return
	 */
	public ServletContext getServletContext()
	{
		return this.servletContext;
	}
	
}
