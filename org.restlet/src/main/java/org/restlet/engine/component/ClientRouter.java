/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
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

package org.restlet.engine.component;

import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * Router that collects calls from all applications and dispatches them to the
 * appropriate client connectors.
 * 
 * Concurrency note: instances of this class or its subclasses can be invoked by
 * several threads at the same time and therefore must be thread-safe. You
 * should be especially careful when storing state in member variables.
 * 
 * @author Jerome Louvel
 */
public class ClientRouter extends Router {
    /** The parent component. */
    private volatile Component component;

    /**
     * Constructor.
     * 
     * @param component
     *            The parent component.
     */
    public ClientRouter(Component component) {
        super((component == null) ? null : component.getContext()
                .createChildContext());
        this.component = component;
    }

    @Override
    protected void logRoute(org.restlet.routing.Route route) {
        if (getLogger().isLoggable(Level.FINE)) {
            if (route instanceof ClientRoute) {
                Client client = ((ClientRoute) route).getClient();

                getLogger().fine(
                        "This client was selected: \"" + client.getProtocols()
                                + "\"");
            } else {
                super.logRoute(route);
            }
        }
    }

    @Override
    public Restlet getNext(Request request, Response response) {
        Restlet result = super.getNext(request, response);

        if (result == null) {
            getLogger()
                    .warning(
                            "The protocol used by this request is not declared in the list of client connectors. ("
                                    + request.getResourceRef()
                                            .getSchemeProtocol()
                                    + "). In case you are using an instance of the Component class, check its \"clients\" property.");
        }
        return result;
    }

    /**
     * Returns the parent component.
     * 
     * @return The parent component.
     */
    private Component getComponent() {
        return this.component;
    }

    /** Starts the Restlet. */
    @Override
    public synchronized void start() throws Exception {
        for (final Client client : getComponent().getClients()) {
            getRoutes().add(new ClientRoute(this, client));
        }

        super.start();
    }
}
