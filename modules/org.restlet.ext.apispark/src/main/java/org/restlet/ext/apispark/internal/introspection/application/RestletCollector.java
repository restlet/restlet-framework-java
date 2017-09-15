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

package org.restlet.ext.apispark.internal.introspection.application;

import java.util.List;
import java.util.logging.Logger;

import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.ext.apispark.internal.introspection.IntrospectionHelper;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.security.ChallengeAuthenticator;

/**
 * @author Manuel Boillod
 */
public class RestletCollector {

    /** Internal logger. */
    private static Logger LOGGER = Logger.getLogger(RestletCollector.class
            .getName());

    public static void collect(CollectInfo collectInfo, String basePath,
            Restlet restlet, ChallengeScheme scheme,
            List<? extends IntrospectionHelper> introspectorHelpers) {
        if (restlet instanceof Router) {
            collectForRouter(collectInfo, basePath, (Router) restlet, scheme,
                    introspectorHelpers);
        } else if (restlet instanceof Route) {
            collectForRoute(collectInfo, basePath, (Route) restlet, scheme,
                    introspectorHelpers);
        } else if (restlet instanceof Filter) {
            collectForFilter(collectInfo, basePath, (Filter) restlet, scheme,
                    introspectorHelpers);
        } else if (restlet instanceof Finder) {
            collectForFinder(collectInfo, basePath, (Finder) restlet, scheme,
                    introspectorHelpers);
        } else {
            LOGGER.fine("Restlet type ignored. Class " + restlet.getClass());
        }
    }

    private static void collectForFilter(CollectInfo collectInfo,
            String basePath, Filter filter, ChallengeScheme scheme,
            List<? extends IntrospectionHelper> introspectionHelper) {

        if (filter instanceof ChallengeAuthenticator) {
            scheme = ((ChallengeAuthenticator) filter).getScheme();
            collectInfo.addSchemeIfNotExists(scheme);
        }

        collect(collectInfo, basePath, filter.getNext(), scheme,
                introspectionHelper);
    }

    /**
     * Completes the data available about a given Finder instance.
     * 
     * @param finder
     *            The Finder instance to document.
     * @param introspectionHelper
     */
    private static void collectForFinder(CollectInfo collectInfo,
            String basePath, Finder finder, ChallengeScheme scheme,
            List<? extends IntrospectionHelper> introspectionHelper) {
        if (finder instanceof Directory) {
            ResourceCollector.collectResource(collectInfo,
                    (Directory) finder, basePath, scheme, introspectionHelper);
        } else {
            ServerResource serverResource = finder.find(null, null);
            if (serverResource != null) {
                ResourceCollector.collectResource(
                        collectInfo, serverResource, basePath, scheme,
                        introspectionHelper);
            } else {
                LOGGER.fine("Finder has no server resource. Class "
                        + finder.getClass());
            }
        }
    }

    private static void collectForRoute(CollectInfo collectInfo,
            String basePath, Route route, ChallengeScheme scheme,
            List<? extends IntrospectionHelper> introspectionHelper) {
        if (route instanceof TemplateRoute) {
            TemplateRoute templateRoute = (TemplateRoute) route;
            String path = templateRoute.getTemplate().getPattern();
            collect(collectInfo, basePath + path, route.getNext(), scheme,
                    introspectionHelper);
        } else {
            LOGGER.fine("Route type ignored. Class " + route.getClass());
        }
    }

    /**
     * Completes the list of ResourceInfo instances for the given Router
     * instance.
     * 
     * 
     * @param router
     *            The router to document.
     * @param introspectionHelper
     */
    private static void collectForRouter(CollectInfo collectInfo,
            String basePath, Router router, ChallengeScheme scheme,
            List<? extends IntrospectionHelper> introspectionHelper) {
        for (Route route : router.getRoutes()) {
            collectForRoute(collectInfo, basePath, route, scheme,
                    introspectionHelper);
        }

        if (router.getDefaultRoute() != null) {
            collectForRoute(collectInfo, basePath, router.getDefaultRoute(),
                    scheme, introspectionHelper);
        }
    }
}
