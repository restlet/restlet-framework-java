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
        for (Enumeration enum1 = servletConfig.getInitParameterNames(); enum1
                .hasMoreElements();) {
            initParam = (String) enum1.nextElement();
            getParameters().add(initParam,
                    servletConfig.getInitParameter(initParam));
        }

        // Copy all the Web Application initialization parameters
        for (Enumeration enum1 = getServletContext().getInitParameterNames(); enum1
                .hasMoreElements();) {
            initParam = (String) enum1.nextElement();
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
