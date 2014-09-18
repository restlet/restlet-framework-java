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

package org.restlet.ext.raml.internal.reflect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Restlet;
import org.restlet.Server;
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
import org.restlet.ext.apispark.internal.info.PropertyInfo;
import org.restlet.ext.apispark.internal.info.RepresentationInfo;
import org.restlet.ext.apispark.internal.info.ResourceInfo;
import org.restlet.ext.apispark.internal.info.ResponseInfo;
import org.restlet.ext.apispark.internal.model.Contract;
import org.restlet.ext.apispark.internal.model.Definition;
import org.restlet.ext.apispark.internal.model.Endpoint;
import org.restlet.ext.apispark.internal.model.Entity;
import org.restlet.ext.apispark.internal.model.Header;
import org.restlet.ext.apispark.internal.model.Operation;
import org.restlet.ext.apispark.internal.model.PathVariable;
import org.restlet.ext.apispark.internal.model.Property;
import org.restlet.ext.apispark.internal.model.QueryParameter;
import org.restlet.ext.apispark.internal.model.Representation;
import org.restlet.ext.apispark.internal.model.Resource;
import org.restlet.ext.apispark.internal.model.Response;
import org.restlet.ext.apispark.internal.reflect.ReflectUtils;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.VirtualHost;

/**
 * Publish the documentation of a Restlet-based Application to the APISpark
 * console.
 * 
 * @author Thierry Boileau
 */
public class Introspector {

