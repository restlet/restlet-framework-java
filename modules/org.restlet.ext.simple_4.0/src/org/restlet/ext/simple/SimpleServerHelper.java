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

package org.restlet.ext.simple;

import java.net.InetSocketAddress;

import org.restlet.Server;
import org.restlet.engine.http.HttpServerHelper;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.http.core.ContainerServer;

/**
 * Abstract Simple Web server connector. Here is the list of parameters that are
 * supported:
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
 * <tr>
 * <td>converter</td>
 * <td>String</td>
 * <td>org.restlet.engine.http.HttpServerConverter</td>
 * <td>Class name of the converter of low-level HTTP calls into high level
 * requests and responses.</td>
 * </tr>
 * <tr>
 * <td>useForwardedForHeader</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Lookup the "X-Forwarded-For" header supported by popular proxies and
 * caches and uses it to populate the Request.getClientAddresses() method
 * result. This information is only safe for intermediary components within your
 * local network. Other addresses could easily be changed by setting a fake
 * header and should not be trusted for serious security checks.</td>
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
     * Simple pipeline handler.
     */
    private volatile ContainerServer container;

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
                "defaultThreads", "10"));
    }

    /**
     * Returns the Simple server.
     * 
     * @return The Simple container server.
     */
    protected ContainerServer getContainer() {
        return this.container;
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
    protected boolean isConfidential() {
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
     * Sets the Simple container.
     * 
     * @param handler
     *            The Simple pipeline handler.
     */
    protected void setContainer(ContainerServer container) {
        this.container = container;
    }

    /**
     * Sets the socket address this server is listening to.
     * 
     * @param socket
     *            The socket address this server is listening to.
     */
    protected void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        getLogger().info("Starting the Simple server");

        // Sets the ephemeral port is necessary
        setEphemeralPort(getAddress().getPort());
    }

    @Override
    public synchronized void stop() throws Exception {
        getLogger().info("Stopping the Simple server");
        
       	getConnection().close();
      	getContainer().stop();

        // For further information on how to shutdown a Simple
        // server, see
        // http://sourceforge.net/mailarchive/forum.php?thread_id=10138257&
        // forum_id=38791
        // There seems to be place for improvement in this method.
    }

}
