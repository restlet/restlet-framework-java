/*
 * Copyright 2005-2006 Jérôme LOUVEL
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

package com.noelios.restlet.component;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.restlet.UniformCall;
import org.restlet.component.RestletContainer;
import org.restlet.component.RestletServer;

/**
 * Origin server containing Restlets containers.<br/>
 * Each container is managing its own resource namespace.
 * Incoming calls are normally handled via pluggable server connectors.<br/>
 * Outcoming calls are normally handled via pluggable client connectors.<br/>
 * Other direct calls are handled by the default container.
 */
public class RestletServerImpl extends ComponentImpl implements RestletServer
{
   /**
    * The restlets containers.
    * @link aggregationByValue
    * @associates <{DefaultContainer}>
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
    * @param name The unique name of the container.
    * @param container The container to add.
    * @return The added container.
    */
   public RestletContainer addContainer(String name, RestletContainer container)
   {
      this.containers.put(name, container);
      return container;
   }

   /**
    * Removes a restlet container.
    * @param name The name of the container to remove.
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
      if(getDefaultContainer() != null)
      {
         getDefaultContainer().handle(call);
      }
      else
      {
         // LOG PROBLEM
      }
   }

   /**
    * Start hook. Starts all containers.
    */
   public void start() throws Exception
   {
      super.start();

      for(Iterator iter = this.containers.keySet().iterator(); iter.hasNext();)
      {
         this.containers.get(iter.next()).start();
      }
   }

   /**
    * Stop hook. Stops all containers.
    */
   public void stop() throws Exception
   {
      super.stop();

      for(Iterator iter = this.containers.keySet().iterator(); iter.hasNext();)
      {
         this.containers.get(iter.next()).stop();
      }
   }

}
