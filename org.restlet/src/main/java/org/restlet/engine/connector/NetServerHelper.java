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

package org.restlet.engine.connector;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.restlet.Server;
import org.restlet.engine.adapter.HttpServerHelper;
import org.restlet.engine.log.LoggingThreadFactory;

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
 * <tr>
 * <td>minThreads</td>
 * <td>int</td>
 * <td>1</td>
 * <td>Minimum number of worker threads waiting to service calls, even if they
 * are idle. Technically speaking, this is a core number of threads that are
 * pre-started.</td>
 * </tr>
 * <tr>
 * <td>maxThreads</td>
 * <td>int</td>
 * <td>10</td>
 * <td>Maximum number of worker threads that can service calls. If this number
 * is reached then additional calls are queued if the "maxQueued" value hasn't
 * been reached.</td>
 * </tr>
 * <tr>
 * <td>maxQueued</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Maximum number of calls that can be queued if there aren't any worker
 * thread available to service them. If the value is '0', then no queue is used
 * and calls are rejected if no worker thread is immediately available. If the
 * value is '-1', then an unbounded queue is used and calls are never rejected.<br>
 * <br>
 * Note: make sure that this value is consistent with {@link #getMinThreads()}
 * and the behavior of the {@link ThreadPoolExecutor} configured internally.</td>
 * </tr>
 * <tr>
 * <td>maxThreadIdleTimeMs</td>
 * <td>int</td>
 * <td>300 000</td>
 * <td>Time for an idle thread to wait for an operation before being collected.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public abstract class NetServerHelper extends HttpServerHelper {
    /**
     * Socket this server is listening to.
     */
    private volatile InetSocketAddress address;

    /**
     * Indicates if this service is acting in HTTP or HTTPS mode.
     */
    private volatile boolean confidential;

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
     * Creates the handler service.
     * 
     * @return The handler service.
     */
    protected ThreadPoolExecutor createThreadPool() {
        int maxThreads = getMaxThreads();
        int minThreads = getMinThreads();

        BlockingQueue<Runnable> queue = null;

        if (getMaxQueued() == 0) {
            queue = new SynchronousQueue<Runnable>();
        } else if (getMaxQueued() < 0) {
            queue = new LinkedBlockingQueue<Runnable>();
        } else {
            queue = new ArrayBlockingQueue<Runnable>(getMaxQueued());
        }

        ThreadPoolExecutor result = new ThreadPoolExecutor(minThreads,
                maxThreads, getMaxThreadIdleTimeMs(), TimeUnit.MILLISECONDS,
                queue, new LoggingThreadFactory(getLogger(), true));
        result.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r,
                    ThreadPoolExecutor executor) {
                getLogger().warning(
                        "Unable to run the following server-side task: " + r);
            }
        });

        // Ensure that core threads act like a minimum number of threads
        result.prestartAllCoreThreads();
        return result;
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
     * Returns the maximum number of calls that can be queued if there aren't
     * any worker thread available to service them. If the value is '0', then no
     * queue is used and calls are rejected if no worker thread is immediately
     * available. If the value is '-1', then an unbounded queue is used and
     * calls are never rejected.<br>
     * <br>
     * Note: make sure that this value is consistent with
     * {@link #getMinThreads()} and the behavior of the
     * {@link ThreadPoolExecutor} configured internally.
     * 
     * @return The maximum number of calls that can be queued.
     */
    public int getMaxQueued() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxQueued", "0"));
    }

    /**
     * Returns the time for an idle thread to wait for an operation before being
     * collected.
     * 
     * @return The time for an idle thread to wait for an operation before being
     *         collected.
     */
    public int getMaxThreadIdleTimeMs() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxThreadIdleTimeMs", "300000"));
    }

    /**
     * Returns the maximum threads that will service requests.
     * 
     * @return The maximum threads that will service requests.
     */
    public int getMaxThreads() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxThreads", "10"));
    }

    /**
     * Returns the minimum threads waiting to service requests. Technically
     * speaking, this is a core number of threads that are pre-started.
     * 
     * @return The minimum threads waiting to service requests.
     */
    public int getMinThreads() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "minThreads", "1"));
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
     * Sets the socket address this server is listening to.
     * 
     * @param address
     *            The socket address this server is listening to.
     */
    protected void setAddress(InetSocketAddress address) {
        this.address = address;
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
