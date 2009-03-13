/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or CDL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.ext.jetty;

import java.io.IOException;

import javax.servlet.ServletException;

import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Server;
import org.mortbay.thread.QueuedThreadPool;

/**
 * Abstract Jetty Web server connector. Here is the list of parameters that are
 * supported:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>minThreads</td>
 * <td>int</td>
 * <td>1</td>
 * <td>Minimum threads waiting to service requests.</td>
 * </tr>
 * <tr>
 * <td>maxThread</td>
 * <td>int</td>
 * <td>255</td>
 * <td>Maximum threads that will service requests.</td>
 * </tr>
 * <tr>
 * <td>threadMaxIdleTimeMs</td>
 * <td>int</td>
 * <td>60000</td>
 * <td>Time for an idle thread to wait for a request or read.</td>
 * </tr>
 * <tr>
 * <td>lowThreads</td>
 * <td>int</td>
 * <td>25</td>
 * <td>Threshold of remaining threads at which the server is considered as
 * running low on resources.</td>
 * </tr>
 * <tr>
 * <td>lowResourceMaxIdleTimeMs</td>
 * <td>int</td>
 * <td>2500</td>
 * <td>Time in ms that connections will persist if listener is low on resources.
 * </td>
 * </tr>
 * <tr>
 * <td>acceptorThreads</td>
 * <td>int</td>
 * <td>1</td>
 * <td>Number of acceptor threads to set.</td>
 * </tr>
 * <tr>
 * <td>acceptQueueSize</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Size of the accept queue.</td>
 * </tr>
 * <tr>
 * <td>headerBufferSize</td>
 * <td>int</td>
 * <td>4*1024</td>
 * <td>Size of the buffer to be used for request and response headers.</td>
 * </tr>
 * <tr>
 * <td>requestBufferSize</td>
 * <td>int</td>
 * <td>8*1024</td>
 * <td>Size of the content buffer for receiving requests.</td>
 * </tr>
 * <tr>
 * <td>responseBufferSize</td>
 * <td>int</td>
 * <td>32*1024</td>
 * <td>Size of the content buffer for sending responses.</td>
 * </tr>
 * <tr>
 * <td>ioMaxIdleTimeMs</td>
 * <td>int</td>
 * <td>30000</td>
 * <td>Maximum time to wait on an idle IO operation.</td>
 * </tr>
 * <tr>
 * <td>soLingerTime</td>
 * <td>int</td>
 * <td>1000</td>
 * <td>SO linger time (see Jetty 6 documentation).</td>
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
 * @see <a href="http://jetty.mortbay.org/">Jetty home page</a>
 * @author Jerome Louvel
 */
