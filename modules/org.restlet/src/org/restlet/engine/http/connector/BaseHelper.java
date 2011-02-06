/**
 * Copyright 2005-2011 Noelios Technologies.
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
import java.nio.channels.SocketChannel;
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
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.engine.ConnectorHelper;
import org.restlet.engine.io.IoUtils;
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
 * <td>controllerDaemon</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Indicates if the controller thread should be a daemon (not blocking JVM
 * exit).</td>
 * </tr>
 * <tr>
 * <td>controllerSleepTimeMs</td>
 * <td>int</td>
 * <td>100</td>
 * <td>Time for the controller thread to sleep between each control.</td>
 * </tr>
 * <tr>
 * <td>inboundBufferSize</td>
 * <td>int</td>
 * <td>{@link IoUtils#BUFFER_SIZE}</td>
 * <td>The size of the buffer when reading messages.</td>
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
 * <td>10</td>
 * <td>Maximum threads that will service requests.</td>
 * </tr>
 * <tr>
 * <td>maxConnectionsPerHost</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>Maximum number of concurrent connections per host (IP address).</td>
 * </tr>
 * <tr>
 * <td>maxTotalConnections</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>Maximum number of concurrent connections in total.</td>
 * </tr>
 * <tr>
 * <td>outboundBufferSize</td>
 * <td>int</td>
 * <td>{@link IoUtils#BUFFER_SIZE}</td>
 * <td>The size of the buffer when writing messages.</td>
 * </tr>
 * <tr>
 * <td>persistingConnections</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Indicates if connections should be kept alive after a call.</td>
 * </tr>
 * <tr>
 * <td>pipeliningConnections</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if pipelining connections are supported.</td>
 * </tr>
 * <tr>
 * <td>threadMaxIdleTimeMs</td>
 * <td>int</td>
 * <td>60000</td>
 * <td>Time for an idle thread to wait for an operation before being collected.</td>
 * </tr>
 * <tr>
 * <td>tracing</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if all messages should be printed on the standard console.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public abstract class BaseHelper<T extends Connector> extends
        ConnectorHelper<T> {

    /** Indicates if it is helping a client connector. */
    private final boolean clientSide;

    /** The set of active connections. */
    private final Set<Connection<T>> connections;

    /** The controller task. */
    private final Controller controller;

    /** The controller service. */
    private volatile ExecutorService controllerService;

    /** The queue of inbound messages. */
    private final Queue<Response> inboundMessages;

    /** The queue of outbound messages. */
    private final Queue<Response> outboundMessages;

    /** The worker service. */
    private volatile ThreadPoolExecutor workerService;

    /**
     * Constructor.
     * 
     * @param connector
     *            The helped connector.
     * @param clientSide
     *            True if it is helping a client connector.
     */
    public BaseHelper(T connector, boolean clientSide) {
        super(connector);
        this.clientSide = clientSide;
        this.connections = new CopyOnWriteArraySet<Connection<T>>();
        this.inboundMessages = new ConcurrentLinkedQueue<Response>();
        this.outboundMessages = new ConcurrentLinkedQueue<Response>();
        this.controller = createController();
        this.workerService = null;
    }

    /**
     * Creates a connection associated to the given socket.
     * 
     * @param helper
     *            The parent helper.
     * @param socket
     *            The underlying BIO socket.
     * @param socketChannel
     *            The underlying NIO socket channel.
     * @return The new connection.
     * @throws IOException
     */
    protected abstract Connection<T> createConnection(BaseHelper<T> helper,
            Socket socket, SocketChannel socketChannel) throws IOException;

    /**
     * Creates a new controller.
     * 
     * @return A new controller.
     */
    protected Controller createController() {
        return new Controller(this);
    }

    /**
     * Creates the connector controller service.
     * 
     * @return The connector controller service.
     */
    protected ExecutorService createControllerService() {
        return Executors.newSingleThreadExecutor(new LoggingThreadFactory(
                getLogger(), isControllerDaemon()));
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
                        getLogger(), true));
        result.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r,
                    ThreadPoolExecutor executor) {
                getLogger().warning(
                        "Unable to run the following "
                                + (isClientSide() ? "client-side"
                                        : "server-side") + " task: " + r);
                getLogger().info(
                        "Worker service state: "
                                + (isWorkerServiceFull() ? "Full" : "Normal"));
                getLogger().info(
                        "Worker service tasks: "
                                + getWorkerService().getQueue().size()
                                + " queued, "
                                + getWorkerService().getActiveCount()
                                + " active, "
                                + getWorkerService().getCompletedTaskCount()
                                + " completed, "
                                + getWorkerService().getTaskCount()
                                + " scheduled.");
                getLogger().info(
                        "Worker service thread pool: "
                                + getWorkerService().getCorePoolSize()
                                + " core size, "
                                + getWorkerService().getLargestPoolSize()
                                + " largest size, "
                                + getWorkerService().getMaximumPoolSize()
                                + " maximum size, "
                                + getWorkerService().getPoolSize()
                                + " current size");
            }
        });
        return result;
    }

    /**
     * Returns the set of active connections.
     * 
     * @return The set of active connections.
     */
    protected Set<Connection<T>> getConnections() {
        return connections;
    }

    /**
     * Returns the controller task.
     * 
     * @return The controller task.
     */
    public Controller getController() {
        return controller;
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
     * Returns the size of the buffer when reading messages..
     * 
     * @return The size of the buffer when reading messages..
     */
    public int getInboundBufferSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "inboundBufferSize", Integer.toString(IoUtils.BUFFER_SIZE)));
    }

    /**
     * Returns the queue of inbound messages pending for handling.
     * 
     * @return The queue of inbound messages.
     */
    protected Queue<Response> getInboundMessages() {
        return inboundMessages;
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
                "maxThreads", "10"));
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
     * Returns the size of the buffer when writing messages..
     * 
     * @return The size of the buffer when writing messages..
     */
    public int getOutboundBufferSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "outboundBufferSize", Integer.toString(IoUtils.BUFFER_SIZE)));
    }

    /**
     * Returns the queue of outbound messages pending for handling.
     * 
     * @return The queue of outbound messages.
     */
    protected Queue<Response> getOutboundMessages() {
        return outboundMessages;
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
     * Handles an inbound message.
     * 
     * @param response
     *            The response to handle.
     */
    public abstract void handleInbound(Response response);

    /**
     * Handles the next inbound message.
     */
    public void handleNextInbound() {
        handleInbound(getInboundMessages().poll());
    }

    /**
     * Handles the next outbound message.
     */
    protected void handleNextOutbound() {
        handleOutbound(getOutboundMessages().poll());
    }

    /**
     * Handles an outbound message.
     * 
     * @param response
     *            The response to handle.
     */
    public abstract void handleOutbound(Response response);

    /**
     * Indicates if it is helping a client connector.
     * 
     * @return True if it is helping a client connector.
     */
    public boolean isClientSide() {
        return clientSide;
    }

    /**
     * Indicates if the controller thread should be a daemon (not blocking JVM
     * exit).
     * 
     * @return True if the controller thread should be a daemon (not blocking
     *         JVM exit).
     */
    public boolean isControllerDaemon() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "controllerDaemon", "true"));
    }

    /**
     * Indicates if persistent connections should be used if possible.
     * 
     * @return True if persistent connections should be used if possible.
     */
    public boolean isPersistingConnections() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "persistingConnections", "true"));
    }

    /**
     * Indicates if pipelining connections are supported.
     * 
     * @return True if pipelining connections are supported.
     */
    public boolean isPipeliningConnections() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "pipeliningConnections", "false"));
    }

    /**
     * Indicates if the helper is going through a client proxy or is a server
     * proxy.
     * 
     * @return True if the helper is going through a client proxy or is a server
     *         proxy.
     */
    public abstract boolean isProxying();

    /**
     * Indicates if it is helping a server connector.
     * 
     * @return True if it is helping a server connector.
     */
    public boolean isServerSide() {
        return !isClientSide();
    }

    /**
     * Indicates if console tracing is enabled.
     * 
     * @return True if console tracing is enabled.
     */
    public boolean isTracing() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "tracing", "false"));
    }

    /**
     * Indicates if the worker service is busy. This state is detected by
     * checking if the number of active task running is superior or equal to the
     * maximum pool size.
     * 
     * @return True if the worker service is busy.
     */
    protected boolean isWorkerServiceFull() {
        return (getWorkerService().getActiveCount()) >= (getWorkerService()
                .getMaximumPoolSize());
    }

    @Override
    public void start() throws Exception {
        super.start();
        this.controllerService = createControllerService();
        this.workerService = createWorkerService();
        this.controllerService.submit(this.controller);
    }

    @Override
    public void stop() throws Exception {
        // For atomicity purpose
        ThreadPoolExecutor workerService = getWorkerService();

        // Stop accepting connections
        super.stop();

        // Gracefully shutdown the workers
        if (workerService != null) {
            workerService.shutdown();
        }

        // Close the open connections
        for (Connection<T> connection : getConnections()) {
            connection.setState(ConnectionState.CLOSING);
            connection.setInboundBusy(false);
            connection.setOutboundBusy(false);
        }

        // Await for completion of pending workers
        if (workerService != null) {
            try {
                workerService.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                getLogger().log(Level.FINE,
                        "Interruption while shutting down the worker service",
                        ex);
            }
        }

        // Stops the controller
        if (this.controllerService != null) {
            this.controller.setRunning(false);
            this.controllerService.shutdown();

            try {
                this.controllerService.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                getLogger()
                        .log(Level.FINE,
                                "Interruption while shutting down the controller service",
                                ex);
            }
        }
        for (Connection<T> connection : getConnections()) {
            getLogger().log(
                    Level.WARNING,
                    "The controller is shutdown whereas a connection was found in the state: "
                            + connection.getState());
        }
    }

}
