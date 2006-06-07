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

package org.restlet.component;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.AbstractRestlet;
import org.restlet.Call;
import org.restlet.connector.Client;
import org.restlet.connector.DefaultClient;
import org.restlet.connector.DefaultServer;
import org.restlet.connector.Server;
import org.restlet.data.Protocol;

/**
 * Abstract component implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AbstractComponent extends AbstractRestlet implements Component
{
	/** The modifiable map of properties. */
	protected Map<String, String> properties;

   /** The map of client connectors. */
   protected Map<String, Client> clients;

   /** The map of server connectors. */
   protected Map<String, Server> servers;

   /**
    * Constructor.
    */
   public AbstractComponent()
   {
      this((Component)null);
   }

   /**
    * Constructor.
    * @param owner The owner component.
    */
   public AbstractComponent(Component owner)
   {
      super(owner);
   	this.properties = null;
      this.clients = null;
      this.servers = null;
   }

	/**
	 * Returns the modifiable map of properties.
	 * @return The modifiable map of properties.
	 */
	public Map<String, String> getProperties()
	{
		if(this.properties == null) this.properties = new TreeMap<String, String>();
		return this.properties;
	}

	/**
	 * Returns the modifiable map of client connectors.
	 * @return The modifiable map of client connectors.
	 */
	public Map<String, Client> getClients()
	{
		if(this.clients == null) this.clients = new TreeMap<String, Client>();
		return this.clients;
	}

	/**
	 * Adds a new client connector to the component.
	 * @param name The connector name.
	 * @param client The client connector to add.
	 * @return The added client.
	 */
	public Client addClient(String name, Client client)
	{
		getClients().put(name, client);
		return client;
	}

	/**
	 * Adds a new client connector to the component.
	 * @param name The connector name.
	 * @param protocol The connector protocol.
	 * @return The added client.
	 */
	public Client addClient(String name, Protocol protocol)
	{
		return addClient(name, new DefaultClient(protocol));
	}
	
	/**
    * Calls a client connector. If no matching connector is available in this component, 
    * the parent components will recursively be used in order to find the closest match.
    * @param name The name of the client connector.
    * @param call The call to handle.
    */
   public void callClient(String name, Call call)
   {
      Client connector = getClients().get(name);

      if(connector == null)
      {
      	if(getOwner() != null)
      	{
      		getOwner().callClient(name, call);      		
      	}
      	else
      	{
      		throw new IllegalArgumentException("Client connector \"" + name + "\" couldn't be found in the components hierarchy.");
      	}
      }
      else
      {
         connector.handle(call);
      }
   }

	/**
	 * Returns the modifiable map of server connectors.
	 * @return The modifiable map of server connectors.
	 */
	public Map<String, Server> getServers()
	{
		if(this.servers == null) this.servers = new TreeMap<String, Server>();
		return this.servers;
	}

	/**
	 * Adds a new server connector to the component.
	 * @param name The connector name.
	 * @param server The server connector to add.
	 * @return The added server.
	 */
	public Server addServer(String name, Server server)
	{
		getServers().put(name, server);
		return server;
	}

	/**
	 * Adds a new server connector to the component.
	 * @param name The connector name.
	 * @param protocol The connector protocol.
	 * @return The added server.
	 */
	public Server addServer(String name, Protocol protocol)
	{
		return addServer(name, new DefaultServer(protocol, this));
	}

	/**
	 * Adds a new server connector to the component.
	 * @param name The connector name.
	 * @param protocol The connector protocol.
    * @param port The listening port.
	 * @return The added server.
	 */
	public Server addServer(String name, Protocol protocol, int port)
	{
		return addServer(name, new DefaultServer(protocol, this, port));
	}

	/**
	 * Adds a new server connector to the component.
	 * @param name The connector name.
	 * @param protocol The connector protocol.
    * @param address The optional listening IP address (useful if multiple IP addresses available).
    * @param port The listening port.
	 * @return The added server.
	 */
	public Server addServer(String name, Protocol protocol, String address, int port)
	{
		return addServer(name, new DefaultServer(protocol, this, address, port));
	}

   /**
    * Start hook. Starts all client and server connectors.
    */
   public void start() throws Exception
   {
   	if(this.clients != null)
   	{
	      for(Iterator iter = this.clients.keySet().iterator(); iter.hasNext();)
	      {
	         this.clients.get(iter.next()).start();
	      }
   	}
   	
   	if(this.servers != null)
   	{
	      for(Iterator iter = this.servers.keySet().iterator(); iter.hasNext();)
	      {
	         this.servers.get(iter.next()).start();
	      }
   	}
   	
      super.start();
   }

   /**
    * Stop hook. Stops all client and server connectors.
    */
   public void stop() throws Exception
   {
   	if(this.clients != null)
   	{
	      for(Iterator iter = this.clients.keySet().iterator(); iter.hasNext();)
	      {
	         this.clients.get(iter.next()).stop();
	      }
   	}
   	
   	if(this.servers != null)
   	{
	      for(Iterator iter = this.servers.keySet().iterator(); iter.hasNext();)
	      {
	         this.servers.get(iter.next()).stop();
	      }
   	}
   	
      super.stop();
   }

}
