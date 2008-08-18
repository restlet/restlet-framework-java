/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.wadl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.Filter;
import org.restlet.Finder;
import org.restlet.Handler;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.Server;
import org.restlet.VirtualHost;
import org.restlet.data.ClientInfo;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.Resource;
import org.restlet.resource.Variant;

/**
 * WADL configured application. Can automatically configure itself given a WADL
 * description document.<br>
 * <br>
 * It creates a root router and for each resource found in the WADL document, it
 * tries to attach a Restlet Resource class to the router using its WADL path.<br>
 * <br>
 * It looks up the qualified name of the Resource class using the WADL "id"
 * attribute of the "resource" elements. This is the only Restlet specific
 * constraint on the WADL document.<br>
 * <br>
 * Also, it has an {@link #attachToComponent(Component)} to attach the
 * application to an existing component and a {@link #attachToHost(VirtualHost)}
 * to attach it to an existing virtual host using the "base" attribute of the
 * WADL "resources" element.<br>
 * Such application is also able to generate a description of itself under two
 * formats: WADL or HTML (the latter is actually a transformation of the
 * former). You can obtain this representation with an OPTIONS request addressed
 * to the "*" URI (e.g. "http://host:port/path/to/application/*"). By default,
 * the returned representation gleans the list of all attached Resources. This
 * default behaviour can be customized by overriding the getApplicationInfo()
 * method.<br>
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables. <br>
 * 
 * @author Jerome Louvel
 */
public class WadlApplication extends Application {

    /**
     * Indicates if the application should be automatically described via WADL
     * when an OPTIONS request handles a "*" target URI.
     */
    private volatile boolean autoDescribed;

    /** The WADL base reference. */
    private volatile Reference baseRef;

    /** The router to Resource classes. */
    private volatile Router router;

    /**
     * Creates an application that can automatically introspect and expose
     * itself as with a WADL description upon reception of an OPTIONS request on
     * the "*" target URI.
     */
    public WadlApplication() {
        this((Context) null);
    }

    /**
     * Creates an application that can automatically introspect and expose
     * itself as with a WADL description upon reception of an OPTIONS request on
     * the "*" target URI.
     * 
     * @param context
     *            The context to use based on parent component context. This
     *            context should be created using the
     *            {@link Context#createChildContext()} method to ensure a proper
     *            isolation with the other applications.
     */
    public WadlApplication(Context context) {
        super(context);
        this.autoDescribed = true;
    }

    /**
     * Creates an application described using a WADL document. Creates a router
     * where Resource classes are attached and set it as the root Restlet.
     * 
     * By default the application is not automatically described. If you want
     * to, you can call {@link #setAutoDescribed(boolean)}.
     * 
     * @param context
     *            The context to use based on parent component context. This
     *            context should be created using the
     *            {@link Context#createChildContext()} method to ensure a proper
     *            isolation with the other applications.
     * @param wadl
     *            The WADL description document.
     */
    public WadlApplication(Context context, Representation wadl) {
        super(context);
        this.autoDescribed = false;

        try {
            // Instantiates a WadlRepresentation of the WADL document
            WadlRepresentation wadlRep = null;
            if (wadl instanceof WadlRepresentation) {
                wadlRep = (WadlRepresentation) wadl;
            } else {
                wadlRep = new WadlRepresentation(wadl);
            }

            final Router root = new Router(getContext());
            this.router = root;
            setRoot(root);

            if ((wadlRep.getApplication() != null)
                    && (wadlRep.getApplication().getResources() != null)) {
                for (final ResourceInfo resource : wadlRep.getApplication()
                        .getResources().getResources()) {
                    attachResource(resource, null, this.router);
                }

                // Analyzes the WADL resources base
                setBaseRef(wadlRep.getApplication().getResources().getBaseRef());
            }
        } catch (final Exception e) {
            getLogger().log(Level.WARNING,
                    "Error during the attachment of the WADL application", e);
        }
    }

    /**
     * Creates an application described using a WADL document. Creates a router
     * where Resource classes are attached and set it as the root Restlet.
     * 
     * By default the application is not automatically described. If you want
     * to, you can call {@link #setAutoDescribed(boolean)}.
     * 
     * @param wadl
     *            The WADL description document.
     */
    public WadlApplication(Representation wadl) {
        this(null, wadl);
    }

