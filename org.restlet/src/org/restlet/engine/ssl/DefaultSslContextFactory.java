/**
 * Copyright 2005-2014 Restlet
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or or EPL 1.0 (the "Licenses"). You can
 * select the license that you prefer but you may not use this file except in
 * compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.engine.ssl;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLSocketFactory;

import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * This {@link SslContextFactory} makes it possible to configure most basic
 * options when building an SSLContext. See the {@link #init(Series)} method for
 * the list of parameters supported by this factory when configuring your HTTP
 * client or server connector. Here is the list of SSL related parameters that
 * are also supported:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>disabledCipherSuites</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Whitespace-separated list of disabled cipher suites and/or can be
 * specified multiple times. It affects the cipher suites manually enabled or
 * the default ones.</td>
 * </tr>
 * <tr>
 * <td>disabledProtocols</td>
 * <td>String (see Java Secure Socket Extension (JSSE) reference guide)</td>
 * <td>null</td>
 * <td>Whitespace-separated list of disabled SSL/TLS protocol names and/or can
 * be specified multiple times. Used when creating SSL sockets and engines.</td>
 * </tr>
 * <tr>
 * <td>enabledCipherSuites</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Whitespace-separated list of enabled cipher suites and/or can be
 * specified multiple times</td>
 * </tr>
 * <tr>
 * <td>enabledProtocols</td>
 * <td>String (see Java Secure Socket Extension (JSSE) reference guide)</td>
 * <td>null</td>
 * <td>Whitespace-separated list of enabled SSL/TLS protocol names and/or can be
 * specified multiple times. Used when creating SSL sockets and engines.</td>
 * </tr>
 * <tr>
 * <td>keyManagerAlgorithm</td>
 * <td>String</td>
 * <td>System property "ssl.KeyManagerFactory.algorithm" or "SunX509"</td>
 * <td>Certificate algorithm for the key manager.</td>
 * </tr>
 * <tr>
 * <td>keyStorePath</td>
 * <td>String</td>
 * <td>System property "javax.net.ssl.keyStore" or ${user.home}/.keystore</td>
 * <td>SSL keystore path.</td>
 * </tr>
 * <tr>
 * <td>keyStorePassword</td>
 * <td>String</td>
 * <td>System property "javax.net.ssl.keyStorePassword"</td>
 * <td>SSL keystore password.</td>
 * </tr>
 * <tr>
 * <td>keyStoreType</td>
 * <td>String</td>
 * <td>System property javax.net.ssl.keyStoreType or JKS</td>
 * <td>SSL keystore type</td>
 * </tr>
 * <tr>
 * <td>keyPassword</td>
 * <td>String</td>
 * <td>System property "javax.net.ssl.keyStorePassword"</td>
 * <td>SSL key password.</td>
 * </tr>
 * <tr>
 * <td>needClientAuthentication</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if we require client certificate authentication. If set to
 * 'true', the "wantClientAuthentication" parameter is ignored.</td>
 * </tr>
 * <tr>
 * <td>protocol</td>
 * <td>String</td>
 * <td>TLS (see Java Secure Socket Extension (JSSE) reference guide)</td>
 * <td>SSL protocol used when creating the SSLContext.</td>
 * </tr>
 * <tr>
 * <td>secureRandomAlgorithm</td>
 * <td>String</td>
 * <td>null (see java.security.SecureRandom)</td>
 * <td>Name of the RNG algorithm. (see java.security.SecureRandom class)</td>
 * </tr>
 * <tr>
 * <td>trustManagerAlgorithm</td>
 * <td>String</td>
 * <td>System property "ssl.TrustManagerFactory.algorithm" or "SunX509"</td>
 * <td>Certificate algorithm for the trust manager.</td>
 * </tr>
 * <tr>
 * <td>trustStorePassword</td>
 * <td>String</td>
 * <td>System property "javax.net.ssl.trustStorePassword"</td>
 * <td>Trust store password</td>
 * </tr>
 * <tr>
 * <td>trustStorePath</td>
 * <td>String</td>
 * <td>System property "javax.net.ssl.trustStore"</td>
 * <td>Path to trust store</td>
 * </tr>
 * <tr>
 * <td>trustStoreType</td>
 * <td>String</td>
 * <td>System property "javax.net.ssl.trustStoreType"</td>
 * <td>Trust store type</td>
 * </tr>
 * <tr>
 * <td>wantClientAuthentication</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if we would like client certificate authentication. Only taken
 * into account if the "needClientAuthentication" parameter is 'false'.</td>
 * </tr>
 * </table>
 * <p>
 * In short, two instances of KeyStore are used when configuring an SSLContext:
 * the key store (which contains the public and private keys and certificates to
 * be used locally) and the trust store (which generally holds the CA
 * certificates to be trusted when connecting to a remote host). Both keystore
 * and trust store are KeyStores. When not explicitly set using the setters of
 * this class, the values will default to the default system properties,
 * following the behavior described in the JSSE reference guide.
 * </p>
 * <p>
 * There is more information in the <a href=
 * "http://download.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html"
 * >JSSE Reference Guide</a>.
 * </p>
 * 
 * @author Bruno Harbulot
 * @see javax.net.ssl.SSLContext
 * @see java.security.KeyStore
 * @see <a
 *      href="http://download.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#AppA">JSSE
 *      Reference - Standard names</a>
 */
