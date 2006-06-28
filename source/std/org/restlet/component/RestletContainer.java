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

import org.restlet.AbstractRestlet;
import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.data.ParameterList;

import com.noelios.restlet.impl.ContextClient;

/**
 * Component whose main purpose is to contain and manage a set of Restlets (Handlers, Routers or Filters) in
 * order to constitue a coherent processing chain for incoming REST calls. One of the cont 
 * Restlet containers can also be 
 * contained within a parent Restlet server.
 * @see <a href="http://www.restlet.org/tutorial#part05">Tutorial: Restlets servers and containers</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class RestletContainer extends AbstractComponent
{
	/** The root Restlet to use. */
	protected Restlet root;
	
   /**
    * Constructor.
    */
   public RestletContainer()
   {
   	this(null, null);
   }

   /**
    * Constructor.
    * @param parameters The initial parameters.
    */
   public RestletContainer(ParameterList parameters)
   {
      this(null, parameters);
   }

   /**
    * Constructor.
    * @param owner The owner component.
    */
   public RestletContainer(Component owner)
   {
      this(owner, null);
   }

   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    */
   public RestletContainer(Component owner, ParameterList parameters)
   {
      super(owner, parameters);

      // Adds the default context client
      getClients().put(ContextClient.DEFAULT_NAME, new ContextClient(this, null));
   }

   /**
    * Attaches the root Restlet that will receive all incoming calls. In general, instance of Router, 
    * Filter or Handler interfaces will be used as root of containers.
    * @param root The root Restlet to use.
    */
   public void attach(Restlet root)
   {
   	this.root = root;
   }

   /**
    * Detaches the root Restlet.
    */
   public void detach()
   {
   	this.root = null;
   }
   
   /**
    * Handles a direct call.
    * @param call The call to handle.
    */
	public void handle(Call call)
   {
		AbstractRestlet.handle(call, this.root);
   }

}
