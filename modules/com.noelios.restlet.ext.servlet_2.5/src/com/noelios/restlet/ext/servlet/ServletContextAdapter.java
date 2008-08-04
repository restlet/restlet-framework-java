/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.restlet.Context;
import org.restlet.Uniform;

/**
 * Exposes a Servlet context as a Restlet one. Context allowing access to the
 * component's connectors, reusing the Servlet's logging mechanism and adding
 * the Servlet's initialization parameters to the context's parameters.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ServletContextAdapter extends Context {
    /** The Servlet context. */
    private volatile ServletContext servletContext;

    /** The client dispatcher. */
    private volatile Uniform clientDispatcher;

    /** The server dispatcher. */
    private volatile Uniform serverDispatcher;

    /**
     * Constructor.
     * 
     * @param servlet
     *            The parent Servlet.
     * @deprecated Use this constructor ServletContextAdapter(Servlet, Context)
     *             instead.
     */
    @Deprecated
    public ServletContextAdapter(Servlet servlet) {
        super(new ServletLogger(servlet.getServletConfig().getServletContext()));
        this.servletContext = servlet.getServletConfig().getServletContext();
    }

    /**
     * Constructor.
     * 
     * @param servlet
     *            The parent Servlet.
     * @param parentContext
     *            The parent Context.
     */
    public ServletContextAdapter(Servlet servlet, Context parentContext) {
        super(new ServletLogger(servlet.getServletConfig().getServletContext()));
        this.servletContext = servlet.getServletConfig().getServletContext();
        this.clientDispatcher = (parentContext != null) ? parentContext
                .getClientDispatcher() : null;
        this.serverDispatcher = (parentContext != null) ? parentContext
                .getServerDispatcher() : null;
    }

    @Override
    public Uniform getClientDispatcher() {
        return this.clientDispatcher;
    }

    @Override
    public Uniform getServerDispatcher() {
        return this.serverDispatcher;
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
