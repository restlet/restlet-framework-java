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

package com.noelios.restlet.ext.jetty5;

import org.mortbay.util.InetAddrPort;
import org.restlet.Server;
import org.restlet.data.Protocol;

/**
 * Jetty HTTP server connector. Here is the list of additional parameters that are supported:
 * <table>
 * 	<tr>
 * 		<td>lowResourcePersistTimeMs</td>
 * 		<td>int</td>
 * 		<td>2000</td>
 * 		<td>Time in ms that connections will persist if listener is low on resources.</td>
 * 	</tr>
 * </table>
 * @see <a href="http://jetty.mortbay.com/">Jetty home page</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpServerHelper extends JettyServerHelper
{
	/**
	 * Constructor.
	 * @param server The server to help.
	 */
	public HttpServerHelper(Server server)
	{
		super(server);
		getSupportedProtocols().add(Protocol.HTTP);
	}

	/** Start hook. */
	public void start() throws Exception
	{
		HttpListener listener;

		if (getServer().getAddress() != null)
		{
			listener = new HttpListener(this, new InetAddrPort(getServer().getAddress(),
					getServer().getPort()));
		}
		else
		{
			listener = new HttpListener(this);
			listener.setPort(getServer().getPort());
		}

		// Configure the listener
		listener.setMinThreads(getMinThreads());
		listener.setMaxThreads(getMaxThreads());
		listener.setMaxIdleTimeMs(getMaxIdleTimeMs());
		listener.setLowResourcePersistTimeMs(getLowResourcePersistTimeMs());

		setListener(listener);
		super.start();
	}

	/**
	 * Returns time in ms that connections will persist if listener is low on resources.
	 * @return Time in ms that connections will persist if listener is low on resources.
	 */
	public int getLowResourcePersistTimeMs()
	{
		return Integer.parseInt(getParameters().getFirstValue("lowResourcePersistTimeMs",
				"2000"));
	}

}
