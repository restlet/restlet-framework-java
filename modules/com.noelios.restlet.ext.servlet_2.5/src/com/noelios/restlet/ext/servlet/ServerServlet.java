/*
 * Copyright 2005-2008 Noelios Consulting.
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
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.util.Engine;

import com.noelios.restlet.application.ApplicationContext;
import com.noelios.restlet.component.ComponentContext;
import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.http.HttpServerHelper;

/**
 * Servlet acting like an HTTP server connector. See <a
 * href="/documentation/1.1/faq#02">Developper FAQ #2</a> for details on how to
 * integrate a Restlet application into a servlet container.<br>
 * Here is a sample configuration for your Restlet webapp:
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
 * them with existing Servlets.<br>
 * <br>
 * It is also possible to specify a component class to be instantiated instead
 * of a default component. You just need to add a "org.restlet.component"
 * context parameter to your ServerServlet, with the qualified class name to
 * instantiate as value. Once instantiated, a server connector will be added to
 * this component and the application specified via the other context parameter
 * will be normally attached to its default virtual host. This allows you to
 * manually attach private applications to its internal router or to declare
 * client connectors, for example for the CLAP, FILE or HTTP protocols.
 * 
 * @see <a href="http://java.sun.com/j2ee/">J2EE home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ServerServlet extends HttpServlet {
    /**
     * Name of the attribute key containing a reference to the current
     * application.
     */
    private static final String APPLICATION_KEY = "org.restlet.application";

    /**
     * Name of the attribute key containing a reference to the current
     * component.
     */
    private static final String COMPONENT_KEY = "org.restlet.component";

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
    private volatile transient Application application;

    /** The associated Restlet component. */
    private volatile transient Component component;

    /** The associated HTTP server helper. */
    private volatile transient HttpServerHelper helper;

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
     *                The Context for the Application
     * 
     * @return The newly created Application or null if unable to create
     */
    @SuppressWarnings("unchecked")
    protected Application createApplication(Context context) {
        Application application = null;

        // Try to instantiate a new target application
        // First, find the application class name
        String applicationClassName = getInitParameter(APPLICATION_KEY, null);

        // Load the application class using the given class name
        if (applicationClassName != null) {
            try {
                Class<?> targetClass = getClass(applicationClassName);

                try {
                    // Create a new instance of the application class by
                    // invoking the constructor with the Context parameter.
                    application = (Application) targetClass.getConstructor(
                            Context.class).newInstance(
                            new ServletContextAdapter(this, context));
                } catch (NoSuchMethodException e) {
                    log(
                            "[Noelios Restlet Engine] - The ServerServlet couldn't invoke the constructor of the target class. Please check this class has a constructor with a single parameter of type Context. The empty constructor and the context setter will be used instead.",
                            e);
                    // The constructor with the Context parameter does not
                    // exist. Instantiate an application with the default
                    // constructor then invoke the setContext method.
                    application = (Application) targetClass.getConstructor()
                            .newInstance();

                    // Set the context based on the Servlet's context
                    ApplicationContext applicationContext = (ApplicationContext) application
                            .getContext();
                    application.setContext(new ApplicationContext(application,
                            new ServletContextAdapter(this, context),
                            applicationContext.getLogger()));
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
        }

        if (application != null) {
            ApplicationContext applicationContext = (ApplicationContext) application
                    .getContext();

            // Copy all the servlet parameters into the context
            String initParam;

            // Copy all the Servlet component initialization parameters
            javax.servlet.ServletConfig servletConfig = getServletConfig();
            for (Enumeration<String> enum1 = servletConfig
                    .getInitParameterNames(); enum1.hasMoreElements();) {
                initParam = enum1.nextElement();
                applicationContext.getParameters().add(initParam,
                        servletConfig.getInitParameter(initParam));
            }

            // Copy all the Servlet application initialization parameters
            for (Enumeration<String> enum1 = getServletContext()
                    .getInitParameterNames(); enum1.hasMoreElements();) {
                initParam = enum1.nextElement();
                applicationContext.getParameters().add(initParam,
                        getServletContext().getInitParameter(initParam));
            }
        }

        return application;
    }

    /**
     * Creates a new Servlet call wrapping a Servlet request/response couple and
     * a Server connector.
     * 
     * @param server
     *                The Server connector.
     * @param request
     *                The Servlet request.
     * @param response
     *                The Servlet response.
     * @return The new ServletCall instance.
     */
    protected HttpServerCall createCall(Server server,
            HttpServletRequest request, HttpServletResponse response) {
        return new ServletCall(server, request, response);
    }

    /**
     * Creates a new client for the WAR protocol.
     * 
     * @param context
     *                The parent context.
     * @param config
     *                The Servlet config.
     * @return The new WAR client instance.
     */
    protected Client createWarClient(Context context, ServletConfig config) {
        return new ServletWarClient(context, config.getServletContext());
    }

    /**
     * Creates the single Component used by this Servlet.
     * 
     * @return The newly created Component or null if unable to create
     */
    @SuppressWarnings("unchecked")
    protected Component createComponent() {
        Component component = null;

        // Try to instantiate a new target component
        // First, find the component class name
        String componentClassName = getInitParameter(COMPONENT_KEY, null);

        // Load the component class using the given class name
        if (componentClassName != null) {
            try {
                Class<?> targetClass = getClass(componentClassName);

                // Create a new instance of the component class by
                // invoking the constructor with the Context parameter.
                component = (Component) targetClass.newInstance();
            } catch (ClassNotFoundException e) {
                log(
                        "[Noelios Restlet Engine] - The ServerServlet couldn't find the target class. Please check that your classpath includes "
                                + componentClassName, e);

            } catch (InstantiationException e) {
                log(
                        "[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the target class. Please check this class has an empty constructor "
                                + componentClassName, e);
            } catch (IllegalAccessException e) {
                log(
                        "[Noelios Restlet Engine] - The ServerServlet couldn't instantiate the target class. Please check that you have to proper access rights to "
                                + componentClassName, e);
            }
        }

        if (component != null) {
            ComponentContext componentContext = (ComponentContext) component
                    .getContext();

            // Set the special WAR client
            // componentContext.setWarClient(new ServletWarClient(
            // componentContext, this.getServletConfig()
            // .getServletContext()));

            // Copy all the servlet parameters into the context
            String initParam;

            // Copy all the Servlet container initialization parameters
            javax.servlet.ServletConfig servletConfig = getServletConfig();
            for (Enumeration<String> enum1 = servletConfig
                    .getInitParameterNames(); enum1.hasMoreElements();) {
                initParam = enum1.nextElement();
                componentContext.getParameters().add(initParam,
                        servletConfig.getInitParameter(initParam));
            }

            // Copy all the Servlet application initialization parameters
            for (Enumeration<String> enum1 = getServletContext()
                    .getInitParameterNames(); enum1.hasMoreElements();) {
                initParam = enum1.nextElement();
                componentContext.getParameters().add(initParam,
                        getServletContext().getInitParameter(initParam));
            }
        } else {
            component = new Component();

            // The status service is disabled by default.
            component.getStatusService().setEnabled(false);

            // Add the WAR client
            component.getClients()
                    .add(
                            createWarClient(component.getContext(),
                                    getServletConfig()));
        }

        return component;
    }

    /**
     * Creates the associated HTTP server handling calls.
     * 
     * @param request
     *                The HTTP Servlet request.
     * @return The new HTTP server handling calls.
     */
    protected HttpServerHelper createServer(HttpServletRequest request) {
        HttpServerHelper result = null;
        Component component = getComponent();
        Application application = getApplication();

        if ((component != null) && (application != null)) {
            // First, let's create a pseudo server
            Server server = new Server(component.getContext(),
                    (List<Protocol>) null, request.getLocalAddr(), request
                            .getLocalPort(), component);
            result = new HttpServerHelper(server);

            // Attach the application
            String uriPattern = request.getContextPath()
                    + request.getServletPath();
            log("[Noelios Restlet Engine] - Attaching application: "
                    + application + " to URI: " + uriPattern);
            component.getDefaultHost().attach(uriPattern, application);
        }

        return result;
    }

    @Override
    public void destroy() {
        if ((getComponent() != null) && (getComponent().isStarted())) {
            try {
                getComponent().stop();
            } catch (Exception e) {
                log("Error during the stopping of the Restlet Component", e);
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
     * Returns a class for a given qualified class name.
     * 
     * @param className
     *                The class name to lookup.
     * @return The class object.
     * @throws ClassNotFoundException
     */
    protected Class<?> getClass(String className) throws ClassNotFoundException {
        return Engine.classForName(className);
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
                    result = createComponent();
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
     *                The parameter name.
     * @param defaultValue
     *                The default to use in case the parameter is not found.
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
     *                The HTTP Servlet request.
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
     *                The HTTP Servlet request.
     * @param response
     *                The HTTP Servlet response.
     */
    @Override
    public void service(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpServerHelper helper = getServer(request);

        if (helper != null) {
            helper.handle(createCall(helper.getHelped(), request, response));
        } else {
            log("[Noelios Restlet Engine] - Unable to get the Restlet HTTP server connector. Status code 500 returned.");
            response.sendError(500);
        }
    }

}
