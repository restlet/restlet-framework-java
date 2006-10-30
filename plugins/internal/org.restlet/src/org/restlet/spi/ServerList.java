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

package org.restlet.spi;

import java.util.List;

import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Modifiable list of server connectors.
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public interface ServerList extends List<Server>
{
	/**
	 * Adds a new server connector in the map supporting the given protocol.
	 * @param protocol The connector protocol.
	 * @return The added server.
	 */
	public Server add(Protocol protocol);

	/**
	 * Adds a new server connector in the map supporting the given protocol on the specified port.
	 * @param protocol The connector protocol.
	 * @param port The listening port.
	 * @return The added server.
	 */
	public Server add(Protocol protocol, int port);

	/**
	 * Adds a new server connector in the map supporting the given protocol on the specified IP address and port.
	 * @param protocol The connector protocol.
	 * @param address The optional listening IP address (useful if multiple IP addresses available).
	 * @param port The listening port.
	 * @return The added server.
	 */
	public Server add(Protocol protocol, String address, int port);
}
