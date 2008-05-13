/*
 * Copyright 2005-2008 Noelios Consulting.
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
package com.noelios.restlet.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import org.restlet.data.Parameter;
import org.restlet.util.Series;

/**
 * This SslContextFactory makes it possible to configure most basic options when
 * building an SSLContext.
 * <p>
 * In short, two instances of KeyStore are used when configuring an SSLContext:
 * the keystore (which contains the public and private keys and certificates to
 * be used locally) and the trust store (which generally holds the CA
 * certificates to be trusted when connecting to a remote host). Both keystore
 * and trust store are KeyStores. When not explicitly set using the setters of
 * this class, the values will default to the default system properties,
 * following the behaviour described in the JSSE reference guide.
 * </p>
 * <p>
 * There is more information in the <a
 * href="http://java.sun.com/j2se/1.5.0/docs/guide/security/jsse/JSSERefGuide.html">JSSE
 * Reference Guide</a>.
 * </p>
 * 
 * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
 * @see SSLContext
 * @see KeyStore
 */
public class DefaultSslContextFactory implements SslContextFactory {
    /**
     * Name of the KeyManager algorithm.
     */
    private String keyManagerAlgorithm = System.getProperty(
            "ssl.KeyManagerFactory.algorithm", "SunX509");

    /**
     * Password for the key in the keystore (as a String).
     */
    private char[] keyStoreKeyPassword = (System.getProperty(
            "javax.net.ssl.keyPassword", System
                    .getProperty("javax.net.ssl.keyStorePassword")) != null) ? System
            .getProperty("javax.net.ssl.keyPassword",
                    System.getProperty("javax.net.ssl.keyStorePassword"))
            .toCharArray()
            : null;

    /**
     * Password for the keystore (as a String).
     */
    private char[] keyStorePassword = (System
            .getProperty("javax.net.ssl.keyStorePassword") != null) ? System
            .getProperty("javax.net.ssl.keyStorePassword").toCharArray() : null;

    /**
     * Path to the KeyStore file.
     */
    private String keyStorePath = System.getProperty("javax.net.ssl.keyStore");

    /**
     * Name of the keystore provider.
     */
    private String keyStoreProvider = System
            .getProperty("javax.net.ssl.keyStoreProvider");

    /**
     * KeyStore type of the keystore.
     */
    private String keyStoreType = System
            .getProperty("javax.net.ssl.keyStoreType");

    /**
     * Name of the SecureRandom algorithm.
     */
    private String secureRandomAlgorithm = null;

    /**
     * Name of the protocol to use when creating the SSLContext.
     */
    private String secureSocketProtocol = "TLS";

    /**
     * Name of the TrustManager algorithm.
     */
    private String trustManagerAlgorithm = System.getProperty(
            "ssl.TrustManagerFactory.algorithm", "PKIX");

    /**
     * Password for the trust store keystore.
     */
    private char[] trustStorePassword = (System
            .getProperty("javax.net.ssl.trustStorePassword") != null) ? System
            .getProperty("javax.net.ssl.trustStorePassword").toCharArray()
            : null;

    /**
     * Path to the trust store (keystore) file.
     */
    private String trustStorePath = System
            .getProperty("javax.net.ssl.trustStore");

    /**
     * Name of the trust store (keystore) provider.
     */
    private String trustStoreProvider = System
            .getProperty("javax.net.ssl.trustStoreProvider");

    /**
     * KeyStore type of the trust store.
     */
    private String trustStoreType = System
            .getProperty("javax.net.ssl.trustStoreType");

