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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * SSL socket factory that wraps the default one to do extra initialization.
 * Configures the cipher suites and the SSL certificate request.
 * 
 * @author Jerome Louvel
 */
public class WrapperSslSocketFactory extends SSLSocketFactory {

    /** The parent SSL context factory. */
    private final DefaultSslContextFactory contextFactory;

    /** The wrapped SSL server socket factory. */
    private final SSLSocketFactory wrappedSocketFactory;

    /**
     * Constructor.
     * 
     * @param contextFactory
     *            The parent SSL context factory.
     * @param wrappedSocketFactory
     *            The wrapped SSL server socket factory.
     */
    public WrapperSslSocketFactory(DefaultSslContextFactory contextFactory,
            SSLSocketFactory wrappedSocketFactory) {
        this.wrappedSocketFactory = wrappedSocketFactory;
        this.contextFactory = contextFactory;
    }

    @Override
    public Socket createSocket() throws IOException {
        SSLSocket result = (SSLSocket) getWrappedSocketFactory().createSocket();
        return initSslSocket(result);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        SSLSocket result = (SSLSocket) getWrappedSocketFactory().createSocket(
                host, port);
        return initSslSocket(result);
    }

    @Override
    public Socket createSocket(InetAddress host, int port,
            InetAddress localAddress, int localPort) throws IOException {
        SSLSocket result = (SSLSocket) getWrappedSocketFactory().createSocket(
                host, port, localAddress, localPort);
        return initSslSocket(result);
    }

    @Override
    public Socket createSocket(Socket s, String host, int port,
            boolean autoClose) throws IOException {
        SSLSocket result = (SSLSocket) getWrappedSocketFactory().createSocket(
                s, host, port, autoClose);
        return initSslSocket(result);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException,
            UnknownHostException {
        SSLSocket result = (SSLSocket) getWrappedSocketFactory().createSocket(
                host, port);
        return initSslSocket(result);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localAddress,
            int localPort) throws IOException, UnknownHostException {
        SSLSocket result = (SSLSocket) getWrappedSocketFactory().createSocket(
                host, port, localAddress, localPort);
        return initSslSocket(result);
    }

    /**
     * Returns the parent SSL context factory.
     * 
     * @return The parent SSL context factory.
     */
    public DefaultSslContextFactory getContextFactory() {
        return contextFactory;
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return getWrappedSocketFactory().getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return getWrappedSocketFactory().getSupportedCipherSuites();
    }

    /**
     * Returns the wrapped SSL socket factory.
     * 
     * @return The wrapped SSL socket factory.
     */
    public SSLSocketFactory getWrappedSocketFactory() {
        return wrappedSocketFactory;
    }

    /**
     * Initializes the SSL socket. Configures the certificate request (need or
     * want) and the enabled cipher suites.
     * 
     * @param sslSocket
     *            The socket to initialize.
     * @return The initialized socket.
     */
    protected SSLSocket initSslSocket(SSLSocket sslSocket) {
        if (getContextFactory().isNeedClientAuthentication()) {
            sslSocket.setNeedClientAuth(true);
        } else if (getContextFactory().isWantClientAuthentication()) {
            sslSocket.setWantClientAuth(true);
        }

        if ((getContextFactory().getEnabledCipherSuites() != null)
                || (getContextFactory().getDisabledCipherSuites() != null)) {
            sslSocket.setEnabledCipherSuites(getContextFactory()
                    .getSelectedCipherSuites(
                            sslSocket.getSupportedCipherSuites()));
        }

        if ((getContextFactory().getEnabledProtocols() != null)
                || (getContextFactory().getDisabledProtocols() != null)) {
            sslSocket
                    .setEnabledProtocols(getContextFactory()
                            .getSelectedSslProtocols(
                                    sslSocket.getSupportedProtocols()));
        }

        return sslSocket;
    }

}
