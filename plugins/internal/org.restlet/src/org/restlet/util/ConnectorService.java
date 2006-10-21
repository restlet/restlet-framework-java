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

package org.restlet.util;

import java.util.List;

import org.restlet.data.Protocol;

/**
 * Service providing client and server connectors.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ConnectorService extends Service
{
	/** The list of client protocols used. */
	private List<Protocol> clientProtocols;

	/** The list of server protocols accepted. */
	private List<Protocol> serverProtocols;
	
	/**
	 * Constructor.
	 * @param enabled True if the service has been enabled.
	 */
	public ConnectorService(boolean enabled)
	{
		super(enabled);
		this.clientProtocols = null;
		this.serverProtocols = null;
	}
	
	/**
	 * Returns the list of client protocols used. 
	 * @return The list of client protocols used.
	 */
	public List<Protocol> getClientProtocols()
	{
		return this.clientProtocols;
	}

	/**
	 * Returns the list of server protocols accepted. 
	 * @return The list of server protocols accepted.
	 */
	public List<Protocol> getServerProtocols()
	{
		return this.serverProtocols;
	}

}
