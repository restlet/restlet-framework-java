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

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.component.ApplicationDelegate;
import org.restlet.component.Container;
import org.restlet.data.Protocol;

import com.noelios.restlet.impl.connector.LocalClient;

/**
 * Context based on a parent container's context but dedicated to an application. This is important to allow
 * contextual access to application's resources.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ApplicationContext extends ContextImpl
{
	/** The local client. */
	private LocalClient localClient;
	
	/** The parent container. */
	private Container container;
	
	/** The application delegate. */
	private ApplicationDelegate applicationDelegate;
	
	/**
	 * Constructor.
	 * @param container The parent container.
	 * @param applicationDelegate The parent application.
    * @param logger The logger instance of use.
	 */
	public ApplicationContext(Container container, ApplicationDelegate applicationDelegate, Logger logger)
	{
		super(logger);
		this.container = container;
		this.applicationDelegate = applicationDelegate;
		this.localClient = null;
	}
	
	/**
    * Handles a call.
    * @param protocol The protocol to use for the handling.
    * @param request The request to handle.
    * @param response The response to update.
    */
   public void handle(Protocol protocol, Request request, Response response)
   {
		if(protocol.equals(Protocol.CONTEXT) || protocol.equals(Protocol.FILE))
		{
			getLocalClient().handle(request, response);
		}
		else
		{
			getContainer().getContext().getClient().handle(request, response);
		}
   }

	/**
	 * Returns the application delegate.
	 * @return the application delegate.
	 */
	public ApplicationDelegate getApplicationDelegate()
	{
		return this.applicationDelegate;
	}

	/**
	 * Sets the application delegate.
	 * @param applicationDelegate The application delegate. 
	 */
	public void setApplicationDelegate(ApplicationDelegate applicationDelegate)
	{
		this.applicationDelegate = applicationDelegate;
	}

	/**
	 * Returns the local client.
	 * @return the local client.
	 */
	protected LocalClient getLocalClient()
	{
		if(this.localClient == null)
		{
			this.localClient = new LocalClient();
		}
		
		return this.localClient;
	}

	/**
	 * Sets the local client.
	 * @param localClient The localClient.
	 */
	protected void setLocalClient(LocalClient localClient)
	{
		this.localClient = localClient;
	}

	/**
	 * Returns the parent container.
	 * @return The parent container.
	 */
	public Container getContainer()
	{
		return this.container;
	}

	/**
	 * Sets the parent container.
	 * @param container The parent container.
	 */
	public void setContainer(Container container)
	{
		this.container = container;
	}
	
}
