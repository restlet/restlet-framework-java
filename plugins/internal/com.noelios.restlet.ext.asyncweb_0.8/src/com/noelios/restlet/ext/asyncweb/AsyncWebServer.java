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
import java.util.logging.Logger;

import org.restlet.component.Component;
import org.restlet.data.ParameterList;
import org.safehaus.asyncweb.container.ContainerLifecycleException;
import org.safehaus.asyncweb.container.ServiceContainer;
import org.safehaus.asyncweb.container.ServiceHandler;
import org.safehaus.asyncweb.http.HttpResponse;
import org.safehaus.asyncweb.request.AsyncWebRequest;
import org.safehaus.asyncweb.transport.Transport;
import org.safehaus.asyncweb.transport.TransportException;

import com.noelios.restlet.impl.AbstractHttpServer;
import com.noelios.restlet.impl.AbstractHttpServerCall;

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
 *	</table>
 *	<br/> 
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
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AsyncWebServer extends AbstractHttpServer implements ServiceContainer
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(AsyncWebServer.class.getCanonicalName());

	/**
	 * Indicates if the server is acting in HTTPS mode.
	 */
	protected boolean confidential;

	/**
	 * AyncWeb transport layer.
	 */
	protected Transport transport;

   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public AsyncWebServer(Component owner, ParameterList parameters, String address, int port)
   {
      super(owner, parameters, address, port);
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
		AbstractHttpServerCall call = new AsyncWebServerCall(request, response, confidential, super.address);
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
				transport.start();

				// Setting the flag directly to avoid catching an exception
				this.started = true;
			}
			catch (TransportException ex)
			{
				logger.log(Level.WARNING, "Failed to start the transport", ex);
				throw new ContainerLifecycleException("Failed to start the transport", ex);
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
				transport.stop();

				// Setting the flag directly to avoid catching an exception
				this.started = false;
			}
			catch (TransportException ex)
			{
				logger.log(Level.WARNING, "Failed to stop transport", ex);
			}
		}
	}

   /**
    * Returns the number of worker threads to employ.
    * @return The number of worker threads to employ.
    */
   public int getIoWorkerCount()
   {
   	return Integer.parseInt(getParameters().getFirstValue("ioWorkerCount", "2"));
   }

}
