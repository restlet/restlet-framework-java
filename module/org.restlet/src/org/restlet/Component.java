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

import org.restlet.util.ClientList;
import org.restlet.util.ServerList;

/**
 * Restlet managing a set of Clients, Servers and other Restlets. "A component is an abstract unit of 
 * software instructions and internal state that provides a transformation of data via its interface." 
 * Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/software_arch.htm#sec_1_2_1">Source
 * dissertation</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class Component extends Restlet
{
	/** The modifiable list of client connectors. */
	private ClientList clients;

	/** The modifiable list of server connectors. */
	private ServerList servers;

	/**
	 * Constructor.
	 */
	public Component()
	{
		this(null);
	}

	/**
	 * Constructor.
	 * @param context The context.
	 */
	public Component(Context context)
	{
		super(context);
	}

	/**
	 * Returns the modifiable list of client connectors.
	 * @return The modifiable list of client connectors.
	 */
	public ClientList getClients()
	{
		if (this.clients == null) this.clients = new ClientList(getContext());
		return this.clients;
	}

	/**
	 * Returns the modifiable list of server connectors.
	 * @return The modifiable list of server connectors.
	 */
	public ServerList getServers()
	{
		if (this.servers == null) this.servers = new ServerList(getContext(), this);
		return this.servers;
	}

	/**
	 * Start hook. Starts all connectors.
	 */
	public void start() throws Exception
	{
		if (this.clients != null)
		{
			for (Client client : this.clients)
			{
				client.start();
			}
		}

		if (this.servers != null)
		{
			for (Server server : this.servers)
			{
				server.start();
			}
		}

		super.start();
	}

	/**
	 * Stop hook. Stops all connectors.
	 */
	public void stop() throws Exception
	{
		super.stop();

		if (this.clients != null)
		{
			for (Client client : this.clients)
			{
				client.stop();
			}
		}

		if (this.servers != null)
		{
			for (Server server : this.servers)
			{
				server.stop();
			}
		}
	}

}
