/**
 * Copyright 2005-2013 Restlet S.A.S.
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
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.swagger;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Directory;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Filter;
import org.restlet.routing.Route;
import org.restlet.routing.Router;
import org.restlet.routing.TemplateRoute;
import org.restlet.routing.VirtualHost;

import com.wordnik.swagger.core.Documentation;
import com.wordnik.swagger.core.DocumentationEndPoint;

/**
 * Swagger enabled application. This {@link Application} subclass can describe
 * itself in Swagger by introspecting its content, supporting the Swagger JSON
 * format. You can obtain this representation with an OPTIONS request addressed
 * exactly to the application URI (e.g. "http://host:port/path/to/application").
 * By default, the returned representation gleans the list of all attached
 * {@link ServerResource} classes and calls {@link #getName()} to get the title
 * and {@link #getDescription()} the textual content of the Swagger document
 * generated. This default behavior can be customized by overriding the
 * {@link #getDocumentation(Request, Response)} method.<br>
 * <br>
 * In case you want to customize the XSLT stylesheet, you can override the
 * {@link #createJsonRepresentation(Documentation)} method and return an
 * instance of an {@link SwaggerRepresentation} subclass overriding the
 * {@link SwaggerRepresentation#getHtmlRepresentation()} method.<br>
 * <br>
 * In addition, this class can create an instance and configure it with an
 * user-provided Swagger/XML document. In this case, it creates a root
 * {@link Router} and for each resource found in the Swagger document, it tries
 * to attach a {@link ServerResource} class to the router using its Swagger
 * path. For this, it looks up the qualified name of the {@link ServerResource}
 * subclass using the Swagger's "id" attribute of the "resource" elements. This
 * is the only Restlet specific convention on the original Swagger document.<br>
 * <br>
 * To attach an application configured in this way to an existing component, you
 * can call the {@link #attachToComponent(Component)} or the
 * {@link #attachToHost(VirtualHost)} methods. In this case, it uses the "base"
 * attribute of the Swagger "resources" element as the URI attachment path to
 * the virtual host.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables. <br>
 * 
 * @author Jerome Louvel
 */
public class SwaggerApplication extends Application {

    /**
     * Indicates if the application should be automatically described via
     * Swagger when an OPTIONS request handles a "*" target URI.
     */
    private volatile boolean autoDescribing;

    /** The Swagger base reference. */
    private volatile Reference baseRef;

    /** The router to {@link ServerResource} classes. */
    private volatile Router router;

    /**
     * Creates an application that can automatically introspect and expose
     * itself as with a Swagger description upon reception of an OPTIONS request
     * on the "*" target URI.
     */
    public SwaggerApplication() {
        this((Context) null);
    }

    /**
     * Creates an application that can automatically introspect and expose
     * itself as with a Swagger description upon reception of an OPTIONS request
     * on the "*" target URI.
     * 
     * @param context
     *            The context to use based on parent component context. This
     *            context should be created using the
     *            {@link Context#createChildContext()} method to ensure a proper
     *            isolation with the other applications.
     */
    public SwaggerApplication(Context context) {
        super(context);
        this.autoDescribing = true;
    }

    /**
     * Creates an application described using a Swagger document. Creates a
     * router where Resource classes are attached and set it as the root
     * Restlet.
     * 
     * By default the application is not automatically described. If you want
     * to, you can call {@link #setAutoDescribing(boolean)}.
     * 
     * @param context
     *            The context to use based on parent component context. This
     *            context should be created using the
     *            {@link Context#createChildContext()} method to ensure a proper
     *            isolation with the other applications.
     * @param rep
     *            The Swagger description document.
     */
    public SwaggerApplication(Context context, Representation rep) {
        super(context);
        this.autoDescribing = false;

        try {
            // Instantiates a SwaggerRepresentation of the Swagger document
            SwaggerRepresentation swaggerRep = null;

            if (rep instanceof SwaggerRepresentation) {
                swaggerRep = (SwaggerRepresentation) rep;
            } else {
                swaggerRep = new SwaggerRepresentation(rep);
            }

            final Router root = new Router(getContext());
            this.router = root;
            setInboundRoot(root);

            if (swaggerRep.getDocumentation() != null) {
                if (swaggerRep.getDocumentation().getApis() != null) {

                    for (DocumentationEndPoint endPoint : swaggerRep
                            .getDocumentation().getApis()) {
                        attachResource(endPoint, null, this.router);
                    }

                    // Analyzes the Swagger resources base
                    setBaseRef(new Reference(swaggerRep.getDocumentation()
                            .getBasePath()));
                }

                // Set the name of the application as the title of the first
                // documentation tag.
                if (!swaggerRep.getDocumentation().getModels().isEmpty()) {
                    setName(swaggerRep.getDocumentation().getModels().get(0)
                            .getName());
                }
            }
        } catch (Exception e) {
            getLogger()
                    .log(Level.WARNING,
                            "Error during the attachment of the Swagger application",
                            e);
        }
    }

