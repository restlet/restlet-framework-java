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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.Context;
import org.restlet.connector.Client;
import org.restlet.connector.GenericClient;
import org.restlet.connector.Server;
import org.restlet.data.Protocol;
import org.restlet.data.Status;

/**
 * Component containing a set of connectors and applications. The connectors are shared by the 
 * applications and the container attemps to isolate each application from each other.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class Container extends Component
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(Container.class.getCanonicalName());

   /** The list of applications. */
   private List<Application> applications;

   /** The map of client connectors. */
	private ClientList clients;

   /** The map of server connectors. */
	private ServerList servers;

	/**
	 * Constructor that adds a default local connector and uses a local logger.
	 */
	public Container()
	{
		this(null);
		setContext(new ContainerContext(this, logger));
      getClients().add(createLocalClient());
	}
	
   /**
    * Constructor.
    * @param context The parent context.
    */
   public Container(Context context)
   {
   	super(context);
      this.applications = null;
      this.clients = null;
      this.servers = null;
   }

   /**
    * Creates a new local client from the factory. 
    * @return A new local client from the factory.
    */
   private static Client createLocalClient()
   {
      List<Protocol> protocols = new ArrayList<Protocol>();
      protocols.add(Protocol.CONTEXT);
      protocols.add(Protocol.FILE);
      return new GenericClient(protocols);
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
    * Handles a direct call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      if(getRoot() != null)
      {
   		handle(call, getRoot());
      }
      else
      {
         call.setStatus(Status.SERVER_ERROR_INTERNAL);
         logger.log(Level.SEVERE, "Handle not implemented yet...");
      }
   }

   /**
    * Start hook. Starts all containers.
    */
   public void start() throws Exception
   {
      super.start();

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
   }

   /**
    * Stop hook. Stops all containers.
    */
   public void stop() throws Exception
   {
      super.stop();

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
		if(this.servers == null) this.servers = new ServerList();
		return this.servers;
	}

}
