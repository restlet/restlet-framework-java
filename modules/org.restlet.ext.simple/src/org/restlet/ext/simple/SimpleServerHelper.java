/**
 * Copyright 2005-2012 Restlet S.A.S.
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
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.simple;

import java.net.InetSocketAddress;
import java.util.logging.Level;

import org.restlet.Server;
import org.restlet.engine.adapter.HttpServerHelper;
import org.simpleframework.http.core.ContainerServer;
import org.simpleframework.transport.connect.Connection;

/**
 * Abstract Simple Web server connector. Here is the list of parameters that are
 * supported. They should be set in the Server's context before it is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>defaultThreads</td>
 * <td>int</td>
 * <td>20</td>
 * <td>Default number of polling threads for a handler object.</td>
 * </tr>
 * <tr>
 * <td>maxWaitTimeMs</td>
 * <td>int</td>
 * <td>200</td>
 * <td>Maximum waiting time between polls of the input.</td>
 * </tr>
 * </table>
 * 
 * @author Lars Heuer
 * @author Jerome Louvel
 */
public abstract class SimpleServerHelper extends HttpServerHelper {
    /**
     * Indicates if this service is acting in HTTP or HTTPS mode.
     */
    private volatile boolean confidential;

    /**
     * Simple connection.
     */
    private volatile Connection connection;

    /**
     * Simple container server.
     */
    private volatile ContainerServer containerServer;

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
    public SimpleServerHelper(Server server) {
        super(server);
    }

    /**
     * Returns the Simple connection.
     * 
     * @return The Simple connection.
     */
    protected Connection getConnection() {
        return this.connection;
    }

    /**
     * Returns the default number of polling threads for a handler object.
     * 
     * @return The default number of polling threads for a handler object.
     */
    public int getDefaultThreads() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "defaultThreads", "5"));
    }

    /**
     * Returns the Simple container server.
     * 
     * @return The Simple container server.
     */
    protected ContainerServer getContainerServer() {
        return this.containerServer;
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
     * Sets the Simple connection.
     * 
     * @param connection
     *            The Simple connection.
     */
    protected void setConnection(Connection connection) {
        this.connection = connection;
    }

    /**
     * Sets the Simple container server.
     * 
     * @param container
     *            The Simple containerServer.
     */
    protected void setContainerServer(ContainerServer container) {
        this.containerServer = container;
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
                "Starting the Simple " + getProtocols() + " server on port "
                        + getHelped().getPort());

        // Sets the ephemeral port is necessary
        // setEphemeralPort(getAddress().getPort());
    }

    @Override
    public synchronized void stop() throws Exception {
        getLogger().info("Stopping the Simple server");

        try {
            getConnection().close();
        } catch (Exception e) {
            getLogger()
                    .log(Level.FINE,
                            "Exception while closing the server socket. Can probably be safely ignored.",
                            e);
        }

        if (getContainerServer() != null) {
            getContainerServer().stop();
        }

    }

}
