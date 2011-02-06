/**
 * Copyright 2005-2011 Noelios Technologies.
 * 
 * The contents of this file are subject to the terms of one of the following
 * open source licenses: LGPL 3.0 or LGPL 2.1 or CDDL 1.0 or EPL 1.0 (the
 * "Licenses"). You can select the license that you prefer but you may not use
 * this file except in compliance with one of these Licenses.
 * 
 * You can obtain a copy of the LGPL 3.0 license at
 * http://www.opensource.org/licenses/lgpl-3.0.html
 * 
 * You can obtain a copy of the LGPL 2.1 license at
 * http://www.opensource.org/licenses/lgpl-2.1.php
 * 
 * You can obtain a copy of the CDDL 1.0 license at
 * http://www.opensource.org/licenses/cddl1.php
 * 
 * You can obtain a copy of the EPL 1.0 license at
 * http://www.opensource.org/licenses/eclipse-1.0.php
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

package org.restlet.engine.security;

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
 * following the behavior described in the JSSE reference guide.
 * </p>
 * <p>
 * There is more information in the <a href=
 * "http://java.sun.com/j2se/1.5.0/docs/guide/security/jsse/JSSERefGuide.html"
 * >JSSE Reference Guide</a>.
 * </p>
 * 
 * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
 * @see SSLContext
 * @see KeyStore
 */
public class DefaultSslContextFactory extends SslContextFactory {
    /**
     * Name of the KeyManager algorithm.
     */
    private volatile String keyManagerAlgorithm = System.getProperty(
            "ssl.KeyManagerFactory.algorithm", KeyManagerFactory
                    .getDefaultAlgorithm());

    /**
     * Password for the key in the keystore (as a String).
     */
    private volatile char[] keyStoreKeyPassword = (System.getProperty(
            "javax.net.ssl.keyPassword", System
                    .getProperty("javax.net.ssl.keyStorePassword")) != null) ? System
            .getProperty("javax.net.ssl.keyPassword",
                    System.getProperty("javax.net.ssl.keyStorePassword"))
            .toCharArray()
            : null;

    /**
     * Password for the keystore (as a String).
     */
    private volatile char[] keyStorePassword = (System
            .getProperty("javax.net.ssl.keyStorePassword") != null) ? System
            .getProperty("javax.net.ssl.keyStorePassword").toCharArray() : null;

    /**
     * Path to the KeyStore file.
     */
    private volatile String keyStorePath = System
            .getProperty("javax.net.ssl.keyStore");

    /**
     * Name of the keystore provider.
     */
    private volatile String keyStoreProvider = System
            .getProperty("javax.net.ssl.keyStoreProvider");

    /**
     * KeyStore type of the keystore.
     */
    private volatile String keyStoreType = System
            .getProperty("javax.net.ssl.keyStoreType");

    /**
     * Name of the SecureRandom algorithm.
     */
    private volatile String secureRandomAlgorithm = null;

    /**
     * Name of the protocol to use when creating the SSLContext.
     */
    private volatile String secureSocketProtocol = "TLS";

    /**
     * Name of the TrustManager algorithm.
     */
    private volatile String trustManagerAlgorithm = System.getProperty(
            "ssl.TrustManagerFactory.algorithm", TrustManagerFactory
                    .getDefaultAlgorithm());

    /**
     * Password for the trust store keystore.
     */
    private volatile char[] trustStorePassword = (System
            .getProperty("javax.net.ssl.trustStorePassword") != null) ? System
            .getProperty("javax.net.ssl.trustStorePassword").toCharArray()
            : null;

    /**
     * Path to the trust store (keystore) file.
     */
    private volatile String trustStorePath = System
            .getProperty("javax.net.ssl.trustStore");

    /**
     * Name of the trust store (keystore) provider.
     */
    private volatile String trustStoreProvider = System
            .getProperty("javax.net.ssl.trustStoreProvider");

