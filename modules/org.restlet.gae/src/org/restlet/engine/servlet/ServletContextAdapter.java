/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.servlet;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.restlet.Context;

/**
 * Exposes a Servlet context as a Restlet one. Context allowing access to the
 * component's connectors, reusing the Servlet's logging mechanism and adding
 * the Servlet's initialization parameters to the context's parameters.
 * 
 * @author Jerome Louvel
 */
public class ServletContextAdapter extends Context {
    /** The Servlet context. */
    private volatile ServletContext servletContext;

    /**
     * Constructor.
     * 
     * @param servlet
     *            The parent Servlet.
     * @param parentContext
     *            The parent context.
     */
    public ServletContextAdapter(Servlet servlet, Context parentContext) {
        super(new ServletLogger(servlet.getServletConfig().getServletContext()));
        this.servletContext = servlet.getServletConfig().getServletContext();
        setClientDispatcher((parentContext != null) ? parentContext
                .getClientDispatcher() : null);
        setServerDispatcher((parentContext != null) ? parentContext
                .getServerDispatcher() : null);
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