    /**
     * Creates an application described using a Swagger document. Creates a
     * router where Resource classes are attached and set it as the root
     * Restlet.
     * 
     * By default the application is not automatically described. If you want
     * to, you can call {@link #setAutoDescribing(boolean)}.
     * 
     * @param swagger
     *            The Swagger description document.
     */
    public SwaggerApplication(Representation swaggerRepresentation) {
        this(null, swaggerRepresentation);
    }

    /**
     * Adds the necessary server connectors to the component.
     * 
     * @param component
     *            The parent component to update.
     */
    private void addConnectors(Component component) {
        // Create the server connector
        Protocol protocol = getBaseRef().getSchemeProtocol();
        int port = getBaseRef().getHostPort();
        boolean exists = false;

        if (port == -1) {
            for (Server server : component.getServers()) {
                if (server.getProtocols().contains(protocol)
                        && (server.getPort() == protocol.getDefaultPort())) {
                    exists = true;
                }
            }

            if (!exists) {
                component.getServers().add(protocol);
            }
        } else {
            for (Server server : component.getServers()) {
                if (server.getProtocols().contains(protocol)
                        && (server.getPort() == port)) {
                    exists = true;
                }
            }

            if (!exists) {
                component.getServers().add(protocol, port);
            }
        }
    }

    /**
     * Attaches a resource, as specified in a Swagger document, to a specified
     * router, then recursively attaches its child resources.
     * 
     * @param currentResource
     *            The resource to attach.
     * @param parentResource
     *            The parent resource. Needed to correctly resolve the "path" of
     *            the resource. Should be null if the resource is root-level.
     * @param router
     *            The router to which to attach the resource and its children.
     * @throws ClassNotFoundException
     *             If the class name specified in the "id" attribute of the
     *             resource does not exist, this exception will be thrown.
     */
    private void attachResource(DocumentationEndPoint currentEndPoint,
            DocumentationEndPoint parentEndPoint, Router router)
            throws ClassNotFoundException {

        String uriPattern = currentEndPoint.getPath();

        // If there is a parentResource, add its uriPattern to this one
        if (parentEndPoint != null) {
            String parentUriPattern = parentEndPoint.getPath();

            if ((parentUriPattern.endsWith("/") == false)
                    && (uriPattern.startsWith("/") == false)) {
                parentUriPattern += "/";
            }

            uriPattern = parentUriPattern + uriPattern;
            currentEndPoint.setPath(uriPattern);
        } else if (!uriPattern.startsWith("/")) {
            uriPattern = "/" + uriPattern;
            currentEndPoint.setPath(uriPattern);
        }

        Finder finder = createFinder(router, uriPattern, currentEndPoint);

        if (finder != null) {
            // Attach the resource itself
            router.attach(uriPattern, finder);
        }

        // TODO are children resources supported?
        // Attach children of the resource
        // for (ResourceInfo childResource :
        // currentEndPoint.getChildResources()) {
        // attachResource(childResource, currentResource, router);
        // }
    }

    /**
     * Attaches the application to the given component if the application has a
     * Swagger base reference. The application will be attached to an existing
     * virtual host if possible, otherwise a new one will be created.
     * 
     * @param component
     *            The parent component to update.
     * @return The parent virtual host.
     */
    public VirtualHost attachToComponent(Component component) {
        VirtualHost result = null;

        if (getBaseRef() != null) {
            // Create the virtual host
            result = getVirtualHost(component);

            // Attach the application to the virtual host
            attachToHost(result);

            // Adds the necessary server connectors
            addConnectors(component);
        } else {
            getLogger()
                    .warning(
                            "The Swagger application has no base reference defined. Unable to guess the virtual host.");
        }

        return result;
    }

    /**
     * Attaches the application to the given host using the Swagger base
     * reference.
     * 
     * @param host
     *            The virtual host to attach to.
     */
    public void attachToHost(VirtualHost host) {
        if (getBaseRef() != null) {
            final String path = getBaseRef().getPath();
            if (path == null) {
                host.attach("", this);
            } else {
                host.attach(path, this);
            }

        } else {
            getLogger()
                    .warning(
                            "The Swagger application has no base reference defined. Unable to guess the virtual host.");
        }
    }

