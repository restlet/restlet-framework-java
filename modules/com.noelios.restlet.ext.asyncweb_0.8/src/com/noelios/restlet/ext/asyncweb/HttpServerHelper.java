/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.asyncweb;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.safehaus.asyncweb.container.ContainerLifecycleException;
import org.safehaus.asyncweb.transport.nio.NIOTransport;

/**
 * AsyncWeb HTTP server connector.
 * 
 * This implementation passes by all of AsyncWeb ServiceContainer,
 * HttpServiceHandler etc. mechanisms and implements a
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
 */
public class HttpServerHelper extends AsyncWebServerHelper {
	/**
	 * Constructor.
	 * 
	 * @param server
	 *            The server to help.
	 */
	public HttpServerHelper(Server server) {
		super(server, false);
		getProtocols().add(Protocol.HTTP);
	}

	/** Starts the Connector. */
	@Override
	public void start() throws ContainerLifecycleException {
		NIOTransport nio = new NIOTransport();
		nio.setPort(getServer().getPort());
		nio.setServiceContainer(this);
		nio.setIoWorkerCount(getIoWorkerCount());
		setTransport(nio);
		super.start();
	}

}
