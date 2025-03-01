/**
 * Copyright 2005-2024 Qlik
 * 
 * The contents of this file is subject to the terms of the Apache 2.0 open
 * source license available at http://www.opensource.org/licenses/apache-2.0
 * 
 * Restlet is a registered trademark of QlikTech International AB.
 */

package org.restlet.ext.jetty.internal;

import java.io.IOException;
import java.net.InetAddress;

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
     * @param restletSslContextFactory
     *            The Restlet SSL context factory to leverage.
     * @throws Exception
     */
    public RestletSslContextFactoryServer(
            org.restlet.engine.ssl.SslContextFactory restletSslContextFactory)
            throws Exception {
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
        SSLServerSocketFactory factory = getSslContext().getServerSocketFactory();
        return (SSLServerSocket) ((host == null)
                ? factory.createServerSocket(port, backlog)
                : factory.createServerSocket(port, backlog, InetAddress.getByName(host)));
    }

    @Override
    public SSLSocket newSslSocket() throws IOException {
        return (SSLSocket) getSslContext().getSocketFactory().createSocket();
    }
}