    /**
     * Indicates if the application and all its resources can be described using
     * Swagger.
     * 
     * @param remainingPart
     *            The URI remaining part.
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    protected boolean canDescribe(String remainingPart, Request request,
            Response response) {
        return isAutoDescribing()
                && Method.OPTIONS.equals(request.getMethod())
                && (response.getStatus().isClientError() || !response
                        .isEntityAvailable())
                && ("/".equals(remainingPart) || "".equals(remainingPart));
    }

    /**
     * Creates a finder for the given resource info. By default, it looks up for
     * an "id" attribute containing a fully qualified class name.
     * 
     * @param router
     *            The parent router.
     * @param resourceInfo
     *            The Swagger resource descriptor.
     * @return The created finder.
     * @throws ClassNotFoundException
     */
    @SuppressWarnings("unchecked")
    protected Finder createFinder(Router router, String uriPattern,
            DocumentationEndPoint resourceInfo) throws ClassNotFoundException {
        Finder result = null;

        // TODO swagger parsing.
        // if (resourceInfo.getIdentifier() != null) {
        // // The "id" attribute conveys the target class name
        // Class<? extends ServerResource> targetClass = (Class<? extends
        // ServerResource>) Engine
        // .loadClass(resourceInfo.getIdentifier());
        // result = router.createFinder(targetClass);
        // } else {
        // getLogger()
        // .fine("Unable to find the 'id' attribute of the resource element with this path attribute \""
        // + uriPattern + "\"");
        // }

        return result;
    }

    /**
     * Creates a new HTML representation for a given {@link Documentation}
     * instance describing an application.
     * 
     * @param documentation
     *            The application description.
     * @return The created {@link SwaggerRepresentation}.
     */
    protected static Representation createHtmlRepresentation(
            Documentation documentation) {
        return new SwaggerRepresentation(documentation).getHtmlRepresentation();
    }

    /**
     * Creates a new Swagger representation for a given {@link Documentation}
     * instance describing an application.
     * 
     * @param Documentation
     *            The application description.
     * @return The created {@link SwaggerRepresentation}.
     */
    protected static Representation createJsonRepresentation(
            Documentation documentation) {
        return new SwaggerRepresentation(documentation);
    }

    /**
     * Creates a new Swagger representation for a given {@link Documentation}
     * instance describing an application.
     * 
     * @param documentation
     *            The application description.
     * @return The created {@link SwaggerRepresentation}.
     */
    protected static Representation createXmlRepresentation(
            Documentation documentation) {
        return new SwaggerRepresentation(documentation);
    }

    /**
     * Returns a Swagger description of the current application. By default,
     * this method discovers all the resources attached to this application. It
     * can be overridden to add documentation, list of representations, etc.
     * 
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return An application description.
     */
    protected Documentation getDocumentation(Request request, Response response) {
        Documentation documentation = new Documentation();
        documentation.setBasePath(request.getResourceRef().getBaseRef()
                .toString());
        documentation.setApis(getEndPoints(documentation,
                getNextRouter(getInboundRoot()), request, response));
        return documentation;
    }

    /**
     * Returns the Swagger base reference.
     * 
     * @return The Swagger base reference.
     */
    public Reference getBaseRef() {
        return this.baseRef;
    }

    /**
     * Returns the next router available.
     * 
     * @param current
     *            The current Restlet to inspect.
     * @return The first router available.
     */
    private Router getNextRouter(Restlet current) {
        Router result = getRouter();

        if (result == null) {
            if (current instanceof Router) {
                result = (Router) current;
            } else if (current instanceof Filter) {
                result = getNextRouter(((Filter) current).getNext());
            }
        }

        return result;
    }

    /**
     * Returns the preferred Swagger variant according to the client preferences
     * specified in the request.
     * 
     * @param request
     *            The request including client preferences.
     * @return The preferred Swagger variant.
     */
    protected Variant getPreferredSwaggerVariant(Request request) {
        return getConnegService().getPreferredVariant(getSwaggerVariants(),
                request, getMetadataService());
    }

