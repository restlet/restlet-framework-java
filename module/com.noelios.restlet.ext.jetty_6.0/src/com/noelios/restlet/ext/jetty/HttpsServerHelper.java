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

import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.restlet.Server;
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
 * 		<td>secureRandomAlgorithm</td>
 * 		<td>String</td>
 * 		<td>null (see java.security.SecureRandom)</td>
 * 		<td>Name of the RNG algorithm. (see java.security.SecureRandom class).</td>
 * 	</tr>
 * 	<tr>
 * 		<td>securityProvider</td>
 * 		<td>String</td>
 * 		<td>null (see javax.net.ssl.SSLContext)</td>
 * 		<td>Java security provider name (see java.security.Provider class).</td>
 * 	</tr>
 * 	<tr>
 * 		<td>needClientAuthentication</td>
 * 		<td>boolean</td>
 * 		<td>false</td>
 * 		<td>Indicates if we require client certificate authentication.</td>
 * 	</tr>
 * 	<tr>
 * 		<td>wantClientAuthentication</td>
 * 		<td>boolean</td>
 * 		<td>false</td>
 * 		<td>Indicates if we would like client certificate authentication.</td>
 * 	</tr>
 * </table>
 * @see <a href="http://jetty.mortbay.org/jetty/faq?s=400-Security&t=ssl">FAQ - Configuring SSL for Jetty</a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpsServerHelper extends JettyServerHelper
{
	/**
	 * Constructor.
	 * @param server The server to help.
	 */
	public HttpsServerHelper(Server server)
	{
		super(server);
		getSupportedProtocols().add(Protocol.HTTPS);
	}

	/**
	 * Creates a new internal Jetty connector.
	 * @return A new internal Jetty connector.
	 */
	protected AbstractConnector createConnector()
	{
		// Create and configure the Jetty HTTP connector
		SslSocketConnector result = new SslSocketConnector();

		// Continue configuration with HTTPS specific parameters
		result.setKeyPassword(getKeyPassword());
		result.setKeystore(getKeystorePath());
		result.setKeystoreType(getKeystoreType());
		result.setNeedClientAuth(isNeedClientAuthentication());
		result.setPassword(getKeystorePassword());
		result.setProtocol(getSslProtocol());
		result.setProvider(getSecurityProvider());
		result.setSecureRandomAlgorithm(getSecureRandomAlgorithm());
		result.setSslKeyManagerFactoryAlgorithm(getCertAlgorithm());
		result.setSslTrustManagerFactoryAlgorithm(getCertAlgorithm());
		result.setTrustPassword(getKeystorePassword());
		result.setWantClientAuth(isWantClientAuthentication());

		return result;
	}

	/**
	 * Returns the SSL keystore path.
	 * @return The SSL keystore path.
	 */
	public String getKeystorePath()
	{
		return getParameters().getFirstValue("keystorePath",
				System.getProperty("user.home") + File.separator + ".keystore");
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
	 * Returns the name of the RNG algorithm.
	 * @return The name of the RNG algorithm.
	 */
	public String getSecureRandomAlgorithm()
	{
		return getParameters().getFirstValue("secureRandomAlgorithm", null);
	}

	/**
	 * Returns the Java security provider name.
	 * @return The Java security provider name.
	 */
	public String getSecurityProvider()
	{
		return getParameters().getFirstValue("securityProvider", null);
	}

	/**
	 * Indicates if we require client certificate authentication.
	 * @return True if we require client certificate authentication.
	 */
	public boolean isNeedClientAuthentication()
	{
		return Boolean.parseBoolean(getParameters().getFirstValue(
				"needClientAuthentication", "false"));
	}

	/**
	 * Indicates if we would like client certificate authentication.
	 * @return True if we would like client certificate authentication.
	 */
	public boolean isWantClientAuthentication()
	{
		return Boolean.parseBoolean(getParameters().getFirstValue(
				"wantClientAuthentication", "false"));
	}

}
