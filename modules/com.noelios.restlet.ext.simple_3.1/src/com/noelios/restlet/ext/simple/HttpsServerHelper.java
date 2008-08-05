/**
 * Copyright 2005-2008 Noelios Technologies.
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
 * Alternatively, you can obtain a royaltee free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.noelios.com/products/restlet-engine/.
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package com.noelios.restlet.ext.simple;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManagerFactory;

import org.restlet.Server;
import org.restlet.data.Protocol;

import simple.http.PipelineHandlerFactory;
import simple.http.connect.ConnectionFactory;

import com.noelios.restlet.http.HttpsUtils;
import com.noelios.restlet.util.SslContextFactory;

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
 * @author Lars Heuer (heuer[at]semagia.com)
 * @author Jerome Louvel
 */
public class HttpsServerHelper extends SimpleServerHelper {
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

        // Initialize the socket
        SSLServerSocket serverSocket = null;
        final String addr = getHelped().getAddress();
        if (addr != null) {
            // this call may throw UnknownHostException and otherwise always
            // returns an instance of INetAddress
            // Note: textual representation of inet addresses are supported
            final InetAddress iaddr = InetAddress.getByName(addr);
            // Note: the backlog of 50 is the default
            serverSocket = (SSLServerSocket) sslContext
                    .getServerSocketFactory().createServerSocket(
                            getHelped().getPort(), 50, iaddr);
        } else {
            serverSocket = (SSLServerSocket) sslContext
                    .getServerSocketFactory().createServerSocket(
                            getHelped().getPort());
        }

        if (isNeedClientAuthentication()) {
            serverSocket.setNeedClientAuth(true);
        } else if (isWantClientAuthentication()) {
            serverSocket.setWantClientAuth(true);
        }

        /*
         * Gets the list of enabled and excluded cipher suites. If excluded
         * cipher suites are specified, they are removed from the list of
         * enabled cipher suites (which is the default one if none is
         * specified).
         */
        String[] enabledCipherSuites = HttpsUtils.getEnabledCipherSuites(this);
        String[] excludedCipherSuites = HttpsUtils
                .getDisabledCipherSuites(this);
        if (excludedCipherSuites != null) {
            if (enabledCipherSuites == null) {
                enabledCipherSuites = serverSocket.getEnabledCipherSuites();
            }
            List<String> enabledCipherSuitesList = new ArrayList<String>(Arrays
                    .asList(enabledCipherSuites));
            for (String excludedCipherSuite : excludedCipherSuites) {
                enabledCipherSuitesList.remove(excludedCipherSuite);
            }
            enabledCipherSuites = enabledCipherSuitesList
                    .toArray(enabledCipherSuites);
        }
        if (enabledCipherSuites != null) {
            serverSocket.setEnabledCipherSuites(enabledCipherSuites);
        }

        serverSocket.setSoTimeout(60000);
        setSocket(serverSocket);

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

}
