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
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Reference;
import org.restlet.data.Status;

/**
 * Base client helper based on NIO non blocking sockets. Here is the list of
 * parameters that are supported. They should be set in the Client's context
 * before it is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>proxyHost</td>
 * <td>String</td>
 * <td>System property "http.proxyHost"</td>
 * <td>The host name of the HTTP proxy.</td>
 * </tr>
 * <tr>
 * <td>proxyPort</td>
 * <td>int</td>
 * <td>System property "http.proxyPort"</td>
 * <td>The port of the HTTP proxy.</td>
 * </tr>
 * <tr>
 * <td>socketConnectTimeoutMs</td>
 * <td>int</td>
 * <td>0</td>
 * <td>The socket connection timeout or 0 for unlimited wait.</td>
 * </tr>
 * <tr>
 * <td>socketKeepAlive</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Indicates if a TCP connection should be automatically kept alive after 2
 * hours of inactivity.</td>
 * </tr>
 * <tr>
 * <td>socketOobInline</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if urgent TCP data received on the socket will be received
 * through the socket input stream.</td>
 * </tr>
 * <tr>
 * <td>socketLingerTimeMs</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>Time to block when a socket close is requested or -1 to not block at all.
 * </td>
 * </tr>
 * <tr>
 * <td>socketNoDelay</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Enables Nagle's algorithm if set to false, preventing sending of small
 * TCP packets.</td>
 * </tr>
 * <tr>
 * <td>socketSendBufferSize</td>
 * <td>int</td>
 * <td>8192</td>
 * <td>The hinted size of the underlying TCP buffers used by the platform for
 * outbound network I/O.</td>
 * </tr>
 * <tr>
 * <td>socketTrafficClass</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Type of service to set in IP packets.</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public abstract class ClientConnectionHelper extends ConnectionHelper<Client> {

    protected static final String CONNECTOR_LATCH = "org.restlet.engine.connector.latch";

    /**
     * Constructor.
     * 
     * @param connector
     *            The helped client connector.
     */
    public ClientConnectionHelper(Client connector) {
        super(connector, true);
    }

    @Override
    protected Connection<Client> createConnection(SocketChannel socketChannel,
            ConnectionController controller, InetSocketAddress socketAddress)
            throws IOException {
        return new Connection<Client>(this, socketChannel, controller,
                socketAddress, getInboundBufferSize(), getOutboundBufferSize());
    }

    @Override
    protected ConnectionController createController() {
        return new ConnectionController(this);
    }

    /**
     * Creates the socket that will be used to send the request and get the
     * response. This method is called by {@link #getBestConnection(Request)}
     * when a new connection is to be created. By default, calls the
     * {@link #createSocketChannel(boolean, String, int)} method.
     * 
     * @param secure
     *            Indicates if messages will be exchanged confidentially, for
     *            example via a SSL-secured connection.
     * @param socketAddress
     *            The holder of a host/port pair.
     * @return The created socket.
     * @throws UnknownHostException
     * @throws IOException
     */
    protected SocketChannel createSocketChannel(boolean secure,
            InetSocketAddress socketAddress) throws UnknownHostException,
            IOException {
        return createSocketChannel(secure, socketAddress.getHostName(),
                socketAddress.getPort());
    }

    /**
     * Creates the socket channel that will be used to send the request and get
     * the response.
     * 
     * @param secure
     *            Indicates if messages will be exchanged confidentially, for
     *            example via a SSL-secured connection.
     * @param hostDomain
     *            The target host domain name.
     * @param hostPort
     *            The target host port.
     * @return The created socket channel.
     * @throws UnknownHostException
     * @throws IOException
     */
    protected SocketChannel createSocketChannel(boolean secure,
            String hostDomain, int hostPort) throws UnknownHostException,
            IOException {
        SocketChannel result = SocketChannel.open();
        result.configureBlocking(false);

        // Configure socket
        Socket socket = result.socket();
        socket.setKeepAlive(isSocketKeepAlive());
        socket.setOOBInline(isSocketOobInline());
        socket.setReceiveBufferSize(getSocketReceiveBufferSize());
        socket.setReuseAddress(isSocketReuseAddress());
        socket.setSoLinger(getSocketLingerTimeMs() > 0, getSocketLingerTimeMs());
        socket.setSendBufferSize(getSocketSendBufferSize());
        socket.setSoTimeout(getMaxIoIdleTimeMs());
        socket.setTcpNoDelay(isSocketNoDelay());
        socket.setTrafficClass(getSocketTrafficClass());

        InetSocketAddress address = new InetSocketAddress(hostDomain, hostPort);
        result.connect(address);
        return result;
    }

    @Override
    public void doHandleInbound(Response response) {
        if (response != null) {
            getLogger().finer("Handling response...");
            boolean handled = false;
            Request request = response.getRequest();

            if ((request != null) && (request.isAsynchronous())) {
                request.getOnResponse().handle(request, response);
                handled = true;
            }

            if (!response.getStatus().isInformational()) {
                // Informational response shouldn't unblock a synchronous
                // call waiting for a final response.
                unblock(response);
            } else if (!handled) {
                getLogger().info("Provisional response ignored: " + response);
            }
        }
    }

    @Override
    public void doHandleOutbound(Response response) {
        try {
            if ((response != null) && (response.getRequest() != null)) {
                if (getLogger().isLoggable(Level.FINE)) {
                    getLogger().log(
                            Level.FINE,
                            "Client request to be sent: "
                                    + response.getRequest());
                }

                Connection<Client> bestConn = getBestConnection(response
                        .getRequest());

                if (bestConn != null) {
                    bestConn.getOutboundWay().handle(response);
                    getConnections().add(bestConn);
                } else {
                    getLogger().log(Level.WARNING,
                            "Unable to find a connection to send the request");
                    response.setStatus(Status.CONNECTOR_ERROR_COMMUNICATION,
                            "Unable to find a connection to send the request");
                    unblock(response);
                }
            }
        } catch (Throwable t) {
            getLogger()
                    .log(Level.FINE,
                            "An error occured during the communication with the remote server.",
                            t);
            response.setStatus(Status.CONNECTOR_ERROR_COMMUNICATION, t);
            unblock(response);
        }
    }

    /**
     * Tries to reuse an existing connection for the given request, or creates a
     * new one. It may return null if the maximum number of connections per host
     * or in general is reached.
     * 
     * @param request
     *            The request to handle.
     * @return An existing connection able to handle the request or new one.
     * @throws UnknownHostException
     * @throws IOException
     */
    protected Connection<Client> getBestConnection(Request request)
            throws UnknownHostException, IOException {
        Connection<Client> result = null;

        // Try to reuse an existing connection for the same host and
        // port
        int hostConnectionCount = 0;
        int bestScore = 0;
        boolean foundConn = false;

        // Determine the target host domain and port of the request.
        InetSocketAddress socketAddress = getSocketAddress(request);

        if (socketAddress == null) {
            getLogger()
                    .log(Level.WARNING,
                            "Unable to create a socket address related to the request.");
        } else {
            // Associate the given request to the first available connection
            // opened on the same host domain and port.
            for (Iterator<Connection<Client>> iterator = getConnections()
                    .iterator(); !foundConn && iterator.hasNext();) {
                Connection<Client> currConn = iterator.next();

                if (socketAddress.equals(currConn.getSocketAddress())) {
                    if (currConn.isAvailable()) {
                        result = currConn;
                        foundConn = true;
                    } else {
                        // Assign the request to the busy connection that
                        // handles the less number of messages. This is useful
                        // in case the maximum number of connections has been
                        // reached. As a drawback, the message will only be
                        // handled as soon as possible.
                        int currScore = currConn.getLoadScore();

                        if (bestScore > currScore) {
                            bestScore = currScore;
                            result = currConn;
                        }

                        hostConnectionCount++;
                    }
                }
            }

            // No connection has been found, try to create a new one that will
            // handle the message soon.
            if (foundConn) {
                getLogger().log(
                        Level.FINE,
                        "Reusing an existing client connection to: "
                                + socketAddress);
            } else if ((getMaxTotalConnections() != -1)
                    && (getConnections().size() >= getMaxTotalConnections())) {
                getLogger()
                        .log(Level.WARNING,
                                "Unable to create a new connection. Maximum total number of connections reached!");
            } else if ((getMaxConnectionsPerHost() != -1)
                    && (hostConnectionCount >= getMaxConnectionsPerHost())) {
                getLogger()
                        .log(Level.WARNING,
                                "Unable to create a new connection. Maximum number of connections reached for host: "
                                        + socketAddress);
            } else {
                // Create a new connection
                if (getLogger().isLoggable(Level.FINE)) {
                    getLogger().log(
                            Level.FINE,
                            "Creating a new client connection to: "
                                    + socketAddress);
                }

                result = checkout(
                        createSocketChannel(request.isConfidential(),
                                socketAddress), getController(), socketAddress);
                getConnections().add(result);
            }
        }

        return result;
    }

    /**
     * Returns the host name of the HTTP proxy, if specified.
     * 
     * @return the host name of the HTTP proxy, if specified.
     */
    public String getProxyHost() {
        return getHelpedParameters().getFirstValue("proxyHost",
                System.getProperty("http.proxyHost"));
    }

    /**
     * Returns the port of the HTTP proxy, if specified, 3128 otherwise.
     * 
     * @return the port of the HTTP proxy.
     */
    public int getProxyPort() {
        String proxyPort = getHelpedParameters().getFirstValue("proxyPort",
                System.getProperty("http.proxyPort"));

        if (proxyPort == null) {
            proxyPort = "3128";
        }

        return Integer.parseInt(proxyPort);
    }

    /**
     * Returns an IP socket address representing the target host domain and port
     * for a given request. If the helper relies on a proxy, the socket
     * represents the domain and port of the proxy host. Used by the
     * {@link #getBestConnection(Request)} method.
     * 
     * @param request
     *            The given request
     * @return The IP socket address representing the target host domain and
     *         port for a given request.
     * @throws UnknownHostException
     *             If the proxy port is invalid or the host unresolved.
     */
    protected InetSocketAddress getSocketAddress(Request request)
            throws UnknownHostException {
        InetSocketAddress result = null;
        String hostDomain = null;
        int hostPort = 0;

        // Does this helper relies on a proxy?
        String proxyDomain = getProxyHost();

        if (proxyDomain != null && !"".equals(proxyDomain)) {
            hostDomain = proxyDomain;
            try {
                hostPort = getProxyPort();
            } catch (NumberFormatException nfe) {
                getLogger().log(Level.WARNING,
                        "The proxy port must be a valid numeric value.", nfe);
                throw new UnknownHostException();
            }
        } else {
            // Resolve relative references
            Reference resourceRef = request.getResourceRef().isRelative() ? request
                    .getResourceRef().getTargetRef() : request.getResourceRef();

            // Extract the host info
            hostDomain = resourceRef.getHostDomain();
            hostPort = resourceRef.getHostPort();
            if (hostPort == -1) {
                if (resourceRef.getSchemeProtocol() != null) {
                    hostPort = resourceRef.getSchemeProtocol().getDefaultPort();
                } else {
                    hostPort = getProtocols().get(0).getDefaultPort();
                }
            }
        }

        if (hostDomain != null) {
            result = new InetSocketAddress(hostDomain, hostPort);
            if (result != null && result.getAddress() == null) {
                throw new UnknownHostException(hostDomain);
            }
        }

        return result;
    }

    /**
     * Returns the socket connection timeout.
     * 
     * @return The socket connection timeout.
     */
    @SuppressWarnings("deprecation")
    public int getSocketConnectTimeoutMs() {
        int result = getHelped().getConnectTimeout();

        if (getHelpedParameters().getNames().contains("socketConnectTimeoutMs")) {
            result = Integer.parseInt(getHelpedParameters().getFirstValue(
                    "socketConnectTimeoutMs", "0"));
        }

        return result;
    }

    /**
     * Returns the time to block when a socket close is requested or -1 to not
     * block at all.
     * 
     * @return The time to block when a socket close is requested or -1 to not
     *         block at all.
     */
    public int getSocketLingerTimeMs() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "socketLingerTimeMs", "-1"));

    }

    /**
     * Returns the hinted size of the underlying TCP buffers used by the
     * platform for outbound network I/O.
     * 
     * @return The hinted size of the underlying TCP buffers used by the
     *         platform for outbound network I/O.
     */
    public int getSocketSendBufferSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "socketSendBufferSize", "8192"));

    }

    /**
     * Returns the type of service to set in IP packets.
     * 
     * @return The type of service to set in IP packets.
     */
    public int getSocketTrafficClass() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "socketTrafficClass", "0"));

    }

    @Override
    public void handle(Request request, Response response) {
        try {
            if (getLogger().isLoggable(Level.FINE)) {
                getLogger().log(Level.FINE,
                        "Handling client request: " + request);
            }

            if ((request != null) && request.isSynchronous()
                    && request.isExpectingResponse()) {
                // Prepare the latch to block the caller thread
                CountDownLatch latch = new CountDownLatch(1);
                request.getAttributes().put(CONNECTOR_LATCH, latch);

                // Add the message to the outbound queue for processing
                getOutboundMessages().add(response);

                // Await on the latch
                latch.await();
            } else {
                // Add the message to the outbound queue for processing
                getOutboundMessages().add(response);
            }
        } catch (Exception e) {
            getLogger().log(
                    Level.INFO,
                    "Error while handling a " + request.getProtocol().getName()
                            + " client request", e);
            response.setStatus(Status.CONNECTOR_ERROR_INTERNAL, e);
        }
    }

    @Override
    protected void handleInbound(Response response) {
        handleInbound(response, response.getRequest().isSynchronous());
    }

    @Override
    protected void handleOutbound(Response response) {
        handleOutbound(response, true);
    }

    @Override
    public boolean isControllerDaemon() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "controllerDaemon", "true"));
    }

    @Override
    public boolean isProxying() {
        return getProxyHost() != null;
    }

    /**
     * Indicates if a TCP connection should be automatically kept alive after 2
     * hours of inactivity.
     * 
     * @return True if a TCP connection should be automatically kept alive after
     *         2 hours of inactivity.
     */
    public boolean isSocketKeepAlive() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "socketKeepAlive", "true"));
    }

    /**
     * Enables Nagle's algorithm if set to false, preventing sending of small
     * TCP packets.
     * 
     * @return True if Nagle's algorithm should be disabled.
     */
    public boolean isSocketNoDelay() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "socketNoDelay", "false"));
    }

    /**
     * Indicates if urgent TCP data received on the socket will be received
     * through the socket input stream.
     * 
     * @return True if urgent TCP data received on the socket will be received
     *         through the socket input stream.
     */
    public boolean isSocketOobInline() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "socketOobInline", "false"));
    }

    @Override
    public void start() throws Exception {
        getLogger().info("Starting the internal " + getProtocols() + " client");
        super.start();
    }

    @Override
    public void stop() throws Exception {
        getLogger().info("Stopping the internal" + getProtocols() + " client");
        super.stop();
    }

    /**
     * Unblocks the thread that handles the given request/response pair.
     * 
     * @param response
     *            The response.
     */
    protected void unblock(Response response) {
        if (response.getRequest() != null) {
            CountDownLatch latch = (CountDownLatch) response.getRequest()
                    .getAttributes().get(CONNECTOR_LATCH);

            if (latch != null) {
                latch.countDown();
            }
        } else {
            getLogger().warning(
                    "The client of the following response couldn't be unblocked: "
                            + response);
        }
    }

}
