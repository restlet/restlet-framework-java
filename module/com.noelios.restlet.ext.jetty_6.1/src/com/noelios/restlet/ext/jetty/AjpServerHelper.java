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

package com.noelios.restlet.ext.jetty;

import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.ajp.Ajp13SocketConnector;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Jetty AJP server connector.
 * @see <a href="http://jetty.mortbay.org/jetty6/">Jetty home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class AjpServerHelper extends JettyServerHelper
{
	/**
	 * Constructor.
	 * @param server The server to help.
	 */
	public AjpServerHelper(Server server)
	{
		super(server);
		getProtocols().add(Protocol.AJP);
	}

	/**
	 * Creates a new internal Jetty connector.
	 * @return A new internal Jetty connector.
	 */
	protected AbstractConnector createConnector()
	{
		return new Ajp13SocketConnector();
	}

}
