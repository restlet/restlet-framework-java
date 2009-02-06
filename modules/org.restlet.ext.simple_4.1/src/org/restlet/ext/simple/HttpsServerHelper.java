/**
 * Copyright 2005-2009 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the following open
 * source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
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
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.ext.simple;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.engine.http.HttpsUtils;
import org.restlet.engine.security.SslContextFactory;
import org.simpleframework.transport.connect.Connection;
import org.simpleframework.transport.connect.SocketConnection;
import org.simpleframework.http.core.Container;
import org.simpleframework.http.core.ContainerServer;

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
 * <td>sslContextFactory</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Let you specify a {@link SslContextFactory} instance for a more complete
 * and flexible SSL context setting. If this parameter is set, it takes
 * precedance over the other SSL parameters below.</td>
 * </tr>
 * <tr>
 * <td>sslContextFactory</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Let you specify a {@link SslContextFactory} class name as a parameter,
 * or an instance as an attribute for a more complete and flexible SSL context
 * setting. If set, it takes precedance over the other SSL parameters below.</td>
 * </tr>
 * <tr>
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
 * <td>enabledCipherSuites</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Whitespace-separated list of enabled cipher suites and/or can be specified multiple times.</td>
 * </tr>
 * <tr>
 * <td>disabledCipherSuites</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Whitespace-separated list of disabled cipher suites and/or can be specified multiple times.
 * It affects the cipher suites manually enabled or the default ones.</td>
 * </tr>
 * <tr>
 * <td>needClientAuthentication</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if we require client certificate authentication.</td>
 * </tr>
 * <tr>
 * <td>sslProtocol</td>
 * <td>String</td>
 * <td>TLS</td>
 * <td>SSL protocol.</td>
 * </tr>
 * <tr>
 * <td>wantClientAuthentication</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if we would like client certificate authentication (only for
 * the BIO connector type).</td>
 * </tr>
 * </table>
 * 
 * @author Lars Heuer
 * @author Jerome Louvel
 */
public class HttpsServerHelper extends SimpleServerHelper {
	
	/**
	 * This is the SSL context.
	 */
	private SSLContext sslContext;
    
	/**
     * Constructor.
     * 
     * @param server
     *                The server to help.
     */
    public HttpsServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTPS);
    }

    /**
     * Returns the SSL certificate algorithm.
     * 
     * @return The SSL certificate algorithm.
     */
    public String getCertAlgorithm() {
        return getHelpedParameters().getFirstValue("certAlgorithm", "SunX509");
    }

    /**
     * Returns the SSL key password.
     * 
     * @return The SSL key password.
     */
    public String getKeyPassword() {
        return getHelpedParameters().getFirstValue("keyPassword", "");
    }

    /**
     * Returns the SSL keystore password.
     * 
     * @return The SSL keystore password.
     */
    public String getKeystorePassword() {
        return getHelpedParameters().getFirstValue("keystorePassword", "");
    }

    /**
     * Returns the SSL keystore path.
     * 
     * @return The SSL keystore path.
     */
    public String getKeystorePath() {
        return getHelpedParameters().getFirstValue("keystorePath",
                System.getProperty("user.home") + File.separator + ".keystore");
    }

    /**
     * Returns the SSL keystore type.
     * 
     * @return The SSL keystore type.
     */
    public String getKeystoreType() {
        return getHelpedParameters().getFirstValue("keystoreType", "JKS");
    }

    /**
     * Returns the SSL keystore type.
     * 
     * @return The SSL keystore type.
     */
    public String getSslProtocol() {
        return getHelpedParameters().getFirstValue("sslProtocol", "TLS");
    }

    /**
     * Indicates if we require client certificate authentication.
     * 
     * @return True if we require client certificate authentication.
     */
    public boolean isNeedClientAuthentication() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "needClientAuthentication", "false"));
    }

    /**
     * Indicates if we would like client certificate authentication.
     * 
     * @return True if we would like client certificate authentication.
     */
    public boolean isWantClientAuthentication() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "wantClientAuthentication", "false"));
    }
    
    /**
     * Gets the SSL context used by this server.
     * 
     * @return this returns the SSL context.
     */
    public SSLContext getSslContext() {
    	return sslContext;
    }

    /**
     * Sets the SSL context for the server.
     * 
     * @param sslContext the SSL context
     */
    public void setSslContext(SSLContext sslContext) {
    	this.sslContext = sslContext;
    }
    
    /** Starts the Restlet. */
    @Override
    public void start() throws Exception {
        // Initialize the SSL context
        final SslContextFactory sslContextFactory = HttpsUtils
                .getSslContextFactory(this);
        SSLContext sslContext;
        /*
         * If an SslContextFactory has been set up, its settings take priority
         * over the other parameters (which are otherwise used to build and
         * initialise an SSLContext).
         */
        if (sslContextFactory == null) {
            final KeyStore keyStore = KeyStore.getInstance(getKeystoreType());
            final FileInputStream fis = getKeystorePath() == null ? null
                    : new FileInputStream(getKeystorePath());
            final char[] password = getKeystorePassword() == null ? null
                    : getKeystorePassword().toCharArray();
            keyStore.load(fis, password);
            if (fis != null) {
                fis.close();
            }

            final KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(getCertAlgorithm());
            keyManagerFactory.init(keyStore, getKeyPassword().toCharArray());

            final TrustManagerFactory trustManagerFactory = TrustManagerFactory
                    .getInstance(getCertAlgorithm());
            trustManagerFactory.init(keyStore);

            sslContext = SSLContext.getInstance(getSslProtocol());
            sslContext.init(keyManagerFactory.getKeyManagers(),
                    trustManagerFactory.getTrustManagers(), null);
        } else {
            sslContext = sslContextFactory.createSslContext();
        }

        final String addr = getHelped().getAddress();
        if (addr != null) {
            // This call may throw UnknownHostException and otherwise always
            // returns an instance of INetAddress.
            // Note: textual representation of inet addresses are supported
            final InetAddress iaddr = InetAddress.getByName(addr);

            // Note: the backlog of 50 is the default
            setAddress(new InetSocketAddress(iaddr, getHelped().getPort()));
        } else {
        	int port = getHelped().getPort();
        	
        	// Use ephemeral port
        	if(port > 0) {
        		setAddress(new InetSocketAddress(getHelped().getPort()));
        	}
        }

        // Complete initialization
        final Container container = new SimpleContainer(this);
        final ContainerServer server = new ContainerServer(container, getDefaultThreads());
        final SimpleServer filter = new SimpleServer(server);
        final Connection connection = new SocketConnection(filter);
        
        setSslContext(sslContext);
        setConfidential(true);
        setContainer(server);
        setConnection(connection);
        
        InetSocketAddress address = (InetSocketAddress)getConnection().connect(getAddress(), getSslContext());
        setEphemeralPort(address.getPort());
        super.start();
    }

}
