/**
 * Copyright 2005-2012 Restlet S.A.S.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: Apache 2.0 or LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL
 * 1.0 (the "Licenses"). You can select the license that you prefer but you may
 * not use this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the Apache 2.0 license at
 * http://www.opensource.org/licenses/apache-2.0
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0
 * 
 * See the Licenses for the specific language governing permissions and
 * limitations under the Licenses.
 * 
 * Alternatively, you can obtain a royalty free commercial license with less
 * limitations, transferable or non-transferable, directly at
 * http://www.restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.ssl;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;

import org.restlet.data.Parameter;
import org.restlet.ext.ssl.internal.DefaultSslContext;
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
 * <td>certAlgorithm</td>
 * <td>String</td>
 * <td>SunX509</td>
 * <td>SSL certificate algorithm.</td>
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
 * <td>System property "javax.net.ssl.keyStorePassword"</td>
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
 * <td>System property "javax.net.ssl.keyStorePassword"</td>
 * <td>SSL key password.</td>
 * </tr>
 * <tr>
 * <td>needClientAuthentication</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if we require client certificate authentication</td>
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
 * <td>sslProtocol</td>
 * <td>String</td>
 * <td>TLS</td>
 * <td>SSL protocol.</td>
 * </tr>
 * <tr>
 * <td>truststorePath</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Path to trust store</td>
 * </tr>
 * <tr>
 * <td>truststorePassword</td>
 * <td>String</td>
 * <td>System property "javax.net.ssl.trustStorePassword"</td>
 * <td>Trust store password</td>
 * </tr>
 * <tr>
 * <td>truststoreType</td>
 * <td>String</td>
 * <td>System property "javax.net.ssl.trustStoreType"</td>
 * <td>Trust store type</td>
 * </tr>
 * <tr>
 * <td>wantClientAuthentication</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicates if we would like client certificate authentication</td>
 * </tr>
 * </table>
 * <p>
 * In short, two instances of KeyStore are used when configuring an SSLContext:
 * the keystore (which contains the public and private keys and certificates to
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
 * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
 * @see javax.net.ssl.SSLContext
 * @see java.security.KeyStore
 * @see <a href="http://download.oracle.com/javase/1.5.0/docs/guide/security/jsse/JSSERefGuide.html#AppA">JSSE Reference - Standard names</a>
 */
public class DefaultSslContextFactory extends SslContextFactory {

    /** The name of the KeyManager algorithm. */
    private volatile String certAlgorithm = System.getProperty(
            "ssl.KeyManagerFactory.algorithm",
            javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());

    /** The whitespace-separated list of disabled cipher suites. */
    private volatile String[] disabledCipherSuites = null;

    /** The whitespace-separated list of enabled cipher suites. */
    private volatile String[] enabledCipherSuites = null;

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
    private volatile String keyStorePath = System
            .getProperty("javax.net.ssl.keyStore");

    /** The name of the keystore provider. */
    private volatile String keyStoreProvider = System
            .getProperty("javax.net.ssl.keyStoreProvider");

    /** The keyStore type of the keystore. */
    private volatile String keyStoreType = System
            .getProperty("javax.net.ssl.keyStoreType");

    /** Indicates if we require client certificate authentication. */
    private volatile boolean needClientAuthentication = false;

    /** The name of the SecureRandom algorithm. */
    private volatile String secureRandomAlgorithm = null;

    /** The standard name of the protocol to use when creating the SSLContext. */
    private volatile String sslProtocol = "TLS";

    /** The name of the TrustManager algorithm. */
    private volatile String trustManagerAlgorithm = System.getProperty(
            "ssl.TrustManagerFactory.algorithm",
            javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm());

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
                    .getInstance(this.certAlgorithm);
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
                .getInstance(this.sslProtocol);
        SecureRandom sr = null;

        if (this.secureRandomAlgorithm != null) {
            sr = SecureRandom.getInstance(this.secureRandomAlgorithm);
        }

        sslContext.init(kmf != null ? kmf.getKeyManagers() : null,
                tmf != null ? tmf.getTrustManagers() : null, sr);

