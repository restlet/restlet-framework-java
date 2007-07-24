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

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.restlet.Context;

/**
 * Context allowing access to the component's connectors, reusing the Servlet's
 * logging mechanism and adding the Servlet's initialization parameters to the
 * context's parameters.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ServletContextAdapter extends Context {
    /** The Servlet context. */
    private ServletContext servletContext;

    /**
     * Constructor.
     * 
     * @param servlet
     *                The parent Servlet.
     */
    public ServletContextAdapter(Servlet servlet) {
        super(new ServletLogger(servlet.getServletConfig().getServletContext()));
        this.servletContext = servlet.getServletConfig().getServletContext();
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
