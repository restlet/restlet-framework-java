/**
 * Copyright 2005-2013 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.ext.osgi.IApplicationProvider;

/**
 * @author Bryan Hunt
 * @author Wolfgang Werner
 */
public class ApplicationServlet extends ServerServlet {
    public ApplicationServlet(IApplicationProvider applicationProvider) {
        this.applicationProvider = applicationProvider;
    }

    @Override
    protected Application createApplication(Context context) {
        Context childContext = context.createChildContext();
        childContext.getAttributes().put(
                IApplicationProvider.SERVLET_CONFIG_ATTRIBUTE, servletConfig);
        childContext.getAttributes().put(
                IApplicationProvider.SERVLET_CONTEXT_ATTRIBUTE,
                getServletContext());
        return applicationProvider.createApplication(childContext);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        servletConfig = config;
        super.init(config);
    }

    private static final long serialVersionUID = 5252087180467260130L;

    private transient IApplicationProvider applicationProvider;

    private ServletConfig servletConfig;
}
