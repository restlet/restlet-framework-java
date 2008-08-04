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

package com.noelios.restlet.ext.jetty;

import java.io.File;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.mortbay.jetty.AbstractConnector;
import org.mortbay.jetty.security.SslSelectChannelConnector;
import org.mortbay.jetty.security.SslSocketConnector;
import org.restlet.Server;
import org.restlet.data.Protocol;

import com.noelios.restlet.http.HttpsUtils;
import com.noelios.restlet.util.SslContextFactory;

/**
 * Jetty HTTPS server connector. Here is the list of additional parameters that
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
 * <td>secureRandomAlgorithm</td>
 * <td>String</td>
 * <td>null (see java.security.SecureRandom)</td>
 * <td>Name of the RNG algorithm. (see java.security.SecureRandom class).</td>
 * </tr>
 * <tr>
 * <td>securityProvider</td>
 * <td>String</td>
 * <td>null (see javax.net.ssl.SSLContext)</td>
 * <td>Java security provider name (see java.security.Provider class).</td>
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
 * <tr>
 * <td>type</td>
 * <td>int</td>
 * <td>2</td>
 * <td>The type of Jetty connector to use.<br>
 * 1 : Selecting NIO connector (Jetty's SslSelectChannelConnector class).<br>
 * 2 : Blocking BIO connector (Jetty's SslSocketConnector class).</td>
 * </tr>
 * </table>
 * 
 * @see <a
 *      href="http://docs.codehaus.org/display/JETTY/How+to+configure+SSL">How
 *      to configure SSL for Jetty< /a>
 * @author Jerome Louvel (contact@noelios.com)
 */
public class HttpsServerHelper extends JettyServerHelper {
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
     * Creates a new internal Jetty connector.
     * 
     * @return A new internal Jetty connector.
     */
    @Override
    protected AbstractConnector createConnector() {
        AbstractConnector result = null;
        final SslContextFactory sslContextFactory = HttpsUtils
                .getSslContextFactory(this);

        final String[] excludedCipherSuites = HttpsUtils
                .getExcludedCipherSuites(this);

        // Create and configure the Jetty HTTP connector
        switch (getType()) {
        case 1:
            // Selecting NIO connector
            /*
             * If an SslContextFactory has been set up, its settings take
             * priority over the other parameters (which would otherwise be used
             * to build and initialise an SSLContext internally). Jetty's
             * SslSelectChannelConnector does not have a setSslContext method
             * yet, so we override its createSSLContext() method for this
             * purpose.
             */
            SslSelectChannelConnector nioResult;
            if (sslContextFactory == null) {
                nioResult = new SslSelectChannelConnector();
                nioResult.setKeyPassword(getKeyPassword());
                nioResult.setKeystore(getKeystorePath());
                nioResult.setKeystoreType(getKeystoreType());
                nioResult.setPassword(getKeystorePassword());
                nioResult.setProtocol(getSslProtocol());
                nioResult.setProvider(getSecurityProvider());
                nioResult.setSecureRandomAlgorithm(getSecureRandomAlgorithm());
                nioResult.setSslKeyManagerFactoryAlgorithm(getCertAlgorithm());
                nioResult
                        .setSslTrustManagerFactoryAlgorithm(getCertAlgorithm());
                nioResult.setTrustPassword(getKeystorePassword());
            } else {
                nioResult = new SslSelectChannelConnector() {
                    @Override
                    protected SSLContext createSSLContext() throws Exception {
                        return sslContextFactory.createSslContext();
                    }
                };
            }

            if (isNeedClientAuthentication()) {
                nioResult.setNeedClientAuth(true);
            } else if (isWantClientAuthentication()) {
                nioResult.setWantClientAuth(true);
            }

            if (excludedCipherSuites != null) {
                nioResult.setExcludeCipherSuites(excludedCipherSuites);
            }

            result = nioResult;
            break;
        case 2:
            // Blocking BIO connector
            /*
             * If an SslContextFactory has been set up, its settings take
             * priority over the other parameters (which would otherwise be used
             * to build and initialise an SSLContext internally). Jetty's
             * SslSocketConnector does not have a setSslContext method yet, so
             * we override its createFactory() method for this purpose.
             */
            SslSocketConnector bioResult;
            if (sslContextFactory == null) {
                bioResult = new SslSocketConnector();
                bioResult.setKeyPassword(getKeyPassword());
                bioResult.setKeystore(getKeystorePath());
                bioResult.setKeystoreType(getKeystoreType());
                bioResult.setPassword(getKeystorePassword());
                bioResult.setProtocol(getSslProtocol());
                bioResult.setProvider(getSecurityProvider());
                bioResult.setSecureRandomAlgorithm(getSecureRandomAlgorithm());
                bioResult.setSslKeyManagerFactoryAlgorithm(getCertAlgorithm());
                bioResult
                        .setSslTrustManagerFactoryAlgorithm(getCertAlgorithm());
                bioResult.setTrustPassword(getKeystorePassword());
            } else {
                bioResult = new SslSocketConnector() {
                    @Override
                    protected SSLServerSocketFactory createFactory()
                            throws Exception {
                        final SSLContext sslContext = sslContextFactory
                                .createSslContext();
                        return sslContext.getServerSocketFactory();
                    }

                };
            }

            if (isNeedClientAuthentication()) {
                bioResult.setNeedClientAuth(true);
            } else if (isWantClientAuthentication()) {
                bioResult.setWantClientAuth(true);
            }

            if (excludedCipherSuites != null) {
                bioResult.setExcludeCipherSuites(excludedCipherSuites);
            }

            result = bioResult;
            break;
        }

        return result;
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
     * Returns the name of the RNG algorithm.
     * 
     * @return The name of the RNG algorithm.
     */
    public String getSecureRandomAlgorithm() {
        return getHelpedParameters().getFirstValue("secureRandomAlgorithm",
                null);
    }

    /**
     * Returns the Java security provider name.
     * 
     * @return The Java security provider name.
     */
    public String getSecurityProvider() {
        return getHelpedParameters().getFirstValue("securityProvider", null);
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
     * Returns the type of Jetty connector to use.
     * 
     * @return The type of Jetty connector to use.
     */
    public int getType() {
        return Integer.parseInt(getHelpedParameters()
                .getFirstValue("type", "2"));
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
     * Indicates if we would use the NIO-based connector instead of the BIO one.
     * 
     * @return True if we would use the NIO-based connector instead of the BIO
     *         one.
     */
    public boolean isUseNio() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "useNio", "true"));
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
