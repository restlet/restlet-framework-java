/**
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.httpclient;

import java.io.IOException;
import java.util.logging.Level;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Request;

import com.noelios.restlet.http.HttpClientCall;

/**
 * HTTP client connector using the HttpMethodCall and Apache HTTP Client
 * project. Note that the response must be fully read in all cases in order to
 * surely release the underlying connection. Not doing so may cause future
 * requests to block.
 * 
 * Here is the list of parameters that are supported:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>followRedirects</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If true, the protocol will automatically follow redirects. If false, the
 * protocol will not automatically follow redirects.</td>
 * </tr>
 * <tr>
 * <td>maxConnectionsPerHost</td>
 * <td>int</td>
 * <td>2 (uses HttpClient's default)</td>
 * <td>The maximum number of connections that will be created for any particular
 * host.</td>
 * </tr>
 * <tr>
 * <td>maxTotalConnections</td>
 * <td>int</td>
 * <td>20 (uses HttpClient's default)</td>
 * <td>The maximum number of active connections.</td>
 * </tr>
 * <tr>
 * <td>connectionManagerTimeout</td>
 * <td>int</td>
 * <td>0</td>
 * <td>The timeout in milliseconds used when retrieving an HTTP connection from
 * the HTTP connection manager.</td>
 * </tr>
 * <tr>
 * <td>stopIdleTimeout</td>
 * <td>int</td>
 * <td>1000</td>
 * <td>The minimum idle time, in milliseconds, for connections to be closed when
 * stopping the connector.</td>
 * </tr>
 * <tr>
 * <td>readTimeout</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Sets the read timeout to a specified timeout, in milliseconds. A timeout
 * of zero is interpreted as an infinite timeout.</td>
 * </tr>
 * <tr>
 * <td>retryHandler</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Class name of the retry handler to use instead of HTTP Client default
 * behavior. The given class name must implement the
 * org.apache.commons.httpclient.HttpMethodRetryHandler interface and have a
 * default constructor</td>
 * </tr>
 * </table>
 * 
 * @see <a href=
 *      "http://jakarta.apache.org/httpcomponents/httpclient-3.x/tutorial.html"
 *      >Apache HTTP Client tutorial< /a>
 * @see <a
 *      href="http://java.sun.com/j2se/1.5.0/docs/guide/net/index.html">Networking
 *      Features< /a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpClientHelper extends com.noelios.restlet.http.HttpClientHelper {
    private volatile HttpClient httpClient;

    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public HttpClientHelper(Client client) {
        super(client);
        this.httpClient = null;
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
    @Override
    public HttpClientCall create(Request request) {
        HttpClientCall result = null;

        try {
            result = new HttpMethodCall(this, request.getMethod().toString(),
                    request.getResourceRef().toString(), request
                            .isEntityAvailable());
        } catch (final IOException ioe) {
            getLogger().log(Level.WARNING,
                    "Unable to create the HTTP client call", ioe);
        }

        return result;
    }

    /**
     * Returns the timeout in milliseconds used when retrieving an HTTP
     * connection from the HTTP connection manager.
     * 
     * @return The timeout in milliseconds used when retrieving an HTTP
     *         connection from the HTTP connection manager.
     */
    public int getConnectionManagerTimeout() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "connectionManagerTimeout", "0"));
    }

    public HttpClient getHttpClient() {
        return this.httpClient;
    }

    /**
     * Returns the maximum number of connections that will be created for any
     * particular host.
     * 
     * @return The maximum number of connections that will be created for any
     *         particular host.
     */
    public int getMaxConnectionsPerHost() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxConnectionsPerHost", "2"));
    }

    /**
     * Returns the maximum number of active connections.
     * 
     * @return The maximum number of active connections.
     */
    public int getMaxTotalConnections() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "maxTotalConnections", "20"));
    }

    /**
     * Returns the read timeout value. A timeout of zero is interpreted as an
     * infinite timeout.
     * 
     * @return The read timeout value.
     */
    public int getReadTimeout() {
        return Integer.parseInt(getHelpedParameters().getFirstValue("readTimeout",
                "0"));
    }

    /**
     * Returns the class name of the retry handler to use instead of HTTP Client
     * default behavior. The given class name must implement the
     * org.apache.commons.httpclient.HttpMethodRetryHandler interface and have a
     * default constructor.
     * 
     * @return The class name of the retry handler.
     */
    public String getRetryHandler() {
        return getHelpedParameters().getFirstValue("retryHandler", null);
    }

    /**
     * Returns the minimum idle time, in milliseconds, for connections to be
     * closed when stopping the connector.
     * 
     * @return The minimum idle time, in milliseconds, for connections to be
     *         closed when stopping the connector.
     */
    public int getStopIdleTimeout() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "stopIdleTimeout", "1000"));
    }

    /**
     * Indicates if the protocol will automatically follow redirects.
     * 
     * @return True if the protocol will automatically follow redirects.
     */
    public boolean isFollowRedirects() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "followRedirects", "false"));
    }

    @Override
    public void start() throws Exception {
        super.start();

        // Create the multi-threaded connection manager and configure it
        final MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        connectionManager.getParams().setDefaultMaxConnectionsPerHost(
                getMaxConnectionsPerHost());
        connectionManager.getParams().setMaxTotalConnections(
                getMaxTotalConnections());

        // Create the internal client connector
        this.httpClient = new HttpClient(connectionManager);
        getHttpClient().getParams().setAuthenticationPreemptive(false);
        getHttpClient().getParams().setConnectionManagerTimeout(
                getConnectionManagerTimeout());
        getHttpClient().getParams()
                .setCookiePolicy(CookiePolicy.IGNORE_COOKIES);
        getHttpClient().getParams().setSoTimeout(getReadTimeout());

        getLogger().info("Starting the HTTP client");
    }

    @Override
    public void stop() throws Exception {
        getHttpClient().getHttpConnectionManager().closeIdleConnections(
                getStopIdleTimeout());
        getLogger().info("Stopping the HTTP client");
    }

}
