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

import org.restlet.Component;
import org.restlet.data.Reference;
import org.restlet.data.Response;
import org.restlet.resource.Representation;

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
     * "file:///C:/YahooSearch.wadl".<br>
     * <br>
     * The necessary client connectors are automatically created.
     * 
     * @param args
     *                List of local WADL document URIs.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Create a new WADL-aware component
        WadlComponent component = new WadlComponent();

        // For each WADL document URI attach a matching Application
        for (String arg : args) {
            component.attach(arg);
        }

        // Start the component
        component.start();
    }

    /**
     * Default constructor.
     */
    public WadlComponent() {
    }

    /**
     * Constructor loading a WADL description document at a given URI.<br>
     * <br>
     * The necessary client connectors are automatically created.
     * 
     * @param wadlRef
     *                The URI reference to the WADL description document.
     */
    public WadlComponent(Reference wadlRef) {
        attach(wadlRef);
    }

    /**
     * Constructor based on a given WADL description document.
     * 
     * @param wadl
     *                The WADL description document.
     */
    public WadlComponent(Representation wadl) {
        attach(wadl);
    }

    /**
     * Constructor loading a WADL description document at a given URI.<br>
     * <br>
     * The necessary client connectors are automatically created.
     * 
     * @param wadlUri
     *                The URI to the WADL description document.
     */
    public WadlComponent(String wadlUri) {
        attach(wadlUri);
    }

    /**
     * Attaches an application created from a WADL description document
     * available at a given URI reference.
     * 
     * @param wadlRef
     *                The URI reference to the WADL description document.
     * @return The created WADL application.
     */
    public WadlApplication attach(Reference wadlRef) {
        WadlApplication result = null;

        // Adds some common client connectors to load the WADL documents
        if (!getClients().contains(wadlRef.getSchemeProtocol())) {
            getClients().add(wadlRef.getSchemeProtocol());
        }

        // Get the WADL document
        Response response = getContext().getClientDispatcher().get(wadlRef);

        if (response.getStatus().isSuccess() && response.isEntityAvailable()) {
            result = attach(response.getEntity());
        }

        return result;
    }

    /**
     * Attaches an application created from a WADL description document to the
     * component.
     * 
     * @param wadl
     *                The WADL description document.
     * @return The created WADL application.
     */
    public WadlApplication attach(Representation wadl) {
        WadlApplication result = new WadlApplication(getContext(), wadl);
        result.attachToComponent(this);
        return result;
    }

    /**
     * Attaches an application created from a WADL description document
     * available at a given URI.
     * 
     * @param wadlUri
     *                The URI to the WADL description document.
     * @return The created WADL application.
     */
    public WadlApplication attach(String wadlUri) {
        return attach(new Reference(wadlUri));
    }

}
