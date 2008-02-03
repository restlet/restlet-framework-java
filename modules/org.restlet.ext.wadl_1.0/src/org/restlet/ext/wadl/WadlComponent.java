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
import org.restlet.Router;
import org.restlet.Server;
import org.restlet.VirtualHost;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.resource.DomRepresentation;
import org.restlet.resource.Representation;
import org.restlet.util.NodeSet;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Component that can automatically configure itself given a list of WADL
 * documents. First, it creates the server connectors and the virtual hosts if
 * needed, trying to reuse existing ones if available.<br>
 * <br>
 * Then it creates a Restlet Application with a router as root. For each
 * resource found in the WADL document, it tries to attach a Restlet Resource
 * class to the router using the WADL path. It looks up the qualified name of
 * the Resource class using the WALD "id" attribute of the "resource" elements.
 * This is the only Restlet specific constraint on the WADL document.<br>
 * <br>
 * Finally, it attaches the Restlet Application to the virtual host using the
 * "base" attribute of the WADL "resources" element.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class WadlComponent extends Component {

    /**
     * Main method capable of configuring and starting a whole Restlet Component
     * based on a list of local WADL documents URIs, for example
     * "file:///C:/YahooSearch.wadl". The FILE and CLAP scheme are available for
     * those URIs.
     * 
     * @param args
     *                List of local WADL document URIs.
     * @throws Exception
     */
    public static void main(String... args) throws Exception {

        // Create a new WADL-aware component
        WadlComponent component = new WadlComponent();

        // For each WADL document URI attach a matching Application
        for (String arg : args) {
            Response response = component.getContext().getClientDispatcher()
                    .get(arg);

            if (response.getStatus().isSuccess()
                    && response.isEntityAvailable()) {
                component.attach(response.getEntity());
            }
        }

        // Start the component
        component.start();
    }

    /**
     * Constructor adding FILE and CLAP clients to load WALD documents.
     */
    public WadlComponent() {
        // Add some common client connectors to load the WADL documents
        getClients().add(Protocol.FILE);
        getClients().add(Protocol.CLAP);
    }

    /**
     * Attaches an application described using a WADL document.
     * 
     * @param wadl
     *                The WADL document.
     */
    public void attach(Representation wadl) {
        try {
            // Create a DOM representation of the WADL document
            DomRepresentation dom = null;
            if (wadl instanceof DomRepresentation) {
                dom = (DomRepresentation) wadl;
            } else {
                dom = new DomRepresentation(wadl);
            }

            // Create the application
            Application app = new Application(getContext());
            Router root = new Router(app.getContext());
            app.setRoot(root);

            NodeSet resources = dom.getNodes("/application/resources/resource");
            for (Node resource : resources) {
                attachResourceAndChildren(dom, resource, null, root);
            }

            // Analyzes the WADL resources base
            Node baseNode = dom.getNode("/application/resources/@base");
            Reference baseRef = new Reference(baseNode.getNodeValue());

            // Create the server connector
            Protocol protocol = baseRef.getSchemeProtocol();
            int port = baseRef.getHostPort();
            boolean exists = false;

            if (port == -1) {
                for (Server server : getServers()) {
                    if (server.getProtocols().contains(protocol)
                            && (server.getPort() == protocol.getDefaultPort())) {
                        exists = true;
                    }
                }

                if (!exists) {
                    getServers().add(protocol);
                }
            } else {
                for (Server server : getServers()) {
                    if (server.getProtocols().contains(protocol)
                            && (server.getPort() == port)) {
                        exists = true;
                    }
                }

                if (!exists) {
                    getServers().add(protocol, port);
                }
            }

            // Create the virtual host if necessary
            String hostDomain = baseRef.getHostDomain();
            String hostPort = Integer.toString(baseRef.getHostPort());
            String hostScheme = baseRef.getScheme();

            VirtualHost host = null;
            for (VirtualHost vh : getHosts()) {
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
                getHosts().add(host);
            }

            // Attach the application to the virtual host
            host.attach(baseRef.getPath(), app);
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Error during the attachment of the WADL application", e);
        }
    }

    /**
     * Attaches a resource, as specified in a WADL document, to a specified
     * router, then recursively attaches its child resources.
     * 
     * @param wadl
     *                The WADL document which contains the resources. Needed so
     *                child resources can be located.
     * @param thisResource
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
    protected void attachResourceAndChildren(DomRepresentation wadl,
            Node thisResource, Node parentResource, Router router)
            throws ClassNotFoundException {

        String uriPattern = thisResource.getAttributes().getNamedItem("path")
                .getNodeValue();
        Node idNode = thisResource.getAttributes().getNamedItem("id");

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
                ((Element) thisResource).setAttribute("path", uriPattern);
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
                attachResourceAndChildren(wadl, childResource, thisResource,
                        router);
            }
        } else {
            getLogger()
                    .warning(
                            "Unable to find the 'id' attribute of the resource element with this path attribute \""
                                    + uriPattern + "\"");
        }
    }
}