public class DefaultSslContextFactory extends SslContextFactory {

    /** The whitespace-separated list of disabled cipher suites. */
    private volatile String[] disabledCipherSuites = null;

    /** The whitespace-separated list of disabled SSL protocols. */
    private volatile String[] disabledProtocols = null;

    /** The whitespace-separated list of enabled cipher suites. */
    private volatile String[] enabledCipherSuites = null;

    /** The whitespace-separated list of enabled SSL protocols. */
    private volatile String[] enabledProtocols = null;

    /** The name of the KeyManager algorithm. */
    private volatile String keyManagerAlgorithm = System.getProperty(
            "ssl.KeyManagerFactory.algorithm", "SunX509");

    /** The password for the key in the keystore (as a String). */
    private volatile char[] keyStoreKeyPassword = (System.getProperty(
            "javax.net.ssl.keyPassword",
            System.getProperty("javax.net.ssl.keyStorePassword")) != null) ? System
            .getProperty("javax.net.ssl.keyPassword",
                    System.getProperty("javax.net.ssl.keyStorePassword"))
            .toCharArray() : null;

    /** The password for the keystore (as a String). */
    private volatile char[] keyStorePassword = (System
            .getProperty("javax.net.ssl.keyStorePassword") != null) ? System
            .getProperty("javax.net.ssl.keyStorePassword").toCharArray() : null;

    /** The path to the KeyStore file. */
    private volatile String keyStorePath = System.getProperty(
            "javax.net.ssl.keyStore",
            (System.getProperty("user.home") != null) ? ((System
                    .getProperty("user.home").endsWith("/")) ? System
                    .getProperty("user.home") + ".keystore" : System
                    .getProperty("user.home") + "/.keystore") : null);

    /** The name of the keystore provider. */
    private volatile String keyStoreProvider = System
            .getProperty("javax.net.ssl.keyStoreProvider");

    /** The keyStore type of the keystore. */
    private volatile String keyStoreType = System.getProperty(
            "javax.net.ssl.keyStoreType", "JKS");

    /** Indicates if we require client certificate authentication. */
    private volatile boolean needClientAuthentication = false;

    /** The standard name of the protocol to use when creating the SSLContext. */
    private volatile String protocol = "TLS";

    /** The name of the SecureRandom algorithm. */
    private volatile String secureRandomAlgorithm = null;

    /** The name of the TrustManager algorithm. */
    private volatile String trustManagerAlgorithm = System.getProperty(
            "ssl.TrustManagerFactory.algorithm", "SunX509");

