/**
 * Copyright 2005-2009 Noelios Technologies.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.server;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
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
        this.connector = new Restlet()
        {
            @Override
            public void handle(Request request, Response response)
            {
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