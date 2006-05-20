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

import java.io.FileInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.restlet.connector.AbstractServer;
import org.restlet.connector.Server;
import org.restlet.data.Protocol;
import org.restlet.data.Protocols;

import simple.http.BufferedPipelineFactory;
import simple.http.ProtocolHandler;
import simple.http.Request;
import simple.http.Response;
import simple.http.connect.Connection;
import simple.http.connect.ConnectionFactory;

/**
 * Simple HTTP(S) server connector.
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://semagia.com/">Semagia</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com">Noelios Consulting</a>
 */
public class SimpleServer extends AbstractServer implements ProtocolHandler
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
	 * Simple connection.
	 */
	protected Connection connection;

   /**
    * Constructor.
    * @param protocol The connector protocol.
    * @param name The unique connector name.
    * @param delegate The delegate Server.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public SimpleServer(Protocol protocol, String name, Server delegate, String address, int port)
   {
		super(protocol, name, delegate, address, port);
	}

	/**
	 * Returns the supported protocols. This method is called by the {@link com.noelios.restlet.impl.FactoryImpl} 
	 * to determine the supported protocols.
	 * @return A list of supported protocols.
	 */
	public static List<Protocol> getProtocols()
	{
		return Arrays.asList(new Protocol[]{Protocols.HTTP, Protocols.HTTPS});
	}

   /**
    * Returns the description of this REST element.
    * @return The description of this REST element.
    */
	public String getDescription()
	{
		return "Simple " + super.protocol.getName() + " server";
	}

   /** Starts the Restlet. */
	public void start() throws Exception
	{
		if(!isStarted())
		{
			if (Protocols.HTTP.equals(super.protocol))
			{
				socket = new ServerSocket(port);
			}
			else if (Protocols.HTTPS.equals(super.protocol))
			{
				KeyStore keyStore = KeyStore.getInstance("JKS");
				keyStore.load(new FileInputStream(keystorePath), keystorePassword
						.toCharArray());
				KeyManagerFactory keyManagerFactory = KeyManagerFactory
						.getInstance("SunX509");
				keyManagerFactory.init(keyStore, keyPassword.toCharArray());
				SSLContext sslContext = SSLContext.getInstance("TLS");
				sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
				socket = sslContext.getServerSocketFactory().createServerSocket(port);
				socket.setSoTimeout(60000);
			}
			else
			{
				// Should never happen.
				throw new RuntimeException("Unsupported protocol: " + super.protocol);
			}
	
			this.confidential = Protocols.HTTPS.equals(getProtocol());
			this.connection = ConnectionFactory.getConnection(this, new BufferedPipelineFactory());
			this.connection.connect(socket);
			super.start();
		}
	}

   /** Stops the Restlet. */
	public void stop() throws Exception
	{
		if(isStarted())
		{
			socket.close();
			socket = null;
			connection = null;
			
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
			handle(new SimpleCall(request, response, this.confidential));
			response.getOutputStream().close();
		}
		catch (IOException ioe)
		{
			logger.log(Level.WARNING, "Error while handling a SimpleServer request", ioe);
		}
	}

}
