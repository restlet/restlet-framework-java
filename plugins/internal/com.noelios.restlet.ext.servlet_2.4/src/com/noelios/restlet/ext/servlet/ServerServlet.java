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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Application;
import org.restlet.Restlet;

import com.noelios.restlet.impl.component.ContainerImpl;
import com.noelios.restlet.impl.connector.HttpServer;

/**
 * Servlet acting like an HTTP server connector. See the getTarget() method for details on how 
 * to provide a target for your server.<br/> Here is a sample configuration for your Restlet webapp:
 * 
 * <pre>
 * &lt;?xml version="1.0" encoding="ISO-8859-1"?&gt;
 * &lt;!DOCTYPE web-app PUBLIC "-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN" "http://java.sun.com/dtd/web-app_2_3.dtd"&gt;
 * &lt;web-app&gt;
 * 	&lt;display-name&gt;Server Servlet&lt;/display-name&gt;
 * 	&lt;description&gt;Servlet acting as a Restlet server connector&lt;/description&gt;
 * 
 * 	&lt;!-- Application class handling calls --&gt;
 * 	&lt;context-param&gt;
 * 		&lt;param-name&gt;org.restlet.application&lt;/param-name&gt;
 * 		&lt;param-value&gt;com.noelios.restlet.test.TraceApplication&lt;/param-value&gt;
 * 	&lt;/context-param&gt;
 * 
 * 	&lt;!-- ServerServlet class or a subclass --&gt;
 * 	&lt;servlet&gt;
 * 		&lt;servlet-name&gt;ServerServlet&lt;/servlet-name&gt;
 * 		&lt;servlet-class&gt;com.noelios.restlet.ext.servlet.ServerServlet&lt;/servlet-class&gt;
 * 	&lt;/servlet&gt;
 * 
 * 	&lt;!-- Mapping of requests to the ServerServlet --&gt;
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
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
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

	/** The associated HTTP server connector. */
	private HttpServer server;

	/**
	 * Constructor.
	 */
	public ServerServlet()
	{
		this.server = null;
	}

	/**
	 * Services a HTTP Servlet request as an uniform call.
	 * @param request The HTTP Servlet request.
	 * @param response The HTTP Servlet response.
	 */
	protected void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException
	{
		ServletCall httpCall = new ServletCall(request, response);
		getServer(request).handle(httpCall);
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
	public HttpServer getServer(HttpServletRequest request)
	{
		HttpServer result = this.server;

		if (result == null)
		{
			synchronized (ServerServlet.class)
			{
				// Find the attribute name to use to store the server reference
				String serverAttributeName = getInitParameter(NAME_SERVER_ATTRIBUTE,
						NAME_SERVER_ATTRIBUTE_DEFAULT);

				// Look up the attribute for a target
				result = (HttpServer) getServletContext().getAttribute(serverAttributeName);

				if (result == null)
				{
					result = new HttpServer();
					getServletContext().setAttribute(NAME_SERVER_ATTRIBUTE, result);

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
							Object target = null;

							// Create a new instance of the application class
							// and store it for reuse by other ServerServlets.
							target = targetClass.newInstance();

							// First, let's locate the closest container
							ContainerImpl container = new ContainerImpl();
							if (target instanceof Application)
							{
								// The target is probably a standalone Restlet or Filter or Router
								// Try to get its parent, even if chances to find one are low
								//								container.setRoot((Restlet)target);
							}

							if (container != null)
							{
								// Add the HTTP server connector adapting the Servlet requests 
								container.getServers().add(result);

								// Create a local client and add it to the container
								container.getClients().add(
										new ServletLocalClient(getServletContext()));

								// Create the context based on the Servlet's context
								// and set it on the container and optionally the target Restlet
								container.setContext(new ServletContext(this, container));
								result.setContext(container.getContext());
								if (target != container)
									((Restlet) target).setContext(container.getContext());

								// Provide the context path as an init parameter
								//								String scheme = request.getScheme();
								//								String hostName = request.getServerName();
								//								int hostPort = request.getServerPort();
								//								String servletPath = request.getContextPath()
								//										+ request.getServletPath();
								//								String contextPath = Reference.toString(scheme, hostName,
								//										hostPort, servletPath, null, null);
								//								container.getContext().getParameters().add(
								//										initContextPathName, contextPath);
								//								log("[Noelios Restlet Engine] - This context path has been provided to the target's init parameter \""
								//										+ initContextPathName + "\": " + contextPath);

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

				this.server = result;
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
