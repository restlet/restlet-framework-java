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

package org.restlet.ext.jetty;

import java.io.IOException;
import java.net.CookieStore;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.logging.Level;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.Protocol;
import org.restlet.engine.adapter.ClientCall;
import org.restlet.engine.ssl.DefaultSslContextFactory;
import org.restlet.engine.util.ReferenceUtils;
import org.restlet.ext.jetty.internal.JettyClientCall;
import org.restlet.ext.jetty.internal.RestletSslContextFactory;

/**
 * HTTP client connector using the Jetty project. Here is the list of parameters
 * that are supported. They should be set in the Client's context before it is
 * started:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>addressResolutionTimeout</td>
 * <td>long</td>
 * <td>15000</td>
 * <td>The timeout in milliseconds for the DNS resolution of host addresses</td>
 * </tr>
 * <tr>
 * <td>bindAddress</td>
 * <td>String</td>
 * <td>null</td>
 * <td>The address to bind socket channels to. You must set <i>both</i> this and
 * bindPort</td>
 * </tr>
 * <tr>
 * <td>bindPort</td>
 * <td>int</td>
 * <td>null</td>
 * <td>The address to bind socket channels to. You must set <i>both</i> this and
 * bindAddress</td>
 * </tr>
 * <tr>
 * <td>connectTimeout</td>
 * <td>long</td>
 * <td>15000</td>
 * <td>The max time in milliseconds a connection can take to connect to
 * destinations</td>
 * </tr>
 * <tr>
 * <td>dispatchIo</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Whether to dispatch I/O operations from the selector thread to a
 * different thread</td>
 * </tr>
 * <tr>
 * <td>followRedirects</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Whether to follow HTTP redirects</td>
 * </tr>
 * <tr>
 * <td>idleTimeout</td>
 * <td>long</td>
 * <td>60000</td>
 * <td>The max time in milliseconds a connection can be idle (that is, without
 * traffic of bytes in either direction)</td>
 * </tr>
 * <tr>
 * <td>maxConnectionsPerDestination</td>
 * <td>int</td>
 * <td>10</td>
 * <td>Sets the max number of connections to open to each destination</td>
 * </tr>
 * <tr>
 * <td>maxRedirects</td>
 * <td>int</td>
 * <td>8</td>
 * <td>The max number of HTTP redirects that are followed</td>
 * </tr>
 * <tr>
 * <td>maxRequestsQueuedPerDestination</td>
 * <td>int</td>
 * <td>1024</td>
 * <td>Sets the max number of requests that may be queued to a destination</td>
 * </tr>
 * <tr>
 * <td>requestBufferSize</td>
 * <td>int</td>
 * <td>4096</td>
 * <td>The size in bytes of the buffer used to write requests</td>
 * </tr>
 * <tr>
 * <td>responseBufferSize</td>
 * <td>int</td>
 * <td>16384</td>
 * <td>The size in bytes of the buffer used to read responses</td>
 * </tr>
 * <tr>
 * <td>stopTimeout</td>
 * <td>long</td>
 * <td>60000</td>
 * <td>Stop timeout in milliseconds; the maximum time allowed for the service to
 * shutdown</td>
 * </tr>
 * <tr>
 * <td>strictEventOrdering</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Whether request events must be strictly ordered</td>
 * </tr>
 * <tr>
 * <td>tcpNoDelay</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Whether TCP_NODELAY is enabled</td>
 * </tr>
 * <tr>
 * <td>userAgentField</td>
 * <td>String</td>
 * <td>null</td>
 * <td>The "User-Agent" HTTP header string; when null, uses the Jetty default</td>
 * </tr>
 * <tr>
 * <td>sslContextFactory</td>
 * <td>String</td>
 * <td>org.restlet.ext.ssl.DefaultSslContextFactory</td>
 * <td>Let you specify a {@link SslContextFactory} qualified class name as a
 * parameter, or an instance as an attribute for a more complete and flexible
 * SSL context setting</td>
 * </tr>
 * </table>
 * For the default SSL parameters see the Javadocs of the
 * {@link DefaultSslContextFactory} class.
 * 
 * @see <a href="http://www.eclipse.org/jetty/">Jetty home page</a>
 * @author Jerome Louvel
 * @author Tal Liron
 */
