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
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */
package org.restlet.test.jaxrs.server;

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

/**
 * This interface wraps a server for the tests. The default implementation is
 * the {@link RestletServerWrapper}, but there are other implementations
 * possible, for example for the JSR-311 reference implementation Jersey.
 * 
 * @see org.restlet.test.jaxrs.services.tests.JaxRsTestCase#setServerWrapper(ServerWrapper)
 * @see ServerWrapperFactory
 * 
 * @author Stephan Koops
 */
public interface ServerWrapper {
    /**
     * Returns the connector to access the application.
     * 
     * @return the connector to access the application.
     */
    public Restlet getClientConnector();

    /**
     * Returns the port the server is running on. throws an
     * {@link IllegalStateException}, if direct access is used.
     */
    public int getServerPort();

    /**
     * Starts the server with the given protocol on the given port with the
     * given Collection of root resource classes. The method {@link #setUp()}
     * will do this on every test start up.
     * 
     * @param protocol
     *            the protocol to use
     * @param appConfig
     * @throws Exception
     */
    public void startServer(Application application, Protocol protocol)
            throws Exception;

    /**
     * Stops the component. The method {@link #tearDown()} do this after every
     * test.
     * 
     * @param component
     * @throws Exception
     */
    public void stopServer() throws Exception;
}