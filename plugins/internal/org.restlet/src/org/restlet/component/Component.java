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

import java.io.IOException;

import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.connector.ClientMap;
import org.restlet.connector.ServerMap;
import org.restlet.data.ParameterList;

/**
 * Abstract unit of software instructions and internal state. "A component is an abstract
 * unit of software instructions and internal state that provides a transformation of data
 * via its interface." Roy T. Fielding
 * @see <a href="http://www.ics.uci.edu/~fielding/pubs/dissertation/software_arch.htm#sec_1_2_1">Source
 * dissertation</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public interface Component extends Restlet
{
	/**
	 * Calls a client connector. If no matching connector is available in this component,
	 * the owner components will recursively be used in order to find the closest match.
	 * @param name The name of the client connector.
	 * @param call The call to handle.
	 * @throws IOException
	 */
	public void callClient(String name, Call call);

	/**
	 * Returns the modifiable list of parameters.
	 * @return The modifiable list of parameters.
	 */
	public ParameterList getParameters();

	/**
	 * Returns the modifiable map of client connectors.
	 * @return The modifiable map of client connectors.
	 */
	public ClientMap getClients();

	/**
	 * Returns the modifiable map of server connectors.
	 * @return The modifiable map of server connectors.
	 */
	public ServerMap getServers();
}
