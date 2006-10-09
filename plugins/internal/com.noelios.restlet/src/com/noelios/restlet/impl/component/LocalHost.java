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

import org.restlet.VirtualHost;
import org.restlet.component.Container;
import org.restlet.data.Protocol;

/**
 * Virtual host allowing all requests to localhost names or addresses.  
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class LocalHost extends VirtualHost
{
	/**
	 * Constructor.
	 * @param context The context.
	 */
	public LocalHost(Container container)
	{
		super(container);

		// Add allowed IP addresses
		getAllowedAddresses().add("127.0.0.1");
		getAllowedAddresses().add(getLocalHostAddress());

		// Add allowed domain names
		getAllowedNames().add("localhost");
		getAllowedNames().add("127.0.0.1");
		getAllowedNames().add(getLocalHostName());

		// Add allowed port numbers (all by default)
		getAllowedPorts().add(ALL_PORTS);

		// Add allowed protocols (all by default)
		getAllowedProtocols().add(Protocol.ALL);
	}
}
