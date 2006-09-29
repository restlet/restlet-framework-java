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

package com.noelios.restlet.impl.component;

import java.util.logging.Logger;

import org.restlet.Call;
import org.restlet.Context;
import org.restlet.component.Container;
import org.restlet.connector.Client;
import org.restlet.data.Protocol;

/**
 * Context allowing access to the container's connectors.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ContainerContext extends Context
{
	/** The parent container. */
	private Container container;

	/**
	 * Constructor. 
	 * @param container The parent container.
    * @param logger The logger instance of use.
	 */
	public ContainerContext(Container container, Logger logger)
	{
		super(logger);
		this.setContainer(container);
	}
   
	/**
    * Handles a call.
    * @param call The call to handle.
    */
   public void handle(Call call)
   {
   	Protocol protocol = call.getProtocol();
   	
   	if(protocol == null)
   	{
   		// Attempt to guess the protocol to use
   		// from the target reference scheme
   		protocol = call.getResourceRef().getSchemeProtocol();
   	}
   	
   	if(protocol == null)
   	{
      	throw new UnsupportedOperationException("Unable to determine the protocol to use for this call.");
   	}
   	else
   	{
	   	for(Client client : getContainer().getClients())
	   	{
	   		if(client.getProtocols().contains(protocol))
	   		{
	   			client.handle(call);
	   			return;
	   		}
	   	}
   	}
   	
   	throw new UnsupportedOperationException("The " + protocol + " protocol is not available in this context.");
   }

	/**
	 * Sets the parent container.
	 * @param container The parent container.
	 */
	protected void setContainer(Container container)
	{
		this.container = container;
	}

	/**
	 * Returns the parent container.
	 * @return The parent container.
	 */
	protected Container getContainer()
	{
		return this.container;
	}
}