        // Wraps the SSL context to be able to set cipher suites and other
        // properties after SSL engine creation for example
        result = new DefaultSslContext(this, sslContext);
        return result;
    }

    /**
     * Returns the name of the KeyManager algorithm.
     * 
     * @return The name of the KeyManager algorithm.
     */
    public String getCertAlgorithm() {
        return certAlgorithm;
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
     * Returns the whitespace-separated list of enabled cipher suites.
     * 
     * @return The whitespace-separated list of enabled cipher suites.
     */
    public String[] getEnabledCipherSuites() {
        return enabledCipherSuites;
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
     * Returns the name of the SecureRandom algorithm.
     * 
     * @return The name of the SecureRandom algorithm.
     */
    public String getSecureRandomAlgorithm() {
        return secureRandomAlgorithm;
    }

    /**
     * Returns the secure socket protocol name, "TLS" by default.
     * 
     * @return The secure socket protocol.
     */
    public String getSslProtocol() {
        return this.sslProtocol;
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
     * up directly in the HttpsClientHelper or HttpsServerHelper parameters.
     * <table>
     * <tr>
     * <th>Parameter name</th>
     * <th>Value type</th>
     * <th>Default value</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>enabledCipherSuites</td>
     * <td>String</td>
     * <td>null</td>
     * <td>Whitespace-separated list of enabled cipher suites and/or can be
     * specified multiple times</td>
     * </tr>
     * <tr>
     * <td>disabledCipherSuites</td>
     * <td>String</td>
     * <td>null</td>
     * <td>Whitespace-separated list of disabled cipher suites and/or can be
     * specified multiple times. It affects the cipher suites manually enabled
     * or the default ones.</td>
     * </tr>
     * <tr>
     * <td>keyStorePath</td>
     * <td>String</td>
     * <td>${user.home}/.keystore</td>
     * <td>SSL keystore path.</td>
     * </tr>
     * <tr>
     * <td>keyStorePassword</td>
     * <td>String</td>
     * <td></td>
     * <td>SSL keystore password.</td>
     * </tr>
     * <tr>
     * <td>keyStoreType</td>
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
     * <td>needClientAuthentication</td>
     * <td>boolean</td>
     * <td>false</td>
     * <td>Indicates if we require client certificate authentication</td>
     * </tr>
     * <tr>
     * <td>secureRandomAlgorithm</td>
     * <td>String</td>
     * <td>null (see java.security.SecureRandom)</td>
     * <td>Name of the RNG algorithm. (see java.security.SecureRandom class)</td>
     * </tr>
     * <tr>
     * <td>sslProtocol</td>
     * <td>String</td>
     * <td>TLS</td>
     * <td>SSL protocol.</td>
     * </tr>
     * <tr>
     * <td>truststorePath</td>
     * <td>String</td>
     * <td>null</td>
     * <td>Path to trust store</td>
     * </tr>
     * <tr>
     * <td>truststorePassword</td>
     * <td>String</td>
     * <td>System property "javax.net.ssl.trustStorePassword"</td>
     * <td>Trust store password</td>
     * </tr>
     * <tr>
     * <td>truststoreType</td>
     * <td>String</td>
     * <td>System property "javax.net.ssl.trustStoreType"</td>
     * <td>Trust store type</td>
     * </tr>
     * <tr>
     * <td>wantClientAuthentication</td>
     * <td>boolean</td>
     * <td>false</td>
     * <td>Indicates if we would like client certificate authentication</td>
     * </tr>
     * </table>
     * 
     * @param helperParameters
     *            Typically, the parameters that would have been obtained from
     *            HttpsServerHelper.getParameters()
     */
    @Override
    public void init(Series<Parameter> helperParameters) {
        setKeyStorePath(helperParameters.getFirstValue("keyStorePath", true,
                System.getProperty("javax.net.ssl.keyStore")));
        setKeyStorePassword(helperParameters.getFirstValue("keyStorePassword",
                true, System.getProperty("javax.net.ssl.keyStorePassword", "")));
        setKeyStoreType(helperParameters.getFirstValue("keyStoreType", true,
                System.getProperty("javax.net.ssl.keyStoreType")));
        setKeyStoreKeyPassword(helperParameters.getFirstValue("keyPassword",
                true, System.getProperty("javax.net.ssl.keyPassword")));

        if (this.keyStoreKeyPassword == null) {
            this.keyStoreKeyPassword = this.keyStorePassword;
        }

        setTrustStorePath(helperParameters.getFirstValue("trustStorePath",
                true, System.getProperty("javax.net.ssl.trustStore")));
        setTrustStorePassword(helperParameters.getFirstValue(
                "trustStorePassword",
                System.getProperty("javax.net.ssl.trustStorePassword")));
        setTrustStoreType(helperParameters.getFirstValue("trustStoreType",
                true, System.getProperty("javax.net.ssl.trustStoreType")));

        setCertAlgorithm(helperParameters.getFirstValue("certAlgorithm", true,
                "SunX509"));
        setSslProtocol(helperParameters.getFirstValue("sslProtocol", true,
                "TLS"));

        setNeedClientAuthentication(Boolean.parseBoolean(helperParameters
                .getFirstValue("needClientAuthentication", true, "false")));
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
     * Sets the KeyManager algorithm. The default value is that of the
     * <i>ssl.KeyManagerFactory.algorithm</i> system property, or
     * <i>"SunX509"</i> if the system property has not been set up.
     * 
     * @param keyManagerAlgorithm
     *            The KeyManager algorithm.
     */
    public void setCertAlgorithm(String keyManagerAlgorithm) {
        this.certAlgorithm = keyManagerAlgorithm;
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
     * Sets the whitespace-separated list of enabled cipher suites.
     * 
     * @param enabledCipherSuites
     *            The whitespace-separated list of enabled cipher suites.
     */
    public void setEnabledCipherSuites(String[] enabledCipherSuites) {
        this.enabledCipherSuites = enabledCipherSuites;
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
     * Sets the secure socket protocol name, "TLS" by default. Typically, this
     * will be either "TLS" or "SSLv3". This is the name used when instantiating
     * the SSLContext.
     * 
     * @param sslProtocol
     *            Name of the secure socket protocol to use.
     */
    public void setSslProtocol(String sslProtocol) {
        this.sslProtocol = sslProtocol;
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
