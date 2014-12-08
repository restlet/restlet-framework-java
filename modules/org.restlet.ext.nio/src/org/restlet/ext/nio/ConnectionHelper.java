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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.restlet.Application;
import org.restlet.Connector;
import org.restlet.Context;
import org.restlet.Response;
import org.restlet.ext.nio.internal.connection.Connection;
import org.restlet.ext.nio.internal.connection.ConnectionPool;
import org.restlet.ext.nio.internal.controller.ConnectionController;
import org.restlet.ext.nio.internal.way.InboundWay;
import org.restlet.ext.nio.internal.way.OutboundWay;
import org.restlet.routing.VirtualHost;

/**
 * Connector helper using network connections. Here is the list of parameters
 * that are supported. They should be set in the connector's context before it
 * is started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>maxConnectionsPerHost</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>Maximum number of concurrent connections per host (IP address).</td>
 * </tr>
 * <tr>
 * <td>initialConnections</td>
 * <td>int</td>
 * <td>100</td>
 * <td>Initial number of connections pre-created in the connections pool. This
 * saves time during establishment of new connections as heavy byte buffers are
 * simply reused.</td>
 * </tr>
 * <tr>
 * <td>maxTotalConnections</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>Maximum number of concurrent connections in total.</td>
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
 * <td>pooledConnections</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Indicates if connections should be pooled to save instantiation time.</td>
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
 * <td>socketReceiveBufferSize</td>
 * <td>int</td>
 * <td>8192</td>
 * <td>The hinted size of the underlying TCP buffers used by the platform for
 * inbound network I/O.</td>
 * </tr>
 * <tr>
 * <td>socketReuseAddress</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Indicates if sockets can be reused right away even if they are busy (in
 * TIME_WAIT or 2MSL wait state).</td>
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
public abstract class ConnectionHelper<T extends Connector> extends
        BaseHelper<T> {
    /** The connection pool. */
    private volatile ConnectionPool<T> connectionPool;

    /** The set of active connections. */
    private final List<Connection<T>> connections;

    /**
     * Constructor.
     * 
     * @param connector
     *            The helped connector.
     * @param clientSide
     *            True if it is helping a client connector.
     */
    public ConnectionHelper(T connector, boolean clientSide) {
        super(connector, clientSide);
        this.connections = new CopyOnWriteArrayList<Connection<T>>();
        this.connectionPool = null;
    }

    /**
     * Add the outbound message to the queue and wake up the IO controller.
     * 
     * @param response
     *            The outbound message.
     */
    public void addOutboundMessage(Response response) {
        if (Application.getCurrent() != null) {
            response.getAttributes().put("org.restlet.application",
                    Application.getCurrent());
        }

        if (Context.getCurrent() != null) {
            response.getAttributes().put("org.restlet.context",
                    Context.getCurrent());
        }

        if (VirtualHost.getCurrent() != null) {
            response.getAttributes().put("org.restlet.virtualHost",
                    VirtualHost.getCurrent());
        }

        getOutboundMessages().add(response);
        getController().wakeup();
    }

    /**
     * Checks in the connection back into the pool.
     * 
     * @param connection
     *            The connection to check in.
     */
    @SuppressWarnings("unchecked")
    public void checkin(Connection<?> connection) {
        connection.clear();

        if (isPooledConnection()) {
            getConnectionPool().checkin((Connection<T>) connection);
        }
    }

    /**
     * Checks out a connection associated to the given socket from the pool.
     * 
     * @param socketChannel
     *            The underlying NIO socket channel.
     * @param controller
     *            The underlying IO controller.
     * @param socketAddress
     *            The associated IP address.
     * @return The new connection.
     * @throws IOException
     */
    public Connection<T> checkout(SocketChannel socketChannel,
            ConnectionController controller, InetSocketAddress socketAddress)
            throws IOException {
        Connection<T> result = null;

        if (isPooledConnection()) {
            result = getConnectionPool().checkout();
            result.reuse(socketChannel, controller, socketAddress);
        } else {
            result = createConnection(socketChannel, controller, socketAddress);
        }

        return result;
    }

    /**
     * Configures a given socket based on the helper parameters.
     * 
     * @param socket
     *            The socket to configure.
     * @throws SocketException
     */
    public void configure(Socket socket) throws SocketException {
        socket.setKeepAlive(isSocketKeepAlive());
        socket.setOOBInline(isSocketOobInline());
        socket.setReceiveBufferSize(getSocketReceiveBufferSize());
        socket.setReuseAddress(isSocketReuseAddress());
        socket.setSoLinger(getSocketLingerTimeMs() > 0, getSocketLingerTimeMs());
        socket.setSendBufferSize(getSocketSendBufferSize());
        socket.setSoTimeout(getMaxIoIdleTimeMs());
        socket.setTcpNoDelay(isSocketNoDelay());
        socket.setTrafficClass(getSocketTrafficClass());
    }

    /**
     * Creates a connection associated to the given socket.
     * 
     * @param socketChannel
     *            The underlying NIO socket channel.
     * @param controller
     *            The underlying IO controller.
     * @param socketAddress
     *            The associated IP address.
     * @return The new connection.
     * @throws IOException
     */
    public abstract Connection<T> createConnection(SocketChannel socketChannel,
            ConnectionController controller, InetSocketAddress socketAddress)
            throws IOException;

    /**
     * Creates the connection pool.
     */
    public void createConnectionPool() {
        if (isPooledConnection()) {
            this.connectionPool = new ConnectionPool<T>(this,
                    getInitialConnections());
        }
    }

    /**
     * Creates an inbound way for the given connection.
     * 
     * @param connection
     *            The parent connection.
     * @param bufferSize
     *            The byte buffer size.
     * @return The inbound way created.
     */
    public abstract InboundWay createInboundWay(Connection<T> connection,
            int bufferSize);

    /**
     * Creates an outbound way for the given connection.
     * 
     * @param connection
     *            The parent connection.
     * @param bufferSize
     *            The byte buffer size.
     * @return The outbound way created.
     */
    public abstract OutboundWay createOutboundWay(Connection<T> connection,
            int bufferSize);

    @Override
    protected void doFinishStop() {
        super.doFinishStop();

        if (isPooledConnection()) {
            this.connectionPool = null;
        }
    }

    @Override
    protected void doGracefulStop() {
        super.doGracefulStop();

        // Gracefully close the open connections
        for (Connection<T> connection : getConnections()) {
            connection.close(true);
        }
    }

    /**
     * Returns the connection pool.
     * 
     * @return The connection pool.
     */
    public ConnectionPool<T> getConnectionPool() {
        return connectionPool;
    }

    /**
     * Returns the set of active connections.
     * 
     * @return The set of active connections.
     */
    public List<Connection<T>> getConnections() {
        return connections;
    }

    /**
     * Returns the initial number of connections pre-created in the connections
     * pool.
     * 
     * @return The initial number of connections pre-created in the connections
     * 
     *         pool.
     */
    public int getInitialConnections() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "initialConnections", "100"));
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
     * platform for inbound network I/O.
     * 
     * @return The hinted size of the underlying TCP buffers used by the
     *         platform for inbound network I/O.
     */
    public int getSocketReceiveBufferSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "socketReceiveBufferSize", "8192"));

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
     * Indicates if the connection objects should be pooled to save
     * instantiation time.
     * 
     * @return True if the connection objects should be pooled.
     */
    public boolean isPooledConnection() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "pooledConnections", "true"));
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

    /**
     * Indicates if sockets can be reused right away even if they are busy (in
     * TIME_WAIT or 2MSL wait state).
     * 
     * @return True if sockets can be reused right away even if they are busy
     *         (in TIME_WAIT or 2MSL wait state).
     */
    public boolean isSocketReuseAddress() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "socketReuseAddress", "true"));
    }
}