    /**
     * Completes the data available about a given Filter instance.
     * 
     * @param Documentation
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
    private DocumentationEndPoint getEndPoint(Documentation documentation,
            Filter filter, String path, Request request, Response response) {
        return getEndPoint(documentation, filter.getNext(), path, request,
                response);
    }

    /**
     * Completes the data available about a given Finder instance.
     * 
     * @param Documentation
     *            The parent application.
     * @param resourceInfo
     *            The ResourceInfo object to complete.
     * @param finder
     *            The Finder instance to document.
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     */
    private DocumentationEndPoint getEndPoint(Documentation documentation,
            Finder finder, String path, Request request, Response response) {
        DocumentationEndPoint result = null;
        Object resource = null;

        // Save the current application
        Application.setCurrent(this);

        if (finder instanceof Directory) {
            resource = finder;
        } else {
            // The handler instance targeted by this finder.
            ServerResource sr = finder.find(request, response);

            if (sr != null) {
                sr.init(getContext(), request, response);
                sr.updateAllowedMethods();
                resource = sr;
            }
        }

        if (resource != null) {
            result = new DocumentationEndPoint();
            SwaggerServerResource.describe(documentation, result, resource,
                    path);
        }

        return result;
    }

    /**
     * Completes the data available about a given Restlet instance.
     * 
     * @param Documentation
     *            The parent application.
     * @param resourceInfo
     *            The ResourceInfo object to complete.
     * @param restlet
     *            The Restlet instance to document.
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     */
    private DocumentationEndPoint getEndPoint(Documentation Documentation,
            Restlet restlet, String path, Request request, Response response) {
        DocumentationEndPoint result = null;

        if (restlet instanceof SwaggerDescribable) {
            result = ((SwaggerDescribable) restlet)
                    .getDocumentationEndPoint(Documentation);
            result.setPath(path);
        } else if (restlet instanceof Finder) {
            result = getEndPoint(Documentation, (Finder) restlet, path,
                    request, response);
        } else if (restlet instanceof Router) {
            result = new DocumentationEndPoint();
            result.setPath(path);
            // TODO Can't handle resources as a tree.
            // result.setChildResources(getResourceInfos(Documentation,
            // (Router) restlet, request, response));
        } else if (restlet instanceof Filter) {
            result = getEndPoint(Documentation, (Filter) restlet, path,
                    request, response);
        }

        return result;
    }

    /**
     * Returns the Swagger data about the given Route instance.
     * 
     * @param Documentation
     *            The parent application.
     * @param route
     *            The Route instance to document.
     * @param basePath
     *            The base path.
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return The Swagger data about the given Route instance.
     */
    private DocumentationEndPoint getEndPoint(Documentation Documentation,
            Route route, String basePath, Request request, Response response) {
        DocumentationEndPoint result = null;

        if (route instanceof TemplateRoute) {
            TemplateRoute templateRoute = (TemplateRoute) route;
            String path = templateRoute.getTemplate().getPattern();

            // Swagger requires resource paths to be relative to parent path
            if (path.startsWith("/") && basePath.endsWith("/")) {
                path = path.substring(1);
            }

            result = getEndPoint(Documentation, route.getNext(), path, request,
                    response);
        }

        return result;
    }

    /**
     * Completes the list of ResourceInfo instances for the given Router
     * instance.
     * 
     * @param Documentation
     *            The parent application.
     * @param router
     *            The router to document.
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return The list of ResourceInfo instances to complete.
     */
    private List<DocumentationEndPoint> getEndPoints(
            Documentation documentation, Router router, Request request,
            Response response) {
        List<DocumentationEndPoint> result = new ArrayList<DocumentationEndPoint>();

        for (Route route : router.getRoutes()) {
            DocumentationEndPoint endPoint = getEndPoint(documentation, route,
                    "/", request, response);

            if (endPoint != null) {
                result.add(endPoint);
            }
        }

        if (router.getDefaultRoute() != null) {
            DocumentationEndPoint endPoint = getEndPoint(documentation,
                    router.getDefaultRoute(), "/", request, response);
            if (endPoint != null) {
                result.add(endPoint);
            }
        }

        return result;
    }

    /**
     * Returns the router where the {@link ServerResource} classes created from
     * the Swagger description document are attached.
     * 
     * @return The root router.
     */
    public Router getRouter() {
        return this.router;
    }

    /**
     * Returns the virtual host matching the Swagger application's base
     * reference. Creates a new one and attaches it to the component if
     * necessary.
     * 
     * @param component
     *            The parent component.
     * @return The related virtual host.
     */
    private VirtualHost getVirtualHost(Component component) {
        // Create the virtual host if necessary
        final String hostDomain = this.baseRef.getHostDomain();
        final String hostPort = Integer.toString(this.baseRef.getHostPort());
        final String hostScheme = this.baseRef.getScheme();

        VirtualHost host = null;
        for (final VirtualHost vh : component.getHosts()) {
            if (vh.getHostDomain().equals(hostDomain)
                    && vh.getHostPort().equals(hostPort)
                    && vh.getHostScheme().equals(hostScheme)) {
                host = vh;
            }
        }

        if (host == null) {
            // A new virtual host needs to be created
            host = new VirtualHost(component.getContext().createChildContext());
            host.setHostDomain(hostDomain);
            host.setHostPort(hostPort);
            host.setHostScheme(hostScheme);
            component.getHosts().add(host);
        }

        return host;
    }

