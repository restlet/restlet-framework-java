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
 * http://www.noelios.com/products/restlet-engine
 * 
 * Restlet is a registered trademark of Noelios Technologies.
 */

package org.restlet.engine.http;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.logging.Level;

import javax.net.SocketFactory;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.restlet.Client;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.engine.Engine;


/**
 * HTTP client helper based on BIO sockets. Here is the list of parameters that are
 * supported:
 * <table>
 * <tr>
 * <th>Parameter name</th>
 * <th>Value type</th>
 * <th>Default value</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td>tcpNoDelay</td>
 * <td>boolean</td>
 * <td>false</td>
 * <td>Indicate if Nagle's TCP_NODELAY algorithm should be used.</td>
 * </tr>
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
 * <td>truststoreType</td>
 * <td>String</td>
 * <td>null</td>
 * <td>Trust store type</td>
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
 * <td>null</td>
 * <td>Trust store password</td>
 * </tr>
 * </table>
 * 
 * @author Jerome Louvel
 */
public class StreamClientHelper extends HttpClientHelper {
    /**
     * Constructor.
     * 
     * @param client
     *            The client to help.
     */
    public StreamClientHelper(Client client) {
        super(client);
        getProtocols().add(Protocol.HTTP);
        getProtocols().add(Protocol.HTTPS);
    }

    @Override
    public HttpClientCall create(Request request) {
        request.getClientInfo().setAgent(Engine.VERSION_HEADER);
        
        return new StreamClientCall(this, request, createSocketFactory(request.isConfidential()));
    }

    /**
     * Indicates if the protocol will use Nagle's algorithm
     * 
     * @return True to enable TCP_NODELAY, false to disable.
     * @see java.net.Socket#setTcpNoDelay(boolean)
     */
    public boolean getTcpNoDelay() {
        return Boolean.parseBoolean(getHelpedParameters().getFirstValue(
                "tcpNoDelay", "false"));
    }

    @Override
    public synchronized void start() throws Exception {
        super.start();
        getLogger().info("Starting the HTTP client");
    }
    
    @Override
    public synchronized void stop() throws Exception {
        super.stop();
        getLogger().info("Stopping the HTTP client");
    }
    
    protected SocketFactory createSocketFactory(boolean secure) {
        if (secure) {
            try {
                return createSecureSocketFactory();
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Could not create secure socket factory: "+ex.getMessage(), ex);
            } catch (GeneralSecurityException ex) {
                getLogger().log(Level.SEVERE, "Could not create secure socket factory: "+ex.getMessage(), ex);
            }
        }
        return SocketFactory.getDefault();
    }
    
    /**
     * The code below is heavily based on {@link org.mortbay.jetty.security.SslSocketConnector#createFactory()}
     *  
     * @return Properly configured secure socket factory
     * @throws IOException
     * @throws GeneralSecurityException
     */
    protected SocketFactory createSecureSocketFactory() throws IOException, GeneralSecurityException {
        String keystorePath = getKeystorePath();
        String keystorePassword = getKeystorePassword();
        String keyPassword = getKeyPassword();
        String truststorePath = getTruststorePath();
        String truststorePassword = getTruststorePassword();
        String secureRandomAlgorithm = getSecureRandomAlgorithm();
        String securityProvider = getSecurityProvider();
        
        InputStream keystoreInputStream = null;
        if (keystorePath != null) {
            keystoreInputStream = new FileInputStream(keystorePath);
        }
        
        KeyStore keyStore = KeyStore.getInstance(getKeystoreType());
        keyStore.load(keystoreInputStream, keystorePassword == null ? null : keystorePassword.toCharArray());

        KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(getCertAlgorithm());        
        keyManagerFactory.init(keyStore, keyPassword == null ? null : keyPassword.toCharArray());
        KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();

        InputStream truststoreInputStream = null;
        if (truststorePath != null) {
            truststoreInputStream = new FileInputStream(truststorePath);
        }
        KeyStore trustStore = KeyStore.getInstance(getTruststoreType());
        trustStore.load(truststoreInputStream, truststorePassword == null ? null : truststorePassword.toCharArray());
        
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(getCertAlgorithm());
        trustManagerFactory.init(trustStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();

        SecureRandom secureRandom = secureRandomAlgorithm == null ? null : SecureRandom.getInstance(secureRandomAlgorithm);

        SSLContext context = securityProvider == null ? SSLContext.getInstance(getSslProtocol()) : SSLContext.getInstance(getSslProtocol(), securityProvider);
        context.init(keyManagers, trustManagers, secureRandom);
        return context.getSocketFactory();
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
     * Returns the SSL keystore path.
     * 
     * @return The SSL keystore path.
     */
    public String getKeystorePath() {
        return getHelpedParameters().getFirstValue("keystorePath",
                System.getProperty("user.home") + File.separator + ".keystore");
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
     * Returns the SSL truststore type.
     * 
     * @return The SSL truststore type.
     */
    public String getTruststoreType() {
        return getHelpedParameters().getFirstValue("truststoreType", null);
    }
    
    /**
     * Returns the SSL truststore path.
     * 
     * @return The SSL truststore path.
     */
    public String getTruststorePath() {
        return getHelpedParameters().getFirstValue("truststorePath", null);
    }
    
    /**
     * Returns the SSL truststore password.
     * 
     * @return The SSL truststore password.
     */
    public String getTruststorePassword() {
        return getHelpedParameters().getFirstValue("truststorePassword", "");
    }
}
