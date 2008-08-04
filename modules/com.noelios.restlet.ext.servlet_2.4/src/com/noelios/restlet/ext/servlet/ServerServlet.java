/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.servlet;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;

import com.noelios.restlet.http.HttpServerHelper;

/**
 * Servlet acting like an HTTP server connector. See <a
 * href="/documentation/1.0/faq#02">Developper FAQ #2</a> for details on how to
 * integrate a Restlet application into a servlet container.<br/> Here is a
 * sample configuration for your Restlet webapp:
 * 
 * <pre>
 * &lt;?xml version=&quot;1.0&quot; encoding=&quot;ISO-8859-1&quot;?&gt;
 * &lt;!DOCTYPE web-app PUBLIC &quot;-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN&quot; &quot;http://java.sun.com/dtd/web-app_2_3.dtd&quot;&gt;
 * &lt;web-app&gt;
 *         &lt;display-name&gt;Restlet adapter&lt;/display-name&gt;
 *                                                         
 *         &lt;!-- Your application class name --&gt;
 *         &lt;context-param&gt;
 *                 &lt;param-name&gt;org.restlet.application&lt;/param-name&gt;
 *                 &lt;param-value&gt;com.noelios.restlet.test.TraceApplication&lt;/param-value&gt;
 *         &lt;/context-param&gt;
 *                                                         
 *         &lt;!-- Restlet adapter --&gt;
 *         &lt;servlet&gt;
 *                 &lt;servlet-name&gt;ServerServlet&lt;/servlet-name&gt;
 *                 &lt;servlet-class&gt;com.noelios.restlet.ext.servlet.ServerServlet&lt;/servlet-class&gt;
 *         &lt;/servlet&gt;
 *                                                         
 *         &lt;!-- Catch all requests --&gt;
 *         &lt;servlet-mapping&gt;
 *                 &lt;servlet-name&gt;ServerServlet&lt;/servlet-name&gt;
 *                 &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *         &lt;/servlet-mapping&gt;
 * &lt;/web-app&gt;
 * </pre>
 * 
 * The enumeration of initParameters of your Servlet will be copied to the
 * "context.parameters" property of your application. This way, you can pass
 * additional initialization parameters to your Restlet application, and share
 * them with existing Servlets.
 * 
 * @see <a href="http://java.sun.com/j2ee/">J2EE home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ServerServlet extends HttpServlet {
	/**
	 * The Servlet context initialization parameter's name containing the name
	 * of the Servlet context attribute that should be used to store the Restlet
	 * Application instance.
	 */
	private static final String NAME_APPLICATION_ATTRIBUTE = "org.restlet.attribute.application";

	/** The default value for the NAME_APPLICATION_ATTRIBUTE parameter. */
	private static final String NAME_APPLICATION_ATTRIBUTE_DEFAULT = "com.noelios.restlet.ext.servlet.ServerServlet.application";

	/**
	 * The Servlet context initialization parameter's name containing the name
	 * of the Servlet context attribute that should be used to store the Restlet
	 * Component instance.
	 */
	private static final String NAME_COMPONENT_ATTRIBUTE = "org.restlet.attribute.component";

	/** The default value for the NAME_COMPONENT_ATTRIBUTE parameter. */
	private static final String NAME_COMPONENT_ATTRIBUTE_DEFAULT = "com.noelios.restlet.ext.servlet.ServerServlet.component";

	/**
	 * The Servlet context initialization parameter's name containing the name
	 * of the Servlet context attribute that should be used to store the HTTP
	 * server connector instance.
	 */
	private static final String NAME_SERVER_ATTRIBUTE = "org.restlet.attribute.server";

	/** The default value for the NAME_SERVER_ATTRIBUTE parameter. */
	private static final String NAME_SERVER_ATTRIBUTE_DEFAULT = "com.noelios.restlet.ext.servlet.ServerServlet.server";

	/** Serial version identifier. */
	private static final long serialVersionUID = 1L;

	/** The associated Restlet application. */
	private transient Application application;

	/** The associated Restlet component. */
	private transient Component component;

	/** The associated HTTP server helper. */
	private transient HttpServerHelper helper;

	/**
	 * Constructor.
	 */
	public ServerServlet() {
		this.application = null;
		this.component = null;
		this.helper = null;
	}

	/**
	 * Creates the single Application used by this Servlet.
	 * 
	 * @param context
	 *            The Context for the Application
	 * 
	 * @return The newly created Application or null if unable to create
	 */
	public Application createApplication(Context context) {
		Application application = null;
		// Try to instantiate a new target application
		// First, find the application class name
		String applicationClassName = getInitParameter(Application.KEY, null);

		// Load the application class using the given class name
		if (applicationClassName != null) {
			try {
				// According to
				// http://www.caucho.com/resin-3.0/webapp/faq.xtp#Class.forName()-doesn't-seem-to-work-right
				// this approach may need to used when loading classes.
				Class<?> targetClass;
				ClassLoader loader = Thread.currentThread()
						.getContextClassLoader();

				if (loader != null)
					targetClass = Class.forName(applicationClassName, false,
							loader);
				else
					targetClass = Class.forName(applicationClassName);

				try {
					// Create a new instance of the application class by
					// invoking the constructor with the Context parameter.
					application = (Application) targetClass.getConstructor(
							Context.class).newInstance(context);
				} catch (NoSuchMethodException e) {
					log(
							"[Noelios Restlet Engine] - The ServerServlet couldn't invoke the constructor of the target class. Please check this class has a constructor with a single parameter of Context. The empty constructor and the context setter wille used instead. "
									+ applicationClassName, e);
					// The constructor with the Context parameter does not
					// exist. Instantiate an application with the default
					// constructor then invoke the setContext method.
					application = (Application) targetClass.getConstructor()
							.newInstance();
				}
			} catch (ClassNotFoundException e) {
				log(
						"[Noelios Restlet Engine] - The ServerServlet couldn't find the target class. Please check that your classpath includes "
								+ applicationClassName, e);

			} catch (InstantiationException e) {
				log(
						"[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the target class. Please check this class has an empty constructor "
								+ applicationClassName, e);
			} catch (IllegalAccessException e) {
				log(
						"[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the target class. Please check that you have to proper access rights to "
								+ applicationClassName, e);
			} catch (NoSuchMethodException e) {
				log(
						"[Noelios Restlet Engine] - The ServerServlet couldn't invoke the constructor of the target class. Please check this class has a constructor with a single parameter of Context "
								+ applicationClassName, e);
			} catch (InvocationTargetException e) {
				log(
						"[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the target class. An exception was thrown while creating "
								+ applicationClassName, e);
			}

			if (application != null) {
				// Set the context based on the Servlet's context
				application.setContext(new ServletContextAdapter(this,
						application, context));
			}
		}

		return application;
	}

	/**
	 * Creates the associated HTTP server handling calls.
	 * 
	 * @param request
	 *            The HTTP Servlet request.
	 * @return The new HTTP server handling calls.
	 */
	public HttpServerHelper createServer(HttpServletRequest request) {
		HttpServerHelper result = null;
		Component component = getComponent();
		Application application = getApplication();

		if ((component != null) && (application != null)) {
			// First, let's locate the closest component
			Server server = new Server(component.getContext(),
					(List<Protocol>) null, request.getLocalAddr(), request
							.getLocalPort(), component);
			result = new HttpServerHelper(server);

			// Attach the application
			String uriPattern = request.getContextPath()
					+ request.getServletPath();
			component.getDefaultHost().attach(uriPattern, application);
		}

		return result;
	}

	@Override
	public void destroy() {
		if ((getApplication() != null) && (getApplication().isStarted())) {
			try {
				getApplication().stop();
			} catch (Exception e) {
				log("Error during the stopping of the Restlet Application", e);
			}
		}

		super.destroy();
	}

	/**
	 * Returns the application. It creates a new one if none exists.
	 * 
	 * @return The application.
	 */
	public Application getApplication() {
		Application result = this.application;

		if (result == null) {
			synchronized (ServerServlet.class) {
				// Find the attribute name to use to store the application
				String applicationAttributeName = getInitParameter(
						NAME_APPLICATION_ATTRIBUTE,
						NAME_APPLICATION_ATTRIBUTE_DEFAULT);

				// Look up the attribute for a target
				result = (Application) getServletContext().getAttribute(
						applicationAttributeName);

				if (result == null) {
					result = createApplication(getComponent().getContext());
					getServletContext().setAttribute(applicationAttributeName,
							result);
				}

				this.application = result;
			}
		}

		return result;
	}

	/**
	 * Returns the component. It creates a new one if none exists.
	 * 
	 * @return The component.
	 */
	public Component getComponent() {
		Component result = this.component;

		if (result == null) {
			synchronized (ServerServlet.class) {
				// Find the attribute name to use to store the component
				String componentAttributeName = getInitParameter(
						NAME_COMPONENT_ATTRIBUTE,
						NAME_COMPONENT_ATTRIBUTE_DEFAULT);

				// Look up the attribute for a target
				result = (Component) getServletContext().getAttribute(
						componentAttributeName);

				if (result == null) {
					result = new Component();
					// The status service is disabled by default.
					result.getStatusService().setEnabled(false);
					getServletContext().setAttribute(componentAttributeName,
							result);
				}

				this.component = result;
			}
		}

		return result;
	}

	/**
	 * Returns the value of a given initialization parameter, first from the
	 * Servlet configuration, then from the Web Application context.
	 * 
	 * @param name
	 *            The parameter name.
	 * @param defaultValue
	 *            The default to use in case the parameter is not found.
	 * @return The value of the parameter or null.
	 */
	public String getInitParameter(String name, String defaultValue) {
		String result = getServletConfig().getInitParameter(name);

		if (result == null) {
			result = getServletConfig().getServletContext().getInitParameter(
					name);
		}

		if (result == null) {
			result = defaultValue;
		}

		return result;
	}

	/**
	 * Returns the associated HTTP server handling calls. It creates a new one
	 * if none exists.
	 * 
	 * @param request
	 *            The HTTP Servlet request.
	 * @return The HTTP server handling calls.
	 */
	public HttpServerHelper getServer(HttpServletRequest request) {
		HttpServerHelper result = this.helper;

		if (result == null) {
			synchronized (ServerServlet.class) {
				// Find the attribute name to use to store the server reference
				String serverAttributeName = getInitParameter(
						NAME_SERVER_ATTRIBUTE, NAME_SERVER_ATTRIBUTE_DEFAULT);

				// Look up the attribute for a target
				result = (HttpServerHelper) getServletContext().getAttribute(
						serverAttributeName);

				if (result == null) {
					result = createServer(request);
					getServletContext().setAttribute(serverAttributeName,
							result);
				}

				this.helper = result;
			}
		}

		return result;
	}

	@Override
	public void init() throws ServletException {
		if ((getApplication() != null) && (getApplication().isStopped())) {
			try {
				getApplication().start();
			} catch (Exception e) {
				log("Error during the starting of the Restlet Application", e);
			}
		}
	}

	/**
	 * Services a HTTP Servlet request as an uniform call.
	 * 
	 * @param request
	 *            The HTTP Servlet request.
	 * @param response
	 *            The HTTP Servlet response.
	 */
	@Override
	public void service(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpServerHelper helper = getServer(request);

		if (helper != null) {
			helper
					.handle(new ServletCall(helper.getServer(), request,
							response));
		} else {
			log("[Noelios Restlet Engine] - Unable to get the Restlet HTTP server connector. Status code 500 returned.");
			response.sendError(500);
		}
	}

}
