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

package org.restlet.ext.nio;

import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.restlet.Connector;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Status;
import org.restlet.engine.Engine;
import org.restlet.engine.connector.ConnectorHelper;
import org.restlet.engine.log.LoggingThreadFactory;
import org.restlet.ext.nio.internal.controller.ConnectionController;

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
 * <td>true (client), false (server)</td>
 * <td>Indicates if the controller thread should be a daemon (not blocking JVM
 * exit).</td>
 * </tr>
 * <tr>
 * <td>controllerSleepTimeMs</td>
 * <td>int</td>
 * <td>60 000</td>
 * <td>Time for the controller thread to sleep between each control. A value
 * strictly superior to 0 is required.</td>
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
 * <td>lowThreads</td>
 * <td>int</td>
 * <td>8</td>
 * <td>Number of worker threads determining when the connector is considered
 * overloaded. This triggers some protection actions such as not accepting new
 * connections.</td>
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
 * <td>maxIoIdleTimeMs</td>
 * <td>int</td>
 * <td>60 000</td>
 * <td>Maximum time for an idle IO connection or request to wait for an
 * operation before being closed. For an unlimited wait, use '0' as value.</td>
 * </tr>
 * <tr>
 * <td>maxThreadIdleTimeMs</td>
 * <td>int</td>
 * <td>300 000</td>
 * <td>Time for an idle thread to wait for an operation before being collected.</td>
 * </tr>
 * <tr>
 * <td>tracing</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if all messages should be printed on the standard console.</td>
 * </tr>
 * <tr>
 * <td>workerThreads</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Indicates if the processing of calls should be done via threads provided
 * by a worker service (i.e. a pool of worker threads). Note that if set to
 * false, calls will be processed a single IO selector thread, which should
 * never block, otherwise the other connections would hang.</td>
 * </tr>
 * <tr>
 * <td>inboundBufferSize</td>
 * <td>int</td>
 * <td>16 * 1024</td>
 * <td>Size of the content buffer for receiving messages.</td>
 * </tr>
 * <tr>
 * <td>outboundBufferSize</td>
 * <td>int</td>
 * <td>32 * 1024</td>
 * <td>Size of the content buffer for sending messages.</td>
 * </tr>
 * <tr>
 * <td>directBuffers</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if direct NIO buffers should be allocated instead of regular
 * buffers. See NIO's ByteBuffer Javadocs. Note that tracing must be disabled to
 * use direct buffers.</td>
 * </tr>
 * <tr>
 * <td>throttleTimeMs</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Time to wait between socket write operations in milliseconds. Can prevent
 * TCP buffer overflows.</td>
 * </tr>
 * <tr>
 * <td>transport</td>
 * <td>String</td>
 * <td>TCP</td>
 * <td>Indicates the transport protocol such as TCP or UDP.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public abstract class BaseHelper<T extends Connector> extends
        ConnectorHelper<T> {

    /** Indicates if it is helping a client connector. */
    protected final boolean clientSide;

    /** The controller task. */
    protected final ConnectionController controller;

    /** The controller service. */
    private volatile ExecutorService controllerService;

    /** The queue of inbound messages. */
    protected final Queue<Response> inboundMessages;

    /** The queue of outbound messages. */
    protected final Queue<Response> outboundMessages;

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
        this.inboundMessages = new ConcurrentLinkedQueue<Response>();
        this.outboundMessages = new ConcurrentLinkedQueue<Response>();
        this.controller = createController();
    }

    /**
     * Controls the helper for inbound or outbound messages to handle.
     * 
     * @return Indicates if some concrete activity occurred.
     */
    public boolean control() {
        boolean result = false;
        int size;

        // Control pending inbound messages
        size = getInboundMessages().size();

        for (int i = 0; i < size; i++) {
            handleInbound(getInboundMessages().poll());
        }

        // Control pending outbound messages
        size = getOutboundMessages().size();

        for (int i = 0; i < size; i++) {
            handleOutbound(getOutboundMessages().poll());
        }

        return result;
    }

    /**
     * Creates a new controller.
     * 
     * @return A new controller.
     */
    protected abstract ConnectionController createController();

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
     * Creates the request object.
     * 
     * @return The request object.
     */
    protected Request createRequest() {
        return new Request();
    }

    /**
     * Creates the handler service.
     * 
     * @return The handler service.
     */
    protected ThreadPoolExecutor createWorkerService() {
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
                        "Unable to run the following "
                                + (isClientSide() ? "client-side"
                                        : "server-side") + " task: " + r);
                traceWorkerService();
            }
        });

        // Ensure that core threads act like a minimum number of threads
        result.prestartAllCoreThreads();
        return result;
    }

    /**
     * Finish stopping the helper.
     */
    protected void doFinishStop() {
        // Await for completion of pending workers
        if (getWorkerService() != null) {
            try {
                getWorkerService().awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                getLogger().log(Level.FINE,
                        "Interruption while shutting down the worker service",
                        ex);
            }
        }

        // Stops the controller
        if (this.controllerService != null) {
            this.controller.shutdown();
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
    }

    /**
     * Do a graceful stop first.
     */
    protected void doGracefulStop() {
        // Gracefully shutdown the workers
        if (getWorkerService() != null) {
            getWorkerService().shutdown();
        }
    }

    /**
     * Effectively handles an inbound message.
     * 
     * @param response
     *            The response to handle.
     */
    public abstract void doHandleInbound(Response response);

    /**
     * Effectively handles an outbound message.
     * 
     * @param response
     *            The response to handle.
     */
    public abstract void doHandleOutbound(Response response);

    /**
     * Executes the next task in a separate thread provided by the worker
     * service, only if the worker service isn't busy.
     * 
     * @param task
     *            The next task to execute.
     */
    protected void execute(Runnable task) {
        try {
            if (!getController().isOverloaded() && (getWorkerService() != null)
                    && !getWorkerService().isShutdown()
                    && getController().isRunning()) {
                getWorkerService().execute(task);
            }
        } catch (Exception e) {
            getLogger().log(
                    Level.WARNING,
                    "Unable to execute a "
                            + (isClientSide() ? "client-side" : "server-side")
                            + " controller task", e);
        }
    }

    /**
     * Returns the controller task.
     * 
     * @return The controller task.
     */
    public ConnectionController getController() {
        return controller;
    }

    /**
     * Returns the time for the controller thread to sleep between each control.
     * 
     * @return The time for the controller thread to sleep between each control.
     */
    public int getControllerSleepTimeMs() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "controllerSleepTimeMs", "60000"));
    }

    /**
     * Returns the size of the content buffer for receiving messages.
     * 
     * @return The size of the content buffer for receiving messages.
     */
    public int getInboundBufferSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "inboundBufferSize", Integer.toString(16 * 1024)));
    }

    /**
     * Returns the queue of inbound messages pending for handling.
     * 
     * @return The queue of inbound messages.
     */
    public Queue<Response> getInboundMessages() {
        return inboundMessages;
    }

    /**
     * Returns the number of threads for the overload state.
     * 
     * @return The number of threads for the overload state.
     */
    public int getLowThreads() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "lowThreads", "8"));
    }

    /**
     * Returns the time for an idle IO connection or request to wait for an
     * operation before being closed. For an unlimited wait, use '0' as value.
     * 
     * @return The time for an idle IO connection to wait for an operation
     *         before being closed.
     */
    public int getMaxIoIdleTimeMs() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxIoIdleTimeMs", "60000"));
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
     * Returns the size of the content buffer for sending responses.
     * 
     * @return The size of the content buffer for sending responses.
     */
    public int getOutboundBufferSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "outboundBufferSize", Integer.toString(32 * 1024)));
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
     * Returns the parent request of this response.
     * 
     * @param response
     *            The response to analyze.
     * @return The parent request if available.
     */
    public Request getRequest(Response response) {
        return response.getRequest();
    }

    /**
     * Returns the time to wait between socket write operations in milliseconds.
     * Can prevent TCP buffer overflows.
     * 
     * @return The time to wait between socket write operations in milliseconds.
     */
    public int getThrottleTimeMs() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "throttleTimeMs", "0"));
    }

    /**
     * Returns the trace output stream to use if tracing is enabled.
     * 
     * @return The trace output stream to use if tracing is enabled.
     */
    public OutputStream getTraceStream() {
        return System.out;
    }

    /**
     * Returns the transport protocol.
     * 
     * @return The transport protocol.
     */
    public String getTransport() {
        return getHelpedParameters().getFirstValue("transport", "TCP");
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
     * Handle the given inbound message.
     * 
     * @param response
     *            The message to handle.
     */
    protected abstract void handleInbound(final Response response);

    /**
     * Handle the given inbound message.
     * 
     * @param response
     *            The message to handle.
     * @param synchronous
     *            True if the current thread should be used.
     */
    protected void handleInbound(final Response response, boolean synchronous) {
        if (response != null) {
            if (synchronous || !hasWorkerThreads()) {
                doHandleInbound(response);
            } else {
                execute(new Runnable() {
                    public void run() {
                        try {
                            doHandleInbound(response);
                        } finally {
                            Engine.clearThreadLocalVariables();
                        }
                    }

                    @Override
                    public String toString() {
                        return "Handle inbound messages";
                    }
                });
            }
        }
    }

    /**
     * Handle the given outbound message.
     * 
     * @param response
     *            The message to handle.
     */
    protected abstract void handleOutbound(final Response response);

    /**
     * Handle the given outbound message.
     * 
     * @param response
     *            The message to handle.
     * @param synchronous
     *            True if the current thread should be used.
     */
    protected void handleOutbound(final Response response, boolean synchronous) {
        if (response != null) {
            if (synchronous || !hasWorkerThreads()) {
                doHandleOutbound(response);
            } else {
                execute(new Runnable() {
                    public void run() {
                        try {
                            doHandleOutbound(response);
                        } finally {
                            Engine.clearThreadLocalVariables();
                        }
                    }

                    @Override
                    public String toString() {
                        return "Handle outbound messages";
                    }
                });
            }
        }
    }

    /**
     * Indicates if the worker service (pool of worker threads) is enabled.
     * 
     * @return True if the worker service (pool of worker threads) is enabled.
     */
    public boolean hasWorkerThreads() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "workerThreads", "true"));
    }

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
    public abstract boolean isControllerDaemon();

    /**
     * Indicates if direct NIO buffers should be used. Note that tracing must be
     * disabled to use direct buffers.
     * 
     * @return True if direct NIO buffers should be used.
     */
    public boolean isDirectBuffers() {
        return !isTracing()
                && Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                        "directBuffers", "false"));
    }

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
    public boolean isWorkerServiceOverloaded() {
        return (getWorkerService() != null)
                && getWorkerService().getActiveCount() >= getLowThreads();
    }

    /**
     * Called on error. Unblocks the message.
     * 
     * @param status
     *            The error status to set on the responses.
     * @param message
     *            The message to unblock.
     */
    public void onInboundError(Status status, Response message) {
        if (message != null) {
            message.setStatus(status);
            getInboundMessages().add(message);
        }
    }

    /**
     * Called on error. Unblocks the message.
     * 
     * @param status
     *            The error status to set on the responses.
     * @param message
     *            The message to unblock.
     */
    public void onOutboundError(Status status, Response message) {
        if (message != null) {
            message.setStatus(status);
            Request request = getRequest(message);

            if (request.getOnError() != null) {
                request.getOnError().handle(request, message);
            }

            getInboundMessages().add(message);
        }
    }

    @Override
    public void start() throws Exception {
        super.start();
        this.controllerService = createControllerService();

        if (hasWorkerThreads()) {
            this.workerService = createWorkerService();
        }

        this.controllerService.submit(this.controller);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        doGracefulStop();
        doFinishStop();
    }

    /**
     * Adds traces on the worker service.
     */
    public void traceWorkerService() {
        if ((getWorkerService() != null) && getLogger().isLoggable(Level.FINE)) {
            getLogger().fine(
                    "Worker service state: "
                            + (isWorkerServiceOverloaded() ? "Overloaded"
                                    : "Normal"));
            getLogger()
                    .fine("Worker service tasks: "
                            + getWorkerService().getQueue().size()
                            + " queued, " + getWorkerService().getActiveCount()
                            + " active, "
                            + getWorkerService().getCompletedTaskCount()
                            + " completed, "
                            + getWorkerService().getTaskCount() + " scheduled.");
            getLogger().fine(
                    "Worker service thread pool: "
                            + getWorkerService().getCorePoolSize()
                            + " mimimum size, "
                            + getWorkerService().getMaximumPoolSize()
                            + " maximum size, "
                            + getWorkerService().getPoolSize()
                            + " current size, "
                            + getWorkerService().getLargestPoolSize()
                            + " largest size");
        }
    }

}
