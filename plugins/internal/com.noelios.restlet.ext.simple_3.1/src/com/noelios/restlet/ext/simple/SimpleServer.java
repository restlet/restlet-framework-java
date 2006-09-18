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

package com.noelios.restlet.ext.simple;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import simple.http.PipelineHandler;
import simple.http.ProtocolHandler;
import simple.http.Request;
import simple.http.Response;
import simple.http.connect.Connection;

import com.noelios.restlet.impl.connector.HttpServer;

/**
 * Abstract Simple Web server connector. Here is the list of parameters that are supported:
 * <table>
 * 	<tr>
 * 		<th>Parameter name</th>
 * 		<th>Value type</th>
 * 		<th>Default value</th>
 * 		<th>Description</th>
 * 	</tr>
 * 	<tr>
 * 		<td>defaultThreads</td>
 * 		<td>int</td>
 * 		<td>20</td>
 * 		<td>Default number of polling threads for a handler object.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>maxWaitTimeMs</td>
 * 		<td>int</td>
 * 		<td>200</td>
 * 		<td>Maximum waiting time between polls of the input.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>useForwardedForHeader</td>
 * 		<td>boolean</td>
 * 		<td>false</td>
 * 		<td>True if the "X-Forwarded-For" HTTP header should be used to get client addresses.</td>
 * 	</tr>
 * </table>
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://semagia.com/">Semagia</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com">Noelios Consulting</a>
 */
public abstract class SimpleServer extends HttpServer implements ProtocolHandler
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger(SimpleServer.class.getCanonicalName());

   /**
	 * Indicates if this service is acting in HTTP or HTTPS mode.
	 */
	private boolean confidential;

	/**
	 * Server socket this server is listening to.
	 */
	private ServerSocket socket;

	/**
	 * Simple pipeline handler.
	 */
	private PipelineHandler handler;
	
	/**
	 * Simple connection.
	 */
	private Connection connection;

   /**
    * Constructor.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public SimpleServer(String address, int port)
   {
      super(address, port);
   }

   /** Stops the Restlet. */
	public void stop() throws Exception
	{
		if(isStarted())
		{
			getSocket().close();
			setSocket(null);
			this.setHandler(null);
			this.setConnection(null);
			
			// For further information on how to shutdown a Simple
			// server, see http://sourceforge.net/mailarchive/forum.php?thread_id=10138257&forum_id=38791
			// There seems to be place for improvement in this method.

			super.stop();
		}
	}

	/**
	 * Handles a Simple request/response transaction.
	 * @param request The Simple request.
	 * @param response The Simple response.
	 */
	public void handle(Request request, Response response)
	{
		handle(new SimpleCall(request, response, this.isConfidential(), getPort()));
		
		try
		{
			response.getOutputStream().close();
		}
		catch (IOException ioe)
		{
         logger.log(Level.WARNING, "Exception while closing the Simple response's output stream", ioe);
		}
	}

   /**
    * Returns the default number of polling threads for a handler object.
    * @return The default number of polling threads for a handler object.
    */
   public int getDefaultThreads()
   {
   	return Integer.parseInt(getContext().getParameters().getFirstValue("defaultThreads", "20"));
   }

   /**
    * Returns the maximum waiting time between polls of the input.
    * @return The maximum waiting time between polls of the input.
    */
   public int getMaxWaitTimeMs()
   {
   	return Integer.parseInt(getContext().getParameters().getFirstValue("maxWaitTimeMs", "200"));
   }

	/**
	 * Sets the server socket this server is listening to.
	 * @param socket The server socket this server is listening to.
	 */
	protected void setSocket(ServerSocket socket)
	{
		this.socket = socket;
	}

	/**
	 * Returns the server socket this server is listening to.
	 * @return The server socket this server is listening to.
	 */
	protected ServerSocket getSocket()
	{
		return socket;
	}

	/**
	 * Sets the Simple pipeline handler.
	 * @param handler The Simple pipeline handler.
	 */
	protected void setHandler(PipelineHandler handler)
	{
		this.handler = handler;
	}

	/**
	 * Returns the Simple pipeline handler.
	 * @return The Simple pipeline handler.
	 */
	protected PipelineHandler getHandler()
	{
		return handler;
	}

	/**
	 * Sets the Simple connection.
	 * @param connection The Simple connection.
	 */
	protected void setConnection(Connection connection)
	{
		this.connection = connection;
	}

	/**
	 * Returns the Simple connection.
	 * @return The Simple connection.
	 */
	protected Connection getConnection()
	{
		return connection;
	}

	/**
	 * Indicates if this service is acting in HTTP or HTTPS mode.
	 * @param confidential True if this service is acting in HTTP or HTTPS mode.
	 */
	protected void setConfidential(boolean confidential)
	{
		this.confidential = confidential;
	}

	/**
	 * Indicates if this service is acting in HTTP or HTTPS mode.
	 * @return True if this service is acting in HTTP or HTTPS mode.
	 */
	protected boolean isConfidential()
	{
		return confidential;
	}

}
