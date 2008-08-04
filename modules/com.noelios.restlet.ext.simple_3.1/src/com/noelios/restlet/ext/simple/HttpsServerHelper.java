/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). 
 * You can select the license that you prefer but you may not use this file 
 * except in compliance with one of these Licenses.
 *
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.gnu.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.gnu.org/licenses/lgpl-2.1.html
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.sun.com/cddl/cddl.html
 * 
 * See the Licenses for the specific language governing permissions and 
 * limitations under the Licenses. 
 *
 * Alternatively, you can obtain a royaltee free commercial license with 
 * less limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 *
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.simple;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;

import org.restlet.Server;
import org.restlet.data.Protocol;

import simple.http.PipelineHandlerFactory;
import simple.http.connect.ConnectionFactory;

/**
 * Simple HTTP server connector. Here is the list of additional parameters that
 * are supported: <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>keystorePath</td>
 * <td>String</td>
 * <td>${user.home}/.keystore</td>
 * <td>SSL keystore path.</td>
 * </tr>
 * <tr>
 * <td>keystorePassword</td>
 * <td>String</td>
 * <td></td>
 * <td>SSL keystore password.</td>
 * </tr>
 * <tr>
 * <td>keystoreType</td>
 * <td>String</td>
 * <td>JKS</td>
 * <td>SSL keystore type</td>
 * </tr>
 * <tr>
 * <td>keyPassword</td>
 * <td>String</td>
 * <td></td>
 * <td>SSL key password.</td>
 * </tr>
 * <tr>
 * <td>certAlgorithm</td>
 * <td>String</td>
 * <td>SunX509</td>
 * <td>SSL certificate algorithm.</td>
 * </tr>
 * <tr>
 * <td>sslProtocol</td>
 * <td>String</td>
 * <td>TLS</td>
 * <td>SSL protocol.</td>
 * </tr>
 * </table>
 * 
 * @author Lars Heuer (heuer[at]semagia.com)
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpsServerHelper extends SimpleServerHelper {
	/**
	 * Constructor.
	 * 
	 * @param server
	 *            The server to help.
	 */
	public HttpsServerHelper(Server server) {
		super(server);
		getProtocols().add(Protocol.HTTPS);
	}

	/** Starts the Restlet. */
	@Override
	public void start() throws Exception {
		// Initialize the SSL context
		KeyStore keyStore = KeyStore.getInstance(getKeystoreType());
		FileInputStream fis = getKeystorePath() == null ? null
				: new FileInputStream(getKeystorePath());
		char[] password = getKeystorePassword() == null ? null
				: getKeystorePassword().toCharArray();
		keyStore.load(fis, password);
		KeyManagerFactory keyManagerFactory = KeyManagerFactory
				.getInstance(getCertAlgorithm());
		keyManagerFactory.init(keyStore, getKeyPassword().toCharArray());
		SSLContext sslContext = SSLContext.getInstance(getSslProtocol());
		sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

		// Initialize the socket
		SSLServerSocket serverSocket = null;
		String addr = getServer().getAddress();
		if (addr != null) {
			// this call may throw UnknownHostException and otherwise always
			// returns an instance of INetAddress
			// Note: textual representation of inet addresses are supported
			InetAddress iaddr = InetAddress.getByName(addr);
			// Note: the backlog of 50 is the default
			serverSocket = (SSLServerSocket) sslContext
					.getServerSocketFactory().createServerSocket(
							getServer().getPort(), 50, iaddr);
		} else {
			serverSocket = (SSLServerSocket) sslContext
					.getServerSocketFactory().createServerSocket(
							getServer().getPort());
		}

		serverSocket.setSoTimeout(60000);
		setSocket(serverSocket);
		fis.close();

		// Complete initialization
		setConfidential(true);
		setHandler(PipelineHandlerFactory.getInstance(
				new SimpleProtocolHandler(this), getDefaultThreads(),
				getMaxWaitTimeMs()));
		setConnection(ConnectionFactory.getConnection(getHandler(),
				new SimplePipelineFactory()));
		getConnection().connect(getSocket());
		super.start();
	}

	/**
	 * Returns the SSL keystore path.
	 * 
	 * @return The SSL keystore path.
	 */
	public String getKeystorePath() {
		return getParameters().getFirstValue("keystorePath",
				System.getProperty("user.home") + File.separator + ".keystore");
	}

	/**
	 * Returns the SSL keystore password.
	 * 
	 * @return The SSL keystore password.
	 */
	public String getKeystorePassword() {
		return getParameters().getFirstValue("keystorePassword", "");
	}

	/**
	 * Returns the SSL keystore type.
	 * 
	 * @return The SSL keystore type.
	 */
	public String getKeystoreType() {
		return getParameters().getFirstValue("keystoreType", "JKS");
	}

	/**
	 * Returns the SSL key password.
	 * 
	 * @return The SSL key password.
	 */
	public String getKeyPassword() {
		return getParameters().getFirstValue("keyPassword", "");
	}

	/**
	 * Returns the SSL certificate algorithm.
	 * 
	 * @return The SSL certificate algorithm.
	 */
	public String getCertAlgorithm() {
		return getParameters().getFirstValue("certAlgorithm", "SunX509");
	}

	/**
	 * Returns the SSL keystore type.
	 * 
	 * @return The SSL keystore type.
	 */
	public String getSslProtocol() {
		return getParameters().getFirstValue("sslProtocol", "TLS");
	}

}
