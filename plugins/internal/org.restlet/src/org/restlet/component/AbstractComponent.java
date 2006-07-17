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

import org.restlet.AbstractRestlet;
import org.restlet.Call;
import org.restlet.connector.Client;
import org.restlet.connector.ClientMap;
import org.restlet.connector.ServerMap;
import org.restlet.data.ParameterList;

/**
 * Abstract component implementation.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class AbstractComponent extends AbstractRestlet implements Component
{
	/** The modifiable list of parameters. */
	protected ParameterList parameters;

   /** The map of client connectors. */
   protected ClientMap clients;

   /** The map of server connectors. */
   protected ServerMap servers;
   
   /**
    * Constructor.
    */
   public AbstractComponent()
   {
      this((Component)null);
   }

   /**
    * Constructor.
    * @param parameters The initial parameters.
    */
   public AbstractComponent(ParameterList parameters)
   {
      this(null, parameters);
   }

   /**
    * Constructor.
    * @param owner The owner component.
    */
   public AbstractComponent(Component owner)
   {
      this(owner, null);
   }

   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    */
   public AbstractComponent(Component owner, ParameterList parameters)
   {
      super(owner);
   	this.parameters = parameters;
      this.clients = null;
      this.servers = null;
   }
	
	/**
    * Calls a client connector. If no matching connector is available in this component, 
    * the owner components will recursively be used in order to find the closest match.
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
	 * Returns the modifiable map of client connectors.
	 * @return The modifiable map of client connectors.
	 */
	public ClientMap getClients()
	{
		if(this.clients == null) this.clients = new ClientMap();
		return this.clients;
	}

	/**
	 * Returns the modifiable list of parameters.
	 * @return The modifiable list of parameters.
	 */
	public ParameterList getParameters()
	{
		if(this.parameters == null) this.parameters = new ParameterList();
		return this.parameters;
	}

	/**
	 * Returns the modifiable map of server connectors.
	 * @return The modifiable map of server connectors.
	 */
	public ServerMap getServers()
	{
		if(this.servers == null) this.servers = new ServerMap(this);
		return this.servers;
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
