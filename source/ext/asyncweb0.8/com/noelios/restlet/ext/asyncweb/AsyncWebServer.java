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

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.connector.AbstractServer;
import org.restlet.connector.Server;
import org.restlet.connector.ServerCall;
import org.restlet.data.Protocol;
import org.restlet.data.Protocols;

import org.safehaus.asyncweb.container.ContainerLifecycleException;
import org.safehaus.asyncweb.container.ServiceContainer;
import org.safehaus.asyncweb.container.ServiceHandler;
import org.safehaus.asyncweb.http.HttpResponse;
import org.safehaus.asyncweb.request.AsyncWebRequest;
import org.safehaus.asyncweb.transport.Transport;
import org.safehaus.asyncweb.transport.TransportException;
import org.safehaus.asyncweb.transport.nio.NIOTransport;

/**
 * Represents a thin layer around AsyncWeb.
 * 
 * This implementation passes by all of AsyncWeb ServiceContainer, 
 * HttpServiceHandler etc. mechanisms and implements a 
 * {@link org.restlet.connector.Server} and a 
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
public class AsyncWebServer extends AbstractServer implements ServiceContainer
{
	/**
	 * Indicates if the server is acting in HTTPS mode.
	 */
	protected boolean confidential;

	/**
	 * AyncWeb transport layer.
	 */
	protected NIOTransport transport;

	/**
	 * Logger.
	 */
	private static Logger logger = Logger.getLogger("com.noelios.restlet.ext.asyncweb.AsyncWebServer");

	/**
	 * Constructor.
	 * 
	 * @param protocol The connector protocol.
	 * @param delegate The delegate Server.
	 * @param address The optional listening IP address (local host used if null).
	 * @param port The listening port.
	 */
	public AsyncWebServer(Protocol protocol, Server delegate, String address, int port)
	{
		super(protocol, delegate, address, port);
	}

	/**
	 * Returns the supported protocols. 
	 * 
	 * This method is called by the {@link com.noelios.restlet.impl.FactoryImpl} 
	 * to determine the supported protocols.
	 * 
	 * @return A list of supported protocols.
	 */
	public static List<Protocol> getProtocols()
	{
		return Collections.<Protocol>singletonList(Protocols.HTTP);
	}

	/* (non-Javadoc)
	 * @see org.safehaus.asyncweb.container.ServiceContainer#addServiceHandler(org.safehaus.asyncweb.container.ServiceHandler)
	 */
	public void addServiceHandler(ServiceHandler serviceHandler)
	{
		throw new UnsupportedOperationException("This container accepts no service handlers");
	}

	/* (non-Javadoc)
	 * @see org.safehaus.asyncweb.container.ServiceContainer#addTransport(org.safehaus.asyncweb.transport.Transport)
	 */
	public void addTransport(Transport transport)
	{
		throw new UnsupportedOperationException("This container is bound to a transport");
	}

	/* (non-Javadoc)
	 * @see org.safehaus.asyncweb.container.ServiceContainer#dispatchRequest(org.safehaus.asyncweb.request.AsyncWebRequest)
	 */
	public void dispatchRequest(AsyncWebRequest request)
	{
		HttpResponse response = request.createHttpResponse();
		ServerCall call = new AsyncWebServerCall(request, response, confidential, super.address);
		try
		{
			super.handle(call);
			request.commitResponse(response);
		}
		catch (IOException ex)
		{
			logger.log(Level.WARNING, "Error while handling server call", ex);
			ex.printStackTrace();
			throw new RuntimeException(ex.getMessage(), ex);
		}
	}

	/* (non-Javadoc)
	 * @see org.restlet.Restlet#start()
	 */
	@SuppressWarnings("unchecked")
	public void start() throws ContainerLifecycleException
	{
		if(isStarted())
		{
			return;
		}
		if (Protocols.HTTPS.equals(super.protocol))
		{
			// Stub, we shouldn't run into this, because HTTPS is not exposed as
			// supported protocol

			// TODO: Support HTTPS.
			throw new UnsupportedOperationException("HTTPS is currenly not supported");
		}
		else if (Protocols.HTTP.equals(super.protocol))
		{
			if (transport == null)
			{
				transport = new NIOTransport();
				transport.setPort(super.port);
				transport.setServiceContainer(this);
			}
		}
		else
		{
			// Should never happen.
			throw new RuntimeException("Unsupported protocol: " + super.protocol);
		}
		try
		{
			transport.start();
		}
		catch (TransportException ex)
		{
			logger.log(Level.WARNING, "Failed to start the transport", ex);
			throw new ContainerLifecycleException("Failed to start the transport", ex);
		}
		
		this.confidential = Protocols.HTTPS.equals(super.protocol);
		
		// Setting the flag directly to avoid catching an exception
		super.started = true;
	}

	/* (non-Javadoc)
	 * @see org.restlet.Restlet#stop()
	 */
	@SuppressWarnings("unchecked")
	public void stop()
	{
		if (!isStarted())
		{
			return;
		}
		try
		{
			transport.stop();
		}
		catch (TransportException ex)
		{
			logger.log(Level.WARNING, "Failed to stop transport", ex);
		}
		
		// Setting the flag directly to avoid catching an exception
		super.started = false;
	}

}
