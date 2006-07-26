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

import org.mortbay.jetty.Connector;
import org.mortbay.jetty.bio.SocketConnector;
import org.mortbay.jetty.nio.BlockingChannelConnector;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.restlet.component.Component;
import org.restlet.data.ParameterList;
import org.restlet.data.Protocols;

/**
 * Jetty HTTP server connector. Here is the list of additional parameters that are supported:
 * <table>
 * 	<tr>
 * 		<th>Parameter name</th>
 * 		<th>Value type</th>
 * 		<th>Default value</th>
 * 		<th>Description</th>
 * 	</tr>
 * 	<tr>
 * 		<td>type</td>
 * 		<td>int</td>
 * 		<td>1</td>
 * 		<td>The type of Jetty connector to use.
 * 			<ul>
 * 				<li>1 : Selecting NIO connector (Jetty's SelectChannelConnector class).</li>
 * 				<li>2 : Blocking NIO connector (Jetty's BlockingChannelConnector class).</li>
 * 				<li>3 : Blocking BIO connector (Jetty's SocketConnector class).</li>
 * 			</ul>
 * 		</td>
 * 	</tr>
 * </table>
 * @see <a href="http://jetty.mortbay.org/jetty6/">Jetty home page</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpServer extends JettyServer
{
   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public HttpServer(Component owner, ParameterList parameters, String address, int port)
   {
   	super(owner, parameters, address, port);
   	getProtocols().add(Protocols.HTTP);
   }

   /** Starts the Restlet. */
   public void start() throws Exception
   {
   	if(!isStarted())
   	{
         // Create and configure the Jetty HTTP connector
   		Connector connector = null;
   		
   		switch(getType())
   		{
   			case 1:
					// Selecting NIO connector
   				connector = new SelectChannelConnector(); 
				break;
   			case 2:
					// Blocking NIO connector
   				connector = new BlockingChannelConnector();
				break;
   			case 3:
   				// Blocking BIO connector
   				connector = new SocketConnector();
				break;
   		}
   		
         if(address != null) connector.setHost(this.address);
         connector.setPort(this.port);
         this.wrappedServer.addConnector(connector);
   		super.start();
   	}
   }

   /**
    * Returns the type of Jetty connector to use.
    * @return The type of Jetty connector to use.
    */
   public int getType()
   {
   	return Integer.parseInt(getParameters().getFirstValue("type", "1"));
   }

}
