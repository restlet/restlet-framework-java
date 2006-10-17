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

import java.util.logging.Level;

import org.restlet.Context;
import org.safehaus.asyncweb.container.ContainerLifecycleException;
import org.safehaus.asyncweb.container.ServiceContainer;
import org.safehaus.asyncweb.container.ServiceHandler;
import org.safehaus.asyncweb.http.HttpResponse;
import org.safehaus.asyncweb.request.AsyncWebRequest;
import org.safehaus.asyncweb.transport.Transport;
import org.safehaus.asyncweb.transport.TransportException;

import com.noelios.restlet.impl.http.HttpServer;
import com.noelios.restlet.impl.http.HttpServerCall;

/**
 * Abstract AsyncWeb server connector. Here is the list of parameters that are supported:
 * <table>
 * 	<tr>
 * 		<th>Parameter name</th>
 * 		<th>Value type</th>
 * 		<th>Default value</th>
 * 		<th>Description</th>
 * 	</tr>
 * 	<tr>
 * 		<td>ioWorkerCount</td>
 * 		<td>int</td>
 * 		<td>2</td>
 * 		<td>Number of worker threads to employ.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>useForwardedForHeader</td>
 * 		<td>boolean</td>
 * 		<td>false</td>
 * 		<td>True if the "X-Forwarded-For" HTTP header should be used to get client addresses.</td>
 * 	</tr>
 *	</table>
 *	<br/> 
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
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AsyncWebServer extends HttpServer implements ServiceContainer
{
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
    * @param context The context.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public AsyncWebServer(Context context, String address, int port)
   {
      super(context, address, port);
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
		HttpServerCall call = new AsyncWebServerCall(getLogger(), request, response, confidential, getAddress());
		handle(call);
		request.commitResponse(response);
	}

   /** Starts the Connector. */
	@SuppressWarnings("unchecked")
	public void start() throws ContainerLifecycleException
	{
		if(!isStarted())
		{
			try
			{
				getTransport().start();
				super.start();
			}
			catch (TransportException ex)
			{
				getLogger().log(Level.WARNING, "Failed to start the transport", ex);
				throw new ContainerLifecycleException("Failed to start the transport", ex);
			}
			catch (Exception e)
			{
				getLogger().log(Level.WARNING, "Failed to start the AsyncWeb HTTP Server", e);
				throw new ContainerLifecycleException("Failed to start the AsyncWeb HTTP Server", e);
			}
		}
	}

   /** Stops the Connector. */
	@SuppressWarnings("unchecked")
	public void stop()
	{
		if(isStarted())
		{
			try
			{
				getTransport().stop();
				super.stop();
			}
			catch (TransportException ex)
			{
				getLogger().log(Level.WARNING, "Failed to stop transport", ex);
			}
			catch (Exception e)
			{
				getLogger().log(Level.WARNING, "Failed to start the AsyncWeb HTTP Server", e);
			}
		}
	}

   /**
    * Returns the number of worker threads to employ.
    * @return The number of worker threads to employ.
    */
   public int getIoWorkerCount()
   {
   	return Integer.parseInt(getContext().getParameters().getFirstValue("ioWorkerCount", "2"));
   }

	/**
	 * Sets the AsyncWeb transport layer.
	 * @param transport The AsyncWeb transport layer.
	 */
	protected void setTransport(Transport transport)
	{
		this.transport = transport;
	}

	/**
	 * Returns the AsyncWeb transport layer.
	 * @return The AsyncWeb transport layer.
	 */
	protected Transport getTransport()
	{
		return this.transport;
	}

}
