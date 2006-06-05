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

import java.util.Map;

import org.restlet.Call;
import org.restlet.Restlet;
import org.restlet.connector.Client;
import org.restlet.connector.Server;
import org.restlet.data.Protocol;

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
	 * Returns the modifiable map of properties.
	 * @return The modifiable map of properties.
	 */
	public Map<String, String> getProperties();
	
	/**
	 * Returns the modifiable map of client connectors.
	 * @return The modifiable map of client connectors.
	 */
	public Map<String, Client> getClients();

	/**
	 * Adds a new client connector to the component.
	 * @param name The connector name.
	 * @param client The client connector to add.
	 * @return The added client.
	 */
	public Client addClient(String name, Client client);

	/**
	 * Adds a new client connector to the component.
	 * @param name The connector name.
	 * @param protocol The connector protocol.
	 * @return The added client.
	 */
	public Client addClient(String name, Protocol protocol);
	
   /**
    * Calls a client connector.
    * @param name The name of the client connector.
    * @param call The call to handle.
    * @throws IOException
    */
   public void callClient(String name, Call call);
	
	/**
	 * Returns the modifiable map of server connectors.
	 * @return The modifiable map of server connectors.
	 */
	public Map<String, Server> getServers();

	/**
	 * Adds a new server connector to the component.
	 * @param name The connector name.
	 * @param server The server connector to add.
	 * @return The added server.
	 */
	public Server addServer(String name, Server server);

	/**
	 * Adds a new server connector to the component.
	 * @param name The connector name.
	 * @param protocol The connector protocol.
	 * @return The added server.
	 */
	public Server addServer(String name, Protocol protocol);

	/**
	 * Adds a new server connector to the component.
	 * @param name The connector name.
	 * @param protocol The connector protocol.
    * @param port The listening port.
	 * @return The added server.
	 */
	public Server addServer(String name, Protocol protocol, int port);

	/**
	 * Adds a new server connector to the component.
	 * @param name The connector name.
	 * @param protocol The connector protocol.
    * @param address The optional listening IP address (useful if multiple IP addresses available).
    * @param port The listening port.
	 * @return The added server.
	 */
	public Server addServer(String name, Protocol protocol, String address, int port);
	
}
