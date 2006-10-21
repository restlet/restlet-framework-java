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

package com.noelios.restlet.impl.container;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.restlet.Client;
import org.restlet.Container;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.spi.Helper;

import com.noelios.restlet.impl.LogFilter;

/**
 * Container helper.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ContainerHelper implements Helper
{
	/** The helped container. */
	private Container container;

	/** The first Restlet. */
	private Restlet first;

	/** The internal client router. */
	private ClientRouter clientRouter;

	/** The internal server router. */
	private ServerRouter serverRouter;

	/**
	 * Constructor.
	 */
	public ContainerHelper(Container container)
	{
		this.container = container;
		this.first = null;
		this.clientRouter = new ClientRouter(getContainer());
		this.serverRouter = new ServerRouter(getContainer());

		// Add a local client
		List<Protocol> protocols = new ArrayList<Protocol>();
		protocols.add(Protocol.FILE);
		getContainer().getClients().add(new Client(getContainer().getContext(), protocols));
	}

	/**
	 * Creates a new context.
	 * @return The new context.
	 */
	public Context createContext()
	{
		return new ContainerContext(this);
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
	 * Returns the helped container.
	 * @return The helped container.
	 */
	protected Container getContainer()
	{
		return this.container;
	}

	/**
	 * Returns the internal host router.
	 * @return the internal host router.
	 */
	public ServerRouter getServerRouter()
	{
		return this.serverRouter;
	}

	/**
	 * Handles a call.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Request request, Response response)
	{
		if (getFirst() != null)
		{
			getFirst().handle(request, response);
		}
		else
		{
			response.setStatus(Status.SERVER_ERROR_INTERNAL);
			getContainer().getLogger().log(Level.SEVERE,
					"The container wasn't properly started, it can't handle calls.");
		}
	}

	/** Start callback. */
	public void start() throws Exception
	{
		// Initialization of services
		Filter lastFilter = null;

		// Logging of calls
		if (getContainer().getLogService().isEnabled())
		{
			lastFilter = new LogFilter(getContainer().getContext(), getContainer()
					.getLogService().getLoggerName(), getContainer().getLogService()
					.getFormat());
			setFirst(lastFilter);
		}

		// Addition of status pages
		if (getContainer().getStatusService().isEnabled())
		{
			Filter statusFilter = new ContainerStatusFilter(getContainer());
			if (lastFilter != null) lastFilter.setNext(statusFilter);
			if (getFirst() == null) setFirst(statusFilter);
			lastFilter = statusFilter;
		}

		// Reattach the original filter's attached Restlet
		if (getFirst() == null)
		{
			setFirst(getServerRouter());
		}
		else
		{
			lastFilter.setNext(getServerRouter());
		}
	}

	/** Stop callback. */
	public void stop() throws Exception
	{
	}

	/**
	 * Returns the first Restlet.
	 * @return the first Restlet.
	 */
	private Restlet getFirst()
	{
		return this.first;
	}

	/**
	 * Sets the first Restlet.
	 * @param first The first Restlet.
	 */
	private void setFirst(Restlet first)
	{
		this.first = first;
	}

}
