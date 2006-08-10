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

import org.restlet.AbstractRestlet;
import org.restlet.Call;
import org.restlet.Factory;
import org.restlet.Restlet;
import org.restlet.connector.DefaultClient;
import org.restlet.data.ParameterList;
import org.restlet.data.Protocol;

/**
 * Component whose main purpose is to contain and manage a set of Restlets. The goal is create to constitue 
 * a coherent processing chain for incoming REST calls. In addition, Restlet containers can also be 
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
      List<Protocol> protocols = new ArrayList<Protocol>();
      protocols.add(Protocol.CONTEXT);
      protocols.add(Protocol.FILE);
      getClients().put(Factory.CONTEXT_CLIENT_NAME, new DefaultClient(protocols));
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
    * Returns the root Restlet.
    * @return The root Restlet.
    */
   public Restlet getRoot()
   {
   	return this.root;
   }
   
   /**
    * Indicates if a root Restlet is set. 
    * @return True if a root Restlet is set. 
    */
   public boolean hasRoot()
   {
   	return this.root != null;
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