    /**
     * Returns the available Swagger variants.
     * 
     * @return The available Swagger variants.
     */
    protected List<Variant> getSwaggerVariants() {
        final List<Variant> result = new ArrayList<Variant>();
        result.add(new Variant(MediaType.APPLICATION_JSON));
        result.add(new Variant(MediaType.APPLICATION_XML));
        result.add(new Variant(MediaType.TEXT_XML));
        return result;
    }

    /**
     * Handles the requests normally in all cases then handles the special case
     * of the OPTIONS requests that exactly target the application. In this
     * case, the application is automatically introspected and described as a
     * Swagger representation based on the result of the
     * {@link #getDocumentation(Request, Response)} method.<br>
     * The automatic introspection happens only if the request hasn't already
     * been successfully handled. That is to say, it lets users provide their
     * own handling of OPTIONS requests.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        // Preserve the resource reference.
        Reference rr = request.getResourceRef().clone();

        // Do the regular handling
        super.handle(request, response);

        // Restore the resource reference
        request.setResourceRef(rr);

        // Handle OPTIONS requests.
        String rp = rr.getRemainingPart(false, false);

        if (canDescribe(rp, request, response)) {
            // Make sure that the base of the "resources" element ends with a
            // "/".
            if (!rr.getBaseRef().getIdentifier().endsWith("/")) {
                rr.setBaseRef(rr.getBaseRef() + "/");
            }

            // Returns a Swagger representation of the application.
            response.setEntity(swaggerRepresent(request, response));

            if (response.isEntityAvailable()) {
                response.setStatus(Status.SUCCESS_OK);
            }
        }
    }

    /**
     * Indicates if the application should be automatically described via
     * Swagger when an OPTIONS request handles a "*" target URI.
     * 
     * @return True if the application should be automatically described via
     *         Swagger.
     */
    public boolean isAutoDescribing() {
        return autoDescribing;
    }

    /**
     * Indicates if the application should be automatically described via
     * Swagger when an OPTIONS request handles a "*" target URI.
     * 
     * @param autoDescribed
     *            True if the application should be automatically described via
     *            Swagger.
     */
    public void setAutoDescribing(boolean autoDescribed) {
        this.autoDescribing = autoDescribed;
    }

    /**
     * Sets the Swagger base reference.
     * 
     * @param baseRef
     *            The Swagger base reference.
     */
    public void setBaseRef(Reference baseRef) {
        this.baseRef = baseRef;
    }

    /**
     * Represents the resource as a Swagger description.
     * 
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return The Swagger description.
     */
    protected Representation swaggerRepresent(Request request, Response response) {
        return swaggerRepresent(getPreferredSwaggerVariant(request), request,
                response);
    }

    /**
     * Represents the resource as a Swagger description for the given variant.
     * 
     * @param variant
     *            The Swagger variant.
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return The Swagger description.
     */
    protected Representation swaggerRepresent(Variant variant, Request request,
            Response response) {
        Representation result = null;

        if (variant != null) {
            Documentation documentation = getDocumentation(request, response);

            // TODO there is no equivalent.
            // DocumentationInfo doc = null;
            //
            // if ((getName() != null) && !"".equals(getName())) {
            // if (documentation.getDocumentations().isEmpty()) {
            // doc = new DocumentationInfo();
            // documentation.getDocumentations().add(doc);
            // } else {
            // doc = documentation.getDocumentations().get(0);
            // }
            //
            // doc.setTitle(getName());
            // }
            //
            // if ((doc != null) && (getDescription() != null)
            // && !"".equals(getDescription())) {
            // doc.setTextContent(getDescription());
            // }

            if (MediaType.APPLICATION_JSON.equals(variant.getMediaType())) {
                result = createJsonRepresentation(documentation);
            } else if (MediaType.TEXT_HTML.equals(variant.getMediaType())) {
                result = createXmlRepresentation(documentation);
            } else if (MediaType.TEXT_HTML.equals(variant.getMediaType())) {
                result = createHtmlRepresentation(documentation);
            }
        }

        return result;
    }

}
