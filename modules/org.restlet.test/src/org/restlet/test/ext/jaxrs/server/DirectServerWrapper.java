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

package org.restlet.test.ext.jaxrs.server;

import org.restlet.Application;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.jaxrs.internal.exceptions.JaxRsRuntimeException;

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
        if (this.connector == null) {
            throw new IllegalStateException("The Server is not yet started");
        }
        return this.connector;
    }

    public int getServerPort() {
        throw new IllegalStateException(
                "Uses direct access, so you can access the port");
    }

    public void startServer(final Application application, Protocol protocol)
            throws Exception {
        this.connector = new Restlet() {
            @Override
            public void handle(Request request, Response response) {
                try {
                    application.handle(request, response);
                } catch (JaxRsRuntimeException e) {
                    response.setStatus(Status.SERVER_ERROR_INTERNAL);
                }
            }
        };
    }

    public void stopServer() throws Exception {
        this.connector = null;
    }
}
