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
package com.noelios.restlet.ext.ssl;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;

import jsslutils.keystores.KeyStoreLoader;
import jsslutils.sslcontext.PKIXSSLContextFactory;
import jsslutils.sslcontext.SSLContextFactory.SSLContextFactoryException;

import org.restlet.data.Parameter;
import org.restlet.util.Series;

import com.noelios.restlet.util.DefaultSslContextFactory;
import com.noelios.restlet.util.SslContextFactory;

/**
 * This SslContextFactory uses PKIXSSLContextFactory from <a
 * href="http://code.google.com/p/jsslutils/">jSSLutils</a> and can be
 * configured via parameters.
 * 
 * @author Bruno Harbulot (Bruno.Harbulot@manchester.ac.uk)
 */
public class PkixSslContextFactory extends SslContextFactory {
    private String sslProtocol = "TLS";

    private PKIXSSLContextFactory sslContextFactory;

    /**
     * Creates a configured and initialised SSLContext by delegating the call to
     * the PKIXSSLContextFactory with has been initialised using 'init'.
     * 
     * @see PKIXSSLContextFactory#buildSSLContext()
     */
    @Override
    public SSLContext createSslContext() throws Exception {
        synchronized (this) {
            return this.sslContextFactory.buildSSLContext(this.sslProtocol);
        }
    }

    /**
     * Sets the following options according to parameters that may have been set
     * up directly in the HttpsServerHelper parameters. <table>
     * <tr>
     * <th>Parameter name</th>
     * <th>Value type</th>
     * <th>Default value</th>
     * <th>Description</th>
     * </tr>
     * <tr>
     * <td>keystorePath</td>
     * <td>String</td>
     * <td>javax.net.ssl.keyStore system property</td>
     * <td>SSL keystore path.</td>
     * </tr>
     * <tr>
     * <td>keystorePassword</td>
     * <td>String</td>
     * <td>javax.net.ssl.keyStorePassword system property</td>
     * <td>SSL keystore password.</td>
     * </tr>
     * <tr>
     * <td>keystoreType</td>
     * <td>String</td>
     * <td>javax.net.ssl.keyStoreType system property, otherwise default type</td>
     * <td>SSL keystore type</td>
     * </tr>
     * <tr>
     * <td>keystoreProvider</td>
     * <td>String</td>
     * <td>javax.net.ssl.keyStoreProvider system property, otherwise default
     * provider</td>
     * <td>SSL keystore provider</td>
     * </tr>
     * <tr>
     * <td>keyPassword</td>
     * <td>String</td>
     * <td></td>
     * <td>SSL key password.</td>
     * </tr>
     * <tr>
     * <td>truststorePath</td>
     * <td>String</td>
     * <td>javax.net.ssl.trustStore system property</td>
     * <td>SSL truststore path.</td>
     * </tr>
     * <tr>
     * <td>truststorePassword</td>
     * <td>String</td>
     * <td>javax.net.ssl.trustStorePassword system property</td>
     * <td>SSL truststore password.</td>
     * </tr>
     * <tr>
     * <td>truststoreType</td>
     * <td>String</td>
     * <td>javax.net.ssl.trustStoreType system property, otherwise default type</td>
     * <td>SSL truststore type</td>
     * </tr>
     * <tr>
     * <td>truststoreProvider</td>
     * <td>String</td>
     * <td>javax.net.ssl.trustStoreProvider system property, otherwise default
     * provider</td>
     * <td>SSL truststore provider</td>
     * </tr>
     * <tr>
     * <td>sslProtocol</td>
     * <td>String: TLS/SSLv3</td>
     * <td>TLS</td>
     * <td>SSL protocol</td>
     * </tr>
     * <tr>
     * <td>disableCrl</td>
     * <td>String (true/false)</td>
     * <td>false</td>
     * <td>Set to true if you want not to use the CRLs</td>
     * </tr>
     * <tr>
     * <td>crlUrl</td>
     * <td>String (URL)</td>
     * <td></td>
     * <td>URL of CRL to load (there can be multiple occurrences of this
     * parameter).</td>
     * </tr>
     * </table>
     * 
     * @param helperParameters
     *                typically, the parameters that would have been obtained
     *                from HttpsServerHelper.getParameters()
     * 
     */
    @Override
    public void init(Series<Parameter> parameters) {
        KeyStoreLoader keyStoreLoader = KeyStoreLoader
                .getKeyStoreDefaultLoader();
        String keyStorePath = parameters.getFirstValue("keystorePath");
        if (keyStorePath != null) {
            keyStoreLoader.setKeyStorePath(keyStorePath);
        }
        String keyStorePassword = parameters.getFirstValue("keystorePassword");
        if (keyStorePassword != null) {
            keyStoreLoader.setKeyStorePassword(keyStorePassword);
        }
        String keyStoreType = parameters.getFirstValue("keystoreType");
        if (keyStoreType != null) {
            keyStoreLoader.setKeyStoreType(keyStoreType);
        }
        String keyStoreProvider = parameters.getFirstValue("keystoreProvider");
        if (keyStoreProvider != null) {
            keyStoreLoader.setKeyStoreProvider(keyStoreProvider);
        }

        KeyStoreLoader trustStoreLoader = KeyStoreLoader
                .getKeyStoreDefaultLoader();
        String trustStorePath = parameters.getFirstValue("truststorePath");
        if (trustStorePath != null) {
            trustStoreLoader.setKeyStorePath(trustStorePath);
        }
        String trustStorePassword = parameters
                .getFirstValue("truststorePassword");
        if (trustStorePassword != null) {
            trustStoreLoader.setKeyStorePassword(trustStorePassword);
        }
        String trustStoreType = parameters.getFirstValue("truststoreType");
        if (trustStoreType != null) {
            trustStoreLoader.setKeyStoreType(trustStoreType);
        }
        String trustStoreProvider = parameters
                .getFirstValue("truststoreProvider");
        if (trustStoreProvider != null) {
            trustStoreLoader.setKeyStoreProvider(trustStoreProvider);
        }

        String keyPassword = parameters.getFirstValue("keyPassword", "");

        String sslProtocol = parameters.getFirstValue("sslProtocol");

        boolean disableRevocation = Boolean.parseBoolean(parameters
                .getFirstValue("disableCrl"));

        try {
            KeyStore keyStore = keyStoreLoader.loadKeyStore();
            KeyStore trustStore = trustStoreLoader.loadKeyStore();

            PKIXSSLContextFactory sslContextFactory = new PKIXSSLContextFactory(
                    keyStore, keyPassword, trustStore, !disableRevocation);

            String[] crlArray = parameters.getValuesArray("crlUrl");
            if (crlArray != null) {
                for (String crlUrl : crlArray) {
                    sslContextFactory.addCrl(crlUrl);
                }
            }

            synchronized (this) {
                this.sslContextFactory = sslContextFactory;
                if (sslProtocol != null) {
                    this.sslProtocol = sslProtocol;
                }
            }
        } catch (KeyStoreException e) {
            throw new RuntimeException(e);
        } catch (NoSuchProviderException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SSLContextFactoryException e) {
            throw new RuntimeException(e);
        }
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
}
