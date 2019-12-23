/**
 * Copyright 2005-2019 Talend
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * https://restlet.talend.com/
 * 
 * Restlet is a registered trademark of Talend S.A.
 */

package org.restlet.ext.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.ext.osgi.ApplicationProvider;

/**
 * @author Bryan Hunt
 * @author Wolfgang Werner
 */
public class ApplicationServlet extends ServerServlet {
    private static final long serialVersionUID = 5252087180467260130L;

    private transient ApplicationProvider applicationProvider;

    private ServletConfig servletConfig;

    public ApplicationServlet(ApplicationProvider applicationProvider) {
        this.applicationProvider = applicationProvider;
    }

    @Override
    protected Application createApplication(Context context) {
        Context childContext = context.createChildContext();
        childContext.getAttributes().put(
                ApplicationProvider.SERVLET_CONFIG_ATTRIBUTE, servletConfig);
        childContext.getAttributes().put(
                ApplicationProvider.SERVLET_CONTEXT_ATTRIBUTE,
                getServletContext());
        return applicationProvider.createApplication(childContext);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        servletConfig = config;
        super.init(config);
    }
}
