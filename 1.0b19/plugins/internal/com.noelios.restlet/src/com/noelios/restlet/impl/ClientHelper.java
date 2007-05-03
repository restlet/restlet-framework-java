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

import java.util.logging.Logger;

import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.ParameterList;

/**
 * Delegate used by API connector classes to get support from the implementation classes.  
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ClientHelper extends ConnectorHelper
{
	/** The client to help. */
	private Client client;
	
	/**
	 * Constructor.
	 * @param client The client to help.
	 */
	public ClientHelper(Client client)
	{
		this.client = client;
	}
	
	/**
	 * Returns the client to help.
	 * @return The client to help.
	 */
	public Client getClient()
	{
		return this.client;
	}

	/**
	 * Returns the server parameters.
	 * @return The server parameters.
	 */
	public ParameterList getParameters()
	{
		ParameterList result = (getClient() != null) ? getClient().getContext().getParameters() : null;
		if(result == null) result = new ParameterList();
		return result;
	}

	/**
	 * Returns the server logger.
	 * @return The server logger.
	 */
	public Logger getLogger()
	{
		return getClient().getLogger();
	}

	/**
	 * Returns the server context.
	 * @return The server context.
	 */
	public Context getContext()
	{
		return getClient().getContext();
	}

}
