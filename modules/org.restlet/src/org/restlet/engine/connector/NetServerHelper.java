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
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet
 */

package org.restlet.engine.connector;

import java.net.InetSocketAddress;

import org.restlet.Server;
import org.restlet.engine.adapter.HttpServerHelper;

/**
 * Abstract Internal web server connector based on com.sun.net.httpserver
 * package. Here is the list of parameters that are supported. They should be
 * set in the Server's context before it is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public abstract class NetServerHelper extends HttpServerHelper {
    /**
     * Indicates if this service is acting in HTTP or HTTPS mode.
     */
    private volatile boolean confidential;

    /**
     * Socket this server is listening to.
     */
    private volatile InetSocketAddress address;

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public NetServerHelper(Server server) {
        super(server);
    }

    /**
     * Returns the socket address this server is listening to.
     * 
     * @return The socket address this server is listening to.
     */
    protected InetSocketAddress getAddress() {
        return this.address;
    }

    /**
     * Indicates if this service is acting in HTTP or HTTPS mode.
     * 
     * @return True if this service is acting in HTTP or HTTPS mode.
     */
    public boolean isConfidential() {
        return this.confidential;
    }

    /**
     * Indicates if this service is acting in HTTP or HTTPS mode.
     * 
     * @param confidential
     *            True if this service is acting in HTTP or HTTPS mode.
     */
    protected void setConfidential(boolean confidential) {
        this.confidential = confidential;
    }

    /**
     * Sets the socket address this server is listening to.
     * 
     * @param address
     *            The socket address this server is listening to.
     */
    protected void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        getLogger().info(
                "Starting the internal " + getProtocols() + " server on port "
                        + getHelped().getPort());
    }

    @Override
    public synchronized void stop() throws Exception {
        getLogger().info("Stopping the internal server");
    }

}