    /** The password for the trust store keystore. */
    private volatile char[] trustStorePassword = (System
            .getProperty("javax.net.ssl.trustStorePassword") != null) ? System
            .getProperty("javax.net.ssl.trustStorePassword").toCharArray()
            : null;

    /** The path to the trust store (keystore) file. */
    private volatile String trustStorePath = System
            .getProperty("javax.net.ssl.trustStore");

    /** The name of the trust store (keystore) provider. */
    private volatile String trustStoreProvider = System
            .getProperty("javax.net.ssl.trustStoreProvider");

    /** The KeyStore type of the trust store. */
    private volatile String trustStoreType = System
            .getProperty("javax.net.ssl.trustStoreType");

    /** Indicates if we would like client certificate authentication. */
    private volatile boolean wantClientAuthentication = false;

    /**
     * This class is likely to contain sensitive information; cloning is
     * therefore not allowed.
     */
    @Override
    protected final DefaultSslContextFactory clone()
            throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    /**
     * Creates a configured and initialized SSLContext from the values set via
     * the various setters of this class. If <code>keyStorePath</code>,
     * <code>keyStoreProvider</code>, <code>keyStoreType</code> are all
     * <code>null</code>, the SSLContext will be initialized with a
     * <code>null</core> array of <code>KeyManager</code>s. Similarly, if
     * <code>trustStorePath</code>, <code>trustStoreProvider</code>,
     * <code>trustStoreType</code> are all <code>null</code>, a
     * <code>null</code> array of <code>TrustManager</code>s will be used.
     * 
     * @see SSLContext#init(javax.net.ssl.KeyManager[],
     *      javax.net.ssl.TrustManager[], SecureRandom)
     */
    @Override
    public javax.net.ssl.SSLContext createSslContext() throws Exception {
        javax.net.ssl.SSLContext result = null;
        javax.net.ssl.KeyManagerFactory kmf = null;

        if ((this.keyStorePath != null) || (this.keyStoreProvider != null)
                || (this.keyStoreType != null)) {
            // Loads the key store.
            KeyStore keyStore = (this.keyStoreProvider != null) ? KeyStore
                    .getInstance(
                            (this.keyStoreType != null) ? this.keyStoreType
                                    : KeyStore.getDefaultType(),
                            this.keyStoreProvider)
                    : KeyStore
                            .getInstance((this.keyStoreType != null) ? this.keyStoreType
                                    : KeyStore.getDefaultType());
            FileInputStream keyStoreInputStream = null;

            try {
                keyStoreInputStream = ((this.keyStorePath != null) && (!"NONE"
                        .equals(this.keyStorePath))) ? new FileInputStream(
                        this.keyStorePath) : null;
                keyStore.load(keyStoreInputStream, this.keyStorePassword);
            } finally {
                if (keyStoreInputStream != null) {
                    keyStoreInputStream.close();
                }
            }

            // Creates the key-manager factory.
            kmf = javax.net.ssl.KeyManagerFactory
                    .getInstance(this.keyManagerAlgorithm);
            kmf.init(keyStore, this.keyStoreKeyPassword);
        }

        javax.net.ssl.TrustManagerFactory tmf = null;

        if ((this.trustStorePath != null) || (this.trustStoreProvider != null)
                || (this.trustStoreType != null)) {
            // Loads the trust store.
            KeyStore trustStore = (this.trustStoreProvider != null) ? KeyStore
                    .getInstance(
                            (this.trustStoreType != null) ? this.trustStoreType
                                    : KeyStore.getDefaultType(),
                            this.trustStoreProvider)
                    : KeyStore
                            .getInstance((this.trustStoreType != null) ? this.trustStoreType
                                    : KeyStore.getDefaultType());
            FileInputStream trustStoreInputStream = null;

            try {
                trustStoreInputStream = ((this.trustStorePath != null) && (!"NONE"
                        .equals(this.trustStorePath))) ? new FileInputStream(
                        this.trustStorePath) : null;
                trustStore.load(trustStoreInputStream, this.trustStorePassword);
            } finally {
                if (trustStoreInputStream != null) {
                    trustStoreInputStream.close();
                }
            }

            // Creates the trust-manager factory.
            tmf = javax.net.ssl.TrustManagerFactory
                    .getInstance(this.trustManagerAlgorithm);
            tmf.init(trustStore);
        }

        // Creates the SSL context
        javax.net.ssl.SSLContext sslContext = javax.net.ssl.SSLContext
                .getInstance(this.protocol);
        SecureRandom sr = null;

        if (this.secureRandomAlgorithm != null) {
            sr = SecureRandom.getInstance(this.secureRandomAlgorithm);
        }

        sslContext.init(kmf != null ? kmf.getKeyManagers() : null,
                tmf != null ? tmf.getTrustManagers() : null, sr);

        // Wraps the SSL context to be able to set cipher suites and other
        // properties after SSL engine creation for example
        result = createWrapper(sslContext);
        return result;
    }

