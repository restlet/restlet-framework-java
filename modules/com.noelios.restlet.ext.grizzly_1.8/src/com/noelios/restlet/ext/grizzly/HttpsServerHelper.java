/*
 * Copyright 2005-2008 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the "License"). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * http://www.opensource.org/licenses/cddl1.txt See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL HEADER in each file and
 * include the License file at http://www.opensource.org/licenses/cddl1.txt If
 * applicable, add the following below this CDDL HEADER, with the fields
 * enclosed by brackets "[]" replaced with your own identifying information:
 * Portions Copyright [yyyy] [name of copyright owner]
 */

package com.noelios.restlet.ext.grizzly;

import java.io.File;
import java.io.FileInputStream;
import java.net.InetAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

import org.restlet.Server;
import org.restlet.data.Protocol;

import com.noelios.restlet.http.HttpsUtils;
import com.noelios.restlet.util.SslContextFactory;
import com.sun.grizzly.Controller;
import com.sun.grizzly.DefaultProtocolChain;
import com.sun.grizzly.DefaultProtocolChainInstanceHandler;
import com.sun.grizzly.ProtocolChain;
import com.sun.grizzly.TCPSelectorHandler;
import com.sun.grizzly.filter.SSLReadFilter;

/**
 * HTTPS connector based on Grizzly. Here is the list of additional parameters
 * that are supported: <table>
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
 * <td>Let you specify a {@link SslContextFactory} class name as a parameter,
 * or an instance as an attribute for a more complete and flexible SSL context
 * setting. If set, it takes precedance over the other SSL parameters below.</td>
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
 * <tr>
 * <td>needClientAuthentication</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if we require client certificate authentication.</td>
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
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpsServerHelper extends GrizzlyServerHelper {

    /**
     * Constructor.
     * 
     * @param server
     *                The helped server.
     */
    public HttpsServerHelper(Server server) {
        super(server);
        getProtocols().add(Protocol.HTTPS);
    }

    @Override
    protected void configure(Controller controller) throws Exception {
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
            final FileInputStream fis = new FileInputStream(getKeystorePath());
            keyStore.load(fis, getKeystorePassword().toCharArray());
            final KeyManagerFactory keyManagerFactory = KeyManagerFactory
                    .getInstance(getCertAlgorithm());
            keyManagerFactory.init(keyStore, getKeyPassword().toCharArray());
            sslContext = SSLContext.getInstance(getSslProtocol());
            sslContext.init(keyManagerFactory.getKeyManagers(), null, null);
        } else {
            sslContext = sslContextFactory.createSslContext();
        }

        // Create and configure a select handler
        final TCPSelectorHandler selectorHandler = new TCPSelectorHandler();

        // Create the Grizzly filters
        final SSLReadFilter readFilter = new SSLReadFilter();
        readFilter.setSSLContext(sslContext);

        final String[] enabledCipherSuites = HttpsUtils
                .getEnabledCipherSuites(this);
        if (enabledCipherSuites != null) {
            readFilter.setEnabledCipherSuites(enabledCipherSuites);
        }

        if (isNeedClientAuthentication()) {
            readFilter.setNeedClientAuth(isNeedClientAuthentication());
        } else if (isWantClientAuthentication()) {
            readFilter.setWantClientAuth(isWantClientAuthentication());
        }

        selectorHandler.setPort(getHelped().getPort());
        if (getHelped().getAddress() != null) {
            selectorHandler.setInet(InetAddress.getByName(getHelped()
                    .getAddress()));
        }
        final HttpParserFilter httpParserFilter = new HttpParserFilter(this);

        // Create the Grizzly controller
        controller.setSelectorHandler(selectorHandler);
        controller
                .setProtocolChainInstanceHandler(new DefaultProtocolChainInstanceHandler() {
                    @Override
                    public ProtocolChain poll() {
                        ProtocolChain protocolChain = this.protocolChains
                                .poll();
                        if (protocolChain == null) {
                            protocolChain = new DefaultProtocolChain();
                            protocolChain.addFilter(readFilter);
                            protocolChain.addFilter(httpParserFilter);
                        }
                        return protocolChain;
                    }
                });
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

}
