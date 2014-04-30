package org.restlet.ext.apispark;

import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.ChallengeScheme;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.ext.apispark.internal.info.ApplicationInfo;
import org.restlet.ext.apispark.internal.info.DocumentationInfo;
import org.restlet.ext.apispark.internal.info.MethodInfo;
import org.restlet.ext.apispark.internal.info.ParameterInfo;
import org.restlet.ext.apispark.internal.info.ParameterStyle;
import org.restlet.ext.apispark.internal.info.RepresentationInfo;
import org.restlet.ext.apispark.internal.info.ResourceInfo;
import org.restlet.ext.apispark.internal.info.ResponseInfo;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.VirtualHost;

import com.sun.istack.internal.logging.Logger;

/**
 * Publish the documentation of a Restlet-based Application to the APISpark
 * console.
 * 
 * @author thboileau
 */
public class Introspector {

    /** Internal logger. */
    private static Logger LOGGER = Logger.getLogger(Introspector.class);

    /**
     * Completes a map of representations with a list of representations.
     * 
     * @param mapReps
     *            The map to complete.
     * @param representations
     *            The source list.
     */
    private static void addRepresentations(
            Map<String, RepresentationInfo> mapReps,
            List<RepresentationInfo> representations) {
        if (representations != null) {
            for (RepresentationInfo r : representations) {
                if (!mapReps.containsKey(r.getIdentifier())) {
                    mapReps.put(r.getIdentifier(), r);
                }
            }
        }
    }

