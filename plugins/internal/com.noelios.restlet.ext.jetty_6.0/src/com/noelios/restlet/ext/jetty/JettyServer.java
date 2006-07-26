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
import org.mortbay.thread.BoundedThreadPool;
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
 * 		<td>1</td>
 * 		<td>Minumum threads waiting to service requests.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>maxThread</td>
 * 		<td>int</td>
 * 		<td>255</td>
 * 		<td>Maximum threads that will service requests.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>maxIdleTimeMs</td>
 * 		<td>int</td>
 * 		<td>30000</td>
 * 		<td>Time for an idle thread to wait for a request or read.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>lowThreads</td>
 * 		<td>int</td>
 * 		<td>25</td>
 * 		<td>Threshold of remaining threads at which the server is considered as running low on resources.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>lowResourceMaxIdleTimeMs</td>
 * 		<td>int</td>
 * 		<td>2500</td>
 * 		<td>Time in ms that connections will persist if listener is low on resources.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>acceptorThreads</td>
 * 		<td>int</td>
 * 		<td>1</td>
 * 		<td>Number of acceptor threads to set.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>acceptQueueSize</td>
 * 		<td>int</td>
 * 		<td>0</td>
 * 		<td>Size of the accept queue.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>headerBufferSize</td>
 * 		<td>int</td>
 * 		<td>4*1024</td>
 * 		<td>Size of the buffer to be used for request and response headers.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>requestBufferSize</td>
 * 		<td>int</td>
 * 		<td>8*1024</td>
 * 		<td>Size of the content buffer for receiving requests.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>responseBufferSize</td>
 * 		<td>int</td>
 * 		<td>32*1024</td>
 * 		<td>Size of the content buffer for sending responses.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>soLingerTime</td>
 * 		<td>int</td>
 * 		<td>1000</td>
 * 		<td>SO linger time (see Jetty 6 documentation).</td>
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

   	// Configuring the thread pool
      BoundedThreadPool btp=new BoundedThreadPool();
      btp.setLowThreads(getLowThreads());
      btp.setMaxIdleTimeMs(getMaxIdleTimeMs());
      btp.setMaxThreads(getMaxThreads());
      btp.setMinThreads(getMinThreads());
      this.wrappedServer.setThreadPool(btp);
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

   /**
    * Returns the minumum threads waiting to service requests.
    * @return The minumum threads waiting to service requests.
    */
   public int getMinThreads()
   {
   	return Integer.parseInt(getParameters().getFirstValue("minThreads", "1"));
   }

   /**
    * Returns the maximum threads that will service requests.
    * @return The maximum threads that will service requests.
    */
   public int getMaxThreads()
   {
   	return Integer.parseInt(getParameters().getFirstValue("maxThreads", "255"));
   }

   /**
    * Returns the threshold of remaining threads at which the server is considered as running low on resources.
    * @return The threshold of remaining threads at which the server is considered as running low on resources.
    */
   public int getLowThreads()
   {
   	return Integer.parseInt(getParameters().getFirstValue("lowThreads", "25"));
   }

   /**
    * Returns the time in ms that connections will persist if listener is low on resources.
    * @return The time in ms that connections will persist if listener is low on resources.
    */
   public int getLowResourceMaxIdleTimeMs()
   {
   	return Integer.parseInt(getParameters().getFirstValue("lowResourceMaxIdleTimeMs", "2500"));
   }

   /**
    * Returns the time for an idle thread to wait for a request or read.
    * @return The time for an idle thread to wait for a request or read.
    */
   public int getMaxIdleTimeMs()
   {
   	return Integer.parseInt(getParameters().getFirstValue("maxIdleTimeMs", "10000"));
   }

   /**
    * Returns the number of acceptor threads to set. 
    * @return The number of acceptor threads to set.
    */
   public int getAcceptorThreads()
   {
   	return Integer.parseInt(getParameters().getFirstValue("acceptorThreads", "1"));
   }

   /**
    * Returns the size of the accept queue.
    * @return The size of the accept queue.
    */
   public int getAcceptQueueSize()
   {
   	return Integer.parseInt(getParameters().getFirstValue("acceptQueueSize", "0"));
   }

   /**
    * Returns the size of the buffer to be used for request and response headers.
    * @return The size of the buffer to be used for request and response headers.
    */
   public int getHeaderBufferSize()
   {
   	return Integer.parseInt(getParameters().getFirstValue("headerBufferSize", Integer.toString(4*1024)));
   }

   /**
    * Returns the size of the content buffer for receiving requests.
    * @return The size of the content buffer for receiving requests.
    */
   public int getRequestBufferSize()
   {
   	return Integer.parseInt(getParameters().getFirstValue("requestBufferSize", Integer.toString(8*1024)));
   }

   /**
    * Returns the size of the content buffer for sending responses.
    * @return The size of the content buffer for sending responses.
    */
   public int getResponseBufferSize()
   {
   	return Integer.parseInt(getParameters().getFirstValue("responseBufferSize", Integer.toString(32*1024)));
   }

   /**
    * Returns the SO linger time (see Jetty 6 documentation).
    * @return The SO linger time (see Jetty 6 documentation).
    */
   public int getSoLingerTime()
   {
   	return Integer.parseInt(getParameters().getFirstValue("soLingerTime", "1000"));
   }
  
}
