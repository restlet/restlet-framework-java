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

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.restlet.Context;
import org.restlet.data.Protocol;

import simple.http.BufferedPipelineFactory;
import simple.http.PipelineHandlerFactory;
import simple.http.connect.ConnectionFactory;

/**
 * Simple HTTP server connector. Here is the list of additional parameters that are supported:
 * <table>
 * 	<tr>
 * 		<th>Parameter name</th>
 * 		<th>Value type</th>
 * 		<th>Default value</th>
 * 		<th>Description</th>
 * 	</tr>
 * 	<tr>
 * 		<td>keystorePath</td>
 * 		<td>String</td>
 * 		<td>${user.home}/.keystore</td>
 * 		<td>SSL keystore path.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>keystorePassword</td>
 * 		<td>String</td>
 * 		<td></td>
 * 		<td>SSL keystore password.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>keystoreType</td>
 * 		<td>String</td>
 * 		<td>JKS</td>
 * 		<td>SSL keystore type</td>
 * 	</tr>
 * 	<tr>
 * 		<td>keyPassword</td>
 * 		<td>String</td>
 * 		<td></td>
 * 		<td>SSL key password.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>certAlgorithm</td>
 * 		<td>String</td>
 * 		<td>SunX509</td>
 * 		<td>SSL certificate algorithm.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>sslProtocol</td>
 * 		<td>String</td>
 * 		<td>TLS</td>
 * 		<td>SSL protocol.</td>
 * 	</tr>
 * </table>
 * @author Lars Heuer (heuer[at]semagia.com) <a href="http://semagia.com/">Semagia</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com">Noelios Consulting</a>
 */
public class HttpsServer extends SimpleServer
{
	/**
	 * Constructor.
    * @param context The context.
	 * @param address The optional listening IP address (local host used if null).
	 * @param port The listening port.
	 */
	public HttpsServer(Context context, String address, int port)
	{
		super(context, address, port);
		getProtocols().add(Protocol.HTTPS);
	}

	/** Starts the Restlet. */
	public void start() throws Exception
	{
		if (!isStarted())
		{
			// Initialize the SSL context
			KeyStore keyStore = KeyStore.getInstance(getKeystoreType());
			keyStore.load(new FileInputStream(getKeystorePath()), getKeystorePassword().toCharArray());
			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(getCertAlgorithm());
			keyManagerFactory.init(keyStore, getKeyPassword().toCharArray());
			SSLContext sslContext = SSLContext.getInstance(getSslProtocol());
			sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
			setSocket(sslContext.getServerSocketFactory().createServerSocket(getPort()));
			getSocket().setSoTimeout(60000);

			// Complete initialization
			setConfidential(true);
			setHandler(PipelineHandlerFactory.getInstance(this, getDefaultThreads(), getMaxWaitTimeMs()));
			setConnection(ConnectionFactory.getConnection(getHandler(), new BufferedPipelineFactory()));
			getConnection().connect(getSocket());
			super.start();
		}
	}

   /**
    * Returns the SSL keystore path.
    * @return The SSL keystore path.
    */
   public String getKeystorePath()
   {
   	return getContext().getParameters().getFirstValue("keystorePath", System.getProperty("user.home") + File.separator + ".keystore");
   }

   /**
    * Returns the SSL keystore password.
    * @return The SSL keystore password.
    */
   public String getKeystorePassword()
   {
   	return getContext().getParameters().getFirstValue("keystorePassword", "");
   }

   /**
    * Returns the SSL keystore type.
    * @return The SSL keystore type.
    */
   public String getKeystoreType()
   {
   	return getContext().getParameters().getFirstValue("keystoreType", "JKS");
   }

   /**
    * Returns the SSL key password.
    * @return The SSL key password.
    */
   public String getKeyPassword()
   {
   	return getContext().getParameters().getFirstValue("keyPassword", "");
   }

   /**
    * Returns the SSL certificate algorithm.
    * @return The SSL certificate algorithm.
    */
   public String getCertAlgorithm()
   {
   	return getContext().getParameters().getFirstValue("certAlgorithm", "SunX509");
   }

   /**
    * Returns the SSL keystore type.
    * @return The SSL keystore type.
    */
   public String getSslProtocol()
   {
   	return getContext().getParameters().getFirstValue("sslProtocol", "TLS");
   }

}
