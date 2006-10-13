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

package org.restlet;

import java.util.Arrays;
import java.util.List;

import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.spi.Factory;

/**
 * Generic server connector. It internally uses one of the available connectors registered with the current
 * Restlet implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Server extends Connector
{
	/** The target handler. */
	private Handler target;

	/**
	 * Constructor.
	 * @param context The context.
	 */
	public Server(Context context)
	{
		this(context, null);
	}

	/**
	 * Constructor.
	 * @param context The context.
	 * @param target The target handler.
	 */
	public Server(Context context, Handler target)
	{
		super(context);
		this.target = target;
	}

	/**
	 * Constructor.
	 * @param wrappedServer The wrapped server.
	 */
	protected Server(Server wrappedServer)
	{
		super(wrappedServer);
	}

	/**
	 * Constructor using the protocol's default port.
	 * @param protocol The connector protocol.
	 * @param target The target handler.
	 */
	public Server(Protocol protocol, Handler target)
	{
		this(protocol, null, protocol.getDefaultPort(), target);
	}

	/**
	 * Constructor.
	 * @param protocol The connector protocol.
	 * @param port The listening port.
	 * @param target The target handler.
	 */
	public Server(Protocol protocol, int port, Handler target)
	{
		this(protocol, null, port, target);
	}

	/**
	 * Constructor.
	 * @param protocols The connector protocols.
	 * @param port The listening port.
	 * @param target The target handler.
	 */
	public Server(List<Protocol> protocols, int port, Handler target)
	{
		this(protocols, null, port, target);
	}

	/**
	 * Constructor.
	 * @param protocol The connector protocol.
	 * @param address The optional listening IP address (useful if multiple IP addresses available).
	 * @param port The listening port.
	 * @param target The target handler.
	 */
	public Server(Protocol protocol, String address, int port, Handler target)
	{
		this(Arrays.asList(protocol), address, port, target);
	}

	/**
	 * Constructor.
	 * @param protocols The connector protocols.
	 * @param address The optional listening IP address (useful if multiple IP addresses available).
	 * @param port The listening port.
	 * @param target The target handler.
	 */
	public Server(List<Protocol> protocols, String address, int port, Handler target)
	{
		super(Factory.getInstance().createServer(protocols, address, port, target));
	}

	/**
	 * Returns the wrapped server.
	 * @return The wrapped server.
	 */
	private Server getWrappedServer()
	{
		return (Server)getWrappedHandler();
	}

	/**
	 * Returns the target handler.
	 * @return The target handler.
	 */
	public Handler getTarget()
	{
		return (getWrappedServer() != null) ? getWrappedServer().getTarget() : this.target;
	}

	/**
	 * Handles a call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
   	if(getWrappedServer() != null)
   	{
   		getWrappedServer().handle(request, response);
   	}
   	else
   	{
   		super.handle(request, response);
   		
   		if(getTarget() != null)
   		{
   			response.setStatus(Status.SUCCESS_OK);
   			getTarget().handle(request, response);
   		}
   	}
	}
	
	/**
	 * Indicates if a target handler is set. 
	 * @return True if a target handler is set. 
	 */
	public boolean hasTarget()
	{
		return (getWrappedServer() != null) ? getWrappedServer().hasTarget()
				: this.target != null;
	}

	/**
	 * Sets the target handler.
	 * @param target The target handler.
	 */
	public void setTarget(Handler target)
	{
		if (getWrappedServer() != null)
		{
			getWrappedServer().setTarget(target);
		}
		else
		{
			this.target = target;
		}
	}

}
