/*
 * Copyright 2005-2007 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.servlet;

import java.io.IOException;
import java.util.List;

import java.lang.reflect.InvocationTargetException;

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
 * Servlet acting like an HTTP server connector. See the getTarget() method for
 * details on how to provide a target for your server.<br/> Here is a sample
 * configuration for your Restlet webapp:
 * 
 * <pre>
 *                &lt;?xml version=&quot;1.0&quot; encoding=&quot;ISO-8859-1&quot;?&gt;
 *                &lt;!DOCTYPE web-app PUBLIC &quot;-//Sun Microsystems, Inc.//DTD Web Application 2.3//EN&quot; &quot;http://java.sun.com/dtd/web-app_2_3.dtd&quot;&gt;
 *                &lt;web-app&gt;
 *                	&lt;display-name&gt;Restlet adapter&lt;/display-name&gt;
 *                
 *                	&lt;!-- Your application class name --&gt;
 *                	&lt;context-param&gt;
 *                		&lt;param-name&gt;org.restlet.application&lt;/param-name&gt;
 *                		&lt;param-value&gt;com.noelios.restlet.test.TraceApplication&lt;/param-value&gt;
 *                	&lt;/context-param&gt;
 *                
 *                	&lt;!-- Restlet adapter --&gt;
 *                	&lt;servlet&gt;
 *                		&lt;servlet-name&gt;ServerServlet&lt;/servlet-name&gt;
 *                		&lt;servlet-class&gt;com.noelios.restlet.ext.servlet.ServerServlet&lt;/servlet-class&gt;
 *                	&lt;/servlet&gt;
 *                
 *                	&lt;!-- Catch all requests --&gt;
 *                	&lt;servlet-mapping&gt;
 *                		&lt;servlet-name&gt;ServerServlet&lt;/servlet-name&gt;
 *                		&lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *                	&lt;/servlet-mapping&gt;
 *                &lt;/web-app&gt;}
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
     * The Servlet context initialization parameter's name containing the
     * application's class name to use to create the application instance.
     */
    public static final String NAME_APPLICATION_CLASS = "org.restlet.application";

    /**
     * The Servlet context initialization parameter's name containing the name
     * of the Servlet context attribute that should be used to store the HTTP
     * server connector instance.
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
    public ServerServlet() {
        this.helper = null;
    }

    /**
     * Services a HTTP Servlet request as an uniform call.
     * 
     * @param request
     *            The HTTP Servlet request.
     * @param response
     *            The HTTP Servlet response.
     */
    protected void service(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
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

    /**
     * Returns the associated HTTP server handling calls.<br/> For the first
     * invocation, we look for an existing target in the application context,
     * using the NAME_APPLICATION_ATTRIBUTE parameter.<br/> We lookup for the
     * attribute name in the servlet configuration, then in the application
     * context.<br/> If no target exists, we try to instantiate one based on
     * the class name set in the NAME_APPLICATION_CLASS parameter.<br/> We
     * lookup for the class name in the servlet configuration, then in the
     * application context.<br/> Once the target is found, we wrap the servlet
     * request and response into a Restlet HTTP call and ask the target
     * application to handle it.<br/> When the handling is done, we write the
     * result back into the result object and return from the service method.
     * 
     * @param request
     *            The HTTP Servlet request.
     * @return The HTTP server handling calls.
     */
    protected HttpServerHelper getServer(HttpServletRequest request) {
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
                    if (result != null) {
                        // Starts the target Restlet
                        try {
                            result.start();
                        } catch (Exception e) {
                            log(
                                    "[Noelios Restlet Engine] - The ServerServlet couldn't start the target Restlet.",
                                    e);
                        }
                    } else {
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
     * Creates the associated HTTP server handling calls.
     * 
     * @param request
     *            The HTTP Servlet request.
     * @return The new HTTP server handling calls.
     */
    protected HttpServerHelper createServer(HttpServletRequest request) {
        HttpServerHelper result = null;

        Component component = new Component();
        Application application = createApplication(component.getContext());
        if (application != null) {
            // First, let's locate the closest component
            Server server = new Server(component.getContext(),
                    (List<Protocol>) null, request.getLocalAddr(), request
                            .getLocalPort(), component);
            result = new HttpServerHelper(server);
            getServletContext().setAttribute(NAME_SERVER_ATTRIBUTE, result);

            // Set the Servlet context
            application.setContext(new ServletContextAdapter(this, application,
                    component.getContext()));

            // Attach the application
            String uriPattern = request.getContextPath()
                    + request.getServletPath();
            component.getDefaultHost().attach(uriPattern, application);

        }
        return result;
    }

    /**
     * Creates the single Application used by this Servlet.
     * 
     * @param context
     *            The Context for the Application
     * 
     * @return The newly created Application or null if unable to create
     */
    protected Application createApplication(Context context) {
        // Try to instantiate a new target application
        // First, find the application class name
        String applicationClassName = getInitParameter(NAME_APPLICATION_CLASS,
                null);

        // Load the application class using the given class name
        if (applicationClassName != null) {
            try {
                Class targetClass = Class.forName(applicationClassName);

                // Create a new instance of the application class
                return (Application) targetClass.getConstructor(Context.class)
                        .newInstance(context);
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
        }

        return null;
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
    protected String getInitParameter(String name, String defaultValue) {
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

}
