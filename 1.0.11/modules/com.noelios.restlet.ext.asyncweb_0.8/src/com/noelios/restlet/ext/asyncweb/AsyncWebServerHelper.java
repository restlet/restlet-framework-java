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
package com.noelios.restlet.ext.asyncweb;

import java.util.logging.Level;

import org.restlet.Server;
import org.safehaus.asyncweb.container.ContainerLifecycleException;
import org.safehaus.asyncweb.container.ServiceContainer;
import org.safehaus.asyncweb.container.ServiceHandler;
import org.safehaus.asyncweb.http.HttpResponse;
import org.safehaus.asyncweb.request.AsyncWebRequest;
import org.safehaus.asyncweb.transport.Transport;
import org.safehaus.asyncweb.transport.TransportException;

import com.noelios.restlet.http.HttpServerCall;
import com.noelios.restlet.http.HttpServerHelper;

/**
 * Abstract AsyncWeb server connector. Here is the list of parameters that are
 * supported: <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>ioWorkerCount</td>
 * <td>int</td>
 * <td>2</td>
 * <td>Number of worker threads to employ.</td>
 * </tr>
 * <tr>
 * <td>converter</td>
 * <td>String</td>
 * <td>com.noelios.restlet.http.HttpServerConverter</td>
 * <td>Class name of the converter of low-level HTTP calls into high level
 * requests and responses.</td>
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
 * </table> <br/> This implementation passes by all of AsyncWeb
 * ServiceContainer, HttpServiceHandler etc. mechanisms and implements a
 * {@link com.noelios.restlet.http.HttpServerHelper} and a
 * {@link org.safehaus.asyncweb.container.ServiceContainer} directly. It takes
 * care about setting up a
 * {@link org.safehaus.asyncweb.transport.nio.NIOTransport}.
 * <p>
 * Note: This implementation is not usable inside an AsyncWeb standard
 * environment because it represents a container and not a handler; it takes
 * full control over the container lifecycle.
 * </p>
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a
 *         href="http://www.semagia.com/">Semagia</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public abstract class AsyncWebServerHelper extends HttpServerHelper implements
		ServiceContainer {
	/**
	 * Indicates if the server is acting in HTTPS mode.
	 */
	private boolean confidential;

	/**
	 * The AsyncWeb transport layer.
	 */
	private Transport transport;

	/**
	 * Constructor.
	 * 
	 * @param server
	 *            The server to help.
	 * @param confidential
	 *            Indicates if the server is acting in HTTPS mode.
	 */
	public AsyncWebServerHelper(Server server, boolean confidential) {
		super(server);
		this.confidential = confidential;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.safehaus.asyncweb.container.ServiceContainer#addServiceHandler(org.safehaus.asyncweb.container.ServiceHandler)
	 */
	public void addServiceHandler(ServiceHandler serviceHandler) {
		throw new UnsupportedOperationException(
				"This container accepts no service handlers");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.safehaus.asyncweb.container.ServiceContainer#addTransport(org.safehaus.asyncweb.transport.Transport)
	 */
	public void addTransport(Transport transport) {
		throw new UnsupportedOperationException(
				"This container is bound to a transport");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.safehaus.asyncweb.container.ServiceContainer#dispatchRequest(org.safehaus.asyncweb.request.AsyncWebRequest)
	 */
	public void dispatchRequest(AsyncWebRequest request) {
		HttpResponse response = request.createHttpResponse();
		HttpServerCall call = new AsyncWebServerCall(getServer(), request,
				response, confidential);
		handle(call);
		request.commitResponse(response);
	}

	/** Starts the Connector. */
	@Override
	public void start() throws ContainerLifecycleException {
		try {
			getTransport().start();
		} catch (TransportException ex) {
			getLogger().log(Level.WARNING, "Failed to start the transport", ex);
			throw new ContainerLifecycleException(
					"Failed to start the transport", ex);
		} catch (Exception e) {
			getLogger().log(Level.WARNING,
					"Failed to start the AsyncWeb HTTP Server", e);
			throw new ContainerLifecycleException(
					"Failed to start the AsyncWeb HTTP Server", e);
		}
	}

	/** Stops the Connector. */
	@Override
	public void stop() {
		try {
			getTransport().stop();
		} catch (TransportException ex) {
			getLogger().log(Level.WARNING, "Failed to stop transport", ex);
		} catch (Exception e) {
			getLogger().log(Level.WARNING,
					"Failed to start the AsyncWeb HTTP Server", e);
		}
	}

	/**
	 * Returns the number of worker threads to employ.
	 * 
	 * @return The number of worker threads to employ.
	 */
	public int getIoWorkerCount() {
		return Integer.parseInt(getParameters().getFirstValue("ioWorkerCount",
				"2"));
	}

	/**
	 * Sets the AsyncWeb transport layer.
	 * 
	 * @param transport
	 *            The AsyncWeb transport layer.
	 */
	protected void setTransport(Transport transport) {
		this.transport = transport;
	}

	/**
	 * Returns the AsyncWeb transport layer.
	 * 
	 * @return The AsyncWeb transport layer.
	 */
	protected Transport getTransport() {
		return this.transport;
	}

}
