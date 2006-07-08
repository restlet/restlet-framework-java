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

package org.restlet.connector;

import org.restlet.component.Component;
import org.restlet.data.Protocol;
import org.restlet.data.WrapperMap;

/**
 * Modifiable map of server connectors.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class ServerMap extends WrapperMap<String, Server>
{
	/** The owner component. */
	protected Component owner;
	
	/**
	 * Constructor.
	 * @param owner The owner component.
	 */
	public ServerMap(Component owner)
	{
		this.owner = owner;
	}
	
	/**
	 * Returns the owner component.
	 * @return The owner component.
	 */
	public Component getOwner()
	{
		return this.owner;
	}
	
	/**
	 * Sets the owner component.
	 * @param owner The owner component.
	 */
	public void setOwner(Component owner)
	{
		this.owner = owner;
	}
	
	/**
	 * Puts a new server connector in the map supporting the given protocol.
	 * @param name The connector name.
	 * @param protocol The connector protocol.
	 * @return The added server.
	 */
	public Server put(String name, Protocol protocol)
	{
		return put(name, new DefaultServer(protocol, getOwner()));
	}

	/**
	 * Puts a new server connector in the map supporting the given protocol on the specified port.
	 * @param name The connector name.
	 * @param protocol The connector protocol.
    * @param port The listening port.
	 * @return The added server.
	 */
	public Server put(String name, Protocol protocol, int port)
	{
		return put(name, new DefaultServer(protocol, getOwner(), port));
	}

	/**
	 * Puts a new server connector in the map supporting the given protocol on the specified IP address and port.
	 * @param name The connector name.
	 * @param protocol The connector protocol.
    * @param address The optional listening IP address (useful if multiple IP addresses available).
    * @param port The listening port.
	 * @return The added server.
	 */
	public Server put(String name, Protocol protocol, String address, int port)
	{
		return put(name, new DefaultServer(protocol, getOwner(), address, port));
	}
}
