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

package org.restlet.engine.component;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.restlet.routing.TemplateRoute;

/**
 * Provides the behavior of the internal router of a Component. It overrides the
 * default behavior of a classic Router.
 * 
 * @author Thierry Boileau
 */
public class InternalRouter extends Router {

    /**
     * Constructor.
     * 
     * @param context
     */
    public InternalRouter(Context context) {
        super(context);
        // Override Router's default modes
        setDefaultMatchingMode(Template.MODE_STARTS_WITH);
        setRoutingMode(Router.MODE_BEST_MATCH);
    }

    @Override
    protected TemplateRoute createRoute(String uriPattern, Restlet target,
            int matchingMode) {
        TemplateRoute result = new TemplateRoute(this, uriPattern, target) {
            @Override
            protected int beforeHandle(Request request, Response response) {
                final int result = super.beforeHandle(request, response);

                // Set the request's root reference in order to help the
                // retrieval of the relative reference.
                request.setRootRef(request.getResourceRef().getBaseRef());

                return result;
            }
        };

        result.getTemplate().setMatchingMode(matchingMode);
        result.setMatchingQuery(getDefaultMatchingQuery());
        return result;
    }

    @Override
    public TemplateRoute attach(Restlet target) {
        if (target.getContext() == null) {
            target.setContext(getContext().createChildContext());
        }

        return super.attach(target);
    }

    @Override
    public TemplateRoute attach(String uriPattern, Restlet target) {
        if (target.getContext() == null) {
            target.setContext(getContext().createChildContext());
        }

        return super.attach(uriPattern, target);
    }

    @Override
    public TemplateRoute attachDefault(Restlet defaultTarget) {
        if (defaultTarget.getContext() == null) {
            defaultTarget.setContext(getContext().createChildContext());
        }

        return super.attachDefault(defaultTarget);
    }

    @Override
    public Finder createFinder(Class<? extends ServerResource> targetClass) {
        Finder result = super.createFinder(targetClass);
        result.setContext(getContext().createChildContext());
        return result;
    }

}
