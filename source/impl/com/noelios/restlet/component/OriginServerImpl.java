/*
 * Copyright © 2005 Jérôme LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.noelios.restlet.component;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.UniformCall;
import org.restlet.UniformInterface;
import org.restlet.component.OriginServer;
import org.restlet.connector.Client;
import org.restlet.connector.Server;

/**
 * Abstract origin server implementation.
 */
public abstract class OriginServerImpl implements OriginServer
{
   /** The component name. */
   private String name;

   /** The map of client connectors. */
   protected Map<String, Client> clients;

   /** The map of server connectors. */
   protected Map<String, Server> servers;

   /**
    * Constructor.
    * @param name The component name.
    */
   public OriginServerImpl(String name)
   {
      this.name = name;
      this.clients = new TreeMap<String, Client>();
      this.servers = new TreeMap<String, Server>();
   }

   /**
    * Returns the component name.
    * @return The component name.
    */
   public String getName()
   {
      return this.name;
   }

   /**
    * Adds a server connector to this component.
    * @param server The server connector to add.
    * @return The server connector added.
    */
   public Server addServer(Server server)
   {
      this.servers.put(server.getName(), server);
      return server;
   }

   /**
    * Removes a server connector from this component.
    * @param name The name of the server connector to remove.
    */
   public void removeServer(String name)
   {
      this.servers.remove(name);
   }

   /**
    * Adds a client connector to this component.
    * @param client The client connector to add.
    * @return The client connector added.
    */
   public Client addClient(Client client)
   {
      this.clients.put(client.getName(), client);
      return client;
   }

   /**
    * Removes a client connector from this component.
    * @param name The name of the client connector to remove.
    */
   public void removeClient(String name)
   {
      this.clients.remove(name);
   }

   /**
    * Calls a client connector.
    * @param name The name of the client connector.
    * @param call The call to handle.
    */
   public void callClient(String name, UniformCall call) throws IOException
   {
      UniformInterface connector = (UniformInterface)this.clients.get(name);

      if(connector == null)
      {
         throw new IOException("Client connector \"" + name + "\" couldn't be found.");
      }
      else
      {
         connector.handle(call);
      }
   }

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return "Abstract origin server";
   }

   /**
    * Start hook. Starts all client and server connectors.
    */
   public void start()
   {
      for(Iterator iter = this.clients.keySet().iterator(); iter.hasNext();)
      {
         this.clients.get(iter.next()).start();
      }
      for(Iterator iter = this.servers.keySet().iterator(); iter.hasNext();)
      {
         this.servers.get(iter.next()).start();
      }
   }

   /**
    * Stop hook. Stops all client and server connectors.
    */
   public void stop()
   {
      for(Iterator iter = this.clients.keySet().iterator(); iter.hasNext();)
      {
         this.clients.get(iter.next()).stop();
      }
      for(Iterator iter = this.servers.keySet().iterator(); iter.hasNext();)
      {
         this.servers.get(iter.next()).stop();
      }
   }

}
