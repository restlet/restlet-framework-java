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

package com.noelios.restlet.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Container;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.spi.Factory;

/**
 * Container implementation. 
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ContainerImpl extends Container
{
	/** The internal client router. */
	private ClientRouter clientRouter;
	
	/** The internal host router. */
	private ServerRouter hostRouter;
	
   /**
    * Constructor.
    */
   public ContainerImpl()
   {
   	super(new ContainerContext(null));
   	((ContainerContext)getContext()).setContainer(this);
   	
      this.clientRouter = new ClientRouter(this);
      this.hostRouter = new ServerRouter(this);

      // Add a local client
      List<Protocol> protocols = new ArrayList<Protocol>();
      protocols.add(Protocol.CONTEXT);
      protocols.add(Protocol.FILE);
      getClients().add(Factory.getInstance().createClient(getContext(), protocols));
   }

   /**
    * Handles a direct call.
    * @param request The request to handle.
    * @param response The response to update.
    */
   public void handle(Request request, Response response)
   {
      if(getHostRouter() != null)
      {
      	getHostRouter().handle(request, response);
      }
      else
      {
      	response.setStatus(Status.SERVER_ERROR_INTERNAL);
         getLogger().log(Level.SEVERE, "No host router defined.");
      }
   }

	/**
	 * Returns the internal client router.
	 * @return the internal client router.
	 */
	public ClientRouter getClientRouter()
	{
		return this.clientRouter;
	}

	/**
	 * Sets the internal client router.
	 * @param clientRouter The internal client router.
	 */
	public void setClientRouter(ClientRouter clientRouter)
	{
		this.clientRouter = clientRouter;
	}

	/**
	 * Returns the internal host router.
	 * @return the internal host router.
	 */
	public ServerRouter getHostRouter()
	{
		return this.hostRouter;
	}

	/**
	 * Sets the internal host router.
	 * @param hostRouter The internal host router.
	 */
	public void setHostRouter(ServerRouter hostRouter)
	{
		this.hostRouter = hostRouter;
	}

}
