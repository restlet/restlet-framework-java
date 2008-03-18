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
package org.restlet.ext.jaxrs;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.ApplicationConfig;

import org.restlet.Application;
import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.Finder;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.ext.jaxrs.internal.todo.NotYetImplementedException;

/**
 * @author Stephan Koops
 */
public class JaxRsApplication extends Application {

    /** The {@link JaxRsRouter} to use. */
    private JaxRsRouter jaxRsRouter;

    /**
     * Default constructor.
     * 
     * @see JaxRsRouter#JaxRsRouter(Context)
     * @param parentContext
     *                The parent component context.
     */
    public JaxRsApplication(Context parentContext) {
        super(parentContext);
        this.jaxRsRouter = new JaxRsRouter(parentContext);
    }

    /**
     * attaches the {@link ApplicationConfig} to this Application.
     * 
     * @param appConfig
     * @see JaxRsRouter#attach(ApplicationConfig)
     */
    public void attach(ApplicationConfig appConfig) {
        this.jaxRsRouter.attach(appConfig);
    }

    @Override
    public Restlet createRoot() {
        // some browser request XML with higher quality than HTML.
        // If you want to change the quality, use this HtmlPreferer
        // filter. If you do not need it, you can directly return the
        // router.
        HtmlPreferer filter = new HtmlPreferer(getContext(), jaxRsRouter);
        return filter;
    }

    /**
     * @return accessControl
     * @see JaxRsRouter#getAccessControl()
     */
    public AccessControl getAccessControl() {
        return this.jaxRsRouter.getAccessControl();
    }

    /**
     * Returns an unmodifiable set with the attached root resource classes.
     * 
     * @return an unmodifiable set with the attached root resource classes.
     * @see JaxRsRouter#getRootResourceClasses()
     */
    public Collection<Class<?>> getRootResources() {
        return this.jaxRsRouter.getRootResourceClasses();
    }

    /**
     * Returns an unmodifiable set of supported URIs (relative).
     * 
     * @return an unmodifiable set of supported URIs (relative).
     * @see JaxRsRouter#getRootResourceClasses()
     */
    public Collection<String> getRootUris() {
        return this.jaxRsRouter.getRootUris();
    }
    
    /**
     * This method (should, if ready implemented) return {@link Route} to attach
     * to a {@link Router}.<br>
     * The {@link JaxRsRouter} does not allow other Restlets directly between
     * it. Example: {@link JaxRsRouter} handles http://host/path1. So you can't
     * directly add another Restlet handling http://host/path2. When addings
     * this {@link Route}s to the main {@link Router} for "host" you can add
     * another {@link Restlet} (e.g. a {@link Directory} or {@link Finder}) for
     * other pathes.
     * 
     * @return an unmodifiable {@link List} of {@link Route}s.
     */
    public List<Route> getRoutes() {
        throw new NotYetImplementedException();
    }
    
    /**
     * @param accessControl
     * @see JaxRsRouter#setAccessControl(AccessControl)
     */
    public void setAccessControl(AccessControl accessControl) {
        this.jaxRsRouter.setAccessControl(accessControl);
    }
}