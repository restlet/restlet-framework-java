/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
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
 * This class provides an implementation of {@link ApplicationProvider}. You
 * register this class as an OSGi declarative service. The service declaration
 * should look like:
 * <p>
 * 
 * <pre>
 * {@code
 * <?xml version="1.0" encoding="UTF-8"?>
 * <scr:component xmlns:scr="http://www.osgi.org/xmlns/scr/v1.1.0" immediate="true" name="org.example.app">
 *   <implementation class="org.restlet.ext.osgi.BaseApplicationProvider"/>
 *   <property name="alias" type="String" value="/"/>
 *   <reference bind="bindRouterProvider" cardinality="1..1" interface="org.restlet.ext.osgi.RouterProvider" name="RouterProvider" policy="static" unbind="unbindRouterProvider"/>
 *   <service>
 *     <provide interface="org.restlet.ext.osgi.ApplicationProvider"/>
 *   </service>
 * </scr:component>
 * }
 * </pre>
 * 
 * </p>
 * <p>
 * The service properties are:
 * <ul>
 * <li>alias - the application alias registered with the http service</li>
 * </ul>
 * </p>
 * <p>
 * The referenced services are:
 * <ul>
 * <li>RouterProvider - policy="static" cardinality="1..1"</li>
 * </ul>
 * </p>
 * <p>
 * The provided services are:
 * <ul>
 * <li>ApplicationProvider</li>
 * </ul>
 * </p>
 * 
 * @author Bryan Hunt
 * 
 */
public class BaseApplicationProvider implements ApplicationProvider {
    private String alias;

    private Application application;

    private RouterProvider routerProvider;

    /**
     * Called by OSGi DS to activate the service after the router provider has
     * been bound
     * 
     * @param context
     *            the OSGi component context
     */
    public void activate(ComponentContext context) {
        @SuppressWarnings("unchecked")
        Dictionary<String, Object> properties = context.getProperties();
        alias = (String) properties.get("alias");
    }

    /**
     * Called by OSGi DS to inject the router provider service
     * 
     * @param routerProvider
     *            the router provider service
     */
    public void bindRouterProvider(RouterProvider routerProvider) {
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

    /**
     * Called to construct the actual application instance. Extenders will
     * generally override this method.
     * 
     * @param context
     *            the Restlet application context
     * @return the newly constructed application instance
     */
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

    /**
     * Accessor for the cached application instance
     * 
     * @return the application
     */
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

    /**
     * Called by OSGi DS to un-inject the router provider service
     * 
     * @param routerProvider
     *            the router provider service
     */
    public void unbindRouterProvider(RouterProvider routerProvider) {
        if (this.routerProvider == routerProvider)
            this.routerProvider = null;
    }
}
