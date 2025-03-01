/**
 * Copyright 2005-2024 Qlik
 * <p>
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * <p>
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty;

import org.eclipse.jetty.client.AuthenticationStore;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.HttpClientTransport;
import org.eclipse.jetty.client.HttpProxy;
import org.eclipse.jetty.client.transport.HttpClientConnectionFactory;
import org.eclipse.jetty.client.transport.HttpClientTransportDynamic;
import org.eclipse.jetty.client.transport.HttpClientTransportOverHTTP;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.http.HttpCookieStore;
import org.eclipse.jetty.http.HttpField;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http2.client.HTTP2Client;
import org.eclipse.jetty.http2.client.transport.ClientConnectionFactoryOverHTTP2;
import org.eclipse.jetty.http2.client.transport.HttpClientTransportOverHTTP2;
import org.eclipse.jetty.http3.client.HTTP3Client;
import org.eclipse.jetty.http3.client.transport.ClientConnectionFactoryOverHTTP3;
import org.eclipse.jetty.http3.client.transport.HttpClientTransportOverHTTP3;
import org.eclipse.jetty.io.ClientConnectionFactory;
import org.eclipse.jetty.io.ClientConnector;
import org.eclipse.jetty.quic.client.ClientQuicConfiguration;
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
import org.restlet.ext.jetty.internal.RestletSslContextFactoryClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executor;
import java.util.logging.Level;

/**
 * HTTP client connector using the Jetty project. Here is the list of parameters
 * that are supported. They should be set in the Client's context before it is
 * started:
 * <table>
 * <caption>list of supported parameters</caption>
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
 * <td>cookieSupported</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Whether to support HTTP cookie, storing and automatically sending them
 * back</td>
 * </tr>
 * <tr>
 * <td>connectBlocking</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates whether the connect operation is blocking. See
 * {@link HttpClient#isConnectBlocking()}.</td>
 * </tr>
 * <tr>
 * <td>connectTimeout</td>
 * <td>long</td>
 * <td>15000</td>
 * <td>The max time in milliseconds a connection can take to connect to
 * destinations</td>
 * </tr>
 * <tr>
 * <td>followRedirects</td>
 * <td>boolean</td>
 * <td>true</td>
 * <td>Whether to follow HTTP redirects</td>
 * </tr>
 * <tr>
 * <td>httpComplianceMode</td>
 * <td>String</td>
 * <td>RFC7230</td>
 * <td>Indicate the HTTP compliance mode among the following options: "RFC7230",
 * "RFC2616", "LEGACY", "RFC7230_LEGACY". See {@link HttpCompliance}.</td>
 * </tr>
 * <tr>
 * <td>httpClientTransportMode</td>
 * <td>String</td>
 * <td>HTTP11</td>
 * <td>Indicate the HTTP client transport mode among the following options:
 * "HTTP11", "HTTP2", "HTTP3", "DYNAMIC. See {@link HttpClientTransport}.</td>
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
 * <td>maxResponseHeaderSize</td>
 * <td>int</td>
 * <td>-1</td>
 * <td>The max size in bytes of the response headers. -1 is unlimited.</td>
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
 * <td>System property "http.proxyPort" or "3128"</td>
 * <td>The port of the HTTP proxy.</td>
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
 * <td>strictEventOrdering</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Whether request events must be strictly ordered</td>
 * </tr>
 * <tr>
 * <td>userAgentField</td>
 * <td>String</td>
 * <td>null</td>
 * <td>The "User-Agent" HTTP header string; when null, uses the Jetty
 * default</td>
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
 * @author Jerome Louvel
 * @author Tal Liron
 */
