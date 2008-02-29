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

package com.noelios.restlet.ext.spring;

import java.util.Enumeration;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.noelios.restlet.application.ApplicationContext;
import com.noelios.restlet.ext.servlet.ServerServlet;
import com.noelios.restlet.ext.servlet.ServletContextAdapter;
import com.noelios.restlet.ext.servlet.ServletWarClient;

/**
 * This class is similar to the ServerServlet, but instead of creating the used
 * Restlet Application and Restlet Component, it lookups them up from the
 * SpringContext which is found in the ServletContext.
 * 
 * If the Application or Component beans can't be found, the default behavior of
 * the parent class is used.
 * 
 * @author Florian Schwarz
 */
public class SpringServerServlet extends ServerServlet {

    /**
     * Name of the Servlet parameter containing a bean-id of the application to
     * use.
     */
    public static final String APPLICATION_BEAN_PARAM_NAME = "org.restlet.application";

    /**
     * Name of the Servlet parameter containing a bean-id of the component to
     * use.
     */
    public static final String Component_BEAN_PARAM_NAME = "org.restlet.component";

    private static final long serialVersionUID = 110030403435929871L;

    /**
     * Lookups the single Restlet Application used by this Servlet from the
     * SpringContext inside the ServletContext. The bean name looked up is
     * {@link #APPLICATION_BEAN_PARAM_NAME}.
     * 
     * @param context
     *                The Context for the Application.
     * @return The Restlet-Application to use.
     */
    @SuppressWarnings("unchecked")
    @Override
    public Application createApplication(Context context) {
        Application application = null;

        String applicationBeanName = getInitParameter(
                SpringServerServlet.APPLICATION_BEAN_PARAM_NAME, null);
        application = (Application) getWebApplicationContext().getBean(
                applicationBeanName);

        if (application != null) {
            // Set the context based on the Servlet's context
            ApplicationContext applicationContext = (ApplicationContext) application
                    .getContext();
            application.setContext(new ApplicationContext(application,
                    new ServletContextAdapter(this, context),
                    applicationContext.getLogger()));

            // Set the special WAR client
            applicationContext.setWarClient(new ServletWarClient(
                    applicationContext, this.getServletConfig()
                            .getServletContext()));

            // Copy all the servlet parameters into the context
            String initParam;

            // Copy all the Web component initialization parameters
            javax.servlet.ServletConfig servletConfig = getServletConfig();
            for (Enumeration<String> enum1 = servletConfig
                    .getInitParameterNames(); enum1.hasMoreElements();) {
                initParam = enum1.nextElement();
                applicationContext.getParameters().add(initParam,
                        servletConfig.getInitParameter(initParam));
            }

            // Copy all the Web Application initialization parameters
            for (Enumeration<String> enum1 = getServletContext()
                    .getInitParameterNames(); enum1.hasMoreElements();) {
                initParam = enum1.nextElement();
                applicationContext.getParameters().add(initParam,
                        getServletContext().getInitParameter(initParam));
            }

        } else {
            application = super.createApplication(context);
        }

        return application;
    }

    /**
     * Lookups the single Restlet Component used by this Servlet from Spring's
     * Context available inside the ServletContext. The bean name looked up is
     * {@link #Component_BEAN_PARAM_NAME}.
     * 
     * @return The Restlet-Component to use.
     */
    @Override
    public Component createComponent() {
        Component component = null;
        String componentBeanName = getInitParameter(Component_BEAN_PARAM_NAME,
                null);
        component = (Component) getWebApplicationContext().getBean(
                componentBeanName);

        if (component == null) {
            component = super.createComponent();
        }

        return component;
    }

    /**
     * Get the Spring WebApplicationContext from the ServletContext. (by hand
     * would be webApplicationContext applicationContext =
     * (WebApplicationContext)
     * getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);)
     * 
     * @return The Spring WebApplicationContext.
     */
    public WebApplicationContext getWebApplicationContext() {
        return WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
    }

}
