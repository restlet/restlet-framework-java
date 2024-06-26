/**
 * Copyright 2005-2024 Qlik
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
 * https://restlet.talend.com/
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
public class RestletSslContextFactory extends SslContextFactory {

    /**
     * Constructor.
     * 
     * @param restletSslContextFactory
     *            The Restlet SSL context factory to leverage.
     * @throws Exception
     */
    public RestletSslContextFactory(
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
        SSLServerSocketFactory factory = getSslContext()
                .getServerSocketFactory();
        return (SSLServerSocket) ((host == null) ? factory.createServerSocket(
                port, backlog) : factory.createServerSocket(port, backlog,
                InetAddress.getByName(host)));
    }

    @Override
    public SSLSocket newSslSocket() throws IOException {
        return (SSLSocket) getSslContext().getSocketFactory().createSocket();
    }
}
