/**
 * Copyright 2005-2026 Qlik
 *<p>
 * The content of this file is subject to the terms of the Apache 2.0 open
 * source license available at https://www.opensource.org/licenses/apache-2.0
 *<p>
 * Restlet is a registered trademark of QlikTech International AB.
 */
package org.restlet.engine.security;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * Jetty SSL context factory based on a Restlet SSL context one.
 *
 * @author Jerome Louvel
 */
public class RestletSslContextFactoryServer extends SslContextFactory.Server {

    /**
     * Constructor.
     *
     * @param restletSslContextFactory The Restlet SSL context factory to leverage.
     * @throws Exception
     */
    public RestletSslContextFactoryServer(
            org.restlet.engine.ssl.SslContextFactory restletSslContextFactory) throws Exception {
        setSslContext(restletSslContextFactory.createSslContext());
    }

    @Override
    public SSLEngine newSSLEngine() {
        return getSslContext().createSSLEngine();
    }

    @Override
    public SSLEngine newSSLEngine(String host, int port) {
        return getSslContext().createSSLEngine(host, port);
    }

    @Override
    public SSLServerSocket newSslServerSocket(String host, int port, int backlog)
            throws IOException {
        final SSLServerSocketFactory factory = getSslContext().getServerSocketFactory();
        final ServerSocket serverSocket =
                (host == null)
                        ? factory.createServerSocket(port, backlog)
                        : factory.createServerSocket(port, backlog, InetAddress.getByName(host));
        return (SSLServerSocket) serverSocket;
    }

    @Override
    public SSLSocket newSslSocket() throws IOException {
        return (SSLSocket) getSslContext().getSocketFactory().createSocket();
    }
}
