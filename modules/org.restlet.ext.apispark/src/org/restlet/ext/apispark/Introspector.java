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

import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.engine.Engine;
import org.restlet.ext.apispark.internal.conversion.IntrospectionTranslator;
import org.restlet.ext.apispark.internal.conversion.SwaggerUtils;
import org.restlet.ext.apispark.internal.conversion.TranslationException;
import org.restlet.ext.apispark.internal.info.ApplicationInfo;
import org.restlet.ext.apispark.internal.info.DocumentationInfo;
import org.restlet.ext.apispark.internal.info.ResourceInfo;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.utils.IntrospectionUtils;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.VirtualHost;

/**
 * Extract and push the web API documentation of a Restlet API-based
 * {@link Application} to the APISpark console.
 * 
 * @author Thierry Boileau
 */
public class Introspector {

    /** Internal logger. */
    private static Logger LOGGER = Context.getCurrentLogger();

    /**
     * Returns an instance of what must be a subclass of {@link Application}.
     * Returns null in case of errors.
     * 
     * @param className
     *            The name of the application class.
     * @return An instance of what must be a subclass of {@link Application}.
     */
    private static Application getApplication(String className) {
        Application result = null;

        if (className == null) {
            return result;
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
            if (Application.class.isAssignableFrom(clazz)) {
                result = (Application) clazz.getConstructor().newInstance();
            } else {
                LOGGER.log(Level.SEVERE, className
                        + " does not seem to be a valid subclass of "
                        + Application.class.getName() + " class.");
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Cannot locate the definition source.", e);
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE,
                    "Cannot instantiate the application class.", e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE,
                    "Cannot instantiate the application class.", e);
        } catch (IllegalArgumentException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Check that the application class has an empty constructor.",
                    e);
        } catch (InvocationTargetException e) {
            LOGGER.log(Level.SEVERE,
                    "Cannot instantiate the application class.", e);
        } catch (NoSuchMethodException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Check that the application class has an empty constructor.",
                    e);
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE,
                    "Cannot instantiate the application class.", e);
        }

        return result;
    }

    /**
     * Returns a APISpark description of the current application. By default,
     * this method discovers all the resources attached to this application. It
     * can be overridden to add documentation, list of representations, etc.
     * 
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return An application description.
     */
    protected static ApplicationInfo getApplicationInfo(
            Application application, Reference baseRef) {
        ApplicationInfo applicationInfo = new ApplicationInfo();
        if ((application.getName() != null) && !application.getName().isEmpty()) {
            DocumentationInfo doc = null;
            if (applicationInfo.getDocumentations().isEmpty()) {
                doc = new DocumentationInfo();
                applicationInfo.getDocumentations().add(doc);
            } else {
                doc = applicationInfo.getDocumentations().get(0);
            }
            applicationInfo.setName(application.getName());
            doc.setTitle(application.getName());
        }
        applicationInfo.getResources().setBaseRef(baseRef);
        applicationInfo.getResources().setResources(
                getResourceInfos(applicationInfo,
                        getNextRouter(application.getInboundRoot()), "/"));

        return applicationInfo;
    }

    /**
     * Returns an instance of what must be a subclass of {@link Component}.
     * Returns null in case of errors.
     * 
     * @param className
     *            The name of the component class.
     * @return An instance of what must be a subclass of {@link Component}.
     */
    private static Component getComponent(String className) {
        Component result = null;

        if (className == null) {
            return result;
        }

        Class<?> clazz = null;
        try {
            clazz = Class.forName(className);
            if (Component.class.isAssignableFrom(clazz)) {
                result = (Component) clazz.getConstructor().newInstance();
            } else {
                LOGGER.log(Level.SEVERE, className
                        + " does not seem to a valid subclass of "
                        + Component.class.getName() + " class.");
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Cannot locate the component class.", e);
        } catch (InstantiationException e) {
            LOGGER.log(Level.SEVERE, "Cannot instantiate the component class.",
                    e);
        } catch (IllegalAccessException e) {
            LOGGER.log(Level.SEVERE, "Cannot instantiate the component class.",
                    e);
        } catch (IllegalArgumentException e) {
            LOGGER.log(Level.SEVERE,
                    "Check that the component class has an empty constructor.",
                    e);
        } catch (InvocationTargetException e) {
            LOGGER.log(Level.SEVERE, "Cannot instantiate the component class.",
                    e);
        } catch (NoSuchMethodException e) {
            LOGGER.log(Level.SEVERE,
                    "Check that the component class has an empty constructor.",
                    e);
        } catch (SecurityException e) {
            LOGGER.log(Level.SEVERE, "Cannot instantiate the component class.",
                    e);
        }

        return result;
    }

    /**
     * Returns the next application available.
     * 
     * @param current
     *            The current Restlet to inspect.
     * @return The first application available.
     */
    private static Application getNextApplication(Restlet current) {
        Application result = null;
        if (current instanceof Application) {
            result = (Application) current;
        } else if (current instanceof Filter) {
            result = getNextApplication(((Filter) current).getNext());
        } else if (current instanceof Router) {
            Router router = (Router) current;
            for (Route route : router.getRoutes()) {
                result = getNextApplication(route.getNext());
                if (result != null) {
                    break;
                }
            }
        }

        return result;
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
     * Returns the value according to its index.
     * 
     * @param args
     *            The argument table.
     * @param index
     *            The index of the argument.
     * @return The value of the given argument.
     */
    private static String getParameter(String[] args, int index) {
        if (index >= args.length) {
            return null;
        } else {
            String value = args[index];
            if ("-s".equals(value) || "-u".equals(value) || "-p".equals(value)
                    || "-d".equals(value) || "-c".equals(value)) {
                // In case the given value is actually an option, reset it.
                value = null;
            }
            return value;
        }
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
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return The resource description.
     */
    private static ResourceInfo getResourceInfo(
            ApplicationInfo applicationInfo, Filter filter, String path) {
        return getResourceInfo(applicationInfo, filter.getNext(), path);
    }

    /**
     * Completes the data available about a given Finder instance.
     * 
     * @param applicationInfo
     *            The parent application.
     * @param resourceInfo
     *            The ResourceInfo object to complete.
     * @param finder
     *            The Finder instance to document.
     */
    private static ResourceInfo getResourceInfo(
            ApplicationInfo applicationInfo, Finder finder, String path) {
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
        }

        return result;
    }

    /**
     * Completes the data available about a given Restlet instance.
     * 
     * @param applicationInfo
     *            The parent application.
     * @param resourceInfo
     *            The ResourceInfo object to complete.
     * @param restlet
     *            The Restlet instance to document.
     */
    private static ResourceInfo getResourceInfo(
            ApplicationInfo applicationInfo, Restlet restlet, String path) {
        ResourceInfo result = null;

        if (restlet instanceof Finder) {
            result = getResourceInfo(applicationInfo, (Finder) restlet, path);
        } else if (restlet instanceof Router) {
            result = new ResourceInfo();
            result.setPath(path);
            result.setChildResources(getResourceInfos(applicationInfo,
                    (Router) restlet, path));
        } else if (restlet instanceof Filter) {
            result = getResourceInfo(applicationInfo, (Filter) restlet, path);
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
            ApplicationInfo applicationInfo, Route route, String basePath) {
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

            result = getResourceInfo(applicationInfo, route.getNext(), path);
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
            ApplicationInfo applicationInfo, Router router, String path) {
        List<ResourceInfo> result = new ArrayList<ResourceInfo>();

        if (router != null) {
            for (Route route : router.getRoutes()) {
                ResourceInfo resourceInfo = getResourceInfo(applicationInfo,
                        route, path);

                if (resourceInfo != null) {
                    result.add(resourceInfo);
                }
            }

            if (router.getDefaultRoute() != null) {
                ResourceInfo resourceInfo = getResourceInfo(applicationInfo,
                        router.getDefaultRoute(), path);
                if (resourceInfo != null) {
                    result.add(resourceInfo);
                }
            }
        }

        return result;
    }

    /**
     * Main class, invoke this class without argument to get help instructions.
     * 
     * @param args
     * @throws TranslationException
     */
    public static void main(String[] args) throws TranslationException {
        Engine.register();
        String ulogin = null;
        String upwd = null;
        String serviceUrl = null;
        String defSource = null;
        String compName = null;
        String definitionId = null;
        String language = null;

        LOGGER.fine("Get parameters");
        for (int i = 0; i < (args.length); i++) {
            if ("-h".equals(args[i])) {
                printHelp();
                System.exit(0);
            } else if ("-u".equals(args[i]) || "--username".equals(args[i])) {
                ulogin = getParameter(args, ++i);
            } else if ("-p".equals(args[i]) || "--password".equals(args[i])) {
                upwd = getParameter(args, ++i);
            } else if ("-s".equals(args[i]) || "--service".equals(args[i])) {
                serviceUrl = getParameter(args, ++i);
            } else if ("-c".equals(args[i]) || "--component".equals(args[i])) {
                compName = getParameter(args, ++i);
            } else if ("-d".equals(args[i]) || "--definition".equals(args[i])) {
                definitionId = getParameter(args, ++i);
            } else if ("-l".equals(args[i]) || "--language".equals(args[i])) {
                language = getParameter(args, ++i).toLowerCase();
            } else if ("-v".equals(args[i]) || "--verbose".equals(args[i])) {
                // [ifndef gae,jee] instruction
                Engine.setLogLevel(Level.FINE);
            } else {
                defSource = args[i];
            }
        }
        Engine.getLogger("").getHandlers()[0]
                .setFilter(new java.util.logging.Filter() {
                    public boolean isLoggable(LogRecord record) {
                        return record.getLoggerName().startsWith(
                                "org.restlet.ext.apispark");
                    }
                });

        LOGGER.fine("Check parameters");
        if (IntrospectionUtils.isEmpty(serviceUrl)) {
            serviceUrl = "https://apispark.com/";
        }
        if (!serviceUrl.endsWith("/")) {
            serviceUrl += "/";
        }

        if (IntrospectionUtils.isEmpty(ulogin)
                || IntrospectionUtils.isEmpty(upwd)
                || IntrospectionUtils.isEmpty(defSource)) {
            printHelp();
            System.exit(1);
        }

        // TODO validate the definition URL:
        // * accept absolute urls
        // * accept relative urls such as /definitions/{id} and concatenate with
        // the serviceUrl
        // * accept relative urls such as {id} and concatenate with the
        // serviceUrl

        // Validate the application class name
        Application application = null;
        Component component = null;
        Definition definition = null;
        javax.ws.rs.core.Application a = null;

        if (language == null) {
            Class<?> clazz = null;
            try {
                clazz = Class.forName(defSource);
                if (Application.class.isAssignableFrom(clazz)) {
                    application = getApplication(defSource);
                    component = getComponent(compName);
                } else if (clazz != null) {
                    a = IntrospectionUtils.getApplication(defSource);
                }
            } catch (ClassNotFoundException e) {
                LOGGER.log(Level.SEVERE,
                        "Cannot locate the application class.", e);
            }
        }

        if (application != null) {
            LOGGER.info("Instantiate introspector");
            Introspector i = new Introspector(component, application);

            LOGGER.info("Generate documentation");
            definition = i.getDefinition();
        } else if (a != null) {
            LOGGER.fine("Instantiate introspector");
            JaxRsIntrospector i = new JaxRsIntrospector(a);

            LOGGER.info("Generate documentation");
            definition = i.getDefinition();
        } else if ("swagger".equals(language)) {
            definition = SwaggerUtils.getDefinition(defSource, ulogin, upwd);
        }
        if (definition != null) {
            IntrospectionUtils.sendDefinition(definition, definitionId, ulogin,
                    upwd, serviceUrl, LOGGER);
        } else {
            LOGGER.severe("Please provide a valid application class name or definition URL.");
        }
    }

    /**
     * Prints the instructions necessary to launch this tool.
     */
    private static void printHelp() {
        PrintStream o = System.out;

        o.println("SYNOPSIS");
        IntrospectionUtils.printSynopsis(o, Introspector.class,
                "[options] APPLICATION");
        IntrospectionUtils.printSynopsis(o, Introspector.class,
                "-l swagger [options] SWAGGER DEFINITION URL/PATH");
        o.println("DESCRIPTION");
        IntrospectionUtils
                .printSentence(
                        o,
                        "Publish to the APISpark platform the description of your Web API, represented by APPLICATION,",
                        "the full name of your Restlet application class or by the swagger definition available on the ",
                        "URL/PATH");
        IntrospectionUtils
                .printSentence(
                        o,
                        "If the whole process is successfull, it displays the url of the corresponding documentation.");
        o.println("OPTIONS");
        IntrospectionUtils.printOption(o, "-h, --help", "Prints this help.");
        IntrospectionUtils.printOption(o, "-u, --username",
                "The mandatory APISpark user name.");
        IntrospectionUtils.printOption(o, "-p, --password",
                "The mandatory APISpark user secret key.");
        IntrospectionUtils
                .printOption(
                        o,
                        "-c, --component",
                        "The optional full name of your Restlet Component class.",
                        "This allows to collect some other data, such as the endpoint.");
        IntrospectionUtils
                .printOption(
                        o,
                        "-d, --definition",
                        "The optional identifier of an existing definition hosted by APISpark you want to update with this new documentation.");
        IntrospectionUtils
                .printOption(
                        o,
                        "-l, --language",
                        "The optional name of the description language of the definition you want to upload. Possible value: swagger");
        IntrospectionUtils
                .printOption(o, "-v, --verbose",
                        "The optional parameter switching the process to a verbose mode");
    }

    /** The current Web API definition. */
    private Definition definition;

    /**
     * Constructor.
     * 
     * @param application
     *            An application to introspect.
     */
    public Introspector(Application application, boolean verbose) {
        this(null, application);
    }

    /**
     * Constructor.
     * 
     * @param component
     *            A {@link Component} to introspect in order to get extra
     *            details such as the endpoint.
     * @param application
     *            An {@link Application} to introspect.
     */
    public Introspector(Component component, Application application) {
        definition = IntrospectionTranslator.toDefinition(
                getApplicationInfo(application, null), LOGGER);

        if (component != null && definition != null) {
            LOGGER.fine("Look for the endpoint.");
            String endpoint = null;
            // TODO What if the application is attached to several endpoints?
            // Look for the endpoint to which this application is attached.
            endpoint = getEndpoint(component.getDefaultHost(), application);
            for (int i = 0; endpoint == null && i < component.getHosts().size(); i++) {
                VirtualHost virtualHost = component.getHosts().get(i);
                endpoint = getEndpoint(virtualHost, application);
            }
            definition.setEndpoint(endpoint);
        }
    }

    /**
     * Returns the current definition.
     * 
     * @return The current definition.
     */
    private Definition getDefinition() {
        return definition;
    }

    /**
     * Returns the endpoint to which the application is attached.
     * 
     * @param virtualHost
     *            The virtual host to which this application may be attached.
     * @param application
     *            The application.
     * @return The endpoint.
     */
    private String getEndpoint(VirtualHost virtualHost, Application application) {
        String result = null;

        for (Route route : virtualHost.getRoutes()) {
            if (route.getNext() != null) {
                Application app = getNextApplication(route.getNext());
                if (app != null
                        && application.getClass().equals(app.getClass())) {
                    String hostDomain = null;
                    if (virtualHost.getHostDomain() != null
                            && !".*".equals(virtualHost.getHostDomain())) {
                        if (virtualHost.getHostDomain().contains("|")) {
                            hostDomain = virtualHost.getHostDomain().split("|")[0];
                        } else {
                            hostDomain = virtualHost.getHostDomain();
                        }
                    }
                    if (hostDomain != null) {
                        Protocol scheme = null;
                        if (!".*".equals(virtualHost.getHostScheme())) {
                            scheme = Protocol.valueOf(virtualHost
                                    .getHostScheme());
                        }
                        if (scheme == null) {
                            scheme = Protocol.HTTP;
                        }
                        Reference ref = new Reference();
                        ref.setProtocol(scheme);
                        ref.setHostDomain(hostDomain);
                        if (route instanceof TemplateRoute) {
                            ref.addSegment(((TemplateRoute) route)
                                    .getTemplate().getPattern());
                        }
                        try {
                            ref.setHostPort(Integer.parseInt(virtualHost
                                    .getHostPort()));
                        } catch (Exception e) {
                            // Nothing
                        }
                        // Concatenate in order to get the endpoint
                        result = ref.toString();
                    }
                }
            }
        }
        return result;
    }

}
