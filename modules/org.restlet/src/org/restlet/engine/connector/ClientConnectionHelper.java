/**
 * Copyright 2005-2010 Noelios Technologies.
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

package org.restlet.engine.connector;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.channels.SocketChannel;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

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
 * <tr>
 * <td>keystorePath</td>
 * <td>String</td>
 * <td>${user.home}/.keystore</td>
 * <td>SSL keystore path.</td>
 * </tr>
 * <tr>
 * <td>keystorePassword</td>
 * <td>String</td>
 * <td>System property "javax.net.ssl.keyStorePassword"</td>
 * <td>SSL keystore password.</td>
 * </tr>
 * <tr>
 * <td>keystoreType</td>
 * <td>String</td>
 * <td>JKS</td>
 * <td>SSL keystore type</td>
 * </tr>
 * <tr>
 * <td>keyPassword</td>
 * <td>String</td>
 * <td>System property "javax.net.ssl.keyStorePassword"</td>
 * <td>SSL key password.</td>
 * </tr>
 * <tr>
 * <td>certAlgorithm</td>
 * <td>String</td>
 * <td>SunX509</td>
 * <td>SSL certificate algorithm.</td>
 * </tr>
 * <tr>
 * <td>secureRandomAlgorithm</td>
 * <td>String</td>
 * <td>null (see java.security.SecureRandom)</td>
 * <td>Name of the RNG algorithm. (see java.security.SecureRandom class).</td>
 * </tr>
 * <tr>
 * <td>securityProvider</td>
 * <td>String</td>
 * <td>null (see javax.net.ssl.SSLContext)</td>
 * <td>Java security provider name (see java.security.Provider class).</td>
 * </tr>
 * <tr>
 * <td>sslProtocol</td>
 * <td>String</td>
 * <td>TLS</td>
 * <td>SSL protocol.</td>
 * </tr>
 * <tr>
 * <td>truststoreType</td>
 * <td>String</td>
 * <td>System property "javax.net.ssl.trustStoreType"</td>
 * <td>Trust store type</td>
 * </tr>
 * <tr>
 * <td>truststorePath</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Path to trust store</td>
 * </tr>
 * <tr>
 * <td>truststorePassword</td>
 * <td>String</td>
 * <td>System property "javax.net.ssl.trustStorePassword"</td>
 * <td>Trust store password</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public abstract class ClientConnectionHelper extends ConnectionHelper<Client> {

    private static final String CONNECTOR_LATCH = "org.restlet.engine.connector.latch";

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
        return new ClientConnectionController(this);
    }

    /**
     * Creates a properly configured secure socket factory.
     * 
     * @return Properly configured secure socket factory.
     * @throws IOException
     * @throws GeneralSecurityException
     */
    protected SocketFactory createSecureSocketFactory() throws IOException,
            GeneralSecurityException {
        // Retrieve the configuration variables
        String certAlgorithm = getCertAlgorithm();
        String keystorePath = getKeystorePath();
        String keystorePassword = getKeystorePassword();
        String keyPassword = getKeyPassword();
        String truststoreType = getTruststoreType();
        String truststorePath = getTruststorePath();
        String truststorePassword = getTruststorePassword();
        String secureRandomAlgorithm = getSecureRandomAlgorithm();
        String securityProvider = getSecurityProvider();

        // Initialize a key store
        InputStream keystoreInputStream = null;
        if ((keystorePath != null) && (new File(keystorePath).exists())) {
            keystoreInputStream = new FileInputStream(keystorePath);
        }

        KeyStore keystore = null;
        if (keystoreInputStream != null) {
            try {
                keystore = KeyStore.getInstance(getKeystoreType());
                keystore.load(
                        keystoreInputStream,
                        keystorePassword == null ? null : keystorePassword
                                .toCharArray());
            } catch (IOException ioe) {
                getLogger().log(Level.WARNING, "Unable to load the key store",
                        ioe);
                keystore = null;
            }
        }

        KeyManager[] keyManagers = null;
        if ((keystore != null) && (keyPassword != null)) {
            // Initialize a key manager
            KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(certAlgorithm);
            keyManagerFactory.init(keystore, keyPassword.toCharArray());
            keyManagers = keyManagerFactory.getKeyManagers();
        }

        // Initialize the trust store
        InputStream truststoreInputStream = null;
        if ((truststorePath != null) && (new File(truststorePath).exists())) {
            truststoreInputStream = new FileInputStream(truststorePath);
        }

        KeyStore truststore = null;
        if ((truststoreType != null) && (truststoreInputStream != null)) {
            try {
                truststore = KeyStore.getInstance(truststoreType);
                truststore.load(
                        truststoreInputStream,
                        truststorePassword == null ? null : truststorePassword
                                .toCharArray());
            } catch (IOException ioe) {
                getLogger().log(Level.WARNING,
                        "Unable to load the trust store", ioe);
                truststore = null;
            }
        }

        TrustManager[] trustManagers = null;
        if (truststore != null) {
            // Initialize the trust manager
            TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(certAlgorithm);
            trustManagerFactory.init(truststore);
            trustManagers = trustManagerFactory.getTrustManagers();
        }

        // Initialize the SSL context
        SecureRandom secureRandom = secureRandomAlgorithm == null ? null
                : SecureRandom.getInstance(secureRandomAlgorithm);

        SSLContext context = securityProvider == null ? SSLContext
                .getInstance(getSslProtocol()) : SSLContext.getInstance(
                getSslProtocol(), securityProvider);
        context.init(keyManagers, trustManagers, secureRandom);

        // Return the SSL socket factory
        return context.getSocketFactory();
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

    /**
     * Creates a normal or secure socket factory.
     * 
     * @param secure
     *            Indicates if the sockets should be secured.
     * @return A normal or secure socket factory.
     */
    protected SocketFactory createSocketFactory(boolean secure) {
        SocketFactory result = null;

        if (secure) {
            try {
                return createSecureSocketFactory();
            } catch (IOException ex) {
                getLogger().log(
                        Level.SEVERE,
                        "Could not create secure socket factory: "
                                + ex.getMessage(), ex);
            } catch (GeneralSecurityException ex) {
                getLogger().log(
                        Level.SEVERE,
                        "Could not create secure socket factory: "
                                + ex.getMessage(), ex);
            }
        } else {
            result = SocketFactory.getDefault();
        }

        return result;
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
                    if (currConn.isReady()) {
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
                getLogger()
                        .log(Level.FINE,
                                "Creating a new client connection to: "
                                        + socketAddress);
                result = checkout(
                        createSocketChannel(request.isConfidential(),
                                socketAddress), getController(), socketAddress);
                getConnections().add(result);
            }
        }

        return result;
    }

    /**
     * Returns the SSL certificate algorithm.
     * 
     * @return The SSL certificate algorithm.
     */
    public String getCertAlgorithm() {
        return getHelpedParameters().getFirstValue("certAlgorithm", "SunX509");
    }

    /**
     * Returns the SSL key password.
     * 
     * @return The SSL key password.
     */
    public String getKeyPassword() {
        return getHelpedParameters().getFirstValue("keyPassword",
                System.getProperty("javax.net.ssl.keyStorePassword"));
    }

    /**
     * Returns the SSL keystore password.
     * 
     * @return The SSL keystore password.
     */
    public String getKeystorePassword() {
        return getHelpedParameters().getFirstValue("keystorePassword",
                System.getProperty("javax.net.ssl.keyStorePassword"));
    }

    /**
     * Returns the SSL keystore path.
     * 
     * @return The SSL keystore path.
     */
    public String getKeystorePath() {
        return getHelpedParameters().getFirstValue("keystorePath",
                System.getProperty("user.home") + File.separator + ".keystore");
    }

    /**
     * Returns the SSL keystore type.
     * 
     * @return The SSL keystore type.
     */
    public String getKeystoreType() {
        return getHelpedParameters().getFirstValue("keystoreType", "JKS");
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
     * Returns the name of the RNG algorithm.
     * 
     * @return The name of the RNG algorithm.
     */
    public String getSecureRandomAlgorithm() {
        return getHelpedParameters().getFirstValue("secureRandomAlgorithm",
                null);
    }

    /**
     * Returns the Java security provider name.
     * 
     * @return The Java security provider name.
     */
    public String getSecurityProvider() {
        return getHelpedParameters().getFirstValue("securityProvider", null);
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

    /**
     * Returns the SSL keystore type.
     * 
     * @return The SSL keystore type.
     */
    public String getSslProtocol() {
        return getHelpedParameters().getFirstValue("sslProtocol", "TLS");
    }

    /**
     * Returns the SSL trust store password.
     * 
     * @return The SSL trust store password.
     */
    public String getTruststorePassword() {
        return getHelpedParameters().getFirstValue("truststorePassword",
                System.getProperty("javax.net.ssl.keyStorePassword"));
    }

    /**
     * Returns the SSL trust store path.
     * 
     * @return The SSL trust store path.
     */
    public String getTruststorePath() {
        return getHelpedParameters().getFirstValue("truststorePath", null);
    }

    /**
     * Returns the SSL trust store type.
     * 
     * @return The SSL trust store type.
     */
    public String getTruststoreType() {
        return getHelpedParameters().getFirstValue("truststoreType",
                System.getProperty("javax.net.ssl.trustStoreType"));
    }

    @Override
    public void handle(Request request, Response response) {
        try {
            getLogger().finer("Handling request...");

            if (isSynchronous(request) && request.isExpectingResponse()) {
                // Prepare the latch to block the caller thread
                CountDownLatch latch = new CountDownLatch(1);
                request.getAttributes().put(CONNECTOR_LATCH, latch);

                // Add the message to the outbound queue for processing
                getOutboundMessages().add(response);

                // Wake up the controller if it is sleeping
                getController().wakeup();

                // Await on the latch
                if (getMaxIoIdleTimeMs() <= 0) {
                    latch.await();
                } else {
                    if (!latch.await(getMaxIoIdleTimeMs(),
                            TimeUnit.MILLISECONDS)) {
                        // Timeout detected
                        response.setStatus(Status.CONNECTOR_ERROR_INTERNAL,
                                "The calling thread timed out while waiting for a response to unblock it.");
                    }
                }
            } else {
                // Add the message to the outbound queue for processing
                getOutboundMessages().add(response);

                // Wake up the controller if it is sleeping
                getController().wakeup();
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
    public void handleInbound(Response response) {
        if (response != null) {
            getLogger().finer("Handling response...");

            if (response.getRequest().getOnResponse() != null) {
                response.getRequest().getOnResponse()
                        .handle(response.getRequest(), response);
            }

            if (!response.getStatus().isInformational()) {
                // Informational response shouldn't unblock a synchronous
                // call waiting for a final response.
                unblock(response);
            }
        }
    }

    @Override
    public void handleOutbound(Response response) {
        if ((response != null) && (response.getRequest() != null)) {
            try {
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
            } catch (Throwable t) {
                getLogger()
                        .log(Level.FINE,
                                "An error occured during the communication with the remote server.",
                                t);
                response.setStatus(Status.CONNECTOR_ERROR_COMMUNICATION, t);
                unblock(response);
            }
        }
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

    /**
     * Indicates if the given request is handled in a synchronous way, blocking
     * the calling thread.
     * 
     * @param request
     *            The request to test.
     * @return True if the given request is handled in a synchronous way.
     */
    public boolean isSynchronous(Request request) {
        return (request.getOnResponse() == null);
    }

    @Override
    public void onError(Status status, Response message) {
        if (message != null) {
            message.setStatus(status);
            getInboundMessages().add(message);

            // Wake up the controller if it is sleeping
            getController().wakeup();
        }
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
    private void unblock(Response response) {
        CountDownLatch latch = (CountDownLatch) response.getRequest()
                .getAttributes().get(CONNECTOR_LATCH);
        if (latch != null) {
            latch.countDown();
        }
    }

}
