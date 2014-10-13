package org.restlet.ext.apispark.internal.introspection;

import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.security.ChallengeAuthenticator;

import java.util.logging.Logger;

/**
 * Created by manu on 12/10/2014.
 */
public class RestletCollector {

    /** Internal logger. */
    private static Logger LOGGER = Logger.getLogger(RestletCollector.class
            .getName());
            
    public static void collect(CollectInfo collectInfo, String basePath, Restlet restlet, ChallengeScheme scheme) {
        if (restlet instanceof Router) {
            collectForRouter(collectInfo, basePath, (Router) restlet, scheme);
        } else if (restlet instanceof Route) {
            collectForRoute(collectInfo, basePath, (Route) restlet, scheme);
        } else if (restlet instanceof Filter) {
            collectForFilter(collectInfo, basePath, (Filter) restlet, scheme);
        } else if (restlet instanceof Finder) {
            collectForFinder(collectInfo, basePath, (Finder) restlet, scheme);
        } else {
            LOGGER.fine("Restlet type ignored. Class " + restlet.getClass());
        }
    }

    /**
     * Completes the list of ResourceInfo instances for the given Router
     * instance.
     *
     *
     * @param router
     *            The router to document.
     */
    private static void collectForRouter(CollectInfo collectInfo, String basePath, Router router, ChallengeScheme scheme) {
        for (Route route : router.getRoutes()) {
            collectForRoute(collectInfo, basePath, route, scheme);
        }

        if (router.getDefaultRoute() != null) {
            collectForRoute(collectInfo, basePath, router.getDefaultRoute(), scheme);
        }
    }

    private static void collectForRoute(CollectInfo collectInfo,  String basePath, Route route, ChallengeScheme scheme) {
        if (route instanceof TemplateRoute) {
            TemplateRoute templateRoute = (TemplateRoute) route;
            String path = templateRoute.getTemplate().getPattern();
            collect(collectInfo, basePath + path, route.getNext(), scheme);
        } else {
            LOGGER.fine("Route type ignored. Class " + route.getClass());
        }
    }

    private static void collectForFilter(CollectInfo collectInfo, String basePath, Filter filter, ChallengeScheme scheme) {

        if (filter instanceof ChallengeAuthenticator) {
            scheme = ((ChallengeAuthenticator) filter).getScheme();
            collectInfo.addSchemeIfNotExists(scheme);
        }

        collect(collectInfo, basePath, filter.getNext(), scheme);
    }



    /**
     * Completes the data available about a given Finder instance.
     *
     * @param finder
     *            The Finder instance to document.
     */
    private static void collectForFinder(CollectInfo collectInfo, String basePath,
                                             Finder finder, ChallengeScheme scheme) {
        if (finder instanceof Directory) {
            ResourceCollector.collectResourceForDirectory(collectInfo, (Directory) finder, basePath, scheme);
        } else {
            ServerResource serverResource = finder.find(null, null);
            if (serverResource != null) {
                ResourceCollector.collectResourceForServletResource(collectInfo, serverResource, basePath, scheme);
            } else {
                LOGGER.fine("Finder has no server resource. Class " + finder.getClass());
            }
        }
    }
}
