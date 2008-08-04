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
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.component;

import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.VirtualHost;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;

/**
 * Router that collects calls from all server connectors and dispatches them to
 * the appropriate host routers for dispatching to the user applications.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ServerRouter extends Router {
    /** The parent component. */
    private Component component;

    /**
     * Constructor.
     * 
     * @param component
     *                The parent component.
     */
    public ServerRouter(Component component) {
        super(component.getContext());
        this.component = component;
        setRoutingMode(FIRST);
    }

    /** Starts the Restlet. */
	@Override
    public void start() throws Exception {
        // Attach all virtual hosts
        for (VirtualHost host : getComponent().getHosts()) {
            getRoutes().add(new HostRoute(this, host));
        }

        // Also attach the default host if it exists
        if (getComponent().getDefaultHost() != null) {
            getRoutes().add(
                    new HostRoute(this, getComponent().getDefaultHost()));
        }

        // If no host matches, display and error page with a precise message
        Restlet noHostMatched = new Restlet(getComponent().getContext()) {
        	@Override
            public void handle(Request request, Response response) {
                response.setStatus(Status.CLIENT_ERROR_NOT_FOUND,
                        "No virtual host could handle the request");
            }
        };
        setDefaultRoute(new Route(this, "", noHostMatched));

        // Start the router
        super.start();
    }

    @Override
    public void stop() throws Exception {
        getRoutes().clear();
        super.stop();
    }

    /**
     * Returns the parent component.
     * 
     * @return The parent component.
     */
    private Component getComponent() {
        return this.component;
    }
}