    /**
     * Creates a new {@link SSLContext} wrapper. Necessary to properly
     * initialize the {@link SSLEngine} or {@link SSLSocketFactory} or
     * {@link javax.net.ssl.SSLServerSocketFactory} created.
     * 
     * @param sslContext
     *            The SSL context to wrap.
     * @return The SSL context wrapper.
     */
    protected javax.net.ssl.SSLContext createWrapper(
            javax.net.ssl.SSLContext sslContext) {
        return new DefaultSslContext(this, sslContext);
    }

    /**
     * Returns the whitespace-separated list of disabled cipher suites.
     * 
     * @return The whitespace-separated list of disabled cipher suites.
     */
    public String[] getDisabledCipherSuites() {
        return disabledCipherSuites;
    }

    /**
     * Returns the whitespace-separated list of disabled SSL protocols.
     * 
     * @return The whitespace-separated list of disabled SSL protocols.
     */
    public String[] getDisabledProtocols() {
        return disabledProtocols;
    }

    /**
     * Returns the whitespace-separated list of enabled cipher suites.
     * 
     * @return The whitespace-separated list of enabled cipher suites.
     */
    public String[] getEnabledCipherSuites() {
        return enabledCipherSuites;
    }

    /**
     * Returns the whitespace-separated list of enabled SSL protocols.
     * 
     * @return The whitespace-separated list of enabled SSL protocols.
     */
    public String[] getEnabledProtocols() {
        return enabledProtocols;
    }

    /**
     * Returns the name of the KeyManager algorithm.
     * 
     * @return The name of the KeyManager algorithm.
     */
    public String getKeyManagerAlgorithm() {
        return keyManagerAlgorithm;
    }

    /**
     * Returns the password for the key in the keystore (as a String).
     * 
     * @return The password for the key in the keystore (as a String).
     */
    public char[] getKeyStoreKeyPassword() {
        return keyStoreKeyPassword;
    }

    /**
     * Returns the password for the keystore (as a String).
     * 
     * @return The password for the keystore (as a String).
     */
    public char[] getKeyStorePassword() {
        return keyStorePassword;
    }

    /**
     * Returns the path to the KeyStore file.
     * 
     * @return The path to the KeyStore file.
     */
    public String getKeyStorePath() {
        return keyStorePath;
    }

    /**
     * Returns the name of the keystore provider.
     * 
     * @return The name of the keystore provider.
     */
    public String getKeyStoreProvider() {
        return keyStoreProvider;
    }

    /**
     * Returns the keyStore type of the keystore.
     * 
     * @return The keyStore type of the keystore.
     */
    public String getKeyStoreType() {
        return keyStoreType;
    }

    /**
     * Returns the secure socket protocol name, "TLS" by default.
     * 
     * @return The secure socket protocol.
     */
    public String getProtocol() {
        return this.protocol;
    }

    /**
     * Returns the name of the SecureRandom algorithm.
     * 
     * @return The name of the SecureRandom algorithm.
     */
    public String getSecureRandomAlgorithm() {
        return secureRandomAlgorithm;
    }

