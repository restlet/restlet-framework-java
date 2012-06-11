/**
 * Copyright 2005-2012 Restlet S.A.S.
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

package org.restlet.ext.osgi;

import java.util.Dictionary;

import org.osgi.service.http.HttpContext;
import org.restlet.Application;
import org.restlet.Context;

/**
 * This is an OSGi service interface for registering Restlet applications with a
 * server servlet. Users are expected to register an instance as an OSGi
 * service. You may use the
 * {@link org.eclipselabs.restlet.impl.ApplicationProvider} directly, extend it,
 * or provide your own implementation of {@link IApplicationProvider}. A server
 * servlet will be created and registered with the web container at the
 * specified alias. The application will then be registered with the servlet.
 * 
 * @author Bryan Hunt
 * @author Wolfgang Werner
 */
public interface IApplicationProvider {
    String COMPONENT_ATTRIBUTE = "org.restlet.ext.servlet.ServerServlet.component.org.eclipselabs.restlet.servlet.ApplicationServlet";

    String SERVLET_CONFIG_ATTRIBUTE = "javax.servlet.ServletConfig";

    String SERVLET_CONTEXT_ATTRIBUTE = "org.restlet.ext.servlet.ServletContext";

    /**
     * 
     * @return the application to be register at the specified alias.
     */
    Application createApplication(Context context);

    String getAlias();

    /**
     * The context is passed to
     * {@link org.osgi.service.http.HttpService#registerServlet(String alias, Servlet servlet, Dictionary initparams, HttpContext context)}
     * when the servlet is registered.
     * 
     * @return the context to use with the server servlet.
     */
    HttpContext getContext();

    /**
     * The parameters are passed to
     * {@link org.osgi.service.http.HttpService#registerServlet(String alias, Servlet servlet, Dictionary initparams, HttpContext context)}
     * when the servlet is registered.
     * 
     * @return the initialization parameters to use with the server servlet.
     */
    Dictionary<String, Object> getInitParms();
}
