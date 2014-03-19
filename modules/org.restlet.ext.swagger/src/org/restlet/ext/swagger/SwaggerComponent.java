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

package org.restlet.ext.swagger;

import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;

/**
 * Component that can configure itself given a Swagger document. First, it creates
 * the server connectors and the virtual hosts if needed, trying to reuse
 * existing ones if available. Then it creates a {@link SwaggerApplication} using
 * this {@link SwaggerApplication#SwaggerApplication(Representation)} constructor.<br>
 * <br>
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class SwaggerComponent extends Component {

    /**
     * Main method capable of configuring and starting a whole Restlet Component
     * based on a list of local Swagger documents URIs, for example
     * "file:///C:/YahooSearch.swagger".<br>
     * <br>
     * The necessary client connectors are automatically created.
     * 
     * @param args
     *            List of local Swagger document URIs.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // Create a new Swagger-aware component
        final SwaggerComponent component = new SwaggerComponent();

        // For each Swagger document URI attach a matching Application
        for (final String arg : args) {
            component.attach(arg);
        }

        // Start the component
        component.start();
    }

    /**
     * Default constructor.
     */
    public SwaggerComponent() {
    }

    /**
     * Constructor loading a Swagger description document at a given URI.<br>
     * <br>
     * The necessary client connectors are automatically created.
     * 
     * @param swaggerRef
     *            The URI reference to the Swagger description document.
     */
    public SwaggerComponent(Reference swaggerRef) {
        attach(swaggerRef);
    }

    /**
     * Constructor based on a given Swagger description document.
     * 
     * @param swagger
     *            The Swagger description document.
     */
    public SwaggerComponent(Representation swagger) {
        attach(swagger);
    }

    /**
     * Constructor loading a Swagger description document at a given URI.<br>
     * <br>
     * The necessary client connectors are automatically created.
     * 
     * @param swaggerUri
     *            The URI to the Swagger description document.
     */
    public SwaggerComponent(String swaggerUri) {
        attach(swaggerUri);
    }

    /**
     * Attaches an application created from a Swagger description document
     * available at a given URI reference.
     * 
     * @param swaggerRef
     *            The URI reference to the Swagger description document.
     * @return The created Swagger application.
     */
    public SwaggerApplication attach(Reference swaggerRef) {
        SwaggerApplication result = null;

        // Adds some common client connectors to load the Swagger documents
        if (!getClients().contains(swaggerRef.getSchemeProtocol())) {
            getClients().add(swaggerRef.getSchemeProtocol());
        }

        // Get the Swagger document
        final Response response = getContext().getClientDispatcher().handle(
                new Request(Method.GET, swaggerRef));

        if (response.getStatus().isSuccess() && response.isEntityAvailable()) {
            result = attach(response.getEntity());
        }

        return result;
    }

    /**
     * Attaches an application created from a Swagger description document to the
     * component.
     * 
     * @param swagger
     *            The Swagger description document.
     * @return The created Swagger application.
     */
    public SwaggerApplication attach(Representation swagger) {
        final SwaggerApplication result = new SwaggerApplication(getContext()
                .createChildContext(), swagger);
        result.attachToComponent(this);
        return result;
    }

    /**
     * Attaches an application created from a Swagger description document
     * available at a given URI.
     * 
     * @param swaggerUri
     *            The URI to the Swagger description document.
     * @return The created Swagger application.
     */
    public SwaggerApplication attach(String swaggerUri) {
        return attach(new Reference(swaggerUri));
    }

}
