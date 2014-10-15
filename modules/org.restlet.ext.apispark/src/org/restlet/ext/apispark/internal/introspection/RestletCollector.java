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

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by manu on 12/10/2014.
 */
public class RestletCollector {

    /** Internal logger. */
    private static Logger LOGGER = Logger.getLogger(RestletCollector.class
            .getName());
            
    public static void collect(CollectInfo collectInfo, String basePath,
                               Restlet restlet, ChallengeScheme scheme,
                               List<IntrospectorPlugin> introspectorPlugins) {
        if (restlet instanceof Router) {
            collectForRouter(collectInfo, basePath, (Router) restlet, scheme, introspectorPlugins);
        } else if (restlet instanceof Route) {
            collectForRoute(collectInfo, basePath, (Route) restlet, scheme, introspectorPlugins);
        } else if (restlet instanceof Filter) {
            collectForFilter(collectInfo, basePath, (Filter) restlet, scheme, introspectorPlugins);
        } else if (restlet instanceof Finder) {
            collectForFinder(collectInfo, basePath, (Finder) restlet, scheme, introspectorPlugins);
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
     * @param introspectorPlugins
     */
    private static void collectForRouter(CollectInfo collectInfo, String basePath,
                                         Router router, ChallengeScheme scheme,
                                         List<IntrospectorPlugin> introspectorPlugins) {
        for (Route route : router.getRoutes()) {
            collectForRoute(collectInfo, basePath, route, scheme, introspectorPlugins);
        }

        if (router.getDefaultRoute() != null) {
            collectForRoute(collectInfo, basePath, router.getDefaultRoute(), scheme, introspectorPlugins);
        }
    }

    private static void collectForRoute(CollectInfo collectInfo,  String basePath,
                                        Route route, ChallengeScheme scheme,
                                        List<IntrospectorPlugin> introspectorPlugins) {
        if (route instanceof TemplateRoute) {
            TemplateRoute templateRoute = (TemplateRoute) route;
            String path = templateRoute.getTemplate().getPattern();
            collect(collectInfo, basePath + path, route.getNext(), scheme, introspectorPlugins);
        } else {
            LOGGER.fine("Route type ignored. Class " + route.getClass());
        }
    }

    private static void collectForFilter(CollectInfo collectInfo, String basePath,
                                         Filter filter, ChallengeScheme scheme,
                                         List<IntrospectorPlugin> introspectorPlugins) {

        if (filter instanceof ChallengeAuthenticator) {
            scheme = ((ChallengeAuthenticator) filter).getScheme();
            collectInfo.addSchemeIfNotExists(scheme);
        }

        collect(collectInfo, basePath, filter.getNext(), scheme, introspectorPlugins);
    }



    /**
     * Completes the data available about a given Finder instance.
     *
     * @param finder
     *            The Finder instance to document.
     * @param introspectorPlugins
     */
    private static void collectForFinder(CollectInfo collectInfo, String basePath,
                                             Finder finder, ChallengeScheme scheme,
                                             List<IntrospectorPlugin> introspectorPlugins) {
        if (finder instanceof Directory) {
            ResourceCollector.collectResourceForDirectory(collectInfo,
                    (Directory) finder, basePath, scheme, introspectorPlugins);
        } else {
            ServerResource serverResource = finder.find(null, null);
            if (serverResource != null) {
                ResourceCollector.collectResourceForServletResource(collectInfo,
                        serverResource, basePath, scheme, introspectorPlugins);
            } else {
                LOGGER.fine("Finder has no server resource. Class " + finder.getClass());
            }
        }
    }
}
