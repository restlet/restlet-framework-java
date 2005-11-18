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

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.UniformCall;
import org.restlet.component.RestletContainer;
import org.restlet.component.RestletServer;

/**
 * Component acting as a container for other components called restlets containers.
 * Incoming calls are normally handled via pluggable server connectors.
 * Outcoming calls are normally handled via pluggable client connectors.
 * Other direct calls are handled by the default container.
 */
public class RestletServerImpl extends OriginServerImpl implements RestletServer
{
   /**
    * The restlets containers.
    *@link aggregationByValue
    *      @associates <{DefaultContainer}>
    * @supplierCardinality 0..*
    * @clientCardinality 1
    * @label containers
    */
   protected Map<String, RestletContainer> containers;

   /** The default container handling direct calls on the server. */
   protected RestletContainer defaultContainer;

   /**
    * Constructor.
    * @param name The origin server's name.
    */
   public RestletServerImpl(String name)
   {
      super(name);
      this.containers = new TreeMap<String, RestletContainer>();
      this.defaultContainer = null;
   }

   /**
    * Adds a restlet container.
    * @param name 		The unique name of the container.
    * @param container 	The container to add.
    * @return 				The added container.
    */
   public RestletContainer addContainer(String name, RestletContainer container)
   {
      this.containers.put(name, container);
      return container;
   }

   /**
    * Removes a restlet container.
    * @param name	The name of the container to remove.
    */
   public void removeContainer(String name)
   {
      this.containers.remove(name);
   }

   /**
    * Sets the default container handling direct calls to the server.
    * @param container The default container.
    */
   public void setDefaultContainer(RestletContainer container)
   {
      this.defaultContainer = container;
   }

   /**
    * Returns the default container handling direct calls to the server.
    * @return The default container.
    */
   public RestletContainer getDefaultContainer()
   {
      return this.defaultContainer;
   }

   /**
    * Handles a direct call.
    * @param call The call to handle.
    */
   public void handle(UniformCall call)
   {
      if (getDefaultContainer() != null)
      {
         getDefaultContainer().handle(call);
      }
      else
      {
         // LOG PROBLEM
      }
   }

   /**
    * Start hook.
    * Starts all containers.
    */
   public void start()
   {
      super.start();

      for (Iterator iter = this.containers.keySet().iterator(); iter.hasNext(); )
      {
         this.containers.get(iter.next()).start();
      }
   }

   /**
    * Stop hook.
    * Stops all containers.
    */
   public void stop()
   {
      super.stop();

      for (Iterator iter = this.containers.keySet().iterator(); iter.hasNext(); )
      {
         this.containers.get(iter.next()).stop();
      }
   }

}