    /**
     * Completes the given {@link Contract} with the list of resources.
     * 
     * @param application
     *            The source application.
     * @param contract
     *            The contract to complete.
     * @param resources
     *            The list of resources.
     * @param basePath
     *            The resources base path.
     * @param mapReps
     *            The lndex of representations.
     */
    private static void addResources(ApplicationInfo application,
            Contract contract, List<ResourceInfo> resources, String basePath,
            Map<String, RepresentationInfo> mapReps) {
        for (ResourceInfo ri : resources) {
            Resource resource = new Resource();
            resource.setDescription(toString(ri.getDocumentations()));
            resource.setName(ri.getIdentifier());
            if (basePath != null) {
                if (basePath.endsWith("/")) {
                    if (ri.getPath().startsWith("/")) {
                        resource.setResourcePath(basePath
                                + ri.getPath().substring(1));
                    } else {
                        resource.setResourcePath(basePath + ri.getPath());
                    }
                } else {
                    if (ri.getPath().startsWith("/")) {
                        resource.setResourcePath(basePath + ri.getPath());
                    } else {
                        resource.setResourcePath(basePath + "/" + ri.getPath());
                    }
                }

            } else {
                resource.setResourcePath(ri.getPath());
            }

            if (!ri.getChildResources().isEmpty()) {
                addResources(application, contract, ri.getChildResources(),
                        resource.getResourcePath(), mapReps);
            }

            if (ri.getMethods().isEmpty()) {
                continue;
            }

            resource.setPathVariables(new ArrayList<PathVariable>());
            for (ParameterInfo pi : ri.getParameters()) {
                if (ParameterStyle.TEMPLATE.equals(pi.getStyle())) {
                    PathVariable pathVariable = new PathVariable();

                    pathVariable
                            .setDescription(toString(pi.getDocumentations()));
                    pathVariable.setName(pi.getName());

                    resource.getPathVariables().add(pathVariable);
                }
            }

            resource.setOperations(new ArrayList<Operation>());
            for (MethodInfo mi : ri.getMethods()) {

                Operation operation = new Operation();
                operation.setDescription(toString(mi.getDocumentations()));
                operation.setName(mi.getMethod().getName());
                // TODO complete Method class with mi.getName()
                operation.setMethod(new org.restlet.ext.apispark.Method());
                operation.getMethod().setDescription(
                        mi.getMethod().getDescription());
                operation.getMethod().setName(mi.getMethod().getName());

                // Complete parameters
                operation.setHeaders(new ArrayList<Parameter>());
                operation.setQueryParameters(new ArrayList<Parameter>());
                if (mi.getRequest() != null
                        && mi.getRequest().getParameters() != null) {
                    for (ParameterInfo pi : mi.getRequest().getParameters()) {
                        if (ParameterStyle.HEADER.equals(pi.getStyle())) {
                            Parameter parameter = new Parameter();
                            parameter.setAllowMultiple(pi.isRepeating());
                            parameter.setDefaultValue(pi.getDefaultValue());
                            parameter.setDescription(toString(pi
                                    .getDocumentations()));
                            parameter.setName(pi.getName());
                            parameter
                                    .setPossibleValues(new ArrayList<String>());
                            parameter.setRequired(pi.isRequired());

                            operation.getHeaders().add(parameter);
                        } else if (ParameterStyle.QUERY.equals(pi.getStyle())) {
                            Parameter parameter = new Parameter();
                            parameter.setAllowMultiple(pi.isRepeating());
                            parameter.setDefaultValue(pi.getDefaultValue());
                            parameter.setDescription(toString(pi
                                    .getDocumentations()));
                            parameter.setName(pi.getName());
                            parameter
                                    .setPossibleValues(new ArrayList<String>());
                            parameter.setRequired(pi.isRequired());

                            operation.getHeaders().add(parameter);
                        }
                    }
                }

                if (mi.getRequest() != null
                        && mi.getRequest().getRepresentations() != null
                        && !mi.getRequest().getRepresentations().isEmpty()) {
                    addRepresentations(mapReps, mi.getRequest()
                            .getRepresentations());

                    Body body = new Body();
                    // TODO analyze
                    // The models differ : one representation / one variant
                    // for Restlet one representation / several variants for
                    // APIspark
                    body.setRepresentation(mi.getRequest().getRepresentations()
                            .get(0).getName());

                    operation.setInRepresentation(body);
                }

                if (mi.getResponses() != null && !mi.getResponses().isEmpty()) {
                    operation.setResponses(new ArrayList<Response>());

                    Body body = new Body();
                    // TODO analyze
                    // The models differ : one representation / one variant
                    // for Restlet one representation / several variants for
                    // APIspark
                    if (!mi.getResponse().getRepresentations().isEmpty()) {
                        body.setRepresentation(mi.getResponse()
                                .getRepresentations().get(0).getName());
                    }
                    operation.setOutRepresentation(body);

                    for (ResponseInfo rio : mi.getResponses()) {
                        addRepresentations(mapReps, rio.getRepresentations());

                        if (!rio.getStatuses().isEmpty()) {
                            Status status = rio.getStatuses().get(0);
                            // TODO analyze
                            // The models differ : one representation / one
                            // variant
                            // for Restlet one representation / several
                            // variants for
                            // APIspark

                            Response response = new Response();
                            response.setBody(body);
                            response.setCode(status.getCode());
                            response.setName(toString(rio.getDocumentations()));
                            response.setDescription(toString(rio
                                    .getDocumentations()));
                            response.setMessage(status.getDescription());
                            // response.setName();

                            operation.getResponses().add(response);
                        }
                    }
                }

                resource.getOperations().add(operation);
            }

            contract.getResources().add(resource);
        }
    }

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
                        + " does not seem to a valid subclass of "
                        + Application.class.getName() + " class.");
            }
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Cannot locate the application class.", e);
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
                        getNextRouter(application.getInboundRoot())));

        //

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

    private static String getParameter(String[] args, int index) {
        if (index >= args.length) {
            return null;
        } else {
            String value = args[index];
            if ("-s".equals(value) || "-u".equals(value) || "-p".equals(value)
                    || "-d".equals(value) || "-c".equals(value)) {
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
                    (Router) restlet));
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
                path = path.substring(1);
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
     * @return The list of ResourceInfo instances to complete.
     */
    private static List<ResourceInfo> getResourceInfos(
            ApplicationInfo applicationInfo, Router router) {
        List<ResourceInfo> result = new ArrayList<ResourceInfo>();

        if (router != null) {
            for (Route route : router.getRoutes()) {
                ResourceInfo resourceInfo = getResourceInfo(applicationInfo,
                        route, "/");

                if (resourceInfo != null) {
                    result.add(resourceInfo);
                }
            }

            if (router.getDefaultRoute() != null) {
                ResourceInfo resourceInfo = getResourceInfo(applicationInfo,
                        router.getDefaultRoute(), "/");
                if (resourceInfo != null) {
                    result.add(resourceInfo);
                }
            }
        }

        return result;
    }

    /**
     * Indicates if the given velue is either null or empty.
     * 
     * @param value
     *            The value.
     * @return True if the value is either null or empty.
     */
    private static boolean isEmpty(String value) {
        return value == null || value.isEmpty();
    }

    /**
     * Main class, invoke this class withour argument to get help instructions.
     * 
     * @param args
     */
    public static void main(String[] args) {
        String ulogin = null;
        String upwd = null;
        String serviceUrl = null;
        String appName = null;
        String compName = null;
        String definitionId = null;

        LOGGER.fine("Get parameters");
        for (int i = 0; i < (args.length); i++) {
            if ("-h".equals(args[i])) {
                printHelp();
                System.exit(0);
            } else if ("-u".equals(args[i])) {
                ulogin = getParameter(args, ++i);
            } else if ("-p".equals(args[i])) {
                upwd = getParameter(args, ++i);
            } else if ("-s".equals(args[i])) {
                serviceUrl = getParameter(args, ++i);
            } else if ("-c".equals(args[i])) {
                compName = getParameter(args, ++i);
            } else if ("-d".equals(args[i])) {
                definitionId = getParameter(args, ++i);
            } else {
                appName = args[i];
            }
        }

        LOGGER.fine("Check parameters");
        if (isEmpty(serviceUrl)) {
            serviceUrl = "https://apispark.com/";
        }
        if (!serviceUrl.endsWith("/")) {
            serviceUrl += "/";
        }

        if (isEmpty(ulogin) || isEmpty(upwd) || isEmpty(appName)) {
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
        Application application = getApplication(appName);
        Component component = getComponent(compName);

        if (application != null) {
            LOGGER.fine("Instantiate introspector");
            Introspector i = new Introspector(component, application);

            try {
                ClientResource cr = new ClientResource(serviceUrl
                        + "definitions");
                cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, ulogin,
                        upwd);
                LOGGER.fine("Generate documentation");
                Definition definition = i.getDefinition();

                if (definitionId == null) {
                    LOGGER.fine("Create a new documentation");
                    cr.post(definition, MediaType.APPLICATION_JSON);
                } else {
                    cr.addSegment(definitionId);
                    LOGGER.fine("Update the documentation of "
                            + cr.getReference().toString());
                    cr.put(definition, MediaType.APPLICATION_JSON);
                }

                LOGGER.fine("Display result");
                System.out.println("Process successfully achieved.");
                // This is not printed by a logger which may be muted.
                if (cr.getLocationRef() != null) {
                    System.out.println(cr.getLocationRef());
                }
                if (cr.getResponseEntity() != null
                        && cr.getResponseEntity().isAvailable()) {
                    try {
                        cr.getResponseEntity().write(System.out);
                    } catch (IOException e) {
                        // [PENDING] analysis
                        LOGGER.warning("Request successfully achieved by the server, but it's response cannot be printed");
                    }
                }
            } catch (ResourceException e) {
                // TODO Should we detail by status?
                if (e.getStatus().isConnectorError()) {
                    LOGGER.severe("Cannot reach the remote service, could you check your network connection?");
                    LOGGER.severe("Could you check that the following service is up? "
                            + serviceUrl);
                } else if (e.getStatus().isClientError()) {
                    LOGGER.severe("Check that you provide valid credentials, or valid service url.");
                } else if (e.getStatus().isServerError()) {
                    LOGGER.severe("The server side encounters some issues, please try later.");
                }
            }
        } else {
            LOGGER.severe("Please provide a valid application class name.");
        }
    }

    /**
     * Prints the instructions necessary to launch this tool.
     */
    private static void printHelp() {
        PrintStream o = System.out;

        o.println("SYNOPSIS");
        printSynopsis(o, Introspector.class, "[options] APPLICATION");
        o.println("DESCRIPTION");
        printSentence(
                o,
                "Publish to the APISpark platform the description of your Web API, represented by APPLICATION,",
                "the full name of your Restlet application class.");
        printSentence(
                o,
                "If the whole process is successfull, it displays the url of the corresponding documentation.");
        o.println("OPTIONS");
        printOption(o, "-h", "Prints this help.");
        printOption(o, "-u", "The mandatory APISpark user login.");
        printOption(o, "-p", "The mandatory APISpark user security token.");
        printOption(o, "-s",
                "The optional APISpark platform URL (by default https://apispark.com).");
        printOption(o, "-c",
                "The optional full name of your Restlet Component class.",
                "This allows to collect some other data, such as the endpoint.");
        printOption(
                o,
                "-d",
                "The optional identifier of an existing definition hosted by APISpark you want to update with this new documentation.");
        o.println("LOGGING");
        printSentence(
                o,
                "You can get a detailled log of the process using the JDK's API.",
                "See the official documentation: http://docs.oracle.com/javase/7/docs/technotes/guides/logging/overview.html",
                "Here is the name of the used Logger: "
                        + Introspector.class.getName());
    }

    /**
     * Displays an option and its description to the console.
     * 
     * @param o
     *            The console stream.
     * @param option
     *            The option.
     * @param strings
     *            The option's description.
     */
    private static void printOption(PrintStream o, String option,
            String... strings) {
        printSentence(o, 7, option);
        printSentence(o, 14, strings);
    }

    /**
     * Formats a list of Strings by lines of 80 characters maximul, and displays
     * it to the console.
     * 
     * @param o
     *            The console.
     * @param shift
     *            The number of characters to shift the list of strings on the
     *            left.
     * @param strings
     *            The list of Strings to display.
     */
    private static void printSentence(PrintStream o, int shift,
            String... strings) {
        int blockLength = 80 - shift - 1;
        String tab = "";
        for (int i = 0; i < shift; i++) {
            tab = tab.concat(" ");
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < strings.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            sb.append(strings[i]);
        }
        String sentence = sb.toString();
        // Cut in slices
        int index = 0;
        while (index < (sentence.length() - 1)) {
            o.print(tab);
            int length = Math.min(index + blockLength, sentence.length() - 1);
            if ((length - index) < blockLength) {
                o.println(sentence.substring(index));
                index = length + 1;
            } else if (sentence.charAt(length) == ' ') {
                o.println(sentence.substring(index, length));
                index = length + 1;
            } else {
                length = sentence.substring(index, length - 1).lastIndexOf(' ');
                if (length != -1) {
                    o.println(sentence.substring(index, index + length));
                    index += length + 1;
                } else {
                    length = sentence.substring(index).indexOf(' ');
                    if (length != -1) {
                        o.println(sentence.substring(index, index + length));
                        index += length + 1;
                    } else {
                        o.println(sentence.substring(index));
                        index = sentence.length();
                    }
                }
            }
        }
    }

    /**
     * Displays a list of String to the console.
     * 
     * @param o
     *            The console stream.
     * @param strings
     *            The list of Strings to display.
     */
    private static void printSentence(PrintStream o, String... strings) {
        printSentence(o, 7, strings);
    }

    /**
     * Displays the command line.
     * 
     * @param o
     *            The console stream.
     * @param clazz
     *            The main class.
     * @param command
     *            The command line.
     */
    private static void printSynopsis(PrintStream o, Class<?> clazz,
            String command) {
        printSentence(o, 7, clazz.getName(), command);
    }

    /**
     * Converts a ApplicationInfo to a {@link Definition} object.
     * 
     * @param application
     *            The {@link ApplicationInfo} instance.
     * @return The definintion instance.
     */
    private static Definition toDefinition(ApplicationInfo application) {
        Definition result = null;
        if (application != null) {
            result = new Definition();
            result.setVersion(application.getVersion());
            if (application.getResources().getBaseRef() != null) {
                result.setEndpoint(application.getResources().getBaseRef()
                        .toString());
            }

            Contract contract = new Contract();
            result.setContract(contract);
            contract.setDescription(toString(application.getDocumentations()));
            contract.setName(application.getName());
            if (contract.getName() == null || contract.getName().isEmpty()) {
                contract.setName(application.getClass().getName());
                Context.getCurrentLogger().log(
                        Level.WARNING,
                        "Please provide a name to your application, used "
                                + contract.getName() + " by default.");
            }

            // List of resources.
            contract.setResources(new ArrayList<Resource>());
            Map<String, RepresentationInfo> mapReps = new HashMap<String, RepresentationInfo>();
            addResources(application, contract, application.getResources()
                    .getResources(), result.getEndpoint(), mapReps);

            java.util.List<String> protocols = new ArrayList<String>();
            for (ConnectorHelper<Server> helper : Engine.getInstance()
                    .getRegisteredServers()) {
                for (Protocol protocol : helper.getProtocols()) {
                    if (!protocols.contains(protocol.getName())) {
                        protocols.add(protocol.getName());
                    }
                }
            }

            // List of representations.
            contract.setRepresentations(new ArrayList<Representation>());
            for (RepresentationInfo ri : application.getRepresentations()) {
                if (!mapReps.containsKey(ri.getIdentifier())) {
                    mapReps.put(ri.getIdentifier(), ri);
                }
            }
            // This first phase discovers representations related to annotations
            // Let's cope with the inheritance chain, and complex properties
            for (RepresentationInfo ri : mapReps.values()) {
                String parentType = ri.getParentType();
                while (parentType != null) {
                    if (!mapReps.containsKey(parentType)) {
                        try {
                            RepresentationInfo r = RepresentationInfo
                                    .introspect(
                                            Class.forName(ri.getParentType()),
                                            null);
                            mapReps.put(r.getIdentifier(), r);
                        } catch (ClassNotFoundException e) {
                            LOGGER.warning("Cannot locate class "
                                    + ri.getParentType()
                                    + ", referenced by class "
                                    + ri.getIdentifier());
                        }
                    }
                    parentType = mapReps.get(parentType).getParentType();
                }
            }

            for (RepresentationInfo ri : mapReps.values()) {
                Representation rep = new Representation();

                // TODO analyze
                // The models differ : one representation / one variant for
                // Restlet
                // one representation / several variants for APIspark
                rep.setDescription(toString(ri.getDocumentations()));
                rep.setName(ri.getName());
                Variant variant = new Variant();
                variant.setDataType(ri.getMediaType().getName());
                rep.setVariants(new ArrayList<Variant>());
                rep.getVariants().add(variant);

                rep.setProperties(ri.getProperties());
                rep.setRaw(ri.isRaw());
                contract.getRepresentations().add(rep);
            }

        }
        return result;
    }

    /**
     * Concats a list of {@link DocumentationInfo} instances as a single String.
     * 
     * @param dis
     *            The list of {@link DocumentationInfo} instances.
     * @return A String value.
     */
    private static String toString(List<DocumentationInfo> dis) {
        StringBuilder d = new StringBuilder();
        for (DocumentationInfo doc : dis) {
            d.append(doc.getTextContent());
        }
        return d.toString();
    }

    /** The current Web API definition. */
    private Definition definition;

    /**
     * Constructor.
     * 
     * @param application
     *            An application to introspect.
     */
    public Introspector(Application application) {
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
    public Introspector(Component component, Application application) {
        definition = toDefinition(getApplicationInfo(application, null));

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
    public Definition getDefinition() {
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

    /**
     * Sets the current definition.
     * 
     * @param definition
     *            The current definition.
     */
    public void setDefinition(Definition definition) {
        this.definition = definition;
    }
}