public class HttpClientHelper extends
        org.restlet.engine.adapter.HttpClientHelper {

    /** The timeout in milliseconds for the DNS resolution of host addresses */
    private long addressResolutionTimeout = 15000;

    /**
     * The address to bind socket channels to. You must set <i>both</i> this and
     * bindPort.
     */
    private String bindAddress = null;

    /**
     * The address to bind socket channels to. You must set <i>both</i> this and
     * bindAddress.
     */
    private int bindPort;

    /**
     * The max time in milliseconds a connection can take to connect to
     * destinations.
     */
    private long connectTimeout = 15000;

    /**
     * Indicates whether to dispatch I/O operations from the selector thread to
     * a different thread.
     */
    private boolean dispatchingIo = true;

    /** Indicates whether to follow HTTP redirects */
    private boolean followingRedirects = true;

    /** The wrapped Jetty HTTP client. */
    private volatile HttpClient httpClient;

    /**
     * The max time in milliseconds a connection can be idle (that is, without
     * traffic of bytes in either direction).
     */
    private long idleTimeout = 60000;

    /** The max number of connections to open to each destination */
    private int maxConnectionsPerDestination = 10;

    /** The max number of HTTP redirects that are followed */
    private int maxRedirects = 8;

    /** The max number of requests that may be queued to a destination */
    private int maxRequestsQueuedPerDestination = 1024;

    /** The size in bytes of the buffer used to write requests */
    private int requestBufferSize = 4096;

    /** The size in bytes of the buffer used to read responses */
    private int responseBufferSize = 16384;

    /**
     * Stop timeout in milliseconds; the maximum time allowed for the service
     * toshutdown
     */
    private long stopTimeout = 60000;

    /** Indicates whether request events must be strictly ordered */
    private boolean strictEventOrdering = false;

    /** Indicates whether TCP_NODELAY is enabled */
    private boolean tcpNoDelay = true;

    /** The "User-Agent" HTTP header string; when null, uses the Jetty default */
    private String userAgentField = null;

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public HttpClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.HTTP);
        getProtocols().add(Protocol.HTTPS);
    }

    /**
     * Creates a low-level HTTP client call from a high-level uniform call.
     * 
     * @param request
     *            The high-level request.
     * @return A low-level HTTP client call.
     */
    public ClientCall create(Request request) {
        ClientCall result = null;

        try {
            result = new JettyClientCall(this, request.getMethod().toString(),
                    ReferenceUtils.update(request.getResourceRef(), request)
                            .toString());
        } catch (IOException e) {
            getLogger().log(Level.WARNING,
                    "Unable to create the Jetty HTTP/HTTPS client call", e);
        }

        return result;
    }

    /**
     * Creates a Jetty HTTP client.
     * 
     * @return A new HTTP client.
     */
    private HttpClient createHttpClient() {
        SslContextFactory sslContextFactory = null;

        try {
            sslContextFactory = new RestletSslContextFactory(
                    org.restlet.engine.ssl.SslUtils.getSslContextFactory(this));
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Unable to create the SSL context factory.", e);
        }

        HttpClient httpClient = new HttpClient(sslContextFactory);
        httpClient.setAddressResolutionTimeout(getAddressResolutionTimeout());
        httpClient.setBindAddress(getBindAddress());
        httpClient.setConnectTimeout(getConnectTimeout());

        CookieStore cookieStore = getCookieStore();

        if (cookieStore != null) {
            httpClient.setCookieStore(cookieStore);
        }

        httpClient.setDispatchIO(isDispatchIO());
        httpClient.setExecutor(getExecutor());
        httpClient.setFollowRedirects(isFollowRedirects());
        httpClient.setIdleTimeout(getIdleTimeout());
        httpClient
                .setMaxConnectionsPerDestination(getMaxConnectionsPerDestination());
        httpClient.setMaxRedirects(getMaxRedirects());
        httpClient
                .setMaxRequestsQueuedPerDestination(getMaxRequestsQueuedPerDestination());
        httpClient.setRequestBufferSize(getRequestBufferSize());
        httpClient.setResponseBufferSize(getResponseBufferSize());
        httpClient.setScheduler(getScheduler());
        httpClient.setStopTimeout(getStopTimeout());
        httpClient.setStrictEventOrdering(isStrictEventOrdering());
        httpClient.setTCPNoDelay(isTcpNoDelay());
        String userAgentField = getUserAgentField();

        if (userAgentField != null) {
            httpClient.setUserAgentField(new HttpField(HttpHeader.USER_AGENT,
                    userAgentField));
        }

        return httpClient;
    }

    /**
     * Returns the timeout in milliseconds for the DNS resolution of host
     * addresses
     * 
     * @return The timeout in milliseconds for the DNS resolution of host
     *         addresses
     */
    public long getAddressResolutionTimeout() {
        return addressResolutionTimeout;
    }

    /**
     * The address to bind socket channels to. Default to null.
     * 
     * @return The bind address or null.
     */
    public SocketAddress getBindAddress() {
        if ((bindAddress != null))
            return new InetSocketAddress(bindAddress, bindPort);
        return null;
    }

    /**
     * Returns the address to bind socket channels to. You must set <i>both</i>
     * this and bindAddress.
     * 
     * @return The address to bind socket channels to. You must set <i>both</i>
     *         this and bindAddress.
     */
    public int getBindPort() {
        return bindPort;
    }

    /**
     * Returns the max time in milliseconds a connection can take to connect to
     * destinations.
     * 
     * @return The max time in milliseconds a connection can take to connect to
     *         destinations.
     */
    public long getConnectTimeout() {
        return connectTimeout;
    }

    /**
     * The cookie store. Defaults to null. When null, creates a new instance of
     * {@link java.net.InMemoryCookieStore}.
     * 
     * @return The cookie store.
     */
    public CookieStore getCookieStore() {
        return null;
    }

    /**
     * The executor. Defaults to null. When null, creates a new instance of
     * {@link QueuedThreadPool}.
     * 
     * @return The executor.
     */
    public Executor getExecutor() {
        return null;
    }

    /**
     * Returns the wrapped Jetty HTTP client.
     * 
     * @return The wrapped Jetty HTTP client.
     */
    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * Returns the max time in milliseconds a connection can be idle (that is,
     * without traffic of bytes in either direction).
     * 
     * @return The max time in milliseconds a connection can be idle (that is,
     *         without traffic of bytes in either direction).
     */
    public long getIdleTimeout() {
        return idleTimeout;
    }

    /**
     * Returns the max number of connections to open to each destination.
     * Defaults to 10.
     * <p>
     * RFC 2616 suggests that 2 connections should be opened per each
     * destination, but browsers commonly open 6. If this client is used for
     * load testing, it is common to have only one destination (the server to
     * load test), and it is recommended to set this value to a high value (at
     * least as much as the threads present in the {@link #getExecutor()
     * executor}).
     * 
     * @return The max number of connections to open to each destination
     */
    public int getMaxConnectionsPerDestination() {
        return maxConnectionsPerDestination;
    }

    /**
     * Returns the max number of HTTP redirects that are followed. Defaults to
     * 8.
     * 
     * @return The max number of HTTP redirects that are followed
     */
    public int getMaxRedirects() {
        return maxRedirects;
    }

    /**
     * Sets the max number of requests that may be queued to a destination.
     * Defaults to 1024.
     * <p>
     * If this client performs a high rate of requests to a destination, and all
     * the connections managed by that destination are busy with other requests,
     * then new requests will be queued up in the destination. This parameter
     * controls how many requests can be queued before starting to reject them.
     * If this client is used for load testing, it is common to have this
     * parameter set to a high value, although this may impact latency (requests
     * sit in the queue for a long time before being sent).
     * 
     * @return The maximum requests queues per destination.
     */
    public int getMaxRequestsQueuedPerDestination() {
        return maxRequestsQueuedPerDestination;
    }

    /**
     * The size in bytes of the buffer used to write requests. Defaults to 4096.
     * 
     * @return The request buffer size.
     */
    public int getRequestBufferSize() {
        return requestBufferSize;
    }

    /**
     * The size in bytes of the buffer used to read responses. Defaults to
     * 16384.
     * 
     * @return The response buffer size.
     */
    public int getResponseBufferSize() {
        return responseBufferSize;
    }

    /**
     * The scheduler. Defaults to null. When null, creates a new instance of
     * {@link ScheduledExecutorScheduler}.
     * 
     * @return The scheduler.
     */
    public Scheduler getScheduler() {
        return null;
    }

    /**
     * Stop timeout in milliseconds. Defaults to 60000.
     * <p>
     * The maximum time allowed for the service to shutdown.
     * 
     * @return The stop timeout.
     */
    public long getStopTimeout() {
        return stopTimeout;
    }

    /**
     * Returns the "User-Agent" HTTP header string; when null, uses the Jetty
     * default
     * 
     * @return The "User-Agent" HTTP header string; when null, uses the Jetty
     *         default
     */
    public String getUserAgentField() {
        return userAgentField;
    }

    /**
     * Returns the Indicates whether to dispatch I/O operations from the
     * selector thread to a different thread.
     * 
     * @return The Indicates whether to dispatch I/O operations from the
     *         selector thread to a different thread.
     */
    public boolean isDispatchingIo() {
        return dispatchingIo;
    }

    /**
     * Whether to dispatch I/O operations from the selector thread to a
     * different thread. Defaults to true.
     * <p>
     * This implementation never blocks on I/O operation, but invokes
     * application callbacks that may take time to execute or block on other
     * I/O. If application callbacks are known to take time or block on I/O,
     * then this should be set to true. If application callbacks are known to be
     * quick and never block on I/O, then this may be set to false.
     * 
     * @return Whether to dispatch I/O.
     * @deprecated use {@link #isDispatchingIo()} instead.
     */
    public boolean isDispatchIO() {
        return dispatchingIo;
    }

    /**
     * Returns the Indicates whether to follow HTTP redirects
     * 
     * @return The Indicates whether to follow HTTP redirects
     */
    public boolean isFollowingRedirects() {
        return followingRedirects;
    }

    /**
     * Whether to follow HTTP redirects. Defaults to true.
     * 
     * @return Whether to follow redirects.
     * @deprecated use {@link #isFollowingRedirects()} instead.
     */
    public boolean isFollowRedirects() {
        return followingRedirects;
    }

    /**
     * Whether request events must be strictly ordered. Defaults to false.
     * <p>
     * Client listeners may send a second request. If the second request is for
     * the same destination, there is an inherent race condition for the use of
     * the connection: the first request may still be associated with the
     * connection, so the second request cannot use that connection and is
     * forced to open another one.
     * <p>
     * From the point of view of connection usage, the connection is reusable
     * just before the "complete" event, so it would be possible to reuse that
     * connection from complete listeners; but in this case the second request's
     * events will fire before the "complete" events of the first request.
     * <p>
     * This setting enforces strict event ordering so that a "begin" event of a
     * second request can never fire before the "complete" event of a first
     * request, but at the expense of an increased usage of connections.
     * <p>
     * When not enforced, a "begin" event of a second request may happen before
     * the "complete" event of a first request and allow for better usage of
     * connections.
     * 
     * @return Whether request events must be strictly ordered.
     */
    public boolean isStrictEventOrdering() {
        return strictEventOrdering;
    }

    /**
     * Whether TCP_NODELAY is enabled. Defaults to true.
     * 
     * @return Whether TCP_NODELAY is enabled.
     */
    public boolean isTcpNoDelay() {
        return tcpNoDelay;
    }

    /**
     * Sets the timeout in milliseconds for the DNS resolution of host addresses
     * 
     * @param addressResolutionTimeout
     *            The timeout in milliseconds for the DNS resolution of host
     *            addresses
     */
    public void setAddressResolutionTimeout(long addressResolutionTimeout) {
        this.addressResolutionTimeout = addressResolutionTimeout;
    }

    /**
     * Sets the address to bind socket channels to. You must set <i>both</i>
     * this and bindPort.
     * 
     * @param bindAddress
     *            The address to bind socket channels to. You must set
     *            <i>both</i> this and bindPort.
     */
    public void setBindAddress(String bindAddress) {
        this.bindAddress = bindAddress;
    }

    /**
     * Sets the address to bind socket channels to. You must set <i>both</i>
     * this and bindAddress.
     * 
     * @param bindPort
     *            The address to bind socket channels to. You must set
     *            <i>both</i> this and bindAddress.
     */
    public void setBindPort(int bindPort) {
        this.bindPort = bindPort;
    }

    /**
     * Sets the max time in milliseconds a connection can take to connect to
     * destinations.
     * 
     * @param connectTimeout
     *            The max time in milliseconds a connection can take to connect
     *            to destinations.
     */
    public void setConnectTimeout(long connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    /**
     * Sets the Indicates whether to dispatch I/O operations from the selector
     * thread to a different thread.
     * 
     * @param dispatchIo
     *            The Indicates whether to dispatch I/O operations from the
     *            selector thread to a different thread.
     */
    public void setDispatchIo(boolean dispatchIo) {
        this.dispatchingIo = dispatchIo;
    }

    /**
     * Sets the Indicates whether to follow HTTP redirects
     * 
     * @param followRedirects
     *            The Indicates whether to follow HTTP redirects
     */
    public void setFollowRedirects(boolean followRedirects) {
        this.followingRedirects = followRedirects;
    }

    /**
     * Sets the max time in milliseconds a connection can be idle (that is,
     * without traffic of bytes in either direction).
     * 
     * @param idleTimeout
     *            The max time in milliseconds a connection can be idle (that
     *            is, without traffic of bytes in either direction).
     */
    public void setIdleTimeout(long idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    /**
     * Sets the max number of connections to open to each destination
     * 
     * @param maxConnectionsPerDestination
     *            The max number of connections to open to each destination
     */
    public void setMaxConnectionsPerDestination(int maxConnectionsPerDestination) {
        this.maxConnectionsPerDestination = maxConnectionsPerDestination;
    }

    /**
     * The max number of HTTP redirects that are followed
     * 
     * @param maxRedirects
     *            The max number of HTTP redirects that are followed
     */
    public void setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }

    /**
     * Sets the max number of requests that may be queued to a destination
     * 
     * @param maxRequestsQueuedPerDestination
     *            The max number of requests that may be queued to a destination
     */
    public void setMaxRequestsQueuedPerDestination(
            int maxRequestsQueuedPerDestination) {
        this.maxRequestsQueuedPerDestination = maxRequestsQueuedPerDestination;
    }

    /**
     * Sets the size in bytes of the buffer used to write requests
     * 
     * @param requestBufferSize
     *            The size in bytes of the buffer used to write requests
     */
    public void setRequestBufferSize(int requestBufferSize) {
        this.requestBufferSize = requestBufferSize;
    }

    /**
     * Sets the size in bytes of the buffer used to read responses
     * 
     * @param responseBufferSize
     *            The size in bytes of the buffer used to read responses
     */
    public void setResponseBufferSize(int responseBufferSize) {
        this.responseBufferSize = responseBufferSize;
    }

    /**
     * Sets the stop timeout in milliseconds; the maximum time allowed for the
     * service toshutdown
     * 
     * @param stopTimeout
     *            The stop timeout in milliseconds; the maximum time allowed for
     *            the service toshutdown
     */
    public void setStopTimeout(long stopTimeout) {
        this.stopTimeout = stopTimeout;
    }

    /**
     * Indicates whether request events must be strictly ordered
     * 
     * @param strictEventOrdering
     *            True if request events must be strictly ordered
     */
    public void setStrictEventOrdering(boolean strictEventOrdering) {
        this.strictEventOrdering = strictEventOrdering;
    }

    /**
     * Indicates whether TCP_NODELAY is enabled
     * 
     * @param tcpNoDelay
     *            True if TCP_NODELAY is enabled
     */
    public void setTcpNoDelay(boolean tcpNoDelay) {
        this.tcpNoDelay = tcpNoDelay;
    }

    /**
     * Sets the "User-Agent" HTTP header string; when null, uses the Jetty
     * default
     * 
     * @param userAgentField
     *            The "User-Agent" HTTP header string; when null, uses the Jetty
     *            default
     */
    public void setUserAgentField(String userAgentField) {
        this.userAgentField = userAgentField;
    }

    @Override
    public void start() throws Exception {
        super.start();

        if (this.httpClient == null)
            this.httpClient = createHttpClient();

        final HttpClient httpClient = getHttpClient();
        if (httpClient != null) {
            getLogger().info("Starting a Jetty HTTP/HTTPS client");
            httpClient.start();
        }
    }

    @Override
    public void stop() throws Exception {
        final HttpClient httpClient = getHttpClient();
        if (httpClient != null) {
            getLogger().info("Stopping a Jetty HTTP/HTTPS client");
            httpClient.stop();
        }

        super.stop();
    }
}
