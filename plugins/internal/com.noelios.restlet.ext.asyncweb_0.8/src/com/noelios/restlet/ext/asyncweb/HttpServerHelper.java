/*
 * Copyright 2005-2006 Noelios Consulting.
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * http://www.opensource.org/licenses/cddl1.txt
 * If applicable, add the following below this CDDL
 * HEADER, with the fields enclosed by brackets "[]"
 * replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
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
 * {@link com.noelios.restlet.impl.http.ServerImpl} and a 
 * {@link org.safehaus.asyncweb.container.ServiceContainer} directly. It takes
 * care about setting up a {@link org.safehaus.asyncweb.transport.nio.NIOTransport}.
 * <p>
 * Note: This implementation is not usable inside an AsyncWeb standard 
 * environment because it represents a container and not a handler; it takes
 * full control over the container lifecycle.
 * </p>
 * 
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://www.semagia.com/">Semagia</a>
 */
public class HttpServerHelper extends AsyncWebServerHelper
{
   /**
    * Constructor.
	 * @param server The server to help.
    */
   public HttpServerHelper(Server server)
   {
      super(server);
      getSupportedProtocols().add(Protocol.HTTP);
   }

   /** Starts the Connector. */
	public void start() throws ContainerLifecycleException
	{
		NIOTransport nio = new NIOTransport();
		nio.setPort(getServer().getPort());
		nio.setServiceContainer(this);
		nio.setIoWorkerCount(getIoWorkerCount());
		setTransport(nio);
		super.start();		
	}

}
