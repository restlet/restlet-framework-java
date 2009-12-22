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
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
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
public abstract class BaseServerHelper extends
        BaseHelper<Server, ConnectedRequest, Response> {

    /** The connection acceptor service. */
    private volatile ExecutorService acceptorService;

    /** The server socket channel. */
    private volatile ServerSocketChannel serverSocketChannel;

    /** The synchronization aid between listener and handler service. */
    private volatile CountDownLatch latch;

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public BaseServerHelper(Server server) {
        super(server);

        // Clear the ephemeral port
        getAttributes().put("ephemeralPort", -1);
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

    @Override
    public void handle(Request request) {
        if (request != null) {
            ConnectedRequest connectedRequest = (ConnectedRequest) request;
            Response response = null;

            if (connectedRequest.isExpectingResponse()) {
                response = createResponse(connectedRequest);
            }

            handle(request, response);

            if ((response != null) && !response.isCommitted()
                    && response.isAutoCommitting()) {
                getPendingResponses().add(response);
                response.setCommitted(true);
            }

            if ((request.getEntity() != null)
                    && (!connectedRequest.isExpectingResponse() || ((response != null) && response
                            .isCommitted()))) {
                try {
                    request.getEntity().exhaust();
                } catch (IOException e) {
                    getLogger().log(Level.INFO,
                            "Unable to exhaust request entity", e);
                } finally {
                    request.getEntity().release();
                }
            }
        }

        handleNextResponse();
    }

    /**
     * Handles a call by invoking the helped Server's
     * {@link Server#handle(Request, Response)} method.
     * 
     * @param request
     *            The request to handle.
     * @param response
     *            The response to update.
     */
    @Override
    public void handle(Request request, Response response) {
        super.handle(request, response);
        getHelped().handle(request, response);
    }

    @Override
    public void handle(Response response) {
        if (response != null) {
            ConnectedRequest request = (ConnectedRequest) response.getRequest();
            ServerConnection connection = (ServerConnection) request
                    .getConnection();

            // Check if the response is indeed the next one
            // to be written for this connection
            if (connection.getInboundMessages().peek() == request) {
                // Check if a final response was received for the request
                if (!response.getStatus().isInformational()) {
                    // Remove the matching request from the inbound queue
                    connection.getInboundMessages().remove(request);
                }

                // Add the response to the outbound queue
                connection.getOutboundMessages().add(response);

                // Attempt to directly write the response, preventing a context
                // switching
                connection.writeMessages();
            } else {
                // Put the response at the beginning of the queue
                getPendingResponses().add(response);
            }
        }
    }

    /**
     * Sets the ephemeral port in the attributes map if necessary.
     * 
     * @param localPort
     *            The ephemeral local port.
     */
    public void setEphemeralPort(int localPort) {
        // If an ephemeral port is used, make sure we update the attribute for
        // the API
        if (getHelped().getPort() == 0) {
            getAttributes().put("ephemeralPort", localPort);
        }
    }

    /**
     * Sets the ephemeral port in the attributes map if necessary.
     * 
     * @param socket
     *            The bound server socket.
     */
    public void setEphemeralPort(ServerSocket socket) {
        setEphemeralPort(socket.getLocalPort());
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();

        // Create the thread services
        this.acceptorService = createAcceptorService();

        // Create the server socket
        this.serverSocketChannel = createServerSocket();

        // Sets the ephemeral port is necessary
        setEphemeralPort(this.serverSocketChannel.socket());

        // Start the socket listener service
        this.latch = new CountDownLatch(1);
        this.acceptorService.submit(new AcceptorTask(this,
                this.serverSocketChannel, this.latch));

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

        // Clear the ephemeral port
        getAttributes().put("ephemeralPort", -1);

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
