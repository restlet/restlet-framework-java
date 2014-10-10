/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.apispark;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.Reference;
import org.restlet.ext.apispark.internal.conversion.DefinitionTranslator;
import org.restlet.ext.apispark.internal.info.ApplicationInfo;
import org.restlet.ext.apispark.internal.info.ResourceInfo;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.utils.IntrospectionUtils;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.*;
import org.restlet.security.ChallengeAuthenticator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Publish the documentation of a Restlet-based Application to the APISpark
 * console.
 * 
 * @author Thierry Boileau
 */
public class ApplicationIntrospector extends IntrospectionUtils {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(ApplicationIntrospector.class
            .getName());

    /**
     * Returns an instance of what must be a subclass of {@link org.restlet.Application}.
     * Returns null in case of errors.
     *
     * @param className
     *            The name of the application class.
     * @return An instance of what must be a subclass of {@link org.restlet.Application}.
     */
    public static Application getApplication(String className) {
        if (className == null) {
            return null;
        }

        try {
            Class<?> clazz = Class.forName(className);
            if (Application.class.isAssignableFrom(clazz)) {
                return (Application) clazz.getConstructor().newInstance();
            } else {
                throw new RuntimeException(className
                        + " does not seem to be a valid subclass of "
                        + Application.class.getName() + " class.");
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Cannot locate the definition source.", e);
        } catch (Exception e) {
            throw new RuntimeException("Cannot instantiate the application class. " +
                    "Check that the application class has an empty constructor.", e);
        }
    }

    /**
     * Returns a APISpark description of the current application. By default,
     * this method discovers all the resources attached to this application. It
     * can be overridden to add documentation, list of representations, etc.
     *
     * @return An application description.
     */
    protected static ApplicationInfo getApplicationInfo(
            Application application, Reference baseRef) {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.getResources().setBaseRef(baseRef);
        applicationInfo.setName(application.getName());
        applicationInfo.setDescription(application.getDescription());
        applicationInfo.getResources()
                .setResources(
                        getResourceInfos(applicationInfo,
                                getNextRouter(application.getInboundRoot()),
                                "/", null));

        return applicationInfo;
    }

    /**
     * Returns the next router available.
     *
     * @param current
     *            The current Restlet to inspect.
     * @return The first router available.
     */
    private static Router getNextRouter(Restlet current) {
        Router result = null;
        if (current instanceof Router) {
            result = (Router) current;
        } else if (current instanceof Filter) {
            result = getNextRouter(((Filter) current).getNext());
        }

        return result;
    }



    /**
     * Completes the data available about a given Filter instance.
     *
     * @param applicationInfo
     *            The parent application.
     * @param filter
     *            The Filter instance to document.
     * @param path
     *            The base path.
     * @return The resource description.
     */
    private static ResourceInfo getResourceInfo(
            ApplicationInfo applicationInfo, Filter filter, String path,
            ChallengeScheme scheme) {
        if (filter instanceof ChallengeAuthenticator) {
            scheme = ((ChallengeAuthenticator) filter).getScheme();
            applicationInfo.setAuthenticationProtocol(scheme);
        }
        return getResourceInfo(applicationInfo, filter.getNext(), path, scheme);
    }

    /**
     * Completes the data available about a given Finder instance.
     *
     * @param applicationInfo
     *            The parent application.
     * @param finder
     *            The Finder instance to document.
     */
    private static ResourceInfo getResourceInfo(
            ApplicationInfo applicationInfo, Finder finder, String path,
            ChallengeScheme scheme) {
        ResourceInfo result = null;
        Object resource = null;

        if (finder instanceof Directory) {
            resource = finder;
        } else {
            ServerResource sr = finder.find(null, null);

            if (sr != null) {
                // The handler instance targeted by this finder.
                Request request = new Request();
                org.restlet.Response response = new org.restlet.Response(
                        request);
                sr.setRequest(request);
                sr.setResponse(response);
                sr.updateAllowedMethods();
                resource = sr;
            }
        }

        if (resource != null) {
            result = new ResourceInfo();
            ResourceInfo.describe(applicationInfo, result, resource, path);
            if (scheme != null) {
                result.setAuthenticationProtocol(scheme.getName());
            }
        }

        return result;
    }

    /**
     * Completes the data available about a given Restlet instance.
     *
     * @param applicationInfo
     *            The parent application.
     * @param restlet
     *            The Restlet instance to document..
     */
    private static ResourceInfo getResourceInfo(
            ApplicationInfo applicationInfo, Restlet restlet, String path,
            ChallengeScheme scheme) {
        ResourceInfo result = null;

        if (restlet instanceof Finder) {
            result = getResourceInfo(applicationInfo, (Finder) restlet, path,
                    scheme);
        } else if (restlet instanceof Router) {
            result = new ResourceInfo();
            result.setPath(path);
            result.setChildResources(getResourceInfos(applicationInfo,
                    (Router) restlet, path, scheme));
        } else if (restlet instanceof Filter) {
            result = getResourceInfo(applicationInfo, (Filter) restlet, path,
                    scheme);
        }

        return result;
    }

    /**
     * Returns the APISpark data about the given Route instance.
     *
     * @param applicationInfo
     *            The parent application.
     * @param route
     *            The Route instance to document.
     * @param basePath
     *            The base path.
     * @return The APISpark data about the given Route instance.
     */
    private static ResourceInfo getResourceInfo(
            ApplicationInfo applicationInfo, Route route, String basePath,
            ChallengeScheme scheme) {
        ResourceInfo result = null;

        if (route instanceof TemplateRoute) {
            TemplateRoute templateRoute = (TemplateRoute) route;
            String path = templateRoute.getTemplate().getPattern();

            // APISpark requires resource paths to be relative to parent path
            if (path.startsWith("/") && basePath.endsWith("/")) {
                path = basePath + path.substring(1);
            } else {
                path = basePath + path;
            }

            result = getResourceInfo(applicationInfo, route.getNext(), path,
                    scheme);
        }

        return result;
    }

    /**
     * Completes the list of ResourceInfo instances for the given Router
     * instance.
     *
     * @param applicationInfo
     *            The parent application.
     * @param router
     *            The router to document.
     * @param path
     *            The base path.
     * @return The list of ResourceInfo instances to complete.
     */
    private static List<ResourceInfo> getResourceInfos(
            ApplicationInfo applicationInfo, Router router, String path,
            ChallengeScheme scheme) {
        List<ResourceInfo> result = new ArrayList<ResourceInfo>();

        if (router != null) {
            for (Route route : router.getRoutes()) {
                ResourceInfo resourceInfo = getResourceInfo(applicationInfo,
                        route, path, scheme);

                if (resourceInfo != null) {
                    result.add(resourceInfo);
                }
            }

            if (router.getDefaultRoute() != null) {
                ResourceInfo resourceInfo = getResourceInfo(applicationInfo,
                        router.getDefaultRoute(), path, scheme);
                if (resourceInfo != null) {
                    result.add(resourceInfo);
                }
            }
        }

        return result;
    }


    /** The current Web API definition. */
    private Definition definition;

    /**
     * Constructor.
     *
     * @param application
     *            An application to introspect.
     */
    public ApplicationIntrospector(Application application) {
        this(null, application);
    }

    /**
     * Constructor.
     *
     * @param component
     *            An component to introspect in order to get extra details such
     *            as the endpoint.
     * @param application
     *            An application to introspect.
     */
    public ApplicationIntrospector(Component component, Application application) {
        ApplicationInfo applicationInfo = getApplicationInfo(application, null);
        definition = DefinitionTranslator.toDefinition(
                applicationInfo);

        if (component != null && definition != null) {
            LOGGER.fine("Look for the endpoint.");
            // TODO What if the application is attached to several endpoints?
            // Look for the endpoint to which this application is attached.
            Endpoint endpoint = ComponentIntrospector.getEndpoint(component.getDefaultHost(), application);
            if (endpoint != null) {
                definition.getEndpoints().add(endpoint);
            }
            for (VirtualHost virtualHost : component.getHosts()) {
                endpoint = ComponentIntrospector.getEndpoint(virtualHost, application);
                if (endpoint != null) {
                    definition.getEndpoints().add(endpoint);
                }
            }
        }
    }

    /**
     * Returns the current definition.
     * 
     * @return The current definition.
     */
    public Definition getDefinition() {
        return definition;
    }

}