    /**
     * KeyStore type of the trust store.
     */
    private volatile String trustStoreType = System
            .getProperty("javax.net.ssl.trustStoreType");

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
     * Creates a configured and initialised SSLContext from the values set via
     * the various setters of this class. If <code>keyStorePath</code>,
     * <code>keyStoreProvider</code>, <code>keyStoreType</code> are all
     * <code>null</code>, the SSLContext will be initialised with a
     * <code>null</core> array of <code>KeyManager</code>s. Similarly, if
     * <code>trustStorePath</code>, <code>trustStoreProvider</code>,
     * <code>trustStoreType</code> are all <code>null</code>, a
     * <code>null</code> array of <code>TrustManager</code>s will be used.
     * 
     * @see SSLContext#init(javax.net.ssl.KeyManager[],
     *      javax.net.ssl.TrustManager[], SecureRandom)
     */
    @Override
    public SSLContext createSslContext() throws Exception {

        KeyManagerFactory kmf = null;
        if ((this.keyStorePath != null) || (this.keyStoreProvider != null)
                || (this.keyStoreType != null)) {
            /*
             * Loads the key store.
             */
            final KeyStore keyStore = (this.keyStoreProvider != null) ? KeyStore
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
            final KeyStore trustStore = (this.trustStoreProvider != null) ? KeyStore
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

            /*
             * Creates the trust-manager factory.
             */
            tmf = TrustManagerFactory.getInstance(this.trustManagerAlgorithm);
            tmf.init(trustStore);
        }

        /*
         * Creates the SSLContext.
         */
        final SSLContext sslContext = SSLContext
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
     * Returns the secure socket protocol name, "TLS" by default.
     * 
     * @return The secure socket protocol.
     */
    public String getSecureSocketProtocol() {
        return this.secureSocketProtocol;
    }

    /**
     * Sets the following options according to parameters that may have been set
     * up directly in the HttpsServerHelper parameters.
     * <table>
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
     *            Typically, the parameters that would have been obtained from
     *            HttpsServerHelper.getParameters()
     * 
     */
    @Override
    public void init(Series<Parameter> helperParameters) {
        setKeyStorePath(helperParameters.getFirstValue("keystorePath", System
                .getProperty("javax.net.ssl.keyStore")));
        setKeyStorePassword(helperParameters.getFirstValue("keystorePassword",
                System.getProperty("javax.net.ssl.keyStorePassword", "")));
        setKeyStoreType(helperParameters.getFirstValue("keystoreType", System
                .getProperty("javax.net.ssl.keyStoreType")));
        setKeyStoreKeyPassword(helperParameters.getFirstValue("keyPassword",
                System.getProperty("javax.net.ssl.keyPassword")));
        if (this.keyStoreKeyPassword == null) {
            this.keyStoreKeyPassword = this.keyStorePassword;
        }

        setTrustStorePath(helperParameters.getFirstValue("truststorePath",
                System.getProperty("javax.net.ssl.trustStore")));
        setTrustStorePassword(helperParameters.getFirstValue(
                "truststorePassword", System
                        .getProperty("javax.net.ssl.trustStorePassword")));
        setTrustStoreType(helperParameters.getFirstValue("truststoreType",
                System.getProperty("javax.net.ssl.trustStoreType")));

        setKeyManagerAlgorithm(helperParameters.getFirstValue("certAlgorithm",
                "SunX509"));
        setSecureSocketProtocol(helperParameters.getFirstValue("sslProtocol",
                "TLS"));
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
     *            The password of the key in the keystore.
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
     *            Sets the keystore password.
     */
    public final void setKeyStorePassword(char[] keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    /**
     * Sets the keystore password. The default value is that of the
     * <i>javax.net.ssl.keyStorePassword</i> system property.
     * 
     * @param keyStorePassword
     *            Sets the keystore password.
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
     *            The path to the keystore file.
     */
    public final void setKeyStorePath(String keyStorePath) {
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
    public final void setKeyStoreType(String keyStoreType) {
        this.keyStoreType = keyStoreType;
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
     * @param secureSocketProtocol
     *            Name of the secure socket protocol to use.
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
    public final void setTrustStorePassword(char[] trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    /**
     * Sets the password of the trust store KeyStore. The default value is that
     * of the <i>javax.net.ssl.trustStorePassword</i> system property.
     * 
     * @param trustStorePassword
     *            The password of the trust store KeyStore.
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
     *            The trustStorePath to set
     */
    public final void setTrustStorePath(String trustStorePath) {
        this.trustStorePath = trustStorePath;
    }

    /**
     * Sets the name of the trust store provider. The default value is that of
     * the <i>javax.net.ssl.trustStoreProvider</i> system property.
     * 
     * @param trustStoreProvider
     *            The name of the trust store provider.
     */
    public final void setTrustStoreProvider(String trustStoreProvider) {
        this.trustStoreProvider = trustStoreProvider;
    }

    /**
     * Sets the KeyStore type of the trust store. The default value is that of
     * the <i>javax.net.ssl.trustStoreType</i> system property.
     * 
     * @param trustStoreType
     *            The KeyStore type of the trust store.
     */
    public final void setTrustStoreType(String trustStoreType) {
        this.trustStoreType = trustStoreType;
    }
}
