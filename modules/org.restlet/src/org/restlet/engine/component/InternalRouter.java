/**
 * Copyright 2005-2011 Noelios Technologies.
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

package org.restlet.engine.component;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.resource.Finder;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

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

    @SuppressWarnings("deprecation")
    @Override
    protected org.restlet.routing.Route createRoute(String uriPattern,
            Restlet target, int matchingMode) {
        org.restlet.routing.Route result = new org.restlet.routing.Route(this,
                uriPattern, target) {
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

    @SuppressWarnings("deprecation")
    @Override
    public org.restlet.routing.Route attach(Restlet target) {
        if (target.getContext() == null) {
            target.setContext(getContext().createChildContext());
        }

        return super.attach(target);
    }

    @Override
    @SuppressWarnings("deprecation")
    public org.restlet.routing.Route attach(String uriPattern, Restlet target) {
        if (target.getContext() == null) {
            target.setContext(getContext().createChildContext());
        }

        return super.attach(uriPattern, target);
    }

    @Override
    @SuppressWarnings("deprecation")
    public org.restlet.routing.Route attachDefault(Restlet defaultTarget) {
        if (defaultTarget.getContext() == null) {
            defaultTarget.setContext(getContext().createChildContext());
        }

        return super.attachDefault(defaultTarget);
    }

    @Override
    public Finder createFinder(Class<?> targetClass) {
        Finder result = super.createFinder(targetClass);
        result.setContext(getContext().createChildContext());
        return result;
    }

}
