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

import org.restlet.Maplet;
import org.restlet.Restlet;
import org.restlet.RestletCall;
import org.restlet.RestletException;
import org.restlet.Manager;
import org.restlet.UniformCall;
import org.restlet.connector.Client;
import org.restlet.connector.Server;

/**
 * Default restlet container that can be easily subclassed.<br/>
 * <br/>
 * Component acting as a container for call handlers named restlets.
 * Calls are first intercepted by the container which can do various checks before effectively
 * delegating it to one of the registered root maplets or restlets.
 */
public class DefaultRestletContainer implements RestletContainer
{
   /** The delegate restlet container. */
   protected RestletContainer delegate;
   
   /**
    * Constructor.
    * @param name The unique name of the container.
    */
   public DefaultRestletContainer(String name)
   {
      this.delegate = Manager.createRestletContainer(name);
   }
   
   /**
    * Returns a new maplet acting as a delegate for maplets.
    * Developers who need to extend the default maplets should override it.
    * @return A new maplet.
    */
   public Maplet createMapletDelegate()
   {
      return delegate.createMapletDelegate();
   }

   /**
    * Handles a call to a resource or a set of resources.
    * @param call The call to handle.
    * @throws RestletException
    */
   public void handle(RestletCall call) throws RestletException
   {
      delegate.handle(call);
   }

   /**
    * Returns the container.
    * @return The container.
    */
   public RestletContainer getContainer()
   {
      return this;
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

   /**
    * Attaches a restlet instance shared by all calls.
    * @param pathPattern The path pattern used to map calls.
    * @param restlet     The restlet to attach.
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Restlet restlet)
   {
      delegate.attach(pathPattern, restlet);
   }

   /**
    * Attaches a restlet class. A new instance will be created for each call.
    * @param pathPattern   The path pattern used to map calls.
    * @param restletClass  The restlet class to attach (must have a constructor taking a RestletContainer parameter).
    * @see java.util.regex.Pattern
    */
   public void attach(String pathPattern, Class<? extends Restlet> restletClass)
   {
      delegate.attach(pathPattern, restletClass);
   }

   /**
    * Detaches a restlet instance.
    * @param restlet The restlet to detach.
    */
   public void detach(Restlet restlet)
   {
      delegate.detach(restlet);
   }

   /**
    * Detaches a restlet class.
    * @param restletClass  The restlet class to detach.
    */
   public void detach(Class<? extends Restlet> restletClass)
   {
      delegate.detach(restletClass);
   }

   /**
    * Delegates a call to attached restlets.
    * @param call The call to delegate.
    */
   public void delegate(RestletCall call) throws RestletException
   {
      delegate.delegate(call);
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
