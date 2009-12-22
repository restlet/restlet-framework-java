/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
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

package org.restlet.engine.http.connector;

import java.io.IOException;
import java.net.Socket;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.restlet.Connector;
import org.restlet.Message;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.log.LoggingThreadFactory;

/**
 * Base connector helper. Here is the list of parameters that are supported.
 * They should be set in the connector's context before it is started:
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
 * <td>maxThreads</td>
 * <td>int</td>
 * <td>255</td>
 * <td>Maximum threads that will service requests.</td>
 * </tr>
 * <tr>
 * <td>maxTotalConnections</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>Maximum number of concurrent connections in total.</td>
 * </tr>
 * <tr>
 * <td>maxConnectionsPerHost</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>Maximum number of concurrent connections per host (IP address).</td>
 * </tr>
 * <tr>
 * <td>persistingConnections</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Indicates if connections should be kept alive after a call.</td>
 * </tr>
 * <tr>
 * <td>threadMaxIdleTimeMs</td>
 * <td>int</td>
 * <td>60000</td>
 * <td>Time for an idle thread to wait for an operation before being collected.</td>
 * </tr>
 * <tr>
 * <td>controllerSleepTimeMs</td>
 * <td>int</td>
 * <td>100</td>
 * <td>Time for the controller thread to sleep between each control.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public abstract class BaseHelper<T extends Connector, U extends Message, V extends Message>
        extends ConnectorHelper<T> {

    /** The controller service. */
    private volatile ExecutorService controllerService;

    /** The worker service. */
    private volatile ThreadPoolExecutor workerService;

    /** The set of active connections. */
    private final Set<Connection<T, U, V>> connections;

    /** The queue of requests pending for handling. */
    private final Queue<Request> pendingRequests;

    /** The queue of responses pending for writing. */
    private final Queue<Response> pendingResponses;

    /**
     * Constructor.
     * 
     * @param connector
     */
    public BaseHelper(T connector) {
        super(connector);
        this.connections = new CopyOnWriteArraySet<Connection<T, U, V>>();
        this.pendingRequests = new ConcurrentLinkedQueue<Request>();
        this.pendingResponses = new ConcurrentLinkedQueue<Response>();
    }

    /**
     * Creates a connection associated to the given socket.
     * 
     * @param helper
     *            The parent helper.
     * @param socket
     *            The associated socket.
     * @return The new connection.
     * @throws IOException
     */
    protected abstract Connection<T, U, V> createConnection(
            BaseHelper<T, U, V> helper, Socket socket) throws IOException;

    /**
     * Creates the connector controller service.
     * 
     * @return The connector controller service.
     */
    protected ExecutorService createControllerService() {
        return Executors.newSingleThreadExecutor(new LoggingThreadFactory(
                getLogger()));
    }

    /**
     * Creates the response object.
     * 
     * @param request
     *            The associated request.
     * @return The response object.
     */
    protected Response createResponse(Request request) {
        return new Response(request);
    }

    /**
     * Creates the handler service.
     * 
     * @return The handler service.
     */
    protected ThreadPoolExecutor createWorkerService() {
        int maxThreads = getMaxThreads();
        int minThreads = getMinThreads();

        ThreadPoolExecutor result = new ThreadPoolExecutor(minThreads,
                maxThreads, getThreadMaxIdleTimeMs(), TimeUnit.MILLISECONDS,
                new SynchronousQueue<Runnable>(), new LoggingThreadFactory(
                        getLogger()));
        result.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r,
                    ThreadPoolExecutor executor) {
                getLogger().warning("Unable to run the following task: " + r);
            }
        });
        return result;
    }

    /**
     * Returns the set of active connections.
     * 
     * @return The set of active connections.
     */
    protected Set<Connection<T, U, V>> getConnections() {
        return connections;
    }

    /**
     * Returns the time for the controller thread to sleep between each control.
     * 
     * @return The time for the controller thread to sleep between each control.
     */
    public int getControllerSleepTimeMs() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "controllerSleepTimeMs", "100"));
    }

    /**
     * Returns the maximum concurrent connections per host (IP address). By
     * default, it is unbounded.
     * 
     * @return Maximum number of concurrent connections per host (IP address).
     */
    public int getMaxConnectionsPerHost() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxConnectionsPerHost", "-1"));
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
     * Returns the maximum threads that will service requests.
     * 
     * @return The maximum threads that will service requests.
     */
    public boolean isPersistingConnections() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "persistingConnections", "true"));
    }

    /**
     * Returns the maximum number of concurrent connections allowed. By default,
     * it is unbounded.
     * 
     * @return The maximum number of concurrent connections allowed.
     */
    public int getMaxTotalConnections() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxTotalConnections", "-1"));
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
     * Returns the queue of requests pending for handling.
     * 
     * @return The queue of requests pending for handling.
     */
    protected Queue<Request> getPendingRequests() {
        return pendingRequests;
    }

    /**
     * Returns the queue of responses pending for writing.
     * 
     * @return The queue of responses pending for writing.
     */
    protected Queue<Response> getPendingResponses() {
        return pendingResponses;
    }

    /**
     * Returns the time for an idle thread to wait for an operation before being
     * collected.
     * 
     * @return The time for an idle thread to wait for an operation before being
     *         collected.
     */
    public int getThreadMaxIdleTimeMs() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "threadMaxIdleTimeMs", "60000"));
    }

    /**
     * Returns the connection handler service.
     * 
     * @return The connection handler service.
     */
    public ThreadPoolExecutor getWorkerService() {
        return workerService;
    }

    /**
     * Handles a request.
     * 
     * @param request
     *            The request to handle.
     */
    public abstract void handle(Request request);

    /**
     * Handles a response.
     * 
     * @param response
     *            The response to handle.
     */
    public abstract void handle(Response response);

    /**
     * Handles the next request.
     */
    public void handleNextRequest() {
        handle(getPendingRequests().poll());
    }

    /**
     * Handles the next response.
     */
    protected void handleNextResponse() {
        handle(getPendingResponses().poll());
    }

    /**
     * Indicates if the worker service is busy. This state is detected by
     * checking if the number of active task running is superior or equal to the
     * maximum pool size.
     * 
     * @return True if the worker service is busy.
     */
    protected boolean isWorkerServiceBusy() {
        return getWorkerService().getActiveCount() >= (getWorkerService()
                .getMaximumPoolSize() - 1);
    }

    @Override
    public void start() throws Exception {
        super.start();

        this.controllerService = createControllerService();
        this.workerService = createWorkerService();
        this.controllerService.submit(new ControllerTask(this));
    }

    @Override
    public void stop() throws Exception {
        super.stop();

        if (this.workerService != null) {
            // Gracefully shutdown the handlers, they should complete
            // in a timely fashion
            this.workerService.shutdown();
            try {
                this.workerService.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                getLogger().log(Level.FINE,
                        "Interruption while shutting down internal server", ex);
            }
        }
    }

}