public class HttpClientHelper
        extends org.restlet.engine.adapter.HttpClientHelper {

    /**
     * The wrapped Jetty HTTP client.
     */
    private volatile HttpClient httpClient;

    /**
     * The wrapped Jetty authentication store.
     */
    private volatile AuthenticationStore authenticationStore;

    /**
     * The wrapped Jetty cookie store.
     */
    private volatile HttpCookieStore cookieStore;

    /** The wrapper Executor. */
    private volatile Executor executor;

    /**
     * Constructor. Properties can still be set before the wrapped Jetty HTTP
     * client is effectively created and configured via the
     * {@link #createHttpClient()} method.
     *
     * @param client The client connector to help.
     */
    public HttpClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.HTTP);
        getProtocols().add(Protocol.HTTPS);
        this.authenticationStore = null;
        this.cookieStore = isCookieSupported()
                ? new HttpCookieStore.Default()
                : new HttpCookieStore.Empty();
        this.executor = null;
    }

    /**
     * Creates a low-level HTTP client call from a high-level uniform call.
     *
     * @param request The high-level request.
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
    protected HttpClient createHttpClient() {
        SslContextFactory.Client sslContextFactory = null;

        try {
            sslContextFactory = new RestletSslContextFactoryClient(
                    org.restlet.engine.ssl.SslUtils.getSslContextFactory(this));
        } catch (Exception e) {
            getLogger().log(Level.WARNING,
                    "Unable to create the Jetty SSL context factory", e);
        }

        HttpClientTransport httpTransport = null;

        final String httpClientTransportMode = getHttpClientTransportMode();
        switch (httpClientTransportMode) {
            case "HTTP2":
                httpTransport = getHttpClientTransportForHttp2();
                break;
            case "HTTP3":
                httpTransport = getHttpClientTransportForHttp3(sslContextFactory);
                break;
            case "DYNAMIC":
                httpTransport = getHttpClientTransportForDynamicMode(sslContextFactory);
                break;
            case "HTTP11":
                httpTransport = getHttpTransportForHttp1_1();
                break;
            default:
                getLogger().log(Level.WARNING,
                        "Unknown HTTP client transport mode: {0}, use HTTP11 instead", httpClientTransportMode);
                httpTransport = getHttpTransportForHttp1_1();
                break;
        }

        HttpClient httpClient = new HttpClient(httpTransport);
        httpClient.setAddressResolutionTimeout(getAddressResolutionTimeout());
        if (getAuthenticationStore() != null) {
            httpClient.setAuthenticationStore(getAuthenticationStore());
        }
        httpClient.setBindAddress(getBindAddress());
        httpClient.setConnectBlocking(isConnectBlocking());
        httpClient.setConnectTimeout(getConnectTimeout());
        httpClient.setDestinationIdleTimeout(getDestinationIdleTimeout());
        httpClient.setExecutor(getExecutor());
        httpClient.setFollowRedirects(isFollowRedirects());


        switch (getHttpComplianceMode()) {
            case "RFC7230":
                httpClient.setHttpCompliance(HttpCompliance.RFC7230);
                break;
            case "RFC7230_LEGACY":
                httpClient.setHttpCompliance(HttpCompliance.RFC7230_LEGACY);
                break;
            case "RFC2616":
                httpClient.setHttpCompliance(HttpCompliance.RFC2616);
                break;
            case "RFC2616_LEGACY":
                httpClient.setHttpCompliance(HttpCompliance.RFC2616_LEGACY);
                break;
        }

        httpClient.setHttpCookieStore(getCookieStore());
        httpClient.setIdleTimeout(getIdleTimeout());
        httpClient.setMaxConnectionsPerDestination(getMaxConnectionsPerDestination());
        httpClient.setMaxRedirects(getMaxRedirects());
        httpClient.setMaxRequestsQueuedPerDestination(getMaxRequestsQueuedPerDestination());
        httpClient.setMaxResponseHeadersSize(getMaxResponseHeadersSize());

        String httpProxyHost = getProxyHost();
        if (httpProxyHost != null) {
            HttpProxy proxy = new HttpProxy(httpProxyHost, getProxyPort());
            httpClient.getProxyConfiguration().addProxy(proxy);
        }

        httpClient.setRequestBufferSize(getRequestBufferSize());
        httpClient.setResponseBufferSize(getResponseBufferSize());
        httpClient.setScheduler(getScheduler());
        httpClient.setSslContextFactory(sslContextFactory);
        httpClient.setStrictEventOrdering(isStrictEventOrdering());

        String userAgentField = getUserAgentField();
        if (userAgentField != null) {
            httpClient.setUserAgentField(
                    new HttpField(HttpHeader.USER_AGENT, userAgentField));
        }

        return httpClient;
    }

    private static HttpClientTransportOverHTTP getHttpTransportForHttp1_1() {
        return new HttpClientTransportOverHTTP();
    }

    private static HttpClientTransport getHttpClientTransportForHttp2() {
        HTTP2Client http2Client = new HTTP2Client();
        HttpClientTransportOverHTTP2 http2Transport = new HttpClientTransportOverHTTP2(http2Client);
        http2Transport.setUseALPN(true);

        return http2Transport;
    }

    private static HttpClientTransport getHttpClientTransportForHttp3(SslContextFactory.Client sslContextFactory) {
        ClientQuicConfiguration quicConfiguration = new ClientQuicConfiguration(sslContextFactory, null);
        HTTP3Client http3Client = new HTTP3Client(quicConfiguration);
        http3Client.getQuicConfiguration().setSessionRecvWindow(64 * 1024 * 1024);

        return new HttpClientTransportOverHTTP3(http3Client);
    }

    private static HttpClientTransport getHttpClientTransportForDynamicMode(SslContextFactory.Client sslContextFactory) {

        ClientConnectionFactory.Info http1 = HttpClientConnectionFactory.HTTP11;

        HTTP2Client http2Client = new HTTP2Client();
        ClientConnectionFactoryOverHTTP2.HTTP2 http2 = new ClientConnectionFactoryOverHTTP2.HTTP2(http2Client);

        ClientQuicConfiguration quicConfiguration = new ClientQuicConfiguration(sslContextFactory, null);
        HTTP3Client http3Client = new HTTP3Client(quicConfiguration);
        ClientConnectionFactoryOverHTTP3.HTTP3 http3 = new ClientConnectionFactoryOverHTTP3.HTTP3(http3Client);

        return new HttpClientTransportDynamic(new ClientConnector(), http1, http2, http3);
    }

    /**
     * The timeout in milliseconds for the DNS resolution of host addresses.
     * Defaults to 15000.
     *
     * @return The address resolution timeout.
     */
    public long getAddressResolutionTimeout() {
        return Long.parseLong(getHelpedParameters()
                .getFirstValue("addressResolutionTimeout", "15000"));
    }

    /**
     * Returns the wrapped Jetty authentication store.
     *
     * @return The wrapped Jetty authentication store.
     */
    public AuthenticationStore getAuthenticationStore() {
        return authenticationStore;
    }

    /**
     * Sets the wrapped Jetty authentication store.
     *
     * @param authenticationStore The wrapped Jetty authentication store.
     */
    public void setAuthenticationStore(
            AuthenticationStore authenticationStore) {
        this.authenticationStore = authenticationStore;
    }

    /**
     * The address to bind socket channels to. Default to null.
     *
     * @return The bind address or null.
     */
    public SocketAddress getBindAddress() {
        final String bindAddress = getHelpedParameters()
                .getFirstValue("bindAddress", null);
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
        return Long.parseLong(
                getHelpedParameters().getFirstValue("connectTimeout", "15000"));
    }

    /**
     * Returns the wrapped Jetty cookie store.
     *
     * @return The wrapped Jetty cookie store.
     */
    public HttpCookieStore getCookieStore() {
        return this.cookieStore;
    }

    /**
     * Sets the wrapped Jetty cookie store.
     *
     * @param cookieStore The wrapped Jetty cookie store.
     */
    public void setCookieStore(HttpCookieStore cookieStore) {
        this.cookieStore = cookieStore;
    }

    /**
     * The timeout in milliseconds for idle destinations to be removed. Defaults
     * to 15000.
     *
     * @return The address resolution timeout.
     */
    public long getDestinationIdleTimeout() {
        return Long.parseLong(getHelpedParameters()
                .getFirstValue("destinationIdleTimeout", "15000"));
    }

    /**
     * Returns the executor. By default, returns an instance of
     * {@link QueuedThreadPool}.
     *
     * @return Returns the executor.
     */
    public Executor getExecutor() {
        return this.executor;
    }

    /**
     * Sets the executor.
     *
     * @param executor The executor.
     */
    public void setExecutor(Executor executor) {
        this.executor = executor;
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
     * Returns the HTTP compliance mode among the following options: "RFC7230",
     * "RFC2616", "LEGACY", "RFC7230_LEGACY". See {@link HttpCompliance}.
     * Default to "RFC7230".
     *
     * @return The HTTP compliance mode.
     */
    public String getHttpComplianceMode() {
        return getHelpedParameters().getFirstValue("httpComplianceMode",
                "RFC7230");
    }

    /**
     * Returns the HTTP client transport mode among the following options:
     * "HTTP11", "HTTP2", "HTTP3", "DYNAMIC. See {@link HttpClientTransport}.
     * Defaults to "HTTP11".
     *
     * @return The HTTP client transport mode.
     */
    public String getHttpClientTransportMode() {
        return getHelpedParameters().getFirstValue("httpClientTransportMode",
                "HTTP11");
    }

    /**
     * The max time in milliseconds a connection can be idle (that is, without
     * traffic of bytes in either direction). Defaults to 60000.
     *
     * @return The idle timeout.
     */
    public long getIdleTimeout() {
        return Long.parseLong(
                getHelpedParameters().getFirstValue("idleTimeout", "60000"));
    }

    /**
     * Sets the max number of connections to open to each destination. Defaults
     * to 10.
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
        return Integer.parseInt(getHelpedParameters()
                .getFirstValue("maxConnectionsPerDestination", "10"));
    }

    /**
     * The max number of HTTP redirects that are followed. Defaults to 8.
     *
     * @return The maximum redirects.
     */
    public int getMaxRedirects() {
        return Integer.parseInt(
                getHelpedParameters().getFirstValue("maxRedirects", "8"));
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
        return Integer.parseInt(getHelpedParameters()
                .getFirstValue("maxRequestsQueuedPerDestination", "1024"));
    }

    /**
     * Returns the max size in bytes of the response headers. Default is -1
     * that is unlimited.
     *
     * @return the max size in bytes of the response headers.
     */
    public int getMaxResponseHeadersSize() {
        return Integer.parseInt(getHelpedParameters()
                .getFirstValue("maxResponseHeadersSize", "-1"));
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
        return Integer.parseInt(getHelpedParameters().getFirstValue("proxyPort",
                System.getProperty("http.proxyPort", "3128")));
    }

    /**
     * The size in bytes of the buffer used to write requests. Defaults to 4096.
     *
     * @return The request buffer size.
     */
    public int getRequestBufferSize() {
        return Integer.parseInt(getHelpedParameters()
                .getFirstValue("requestBufferSize", "4096"));
    }

    /**
     * The size in bytes of the buffer used to read responses. Defaults to
     * 16384.
     *
     * @return The response buffer size.
     */
    public int getResponseBufferSize() {
        return Integer.parseInt(getHelpedParameters()
                .getFirstValue("responseBufferSize", "16384"));
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
     * The "User-Agent" HTTP header string. When null, uses the Jetty default.
     * Default to null.
     *
     * @return The user agent field or null.
     */
    public String getUserAgentField() {
        return getHelpedParameters().getFirstValue("userAgentField", null);
    }

    /**
     * Indicates whether the connect operation is blocking. See
     * {@link HttpClient#isConnectBlocking()}.
     *
     * @return True if the connect operation is blocking.
     */
    public boolean isConnectBlocking() {
        return Boolean.parseBoolean(getHelpedParameters()
                .getFirstValue("connectBlocking", "false"));
    }

    /**
     * Whether to support cookies, storing and automatically sending them back.
     * Defaults to false.
     *
     * @return Whether to support cookies.
     */
    public boolean isCookieSupported() {
        return Boolean.parseBoolean(getHelpedParameters()
                .getFirstValue("cookieSupported", "false"));
    }

    /**
     * Whether to follow HTTP redirects. Defaults to true.
     *
     * @return Whether to follow redirects.
     */
    public boolean isFollowRedirects() {
        return Boolean.parseBoolean(
                getHelpedParameters().getFirstValue("followRedirects", "true"));
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
        return Boolean.parseBoolean(getHelpedParameters()
                .getFirstValue("strictEventOrdering", "false"));
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
