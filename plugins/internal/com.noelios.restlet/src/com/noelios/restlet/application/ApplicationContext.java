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

package com.noelios.restlet.application;

import java.util.logging.Logger;

import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

import com.noelios.restlet.DefaultContext;

/**
 * Context based on a parent container's context but dedicated to an application. This is important to allow
 * contextual access to application's resources.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ApplicationContext extends DefaultContext
{
	/** The WAR client. */
	private Client warClient;

	/** The application delegate. */
	private Application application;

	/** The parent context. */
	private Context parentContext;

	/**
	 * Constructor.
	 * @param application The application.
	 * @param parentContext The parent context.
	 * @param logger The logger instance of use.
	 */
	public ApplicationContext(Application application, Context parentContext, Logger logger)
	{
		super(logger);
		this.application = application;
		this.parentContext = parentContext;
		this.warClient = null;
	}

	/**
	 * Handles a call.
	 * @param protocol The protocol to use for the handling.
	 * @param request The request to handle.
	 * @param response The response to update.
	 */
	public void handle(Protocol protocol, Request request, Response response)
	{
		// Add the metadata service as a request attribute
		// so the the client helper can be it
		request.getAttributes().put("org.restlet.metadataService",
				getApplication().getMetadataService());

		if (protocol.equals(Protocol.WAR))
		{
			getWarClient().handle(request, response);
		}
		else
		{
			getParentContext().getClient().handle(request, response);
		}
	}

	/**
	 * Returns the application.
	 * @return the application.
	 */
	public Application getApplication()
	{
		return this.application;
	}

	/**
	 * Returns the WAR client.
	 * @return the WAR client.
	 */
	protected Client getWarClient()
	{
		if (this.warClient == null)
		{
			this.warClient = new Client(Protocol.WAR);
		}

		return this.warClient;
	}

	/**
	 * Sets the WAR client.
	 * @param warClient the WAR client.
	 */
	protected void setWarClient(Client warClient)
	{
		this.warClient = warClient;
	}

	/**
	 * Returns the parent context.
	 * @return The parent context.
	 */
	public Context getParentContext()
	{
		return this.parentContext;
	}

}
