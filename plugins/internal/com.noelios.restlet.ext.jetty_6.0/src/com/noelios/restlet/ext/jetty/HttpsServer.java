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

import java.io.File;

import org.mortbay.jetty.security.SslSocketConnector;
import org.restlet.component.Component;
import org.restlet.data.ParameterList;
import org.restlet.data.Protocols;

/**
 * Jetty HTTPS server connector. Here is the list of additional parameters that are supported:
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
 * 	<tr>
 * 		<td>securityProvider</td>
 * 		<td>String</td>
 * 		<td>null (uses JDK's default provider, see javax.net.ssl.SSLContext)</td>
 * 		<td>Java security provider name (see java.security.Provider class).</td>
 * 	</tr>
 * </table>
 * @see <a href="http://jetty.mortbay.org/jetty/faq?s=400-Security&t=ssl">FAQ - Configuring SSL for Jetty</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpsServer extends JettyServer
{
   /**
    * Constructor.
    * @param owner The owner component.
    * @param parameters The initial parameters.
    * @param address The optional listening IP address (local host used if null).
    * @param port The listening port.
    */
   public HttpsServer(Component owner, ParameterList parameters, String address, int port)
   {
      super(owner, parameters, address, port);
      getProtocols().add(Protocols.HTTPS);
   }

   /** Start hook. */
   public void start() throws Exception
   {
   	if(!isStarted())
   	{
         // Create and configure the Jetty HTTP connector
   		SslSocketConnector connector = new SslSocketConnector();
         if(address != null) connector.setHost(this.address);
         connector.setPort(this.port);
         connector.setAlgorithm(getCertAlgorithm());
         connector.setKeyPassword(getKeyPassword());
         connector.setKeystore(getKeystorePath());
         connector.setKeystoreType(getKeystoreType());
         connector.setPassword(getKeystorePassword());
         connector.setProtocol(getSslProtocol());
         connector.setProvider(getSecurityProvider());
         
         this.wrappedServer.addConnector(connector);
   		super.start();
   	}
   }

   /**
    * Returns the SSL keystore path.
    * @return The SSL keystore path.
    */
   public String getKeystorePath()
   {
   	return getParameters().getFirstValue("keystorePath", System.getProperty("user.home") + File.separator + ".keystore");
   }

   /**
    * Returns the SSL keystore password.
    * @return The SSL keystore password.
    */
   public String getKeystorePassword()
   {
   	return getParameters().getFirstValue("keystorePassword", "");
   }

   /**
    * Returns the SSL keystore type.
    * @return The SSL keystore type.
    */
   public String getKeystoreType()
   {
   	return getParameters().getFirstValue("keystoreType", "JKS");
   }

   /**
    * Returns the SSL key password.
    * @return The SSL key password.
    */
   public String getKeyPassword()
   {
   	return getParameters().getFirstValue("keyPassword", "");
   }

   /**
    * Returns the SSL certificate algorithm.
    * @return The SSL certificate algorithm.
    */
   public String getCertAlgorithm()
   {
   	return getParameters().getFirstValue("certAlgorithm", "SunX509");
   }

   /**
    * Returns the SSL keystore type.
    * @return The SSL keystore type.
    */
   public String getSslProtocol()
   {
   	return getParameters().getFirstValue("sslProtocol", "TLS");
   }

   /**
    * Returns the Java security provider name.
    * @return The Java security provider name.
    */
   public String getSecurityProvider()
   {
   	return getParameters().getFirstValue("securityProvider", null);
   }

}
