/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.engine.connector;

import org.restlet.Client;
import org.restlet.Request;
import org.restlet.data.Protocol;
import org.restlet.engine.adapter.ClientCall;
import org.restlet.engine.util.ReferenceUtils;

import javax.net.ssl.HostnameVerifier;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.logging.Level;

/**
 * HTTP client connector using the {@link HttpUrlConnectionCall}. Here is the
 * list of parameters that are supported. They should be set in the Client's
 * context before it is started:
 * <table>
 * <caption>list of supported parameters</caption>
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
 * <td>followRedirects</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If true, the protocol will automatically follow redirects. If false, the
 * protocol will not automatically follow redirects.</td>
 * </tr>
 * <tr>
 * <td>readTimeout</td>
 * <td>int</td>
 * <td>60000</td>
 * <td>Sets the read timeout to a specified timeout, in milliseconds. A timeout
 * of zero is interpreted as an infinite timeout.</td>
 * </tr>
 * <tr>
 * <td>socketConnectTimeoutMs</td>
 * <td>int</td>
 * <td>15000</td>
 * <td>The socket connection timeout or 0 for unlimited wait.</td>
 * </tr>
 * <tr>
 * <td>sslContextFactory</td>
 * <td>String</td>
 * <td>org.restlet.engine.ssl.DefaultSslContextFactory</td>
 * <td>Let you specify a {@link org.restlet.engine.ssl.SslContextFactory}
 * qualified class name as a parameter, or an instance as an attribute for a
 * more complete and flexible SSL context setting.</td>
 * </tr>
 * <tr>
 * <td>useCaches</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>If true, the protocol is allowed to use caching whenever it can.</td>
 * </tr>
 * </table>
 * For the default SSL parameters see the Javadocs of the
 * {@link org.restlet.engine.ssl.DefaultSslContextFactory} class.
 * <p>
 * It is also possible to specify a hostname verifier for HTTPS connections. See
 * the {@link #getHostnameVerifier()} method for details.
 * <p>
 * Note that by default, the {@link HttpURLConnection} class as implemented by
 * Sun will retry a request if an IO exception is caught, for example, due to a
 * connection reset by the server. This can be annoying, especially because the
 * HTTP semantics of non-idempotent methods like POST can be broken, but also
 * because the new request won't include an entity. There is one way to disable
 * this behavior for POST requests only by setting the system property
 * "sun.net.http.retryPost" to "false".
 * 
 * @see <a href=
 *      "https://docs.oracle.com/javase/1.5.0/docs/guide/net/index.html">Networking
 *      Features</a>
 * @author Jerome Louvel
 * @deprecated Will be removed in the next 2.7/3.0 release.
 */
@Deprecated
public class HttpClientHelper extends org.restlet.engine.adapter.HttpClientHelper {

	/**
	 * Constructor.
	 * 
	 * @param client The client to help.
	 */
	public HttpClientHelper(Client client) {
		super(client);
		getProtocols().add(Protocol.HTTP);
		getProtocols().add(Protocol.HTTPS);
	}

	/**
	 * Creates a low-level HTTP client call from a high-level uniform call.
	 * 
	 * @param request The high-level request.
	 * @return A low-level HTTP client call.
	 */
	@Override
	public ClientCall create(Request request) {
		ClientCall result = null;

		try {
			result = new HttpUrlConnectionCall(this, request.getMethod().toString(),
					ReferenceUtils.update(request.getResourceRef(), request).toString(), request.isEntityAvailable());
		} catch (IOException ioe) {
			getLogger().log(Level.WARNING, "Unable to create the HTTP client call", ioe);
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
		return Integer.parseInt(getHelpedParameters().getFirstValue("chunkLength", "0"));
	}

	/**
	 * Returns the hostname verifier by looking up the "hostnameVerifier" attribute
	 * of the client's context.
	 * 
	 * @return The hostname verifier or null.
	 */
	public HostnameVerifier getHostnameVerifier() {
		return (HostnameVerifier) ((getContext() == null) ? null
				: getContext().getAttributes().get("hostnameVerifier"));
	}

	/**
	 * Returns the read timeout value. A timeout of zero is interpreted as an
	 * infinite timeout. Default to 60000.
	 * 
	 * @return The read timeout value.
	 */
	public int getReadTimeout() {
		return Integer.parseInt(getHelpedParameters().getFirstValue("readTimeout", "60000"));
	}

	/**
	 * Indicates if this URL is being examined in a context in which it makes sense
	 * to allow user interactions such as popping up an authentication dialog.
	 * 
	 * @return True if it makes sense to allow user interactions.
	 */
	public boolean isAllowUserInteraction() {
		return Boolean.parseBoolean(getHelpedParameters().getFirstValue("allowUserInteraction", "false"));
	}

	/**
	 * Indicates if the protocol automatically follows redirects.
	 * 
	 * @return True if the protocol automatically follows redirects.
	 */
	public boolean isFollowRedirects() {
		return Boolean.parseBoolean(getHelpedParameters().getFirstValue("followRedirects", "false"));
	}

	/**
	 * Indicates if the protocol is allowed to use caching whenever it can.
	 * 
	 * @return True if the protocol is allowed to use caching whenever it can.
	 */
	public boolean isUseCaches() {
		return Boolean.parseBoolean(getHelpedParameters().getFirstValue("useCaches", "false"));
	}

	@Override
	public synchronized void start() throws Exception {
		super.start();
		getLogger().info("Starting the internal HTTP client");
	}

	@Override
	public synchronized void stop() throws Exception {
		super.stop();
		getLogger().info("Stopping the internal HTTP client");
	}

}
