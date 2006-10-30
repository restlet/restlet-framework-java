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

package com.noelios.restlet.util;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.spi.ServerList;
import org.restlet.util.WrapperList;

/**
 * Servers list implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ServerListImpl extends WrapperList<Server> implements ServerList
{
	/** The context. */
	private Context context;
	
	/** The target Restlet of added servers. */
	private Restlet target;

	/**
	 * Constructor.
	 * @param context The context.
	 * @param target The target Restlet of added servers.
	 */
	public ServerListImpl(Context context, Restlet target)
	{
		this.context = context;
		this.target = target;
	}

	/**
	 * Returns the context.
	 * @return The context.
	 */
	public Context getContext()
	{
		return this.context;
	}

	/**
	 * Returns the target Restlet.
	 * @return The target Restlet.
	 */
	public Restlet getTarget()
	{
		return this.target;
	}

	/**
	 * Adds a server at the end of the list.
	 * @return True (as per the general contract of the Collection.add method).
	 */
	public boolean add(Server server)
	{
		return super.add(server);
	}

	/**
	 * Adds a new server connector in the map supporting the given protocol.
	 * @param protocol The connector protocol.
	 * @return The added server.
	 */
	public Server add(Protocol protocol)
	{
		Server result = new Server(getContext(), protocol, null, protocol.getDefaultPort(), getTarget());
		add(result);
		return result;
	}

	/**
	 * Adds a new server connector in the map supporting the given protocol on the specified port.
	 * @param protocol The connector protocol.
	 * @param port The listening port.
	 * @return The added server.
	 */
	public Server add(Protocol protocol, int port)
	{
		Server result = new Server(getContext(), protocol, null, port, getTarget());
		add(result);
		return result;
	}

	/**
	 * Adds a new server connector in the map supporting the given protocol on the specified IP address and port.
	 * @param protocol The connector protocol.
	 * @param address The optional listening IP address (useful if multiple IP addresses available).
	 * @param port The listening port.
	 * @return The added server.
	 */
	public Server add(Protocol protocol, String address, int port)
	{
		Server result = new Server(getContext(), protocol, address, port, getTarget());
		add(result);
		return result;
	}

}