    /**
     * Adds the necessary server connectors to the component.
     * 
     * @param component
     *            The parent component to update.
     */
    private void addConnectors(Component component) {
        // Create the server connector
        final Protocol protocol = getBaseRef().getSchemeProtocol();
        final int port = getBaseRef().getHostPort();
        boolean exists = false;

        if (port == -1) {
            for (final Server server : component.getServers()) {
                if (server.getProtocols().contains(protocol)
                        && (server.getPort() == protocol.getDefaultPort())) {
                    exists = true;
                }
            }

            if (!exists) {
                component.getServers().add(protocol);
            }
        } else {
            for (final Server server : component.getServers()) {
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
     * Attaches a resource, as specified in a WADL document, to a specified
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
    @SuppressWarnings("unchecked")
    private void attachResource(ResourceInfo currentResource,
            ResourceInfo parentResource, Router router)
            throws ClassNotFoundException {

        String uriPattern = currentResource.getPath();

        if (currentResource.getIdentifier() != null) {
            // if there's a parentResource, add it's uriPattern to this one
            if (parentResource != null) {
                String parentUriPattern = parentResource.getPath();

                if ((parentUriPattern.endsWith("/") == false)
                        && (uriPattern.startsWith("/") == false)) {
                    parentUriPattern += "/";
                }

                uriPattern = parentUriPattern + uriPattern;

                // set thisResource's 'path' attribute to the new uriPattern so
                // child resources will be able to use it
                currentResource.setPath(uriPattern);
            }

            // The "id" attribute conveys the target class name
            final Class targetClass = Class.forName(currentResource
                    .getIdentifier());

            // Attach the resource itself
            router.attach(uriPattern, targetClass);

            // Attach any children of the resource
            for (final ResourceInfo childResource : currentResource
                    .getChildResources()) {
                attachResource(childResource, currentResource, router);
            }
        } else {
            getLogger()
                    .warning(
                            "Unable to find the 'id' attribute of the resource element with this path attribute \""
                                    + uriPattern + "\"");
        }
    }

    /**
     * Attaches the application to the given component if the application has a
     * WADL base reference. The application will be attached to an existing
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
                            "The WADL application has no base reference defined. Unable to guess the virtual host.");
        }

        return result;
    }

    /**
     * Attaches the application to the given host using the WADL base reference.
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
                            "The WADL application has no base reference defined. Unable to guess the virtual host.");
        }
    }

    /**
     * Returns a WADL description of the current application. By default, this
     * method discovers all the resources attached to this application. It can
     * be overriden to add documentation, list of representations, etc.
     * 
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return An application description.
     */
    public ApplicationInfo getApplicationInfo(Request request, Response response) {
        final ApplicationInfo applicationInfo = new ApplicationInfo();
        applicationInfo.getResources().setBaseRef(
                request.getResourceRef().getBaseRef());
        applicationInfo.getResources().setResources(
                getResourceInfos(getFirstRouter(getRoot()), request, response));
        return applicationInfo;
    }

    /**
     * Returns the WADL base reference.
     * 
     * @return The WADL base reference.
     */
    public Reference getBaseRef() {
        return this.baseRef;
    }

    /**
     * Returns the first router available.
     * 
     * @param current
     *            The current Restlet to inspect.
     * @return The first router available.
     */
    private Router getFirstRouter(Restlet current) {
        Router result = getRouter();

        if (result == null) {
            if (current instanceof Router) {
                result = (Router) current;
            } else if (current instanceof Filter) {
                result = getFirstRouter(((Filter) current).getNext());
            }
        }

        return result;
    }

    /**
     * Returns the preferred WADL variant according to the client preferences
     * specified in the request.
     * 
     * @param clientInfo
     *            The client preferences and info.
     * @return The preferred WADL variant.
     */
    protected Variant getPreferredWadlVariant(ClientInfo clientInfo) {
        Variant result = null;

        // Compute the preferred variant. Get the default language
        // preference from the Application (if any).
        final Application app = Application.getCurrent();
        Language language = null;

        if (app != null) {
            language = app.getMetadataService().getDefaultLanguage();
        }

        result = clientInfo.getPreferredVariant(getWadlVariants(), language);
        return result;
    }

    /**
     * Completes the data available about a given Filter instance.
     * 
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
    private ResourceInfo getResourceInfo(Filter filter, String path,
            Request request, Response response) {
        return getResourceInfo(filter.getNext(), path, request, response);
    }

    /**
     * Completes the data available about a given Finder instance.
     * 
     * @param resourceInfo
     *            The ResourceInfo object to complete.
     * @param finder
     *            The Finder instance to document.
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     */
    private ResourceInfo getResourceInfo(Finder finder, String path,
            Request request, Response response) {
        ResourceInfo result = null;

        // Save the current application
        Application.setCurrent(this);
        // The handler instance targeted by this finder.
        final Handler handler = finder.createTarget(finder.getTargetClass(),
                request, response);

        if (handler instanceof WadlResource) {
            // This kind of resource gives more information
            final WadlResource resource = (WadlResource) handler;
            result = new ResourceInfo();
            resource.describe(path, result);
        } else {
            result = new ResourceInfo();
            result.setPath(path);

            // The set of allowed methods
            final List<Method> methods = new ArrayList<Method>();
            if (handler != null) {
                methods.addAll(handler.getAllowedMethods());
            } else {
                if (finder instanceof Directory) {
                    Directory directory = (Directory) finder;
                    methods.add(Method.GET);
                    if (directory.isModifiable()) {
                        methods.add(Method.DELETE);
                        methods.add(Method.PUT);
                    }
                }
            }

            Collections.sort(methods, new Comparator<Method>() {
                public int compare(Method m1, Method m2) {
                    return m1.getName().compareTo(m2.getName());
                }
            });

            if (handler instanceof Resource) {
                final Resource resource = (Resource) handler;

                for (final Method method : methods) {
                    final MethodInfo methodInfo = new MethodInfo();
                    methodInfo.setName(method);
                    // Can document the list of supported variants.
                    if (Method.GET.equals(method)) {
                        final ResponseInfo responseInfo = new ResponseInfo();
                        for (final Variant variant : resource.getVariants()) {
                            final RepresentationInfo representationInfo = new RepresentationInfo();
                            representationInfo.setMediaType(variant
                                    .getMediaType());
                            responseInfo.getRepresentations().add(
                                    representationInfo);
                        }
                        methodInfo.setResponse(responseInfo);
                    }

                    result.getMethods().add(methodInfo);
                }
            } else {
                // Can only give information about the list of allowed
                // methods.
                for (final Method method : methods) {
                    final MethodInfo methodInfo = new MethodInfo();
                    methodInfo.setName(method);
                    result.getMethods().add(methodInfo);
                }
            }
        }

        return result;
    }

    /**
     * Completes the data available about a given Restlet instance.
     * 
     * @param resourceInfo
     *            The ResourceInfo object to complete.
     * @param restlet
     *            The Restlet instance to document.
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     */
    private ResourceInfo getResourceInfo(Restlet restlet, String path,
            Request request, Response response) {
        ResourceInfo result = null;

        if (restlet instanceof WadlDescribable) {
            result = ((WadlDescribable) restlet).getResourceInfo();
            result.setPath(path);
        } else if (restlet instanceof Finder) {
            result = getResourceInfo((Finder) restlet, path, request, response);
        } else if (restlet instanceof Router) {
            result = new ResourceInfo();
            result.setPath(path);
            result.setChildResources(getResourceInfos((Router) restlet,
                    request, response));
        } else if (restlet instanceof Filter) {
            result = getResourceInfo((Filter) restlet, path, request, response);
        }
        return result;
    }

    /**
     * Returns the WADL data about the given Route instance.
     * 
     * @param route
     *            The Route instance to document.
     * @param basePath
     *            The base path.
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return The WADL data about the given Route instance.
     */
    private ResourceInfo getResourceInfo(Route route, String basePath,
            Request request, Response response) {
        String path = route.getTemplate().getPattern();

        // WADL requires resource paths to be relative to parent path
        if (path.startsWith("/") && basePath.endsWith("/")) {
            path = path.substring(1);
        }

        final ResourceInfo result = getResourceInfo(route.getNext(), path,
                request, response);
        return result;
    }

    /**
     * Completes the list of ResourceInfo instances for the given Router
     * instance.
     * 
     * @param router
     *            The router to document.
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return The list of ResourceInfo instances to complete.
     */
    private List<ResourceInfo> getResourceInfos(Router router, Request request,
            Response response) {
        List<ResourceInfo> result = new ArrayList<ResourceInfo>();

        for (final Route route : router.getRoutes()) {
            ResourceInfo resourceInfo = getResourceInfo(route, "/", request,
                    response);
            if (resourceInfo != null) {
                result.add(resourceInfo);
            }
        }

        return result;
    }

    /**
     * Returns the router where the Resources created from the WADL description
     * document are attached.
     * 
     * @return The root router.
     */
    public Router getRouter() {
        return this.router;
    }

    /**
     * Returns the virtual host matching the WADL application's base reference.
     * Creates a new one and attaches it to the component if necessary.
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
     * Returns the available WADL variants.
     * 
     * @return The available WADL variants.
     */
    protected List<Variant> getWadlVariants() {
        final List<Variant> result = new ArrayList<Variant>();
        result.add(new Variant(MediaType.APPLICATION_WADL_XML));
        result.add(new Variant(MediaType.TEXT_HTML));
        return result;
    }

    /**
     * Handles the requests normally in all cases then handles handle the
     * special case of the OPTIONS methods with "*" as the target resource
     * reference value. In this case, the application is automatically
     * introspected and described as a WADL representation based on the result
     * of the {@link #getApplicationInfo(Request, Response)} method.<br>
     * The automatic introspection happens only if the request hasn't already
     * been successfully handled. That is to say, it lets users to provide their
     * own handling of OPTIONS requests.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);

        // Handle OPTIONS requests.
        Reference rr = request.getResourceRef();
        String rp = rr.getRemainingPart(false, false);
        if (isAutoDescribed()
                && Method.OPTIONS.equals(request.getMethod())
                && (Status.CLIENT_ERROR_NOT_FOUND.equals(response.getStatus()) || !response
                        .isEntityAvailable())
                && ("/".equals(rp) || "".equals(rp))) {
            // Make sure that the base of the "resources" element ends with a
            // "/".
            if (!rr.getBaseRef().getIdentifier().endsWith("/")) {
                rr.setBaseRef(rr.getBaseRef() + "/");
            }

            // Returns a WADL representation of the application.
            response.setEntity(wadlRepresent(request, response));
            if (response.isEntityAvailable()) {
                response.setStatus(Status.SUCCESS_OK);
            }
        }
    }

    /**
     * Indicates if the application should be automatically described via WADL
     * when an OPTIONS request handles a "*" target URI.
     * 
     * @return True if the application should be automatically described via
     *         WADL.
     */
    public boolean isAutoDescribed() {
        return autoDescribed;
    }

    /**
     * Indicates if the application should be automatically described via WADL
     * when an OPTIONS request handles a "*" target URI.
     * 
     * @param autoDescribed
     *            True if the application should be automatically described via
     *            WADL.
     */
    public void setAutoDescribed(boolean autoDescribed) {
        this.autoDescribed = autoDescribed;
    }

    /**
     * Sets the WADL base reference.
     * 
     * @param baseRef
     *            The WADL base reference.
     */
    public void setBaseRef(Reference baseRef) {
        this.baseRef = baseRef;
    }

    /**
     * Represents the resource as a WADL description.
     * 
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return The WADL description.
     */
    protected Representation wadlRepresent(Request request, Response response) {
        return wadlRepresent(getPreferredWadlVariant(request.getClientInfo()),
                request, response);
    }

    /**
     * Represents the resource as a WADL description for the given variant.
     * 
     * @param variant
     *            The WADL variant.
     * @param request
     *            The current request.
     * @param response
     *            The current response.
     * @return The WADL description.
     */
    public Representation wadlRepresent(Variant variant, Request request,
            Response response) {
        Representation result = null;

        if (variant != null) {
            if (MediaType.APPLICATION_WADL_XML.equals(variant.getMediaType())) {
                result = new WadlRepresentation(getApplicationInfo(request,
                        response));
            } else if (MediaType.TEXT_HTML.equals(variant.getMediaType())) {
                result = new WadlRepresentation(getApplicationInfo(request,
                        response)).getHtmlRepresentation();
            }
        }

        return result;
    }

}
