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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.restlet.AbstractRestlet;
import org.restlet.Call;
import org.restlet.data.ParameterList;
import org.restlet.data.Statuses;

/**
 * Component composed of multiple Restlet containers. Each container is managing its own resource namespace. 
 * Incoming calls are generally received by a server connector and outcoming calls are handled by one of the 
 * client connectors. Other direct calls are handled by the default container.
 * @see <a href="http://www.restlet.org/tutorial#part05">Tutorial: Restlets servers and containers</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class RestletServer extends AbstractComponent
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(RestletServer.class.getCanonicalName());

   /** The Restlet containers. */
   protected Map<String, RestletContainer> containers;

   /** The default container handling direct calls on the server. */
   protected RestletContainer defaultContainer;

   /**
    * Constructor.
    */
   public RestletServer()
   {
   	this(null, null);
   }

   /**
    * Constructor.
    * @param parameters The initial parameters.
    */
   public RestletServer(ParameterList parameters)
   {
      this(null, parameters);
   }

   /**
    * Constructor.
    * @param owner The owner component.
    */
   public RestletServer(Component owner)
   {
      this(owner, null);
   }

   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    */
   public RestletServer(Component owner, ParameterList parameters)
   {
      super(owner, parameters);
      this.containers = null;
      this.defaultContainer = null;
   }

   /**
    * Returns the modifiable map of containers.
    * @return The modifiable map of containers.
    */
   public Map<String, RestletContainer> getContainers()
   {
   	if(this.containers == null) new TreeMap<String, RestletContainer>();
   	return this.containers;
   }
   
   /**
    * Adds a Restlet container.
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
    * Adds a new Restlet container.
    * @param name The unique name of the container.
    * @return The added container.
    */
   public RestletContainer addContainer(String name)
   {
      return addContainer(name, new RestletContainer(this));
   }

   /**
    * Sets the default container handling direct calls to the server.
    * @param container The default container.
    */
   public void setDefaultTarget(RestletContainer container)
   {
      this.defaultContainer = container;
   }

   /**
    * Returns the default container handling direct calls to the server.
    * @return The default container.
    */
   public RestletContainer getDefaultTarget()
   {
      return this.defaultContainer;
   }

   /**
    * Handles a direct call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
      if(getDefaultTarget() != null)
      {
   		AbstractRestlet.handle(call, getDefaultTarget());
      }
      else
      {
         call.setStatus(Statuses.SERVER_ERROR_INTERNAL);
         logger.log(Level.SEVERE, "No default Restlet container defined");
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