    /**
     * Returns the selected cipher suites. The selection is the subset of
     * supported suites that are both in the enable suites and out of the
     * disabled suites.
     * 
     * @param supportedCipherSuites
     *            The initial cipher suites to restrict.
     * @return The selected cipher suites.
     */
    public String[] getSelectedCipherSuites(String[] supportedCipherSuites) {
        Set<String> resultSet = new HashSet<String>();

        if (supportedCipherSuites != null) {
            for (String supportedCipherSuite : supportedCipherSuites) {
                if (((getEnabledCipherSuites() == null) || Arrays.asList(
                        getEnabledCipherSuites())
                        .contains(supportedCipherSuite))
                        && ((getDisabledCipherSuites() == null) || !Arrays
                                .asList(getDisabledCipherSuites()).contains(
                                        supportedCipherSuite))) {
                    resultSet.add(supportedCipherSuite);
                }
            }
        }

        String[] result = new String[resultSet.size()];
        return resultSet.toArray(result);
    }

    /**
     * Returns the selected SSL protocols. The selection is the subset of
     * supported protocols whose name starts with the name of of
     * {@link #getEnabledProtocols()} name.
     * 
     * @param supportedProtocols
     *            The selected SSL protocols.
     * @return The selected SSL protocols.
     */
    public String[] getSelectedSslProtocols(String[] supportedProtocols) {
        Set<String> resultSet = new HashSet<String>();

        if (supportedProtocols != null) {
            for (String supportedProtocol : supportedProtocols) {
                if (((getEnabledProtocols() == null) || Arrays.asList(
                        getEnabledProtocols()).contains(supportedProtocol))
                        && ((getDisabledProtocols() == null) || !Arrays.asList(
                                getDisabledProtocols()).contains(
                                supportedProtocol))) {
                    resultSet.add(supportedProtocol);
                }
            }
        }

        String[] result = new String[resultSet.size()];
        return resultSet.toArray(result);
    }

    /**
     * Returns the name of the TrustManager algorithm.
     * 
     * @return The name of the TrustManager algorithm.
     */
    public String getTrustManagerAlgorithm() {
        return trustManagerAlgorithm;
    }

    /**
     * Returns the password for the trust store keystore.
     * 
     * @return The password for the trust store keystore.
     */
    public char[] getTrustStorePassword() {
        return trustStorePassword;
    }

    /**
     * Returns the path to the trust store (keystore) file.
     * 
     * @return The path to the trust store (keystore) file.
     */
    public String getTrustStorePath() {
        return trustStorePath;
    }

    /**
     * Returns the name of the trust store (keystore) provider.
     * 
     * @return The name of the trust store (keystore) provider.
     */
    public String getTrustStoreProvider() {
        return trustStoreProvider;
    }

    /**
     * Returns the KeyStore type of the trust store.
     * 
     * @return The KeyStore type of the trust store.
     */
    public String getTrustStoreType() {
        return trustStoreType;
    }

