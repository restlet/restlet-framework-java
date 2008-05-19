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

package com.noelios.restlet.ext.grizzly.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import com.noelios.restlet.Engine;
import com.noelios.restlet.ServerHelper;
import com.noelios.restlet.ext.grizzly.HttpServerHelper;
import com.noelios.restlet.ext.grizzly.HttpsServerHelper;

/**
 * OSGi activator.
 * 
 * @author Jerome Louvel
 */
public class Activator implements BundleActivator {

    /** The registered HTTP server helper. */
    private ServerHelper httpServerHelper;

    /** The registered HTTPS server helper. */
    private ServerHelper httpsServerHelper;

    /**
     * Starts the OSGi bundle by registering the connectors.
     */
    public void start(BundleContext context) throws Exception {
        // Instantiate the helpers
        this.httpServerHelper = new HttpServerHelper(null);
        this.httpsServerHelper = new HttpsServerHelper(null);

        // Add the helpers
        Engine.getInstance().getRegisteredServers().add(0,
                this.httpsServerHelper);
        Engine.getInstance().getRegisteredServers().add(0,
                this.httpServerHelper);
    }

    /**
     * Stops the OSGi bundle by deregistering the connectors.
     */
    public void stop(BundleContext context) throws Exception {
        // Removes the helpers
        Engine.getInstance().getRegisteredServers().remove(
                this.httpServerHelper);
        Engine.getInstance().getRegisteredServers().remove(
                this.httpsServerHelper);
    }

}