    /**
     * Creates a configured and initialised SSLContext from the values set via
     * the various setters of this class. If <code>keyStorePath</code>,
     * <code>keyStoreProvider</code>, <code>keyStoreType</code> are all
     * <code>null</code>, the SSLContext will be initialised with a
     * <code>null</core> array of <code>KeyManager</code>s. Similarly, if
     * <code>trustStorePath</code>, <code>trustStoreProvider</code>, <code>trustStoreType</code> are all <code>null</code>,
     * a <code>null</code> array of <code>TrustManager</code>s will be used.
     * @see SSLContext#init(javax.net.ssl.KeyManager[], javax.net.ssl.TrustManager[], SecureRandom)
     */
    public SSLContext createSslContext() throws Exception {

        KeyManagerFactory kmf = null;
        if ((this.keyStorePath != null) || (this.keyStoreProvider != null)
                || (this.keyStoreType != null)) {
            /*
             * Loads the key store.
             */
            KeyStore keyStore = (this.keyStoreProvider != null) ? KeyStore
                    .getInstance(this.keyStoreType, this.keyStoreProvider)
                    : KeyStore.getInstance(this.keyStoreType);
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

            /*
             * Creates the key-manager factory.
             */
            kmf = KeyManagerFactory.getInstance(this.keyManagerAlgorithm);
            kmf.init(keyStore, this.keyStoreKeyPassword);
        }

        TrustManagerFactory tmf = null;
        if ((this.trustStorePath != null) || (this.trustStoreProvider != null)
                || (this.trustStoreType != null)) {
            /*
             * Loads the trust store.
             */
            KeyStore trustStore = (this.trustStoreProvider != null) ? KeyStore
                    .getInstance(this.trustStoreType, this.trustStoreProvider)
                    : KeyStore.getInstance(this.trustStoreType);

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

            /*
             * Creates the trust-manager factory.
             */
            tmf = TrustManagerFactory.getInstance(this.trustManagerAlgorithm);
            tmf.init(trustStore);
        }

        /*
         * Creates the SSLContext.
         */
        SSLContext sslContext = SSLContext
                .getInstance(this.secureSocketProtocol);
        SecureRandom sr = null;
        if (this.secureRandomAlgorithm != null) {
            sr = SecureRandom.getInstance(this.secureRandomAlgorithm);
        }
        sslContext.init(kmf != null ? kmf.getKeyManagers() : null,
                tmf != null ? tmf.getTrustManagers() : null, sr);

        return sslContext;
    }

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
     * Returns the secure socket protocol name, "TLS" by default.
     * 
     * @return the secure socket protocol.
     */
    public String getSecureSocketProtocol() {
        return this.secureSocketProtocol;
    }

    /**
     * Sets the following options according to parameters that may have been set
     * up directly in the HttpsServerHelper parameters. <table>
     * <tr>
     * <th>Setter of this class</th>
     * <th>Parameter name</th>
     * <th>Value type</th>
     * <th>Default value</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>setKeyStorePath</td>
     * <td>keystorePath</td>
     * <td>String</td>
     * <td>${user.home}/.keystore</td>
     * <td>SSL keystore path.</td>
     * </tr>
     * <tr>
     * <td>setKeyStorePassword</td>
     * <td>keystorePassword</td>
     * <td>String</td>
     * <td></td>
     * <td>SSL keystore password.</td>
     * </tr>
     * <tr>
     * <td>setKeyStoreType</td>
     * <td>keystoreType</td>
     * <td>String</td>
     * <td>JKS</td>
     * <td>SSL keystore type</td>
     * </tr>
     * <tr>
     * <td>setKeyStoreKeyPassword</td>
     * <td>keyPassword</td>
     * <td>String</td>
     * <td></td>
     * <td>SSL key password.</td>
     * </tr>
     * <tr>
     * <td>setKeyManagerAlgorithm</td>
     * <td>certAlgorithm</td>
     * <td>String</td>
     * <td>SunX509</td>
     * <td>SSL certificate algorithm.</td>
     * </tr>
     * <tr>
     * <td>setSecureSocketProtocol</td>
     * <td>sslProtocol</td>
     * <td>String</td>
     * <td>TLS</td>
     * <td>SSL protocol.</td>
     * </tr>
     * </table>
     * 
     * @param helperParameters
     *                typically, the parameters that would have been obtained
     *                from HttpsServerHelper.getParameters()
     * 
     */
    public void init(Series<Parameter> helperParameters) {
        setKeyStorePath(helperParameters.getFirstValue("keystorePath", System
                .getProperty("user.home")
                + File.separator + ".keystore"));
        setKeyStorePassword(helperParameters.getFirstValue("keystorePassword",
                ""));
        setKeyStoreType(helperParameters.getFirstValue("keystoreType", "JKS"));
        setKeyStoreKeyPassword(helperParameters
                .getFirstValue("keyPassword", ""));
        setKeyManagerAlgorithm(helperParameters.getFirstValue("certAlgorithm",
                "SunX509"));
        setSecureSocketProtocol(helperParameters.getFirstValue("sslProtocol",
                "TLS"));
    }

    /**
     * Sets the KeyManager algorithm. The default value is that of the
     * <i>ssl.KeyManagerFactory.algorithm</i> system property, or <i>"SunX509"</i>
     * if the system property has not been set up.
     * 
     * @param keyManagerAlgorithm
     *                the KeyManager algorithm.
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
     *                the password of the key in the keystore.
     */
    public final void setKeyStoreKeyPassword(char[] keyStoreKeyPassword) {
        this.keyStoreKeyPassword = keyStoreKeyPassword;
    }

