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

package com.noelios.restlet.impl.component;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.component.ClientList;
import org.restlet.component.Container;
import org.restlet.component.ServerList;
import org.restlet.component.VirtualHost;
import org.restlet.connector.Client;
import org.restlet.connector.Server;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.spi.Factory;

import com.noelios.restlet.impl.ClientRouter;
import com.noelios.restlet.impl.ServerRouter;

/**
 * Container implementation. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ContainerImpl extends Container
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(ContainerImpl.class.getCanonicalName());

   /** The map of client connectors. */
	private ClientList clients;
	
   /** The map of server connectors. */
	private ServerList servers;
	
	/** The modifiable list of virtual hosts. */ 
	private List<VirtualHost> hosts;
	
	/** The local host. */
	private VirtualHost localHost;

   /** The context. */
	private Context context;
	
	/** The internal client router. */
	private ClientRouter clientRouter;
	
	/** The internal host router. */
	private ServerRouter hostRouter;

	/** Indicates if the instance was started. */
   private boolean started;
	
   /**
    * Constructor.
    */
   public ContainerImpl()
   {
   	super((Container)null);
		this.context = new ContainerContext(this, logger);
      this.hosts = new ArrayList<VirtualHost>();
      this.localHost = new LocalHost(this);
      this.clientRouter = new ClientRouter(this);
      this.hostRouter = new ServerRouter(this);
      this.clients = null;
      this.servers = null;
      this.started = false;

      // Add a local client
      List<Protocol> protocols = new ArrayList<Protocol>();
      protocols.add(Protocol.CONTEXT);
      protocols.add(Protocol.FILE);
      getClients().add(Factory.getInstance().createClient(protocols));
   }

   /**
    * Returns the modifiable list of host routers.
    * @return The modifiable list of host routers.
    */
   public List<VirtualHost> getHosts()
   {
		return this.hosts;
   }

   /**
    * Returns the local virtual host.
    * @return The local virtual host.
    */
   public VirtualHost getLocalHost()
   {
		return this.localHost;
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
    * Sets the context.
    * @param context The context.
    */
   public void setContext(Context context)
   {
      this.context = context;
   }

   /**
    * Handles a direct call.
    * @param request The request to handle.
    * @param response The response to update.
    */
   public void handle(Request request, Response response)
   {
      if(getHostRouter() != null)
      {
      	getHostRouter().handle(request, response);
      }
      else
      {
      	response.setStatus(Status.SERVER_ERROR_INTERNAL);
         logger.log(Level.SEVERE, "No host router defined.");
      }
   }

   /**
    * Start hook. Starts all containers.
    */
   public void start() throws Exception
   {
   	if(this.clients != null)
   	{
	      for(Client client : this.clients)
	      {
	      	client.start();
	      }
   	}
   	
   	if(this.servers != null)
   	{
	      for(Server server : this.servers)
	      {
	      	server.start();
	      }
   	}

   	this.started = true;
   }

   /**
    * Stop hook. Stops all containers.
    */
   public void stop() throws Exception
   {
      this.started = false;

      if(this.clients != null)
   	{
	      for(Client client : this.clients)
	      {
	         client.stop();
	      }
   	}
   	
   	if(this.servers != null)
   	{
	      for(Server server : this.servers)
	      {
	      	server.stop();
	      }
   	}
   }

   /**
    * Indicates if the Restlet is started.
    * @return True if the Restlet is started.
    */
   public boolean isStarted()
   {
      return this.started;
   }

   /**
    * Indicates if the Restlet is stopped.
    * @return True if the Restlet is stopped.
    */
   public boolean isStopped()
   {
      return !this.started;
   }

	/**
	 * Returns the modifiable list of client connectors.
	 * @return The modifiable list of client connectors.
	 */
   public ClientList getClients()
	{
		if(this.clients == null) this.clients = new ClientListImpl();
		return this.clients;
	}

	/**
	 * Returns the modifiable list of server connectors.
	 * @return The modifiable list of server connectors.
	 */
	public ServerList getServers()
	{
		if(this.servers == null) this.servers = new ServerListImpl(this);
		return this.servers;
	}

	/**
	 * Returns the internal client router.
	 * @return the internal client router.
	 */
	public ClientRouter getClientRouter()
	{
		return this.clientRouter;
	}

	/**
	 * Sets the internal client router.
	 * @param clientRouter The internal client router.
	 */
	public void setClientRouter(ClientRouter clientRouter)
	{
		this.clientRouter = clientRouter;
	}

	/**
	 * Returns the internal host router.
	 * @return the internal host router.
	 */
	public ServerRouter getHostRouter()
	{
		return this.hostRouter;
	}

	/**
	 * Sets the internal host router.
	 * @param hostRouter The internal host router.
	 */
	public void setHostRouter(ServerRouter hostRouter)
	{
		this.hostRouter = hostRouter;
	}

}
