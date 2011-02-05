/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.wadl;

import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;

/**
 * Component that can configure itself given a WADL document. First, it creates
 * the server connectors and the virtual hosts if needed, trying to reuse
 * existing ones if available. Then it creates a {@link WadlApplication} using
 * this {@link WadlApplication#WadlApplication(Representation)} constructor.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
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
     *            List of local WADL document URIs.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Create a new WADL-aware component
        final WadlComponent component = new WadlComponent();

        // For each WADL document URI attach a matching Application
        for (final String arg : args) {
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
     *            The URI reference to the WADL description document.
     */
    public WadlComponent(Reference wadlRef) {
        attach(wadlRef);
    }

    /**
     * Constructor based on a given WADL description document.
     * 
     * @param wadl
     *            The WADL description document.
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
     *            The URI to the WADL description document.
     */
    public WadlComponent(String wadlUri) {
        attach(wadlUri);
    }

    /**
     * Attaches an application created from a WADL description document
     * available at a given URI reference.
     * 
     * @param wadlRef
     *            The URI reference to the WADL description document.
     * @return The created WADL application.
     */
    public WadlApplication attach(Reference wadlRef) {
        WadlApplication result = null;

        // Adds some common client connectors to load the WADL documents
        if (!getClients().contains(wadlRef.getSchemeProtocol())) {
            getClients().add(wadlRef.getSchemeProtocol());
        }

        // Get the WADL document
        final Response response = getContext().getClientDispatcher().handle(
                new Request(Method.GET, wadlRef));

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
     *            The WADL description document.
     * @return The created WADL application.
     */
    public WadlApplication attach(Representation wadl) {
        final WadlApplication result = new WadlApplication(getContext()
                .createChildContext(), wadl);
        result.attachToComponent(this);
        return result;
    }

    /**
     * Attaches an application created from a WADL description document
     * available at a given URI.
     * 
     * @param wadlUri
     *            The URI to the WADL description document.
     * @return The created WADL application.
     */
    public WadlApplication attach(String wadlUri) {
        return attach(new Reference(wadlUri));
    }

}
