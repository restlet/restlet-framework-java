/*
 * Copyright 2005-2008 Noelios Consulting.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
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
import org.restlet.Filter;
import org.restlet.Finder;
import org.restlet.Handler;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.Server;
import org.restlet.VirtualHost;
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
 * WADL "resources" element.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class WadlApplication extends Application {

    /** The WADL base reference. */
    private volatile Reference baseRef;

    /** The router to Resource classes. */
    private volatile Router router;

    /**
     * Creates an application described using a WADL document. Creates a router
     * where Resource classes are attached and set it as the root Restlet.
     * 
     * @param parentContext
     *                The parent component context.
     * @param wadl
     *                The WADL description document.
     */
    public WadlApplication(Context parentContext, Representation wadl) {
        super(parentContext);

        try {
            // Instantiates a WadlRepresentation of the WADL document
            WadlRepresentation wadlRep = null;
            if (wadl instanceof WadlRepresentation) {
                wadlRep = (WadlRepresentation) wadl;
            } else {
                wadlRep = new WadlRepresentation(wadl);
            }

            Router root = new Router(getContext());
            this.router = root;
            setRoot(root);

            if (wadlRep.getApplication() != null
                    && wadlRep.getApplication().getResources() != null) {
                for (ResourceInfo resource : wadlRep.getApplication()
                        .getResources().getResources()) {
                    attachResource(resource, null, router);
                }
            }

            // Analyzes the WADL resources base
            setBaseRef(wadlRep.getApplication().getResources().getBaseRef());
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Error during the attachment of the WADL application", e);
        }
    }

    /**
     * Adds the necessary server connectors to the component.
     * 
     * @param component
     *                The parent component to update.
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
     * Attaches a resource, as specified in a WADL document, to a specified
     * router, then recursively attaches its child resources.
     * 
     * @param currentResource
     *                The resource to attach.
     * @param parentResource
     *                The parent resource. Needed to correctly resolve the
     *                "path" of the resource. Should be null if the resource is
     *                root-level.
     * @param router
     *                The router to which to attach the resource and its
     *                children.
     * @throws ClassNotFoundException
     *                 If the class name specified in the "id" attribute of the
     *                 resource does not exist, this exception will be thrown.
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

                if (parentUriPattern.endsWith("/") == false
                        && uriPattern.startsWith("/") == false) {
                    parentUriPattern += "/";
                }

                uriPattern = parentUriPattern + uriPattern;

                // set thisResource's 'path' attribute to the new uriPattern so
                // child resources will be able to use it
                currentResource.setPath(uriPattern);
            }

            // The "id" attribute conveys the target class name
            Class targetClass = Class.forName(currentResource.getIdentifier());

            // Attach the resource itself
            router.attach(uriPattern, targetClass);

            // Attach any children of the resource
            for (ResourceInfo childResource : currentResource
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
     *                The parent component to update.
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
     *                The virtual host to attach to.
     */
    public void attachToHost(VirtualHost host) {
        if (getBaseRef() != null) {
            // TODO Added test on the path that may be null.
            String path = getBaseRef().getPath();
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
     * Returns the WADL base reference.
     * 
     * @return The WADL base reference.
     */
    public Reference getBaseRef() {
        return this.baseRef;
    }

    /**
     * Completes the data available about a given Filter instance.
     * 
     * @param resourceInfo
     *                The ResourceInfo object to complete.
     * @param filter
     *                The Filter instance to document.
     */

    private void getResourceInfo(ResourceInfo resourceInfo, Filter filter) {
        getResourceInfo(resourceInfo, filter.getNext());
    }

    /**
     * Completes the data available about a given Finder instance.
     * 
     * @param resourceInfo
     *                The ResourceInfo object to complete.
     * @param finder
     *                The Finder instance to document.
     */
    private void getResourceInfo(ResourceInfo resourceInfo, Finder finder) {
        // The handler instance targeted by this finder.
        Handler handler = finder.createTarget(finder.getTargetClass(), null,
                null);

        // The set of allowed methods
        List<Method> methods = new ArrayList<Method>();
        methods.addAll(handler.getAllowedMethods());

        Collections.sort(methods, new Comparator<Method>() {
            public int compare(Method m1, Method m2) {
                return m1.getName().compareTo(m2.getName());
            }
        });

        if (handler instanceof WadlResource) {
            // This kind of resource gives more information
            WadlResource resource = (WadlResource) handler;
            for (Method method : methods) {
                resourceInfo.getMethods().add(resource.getMethodInfo(method));
            }
        } else if (handler instanceof Resource) {
            Resource resource = (Resource) handler;
            for (Method method : methods) {
                MethodInfo methodInfo = new MethodInfo();
                methodInfo.setName(method);
                // Can document the list of supported variants.
                if (Method.GET.equals(method)) {
                    ResponseInfo responseInfo = new ResponseInfo();
                    for (Variant variant : resource.getVariants()) {
                        RepresentationInfo representationInfo = new RepresentationInfo();
                        representationInfo.setMediaType(variant.getMediaType());
                        responseInfo.getRepresentations().add(
                                representationInfo);
                    }
                    methodInfo.setResponse(responseInfo);
                }

                resourceInfo.getMethods().add(methodInfo);
            }
        } else {
            // Can only give information about the list of allowed methods.
            for (Method method : methods) {
                MethodInfo methodInfo = new MethodInfo();
                methodInfo.setName(method);
                resourceInfo.getMethods().add(methodInfo);
            }
        }
    }

    /**
     * Completes the data available about a given Restlet instance.
     * 
     * @param resourceInfo
     *                The ResourceInfo object to complete.
     * @param restlet
     *                The Restlet instance to document.
     */
    private void getResourceInfo(ResourceInfo resourceInfo, Restlet restlet) {
        if (restlet instanceof Finder) {
            getResourceInfo(resourceInfo, (Finder) restlet);
        } else if (restlet instanceof Router) {
            getResourceInfos((Router) restlet, resourceInfo.getChildResources());
        } else if (restlet instanceof Filter) {
            getResourceInfo(resourceInfo, (Filter) restlet);
        }
    }

    /**
     * Returns the WADL data about the given Route instance.
     * 
     * @param route
     *                The Route instance to document.
     * @return The WADL data about the given Route instance.
     */
    private ResourceInfo getResourceInfo(Route route) {
        ResourceInfo result = new ResourceInfo();
        result.setPath(route.getTemplate().getPattern());
        getResourceInfo(result, route.getNext());
        return result;
    }

    /**
     * Completes the list of ResourceInfo instances for the given Router
     * instance.
     * 
     * @param router
     *                The router to document.
     * @param list
     *                The list of ResourceInfo instances to complete.
     */
    private void getResourceInfos(Router router, List<ResourceInfo> list) {
        for (Route route : getRouter().getRoutes()) {
            list.add(getResourceInfo(route));
        }
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
     *                The parent component.
     * @return The related virtual host.
     */
    private VirtualHost getVirtualHost(Component component) {
        // Create the virtual host if necessary
        String hostDomain = baseRef.getHostDomain();
        String hostPort = Integer.toString(baseRef.getHostPort());
        String hostScheme = baseRef.getScheme();

        VirtualHost host = null;
        for (VirtualHost vh : component.getHosts()) {
            if (vh.getHostDomain().equals(hostDomain)
                    && vh.getHostPort().equals(hostPort)
                    && vh.getHostScheme().equals(hostScheme)) {
                host = vh;
            }
        }

        if (host == null) {
            // A new virtual host needs to be created
            host = new VirtualHost(getContext());
            host.setHostDomain(hostDomain);
            host.setHostPort(hostPort);
            host.setHostScheme(hostScheme);
            component.getHosts().add(host);
        }

        return host;
    }

    @Override
    public void handle(Request request, Response response) {
        if (Method.OPTIONS.equals(request.getMethod())
                && request.getResourceRef().getIdentifier().endsWith("*")) {
            // Returns a WADL representation of the application.
            ApplicationInfo applicationInfo = new ApplicationInfo();
            applicationInfo.getResources().setBaseRef(this.getBaseRef());
            getResourceInfos(getRouter(), applicationInfo.getResources()
                    .getResources());
            response.setEntity(new WadlRepresentation(applicationInfo));
            response.setStatus(Status.SUCCESS_OK);
        } else {
            super.handle(request, response);
        }
    }

    /**
     * Sets the WADL base reference.
     * 
     * @param baseRef
     *                The WADL base reference.
     */
    public void setBaseRef(Reference baseRef) {
        this.baseRef = baseRef;
    }

}
