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

import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Router;
import org.restlet.Server;
import org.restlet.VirtualHost;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.util.NodeSet;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

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
            // Create a DOM representation of the WADL document
            DomRepresentation dom = null;
            if (wadl instanceof DomRepresentation) {
                dom = (DomRepresentation) wadl;
            } else {
                dom = new DomRepresentation(wadl);
            }

            Router root = new Router(getContext());
            this.router = root;
            setRoot(root);

            NodeSet resources = dom.getNodes("/application/resources/resource");
            for (Node resource : resources) {
                attachResource(dom, resource, null, root);
            }

            // Analyzes the WADL resources base
            Node baseNode = dom.getNode("/application/resources/@base");
            setBaseRef(new Reference(baseNode.getNodeValue()));
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
     * @param wadl
     *                The WADL document which contains the resources. Needed so
     *                child resources can be located.
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
    private void attachResource(DomRepresentation wadl, Node currentResource,
            Node parentResource, Router router) throws ClassNotFoundException {

        String uriPattern = currentResource.getAttributes()
                .getNamedItem("path").getNodeValue();
        Node idNode = currentResource.getAttributes().getNamedItem("id");

        if (idNode != null) {
            // if there's a parentResource, add it's uriPattern to this one
            if (parentResource != null) {
                String parentUriPattern = parentResource.getAttributes()
                        .getNamedItem("path").getNodeValue();

                if (parentUriPattern.endsWith("/") == false
                        && uriPattern.startsWith("/") == false) {
                    parentUriPattern += "/";
                }

                uriPattern = parentUriPattern + uriPattern;

                // set thisResource's 'path' attribute to the new uriPattern so
                // child resources will be able to use it
                ((Element) currentResource).setAttribute("path", uriPattern);
            }

            String targetClassName = idNode.getNodeValue();
            Class targetClass = Class.forName(targetClassName);

            // attach the resource itself
            router.attach(uriPattern, targetClass);

            // get a list of the child resources of this resource
            NodeSet childResources = wadl.getNodes("//resource[@id='"
                    + targetClassName + "']/resource");

            // attach any children of the resource
            for (Node childResource : childResources) {
                attachResource(wadl, childResource, currentResource, router);
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
            host.attach(getBaseRef().getPath(), this);
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