    /** Internal logger. */
    protected static Logger LOGGER = Logger.getLogger(Introspector.class
            .getName());

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
            if (ri.getPath() == null) {
                resource.setResourcePath("/");
            } else if (!ri.getPath().startsWith("/")) {
                resource.setResourcePath("/" + ri.getPath());
            } else {
                resource.setResourcePath(ri.getPath());
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

            if (!ri.getChildResources().isEmpty()) {
                addResources(application, contract, ri.getChildResources(),
                        resource.getResourcePath(), mapReps);
            }
            LOGGER.fine("Resource " + ri.getPath() + " added.");

            if (ri.getMethods().isEmpty()) {
                LOGGER.warning("Resource " + ri.getIdentifier()
                        + " has no methods.");
                continue;
            }

            resource.setOperations(new ArrayList<Operation>());
            for (MethodInfo mi : ri.getMethods()) {
                String methodName = mi.getMethod().getName();
                if ("OPTIONS".equals(methodName) || "PATCH".equals(methodName)) {
                    LOGGER.fine("Method " + methodName + " ignored.");
                    continue;
                }
                LOGGER.fine("Method " + methodName + " added.");
                Operation operation = new Operation();
                operation.setDescription(toString(mi.getDocumentations()));
                operation.setName(methodName);
                // TODO complete Method class with mi.getName()
                operation.setMethod(mi.getMethod().getName());

                // Fill fields produces/consumes
                String mediaType;
                if (mi.getRequest() != null
                        && mi.getRequest().getRepresentations() != null) {
                    List<RepresentationInfo> consumed = mi.getRequest()
                            .getRepresentations();
                    for (RepresentationInfo reprInfo : consumed) {
                        mediaType = reprInfo.getMediaType().getName();
                        operation.getConsumes().add(mediaType);
                    }
                }

                if (mi.getResponse() != null
                        && mi.getResponse().getRepresentations() != null) {
                    List<RepresentationInfo> produced = mi.getResponse()
                            .getRepresentations();
                    for (RepresentationInfo reprInfo : produced) {
                        mediaType = reprInfo.getMediaType().getName();
                        operation.getProduces().add(mediaType);
                    }
                }

                // Complete parameters
                operation.setHeaders(new ArrayList<Header>());
                operation.setQueryParameters(new ArrayList<QueryParameter>());
                if (mi.getRequest() != null) {
                    for (ParameterInfo pi : mi.getRequest().getParameters()) {
                        if (ParameterStyle.HEADER.equals(pi.getStyle())) {
                            Header header = new Header();
                            header.setAllowMultiple(pi.isRepeating());
                            header.setDefaultValue(pi.getDefaultValue());
                            header.setDescription(toString(
                                    pi.getDocumentations(),
                                    pi.getDefaultValue()));
                            header.setName(pi.getName());
                            header.setEnumeration(new ArrayList<String>());
                            header.setRequired(pi.isRequired());

                            operation.getHeaders().add(header);
                        } else if (ParameterStyle.QUERY.equals(pi.getStyle())) {
                            QueryParameter queryParameter = new QueryParameter();
                            queryParameter.setAllowMultiple(pi.isRepeating());
                            queryParameter
                                    .setDefaultValue(pi.getDefaultValue());
                            queryParameter.setDescription(toString(
                                    pi.getDocumentations(),
                                    pi.getDefaultValue()));
                            queryParameter.setName(pi.getName());
                            queryParameter
                                    .setEnumeration(new ArrayList<String>());
                            queryParameter.setRequired(pi.isRequired());

                            operation.getQueryParameters().add(queryParameter);
                        }
                    }
                }
                for (ParameterInfo pi : mi.getParameters()) {
                    if (ParameterStyle.HEADER.equals(pi.getStyle())) {
                        Header header = new Header();
                        header.setAllowMultiple(pi.isRepeating());
                        header.setDefaultValue(pi.getDefaultValue());
                        header.setDescription(toString(pi.getDocumentations(),
                                pi.getDefaultValue()));
                        header.setName(pi.getName());
                        header.setEnumeration(new ArrayList<String>());
                        header.setRequired(pi.isRequired());

                        operation.getHeaders().add(header);
                    } else if (ParameterStyle.QUERY.equals(pi.getStyle())) {
                        QueryParameter queryParameter = new QueryParameter();
                        queryParameter.setAllowMultiple(pi.isRepeating());
                        queryParameter.setDefaultValue(pi.getDefaultValue());
                        queryParameter.setDescription(toString(
                                pi.getDocumentations(), pi.getDefaultValue()));
                        queryParameter.setName(pi.getName());
                        queryParameter.setEnumeration(new ArrayList<String>());
                        queryParameter.setRequired(pi.isRequired());

                        operation.getQueryParameters().add(queryParameter);
                    }
                }

                if (mi.getRequest() != null
                        && mi.getRequest().getRepresentations() != null
                        && !mi.getRequest().getRepresentations().isEmpty()) {
                    addRepresentations(mapReps, mi.getRequest()
                            .getRepresentations());

                    Entity entity = new Entity();
                    // TODO analyze
                    // The models differ : one representation / one variant
                    // for Restlet one representation / several variants for
                    // APIspark
                    entity.setType(mi.getRequest().getRepresentations().get(0)
                            .getType().getSimpleName());
                    entity.setArray(mi.getRequest().getRepresentations().get(0)
                            .isCollection());

                    operation.setInRepresentation(entity);
                }

                if (mi.getResponses() != null && !mi.getResponses().isEmpty()) {
                    operation.setResponses(new ArrayList<Response>());

                    Entity entity = new Entity();
                    // TODO analyze
                    // The models differ : one representation / one variant
                    // for Restlet one representation / several variants for
                    // APIspark
                    if (!mi.getResponse().getRepresentations().isEmpty()) {
                        entity.setType(mi.getResponse().getRepresentations()
                                .get(0).getType().getSimpleName());
                        entity.setArray(mi.getResponse().getRepresentations()
                                .get(0).isCollection());
                    }
                    operation.setOutRepresentation(entity);

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
                            response.setEntity(entity);
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

        //

        return applicationInfo;
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
     * Translates a ApplicationInfo to a {@link Definition} object.
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
            Reference ref = application.getResources().getBaseRef();
            if (ref != null) {
                result.getEndpoints().add(
                        new Endpoint(ref.getHostDomain(), ref.getHostPort(),
                                ref.getSchemeProtocol(), ref.getPath(), null));
            }

            Contract contract = new Contract();
            result.setContract(contract);
            contract.setDescription(toString(application.getDocumentations()));
            contract.setName(application.getName());
            if (contract.getName() == null || contract.getName().isEmpty()) {
                contract.setName(application.getClass().getName());
                LOGGER.log(Level.WARNING,
                        "Please provide a name to your application, used "
                                + contract.getName() + " by default.");
            }
            LOGGER.fine("Contract " + contract.getName() + " added.");

            // List of resources.
            contract.setResources(new ArrayList<Resource>());
            Map<String, RepresentationInfo> mapReps = new HashMap<String, RepresentationInfo>();
            addResources(application, contract, application.getResources()
                    .getResources(), result.getEndpoints().get(0).getUrl(), mapReps);

            java.util.List<String> protocols = new ArrayList<String>();
            for (ConnectorHelper<Server> helper : Engine.getInstance()
                    .getRegisteredServers()) {
                for (Protocol protocol : helper.getProtocols()) {
                    if (!protocols.contains(protocol.getName())) {
                        LOGGER.fine("Protocol " + protocol.getName()
                                + " added.");
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
            List<RepresentationInfo> toBeAdded = new ArrayList<RepresentationInfo>();
            // Initialize the list of classes to be anaylized
            for (RepresentationInfo ri : mapReps.values()) {
                if (ri.isRaw()) {
                    continue;
                }
                if (ri.isCollection()
                        && !mapReps.containsKey(ri.getType().getName())) {
                    // Check if the type has been described.
                    RepresentationInfo r = new RepresentationInfo(
                            ri.getMediaType());
                    r.setType(ri.getType());
                    toBeAdded.add(r);
                }
                // Parent class
                Class<?> parentType = ri.getType().getSuperclass();
                if (parentType != null && ReflectUtils.isJdkClass(parentType)) {
                    // TODO This type must introspected too, as it will reveal
                    // other representation
                    parentType = null;
                }
                if (parentType != null
                        && !mapReps.containsKey(parentType.getName())) {
                    RepresentationInfo r = new RepresentationInfo(
                            ri.getMediaType());
                    r.setType(parentType);
                    toBeAdded.add(r);
                }
                for (PropertyInfo pi : ri.getProperties()) {
                    if (pi.getType() != null
                            && !mapReps.containsKey(pi.getType().getName())
                            && !toBeAdded.contains(pi.getType())) {
                        RepresentationInfo r = new RepresentationInfo(
                                ri.getMediaType());
                        r.setType(pi.getType());
                        toBeAdded.add(r);
                    }
                }
            }
            // Second phase, discover classes and loop while classes are unknown
            while (!toBeAdded.isEmpty()) {
                RepresentationInfo[] tab = new RepresentationInfo[toBeAdded
                        .size()];
                toBeAdded.toArray(tab);
                toBeAdded.clear();
                for (int i = 0; i < tab.length; i++) {
                    RepresentationInfo current = tab[i];
                    if (!current.isRaw()
                            && !ReflectUtils.isJdkClass(current.getType())) {
                        if (!mapReps.containsKey(current.getName())) {
                            // TODO clearly something is wrong here. We should
                            // list all representations when discovering the
                            // method.
                            RepresentationInfo ri = RepresentationInfo
                                    .introspect(current.getType(), null,
                                            current.getMediaType());
                            mapReps.put(ri.getIdentifier(), ri);
                            // have a look at the parent type

                            Class<?> parentType = ri.getType().getSuperclass();
                            if (parentType != null
                                    && ReflectUtils.isJdkClass(parentType)) {
                                // TODO This type must introspected too, as it
                                // will reveal
                                // other representation
                                parentType = null;
                            }
                            if (parentType != null
                                    && !mapReps.containsKey(parentType
                                            .getName())) {
                                RepresentationInfo r = new RepresentationInfo(
                                        ri.getMediaType());
                                r.setType(parentType);
                                toBeAdded.add(r);
                            }
                            for (PropertyInfo prop : ri.getProperties()) {
                                if (prop.getType() != null
                                        && !mapReps.containsKey(prop.getType()
                                                .getName())
                                        && !toBeAdded.contains(prop.getType())) {
                                    RepresentationInfo r = new RepresentationInfo(
                                            ri.getMediaType());
                                    r.setType(prop.getType());
                                    toBeAdded.add(r);
                                }
                            }
                        }
                    }
                }
            }

            for (RepresentationInfo ri : mapReps.values()) {
                if (ri.isCollection()) {
                    continue;
                }
                LOGGER.fine("Representation " + ri.getName() + " added.");
                Representation rep = new Representation();

                // TODO analyze
                // The models differ : one representation / one variant for
                // Restlet
                // one representation / several variants for APIspark
                rep.setDescription(toString(ri.getDocumentations()));
                rep.setName(ri.getName());

                rep.setProperties(new ArrayList<Property>());
                for (PropertyInfo pi : ri.getProperties()) {
                    LOGGER.fine("Property " + pi.getName() + " added.");
                    Property p = new Property();
                    p.setDefaultValue(pi.getDefaultValue());
                    p.setDescription(pi.getDescription());
                    p.setMax(pi.getMax());
                    p.setMaxOccurs(pi.getMaxOccurs());
                    p.setMin(pi.getMin());
                    p.setMinOccurs(pi.getMinOccurs());
                    p.setName(pi.getName());
                    p.setEnumeration(pi.getEnumeration());
                    if (pi.getType() != null) {
                        // TODO: handle primitive type, etc
                        p.setType(pi.getType().getSimpleName());
                    }

                    p.setUniqueItems(pi.isUniqueItems());

                    rep.getProperties().add(p);
                }

                rep.setRaw(ri.isRaw() || ReflectUtils.isJdkClass(ri.getType()));
                contract.getRepresentations().add(rep);
            }
        }

        Collections.sort(result.getContract().getRepresentations(),
                new Comparator<Representation>() {

                    @Override
                    public int compare(Representation o1, Representation o2) {
                        return o1.getName().compareTo(o2.getName());
                    }

                });
        Collections.sort(result.getContract().getResources(),
                new Comparator<Resource>() {

                    @Override
                    public int compare(Resource o1, Resource o2) {
                        return o1.getResourcePath().compareTo(
                                o2.getResourcePath());
                    }

                });
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
        return toString(dis, "");
    }

    /**
     * Concats a list of {@link DocumentationInfo} instances as a single String.
     * 
     * @param dis
     *            The list of {@link DocumentationInfo} instances.
     * @return A String value.
     */
    private static String toString(List<DocumentationInfo> dis,
            String defaultValue) {
        if (dis != null && !dis.isEmpty()) {
            StringBuilder d = new StringBuilder();
            for (DocumentationInfo doc : dis) {
                if (doc.getTextContent() != null) {
                    d.append(doc.getTextContent());
                }
            }
            if (d.length() > 0) {
                return d.toString();
            }
        }

        return defaultValue;
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
     *            An component to introspect in order to get extra details such
     *            as the endpoint.
     * @param application
     *            An application to introspect.
     */
    public Introspector(Component component, Application application) {
        definition = toDefinition(getApplicationInfo(application, null));

        if (component != null && definition != null) {
            LOGGER.fine("Look for the endpoint.");
            Endpoint endpoint = null;
            // TODO What if the application is attached to several endpoints?
            // Look for the endpoint to which this application is attached.
            endpoint = getEndpoint(component.getDefaultHost(), application);
            for (int i = 0; endpoint == null && i < component.getHosts().size(); i++) {
                VirtualHost virtualHost = component.getHosts().get(i);
                endpoint = getEndpoint(virtualHost, application);
            }
            definition.getEndpoints().add(endpoint);
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
    private Endpoint getEndpoint(VirtualHost virtualHost, Application application) {
        Endpoint result = null;

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
                        result = new Endpoint(ref.getHostDomain(), ref.getHostPort(),
                                ref.getSchemeProtocol(), ref.getPath(), null);
                    }
                }
            }
        }
        return result;
    }

}