    /**
     * Sets the following options according to parameters that may have been set
     * up directly in the HttpsClientHelper or HttpsServerHelper parameters. See
     * class Javadocs for the list of parameters supported.
     * 
     * @param helperParameters
     *            Typically, the parameters that would have been obtained from
     *            HttpsServerHelper.getParameters()
     */
    @Override
    public void init(Series<Parameter> helperParameters) {
        // Parses and set the disabled cipher suites
        String[] disabledCipherSuitesArray = helperParameters
                .getValuesArray("disabledCipherSuites");
        Set<String> disabledCipherSuites = new HashSet<String>();

        for (String disabledCipherSuiteSeries : disabledCipherSuitesArray) {
            for (String disabledCipherSuite : disabledCipherSuiteSeries
                    .split(" ")) {
                disabledCipherSuites.add(disabledCipherSuite);
            }
        }

        if (disabledCipherSuites.size() > 0) {
            disabledCipherSuitesArray = new String[disabledCipherSuites.size()];
            disabledCipherSuites.toArray(disabledCipherSuitesArray);
            setDisabledCipherSuites(disabledCipherSuitesArray);
        } else {
            setDisabledCipherSuites(null);
        }

        // Parses and set the disabled protocols
        String[] disabledProtocolsArray = helperParameters
                .getValuesArray("disabledProtocols");
        Set<String> disabledProtocols = new HashSet<String>();

        for (String disabledProtocolsSeries : disabledProtocolsArray) {
            for (String disabledProtocol : disabledProtocolsSeries.split(" ")) {
                disabledProtocols.add(disabledProtocol);
            }
        }

        if (disabledProtocols.size() > 0) {
            disabledProtocolsArray = new String[disabledProtocols.size()];
            disabledProtocols.toArray(disabledProtocolsArray);
            setDisabledProtocols(disabledProtocolsArray);
        } else {
            setDisabledProtocols(null);
        }

        // Parses and set the enabled cipher suites
        String[] enabledCipherSuitesArray = helperParameters
                .getValuesArray("enabledCipherSuites");
        Set<String> enabledCipherSuites = new HashSet<String>();

        for (String enabledCipherSuiteSeries : enabledCipherSuitesArray) {
            for (String enabledCipherSuite : enabledCipherSuiteSeries
                    .split(" ")) {
                enabledCipherSuites.add(enabledCipherSuite);
            }
        }

        if (enabledCipherSuites.size() > 0) {
            enabledCipherSuitesArray = new String[enabledCipherSuites.size()];
            enabledCipherSuites.toArray(enabledCipherSuitesArray);
            setEnabledCipherSuites(enabledCipherSuitesArray);
        } else {
            setEnabledCipherSuites(null);
        }

        // Parses and set the enabled protocols
        String[] enabledProtocolsArray = helperParameters
                .getValuesArray("enabledProtocols");
        Set<String> enabledProtocols = new HashSet<String>();

        for (String enabledProtocolSeries : enabledProtocolsArray) {
            for (String enabledProtocol : enabledProtocolSeries.split(" ")) {
                enabledProtocols.add(enabledProtocol);
            }
        }

        if (enabledProtocols.size() > 0) {
            enabledProtocolsArray = new String[enabledProtocols.size()];
            enabledProtocols.toArray(enabledProtocolsArray);
            setEnabledProtocols(enabledProtocolsArray);
        } else {
            setEnabledProtocols(null);
        }

        setKeyManagerAlgorithm(helperParameters.getFirstValue(
                "keyManagerAlgorithm", true, System.getProperty(
                        "ssl.KeyManagerFactory.algorithm", "SunX509")));
        setKeyStorePassword(helperParameters.getFirstValue("keyStorePassword",
                true, System.getProperty("javax.net.ssl.keyStorePassword", "")));
        setKeyStoreKeyPassword(helperParameters.getFirstValue("keyPassword",
                true, System.getProperty("javax.net.ssl.keyPassword")));

        if (this.keyStoreKeyPassword == null) {
            this.keyStoreKeyPassword = this.keyStorePassword;
        }

        setKeyStorePath(helperParameters.getFirstValue("keyStorePath", true,
                System.getProperty("javax.net.ssl.keyStore")));
        setKeyStoreType(helperParameters.getFirstValue("keyStoreType", true,
                System.getProperty("javax.net.ssl.keyStoreType")));
        setNeedClientAuthentication(Boolean.parseBoolean(helperParameters
                .getFirstValue("needClientAuthentication", true, "false")));
        setProtocol(helperParameters.getFirstValue("protocol", true, "TLS"));
        setSecureRandomAlgorithm(helperParameters.getFirstValue(
                "secureRandomAlgorithm", true));
        setTrustManagerAlgorithm(helperParameters.getFirstValue(
                "trustManagerAlgorithm", true, System.getProperty(
                        "ssl.TrustManagerFactory.algorithm", "SunX509")));
        setTrustStorePassword(helperParameters.getFirstValue(
                "trustStorePassword", true,
                System.getProperty("javax.net.ssl.trustStorePassword")));
        setTrustStorePath(helperParameters.getFirstValue("trustStorePath",
                true, System.getProperty("javax.net.ssl.trustStore")));
        setTrustStoreType(helperParameters.getFirstValue("trustStoreType",
                true, System.getProperty("javax.net.ssl.trustStoreType")));
        setWantClientAuthentication(Boolean.parseBoolean(helperParameters
                .getFirstValue("wantClientAuthentication", true, "false")));
    }

