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

import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpContext;
import org.restlet.Application;
import org.restlet.Context;

/**
 * @author Bryan Hunt
 * 
 */
public class ApplicationProvider implements IApplicationProvider {
    private String alias;

    private Application application;

    private IRouterProvider routerProvider;

    protected void activate(ComponentContext context) {
        @SuppressWarnings("unchecked")
        Dictionary<String, Object> properties = context.getProperties();
        alias = (String) properties.get("alias");
    }

    public void bindRouterProvider(IRouterProvider routerProvider) {
        this.routerProvider = routerProvider;

        if (application != null)
            application.setInboundRoot(routerProvider
                    .getInboundRoot(application.getContext()));
    }

    @Override
    public Application createApplication(Context context) {
        application = doCreateApplication(context);

        if (routerProvider != null)
            application.setInboundRoot(routerProvider.getInboundRoot(context));

        return application;
    }

    protected Application doCreateApplication(Context context) {
        // FIXME Workaround for a bug in Restlet 2.1M7 - the context should be
        // passed to the Application
        // constructor.

        Application app = new Application();
        app.setContext(context);
        return app;
    }

    @Override
    public String getAlias() {
        return alias;
    }

    public Application getApplication() {
        return application;
    }

    @Override
    public HttpContext getContext() {
        return null;
    }

    @Override
    public Dictionary<String, Object> getInitParms() {
        return null;
    }

    public void unbindRouterProvider(IRouterProvider routerProvider) {
        if (this.routerProvider == routerProvider)
            this.routerProvider = null;
    }
}
