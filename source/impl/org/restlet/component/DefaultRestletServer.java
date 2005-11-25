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

package org.restlet.component;

import java.io.IOException;

import org.restlet.Manager;
import org.restlet.UniformCall;
import org.restlet.connector.Client;
import org.restlet.connector.Server;

/**
 * Default restlet server that can be easily subclassed.
 */
public class DefaultRestletServer implements RestletServer
{
   /** The deletate restlet server. */
   protected RestletServer delegate;
   
   /**
    * Constructor.
    * @param name The origin server's name.
    */
   public DefaultRestletServer(String name)
   {
      this.delegate = Manager.createRestletServer(name);
   }
   
   /**
    * Adds a restlet container.
    * @param name       The unique name of the container.
    * @param container  The container to add.
    * @return           The added container.
    */
   public RestletContainer addContainer(String name, RestletContainer container)
   {
      return delegate.addContainer(name, container);
   }

   /**
    * Removes a restlet container.
    * @param name The name of the container to remove.
    */
   public void removeContainer(String name)
   {
      delegate.removeContainer(name);
   }

   /**
    * Returns the default container handling direct calls to the server.
    * @return The default container.
    */
   public RestletContainer getDefaultContainer()
   {
      return delegate.getDefaultContainer();
   }

   /**
    * Sets the default container handling direct calls to the server.
    * @param container The default container.
    */
   public void setDefaultContainer(RestletContainer container)
   {
      delegate.setDefaultContainer(container);
   }

   /**
    * Adds a server connector to this component.
    * @param server  The server connector to add.
    * @return        The server connector added.
    */
   public Server addServer(Server server)
   {
      return delegate.addServer(server);
   }

   /**
    * Removes a server connector from this component.
    * @param name The name of the server connector to remove.
    */
   public void removeServer(String name)
   {
      delegate.removeServer(name);
   }

   /**
    * Adds a client connector to this component.
    * @param client  The client connector to add.
    * @return        The client connector added.
    */
   public Client addClient(Client client)
   {
      return delegate.addClient(client);
   }

   /**
    * Removes a client connector from this component.
    * @param name The name of the client connector to remove.
    */
   public void removeClient(String name)
   {
      delegate.removeClient(name);
   }

   /**
    * Calls a client connector.
    * @param name The name of the client connector.
    * @param call The call to handle.
    */
   public void callClient(String name, UniformCall call) throws IOException
   {
      delegate.callClient(name, call);
   }

   /** Start hook. */
   public void start()
   {
      delegate.start();
   }

   /** Stop hook. */
   public void stop()
   {
      delegate.stop();
   }

   /**
    * Handles a uniform call.
    * @param call The uniform call to handle.
    */
   public void handle(UniformCall call)
   {
      delegate.handle(call);
   }

   /**
    * Returns the name of this REST component.
    * @return The name of this REST component.
    */
   public String getName()
   {
      return delegate.getName();
   }
   
   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
   public String getDescription()
   {
      return delegate.getDescription();
   }
   
}
