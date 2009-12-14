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
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.restlet.Response;
import org.restlet.Server;
import org.restlet.engine.ServerHelper;
import org.restlet.engine.log.LoggingThreadFactory;

/**
 * Base server helper based on NIO blocking sockets. Here is the list of
 * parameters that are supported. They should be set in the Server's context
 * before it is started:
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
 * @author Jerome Louvel
 */
public abstract class BaseServerHelper extends ServerHelper {

    /** The connection controller service. */
    private volatile ExecutorService controllerService;

    /** The connection handler service. */
    private volatile ExecutorService handlerService;

    /** The connection acceptor service. */
    private volatile ExecutorService acceptorService;

    /** The server socket channel. */
    private volatile ServerSocketChannel serverSocketChannel;

    /** The synchronization aid between listener and handler service. */
    private volatile CountDownLatch latch;

    /** The set of active connections. */
    private final Set<BaseServerConnection> connections;

    /** The queue of requests pending for handling. */
    private final Queue<ConnectedRequest> pendingRequests;

    /** The queue of responses pending for writing. */
    private final Queue<Response> pendingResponses;

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public BaseServerHelper(Server server) {
        super(server);
        this.connections = new CopyOnWriteArraySet<BaseServerConnection>();
        this.pendingRequests = new ConcurrentLinkedQueue<ConnectedRequest>();
        this.pendingResponses = new ConcurrentLinkedQueue<Response>();
    }

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
     * Creates the handler service.
     * 
     * @return The handler service.
     */
    protected ExecutorService createHandlerService() {
        int maxThreads = getMaxThreads();
        int minThreads = getMinThreads();

        ThreadPoolExecutor result = new ThreadPoolExecutor(minThreads,
                maxThreads, (long) getThreadMaxIdleTimeMs(),
                TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(),
                new LoggingThreadFactory(getLogger()));
        result.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable r,
                    ThreadPoolExecutor executor) {
                getLogger().warning("Unable to run the following task: " + r);
            }
        });
        return result;
    }

    /**
     * Creates the handler service.
     * 
     * @return The handler service.
     */
    protected ExecutorService createAcceptorService() {
        return Executors.newSingleThreadExecutor(new LoggingThreadFactory(
                getLogger()));
    }

    /**
     * Create a server socket channel and bind it to the given address
     * 
     * @return Bound server socket channel.
     * @throws IOException
     */
    protected ServerSocketChannel createServerSocket() throws IOException {
        final ServerSocketChannel server = ServerSocketChannel.open();
        server.socket().bind(createSocketAddress());
        return server;
    }

    /**
     * Creates a socket address to listen on.
     * 
     * @return The created socket address.
     * @throws IOException
     */
    protected SocketAddress createSocketAddress() throws IOException {
        if (getHelped().getAddress() == null) {
            return new InetSocketAddress(getHelped().getPort());
        }

        return new InetSocketAddress(getHelped().getAddress(), getHelped()
                .getPort());
    }

    /**
     * Returns the set of active connections.
     * 
     * @return The set of active connections.
     */
    protected Set<BaseServerConnection> getConnections() {
        return connections;
    }

    /**
     * Returns the connection handler service.
     * 
     * @return The connection handler service.
     */
    public ExecutorService getHandlerService() {
        return handlerService;
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
     * Returns the queue of requests pending for handling.
     * 
     * @return The queue of requests pending for handling.
     */
    protected Queue<ConnectedRequest> getPendingRequests() {
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
     * Returns the time for an idle thread to wait for a request or read.
     * 
     * @return The time for an idle thread to wait for a request or read.
     */
    public int getThreadMaxIdleTimeMs() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "threadMaxIdleTimeMs", "60000"));
    }

    /**
     * Controls the helper for next tasks to accomplish.
     */
    public synchronized void control() {
        // Attempts to handle the next pending request
        ConnectedRequest nextRequest = getPendingRequests().poll();

        if (nextRequest != null) {
            Response response = new Response(nextRequest);
            handle(nextRequest, response);

            if (!response.isCommitted() && response.isAutoCommitting()) {
                getPendingResponses().add(response);
                response.setCommitted(true);
            }
        }

        // Attempts to write the next pending response
        Response nextResponse = null;
        ConnectedRequest request = null;
        BaseServerConnection connection = null;
        int count = getPendingResponses().size();

        for (int i = 0; i < count; i++) {
            nextResponse = getPendingResponses().poll();
            request = (ConnectedRequest) nextResponse.getRequest();
            connection = (BaseServerConnection) request.getConnection();

            // Check if the response is indeed the next one
            // to be written for this connection
            if (connection.getInboundRequests().peek() == request) {
                // Check if a final response was received for the request
                if (!nextResponse.getStatus().isInformational()) {
                    // Remove the matching request from the inbound queue
                    connection.getInboundRequests().remove(request);
                }

                // Add the response to the outbound queue
                connection.getOutboundResponses().add(nextResponse);
            } else {
                // Put the response at the beginning of the queue
                getPendingResponses().add(nextResponse);
            }
        }

        // Control each connection for requests to read or responses to
        // write
        for (BaseServerConnection conn : getConnections()) {
            conn.control();
        }
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();

        // Create the thread services
        this.acceptorService = createAcceptorService();
        this.controllerService = createControllerService();
        this.handlerService = createHandlerService();

        // Create the server socket
        this.serverSocketChannel = createServerSocket();

        // Sets the ephemeral port is necessary
        setEphemeralPort(this.serverSocketChannel.socket());

        // Start the socket listener service
        this.latch = new CountDownLatch(1);
        this.acceptorService.submit(new ConnectionAcceptor(this,
                this.serverSocketChannel, this.latch));
        this.controllerService.submit(new ConnectionController(this));

        // Wait for the listener to start up and count down the latch
        // This blocks until the server is ready to receive connections
        try {
            this.latch.await();
        } catch (InterruptedException ex) {
            getLogger()
                    .log(
                            Level.WARNING,
                            "Interrupted while waiting for starting latch. Stopping...",
                            ex);
            stop();
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();

        if (this.handlerService != null) {
            // Gracefully shutdown the handlers, they should complete
            // in a timely fashion
            this.handlerService.shutdown();
            try {
                this.handlerService.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                getLogger().log(Level.FINE,
                        "Interruption while shutting down internal server", ex);
            }
        }

        if (this.acceptorService != null) {
            // This must be forcefully interrupted because the thread
            // is most likely blocked on channel.accept()
            this.acceptorService.shutdownNow();

            try {
                this.acceptorService.awaitTermination(30, TimeUnit.SECONDS);
            } catch (Exception ex) {
                getLogger().log(Level.FINE,
                        "Interruption while shutting down internal server", ex);
            }
        }

        // Close the server socket
        if (this.serverSocketChannel != null) {
            this.serverSocketChannel.close();
        }
    }
}
