/**
 * Copyright 2005-2014 Restlet
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
 * http://restlet.com/products/restlet-framework
 * 
 * Restlet is a registered trademark of Restlet S.A.S.
 */

package org.restlet.ext.jetty.internal;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.restlet.Context;

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
    public void checkKeyStore() {
        try {
            if (getSslContext() == null) {
                super.checkKeyStore();
            }
        } catch (IllegalStateException e) {
            Context.getCurrentLogger().log(Level.FINE,
                    "Unable to check Jetty SSL keystore", e);
        }
    }

    @Override
    public SSLEngine newSslEngine() {
        return getSslContext().createSSLEngine();
    }

    @Override
    public SSLEngine newSslEngine(String host, int port) {
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
