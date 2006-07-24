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

import java.io.IOException;

import javax.servlet.ServletException;

import org.mortbay.jetty.HttpConnection;
import org.mortbay.jetty.Server;
import org.restlet.component.Component;
import org.restlet.data.ParameterList;

/**
 * Abstract Jetty Web server connector. Here is the list of parameters that are supported:
 * <table>
 * 	<tr>
 * 		<th>Parameter name</th>
 * 		<th>Value type</th>
 * 		<th>Default value</th>
 * 		<th>Description</th>
 * 	</tr>
 * 	<tr>
 * 		<td>minThreads</td>
 * 		<td>int</td>
 * 		<td>2</td>
 * 		<td>Minumum threads waiting to service requests.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>maxThread</td>
 * 		<td>int</td>
 * 		<td>256</td>
 * 		<td>Maximum threads that will service requests.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>maxIdleTimeMs</td>
 * 		<td>int</td>
 * 		<td>10000</td>
 * 		<td>Time for an idle thread to wait for a request or read.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>lowResourcePersistTimeMs</td>
 * 		<td>int</td>
 * 		<td>2000</td>
 * 		<td>Time in ms that connections will persist if listener is low on resources.</td>
 * 	</tr>
 * </table>
 * @see <a href="http://jetty.mortbay.org/jetty6/">Jetty home page</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public abstract class JettyServer extends com.noelios.restlet.impl.HttpServer
{
	/**
	 * The wrapped Jetty server.
	 */
	protected Server wrappedServer;
	
   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public JettyServer(Component owner, ParameterList parameters, String address, int port)
   {
   	super(owner, parameters, address, port);
   	this.wrappedServer = new WrappedServer(this);
   }

   /** Starts the Connector. */
   public void start() throws Exception
   {
   	if(!isStarted())
   	{
   		this.wrappedServer.start();
			super.start();
   	}
   }

   /** Stops the Connector. */
   public void stop() throws Exception
   {
   	if(isStarted())
   	{
   		this.wrappedServer.stop();
   		super.stop();
   	}
   }

   /**
    * Jetty server wrapped by a parent Restlet HTTP server connector.
    * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
    */
	private class WrappedServer extends org.mortbay.jetty.Server
	{
		JettyServer server;

		/**
		 * Constructor.
		 * @param server The Jetty HTTP server.
		 */
		public WrappedServer(JettyServer server)
		{
			this.server = server;
		}
		
		/**
		 * Handler method converting a Jetty Connection into a Restlet Call.
		 * @param connection The connection to handle.
		 */
	   public void handle(HttpConnection connection) throws IOException, ServletException
	   {
	      server.handle(new JettyCall(connection));
	   }

	};

}
