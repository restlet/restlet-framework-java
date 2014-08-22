/**
 * Copyright 2005-2014 Restlet
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
 * <td>0</td>
 * <td>The max time in milliseconds a connection can be idle (that is, without
 * traffic of bytes in either direction)</td>
 * </tr>
 * <tr>
 * <td>maxConnectionsPerDestination</td>
 * <td>int</td>
 * <td>64</td>
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
 * <td>30000</td>
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
     * Returns the wrapped Jetty HTTP client.
     * 
     * @return The wrapped Jetty HTTP client.
     */
    public HttpClient getHttpClient() {
        return this.httpClient;
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

    /**
     * The timeout in milliseconds for the DNS resolution of host addresses.
     * Defaults to 15000.
     * 
     * @return The address resolution timeout.
     */
    public long getAddressResolutionTimeout() {
        return Long.parseLong(getHelpedParameters().getFirstValue(
                "addressResolutionTimeout", "15000"));
    }

    /**
     * The address to bind socket channels to. Default to null.
     * 
     * @return The bind address or null.
     */
    public SocketAddress getBindAddress() {
        final String bindAddress = getHelpedParameters().getFirstValue(
                "bindAddress", null);
        final String bindPort = getHelpedParameters().getFirstValue("bindPort",
                null);
        if ((bindAddress != null) && (bindPort != null))
            return new InetSocketAddress(bindAddress,
                    Integer.parseInt(bindPort));
        return null;
    }

    /**
     * The max time in milliseconds a connection can take to connect to
     * destinations. Defaults to 15000.
     * 
     * @return The connect timeout.
     */
    public long getConnectTimeout() {
        return Long.parseLong(getHelpedParameters().getFirstValue(
                "connectTimeout", "15000"));
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
     */
    public boolean isDispatchIO() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "dispatchIo", "true"));
    }

    /**
     * Whether to follow HTTP redirects. Defaults to true.
     * 
     * @return Whether to follow redirects.
     */
    public boolean isFollowRedirects() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "followRedirects", "true"));
    }

    /**
     * The max time in milliseconds a connection can be idle (that is, without
     * traffic of bytes in either direction). Defaults to 0.
     * 
     * @return The idle timeout.
     */
    public long getIdleTimeout() {
        return Long.parseLong(getHelpedParameters().getFirstValue(
                "idleTimeout", "0"));
    }

    /**
     * Sets the max number of connections to open to each destination. Defaults
     * to 64.
     * <p>
     * RFC 2616 suggests that 2 connections should be opened per each
     * destination, but browsers commonly open 6. If this client is used for
     * load testing, it is common to have only one destination (the server to
     * load test), and it is recommended to set this value to a high value (at
     * least as much as the threads present in the {@link #getExecutor()
     * executor}).
     * 
     * @return The maximum connections per destination.
     */
    public int getMaxConnectionsPerDestination() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxConnectionsPerDestination", "64"));
    }

    /**
     * The max number of HTTP redirects that are followed. Defaults to 8.
     * 
     * @return The maximum redirects.
     */
    public int getMaxRedirects() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxRedirects", "8"));
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
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxRequestsQueuedPerDestination", "1024"));
    }

    /**
     * The size in bytes of the buffer used to write requests. Defaults to 4096.
     * 
     * @return The request buffer size.
     */
    public int getRequestBufferSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "requestBufferSize", "4096"));
    }

    /**
     * The size in bytes of the buffer used to read responses. Defaults to
     * 16384.
     * 
     * @return The response buffer size.
     */
    public int getResponseBufferSize() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "responseBufferSize", "16384"));
    }

    /**
     * Stop timeout in milliseconds. Defaults to 30000.
     * <p>
     * The maximum time allowed for the service to shutdown.
     * 
     * @return The stop timeout.
     */
    public long getStopTimeout() {
        return Long.parseLong(getHelpedParameters().getFirstValue(
                "stopTimeout", "30000"));
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
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "strictEventOrdering", "false"));
    }

    /**
     * Whether TCP_NODELAY is enabled. Defaults to true.
     * 
     * @return Whether TCP_NODELAY is enabled.
     */
    public boolean isTcpNoDelay() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "tcpNoDelay", "true"));
    }

    /**
     * The "User-Agent" HTTP header string. When null, uses the Jetty default.
     * Defaults to null.
     * 
     * @return The user agent field or null.
     */
    public String getUserAgentField() {
        return getHelpedParameters().getFirstValue("userAgentField", null);
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
     * The scheduler. Defaults to null. When null, creates a new instance of
     * {@link ScheduledExecutorScheduler}.
     * 
     * @return The scheduler.
     */
    public Scheduler getScheduler() {
        return null;
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
     * The wrapped Jetty HTTP client.
     */
    private volatile HttpClient httpClient;
}