public abstract class JettyServerHelper extends
        org.restlet.engine.http.HttpServerHelper {
    /**
     * Jetty server wrapped by a parent Restlet HTTP server connector.
     * 
     * @author Jerome Louvel
     */
    private static class WrappedServer extends org.mortbay.jetty.Server {
        JettyServerHelper helper;

        /**
         * Constructor.
         * 
         * @param server
         *            The Jetty HTTP server.
         */
        public WrappedServer(JettyServerHelper server) {
            this.helper = server;
        }

        /**
         * Handler method converting a Jetty Connection into a Restlet Call.
         * 
         * @param connection
         *            The connection to handle.
         */
        @Override
        public void handle(HttpConnection connection) throws IOException,
                ServletException {
            this.helper.handle(new JettyCall(this.helper.getHelped(),
                    connection));
        }
    }

    /** The wrapped Jetty server. */
    private volatile Server wrappedServer;

    /** The internal Jetty connector. */
    private volatile AbstractConnector connector;

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public JettyServerHelper(org.restlet.Server server) {
        super(server);
        this.connector = null;
        this.wrappedServer = new WrappedServer(this);

        // Configuring the thread pool
        final QueuedThreadPool btp = new QueuedThreadPool();
        btp.setLowThreads(getLowThreads());
        btp.setMaxIdleTimeMs(getThreadMaxIdleTimeMs());
        btp.setMaxThreads(getMaxThreads());
        btp.setMinThreads(getMinThreads());
        getWrappedServer().setThreadPool(btp);
    }

    /**
     * Configures the internal Jetty connector.
     * 
     * @param connector
     *            The internal Jetty connector.
     */
    protected void configure(AbstractConnector connector) {
        if (getHelped().getAddress() != null) {
            connector.setHost(getHelped().getAddress());
        }
        connector.setPort(getHelped().getPort());
        connector.setLowResourceMaxIdleTime(getLowResourceMaxIdleTimeMs());
        connector.setAcceptors(getAcceptorThreads());
        connector.setAcceptQueueSize(getAcceptQueueSize());
        connector.setHeaderBufferSize(getHeaderBufferSize());
        connector.setRequestBufferSize(getRequestBufferSize());
        connector.setResponseBufferSize(getResponseBufferSize());
        connector.setMaxIdleTime(getIoMaxIdleTimeMs());
        connector.setSoLingerTime(getSoLingerTime());
    }

    /**
     * Creates a new internal Jetty connector.
     * 
     * @return A new internal Jetty connector.
     */
    protected abstract AbstractConnector createConnector();

    /**
     * Returns the number of acceptor threads to set.
     * 
     * @return The number of acceptor threads to set.
     */
    public int getAcceptorThreads() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "acceptorThreads", "1"));
    }

    /**
     * Returns the size of the accept queue.
     * 
     * @return The size of the accept queue.
     */
    public int getAcceptQueueSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "acceptQueueSize", "0"));
    }

    /**
     * Returns the size of the buffer to be used for request and response
     * headers.
     * 
     * @return The size of the buffer to be used for request and response
     *         headers.
     */
    public int getHeaderBufferSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "headerBufferSize", Integer.toString(4 * 1024)));
    }

    /**
     * Returns the maximum time to wait on an idle IO operation.
     * 
     * @return The maximum time to wait on an idle IO operation.
     */
    public int getIoMaxIdleTimeMs() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "ioMaxIdleTimeMs", "30000"));
    }

    /**
     * Returns the time in ms that connections will persist if listener is low
     * on resources.
     * 
     * @return The time in ms that connections will persist if listener is low
     *         on resources.
     */
    public int getLowResourceMaxIdleTimeMs() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "lowResourceMaxIdleTimeMs", "2500"));
    }

    /**
     * Returns the threshold of remaining threads at which the server is
     * considered as running low on resources.
     * 
     * @return The threshold of remaining threads at which the server is
     *         considered as running low on resources.
     */
    public int getLowThreads() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "lowThreads", "25"));
    }

    /**
     * Returns the maximum threads that will service requests.
     * 
     * @return The maximum threads that will service requests.
     */
    public int getMaxThreads() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxThreads", "255"));
    }

    /**
     * Returns the minimum threads waiting to service requests.
     * 
     * @return The minimum threads waiting to service requests.
     */
    public int getMinThreads() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "minThreads", "1"));
    }

    /**
     * Returns the size of the content buffer for receiving requests.
     * 
     * @return The size of the content buffer for receiving requests.
     */
    public int getRequestBufferSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "requestBufferSize", Integer.toString(8 * 1024)));
    }

    /**
     * Returns the size of the content buffer for sending responses.
     * 
     * @return The size of the content buffer for sending responses.
     */
    public int getResponseBufferSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "responseBufferSize", Integer.toString(32 * 1024)));
    }

    /**
     * Returns the SO linger time (see Jetty 6 documentation).
     * 
     * @return The SO linger time (see Jetty 6 documentation).
     */
    public int getSoLingerTime() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "soLingerTime", "1000"));
    }

    /**
     * Returns the time for an idle thread to wait for a request or read.
     * 
     * @return The time for an idle thread to wait for a request or read.
     */
    public int getThreadMaxIdleTimeMs() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "threadMaxIdleTimeMs", "60000"));
    }

    /**
     * Returns the wrapped Jetty server.
     * 
     * @return The wrapped Jetty server.
     */
    protected Server getWrappedServer() {
        return this.wrappedServer;
    }

    /**
     * Sets the wrapped Jetty server.
     * 
     * @param wrappedServer
     *            The wrapped Jetty server.
     */
    protected void setWrappedServer(Server wrappedServer) {
        this.wrappedServer = wrappedServer;
    }

    @Override
    public void start() throws Exception {
        if (this.connector == null) {
            this.connector = createConnector();
            configure(this.connector);
            getWrappedServer().addConnector(this.connector);
        }

        getWrappedServer().start();
        setEphemeralPort(this.connector.getLocalPort());
    }

    @Override
    public void stop() throws Exception {
        getWrappedServer().stop();
    }

}