    /**
     * Indicates if we require client certificate authentication.
     * 
     * @return True if we require client certificate authentication.
     */
    public boolean isNeedClientAuthentication() {
        return needClientAuthentication;
    }

    /**
     * Indicates if we would like client certificate authentication.
     * 
     * @return True if we would like client certificate authentication.
     */
    public boolean isWantClientAuthentication() {
        return wantClientAuthentication;
    }

    /**
     * Sets the whitespace-separated list of disabled cipher suites.
     * 
     * @param disabledCipherSuites
     *            The whitespace-separated list of disabled cipher suites.
     */
    public void setDisabledCipherSuites(String[] disabledCipherSuites) {
        this.disabledCipherSuites = disabledCipherSuites;
    }

    /**
     * Sets the whitespace-separated list of disabled SSL protocols.
     * 
     * @param disabledProtocols
     *            The whitespace-separated list of disabled SSL protocols.
     */
    public void setDisabledProtocols(String[] disabledProtocols) {
        this.disabledProtocols = disabledProtocols;
    }

    /**
     * Sets the whitespace-separated list of enabled cipher suites.
     * 
     * @param enabledCipherSuites
     *            The whitespace-separated list of enabled cipher suites.
     */
    public void setEnabledCipherSuites(String[] enabledCipherSuites) {
        this.enabledCipherSuites = enabledCipherSuites;
    }

    /**
     * Sets the standard name of the protocols to use when creating the SSL
     * sockets or engines.
     * 
     * @param enabledProtocols
     *            The standard name of the protocols to use when creating the
     *            SSL sockets or engines.
     */
    public void setEnabledProtocols(String[] enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
    }

    /**
     * Sets the KeyManager algorithm. The default value is that of the
     * <i>ssl.KeyManagerFactory.algorithm</i> system property, or
     * <i>"SunX509"</i> if the system property has not been set up.
     * 
     * @param keyManagerAlgorithm
     *            The KeyManager algorithm.
     */
    public void setKeyManagerAlgorithm(String keyManagerAlgorithm) {
        this.keyManagerAlgorithm = keyManagerAlgorithm;
    }

    /**
     * Sets the password of the key in the keystore. The default value is that
     * of the <i>javax.net.ssl.keyPassword</i> system property, falling back to
     * <i>javax.net.ssl.keyStorePassword</i>. This system property name is not
     * standard.
     * 
     * @param keyStoreKeyPassword
     *            The password of the key in the keystore.
     */
    public void setKeyStoreKeyPassword(char[] keyStoreKeyPassword) {
        this.keyStoreKeyPassword = keyStoreKeyPassword;
    }

    /**
     * Sets the password of the key in the keystore. The default value is that
     * of the <i>javax.net.ssl.keyPassword</i> system property, falling back to
     * <i>javax.net.ssl.keyStorePassword</i>. This system property name is not
     * standard.
     * 
     * @param keyStoreKeyPassword
     *            The password of the key in the keystore.
     */
    public void setKeyStoreKeyPassword(String keyStoreKeyPassword) {
        this.keyStoreKeyPassword = (keyStoreKeyPassword != null) ? keyStoreKeyPassword
                .toCharArray() : null;
    }

    /**
     * Sets the keystore password. The default value is that of the
     * <i>javax.net.ssl.keyStorePassword</i> system property.
     * 
     * @param keyStorePassword
     *            Sets the keystore password.
     */
    public void setKeyStorePassword(char[] keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * Sets the keystore password. The default value is that of the
     * <i>javax.net.ssl.keyStorePassword</i> system property.
     * 
     * @param keyStorePassword
     *            Sets the keystore password.
     */
    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = (keyStorePassword != null) ? keyStorePassword
                .toCharArray() : null;
    }