    /**
     * Sets the password of the key in the keystore. The default value is that
     * of the <i>javax.net.ssl.keyPassword</i> system property, falling back to
     * <i>javax.net.ssl.keyStorePassword</i>. This system property name is not
     * standard.
     * 
     * @param keyStoreKeyPassword
     *                the password of the key in the keystore.
     */
    public final void setKeyStoreKeyPassword(String keyStoreKeyPassword) {
        this.keyStoreKeyPassword = (keyStoreKeyPassword != null) ? keyStoreKeyPassword
                .toCharArray()
                : null;
    }

    /**
     * Sets the keystore password. The default value is that of the
     * <i>javax.net.ssl.keyStorePassword</i> system property.
     * 
     * @param keyStorePassword
     *                Sets the keystore password.
     */
    public final void setKeyStorePassword(char[] keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * Sets the keystore password. The default value is that of the
     * <i>javax.net.ssl.keyStorePassword</i> system property.
     * 
     * @param keyStorePassword
     *                Sets the keystore password.
     */
    public final void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = (keyStorePassword != null) ? keyStorePassword
                .toCharArray() : null;
    }

    /**
     * Sets the path to the keystore file. The default value is that of the
     * <i>javax.net.ssl.keyStore</i> system property.
     * 
     * @param keyStorePath
     *                the path to the keystore file.
     */
    public final void setKeyStorePath(String keyStorePath) {
        this.keyStorePath = keyStorePath;
    }

    /**
     * Sets the name of the keystore provider. The default value is that of the
     * <i>javax.net.ssl.keyStoreProvider</i> system property.
     * 
     * @param keyStoreProvider
     *                the name of the keystore provider.
     */
    public void setKeyStoreProvider(String keyStoreProvider) {
        this.keyStoreProvider = keyStoreProvider;
    }

    /**
     * Sets the KeyStore type of the keystore. The default value is that of the
     * <i>javax.net.ssl.keyStoreType</i> system property.
     * 
     * @param keyStoreType
     *                the KeyStore type of the keystore.
     */
    public final void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
    }

    /**
     * Sets the SecureRandom algorithm. The default value is <i>null</i>, in
     * which case the default SecureRandom would be used.
     * 
     * @param secureRandomAlgorithm
     *                the SecureRandom algorithm.
     */
    public void setSecureRandomAlgorithm(String secureRandomAlgorithm) {
        this.secureRandomAlgorithm = secureRandomAlgorithm;
    }

    /**
     * Sets the secure socket protocol name, "TLS" by default. Typically, this
     * will be either "TLS" or "SSLv3". This is the name used when instantiating
     * the SSLContext.
     * 
     * @param secureSocketProtocol
     *                name of the secure socket protocol to use.
     */
    public void setSecureSocketProtocol(String secureSocketProtocol) {
        this.secureSocketProtocol = secureSocketProtocol;
    }

    /**
     * Sets the TrustManager algorithm. The default value is that of the
     * <i>ssl.TrustManagerFactory.algorithm</i> system property, or
     * <i>"SunX509"</i> if the system property has not been set up.
     * 
     * @param trustManagerAlgorithm
     *                the TrustManager algorithm.
     */
    public void setTrustManagerAlgorithm(String trustManagerAlgorithm) {
        this.trustManagerAlgorithm = trustManagerAlgorithm;
    }

    /**
     * Sets the password of the trust store KeyStore. The default value is that
     * of the <i>javax.net.ssl.trustStorePassword</i> system property.
     * 
     * @param trustStorePassword
     *                the password of the trust store KeyStore.
     */
    public final void setTrustStorePassword(char[] trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * Sets the password of the trust store KeyStore. The default value is that
     * of the <i>javax.net.ssl.trustStorePassword</i> system property.
     * 
     * @param trustStorePassword
     *                the password of the trust store KeyStore.
     */
    public final void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = (trustStorePassword != null) ? trustStorePassword
                .toCharArray()
                : null;
    }

    /**
     * Sets the path to the trust store KeyStore. The default value is that of
     * the <i>javax.net.ssl.trustStore</i> system property.
     * 
     * @param trustStorePath
     *                the trustStorePath to set
     */
    public final void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    /**
     * Sets the name of the trust store provider. The default value is that of
     * the <i>javax.net.ssl.trustStoreProvider</i> system property.
     * 
     * @param trustStoreProvider
     *                the name of the trust store provider.
     */
    public final void setTrustStoreProvider(String trustStoreProvider) {
        this.trustStoreProvider = trustStoreProvider;
    }

    /**
     * Sets the KeyStore type of the trust store. The default value is that of
     * the <i>javax.net.ssl.trustStoreType</i> system property.
     * 
     * @param trustStoreType
     *                the KeyStore type of the trust store.
     */
    public final void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }
}
