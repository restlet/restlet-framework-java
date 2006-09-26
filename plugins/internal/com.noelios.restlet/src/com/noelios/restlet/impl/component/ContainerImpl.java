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

import org.restlet.Call;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.component.Application;
import org.restlet.component.ClientList;
import org.restlet.component.Container;
import org.restlet.component.ServerList;
import org.restlet.connector.Client;
import org.restlet.connector.Server;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.spi.Factory;

/**
 * Component containing a set of connectors and applications. The connectors are shared by the 
 * applications and the container attemps to isolate each application from each other.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ContainerImpl extends Container
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(ContainerImpl.class.getCanonicalName());

   /** The context. */
	private Context context;
   
   /** The list of applications. */
   private List<Application> applications;

   /** The map of client connectors. */
	private ClientList clients;

	/** The root Restlet. */
	private Restlet root;
	
   /** The map of server connectors. */
	private ServerList servers;

	/** Indicates if the restlet was started. */
   private boolean started;

	/**
	 * Constructor that adds a default local connector and uses a local logger.
	 */
	public ContainerImpl()
	{
		this(null);
	}
	
   /**
    * Constructor.
    * @param context The context.
    * @param root The root Restlet.
    */
   public ContainerImpl(Restlet root)
   {
   	super((Container)null);
		this.context = new ContainerContext(this, logger);
      this.applications = null;
      this.clients = null;
      this.root = root;
      this.servers = null;

      // Add a local client
      List<Protocol> protocols = new ArrayList<Protocol>();
      protocols.add(Protocol.CONTEXT);
      protocols.add(Protocol.FILE);
      getClients().add(Factory.getInstance().createClient(protocols));
   }
   
   /**
    * Returns the modifiable list of applications.
    * @return The modifiable list of applications.
    */
   public List<Application> getApplications()
   {
   	if(this.applications == null) new ArrayList<Application>();
   	return this.applications;
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
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      if(getRoot() != null)
      {
   		getRoot().handle(call);
      }
      else
      {
         call.setStatus(Status.SERVER_ERROR_INTERNAL);
         logger.log(Level.SEVERE, "No root handler defined.");
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
   	
   	if(this.applications != null)
   	{
	      for(Application application : this.applications)
	      {
	         application.start();
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
   	
   	if(this.applications != null)
   	{
	      for(Application application : this.applications)
	      {
	         application.stop();
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
		if(this.clients == null) this.clients = new ClientList();
		return this.clients;
	}

	/**
	 * Returns the modifiable list of server connectors.
	 * @return The modifiable list of server connectors.
	 */
	public ServerList getServers()
	{
		if(this.servers == null) this.servers = new ServerList(this);
		return this.servers;
	}

	/**
	 * Returns the root Restlet.
	 * @return The root Restlet.
	 */
	public Restlet getRoot()
	{
		return this.root;
	}

   /**
	 * Sets the root Restlet that will receive all incoming calls. In general, instance of Router, 
	 * Filter or Handler interfaces will be used as root of containers.
	 * @param root The root Restlet to use.
	 */
	public void setRoot(Restlet root)
	{
		this.root = root;
	}

	/**
	 * Indicates if a root Restlet is set. 
	 * @return True if a root Restlet is set. 
	 */
	public boolean hasRoot()
	{
		return (getRoot() != null);
	}

}
