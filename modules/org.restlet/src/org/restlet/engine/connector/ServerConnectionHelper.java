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

package org.restlet.engine.connector;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Server;
import org.restlet.data.Status;

/**
 * Base server helper based on NIO non blocking sockets. Here is the list of
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
 * <td>useForwardedForHeader</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Lookup the "X-Forwarded-For" header supported by popular proxies and
 * caches and uses it to populate the Request.getClientAddresses() method
 * result. This information is only safe for intermediary components within your
 * local network. Other addresses could easily be changed by setting a fake
 * header and should not be trusted for serious security checks.</td>
 * </tr>
 * <tr>
 * <td>reuseAddress</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Enable/disable the SO_REUSEADDR socket option. See
 * java.io.ServerSocket#reuseAddress property for additional details.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public abstract class ServerConnectionHelper extends ConnectionHelper<Server> {

    /** The server socket channel. */
    private volatile ServerSocketChannel serverSocketChannel;

    /**
     * Constructor.
     * 
     * @param server
     *            The server to help.
     */
    public ServerConnectionHelper(Server server) {
        super(server, false);

        // Clear the ephemeral port
        getAttributes().put("ephemeralPort", -1);
    }

    /**
     * Indicates if the connection can handle the given response at this point
     * in time.
     * 
     * @param connection
     *            The parent connection.
     * @param response
     *            The response to handle.
     * @return True if the connection can handle the given response at this
     *         point in time.
     * @throws IOException
     */
    protected abstract boolean canHandle(Connection<Server> connection,
            Response response) throws IOException;

    @Override
    protected Connection<Server> createConnection(SocketChannel socketChannel,
            ConnectionController controller, InetSocketAddress socketAddress)
            throws IOException {
        return new Connection<Server>(this, socketChannel, controller,
                socketAddress, getInboundBufferSize(), getOutboundBufferSize());
    }

    @Override
    protected ServerConnectionController createController() {
        return new ServerConnectionController(this);
    }

    /**
     * Creates a new request.
     * 
     * @param connection
     *            The associated connection.
     * @param methodName
     *            The method name.
     * @param resourceUri
     *            The target resource URI.
     * @param protocol
     *            The protocol name and version.
     * @return The created request.
     */
    protected abstract Request createRequest(Connection<Server> connection,
            String methodName, String resourceUri, String protocol);

    /**
     * Create a server socket channel and bind it to the given address
     * 
     * @return Bound server socket channel.
     * @throws IOException
     */
    protected ServerSocketChannel createServerSocketChannel()
            throws IOException {
        ServerSocketChannel result = ServerSocketChannel.open();

        // Configure the server socket
        ServerSocket socket = result.socket();
        socket.setReceiveBufferSize(getSocketReceiveBufferSize());
        socket.setReuseAddress(isSocketReuseAddress());
        socket.setSoTimeout(getMaxIoIdleTimeMs());
        socket.bind(createSocketAddress());

        result.configureBlocking(false);
        return result;
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
    public void doHandleInbound(Response response) {
        if ((response != null) && (response.getRequest() != null)) {
            getLogger().finer("Handling request...");

            try {
                // Effectively handle the request
                handle(response.getRequest(), response);
            } catch (Throwable t) {
                response.setStatus(Status.SERVER_ERROR_INTERNAL, t);
            }

            if (!response.isCommitted() && response.isAutoCommitting()) {
                response.setCommitted(true);
                getOutboundMessages().add(response);
            }
        }
    }

    @Override
    public void doHandleOutbound(Response response) {
        if (response != null) {
            getLogger().finer("Handling response...");
            InboundRequest request = (InboundRequest) response.getRequest();
            Connection<Server> connection = request.getConnection();

            if (response.getRequest().isExpectingResponse()) {
                try {
                    if (canHandle(connection, response)) {
                        // Add the response to the outbound queue
                        connection.getOutboundWay().handle(response);
                    } else {
                        // Put the response at the end of the queue
                        getOutboundMessages().add(response);
                    }
                } catch (IOException e) {
                    getLogger().log(Level.FINE,
                            "Unable to handle outbound message", e);
                }
            } else {
                // The request expects no response, the connection is free to
                // read a new request.
                getLogger()
                        .fine("A response for a request expecting no one was ignored");
            }
        }
    }

    @Override
    public ServerConnectionController getController() {
        return (ServerConnectionController) super.getController();
    }

    /**
     * Returns the server socket channel.
     * 
     * @return The server socket channel.
     */
    public ServerSocketChannel getServerSocketChannel() {
        return serverSocketChannel;
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
    protected void handleInbound(Response response) {
        handleInbound(response, false);
    }

    @Override
    protected void handleOutbound(Response response) {
        handleOutbound(response, true);
    }

    @Override
    public boolean isControllerDaemon() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "controllerDaemon", "false"));
    }

    @Override
    public boolean isProxying() {
        return false;
    }

    /**
     * Indicates if the controller thread should be a daemon (not blocking JVM
     * exit).
     * 
     * @return True if the controller thread should be a daemon (not blocking
     *         JVM exit).
     */
    public boolean isReuseAddress() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "reuseAddress", "true"));
    }

    /**
     * Sets the ephemeral port in the attributes map if necessary.
     * 
     * @param localPort
     *            The ephemeral local port.
     */
    public void setEphemeralPort(int localPort) {
        // If an ephemeral port is used, make sure we update
        // the attribute for the API
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
        // Create the server socket channel
        this.serverSocketChannel = createServerSocketChannel();

        // Sets the ephemeral port is necessary
        setEphemeralPort(this.serverSocketChannel.socket());

        // Start the controller
        getLogger().info(
                "Starting the internal " + getProtocols() + " server on port "
                        + getHelped().getActualPort());
        super.start();

        // Wait for the listener to start up and count down the latch
        // This blocks until the server is ready to receive connections
        try {
            getController().await();
        } catch (InterruptedException ex) {
            getLogger()
                    .log(Level.WARNING,
                            "Interrupted while waiting for starting latch. Stopping...",
                            ex);
            stop();
        }
    }

    @Override
    public synchronized void stop() throws Exception {
        getLogger().info("Stopping the internal " + getProtocols() + " server");

        // Stop the controller
        super.stop();

        // Close the server socket channel
        if (getServerSocketChannel() != null) {
            getServerSocketChannel().close();
        }

        // Clear the ephemeral port
        getAttributes().put("ephemeralPort", -1);
    }
}
