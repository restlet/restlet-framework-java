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

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Application;
import org.restlet.Container;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;

import com.noelios.restlet.http.HttpServerHelper;

/**
 * Servlet acting like an HTTP server connector. See the getTarget() method for details on how 
 * to provide a target for your server.<br/> Here is a sample configuration for your Restlet webapp:
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="ISO-8859-1"?&gt;
 * &lt;!DOCTYPE web-app PUBLIC "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN" "http://java.sun.com/dtd/web-app_2_3.dtd"&gt;
 * &lt;web-app&gt;
 * 	&lt;display-name&gt;Restlet adapter&lt;/display-name&gt;
 * 
 * 	&lt;!-- Your application class name --&gt;
 * 	&lt;context-param&gt;
 * 		&lt;param-name&gt;org.restlet.application&lt;/param-name&gt;
 * 		&lt;param-value&gt;com.noelios.restlet.test.TraceApplication&lt;/param-value&gt;
 * 	&lt;/context-param&gt;
 * 
 * 	&lt;!-- Restlet adapter --&gt;
 * 	&lt;servlet&gt;
 * 		&lt;servlet-name&gt;ServerServlet&lt;/servlet-name&gt;
 * 		&lt;servlet-class&gt;com.noelios.restlet.ext.servlet.ServerServlet&lt;/servlet-class&gt;
 * 	&lt;/servlet&gt;
 * 
 * 	&lt;!-- Catch all requests --&gt;
 * 	&lt;servlet-mapping&gt;
 * 		&lt;servlet-name&gt;ServerServlet&lt;/servlet-name&gt;
 * 		&lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 * 	&lt;/servlet-mapping&gt;
 * &lt;/web-app&gt;}
 * </pre>
 * The enumeration of initParameters of your Servlet will be copied to the "context.parameters" property of 
 * your application. This way, you can pass additional initialization parameters to your Restlet application,
 * and share them with existing Servlets.
 * @see <a href="http://java.sun.com/j2ee/">J2EE home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ServerServlet extends HttpServlet
{
	/** 
	 * The Servlet context initialization parameter's name containing the application's 
	 * class name to use to create the application instance. 
	 */
	public static final String NAME_APPLICATION_CLASS = "org.restlet.application";

	/** 
	 * The Servlet context initialization parameter's name containing the name of the 
	 * Servlet context attribute that should be used to store the HTTP server connector instance. 
	 */
	public static final String NAME_SERVER_ATTRIBUTE = "org.restlet.server";

	/** The default value for the NAME_SERVER_ATTRIBUTE parameter. */
	public static final String NAME_SERVER_ATTRIBUTE_DEFAULT = "com.noelios.restlet.ext.servlet.ServerServlet.server";

	/** Serial version identifier. */
	private static final long serialVersionUID = 1L;

	/** The associated HTTP server helper. */
	private transient HttpServerHelper helper;

	/**
	 * Constructor.
	 */
	public ServerServlet()
	{
		this.helper = null;
	}

	/**
	 * Services a HTTP Servlet request as an uniform call.
	 * @param request The HTTP Servlet request.
	 * @param response The HTTP Servlet response.
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		HttpServerHelper helper = getServer(request);

		if (helper != null)
		{
			helper.handle(new ServletCall(helper.getLogger(), request, response));
		}
		else
		{
			log("[Noelios Restlet Engine] - Unable to get the Restlet HTTP server connector. Status code 500 returned.");
			response.sendError(500);
		}
	}

	/**
	 * Returns the associated HTTP server handling calls.<br/>
	 * For the first invocation, we look for an existing target in the application context, using the 
	 * NAME_APPLICATION_ATTRIBUTE parameter.<br/>
	 * We lookup for the attribute name in the servlet configuration, then in the application context.<br/>
	 * If no target exists, we try to instantiate one based on the class name set in the NAME_APPLICATION_CLASS 
	 * parameter.<br/>
	 * We lookup for the class name in the servlet configuration, then in the application context.<br/>
	 * Once the target is found, we wrap the servlet request and response into a Restlet HTTP call and ask the 
	 * target application to handle it.<br/>
	 * When the handling is done, we write the result back into the result object and return from the service 
	 * method.
	 * @param request The HTTP Servlet request.
	 * @return The HTTP server handling calls.
	 */
	public HttpServerHelper getServer(HttpServletRequest request)
	{
		HttpServerHelper result = this.helper;

		if (result == null)
		{
			synchronized (ServerServlet.class)
			{
				// Find the attribute name to use to store the server reference
				String serverAttributeName = getInitParameter(NAME_SERVER_ATTRIBUTE,
						NAME_SERVER_ATTRIBUTE_DEFAULT);

				// Look up the attribute for a target
				result = (HttpServerHelper) getServletContext().getAttribute(
						serverAttributeName);

				if (result == null)
				{
					// Try to instantiate a new target application
					// First, find the application class name
					String applicationClassName = getInitParameter(NAME_APPLICATION_CLASS,
							null);
					if (applicationClassName != null)
					{
						try
						{
							// Load the application class using the given class name
							Class targetClass = Class.forName(applicationClassName);

							// First, let's locate the closest container
							Container container = new Container();
							Server server = new Server(container.getContext(),
									(List<Protocol>) null, request.getLocalAddr(), request
											.getLocalPort(), container);
							result = new HttpServerHelper(server);
							getServletContext().setAttribute(NAME_SERVER_ATTRIBUTE, result);

							if (container != null)
							{
								// Create a new instance of the application class
								Application application = (Application) targetClass
										.getConstructor(Context.class).newInstance(
												container.getContext());

								// Set the Servlet context
								application.setContext(new ServletContextAdapter(this, application,
										container.getContext()));

								// Attach the application
								String uriPattern = request.getContextPath()
										+ request.getServletPath();
								container.getDefaultHost().attach(uriPattern, application);

								// Starts the target Restlet
								result.start();
							}
							else
							{
								log("[Noelios Restlet Engine] - The Restlet container couldn't be instantiated.");
							}
						}
						catch (ClassNotFoundException e)
						{
							log(
									"[Noelios Restlet Engine] - The ServerServlet couldn't find the target class. Please check that your classpath includes "
											+ applicationClassName, e);
						}
						catch (InstantiationException e)
						{
							log(
									"[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the target class. Please check this class has an empty constructor "
											+ applicationClassName, e);
						}
						catch (IllegalAccessException e)
						{
							log(
									"[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the target class. Please check that you have to proper access rights to "
											+ applicationClassName, e);
						}
						catch (Exception e)
						{
							log(
									"[Noelios Restlet Engine] - The ServerServlet couldn't start the target Restlet.",
									e);
						}
					}
					else
					{
						log("[Noelios Restlet Engine] - The ServerServlet couldn't find the target class name. Please set the initialization parameter called "
								+ NAME_APPLICATION_CLASS);
					}
				}

				this.helper = result;
			}
		}

		return result;
	}

	/**
	 * Returns the value of a given initialization parameter, first from the Servlet configuration, then
	 * from the Web Application context.
	 * @param name The parameter name.
	 * @param defaultValue The default to use in case the parameter is not found.
	 * @return The value of the parameter or null.
	 */
	public String getInitParameter(String name, String defaultValue)
	{
		String result = getServletConfig().getInitParameter(name);

		if (result == null)
		{
			result = getServletConfig().getServletContext().getInitParameter(name);
		}

		if (result == null)
		{
			result = defaultValue;
		}

		return result;
	}

}
