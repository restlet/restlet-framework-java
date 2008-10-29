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
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.net;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;

import javax.net.ssl.HostnameVerifier;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Request;

import com.noelios.restlet.http.HttpClientCall;

/**
 * HTTP client connector using the HttpUrlConnectionCall. Here is the list of
 * parameters that are supported:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>allowUserInteraction</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If true, this URL is being examined in a context in which it makes sense
 * to allow user interactions such as popping up an authentication dialog.</td>
 * </tr>
 * <tr>
 * <td>chunkLength</td>
 * <td>int</td>
 * <td>0 (uses HttpURLConnection's default)</td>
 * <td>The chunk-length when using chunked encoding streaming mode for response
 * entities. A value of -1 means chunked encoding is disabled for response
 * entities.</td>
 * </tr>
 * <tr>
 * <td>connectTimeout</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Sets a specified timeout value, in milliseconds, to be used when opening
 * a communications link to the resource referenced. 0 means infinite timeout.
 * This parameter is deprecated and should be replaced by the connectTimeout
 * attribute defined by the client.</td>
 * </tr>
 * <tr>
 * <td>followRedirects</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If true, the protocol will automatically follow redirects. If false, the
 * protocol will not automatically follow redirects.</td>
 * </tr>
 * <tr>
 * <td>readTimeout</td>
 * <td>int</td>
 * <td>0</td>
 * <td>Sets the read timeout to a specified timeout, in milliseconds. A timeout
 * of zero is interpreted as an infinite timeout.</td>
 * </tr>
 * <tr>
 * <td>useCaches</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If true, the protocol is allowed to use caching whenever it can.</td>
 * </tr>
 * </table>
 * 
 * It is also possible to specify a hostname verifier for HTTPS connections. See
 * the {@link #getHostnameVerifier()} method for details.
 * 
 * Note that by default, the {@link HttpURLConnection} class as implemented by
 * Sun will retry a request if an IO exception is caught, for example due to a
 * connection reset by the server. This can be annoying, especially because the
 * HTTP semantics of non idempotent methods like POST can be broken, but also
 * because the new request won't include an entity. There is one way to disable
 * this behavior for POST requests only by setting the system property
 * "sun.net.http.retryPost" to "false".
 * 
 * @see Client#getConnectTimeout()
 * @see <a
 *      href="http://java.sun.com/j2se/1.5.0/docs/guide/net/index.html">Networking
 *      Features</a>
 * @author Jerome Louvel
 */
public class HttpClientHelper extends com.noelios.restlet.http.HttpClientHelper {
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
    @Override
    public HttpClientCall create(Request request) {
        HttpClientCall result = null;

        try {
            result = new HttpUrlConnectionCall(this, request.getMethod()
                    .toString(), request.getResourceRef().toString(), request
                    .isEntityAvailable());
        } catch (IOException ioe) {
            getLogger().log(Level.WARNING,
                    "Unable to create the HTTP client call", ioe);
        }

        return result;
    }

    /**
     * Returns the chunk-length when using chunked encoding streaming mode for
     * response entities. A value of -1 means chunked encoding is disabled for
     * response entities.
     * 
     * @return The chunk-length when using chunked encoding streaming mode for
     *         response entities.
     */
    public int getChunkLength() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "chunkLength", "0"));
    }

    /**
     * Returns the timeout value, in milliseconds, to be used when opening a
     * communications link to the resource referenced. 0 means infinite timeout.
     * 
     * @return The connection timeout value.
     */
    @Override
    public int getConnectTimeout() {
        if (super.getConnectTimeout() > 0) {
            return super.getConnectTimeout();
        } else {
            return Integer.parseInt(getHelpedParameters().getFirstValue(
                    "connectTimeout", "0"));
        }
    }

    /**
     * Returns the hostname verifier by looking up the "hostnameVerifier"
     * attribute of the client's context.
     * 
     * @return The hostname verifier or null.
     */
    public HostnameVerifier getHostnameVerifier() {
        return (HostnameVerifier) ((getContext() == null) ? null : getContext()
                .getAttributes().get("hostnameVerifier"));
    }

    /**
     * Returns the read timeout value. A timeout of zero is interpreted as an
     * infinite timeout.
     * 
     * @return The read timeout value.
     */
    public int getReadTimeout() {
        return Integer.parseInt(getHelpedParameters().getFirstValue(
                "readTimeout", "0"));
    }

    /**
     * Indicates if this URL is being examined in a context in which it makes
     * sense to allow user interactions such as popping up an authentication
     * dialog.
     * 
     * @return True if it makes sense to allow user interactions.
     */
    public boolean isAllowUserInteraction() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "allowUserInteraction", "false"));
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

    /**
     * Indicates if the protocol is allowed to use caching whenever it can.
     * 
     * @return True if the protocol is allowed to use caching whenever it can.
     */
    public boolean isUseCaches() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "useCaches", "false"));
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        getLogger().info("Starting the HTTP client");
    }

    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        getLogger().info("Stopping the HTTP client");
    }

}
