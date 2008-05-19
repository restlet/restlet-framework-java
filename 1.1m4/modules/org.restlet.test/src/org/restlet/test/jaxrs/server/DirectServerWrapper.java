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
package org.restlet.test.jaxrs.server;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

/**
 * This class allows easy testing of JAX-RS implementations by starting a server
 * for a given class and access the server for a given sub pass relativ to the
 * pass of the root resource class.
 * 
 * @author Stephan Koops
 * @see DirectServerWrapperFactory
 */
public class DirectServerWrapper implements ServerWrapper {

    private Restlet connector;

    public DirectServerWrapper() {
    }

    public Restlet getClientConnector() {
        if (connector == null)
            throw new IllegalStateException("The Server is not yet started");
        return connector;
    }

    public int getServerPort() {
        throw new IllegalStateException(
                "Uses direct access, so you can access the port");
    }

    public void startServer(Application application, Protocol protocol)
            throws Exception {
        connector = application;
    }

    public void stopServer() throws Exception {
        this.connector = null;
    }
}