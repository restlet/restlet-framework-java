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

import org.restlet.component.Component;
import org.restlet.data.ParameterList;

import simple.http.PipelineHandler;
import simple.http.ProtocolHandler;
import simple.http.Request;
import simple.http.Response;
import simple.http.connect.Connection;

import com.noelios.restlet.impl.HttpServer;

/**
 * Abstract Simple Web server connector.
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://semagia.com/">Semagia</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com">Noelios Consulting</a>
 */
public abstract class SimpleServer extends HttpServer implements ProtocolHandler
{
   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.ext.simple.SimpleServer");

	/**
	 * Indicates if this service is acting in HTTP or HTTPS mode.
	 */
	protected boolean confidential;

	/**
	 * Server socket this server is listening to.
	 */
	protected ServerSocket socket;

	/**
	 * Simple pipeline handler.
	 */
	protected PipelineHandler handler;
	
	/**
	 * Simple connection.
	 */
	protected Connection connection;

   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public SimpleServer(Component owner, ParameterList parameters, String address, int port)
   {
      super(owner, parameters, address, port);
   }

   /** Stops the Restlet. */
	public void stop() throws Exception
	{
		if(isStarted())
		{
			this.socket.close();
			this.socket = null;
			this.handler = null;
			this.connection = null;
			
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
		try
		{
			handle(new SimpleCall(request, response, this.confidential, this.port));
			response.getOutputStream().close();
		}
		catch (IOException ioe)
		{
			logger.log(Level.WARNING, "Error while handling a SimpleServer request", ioe);
		}
	}

}
