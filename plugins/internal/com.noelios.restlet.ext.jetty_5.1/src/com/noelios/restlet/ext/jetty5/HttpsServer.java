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

import java.io.File;

import org.mortbay.util.InetAddrPort;
import org.restlet.Context;
import org.restlet.data.Protocol;

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
 * 		<td>lowResourcePersistTimeMs</td>
 * 		<td>int</td>
 * 		<td>2000</td>
 * 		<td>Time in ms that connections will persist if listener is low on resources.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>keystorePath</td>
 * 		<td>String</td>
 * 		<td>${user.home}/.keystore</td>
 * 		<td>The SSL keystore path.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>keystorePassword</td>
 * 		<td>String</td>
 * 		<td></td>
 * 		<td>The SSL keystore password.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>keyPassword</td>
 * 		<td>String</td>
 * 		<td></td>
 * 		<td>The SSL key password.</td>
 * 	</tr>
 * </table>
 * @see <a href="http://jetty.mortbay.org/jetty/faq?s=400-Security&t=ssl">FAQ - Configuring SSL for Jetty</a>
 * @author Jerome Louvel (contact@noelios.com) <a href="http://www.noelios.com/">Noelios Consulting</a>
 */
public class HttpsServer extends JettyServer
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

   /** Start hook. */
   public void start() throws Exception
   {
   	HttpsListener listener = null;
      if(getAddress() != null)
      {
         listener = new HttpsListener(this, new InetAddrPort(getAddress(), getPort()));
      }
      else
      {
      	listener = new HttpsListener(this);
      	listener.setPort(getPort());
      }
      
      // Configure the listener
      listener.setMinThreads(getMinThreads());
      listener.setMaxThreads(getMaxThreads());
      listener.setMaxIdleTimeMs(getMaxIdleTimeMs());
      listener.setLowResourcePersistTimeMs(getLowResourcePersistTimeMs());
      listener.setKeystore(getKeystorePath());
      listener.setPassword(getKeystorePassword());
      listener.setKeyPassword(getKeyPassword());
      
      setListener(listener);
      
      super.start();
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
    * Returns the SSL key password.
    * @return The SSL key password.
    */
   public String getKeyPassword()
   {
   	return getContext().getParameters().getFirstValue("keyPassword", "");
   }

   /**
    * Returns time in ms that connections will persist if listener is low on resources.
    * @return Time in ms that connections will persist if listener is low on resources.
    */
   public int getLowResourcePersistTimeMs()
   {
   	return Integer.parseInt(getContext().getParameters().getFirstValue("lowResourcePersistTimeMs", "2000"));
   }

}
