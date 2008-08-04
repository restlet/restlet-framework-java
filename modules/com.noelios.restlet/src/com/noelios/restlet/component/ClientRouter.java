/*
 * Copyright 2005-2007 Noelios Technologies.
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

package com.noelios.restlet.component;

import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Router;

/**
 * Router that collects calls from all applications and dispatches them to the
 * appropriate client connectors.
 * 
 * @author Jerome Louvel (contact@noelios.com)
 */
public class ClientRouter extends Router {
    /** The parent component. */
    private Component component;

    /**
     * Constructor.
     * 
     * @param component
     *            The parent component.
     */
    public ClientRouter(Component component) {
        super(component.getContext());
        this.component = component;
    }

    /** Starts the Restlet. */
	@Override
    public void start() throws Exception {
        for (Client client : getComponent().getClients()) {
            getRoutes().add(new ClientRoute(this, client));
        }

        super.start();
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
