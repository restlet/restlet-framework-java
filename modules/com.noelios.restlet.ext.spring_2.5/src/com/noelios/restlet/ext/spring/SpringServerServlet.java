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

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.noelios.restlet.application.ApplicationContext;
import com.noelios.restlet.ext.servlet.ServerServlet;
import com.noelios.restlet.ext.servlet.ServletContextAdapter;

/**
 * This class is similiar to the ServerServlet, but instead of creating the used
 * Application-Restlet and Component-Restlet, it lookups them up from a Spring
 * WebApplicationContext.
 * 
 * @author Florian Schwarz
 */
public class SpringServerServlet extends ServerServlet {

    private static final long serialVersionUID = 110030403435929871L;

    /**
     * Name of the attribute key containing a bean-id of the application to use.
     */
    public static final String APPLICATION_KEY = "org.restlet.application";

    /**
     * Name of the attribute key containing a bean-id of the component to use.
     */
    public static final String COMPONENT_KEY = "org.restlet.component";

    /**
     * Lookups the single Restlet-Application used by this Servlet from the
     * SpringContext inside the ServletContext. The bean name looked up is
     * {@link #APPLICATION_KEY}. If no bean is found,
     * 
     * @param context
     *                The Context for the Application.
     * 
     * @return The Restlet-Application to use or null if unable to lookup or
     *         create.
     */
    @Override
    public Application createApplication(Context context) {
        Application application = null;

        String applicationBeanName = getInitParameter(
                SpringServerServlet.APPLICATION_KEY, null);

        if (getWebApplicationContext().containsBean(applicationBeanName)) {
            application = (Application) getWebApplicationContext().getBean(
                    applicationBeanName);
        }

        if (application != null) {
            // Set the context based on the Servlet's context
            ApplicationContext applicationContext = (ApplicationContext) application
                    .getContext();
            application.setContext(new ApplicationContext(application,
                    new ServletContextAdapter(this, context),
                    applicationContext.getLogger()));
        } else {
            application = super.createApplication(context);
        }

        return application;
    }

    /**
     * Lookups the single RestletComponent used by this Servlet from the
     * SpringContext inside the ServletContext. The bean name looked up is
     * {@link #COMPONENT_KEY}.
     * 
     * @return The Restlet-Component to use or null if unable to lookup or
     *         create.
     */

    @Override
    public Component createComponent() {
        Component component = null;
        String componentBeanName = getInitParameter(
                SpringServerServlet.COMPONENT_KEY, null);

        if (getWebApplicationContext().containsBean(componentBeanName)) {
            component = (Component) getWebApplicationContext().getBean(
                    componentBeanName);
        }

        if (component == null) {
            component = super.createComponent();
        }

        return component;
    }

    public WebApplicationContext getWebApplicationContext() {
        // by hand would be
        // webApplicationContext applicationContext = (WebApplicationContext)
        // getServletContext().getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);

        return WebApplicationContextUtils
                .getRequiredWebApplicationContext(getServletContext());
    }
}