    /**
     * Sets the path to the keystore file. The default value is that of the
     * <i>javax.net.ssl.keyStore</i> system property.
     * 
     * @param keyStorePath
     *            The path to the keystore file.
     */
    public void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    /**
     * Sets the name of the keystore provider. The default value is that of the
     * <i>javax.net.ssl.keyStoreProvider</i> system property.
     * 
     * @param keyStoreProvider
     *            The name of the keystore provider.
     */
    public void setKeyStoreProvider(String keyStoreProvider) {
        this.keyStoreProvider = keyStoreProvider;
    }

    /**
     * Sets the KeyStore type of the keystore. The default value is that of the
     * <i>javax.net.ssl.keyStoreType</i> system property.
     * 
     * @param keyStoreType
     *            The KeyStore type of the keystore.
     */
    public void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    /**
     * Indicates if we require client certificate authentication. The default
     * value is false.
     * 
     * @param needClientAuthentication
     *            True if we require client certificate authentication.
     */
    public void setNeedClientAuthentication(boolean needClientAuthentication) {
        this.needClientAuthentication = needClientAuthentication;
    }

    /**
     * Sets the secure socket protocol name, "TLS" by default.
     * 
     * @param protocol
     *            Name of the secure socket protocol to use.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Sets the SecureRandom algorithm. The default value is <i>null</i>, in
     * which case the default SecureRandom would be used.
     * 
     * @param secureRandomAlgorithm
     *            The SecureRandom algorithm.
     */
    public void setSecureRandomAlgorithm(String secureRandomAlgorithm) {
        this.secureRandomAlgorithm = secureRandomAlgorithm;
    }

    /**
     * Sets the TrustManager algorithm. The default value is that of the
     * <i>ssl.TrustManagerFactory.algorithm</i> system property, or
     * <i>"SunX509"</i> if the system property has not been set up.
     * 
     * @param trustManagerAlgorithm
     *            The TrustManager algorithm.
     */
    public void setTrustManagerAlgorithm(String trustManagerAlgorithm) {
        this.trustManagerAlgorithm = trustManagerAlgorithm;
    }

    /**
     * Sets the password of the trust store KeyStore. The default value is that
     * of the <i>javax.net.ssl.trustStorePassword</i> system property.
     * 
     * @param trustStorePassword
     *            The password of the trust store KeyStore.
     */
    public void setTrustStorePassword(char[] trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * Sets the password of the trust store KeyStore. The default value is that
     * of the <i>javax.net.ssl.trustStorePassword</i> system property.
     * 
     * @param trustStorePassword
     *            The password of the trust store KeyStore.
     */
    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = (trustStorePassword != null) ? trustStorePassword
                .toCharArray() : null;
    }

    /**
     * Sets the path to the trust store KeyStore. The default value is that of
     * the <i>javax.net.ssl.trustStore</i> system property.
     * 
     * @param trustStorePath
     *            The trustStorePath to set
     */
    public void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    /**
     * Sets the name of the trust store provider. The default value is that of
     * the <i>javax.net.ssl.trustStoreProvider</i> system property.
     * 
     * @param trustStoreProvider
     *            The name of the trust store provider.
     */
    public void setTrustStoreProvider(String trustStoreProvider) {
        this.trustStoreProvider = trustStoreProvider;
    }

    /**
     * Sets the KeyStore type of the trust store. The default value is that of
     * the <i>javax.net.ssl.trustStoreType</i> system property.
     * 
     * @param trustStoreType
     *            The KeyStore type of the trust store.
     */
    public void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }

    /**
     * Indicates if we would like client certificate authentication. The default
     * value is false.
     * 
     * @param wantClientAuthentication
     *            True if we would like client certificate authentication.
     */
    public void setWantClientAuthentication(boolean wantClientAuthentication) {
        this.wantClientAuthentication = wantClientAuthentication;
    }
}
